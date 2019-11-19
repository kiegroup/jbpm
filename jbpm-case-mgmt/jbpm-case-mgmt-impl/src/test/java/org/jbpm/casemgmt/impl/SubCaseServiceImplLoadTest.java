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

package org.jbpm.casemgmt.impl;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import org.assertj.core.util.Files;
import org.jbpm.casemgmt.api.model.CaseStatus;
import org.jbpm.casemgmt.api.model.instance.CaseFileInstance;
import org.jbpm.casemgmt.api.model.instance.CaseInstance;
import org.jbpm.casemgmt.impl.util.AbstractCaseServicesBaseTest;
import org.jbpm.services.task.impl.model.UserImpl;
import org.junit.Test;
import org.kie.api.runtime.query.QueryContext;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.TaskSummary;
import org.kie.test.util.db.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SubCaseServiceImplLoadTest extends AbstractCaseServicesBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(SubCaseServiceImplLoadTest.class);

    protected static final String BASIC_SUB_CASE_P_ID = "CaseWithSubCase";
    protected static final String QUERY_SUB_CASE_P_ID = "CaseWithSubCaseQuery";
    protected static final String SUB_CASE_ID = "SUB-0000000001";
    protected static final String MID_CASE_ID = "IT-0000000001";
    protected static final String PROCESS_TO_CASE_P_ID = "process2case";
    
    @Override
    protected List<String> getProcessDefinitionFiles() {
        List<String> processes = new ArrayList<String>();
        processes.add("cases/CaseWithSubCase.bpmn2");
        processes.add("cases/CaseWithSubCaseQuery.bpmn2");
        processes.add("cases/UserTaskCase.bpmn2");
        processes.add("cases/EmptyCase.bpmn2");
        processes.add("cases/SubSubCase.bpmn2");
        // add processes that can be used by cases but are not cases themselves
        processes.add("processes/DataVerificationProcess.bpmn2");
        processes.add("processes/Process2Case.bpmn2");
        return processes;
    }

    protected void buildDatasource() {
        Properties driverProperties = new Properties();
        driverProperties.put("user", "root");
        driverProperties.put("password", "redhat1!");
        driverProperties.put("url", "jdbc:mysql://localhost:3306/rhpam7");
        driverProperties.put("driverClassName", "com.mysql.jdbc.Driver");
        driverProperties.put("className", "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
        driverProperties.put("databaseName", "rhpam7");
        driverProperties.put("portNumber", "3306");
        driverProperties.put("serverName", "localhost");

        ds = DataSourceFactory.setupPoolingDataSource("jdbc/testDS1", driverProperties);
        try (Statement sts = ds.getConnection().createStatement()) {
            sts.execute("DROP DATABASE IF EXISTS rhpam7");
            sts.execute("CREATE DATABASE rhpam7");
            sts.execute("USE rhpam7");
            for (String statement : getStatements("/home/egonzale/github/jbpm/jbpm-installer/src/main/resources/db/ddl-scripts/mysqlinnodb/mysql-innodb-jbpm-schema.sql")) {
                sts.execute(statement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String[] getStatements(String file) {
        String content = Files.contentOf(new File(file), Charset.forName("UTF-8"));
        StringTokenizer tokenizer = new StringTokenizer(content, ";");
        List<String> strs = new ArrayList<String>();
        while (tokenizer.hasMoreTokens()) {
            String str = tokenizer.nextToken();
            if (!str.isEmpty() && !str.trim().isEmpty()) {
                strs.add(str);
            }
        }
        return strs.stream().toArray(String[]::new);

    }

    @Test
    public void testCaseWithSubCaseQuery() {

        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));
        roleAssignments.put("manager", new UserImpl("mary"));

        Map<String, Object> data = new HashMap<>();
        data.put("name", "John Doe");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), BASIC_SUB_CASE_P_ID, data, roleAssignments);

        List<String> caseIds = new ArrayList<>();
        for (int i = 0; i < 1400; i++) {
            String caseId = caseService.startCase(deploymentUnit.getIdentifier(), QUERY_SUB_CASE_P_ID, caseFile);
            if (i % 200 == 0 && i > 0) {
                logger.info("Cases created {}", i);
            }
            caseIds.add(caseId);
        }

        try {
            for (int i = 0; i < caseIds.size(); i++) {
                String caseId = caseIds.get(i);
                if (i % 200 == 0 && i > 0) {
                    logger.info("Processing Subcase {}", i);
                }
                CaseInstance cInstance = caseService.getCaseInstance(caseId);
                assertNotNull(cInstance);
                assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

                caseService.triggerAdHocFragment(caseId, "Sub Case", null);

                // we check the parent
                List<CaseStatus> allStatus = Arrays.asList(CaseStatus.values());
                Collection<CaseInstance> subcases = caseRuntimeDataService.getSubCaseInstancesByParentCaseId(caseId, allStatus, new QueryContext());
                assertNotNull(subcases);
                assertEquals(caseId, subcases.iterator().next().getParentCaseId());

                Collection<CaseInstance> allSubcases = caseRuntimeDataService.getAllDescendantSubCaseInstancesByParentCaseId(caseId, allStatus);
                assertNotNull(allSubcases);
                assertEquals(1, allSubcases.size());

                allSubcases.stream().forEach((e) -> {
                    List<TaskSummary> tasks = caseRuntimeDataService.getCaseTasksAssignedAsPotentialOwner(e.getCaseId(), "john", null, new QueryContext());
                    for (TaskSummary task : tasks) {
                        processService.abortProcessInstance(task.getProcessInstanceId());
                    }
                });
            }

            long start = System.nanoTime();
            Collection<CaseInstance> instances = caseRuntimeDataService.getCaseInstances(null);
            long end = System.nanoTime();
            logger.info("loaded {} instances in {}", instances.size(), TimeUnit.SECONDS.convert(end - start, TimeUnit.NANOSECONDS));
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        }
    }

}
