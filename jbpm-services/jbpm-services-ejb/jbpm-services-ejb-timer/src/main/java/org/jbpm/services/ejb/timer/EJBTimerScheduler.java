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
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerHandle;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.RollbackException;
import javax.transaction.UserTransaction;

import org.drools.core.time.JobHandle;
import org.drools.core.time.impl.TimerJobInstance;
import org.jbpm.persistence.timer.GlobalJpaTimerJobInstance;
import org.jbpm.process.core.timer.TimerServiceRegistry;
import org.kie.internal.runtime.manager.SessionNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@TransactionManagement(TransactionManagementType.BEAN)
@Lock(LockType.READ)
public class EJBTimerScheduler {

	private static final Logger logger = LoggerFactory.getLogger(EJBTimerScheduler.class);

    private static final Long TIMER_RETRY_INTERVAL = Long.parseLong(System.getProperty("org.kie.jbpm.timer.retry.interval", "5000"));

    private static final Integer TIMER_RETRY_LIMIT = Integer.parseInt(System.getProperty("org.kie.jbpm.timer.retry.limit", "3"));

	private static final Integer OVERDUE_WAIT_TIME = Integer.parseInt(System.getProperty("org.jbpm.overdue.timer.wait", "20000"));

	private boolean useLocalCache = Boolean.parseBoolean(System.getProperty("org.jbpm.ejb.timer.local.cache", "false"));

	private ConcurrentMap<String, TimerJobInstance> localCache = new ConcurrentHashMap<String, TimerJobInstance>();

	@Resource
	protected javax.ejb.TimerService timerService;

    @Resource
    protected UserTransaction utx;

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

        String timerServiceId = ((EjbGlobalJobHandle) timerJobInstance.getJobHandle()).getDeploymentId();

