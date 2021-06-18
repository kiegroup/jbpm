/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.executor.impl.concurrent;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PrioritisedScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(PrioritisedScheduledThreadPoolExecutor.class);
    
    private ConcurrentHashMap<Long, ScheduledFuture<?>> scheduled = new ConcurrentHashMap<>();

    private Set<Long> pendingRetries = Collections.synchronizedSet(new HashSet<>());

    public PrioritisedScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, threadFactory, handler);
    }

    public PrioritisedScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }


    
    public boolean scheduleNoDuplicates(Runnable command, long delay, TimeUnit unit) {
        Long requestId = null;
        Date fireDate = null;
        if (command instanceof PrioritisedRunnable) {
            PrioritisedRunnable runnable = (PrioritisedRunnable) command;
            requestId = runnable.getId();
            fireDate = runnable.getFireDate();
            ScheduledFuture<?> alreadyScheduled = scheduled.get(requestId);
            logger.debug("Checking if request with id {} is already scheduled {}", requestId, alreadyScheduled);
            if(alreadyScheduled != null && alreadyScheduled.isDone()) {
                this.scheduled.remove(requestId);
            }

            // there are valid scheduled tasks or request is marked as retried
            if (scheduled.containsKey(requestId)) {
                logger.debug("Request {} is already scheduled", requestId);
                return false;
            } else if (pendingRetries.contains(requestId)) {
                logger.debug("Request {} is already scheduled for retry", requestId);
                return false;
            }
        }

        logger.debug("Schedule request {} with fireDate {} is already scheduled at {}", requestId, fireDate, delay);
        super.schedule(command, delay, unit);
        return true;
    }

    @Override
    protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable, RunnableScheduledFuture<V> task) {
        RunnableScheduledFuture<V> r = super.decorateTask(runnable, task);
        
        if (runnable instanceof PrioritisedRunnable) {
            r = new PrioritisedScheduledFutureTask<V>(r, 
                    ((PrioritisedRunnable) runnable).getPriority(), 
                    ((PrioritisedRunnable) runnable).getFireDate());
            // should always replace the old scheduled
            this.scheduled.put(((PrioritisedRunnable) runnable).getId(), r);
            logger.debug("Request job {} has been scheduled number of jobs in the pool {}", ((PrioritisedRunnable) runnable).getId(), scheduled.size());
        }
        
        return r;
    }

    
    public void markAsPendingRetry(Long requestId) {
        this.scheduled.remove(requestId);
        this.pendingRetries.add(requestId);
    }

    public void cancel(Long requestId) {
        pendingRetries.remove(requestId);
        ScheduledFuture<?> future = this.scheduled.remove(requestId);
        if (future != null) {
            boolean canceled = future.cancel(false);
            logger.debug("Request job {} has been attempted to be canceled with result {} number of jobs in the pool {}", requestId, canceled, scheduled.size());
        }
    }
    
    public void done(Long requestId) {
        pendingRetries.remove(requestId);
        this.scheduled.remove(requestId);
        logger.debug("Request job {} has been completed number of jobs in the pool {}", requestId, scheduled.size());
    }

    @Override
    public void shutdown() {
        super.shutdown();
        this.scheduled.clear();
        this.pendingRetries.clear();
    }

    @Override
    public List<Runnable> shutdownNow() {        
        List<Runnable> remaining = super.shutdownNow();
        this.scheduled.clear();
        this.pendingRetries.clear();
        return remaining;
    }


    
}
