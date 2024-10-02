/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.services.ejb.timer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerHandle;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.drools.core.time.JobHandle;
import org.drools.core.time.impl.TimerJobInstance;
import org.jbpm.persistence.timer.GlobalJpaTimerJobInstance;
import org.jbpm.process.core.timer.TimerServiceRegistry;
import org.kie.internal.runtime.manager.SessionNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Startup
@Lock(LockType.READ)
public class EJBTimerScheduler {

	private static final Logger logger = LoggerFactory.getLogger(EJBTimerScheduler.class);

    private static final Long TIMER_RETRY_INTERVAL = Long.parseLong(System.getProperty("org.kie.jbpm.timer.retry.interval", "5000"));

    private static final Integer TIMER_RETRY_LIMIT = Integer.parseInt(System.getProperty("org.kie.jbpm.timer.retry.limit", "3"));

	private static final Integer OVERDUE_WAIT_TIME = Integer.parseInt(System.getProperty("org.jbpm.overdue.timer.wait", "20000"));
	
	private static final Integer OVERDUE_CHECK_TIME = Integer.parseInt(System.getProperty("org.jbpm.overdue.timer.check", "200"));

	private boolean useLocalCache = Boolean.parseBoolean(System.getProperty("org.jbpm.ejb.timer.local.cache", "false"));

	private ConcurrentMap<String, TimerJobInstance> localCache = new ConcurrentHashMap<String, TimerJobInstance>();

	@Resource
    protected TimerService timerService;
	
    @Resource
    protected SessionContext ctx;
    
    public void setUseLocalCache(boolean useLocalCache) {
        this.useLocalCache = useLocalCache;
    }

	@PostConstruct
	public void setup() {
	    // disable auto init of timers since ejb timer service supports persistence of timers
	    System.setProperty("org.jbpm.rm.init.timer", "false");
	    logger.info("Using local cache for EJB timers: {}", useLocalCache);
	}

	@Timeout
	public void executeTimerJob(Timer timer) {
        EjbTimerJob timerJob = (EjbTimerJob) timer.getInfo();
        TimerJobInstance timerJobInstance = timerJob.getTimerJobInstance();
        logger.debug("About to execute timer for job {}", timerJob);
        // handle overdue timers as ejb timer service might start before all deployments are ready
        long time = 0;
        try {
            while (TimerServiceRegistry.getInstance().get(((EjbGlobalJobHandle) timerJobInstance.getJobHandle()).getDeploymentId()) == null) {
                logger.debug("waiting for timer service to be available, elapsed time {} ms", time);
                Thread.sleep(OVERDUE_CHECK_TIME);
                time += OVERDUE_CHECK_TIME;
                if (time > OVERDUE_WAIT_TIME) {
                    logger.debug("No timer service found after waiting {} ms", time);
                    break;
                }
            }
        } catch (InterruptedException e) {
            logger.warn("Thread has been interrupted", e);
            Thread.currentThread().interrupt();
        }        
        try {
            executeTimerJobInstance(timerJobInstance);
        } catch (Exception e) {
        	logger.error("Error executing timer handle {}", timerJobInstance.getJobHandle(), e);
            recoverTimerJobInstance(timerJob, timer, e);
        }
    }

    private void executeTimerJobInstance(TimerJobInstance timerJobInstance) throws Exception {
        ((Callable<?>) timerJobInstance).call();
    }

