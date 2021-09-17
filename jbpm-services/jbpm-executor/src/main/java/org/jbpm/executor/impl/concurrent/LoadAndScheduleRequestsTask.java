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

import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.jbpm.executor.impl.AvailableJobsExecutor;
import org.kie.api.executor.RequestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class LoadAndScheduleRequestsTask implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(LoadAndScheduleRequestsTask.class);

    private ScheduledExecutorService scheduler;
    private AvailableJobsExecutor jobProcessor;
    private Supplier<List<RequestInfo>> taskLoader;
    

    public LoadAndScheduleRequestsTask(ScheduledExecutorService scheduler, AvailableJobsExecutor jobProcessor, Supplier<List<RequestInfo>> taskLoader) {
        super();
        this.scheduler = scheduler;
        this.jobProcessor = jobProcessor;
        this.taskLoader = taskLoader;
    }


    @Override
    public void run() {
        try {
            Date started = new Date();
            List<RequestInfo> loaded = taskLoader.get();

            if (!loaded.isEmpty()) {
                logger.info("Found {} jobs that are waiting for execution, scheduling them...", loaded.size());
                int scheduledCounter = 0;
                for (RequestInfo request : loaded) {
                    
                    PrioritisedRunnable job = new PrioritisedRunnable(request.getId(), request.getPriority(), request.getTime(), jobProcessor);
                    long delay = request.getTime().getTime() - System.currentTimeMillis();
                    logger.debug("Scheduling with delay {} for request {} at time {}", delay, request.getId(), request.getTime());
                    boolean scheduled = ((PrioritisedScheduledThreadPoolExecutor)scheduler).scheduleNoDuplicates(job, delay, TimeUnit.MILLISECONDS);
                    if (scheduled) {
                        logger.debug("Request {} has been successfully scheduled at {}", request.getId(), request.getTime());
                        scheduledCounter++;
                    } else {
                        logger.debug("Request {} has not been scheduled as it's already there", request.getId());
                    }
                }
                logger.info("{} jobs have been successfully scheduled", scheduledCounter);
            }

            logger.info("Load of jobs from storage started at {} and finished {} with size {}", started, new Date(), loaded.size());
        } catch (Throwable e) {
            logger.error("Unexpected error while synchronizing with data base for jobs", e);
        }
    }

}
