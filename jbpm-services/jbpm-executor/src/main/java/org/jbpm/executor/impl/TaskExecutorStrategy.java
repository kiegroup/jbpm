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
import java.util.concurrent.TimeUnit;

import org.drools.persistence.api.TransactionManager;
import org.kie.api.executor.ExecutorStoreService;
import org.kie.api.executor.RequestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface TaskExecutorStrategy {
    static final Logger logger = LoggerFactory.getLogger(TaskExecutorStrategy.class);

    default void setNumberOfThreads(int numberOfThreads) {
        logger.debug("This task executor strategy {} does not accept number of threads", this.getClass().getCanonicalName());
    }

    default void setTransactionManager(TransactionManager transactionManager) {
        logger.debug("This task executor strategy {} does not accept transaction manager", this.getClass().getCanonicalName());
    }

    default void setJobsExecutor(AvailableJobsExecutor jobProcessor) {
        logger.debug("This task executor strategy {} does not accept job processor", this.getClass().getCanonicalName());
    }

    default void setExecutorStoreService(ExecutorStoreService executorStoreService) {
        logger.debug("This task executor strategy {} does not accept executor store service", this.getClass().getCanonicalName());
    }

    default void setInterval(int interval, TimeUnit timeunit) {
        logger.debug("This task executor strategy {} does not accept interval", this.getClass().getCanonicalName());
    }

    default void setExecutor(ExecutorImpl executor) {
        logger.debug("This task executor strategy {} does not accept executor", this.getClass().getCanonicalName());
    }

    void init();

    void destroy();

    void schedule(RequestInfo requestInfo, Date date);

    boolean accept(RequestInfo requestInfo, Date date);

    boolean active();

    void clear(Long requestId);

    void cancel(RequestInfo requestInfo);

}
