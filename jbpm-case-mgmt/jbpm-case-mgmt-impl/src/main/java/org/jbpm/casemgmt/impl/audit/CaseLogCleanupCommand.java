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

package org.jbpm.casemgmt.impl.audit;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.persistence.EntityManagerFactory;

import org.jbpm.executor.commands.LogCleanupCommand;
import org.jbpm.executor.impl.jpa.ExecutorJPAAuditService;
import org.jbpm.process.core.timer.DateTimeUtils;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Case Log clean up command that aims at doing house keeping of audit/log tables used in jBPM extending
 * {@link LogCleanupCommand} for current behavior.
 * This command adds this to LogCleanupCommand. Theses parameters restricts the conditions where the logs are 
 * going to be deleted.
 * <ul>
 * 	<li>ForCaseDefId -  restricts the logs to be removed to those where the case definition id matches this parameter</li>
 * 	<li>ForDeployment -  restricts the logs to be removed to those where the case instance is in this deployment</li>
 * 	<li>Status - restricts the logs to be removed to those where the case instance is in this status list</li>
 * </ul>
 */
public class CaseLogCleanupCommand extends LogCleanupCommand {

    private static final Logger logger = LoggerFactory.getLogger(CaseLogCleanupCommand.class);


    @Override
    public ExecutionResults execute(CommandContext ctx) throws Exception {

        SimpleDateFormat formatToUse = dateFormat;

        String dataFormat = (String) ctx.getData("DateFormat");
        if (dataFormat != null) {
            formatToUse = new SimpleDateFormat(dataFormat);
        }

        String emfName = (String) ctx.getData("EmfName");
        if (emfName == null) {
            emfName = "org.jbpm.domain";
        }

        // this should affect the queries
        String forCaseDefId = (String) ctx.getData("ForCaseDefId");
        String forDeployment = (String) ctx.getData("ForDeployment");
        String statusFilter = (String) ctx.getData("Status");
        Integer[] status = null;

        if (statusFilter != null) {
            status = Arrays.stream(statusFilter.split(",")).map(e -> Integer.parseInt(e)).toArray(Integer[]::new);
        }

        // collect parameters
        String olderThanPeriod = (String) ctx.getData("OlderThanPeriod");
        String olderThan = (String) ctx.getData("OlderThan");

        if (olderThanPeriod != null) {
            long olderThanDuration = DateTimeUtils.parseDateAsDuration(olderThanPeriod);
            Date olderThanDate = new Date(System.currentTimeMillis() - olderThanDuration);

            olderThan = formatToUse.format(olderThanDate);
        }

        // get hold of persistence and create instance of audit service
        EntityManagerFactory emf = EntityManagerFactoryManager.get().getOrCreate(emfName);
        ExecutorJPAAuditService auditLogService = new ExecutorJPAAuditService(emf);
        int recordsPerTransaction = readInt(ctx.getData(), RECORDS_PER_TRANSACTION, 0);

        CaseFileDataLogDeleteBuilder deleteCaseFileDataLog = new CaseFileDataLogDeleteBuilderImpl(auditLogService);
        long caseFileDataLogsRemoved = deleteCaseFileDataLog.dateRangeEnd(olderThan == null ? null : formatToUse.parse(olderThan))
                                                            .logBelongsToProcessInDeployment(forDeployment)
                                                            .logBelongsToProcessInStatus(status)
                                                            .recordsPerTransaction(recordsPerTransaction)
                                                            .inCaseDefId(forCaseDefId)
                                                            .build()
                                                            .execute();

        ExecutionResults caseExecutionResults = new ExecutionResults();
        logger.info("CaseFileDataLog {}", caseFileDataLogsRemoved);
        caseExecutionResults.setData("CaseFileDataLog", caseFileDataLogsRemoved);

        CaseRoleAssignmentLogDeleteBuilder deleteCaseRoleAssignmentLog = new CaseRoleAssignmentLogDeleteBuilderImpl(auditLogService);
        long caseRoleAssignmentLogsRemoved = deleteCaseRoleAssignmentLog.logBelongsToProcessInDeployment(forDeployment)
                                                                        .logBelongsToProcessInStatus(status)
                                                                        .recordsPerTransaction(recordsPerTransaction)
                                                                        .inCaseDefId(forCaseDefId)
                                                                        .build()
                                                                        .execute();

        logger.info("CaseRoleAssignmentLog {}", caseRoleAssignmentLogsRemoved);
        caseExecutionResults.setData("CaseRoleAssignmentLog", caseRoleAssignmentLogsRemoved);

        ctx.setData("ForProcess", forCaseDefId);
        ExecutionResults executionResults = super.execute(ctx);
        executionResults.getData().putAll(caseExecutionResults.getData());

        return executionResults;
    }

}
