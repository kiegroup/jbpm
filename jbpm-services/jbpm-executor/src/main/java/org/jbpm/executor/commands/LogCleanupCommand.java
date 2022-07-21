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

package org.jbpm.executor.commands;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManagerFactory;

import org.jbpm.executor.impl.jpa.ExecutorJPAAuditService;
import org.jbpm.process.core.timer.DateTimeUtils;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.kie.api.executor.Command;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;
import org.kie.api.executor.Reoccurring;
import org.kie.api.executor.STATUS;
import org.kie.api.runtime.process.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Log clean up command that aims at doing house keeping of audit/log tables used in jBPM:
 * <ul>
 * 	<li>process related audit logs (process instance, node instance, variables)</li>
 * 	<li>task related audit logs (audit task, task events)</li>
 * 	<li>executor related data (requests and errors)</li>
 * </ul>
 * Command by default is auto configured to run once a day from the time it was initially scheduled though it can be reconfigured
 * in terms of frequency when it is executed and if it shall run multiple times at all.<br/>
 * Following is a complete list of accepted parameters:
 * <ul>
 * 	<li>SkipProcessLog - indicates if clean up of process logs should be omitted (true|false)</li>
 * 	<li>SkipTaskLog - indicates if clean up of task logs should be omitted (true|false)</li>
 * 	<li>SkipExecutorLog - indicates if clean up of executor logs should be omitted (true|false)</li>
 * 	<li>DateFormat - date format for further date related params - if not given yyyy-MM-dd is used (pattern of SimpleDateFormat class)</li>
 * 	<li>EmfName - name of entity manager factory to be used for queries (valid persistence unit name)</li>
 * 	<li>SingleRun - indicates if execution should be single run only (true|false)</li>
 * 	<li>NextRun - provides next execution time (valid time expression e.g. 1d, 5h, etc)</li>
 * 	<li>OlderThan - indicates what logs should be deleted - older than given date</li>
 * 	<li>OlderThanPeriod - indicated what logs should be deleted older than given time expression (valid time expression e.g. 1d, 5h, etc)</li>
 * 	<li>ForProcess - indicates logs to be deleted only for given process definition</li>
 * 	<li>ForDeployment - indicates logs to be deleted that are from given deployment id</li>
 *  <li>RecordsPerTransaction - indicates number of records to be included in each DB transaction (default is 0 which means do the delete in one single transaction)</li>
 * </ul>
 */
public class LogCleanupCommand implements Command, Reoccurring {

    private static final Logger logger = LoggerFactory.getLogger(LogCleanupCommand.class);
    protected final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static final String RECORDS_PER_TRANSACTION = "RecordsPerTransaction";

    private long nextScheduleTimeAdd;
    private boolean mightBeMore;

    @Override
    public Date getScheduleTime() {
        Date nextSchedule;
        if (mightBeMore) {
            // if there are pending records, reexecute immediately
            nextSchedule = Date.from(Instant.now().plus(Duration.ofMillis(100)));
        }
        else {
            if (nextScheduleTimeAdd <= 0) {
                return null;
            }
            nextSchedule = Date.from(Instant.now().plus(nextScheduleTimeAdd, ChronoUnit.MILLIS));
        }
        logger.debug("Next schedule for job {} is set to {}", this.getClass().getSimpleName(), nextSchedule);
        return nextSchedule;
    }

