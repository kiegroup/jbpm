/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.test.functional.jobexec;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.assertj.core.api.Assertions;
import org.jbpm.casemgmt.impl.utils.DefaultCaseServiceConfigurator;
import org.jbpm.executor.ExecutorServiceFactory;
import org.jbpm.executor.impl.ExecutorServiceImpl;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.services.api.model.DeploymentUnit;
import org.jbpm.services.task.audit.service.TaskJPAAuditService;
import org.jbpm.test.listener.CountDownAsyncJobListener;
import org.jbpm.test.services.AbstractCaseServicesTest;
import org.junit.Test;
import org.kie.api.executor.ExecutorService;
import org.kie.internal.executor.api.CommandContext;
import org.kie.internal.runtime.manager.deploy.DeploymentDescriptorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Note: Boolean parameters are processed as "Boolean.parseBoolean((String) ctx.getData("attr"))" - This does
 * make sense as the command is intended to be used from the Web UI using forms.
 */
public class CaseLogCleanupCommandTest extends AbstractCaseServicesTest {

    private static final Logger logger = LoggerFactory.getLogger(CaseLogCleanupCommandTest.class);

    private static final String DEPLOYMENT_UNIT = "org.jbpm.cases:case-module:1.0.0";
    protected static final String ARTIFACT_ID = "case-module";
    protected static final String GROUP_ID = "org.jbpm.cases";
    protected static final String VERSION = "1.0.0";

    protected static final String USER = "john";

    public static final String LOG_CLEANUP = "org/jbpm/test/functional/jobexec/CaseLogCleanupCommand.bpmn2";
    public static final String LOG_CLEANUP_ID = "org.jbpm.test.functional.jobexec.CaseLogCleanupCommand";

    public static final String LOG_INCOMPLETE_CLEANUP = "org/jbpm/test/functional/jobexec/CaseLogIncompleteCleanupCommand.bpmn2";
    public static final String LOG_INCOMPLETE_CLEANUP_ID = "org.jbpm.test.functional.jobexec.CaseLogIncompleteCleanupCommand";

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    private static final int EXECUTOR_THREADS = 4;
    private static final int EXECUTOR_RETRIES = 3;
    private static final int EXECUTOR_INTERVAL = 0;

    private int executorThreads = EXECUTOR_THREADS;
    private int executorRetries = EXECUTOR_RETRIES;
    private int executorInterval = EXECUTOR_INTERVAL;

    private String dateFormat = DATE_FORMAT;

    private TaskJPAAuditService taskAuditService;
    private JPAAuditLogService auditLogService;

    private ExecutorService executorService;

    // ------------------------ Test Methods ------------------------

    @Override
    protected String getJndiDatasourceName() {
        return "jdbc/jbpm-ds";
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        taskAuditService = new TaskJPAAuditService(getEmf());
        taskAuditService.clear();
        auditLogService = new JPAAuditLogService(getEmf());
        auditLogService.clear();
    }

    @Override
    protected String getPersistenceUnitName() {
        return "org.jbpm.test.persistence";
    }

    private EntityManagerFactory getEmf() {
        return ((DefaultCaseServiceConfigurator) caseConfigurator).getEmf();
    }

