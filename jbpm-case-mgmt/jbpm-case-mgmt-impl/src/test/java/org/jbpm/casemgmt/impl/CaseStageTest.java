/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.casemgmt.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.kie.scanner.MavenRepository.getMavenRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.jbpm.casemgmt.api.model.AdHocFragment;
import org.jbpm.casemgmt.api.model.CaseDefinition;
import org.jbpm.casemgmt.api.model.CaseMilestone;
import org.jbpm.casemgmt.api.model.CaseRole;
import org.jbpm.casemgmt.api.model.CaseStage;
import org.jbpm.casemgmt.api.model.instance.CaseFileInstance;
import org.jbpm.casemgmt.api.model.instance.CaseInstance;
import org.jbpm.casemgmt.api.model.instance.CaseStageInstance;
import org.jbpm.casemgmt.api.model.instance.StageStatus;
import org.jbpm.casemgmt.impl.util.AbstractCaseServicesBaseTest;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.services.api.model.DeploymentUnit;
import org.jbpm.services.api.model.NodeInstanceDesc;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.task.impl.model.UserImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.query.QueryContext;
import org.kie.internal.query.QueryFilter;
import org.kie.scanner.MavenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CaseStageTest extends AbstractCaseServicesBaseTest {
    
    private static final Logger logger = LoggerFactory.getLogger(CaseStageTest.class);

    private List<DeploymentUnit> units = new ArrayList<DeploymentUnit>();

    public static final String SORT_BY_CASE_DEFINITION_NAME = "CaseName";

    public void prepare(String caseDefinition) {
        configureServices();
        KieServices ks = KieServices.Factory.get();
        ReleaseId releaseId = ks.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        List<String> processes = new ArrayList<String>();
        processes.add(caseDefinition);
        
        InternalKieModule kJar1 = createKieJar(ks, releaseId, processes);
        File pom = new File("target/kmodule", "pom.xml");
        pom.getParentFile().mkdir();
        try {
            FileOutputStream fs = new FileOutputStream(pom);
            fs.write(getPom(releaseId).getBytes());
            fs.close();
        } catch (Exception e) {
            
        }
        MavenRepository repository = getMavenRepository();
        repository.deployArtifact(releaseId, kJar1, pom);
    }

    @After
    public void cleanup() {
        identityProvider.reset();
        cleanupSingletonSessionId();
        if (units != null && !units.isEmpty()) {
            for (DeploymentUnit unit : units) {
                deploymentService.undeploy(unit);
            }
            units.clear();
        }
        close();
    }
    
    @Test
    public void testTransitionBetweenStagesInCase() {
        prepare("cases/CaseWithTwoStages.bpmn2");
        // use user name who is part of the case roles assignment
        // so (s)he will be authorized to access case instance
        identityProvider.setName("john");
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));
        
        assertNotNull(deploymentService);        
        DeploymentUnit deploymentUnit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);
        
        deploymentService.deploy(deploymentUnit);
        units.add(deploymentUnit);
        Map<String, Object> data = new HashMap<>();
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), TWO_STAGES_CASE_P_ID, data, roleAssignments);
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), TWO_STAGES_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        try {
            
            Collection<CaseStageInstance> stage = caseRuntimeDataService.getCaseInstanceStages(caseId, true, new QueryContext(0, 1));
            assertNotNull(stage);
            assertEquals(1, stage.size());
            assertEquals("Stage One", stage.iterator().next().getName());
            assertEquals(StageStatus.Active, stage.iterator().next().getStatus());
            
            Collection<AdHocFragment> adhocTasks = caseRuntimeDataService.getAdHocFragmentsForCase(caseId);
            assertNotNull(adhocTasks);
            assertEquals(1, adhocTasks.size());
            assertEquals("Task 1", adhocTasks.iterator().next().getName());
            
            Collection<NodeInstanceDesc> activeNodes = caseRuntimeDataService.getActiveNodesForCase(caseId, new QueryContext(0, 10));
            assertNotNull(activeNodes);
            assertEquals(1, activeNodes.size());
            assertEquals("Stage One", activeNodes.iterator().next().getName());
            
            caseService.addDataToCaseFile(caseId, "myCustomData", "nextStage");
            
            stage = caseRuntimeDataService.getCaseInstanceStages(caseId, true, new QueryContext(0, 1));
            assertNotNull(stage);
            assertEquals(1, stage.size());
            assertEquals("Stage Two", stage.iterator().next().getName());
            assertEquals(StageStatus.Active, stage.iterator().next().getStatus());
            
            adhocTasks = caseRuntimeDataService.getAdHocFragmentsForCase(caseId);
            assertNotNull(adhocTasks);
            assertEquals(1, adhocTasks.size());
            assertEquals("Task 2", adhocTasks.iterator().next().getName());
            
            activeNodes = caseRuntimeDataService.getActiveNodesForCase(caseId, new QueryContext(0, 10));
            assertNotNull(activeNodes);
            assertEquals(1, activeNodes.size());
            assertEquals("Stage Two", activeNodes.iterator().next().getName());
            
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testTransitionBetweenStagesWithConditionsInCase() {
        prepare("cases/CaseWithTwoStagesConditions.bpmn2");
        // use user name who is part of the case roles assignment
        // so (s)he will be authorized to access case instance
        identityProvider.setName("john");
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));
        
        assertNotNull(deploymentService);        
        DeploymentUnit deploymentUnit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);
        
        deploymentService.deploy(deploymentUnit);
        units.add(deploymentUnit);
        Map<String, Object> data = new HashMap<>();
        data.put("customData", "none");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), TWO_STAGES_CONDITIONS_CASE_P_ID, data, roleAssignments);
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), TWO_STAGES_CONDITIONS_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        try {
            
            Collection<CaseStageInstance> stage = caseRuntimeDataService.getCaseInstanceStages(caseId, true, new QueryContext(0, 1));
            assertNotNull(stage);
            assertEquals(1, stage.size());
            assertEquals("Stage One", stage.iterator().next().getName());
            assertEquals(StageStatus.Active, stage.iterator().next().getStatus());
            
            Collection<AdHocFragment> adhocTasks = caseRuntimeDataService.getAdHocFragmentsForCase(caseId);
            assertNotNull(adhocTasks);
            assertEquals(1, adhocTasks.size());
            assertEquals("Task 1", adhocTasks.iterator().next().getName());
            
            caseService.triggerAdHocFragment(caseId, "Task 1", null);
            
            List<TaskSummary> tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("john", new QueryFilter());
            assertNotNull(tasks);
            assertEquals(1, tasks.size());            
            assertTask(tasks.get(0), "john", "Task 1", Status.Reserved);
            
            Map<String, Object> params = new HashMap<>();
            params.put("myData", "nextStage");
            userTaskService.completeAutoProgress(tasks.get(0).getId(), "john", params);
            
            stage = caseRuntimeDataService.getCaseInstanceStages(caseId, true, new QueryContext(0, 1));
            assertNotNull(stage);
            assertEquals(1, stage.size());
            assertEquals("Stage Two", stage.iterator().next().getName());
            assertEquals(StageStatus.Active, stage.iterator().next().getStatus());
            
            adhocTasks = caseRuntimeDataService.getAdHocFragmentsForCase(caseId);
            assertNotNull(adhocTasks);
            assertEquals(1, adhocTasks.size());
            assertEquals("Task 2", adhocTasks.iterator().next().getName());
            
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }
    
}