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

package org.jbpm.test.functional.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.process.workitem.bpmn2.BusinessRuleTaskHandler;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.domain.Person;
import org.jbpm.test.domain.Structure;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Business rules task test. testing execution of rules with specified rule-flow group.
 */
public class BusinessRuleTaskTest extends JbpmTestCase {

    private static final String RULE_TASK = "org/jbpm/test/functional/task/businessRuleTaskProcess.bpmn2";
    private static final String RULE_TASK_ID = "org.jbpm.test.functional.task.businessRuleTask";

    private static final String RULE_TASK_COLLECTION = "org/jbpm/test/functional/task/businessRuleCollectionTaskProcess.bpmn2";
    private static final String RULE_TASK_COLLECTION_ID = "org.jbpm.test.functional.task.businessRuleTask";

    private static final String GROUP_ID = "org.jbpm";
    private static final String ARTIFACT_ID = "test-kjar";
    private static final String VERSION = "1.0";

    private KieServices ks = KieServices.Factory.get();

    public BusinessRuleTaskTest() {
        super(false);
    }

    @Test
    public void testRuleTask() throws Exception {
        // deploy external kjar
        createAndDeployJar(ks,
                           ks.newReleaseId(GROUP_ID,
                                           ARTIFACT_ID,
                                           VERSION),
                           ks.getResources().newClassPathResource("org/jbpm/test/functional/task/buildPersonDecision.dmn"),
                           ks.getResources().newFileSystemResource("src/main/java/org/jbpm/test/domain/Person.java"));

        // create the brt
        BusinessRuleTaskHandler handler = new BusinessRuleTaskHandler(GROUP_ID,
                                                                      ARTIFACT_ID,
                                                                      VERSION, 0, this.getClass().getClassLoader(), this.manager);
        addWorkItemHandler("BusinessRuleTask", handler);

        KieSession kieSession = createKSession(RULE_TASK);
        handler.setRuntimeManager(manager);

        WorkflowProcessInstanceImpl pi = (WorkflowProcessInstanceImpl) kieSession.startProcess(RULE_TASK_ID);
        Assert.assertTrue(pi.getVariable("person") instanceof Person);
    }

    @Test
    public void testBuildDMNStructureProcess() throws Exception {
        Map<String, ResourceType> resources = new HashMap<>();
        resources.put("org/jbpm/test/functional/task/buildDMNStructureProcess.bpmn", ResourceType.BPMN2);
        resources.put("org/jbpm/test/functional/task/buildDMNStructure.dmn", ResourceType.DMN);
        KieSession kieSession = createKSession(resources);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "john");
        params.put("age", 35);

        WorkflowProcessInstanceImpl pi = (WorkflowProcessInstanceImpl) kieSession.startProcess("dmn-bpmn.buildDMNStructureProcess", params);
        Assert.assertTrue(pi.getVariable("person") instanceof Person);