    private void recoverTimerJobInstance(EjbTimerJob ejbTimerJob, Timer timer,  Exception cause) {
        Transaction<TimerJobInstance> tx;
        if (isSessionNotFound(cause)) {
            // if session is not found means the process has already finished. In this case we just need to remove
            // the timer and avoid any recovery as it should not trigger any more timers.
            tx = timerJobInstance -> {
                logger.warn("Trying to recover timer. Not possible due to process instance is not found. More likely already completed. Timer {} won't be recovered", timerJobInstance, cause);
                if (!removeJob(timerJobInstance.getJobHandle(), timer)) {
                    logger.warn("Session not found for timer {}. Timer could not removed.", timerJobInstance);
                }
            };
        }
        else if (ejbTimerJob.getTimerJobInstance().getTrigger().hasNextFireTime() != null) {
            // this is an interval trigger. Problem here is that the timer scheduled by DefaultTimerJobInstance is lost
            // because of the transaction, so we need to do this here.
            tx = timerJobInstance -> {
                logger.warn("Execution of time failed Interval Trigger failed. Skipping {}", timerJobInstance);
                if (removeJob(timerJobInstance.getJobHandle(), timer)) {
                    internalSchedule(timerJobInstance);
                } else {
                    logger.debug("Interval trigger {} was removed before rescheduling", timerJobInstance);
                }
            };
        }
        else {
            // if there is not next date to be fired, we need to apply policy otherwise will be lost
            tx = timerJobInstance -> {
                logger.warn("Execution of time failed. The timer will be retried {}", timerJobInstance);
                ZonedDateTime nextRetry = ZonedDateTime.now().plus(TIMER_RETRY_INTERVAL, ChronoUnit.MILLIS);
                EjbTimerJobRetry info = ejbTimerJob instanceof EjbTimerJobRetry ? ((EjbTimerJobRetry) ejbTimerJob).next() : new EjbTimerJobRetry(timerJobInstance);
                if (TIMER_RETRY_LIMIT > 0 && info.getRetry() > TIMER_RETRY_LIMIT) {
                    logger.warn("The timer {} reached retry limit {}. It won't be retried again", timerJobInstance, TIMER_RETRY_LIMIT);
                } else {
                    TimerConfig config = new TimerConfig(info, true);
                    Timer newTimer = timerService.createSingleActionTimer(Date.from(nextRetry.toInstant()), config);
                    ((GlobalJpaTimerJobInstance) timerJobInstance).setTimerInfo(newTimer.getHandle());
                    ((GlobalJpaTimerJobInstance) timerJobInstance).setExternalTimerId(getPlatformTimerId(newTimer));
                }
            };
        }
        try {
            invokeTransaction (tx, ejbTimerJob.getTimerJobInstance());
        } catch (Exception e) {
            logger.error("Failed to executed timer recovery", e);
        }
    }

    private boolean isSessionNotFound(Exception e) {
        Throwable current = e;
        do {
            if (current instanceof SessionNotFoundException) {
                return true;
            }
            current = current.getCause();
        } while (current != null);
        return false;
    }

    @FunctionalInterface
    private interface Transaction<I> {
        void doWork(I item) throws Exception;
    }

    @TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
    public <I> void transaction(Transaction<I> operation, I item) throws Exception {
        try {
            operation.doWork(item);
        } catch (Exception transactionEx) {
            ctx.setRollbackOnly();
            throw transactionEx;
        }
    }
    
    private <I> void invokeTransaction (Transaction<I> operation, I item) throws Exception {
        ctx.getBusinessObject(EJBTimerScheduler.class).transaction(operation,item);
    }

    public void internalSchedule(TimerJobInstance timerJobInstance) {
        Serializable info = removeTransientFields(new EjbTimerJob(timerJobInstance));
        TimerConfig config = new TimerConfig(info, true);
        Date expirationTime = timerJobInstance.getTrigger().hasNextFireTime();
        logger.debug("Timer expiration date is {}", expirationTime);
        if (expirationTime != null) {
            Timer timer = timerService.createSingleActionTimer(expirationTime, config);
            TimerHandle handle = timer.getHandle();
            ((GlobalJpaTimerJobInstance) timerJobInstance).setTimerInfo(handle);
            logger.debug("Timer scheduled {} on {} scheduler service", timerJobInstance);
            ((GlobalJpaTimerJobInstance) timerJobInstance).setExternalTimerId(getPlatformTimerId(timer));
            if (useLocalCache) {
                localCache.putIfAbsent(((EjbGlobalJobHandle) timerJobInstance.getJobHandle()).getUuid(), timerJobInstance);
            }
        } else {
            logger.info("Timer that was to be scheduled has already expired");
        }
    }


    private String getPlatformTimerId(Timer timer) {
        try {
            Method method = timer.getClass().getMethod("getId");
            return (String) method.invoke(timer);
        } catch (Exception timerIdException) {
            logger.trace("Failed to get the platform timer id {}", timerIdException.getMessage(), timerIdException);
            return null;
        }
    }