    @Override
    public void tearDown() {
        try {
            taskAuditService.clear();
            taskAuditService.dispose();
            auditLogService.clear();
            auditLogService.dispose();
            executorService.clearAllRequests();
            executorService.clearAllErrors();
            executorService.destroy();
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void deleteAllLogsTest() throws Exception {
        CountDownAsyncJobListener countDownListener = new CountDownAsyncJobListener(1);
        ((ExecutorServiceImpl) getExecutorService()).addAsyncJobListener(countDownListener);
        // Generate data
        String caseIdIncomplete = caseService.startCase(DEPLOYMENT_UNIT, LOG_INCOMPLETE_CLEANUP_ID); // cancelled
        logger.info("CASE CREATED: {}", caseIdIncomplete);
        caseService.cancelCase(caseIdIncomplete);

        // Advance time 1+ second forward
        Thread.sleep(1010);

        // Verify presence of data
        Assertions.assertThat(getCaseFileDataLogSize()).isEqualTo(1);
        Assertions.assertThat(getCaseAssignmentRoleLogSize()).isPositive();

        // Schedule cleanup job
        scheduleLogCleanup(null, "1s", Collections.emptyMap());
        countDownListener.waitTillCompleted();


        // Verify absence of data
        Assertions.assertThat(getCaseFileDataLogSize()).isZero();
        Assertions.assertThat(getCaseAssignmentRoleLogSize()).isZero();
        Assertions.assertThat(getProcessLogSize(LOG_INCOMPLETE_CLEANUP_ID)).isZero();
        Assertions.assertThat(getTaskLogSize(LOG_INCOMPLETE_CLEANUP_ID)).isZero();
        Assertions.assertThat(getNodeInstanceLogSize(LOG_INCOMPLETE_CLEANUP_ID)).isZero();
        Assertions.assertThat(getVariableLogSize(LOG_INCOMPLETE_CLEANUP_ID)).isZero();
    }

    @Test
    public void deleteForDeploymentUnit() throws Exception {
        CountDownAsyncJobListener countDownListener = new CountDownAsyncJobListener(1);
        ((ExecutorServiceImpl) getExecutorService()).addAsyncJobListener(countDownListener);
        // Generate data
        String caseIdIncomplete = caseService.startCase(DEPLOYMENT_UNIT, LOG_INCOMPLETE_CLEANUP_ID); // cancelled
        logger.info("CASE CREATED: {}", caseIdIncomplete);
        caseService.cancelCase(caseIdIncomplete);

        // Advance time 1+ second forward
        Thread.sleep(1010);

        // Verify presence of data
        Assertions.assertThat(getCaseFileDataLogSize()).isEqualTo(1);
        Assertions.assertThat(getCaseAssignmentRoleLogSize()).isPositive();

        // Schedule cleanup job
        Map<String, Object> params = new HashMap<>();
        params.put("Status", "2,3");
        params.put("ForDeployment", DEPLOYMENT_UNIT);
        scheduleLogCleanup(null, "1s", params);
        countDownListener.waitTillCompleted();

        // Verify absence of data
        Assertions.assertThat(getCaseFileDataLogSize()).isZero();
        Assertions.assertThat(getCaseAssignmentRoleLogSize()).isZero();
        Assertions.assertThat(getProcessLogSize(LOG_INCOMPLETE_CLEANUP_ID)).isZero();
        Assertions.assertThat(getTaskLogSize(LOG_INCOMPLETE_CLEANUP_ID)).isZero();
        Assertions.assertThat(getNodeInstanceLogSize(LOG_INCOMPLETE_CLEANUP_ID)).isZero();
        Assertions.assertThat(getVariableLogSize(LOG_INCOMPLETE_CLEANUP_ID)).isZero();
    }

    @Test
    public void deleteForCaseDefinition() throws Exception {
        CountDownAsyncJobListener countDownListener = new CountDownAsyncJobListener(1);
        ((ExecutorServiceImpl) getExecutorService()).addAsyncJobListener(countDownListener);
        // Generate data
        String caseIdIncomplete = caseService.startCase(DEPLOYMENT_UNIT, LOG_INCOMPLETE_CLEANUP_ID); // cancelled
        logger.info("CASE CREATED: {}", caseIdIncomplete);
        caseService.cancelCase(caseIdIncomplete);

        // Advance time 1+ second forward
        Thread.sleep(1010);

        // Verify presence of data
        Assertions.assertThat(getCaseFileDataLogSize()).isEqualTo(1);
        Assertions.assertThat(getCaseAssignmentRoleLogSize()).isPositive();

        // Schedule cleanup job
        Map<String, Object> params = new HashMap<>();
        params.put("ForCaseDefId", LOG_INCOMPLETE_CLEANUP_ID);
        scheduleLogCleanup(null, "1s", params);
        countDownListener.waitTillCompleted();


        // Verify absence of data
        Assertions.assertThat(getCaseFileDataLogSize()).isZero();
        Assertions.assertThat(getCaseAssignmentRoleLogSize()).isZero();
        Assertions.assertThat(getProcessLogSize(LOG_INCOMPLETE_CLEANUP_ID)).isZero();
        Assertions.assertThat(getTaskLogSize(LOG_INCOMPLETE_CLEANUP_ID)).isZero();
        Assertions.assertThat(getNodeInstanceLogSize(LOG_INCOMPLETE_CLEANUP_ID)).isZero();
        Assertions.assertThat(getVariableLogSize(LOG_INCOMPLETE_CLEANUP_ID)).isZero();
    }

    @Test
    public void deletePartialLogsTest() throws Exception {
        CountDownAsyncJobListener countDownListener = new CountDownAsyncJobListener(1);
        ((ExecutorServiceImpl) getExecutorService()).addAsyncJobListener(countDownListener);
        // Generate data

        String caseIdComplete = caseService.startCase(DEPLOYMENT_UNIT, LOG_CLEANUP_ID); // complete
        logger.info("CASE CREATED: {}", caseIdComplete);
        String caseIdIncomplete = caseService.startCase(DEPLOYMENT_UNIT, LOG_INCOMPLETE_CLEANUP_ID); // cancelled
        logger.info("CASE CREATED: {}", caseIdIncomplete);
        // Advance time 1+ second forward
        Thread.sleep(1010);

        // Verify presence of data
        Assertions.assertThat(getCaseFileDataLogSize()).isEqualTo(2);
        Assertions.assertThat(getCaseAssignmentRoleLogSize()).isPositive();

        Map<String, Object> params = new HashMap<>();
        params.put("Status", "2");
        scheduleLogCleanup(null, "1s", params);
        countDownListener.waitTillCompleted();

        // Verify absence of data
        Assertions.assertThat(getCaseFileDataLogSize()).isEqualTo(1);
        Assertions.assertThat(getCaseAssignmentRoleLogSize()).isPositive();
        Assertions.assertThat(getProcessLogSize(LOG_CLEANUP_ID)).isZero();
        Assertions.assertThat(getTaskLogSize(LOG_CLEANUP_ID)).isZero();
        Assertions.assertThat(getNodeInstanceLogSize(LOG_CLEANUP_ID)).isZero();
        Assertions.assertThat(getVariableLogSize(LOG_CLEANUP_ID)).isZero();
        Assertions.assertThat(getProcessLogSize(LOG_INCOMPLETE_CLEANUP_ID)).isPositive();
        Assertions.assertThat(getTaskLogSize(LOG_INCOMPLETE_CLEANUP_ID)).isPositive();
        Assertions.assertThat(getNodeInstanceLogSize(LOG_INCOMPLETE_CLEANUP_ID)).isPositive();
        Assertions.assertThat(getVariableLogSize(LOG_INCOMPLETE_CLEANUP_ID)).isPositive();
    }

    // ------------------------ Helper Methods ------------------------



    protected ExecutorService getExecutorService() {
        if (executorService == null) {
            executorService = ExecutorServiceFactory.newExecutorService(getEmf());
            executorService.setThreadPoolSize(executorThreads);
            executorService.setRetries(executorRetries);
            executorService.setInterval(executorInterval);
            executorService.init();

            logger.debug("Created ExecutorService with parameters: '" + executorThreads + " threads', '" + executorRetries + " retries', interval '" + executorInterval + "s'");
        }
        return executorService;
    }

    private long getCaseAssignmentRoleLogSize() {
        EntityManager em = this.getEmf().createEntityManager();
        long count = (long) em.createQuery("SELECT COUNT(O) FROM CaseRoleAssignmentLog O").getSingleResult();
        em.close();
        return count;
    }

    private long getCaseFileDataLogSize() {
        EntityManager em = this.getEmf().createEntityManager();
        long count = (long) em.createQuery("SELECT COUNT(O) FROM CaseFileDataLog O").getSingleResult();
        em.close();
        return count;
    }

    private int getProcessLogSize(String processId) {
        return auditLogService.processInstanceLogQuery()
                              .processId(processId)
                              .build()
                              .getResultList()
                              .size();
    }

    private int getTaskLogSize(String processId) {
        return taskAuditService.auditTaskQuery()
                               .processId(processId)
                               .build()
                               .getResultList()
                               .size();
    }

    private int getNodeInstanceLogSize(String processId) {
        return auditLogService.nodeInstanceLogQuery()
                              .processId(processId)
                              .build()
                              .getResultList()
                              .size();
    }

    private int getVariableLogSize(String processId) {
        return auditLogService.variableInstanceLogQuery()
                              .processId(processId)
                              .build()
                              .getResultList()
                              .size();
    }

    private void scheduleLogCleanup(Date olderThan,
                                    String olderThanDuration,
                                    Map<String, Object> parameters) {
        CommandContext commandContext = new CommandContext();
        commandContext.setData("EmfName", "org.jbpm.test.persistence");
        commandContext.setData("SingleRun", "true");
        commandContext.setData("DateFormat", dateFormat);
        commandContext.setData("OlderThan", LocalDateTime.now().toString());

        Iterator<Map.Entry<String, Object>> iterator = parameters.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            commandContext.setData(entry.getKey(), entry.getValue());
        }

        getExecutorService().scheduleRequest("org.jbpm.casemgmt.impl.audit.CaseLogCleanupCommand", commandContext);
    }

    @Override
    protected DeploymentUnit prepareDeploymentUnit() throws Exception {
        identityProvider.setName(USER);
        return createAndDeployUnit(GROUP_ID, ARTIFACT_ID, VERSION);
    }

    @Override
    protected List<String> getProcessDefinitionFiles() {
        return Arrays.asList(LOG_CLEANUP, LOG_INCOMPLETE_CLEANUP);
    }

    protected void registerDefaultListenerMvelDefinitions() {
        // do nothing
    }
    @Override
    public DeploymentUnit createDeploymentUnit(String groupId, String artifactid, String version) throws Exception {
        listenerMvelDefinitions.clear();
        KModuleDeploymentUnit deploymentUnit = (KModuleDeploymentUnit) super.createDeploymentUnit(groupId, artifactid, version);
        DeploymentDescriptorImpl descriptor = ((DeploymentDescriptorImpl) deploymentUnit.getDeploymentDescriptor());
        descriptor.setPersistenceUnit(getPersistenceUnitName());
        descriptor.setAuditPersistenceUnit(getPersistenceUnitName());
        return deploymentUnit;
    }

}