        Person variable = (Person) pi.getVariable("person");
        assertEquals("john",
                     variable.getName());
        assertEquals(35,
                     variable.getAge());
        assertNull(variable.getAddress());
        assertEquals(ProcessInstance.STATE_COMPLETED,
                     pi.getState());
    }

    @Test
    public void testBuildDMNNestedStructureProcess() throws Exception {
        Map<String, ResourceType> resources = new HashMap<>();
        resources.put("org/jbpm/test/functional/task/buildDMNNestedStructureProcess.bpmn", ResourceType.BPMN2);
        resources.put("org/jbpm/test/functional/task/buildDMNNestedStructure.dmn", ResourceType.DMN);
        KieSession kieSession = createKSession(resources);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "john");
        params.put("age", 35);
        params.put("street", "time square");

        WorkflowProcessInstanceImpl pi = (WorkflowProcessInstanceImpl) kieSession.startProcess("dmn-bpmn.buildDMNNestedStructureProcess", params);
        Assert.assertTrue(pi.getVariable("person") instanceof Person);

        Person variable = (Person) pi.getVariable("person");
        assertEquals("john",
                     variable.getName());
        assertEquals(35,
                     variable.getAge());
        assertEquals("time square", variable.getAddress().getStreet());
        assertEquals(ProcessInstance.STATE_COMPLETED,
                     pi.getState());
    }

    @Test
    public void testBuildDMNStructureFieldNameMismatchProcess() throws Exception {
        Map<String, ResourceType> resources = new HashMap<>();
        resources.put("org/jbpm/test/functional/task/buildDMNStructureFieldNameMismatchProcess.bpmn", ResourceType.BPMN2);
        resources.put("org/jbpm/test/functional/task/buildDMNStructureFieldNameMismatch.dmn", ResourceType.DMN);
        KieSession kieSession = createKSession(resources);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "john");
        params.put("age", 35);
        params.put("street", "time square");

        WorkflowProcessInstanceImpl pi = (WorkflowProcessInstanceImpl) kieSession.startProcess("dmn-bpmn.buildDMNStructureFieldNameMismatchProcess", params);
        Assert.assertTrue(pi.getVariable("person") instanceof Person);

        Person variable = (Person) pi.getVariable("person");
        assertEquals("", variable.getName());
        assertEquals(35, variable.getAge());
        assertEquals(ProcessInstance.STATE_COMPLETED, pi.getState());
    }

    @Test
    public void testBuildDMNWithMoreTypesStructureProcess() throws Exception {
        Map<String, ResourceType> resources = new HashMap<>();
        resources.put("org/jbpm/test/functional/task/buildDMNWithMoreTypesStructureProcess.bpmn", ResourceType.BPMN2);
        resources.put("org/jbpm/test/functional/task/buildDMNWithMoreTypesStructure.dmn", ResourceType.DMN);
        KieSession kieSession = createKSession(resources);

        Map<String, Object> params = new HashMap<>();

        WorkflowProcessInstanceImpl pi = (WorkflowProcessInstanceImpl) kieSession.startProcess("dmn-bpmn.buildDMNWithMoreTypesStructureProcess", params);
        Structure variable = (Structure) pi.getVariable("structure");
        assertEquals(Integer.valueOf(123456), variable.getId());
        assertEquals("value a", variable.getCache().get("keyA"));
        assertEquals("value b", variable.getCache().get("keyB"));
        assertEquals(LocalDate.of(2021, 6, 11), variable.getCurrentDate());
        assertEquals(LocalDateTime.of(LocalDate.of(2021, 6, 11), LocalTime.of(7, 49, 0)), variable.getCurrentDateTime());
        assertEquals(LocalTime.of(7, 49, 0), variable.getCurrentTime());
        assertEquals(1 * 24 * 60 * 60 + 23 * 60 * 60 + 12 * 60 + 30, variable.getDaysTimeDuration().getSeconds());
        assertEquals(3, variable.getYearsMonthsDuration().getYears());
        assertEquals(5, variable.getYearsMonthsDuration().getMonths());
        assertEquals(ProcessInstance.STATE_COMPLETED,
                     pi.getState());
    }

    @Test
    public void testBuildDMNDateProcess() throws Exception {
        Map<String, ResourceType> resources = new HashMap<>();
        resources.put("org/jbpm/test/functional/task/buildDMNDateProcess.bpmn", ResourceType.BPMN2);
        resources.put("org/jbpm/test/functional/task/buildDMNDate.dmn", ResourceType.DMN);
        KieSession kieSession = createKSession(resources);

        Map<String, Object> params = new HashMap<>();

        WorkflowProcessInstanceImpl pi = (WorkflowProcessInstanceImpl) kieSession.startProcess("dmn-bpmn.buildDMNDateProcess", params);
        LocalDate variable = (LocalDate) pi.getVariable("date");
        assertNotNull(variable);
        assertEquals(ProcessInstance.STATE_COMPLETED,
                     pi.getState());
    }

    @Test
    public void testBuildDMNPeriodProcess() throws Exception {
        Map<String, ResourceType> resources = new HashMap<>();
        resources.put("org/jbpm/test/functional/task/buildDMNPeriodProcess.bpmn", ResourceType.BPMN2);
        resources.put("org/jbpm/test/functional/task/buildDMNPeriod.dmn", ResourceType.DMN);
        KieSession kieSession = createKSession(resources);

        Map<String, Object> params = new HashMap<>();

        WorkflowProcessInstanceImpl pi = (WorkflowProcessInstanceImpl) kieSession.startProcess("dmn-bpmn.buildDMNPeriodProcess", params);
        Period variable = (Period) pi.getVariable("period");
        assertEquals(1, variable.getYears());
        assertEquals(5, variable.getMonths());
        assertEquals(ProcessInstance.STATE_COMPLETED, pi.getState());
    }

    @Test
    public void testRuleTaskCollection() throws Exception {
        // deploy external kjar
        createAndDeployJar(ks,
                           ks.newReleaseId(GROUP_ID,
                                           ARTIFACT_ID,
                                           VERSION),
                           ks.getResources().newClassPathResource("org/jbpm/test/functional/task/buildPersonCollectionDecision.dmn"),
                           ks.getResources().newFileSystemResource("src/main/java/org/jbpm/test/domain/Person.java"));

        // create the brt
        BusinessRuleTaskHandler handler = new BusinessRuleTaskHandler(GROUP_ID,
                                                                      ARTIFACT_ID,
                                                                      VERSION, 0, this.getClass().getClassLoader(), this.manager);
        addWorkItemHandler("BusinessRuleTask", handler);

        KieSession kieSession = createKSession(RULE_TASK_COLLECTION);
        handler.setRuntimeManager(manager);

        WorkflowProcessInstanceImpl pi = (WorkflowProcessInstanceImpl) kieSession.startProcess(RULE_TASK_COLLECTION_ID);
        Assert.assertTrue(pi.getVariable("person") instanceof List);
        Assert.assertTrue(((List<Person>) pi.getVariable("person")).get(0) instanceof Person);
    }

    public KieSession createKSession(Map<String, ResourceType> res) {
        createRuntimeManager(res);
        return getRuntimeEngine().getKieSession();
    }
}