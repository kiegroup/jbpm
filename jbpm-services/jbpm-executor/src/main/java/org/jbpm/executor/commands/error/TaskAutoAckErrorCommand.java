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

package org.jbpm.executor.commands.error;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.drools.persistence.api.TransactionManager;
import org.drools.persistence.api.TransactionManagerFactory;
import org.jbpm.runtime.manager.impl.error.filters.TaskExecutionErrorFilter;
import org.jbpm.runtime.manager.impl.jpa.ExecutionErrorInfo;
import org.kie.api.task.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command that will auto acknowledge task errors based on their status - 
 * completed, failed, exited, obsolete, error - will be considered as eligible for ack
 * 
 * Following parameters are supported by this command:
 * <ul>
 *  <li>EmfName - name of entity manager factory to be used for queries (valid persistence unit name)</li>
 *  <li>SingleRun - indicates if execution should be single run only (true|false)</li>
 *  <li>NextRun - provides next execution time (valid time expression e.g. 1d, 5h, etc)</li>
 * </ul>
 */
public class TaskAutoAckErrorCommand extends AutoAckErrorCommand {

    private static final Logger logger = LoggerFactory.getLogger(TaskAutoAckErrorCommand.class);

    private static final String RULE = "Tasks that previously failed but now are in one of the statuses - completed, failed, exited, obsolete, error";
    

    @Override
    protected List<ExecutionErrorInfo> findErrorsToAck(EntityManager em) {
        List<ExecutionErrorInfo> errorsToAck = new ArrayList<>();
        
        TransactionManager txm = TransactionManagerFactory.get().newTransactionManager();
        boolean txOwner = txm.begin();
        try {
            String findTaskErrorsQuery = "select error from ExecutionErrorInfo error " + "where error.type = :type " + "and error.acknowledged =:acknowledged " +
                                         "and error.activityId in (select at.taskId from AuditTaskImpl at where status in (:status))";

            errorsToAck = em.createQuery(findTaskErrorsQuery, ExecutionErrorInfo.class)
                            .setParameter("type", TaskExecutionErrorFilter.TYPE)
                            .setParameter("acknowledged", new Short("0"))
                            .setParameter("status", Arrays.asList(Status.Completed.toString(), Status.Exited.toString(), Status.Failed.toString(), Status.Obsolete.toString(), Status.Error.toString()))
                            .getResultList();
            txm.commit(txOwner);
        } catch (Exception e) {
            logger.error("Execution error in command TaskAutoAckErrorCommand", e);
            txm.rollback(txOwner);
        }
        return errorsToAck;
        
    }

    @Override
    protected String getAckRule() {
        return RULE;
    }

}