        // handle overdue timers as ejb timer service might start before all deployments are ready
        long time = 0;
        while (TimerServiceRegistry.getInstance().get(timerServiceId) == null) {
            logger.debug("waiting for timer service to be available, elapsed time {} ms", time);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            time += 500;

            if (time > OVERDUE_WAIT_TIME) {
                logger.debug("No timer service found after waiting {} ms", time);
                break;
            }
        }
        try {
            transaction(this::executeTimerJobInstance, timerJobInstance);
        } catch (SessionNotFoundException e) {
            logger.warn("Process instance is not found. More likely already completed. Timer {} won't be recovered", timerJobInstance, e);
            removeUnrecoverableTimer(timerJob, timer);
        } catch (Exception e) {
            recoverTimerJobInstance(timerJob, timer, e);
        }
    }

    private void executeTimerJobInstance(TimerJobInstance timerJobInstance) throws Exception {
        try {
            ((Callable<?>) timerJobInstance).call();
        } catch (Exception e) {
            throw e;
        }
    }

    private void removeUnrecoverableTimer(EjbTimerJob ejbTimerJob, Timer timer) {
        try {
            Transaction<TimerJobInstance> tx = timerJobInstance -> {
                if (!this.removeJob(timerJobInstance.getJobHandle(), timer)) {
                    logger.warn("Session not found for timer {}. Timer could not removed.", ejbTimerJob.getTimerJobInstance());
                }
            };
            transaction(tx, ejbTimerJob.getTimerJobInstance());
        } catch (Exception e1) {
            logger.warn("There was a problem during timer removal {}", ejbTimerJob.getTimerJobInstance(), e1);
        }
    }

    private void recoverTimerJobInstance(EjbTimerJob ejbTimerJob, Timer timer, Exception e) {
        if (isSessionNotFound(e)) {
            logger.warn("Trying to recover timer. Not possible due to process instance is not found. More likely already completed. Timer {} won't be recovered", ejbTimerJob.getTimerJobInstance(), e);
            // if session is not found means the process has already finished. In this case we just need to remove
            // the timer and avoid any recovery as it should not trigger any more timers.
            removeUnrecoverableTimer(ejbTimerJob, timer);
            return;
        }

        if (ejbTimerJob.getTimerJobInstance().getTrigger().hasNextFireTime() != null) {
            // this is an interval trigger. Problem here is that the timer scheduled by DefaultTimerJobInstance is lost
            // because of the transaction, so we need to do this here.
            try {            
               
                logger.warn("Execution of time failed Interval Trigger failed. Skipping {}", ejbTimerJob.getTimerJobInstance());
                Transaction<TimerJobInstance> tx = timerJobInstance -> {
                    if (this.removeJob(timerJobInstance.getJobHandle(), null)) {
                        this.internalSchedule(timerJobInstance);
                    } else {
                        logger.debug("Interval trigger {} was removed before rescheduling", ejbTimerJob.getTimerJobInstance());
                    }
                };
                transaction(tx, ejbTimerJob.getTimerJobInstance());
            } catch (Exception e1) {
                logger.warn("Could not schedule the interval trigger {}", ejbTimerJob.getTimerJobInstance(), e1);
            }
            return;
        }

        // if there is not next date to be fired, we need to apply policy otherwise will be lost

        logger.warn("Execution of time failed. The timer will be retried {}", ejbTimerJob.getTimerJobInstance());
        Transaction<TimerJobInstance> operation = (instance) -> {
            ZonedDateTime nextRetry = ZonedDateTime.now().plus(TIMER_RETRY_INTERVAL, ChronoUnit.MILLIS);
            EjbTimerJobRetry info = null;
            if(ejbTimerJob instanceof EjbTimerJobRetry) {
                info = ((EjbTimerJobRetry) ejbTimerJob).next();
            } else {
                info =  new EjbTimerJobRetry(instance);
            }
            if (TIMER_RETRY_LIMIT > 0 && info.getRetry() > TIMER_RETRY_LIMIT) {
                logger.warn("The timer {} reached retry limit {}. It won't be retried again", instance, TIMER_RETRY_LIMIT);
                return;
            }
            TimerConfig config = new TimerConfig(info, true);
            Timer newTimer = timerService.createSingleActionTimer(Date.from(nextRetry.toInstant()), config);
            TimerHandle handler = newTimer.getHandle();
            ((GlobalJpaTimerJobInstance) ejbTimerJob.getTimerJobInstance()).setTimerInfo(handler);
            ((GlobalJpaTimerJobInstance) ejbTimerJob.getTimerJobInstance()).setExternalTimerId(getPlatformTimerId(newTimer));
        };
        try {
            transaction(operation, ejbTimerJob.getTimerJobInstance());
        } catch (Exception e1) {
            logger.error("Failed to executed timer recovery {}", e1.getMessage(), e1);
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

    private <I> void transaction(Transaction<I> operation, I item) throws Exception {
        try {
            utx.begin();
            operation.doWork(item);
            utx.commit();
        } catch(RollbackException e) {
            logger.warn("Transaction was rolled back for {} with status {}", item, utx.getStatus());
            if(utx.getStatus() == javax.transaction.Status.STATUS_ACTIVE) {
                utx.rollback();
            }
            throw new RuntimeException("jbpm timer has been rolledback", e);
        } catch (Exception e) {
            try {
                utx.rollback();
            } catch (Exception re) {
                logger.error("transaction could not be rolled back", re);
            }

            throw e;
        }
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

	public boolean removeJob(JobHandle jobHandle, Timer ejbTimer) {
		EjbGlobalJobHandle ejbHandle = (EjbGlobalJobHandle) jobHandle;
        if (useLocalCache) {
            boolean removedFromCache = localCache.remove(ejbHandle.getUuid()) != null;
            logger.debug("Job handle {} is {} removed from cache ", jobHandle, removedFromCache ? "" : "not" );
        }

        if (ejbTimer != null) {
            try {
                ejbTimer.cancel();
                return true;
            } catch (Throwable e) {
                logger.debug("Timer cancel error due to {}", e.getMessage());
                return false;
            }
        }

        // small speed improvement using the ejb serializable info handler
        GlobalJpaTimerJobInstance timerJobInstance = (GlobalJpaTimerJobInstance) ejbHandle.getTimerJobInstance();
        if (timerJobInstance != null) {
            Object ejbTimerHandle =  timerJobInstance.getTimerInfo();
            if(ejbTimerHandle instanceof TimerHandle) {
                try {
                    ((TimerHandle) ejbTimerHandle).getTimer().cancel();
                } catch (Throwable e) {
                    logger.debug("Timer cancel error due to {}", e.getMessage());
                    return false;
                }
                return true;
            }
        } else {
            logger.warn("No timerJobInstance available for {}", ejbHandle);
        }

		for (Timer timer : timerService.getTimers()) {
			try {
    		    Serializable info = timer.getInfo();
    			if (info instanceof EjbTimerJob) {
    				EjbTimerJob job = (EjbTimerJob) info;

    				EjbGlobalJobHandle handle = (EjbGlobalJobHandle) job.getTimerJobInstance().getJobHandle();
    				if (handle.getUuid().equals(ejbHandle.getUuid())) {
    					logger.debug("Job handle {} does match timer and is going to be canceled", jobHandle);

    					try {
    					    timer.cancel();
    					} catch (Throwable e) {
    					    logger.debug("Timer cancel error due to {}", e.getMessage());
    					    return false;
    					}
    					return true;
    				}
    			}
			} catch (NoSuchObjectLocalException e) {
			    logger.debug("Timer {} has already expired or was canceled ", timer);
			}
		}
		logger.debug("Job handle {} does not match any timer on {} scheduler service", jobHandle, this);
		return false;
	}



	public TimerJobInstance getTimerByName(String jobName) {
    	if (useLocalCache) {
    		if (localCache.containsKey(jobName)) {
    			logger.debug("Found job {} in cache returning", jobName);
    			return localCache.get(jobName);
    		}
    	}
	    TimerJobInstance found = null;

		for (Timer timer : timerService.getTimers()) {
		    try {
    			Serializable info = timer.getInfo();
    			if (info instanceof EjbTimerJob) {
    				EjbTimerJob job = (EjbTimerJob) info;

    				EjbGlobalJobHandle handle = (EjbGlobalJobHandle) job.getTimerJobInstance().getJobHandle();

    				if (handle.getUuid().equals(jobName)) {
    					found = handle.getTimerJobInstance();
							if (useLocalCache) {
    					    localCache.putIfAbsent(jobName, found);
						  }
    					logger.debug("Job {} does match timer and is going to be returned {}", jobName, found);

    					break;
    				}
    			}
		    } catch (NoSuchObjectLocalException e) {
                logger.debug("Timer info for {} was not found ", timer);
            }
		}

		return found;
	}

    public void evictCache(JobHandle jobHandle) {
        String jobName =  ((EjbGlobalJobHandle) jobHandle).getUuid();
        logger.debug("Invalidate job {} with job name {} in cache", jobName, localCache.remove(jobName));
    }

}