    protected int readInt(Map<String, Object> params, String propName, int defaultValue) {
        Object value = params.get(propName);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value != null){
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException ex) {
                // will return default
            }
        }
        return defaultValue;
    }

    @Override
    public ExecutionResults execute(CommandContext ctx) throws Exception {
        boolean skipProcessLog = ctx.getData().containsKey("SkipProcessLog") ? Boolean.parseBoolean((String) ctx.getData("SkipProcessLog")) : false;
        boolean skipTaskLog = ctx.getData().containsKey("SkipTaskLog") ? Boolean.parseBoolean((String) ctx.getData("SkipTaskLog")) : false;
        boolean skipExecutorLog = ctx.getData().containsKey("SkipExecutorLog") ? Boolean.parseBoolean((String) ctx.getData("SkipExecutorLog")) : false;
        int recordsPerTransaction = readInt(ctx.getData(), RECORDS_PER_TRANSACTION, 0);
        mightBeMore = false;

        SimpleDateFormat formatToUse = dateFormat;

        String dataFormat = (String) ctx.getData("DateFormat");
        if (dataFormat != null) {
            formatToUse = new SimpleDateFormat(dataFormat);
        }

        ExecutionResults executionResults = new ExecutionResults();
        String emfName = (String) ctx.getData("EmfName");
        if (emfName == null) {
            emfName = "org.jbpm.domain";
        }
        nextScheduleTimeAdd = TimeUnit.DAYS.toMillis(1);
        String singleRun = (String) ctx.getData("SingleRun");
        if ("true".equalsIgnoreCase(singleRun)) {
            // disable rescheduling
            nextScheduleTimeAdd = -1;
        }
        String nextRun = (String) ctx.getData("NextRun");
        if (nextRun != null) {
            nextScheduleTimeAdd = DateTimeUtils.parseDateAsDuration(nextRun);
        }

        // get hold of persistence and create instance of audit service
        EntityManagerFactory emf = EntityManagerFactoryManager.get().getOrCreate(emfName);
        ExecutorJPAAuditService auditLogService = new ExecutorJPAAuditService(emf);

        // collect parameters
        String olderThan = (String) ctx.getData("OlderThan");
        String olderThanPeriod = (String) ctx.getData("OlderThanPeriod");
        String forProcess = (String) ctx.getData("ForProcess");
        String forDeployment = (String) ctx.getData("ForDeployment");
        String statusFilter = (String) ctx.getData("Status");
        Integer[] status = null;

        if (statusFilter != null) {
            status = Arrays.stream(statusFilter.split(",")).map(e -> Integer.parseInt(e)).toArray(Integer[]::new);
        }

        if (olderThanPeriod != null) {
            long olderThanDuration = DateTimeUtils.parseDateAsDuration(olderThanPeriod);
            Date olderThanDate = new Date(System.currentTimeMillis() - olderThanDuration);

            olderThan = formatToUse.format(olderThanDate);
        }

        if (!skipExecutorLog) {
            // executor tables  
            int errorInfoLogsRemoved = auditLogService.errorInfoLogDeleteBuilder()
                                                  .dateRangeEnd(olderThan == null ? null : formatToUse.parse(olderThan))
                                                  .recordsPerTransaction(recordsPerTransaction)
                                                  .logBelongsToProcessInStatus(status)
                                                  .build()
                                                  .execute();
            this.mightBeMore|= mightBeMore(errorInfoLogsRemoved, recordsPerTransaction);
            logger.info("ErrorInfoLogsRemoved {}", errorInfoLogsRemoved);
            executionResults.setData("ErrorInfoLogsRemoved", errorInfoLogsRemoved);

            int requestInfoLogsRemoved = auditLogService.requestInfoLogDeleteBuilder()
                                                    .dateRangeEnd(olderThan == null ? null : formatToUse.parse(olderThan))
                                                    .recordsPerTransaction(recordsPerTransaction)
                                                    .logBelongsToProcessInStatus(status)
                                                    .status(STATUS.CANCELLED, STATUS.DONE, STATUS.ERROR)
                                                    .build()
                                                    .execute();
            this.mightBeMore|= mightBeMore(requestInfoLogsRemoved, recordsPerTransaction);
            logger.info("RequestInfoLogsRemoved {}", requestInfoLogsRemoved);
            executionResults.setData("RequestInfoLogsRemoved", requestInfoLogsRemoved);

            int executionErrorInfoLogsRemoved = auditLogService.executionErrorInfoDeleteBuilder()
                                                               .dateRangeEnd(olderThan == null ? null : formatToUse.parse(olderThan))
                                                               .recordsPerTransaction(recordsPerTransaction)
                                                               .logBelongsToProcessInStatus(status)
                                                               .build()
                                                               .execute();
            this.mightBeMore|= mightBeMore(executionErrorInfoLogsRemoved, recordsPerTransaction);
            logger.info("ExecutionErrorInfoLogsRemoved {}", executionErrorInfoLogsRemoved);
            executionResults.setData("ExecutionErrorInfoLogsRemoved", executionErrorInfoLogsRemoved);
        }

        if (!skipTaskLog) {
            // task tables
            int taLogsRemoved = auditLogService.auditTaskDelete()
                                           .processId(forProcess)
                                           .dateRangeEnd(olderThan == null ? null : formatToUse.parse(olderThan))
                                           .recordsPerTransaction(recordsPerTransaction)
                                           .deploymentId(forDeployment)
                                           .logBelongsToProcessInStatus(status)
                                           .build()
                                           .execute();
            this.mightBeMore|= mightBeMore(taLogsRemoved, recordsPerTransaction);
            logger.info("TaskAuditLogRemoved {}", taLogsRemoved);
            executionResults.setData("TaskAuditLogRemoved", taLogsRemoved);

            int teLogsRemoved = auditLogService.taskEventInstanceLogDelete()
                                           .dateRangeEnd(olderThan == null ? null : formatToUse.parse(olderThan))
                                           .recordsPerTransaction(recordsPerTransaction)
                                           .logBelongsToProcessInStatus(status)
                                           .build()
                                           .execute();
            this.mightBeMore|= mightBeMore(teLogsRemoved, recordsPerTransaction);
            logger.info("TaskEventLogRemoved {}", teLogsRemoved);
            executionResults.setData("TaskEventLogRemoved", teLogsRemoved);

            int tvLogsRemoved  = auditLogService.taskVariableInstanceLogDelete()
                                           .dateRangeEnd(olderThan == null ? null : formatToUse.parse(olderThan))
                                           .recordsPerTransaction(recordsPerTransaction)
                                           .logBelongsToProcessInStatus(status)
                                           .build()
                                           .execute();
            this.mightBeMore|= mightBeMore(tvLogsRemoved, recordsPerTransaction);
            logger.info("TaskVariableLogRemoved {}", tvLogsRemoved);
            executionResults.setData("TaskVariableLogRemoved", tvLogsRemoved);
        }

        if (!skipProcessLog) {
            // process tables			
            int niLogsRemoved = auditLogService.nodeInstanceLogDelete()
                                           .processId(forProcess)
                                           .dateRangeEnd(olderThan == null ? null : formatToUse.parse(olderThan))
                                           .recordsPerTransaction(recordsPerTransaction)
                                           .externalId(forDeployment)
                                           .logBelongsToProcessInStatus(status)
                                           .build()
                                           .execute();
            this.mightBeMore|= mightBeMore(niLogsRemoved, recordsPerTransaction);
            logger.info("NodeInstanceLogRemoved {}", niLogsRemoved);
            executionResults.setData("NodeInstanceLogRemoved", niLogsRemoved);

            int viLogsRemoved = auditLogService.variableInstanceLogDelete()
                                           .processId(forProcess)
                                           .dateRangeEnd(olderThan == null ? null : formatToUse.parse(olderThan))
                                           .recordsPerTransaction(recordsPerTransaction)
                                           .externalId(forDeployment)
                                           .logBelongsToProcessInStatus(status)
                                           .build()
                                           .execute();
            this.mightBeMore|= mightBeMore(viLogsRemoved, recordsPerTransaction);
            logger.info("VariableInstanceLogRemoved {}", viLogsRemoved);
            executionResults.setData("VariableInstanceLogRemoved", viLogsRemoved);

            if (!mightBeMore) {
                int piLogsRemoved = auditLogService.processInstanceLogDelete()
                                           .processId(forProcess)
                                           .status(ProcessInstance.STATE_COMPLETED, ProcessInstance.STATE_ABORTED)
                                           .endDateRangeEnd(olderThan == null ? null : formatToUse.parse(olderThan))
                                           .recordsPerTransaction(recordsPerTransaction)
                                           .externalId(forDeployment)
                                           .logBelongsToProcessInStatus(status)
                                           .build()
                                           .execute();
                this.mightBeMore|= mightBeMore(piLogsRemoved, recordsPerTransaction);
                logger.info("ProcessInstanceLogRemoved {}", piLogsRemoved);
                executionResults.setData("ProcessInstanceLogRemoved", piLogsRemoved);
            }
        }
        executionResults.setData("BAMLogRemoved", 0L);
        return executionResults;
    }
    
    private  static boolean mightBeMore (int deleted, int recordPerTransaction) {
        return recordPerTransaction > 0 && deleted >= recordPerTransaction;
    }
}
