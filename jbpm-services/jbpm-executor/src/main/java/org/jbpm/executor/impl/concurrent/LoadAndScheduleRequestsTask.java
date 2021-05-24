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

import org.jbpm.executor.impl.ExecutorImpl;
import org.kie.api.executor.ExecutorStoreService;
import org.kie.api.executor.RequestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadAndScheduleRequestsTask implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(LoadAndScheduleRequestsTask.class);

    private ExecutorImpl executor;
    private ExecutorStoreService executorStoreService;


    public LoadAndScheduleRequestsTask(ExecutorStoreService executorStoreService, ExecutorImpl executor) {
        super();
        this.executorStoreService = executorStoreService;
        this.executor = executor;
    }

    @Override
    public void run() {
        boolean owner = executor.getTransactionManager().begin();
        try {
            List<RequestInfo> loaded = executorStoreService.loadRequests();
            logger.debug("Load of jobs from storage started at {} with size {}", new Date(), loaded.size());
            if (!loaded.isEmpty()) {
                logger.info("Found {} jobs that are waiting for execution", loaded.size());
                for (RequestInfo request : loaded) {
                    long delay = request.getTime().getTime() - System.currentTimeMillis();
                    logger.debug("Scheduling with delay {} for request {} at time {}", delay, request.getId(), request.getTime());
                    executor.scheduleExecution(request, delay <= 0 ? null: request.getTime());
                }
            }
            logger.debug("Load of jobs from storage finished at {}", new Date());
            executor.getTransactionManager().commit(owner);
        } catch (Throwable e) {
            executor.getTransactionManager().rollback(owner);
            logger.error("Unexpected error while synchronizing with data base for jobs", e);
        }
    }

}
