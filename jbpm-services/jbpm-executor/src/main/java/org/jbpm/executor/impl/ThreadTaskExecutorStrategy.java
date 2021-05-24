/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.executor.impl;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.naming.InitialContext;

import org.drools.persistence.api.TransactionManager;
import org.drools.persistence.api.TransactionSynchronization;
import org.jbpm.executor.impl.concurrent.LoadAndScheduleRequestsTask;
import org.jbpm.executor.impl.concurrent.PrioritisedScheduledThreadPoolExecutor;
import org.jbpm.executor.impl.concurrent.ScheduleTaskTransactionSynchronization;
import org.kie.api.executor.ExecutorStoreService;
import org.kie.api.executor.RequestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadTaskExecutorStrategy implements TaskExecutorStrategy {

    private static final Logger logger = LoggerFactory.getLogger(ThreadTaskExecutorStrategy.class);

    private String threadFactoryLookup = System.getProperty("org.kie.executor.thread.factory", "java:comp/DefaultManagedThreadFactory");

    private PrioritisedScheduledThreadPoolExecutor scheduler;
    private TransactionManager transactionManager;
    private boolean active;
    private int numberOfThreads;
    private AvailableJobsExecutor jobProcessor;
    private ExecutorStoreService executorStoreService;
    private int interval;
    private TimeUnit timeunit;
    private ScheduledFuture<?> loadTaskFuture;

    private ExecutorImpl executor;
    
    public ThreadTaskExecutorStrategy () {
        active = false;
    }

    @Override
    public void setInterval(int interval, TimeUnit timeunit) {
        this.interval = interval;
        this.timeunit = timeunit;
    }

    @Override
    public void setExecutor(ExecutorImpl executor) {
        this.executor = executor;
    }

    @Override
    public void setExecutorStoreService(ExecutorStoreService executorStoreService) {
        this.executorStoreService = executorStoreService;
    }

    @Override
    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    @Override
    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public void setJobsExecutor(AvailableJobsExecutor jobProcessor) {
        this.jobProcessor = jobProcessor;
    }

    @Override
    public void init() {
        scheduler = getScheduledExecutorService();
        active = true;

        LoadAndScheduleRequestsTask loadTask = new LoadAndScheduleRequestsTask(executorStoreService, executor);

        if (interval <= 0) {
            logger.info("Interval is not set, scheduling load of jobs from the storage just once");
            scheduler.execute(loadTask);
        } else {
            logger.info("Interval ({}) is more than 0, scheduling periodic load of jobs from the storage", interval);
            loadTaskFuture = scheduler.scheduleAtFixedRate(loadTask, 0, interval, timeunit);
        }
    }

    protected PrioritisedScheduledThreadPoolExecutor getScheduledExecutorService() {
        ThreadFactory threadFactory = null;

        try {
            threadFactory = InitialContext.doLookup(threadFactoryLookup);
        } catch (Exception e) {
            threadFactory = Executors.defaultThreadFactory();
        }

        return new PrioritisedScheduledThreadPoolExecutor(numberOfThreads, threadFactory, jobProcessor);
    }

    @Override
    public void destroy() {
        try {
            if(loadTaskFuture != null) {
                loadTaskFuture.cancel(true);
            }
            active = false;
            scheduler.shutdownNow();
            scheduler.awaitTermination(60, TimeUnit.SECONDS);
            scheduler = null;
        } catch (InterruptedException e) {
            logger.warn("ThreadTaskExecutorStrategy was interrupted during awiting termination", e);
        }
    }

    @Override
    public void schedule(RequestInfo requestInfo, Date date) {
        transactionManager.registerTransactionSynchronization(new ScheduleTaskTransactionSynchronization(scheduler, requestInfo, date, jobProcessor));
    }

    @Override
    public boolean accept(RequestInfo requestInfo, Date date) {
        return active;
    }

    @Override
    public boolean active() {
        return active;
    }

    @Override
    public void clear(Long requestId) {
        transactionManager.registerTransactionSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if(status == TransactionManager.STATUS_COMMITTED) {
                    scheduler.done(requestId);
                }
            }

            @Override
            public void beforeCompletion() {
                // do nothing
            }
        });
    }

    @Override
    public void cancel(RequestInfo requestInfo) {
        scheduler.cancel(requestInfo.getId());
    }

}
