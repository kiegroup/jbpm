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

package org.jbpm.executor.impl.concurrent;

import java.util.function.BiConsumer;

import org.drools.persistence.api.TransactionManager;
import org.drools.persistence.api.TransactionSynchronization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defers a JMS executor job notification until after the surrounding JTA transaction
 * has successfully committed.  This ensures the {@code RequestInfo} row (persisted as
 * part of that transaction) is visible in the database before any pod receives the
 * message and attempts to execute the job.
 *
 * <p>Without this deferral the JMS send happens unconditionally inside the open
 * transaction.  If the transaction is later rolled back the row disappears but the
 * message has already been delivered, causing a pod to attempt execution against a
 * non-existent or duplicate job row.</p>
 */
public class JmsSendTransactionSynchronization implements TransactionSynchronization {

    private static final Logger logger = LoggerFactory.getLogger(JmsSendTransactionSynchronization.class);

    /** The job ID (converted to String) and its JMS priority. */
    private final String messageBody;
    private final int    priority;

    /**
     * The actual send operation, supplied by {@code ExecutorImpl} as a lambda so this
     * class has no compile-time dependency on the JMS API or {@code ExecutorImpl} itself.
     * Signature: {@code (messageBody, priority) -> void}.
     */
    private final BiConsumer<String, Integer> sendAction;

    public JmsSendTransactionSynchronization(String messageBody, int priority,
                                             BiConsumer<String, Integer> sendAction) {
        this.messageBody = messageBody;
        this.priority    = priority;
        this.sendAction  = sendAction;
    }

    @Override
    public void beforeCompletion() {
        // no-op
    }

    @Override
    public void afterCompletion(int status) {
        if (status == TransactionManager.STATUS_COMMITTED) {
            logger.debug("Transaction committed - sending JMS message for job {}", messageBody);
            sendAction.accept(messageBody, priority);
        } else {
            logger.debug("Transaction did not commit (status={}) - JMS message for job {} suppressed",
                         status, messageBody);
        }
    }
}
