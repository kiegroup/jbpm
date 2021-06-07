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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jbpm.process.workitem.bpmn2.BusinessRuleTaskHandler;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.domain.Person;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;

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
                VERSION,0, this.getClass().getClassLoader(), this.manager);
        addWorkItemHandler("BusinessRuleTask", handler);

        KieSession kieSession = createKSession(RULE_TASK);
        handler.setRuntimeManager(manager);

        WorkflowProcessInstanceImpl pi = (WorkflowProcessInstanceImpl) kieSession.startProcess(RULE_TASK_ID);
        Assert.assertTrue(pi.getVariable("person") instanceof Person);
        
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
                VERSION,0, this.getClass().getClassLoader(), this.manager);
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