    private Serializable removeTransientFields(Serializable info) {
        // removing transient fields
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(info);
            out.flush();
            ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            return (Serializable) stream.readObject();
        } catch (IOException io) {
            logger.warn("Not possible to serialize the timer info", io);
        } catch (ClassNotFoundException cnf) {
            logger.warn("Class not found in class loader", cnf);
        }
        return info;
    }

    private boolean disableLinearSearch(String suffix) {
        return Boolean.getBoolean("org.jbpm.ejb.timer.disable.linear." + suffix);
    }

    
    public boolean removeJob(JobHandle jobHandle, Timer ejbTimer) {
		EjbGlobalJobHandle ejbHandle = (EjbGlobalJobHandle) jobHandle;
        if (useLocalCache) {
            boolean removedFromCache = localCache.remove(ejbHandle.getUuid()) != null;
            logger.debug("Job handle {} is {} removed from cache ", jobHandle, removedFromCache ? "" : "not");
        }

        if (ejbTimer != null) {
            return cancelTimer(ejbTimer, ejbHandle);
        }

        // small speed improvement using the ejb serializable info handler
        GlobalJpaTimerJobInstance timerJobInstance = (GlobalJpaTimerJobInstance) ejbHandle.getTimerJobInstance();
        if (timerJobInstance != null) {
            Object ejbTimerHandle = timerJobInstance.getTimerInfo();
            if (ejbTimerHandle instanceof TimerHandle) {
                try {
                    ((TimerHandle) ejbTimerHandle).getTimer().cancel();
                    return true;
                } catch (Exception ex) {
                    logger.warn("Cancelling timer failed for handle {}", ejbHandle, ex);
                    return false;
                }
            }
        }
        logger.debug("No valid TimerJob instance {} available for Job handle {}", timerJobInstance, ejbHandle);
        return linearSearch("remove", ejbHandle.getUuid(),
                (timer, job) -> cancelTimer(timer, (EjbGlobalJobHandle) job.getJobHandle())).orElse(false);
    }

    public TimerJobInstance getTimerByName(String jobName) {
        if (useLocalCache) {
            TimerJobInstance found = localCache.get(jobName);
            if (found != null) {
                logger.debug("Found timer job instance with  name {} in cache, returning {}", jobName, found);
                return found;
            }
            logger.debug("Timer Job Instance with name {} not found in cache", jobName);
        }
        return linearSearch("search", jobName, (timer, job) -> {
            if (useLocalCache  && job != null) {
                localCache.putIfAbsent(jobName, job);
            }
            return job;
        }).orElse(null);
    }


    private boolean cancelTimer(Timer timer, EjbGlobalJobHandle ejbHandle) {
        try {
            timer.cancel();
            return true;
        } catch (Exception ex) {
            logger.warn("Cancelling timer failed for handle {}", ejbHandle, ex);
            return false;
        }
    }

    private <T> Optional<T> linearSearch(String suffix, String uuid, BiFunction<Timer, TimerJobInstance, T> function) {
        if (disableLinearSearch(suffix)) {
            logger.warn("Skipping linear search to {} UUID {}", suffix, uuid);
        } else {
            logger.info("Searching UUID {} on {} scheduler service", uuid, this);
            for (Timer timer : timerService.getTimers()) {
                try {
                    Serializable info = timer.getInfo();
                    if (info instanceof EjbTimerJob) {
                        EjbTimerJob job = (EjbTimerJob) info;
                        EjbGlobalJobHandle handle = (EjbGlobalJobHandle) job.getTimerJobInstance().getJobHandle();
                        if (handle.getUuid().equals(uuid)) {
                            logger.debug("UIID {} does match timer {} and handle {}", uuid, timer,
                                    job.getTimerJobInstance());
                            return Optional.ofNullable(function.apply(timer, job.getTimerJobInstance()));
                        }
                    }
                } catch (NoSuchObjectLocalException e) {
                    logger.info("Info for timer {} is not there ", timer, e);
                }
            }
            logger.info("UUID {} does not match any timer on {} scheduler service", uuid, this);
        }
        return Optional.empty();
    }

    public void evictCache(JobHandle jobHandle) {
        String jobName =  ((EjbGlobalJobHandle) jobHandle).getUuid();
        logger.debug("Invalidate job {} with job name {} in cache", localCache.remove(jobName), jobName);
    }

}
