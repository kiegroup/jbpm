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

package org.jbpm.kie.services.impl.admin;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.kie.services.test.KModuleDeploymentServiceTest;
import org.jbpm.kie.test.util.AbstractKieServicesBaseTest;
import org.jbpm.kie.test.util.CountDownListenerFactory;
import org.jbpm.services.api.ProcessInstanceNotFoundException;
import org.jbpm.services.api.admin.ProcessInstanceAdminService;
import org.jbpm.services.api.admin.ProcessNode;
import org.jbpm.services.api.admin.TimerInstance;
import org.jbpm.services.api.model.DeploymentUnit;
import org.jbpm.services.api.model.NodeInstanceDesc;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.query.QueryContext;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.query.QueryFilter;
import org.kie.internal.runtime.conf.ObjectModel;
import org.kie.internal.runtime.error.ExecutionError;
import org.kie.scanner.KieMavenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.kie.scanner.KieMavenRepository.getKieMavenRepository;

public class ProcessInstanceAdminServiceImplTest extends AbstractKieServicesBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(KModuleDeploymentServiceTest.class);
    protected static final String ADMIN_ARTIFACT_ID = "test-admin";
    protected static final String ADMIN_GROUP_ID = "org.jbpm.test";
    protected static final String ADMIN_VERSION_V1 = "1.0.0";
    
    private List<DeploymentUnit> units = new ArrayList<DeploymentUnit>();
    
    private KModuleDeploymentUnit deploymentUnit;
    private Long processInstanceId = null;
    
    @Before
    public void prepare() {
        configureServices();
        logger.debug("Preparing kjar");
        KieServices ks = KieServices.Factory.get();
        
        // version 1 of kjar
        ReleaseId releaseId = ks.newReleaseId(ADMIN_GROUP_ID, ADMIN_ARTIFACT_ID, ADMIN_VERSION_V1);
        List<String> processes = new ArrayList<String>();
        processes.add("repo/processes/general/humanTask.bpmn");
        processes.add("repo/processes/general/boundarytimer.bpmn2");
        processes.add("repo/processes/general/BPMN2-IntermediateCatchEventTimerDuration.bpmn2");
        processes.add("repo/processes/general/BPMN2-UserTaskWithSLAOnTask.bpmn2");
        processes.add("repo/processes/errors/BPMN2-BrokenScriptTask.bpmn2");
        processes.add("repo/processes/errors/BPMN2-UserTaskWithRollback.bpmn2");
        processes.add("repo/processes/general/AdHocSubProcess.bpmn2");
        processes.add("repo/processes/general/BPMN2-ProcessSLA.bpmn2");
        processes.add("repo/processes/general/BPMN2-SuspendUntil.bpmn2");

        InternalKieModule kJar1 = createKieJar(ks, releaseId, processes);
        File pom = new File("target/admin", "pom.xml");
        pom.getParentFile().mkdir();
        try {
            FileOutputStream fs = new FileOutputStream(pom);
            fs.write(getPom(releaseId).getBytes());
            fs.close();
        } catch (Exception e) {

        }
        KieMavenRepository repository = getKieMavenRepository();
        repository.installArtifact(releaseId, kJar1, pom);
        
        // now let's deploy to runtime both kjars
        deploymentUnit = new KModuleDeploymentUnit(ADMIN_GROUP_ID, ADMIN_ARTIFACT_ID, ADMIN_VERSION_V1);
        deploymentService.deploy(deploymentUnit);
        units.add(deploymentUnit);
    }

    @After
    public void cleanup() {
        cleanupSingletonSessionId();
        if (processInstanceId != null) {
            try {
                // let's abort process instance to leave the system in clear state
                processService.abortProcessInstance(processInstanceId);
                
                ProcessInstance pi = processService.getProcessInstance(processInstanceId);      
                assertNull(pi);
            } catch (ProcessInstanceNotFoundException e) {
                // ignore it as it might already be completed/aborted
            }
        }
        if (units != null && !units.isEmpty()) {
            for (DeploymentUnit unit : units) {
                try {
                deploymentService.undeploy(unit);
                } catch (Exception e) {
                    // do nothing in case of some failed tests to avoid next test to fail as well
                }
            }
            units.clear();
        }
        close();
        CountDownListenerFactory.clear();
    }
    
    
    public void setProcessAdminService(ProcessInstanceAdminService processAdminService) {
        this.processAdminService = processAdminService;
    }

    @Test
    public void testGetNodes() {
        processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), "org.jbpm.writedocument");
        assertNotNull(processInstanceId);
        
        Collection<ProcessNode> processNodes = processAdminService.getProcessNodes(processInstanceId);
        assertNotNull(processNodes);
        assertEquals(8, processNodes.size());
        
        Map<String, String> mappedNodes = processNodes.stream().collect(Collectors.toMap(ProcessNode::getNodeName, ProcessNode::getNodeType));
        assertEquals("StartNode", mappedNodes.get("Start"));
        assertEquals("HumanTaskNode", mappedNodes.get("Write a Document"));
        assertEquals("Split", mappedNodes.get("Review and Translate"));
        assertEquals("HumanTaskNode", mappedNodes.get("Translate Document"));
        assertEquals("HumanTaskNode", mappedNodes.get("Review Document"));
        assertEquals("Join", mappedNodes.get("Reviewed and Translated"));
        assertEquals("ActionNode", mappedNodes.get("Report"));
        assertEquals("EndNode", mappedNodes.get("End"));
    }

    
    @Test
    public void testQuerySlaProcess() {
        processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), "test.processSQLquery");
        Collection<TimerInstance> timers = processAdminService.getTimerInstances(processInstanceId);
        assertEquals(1, timers.size());
        processService.abortProcessInstance(processInstanceId);
    }

    @Test
    public void testCancelAndTrigger() {
        processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), "org.jbpm.writedocument");
        assertNotNull(processInstanceId);
        
        Collection<NodeInstanceDesc> activeNodes = processAdminService.getActiveNodeInstances(processInstanceId);
        assertNotNull(activeNodes);
        assertEquals(1, activeNodes.size());
        
        NodeInstanceDesc active = activeNodes.iterator().next();       
        assertEquals("Write a Document", active.getName());
        
        List<TaskSummary> tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("salaboy", new QueryFilter());
        assertEquals(1, tasks.size());
        
        processAdminService.cancelNodeInstance(processInstanceId, active.getId());
        
        activeNodes = processAdminService.getActiveNodeInstances(processInstanceId);
        assertNotNull(activeNodes);
        assertEquals(0, activeNodes.size());
        tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("salaboy", new QueryFilter());
        assertEquals(0, tasks.size());
        
        Collection<ProcessNode> processNodes = processAdminService.getProcessNodes(processInstanceId);
        ProcessNode writeDocNode = processNodes.stream().filter(pn -> pn.getNodeName().equals(active.getName())).findFirst().orElse(null);
        
        processAdminService.triggerNode(processInstanceId, writeDocNode.getNodeId());
        activeNodes = processAdminService.getActiveNodeInstances(processInstanceId);
        assertNotNull(activeNodes);
        assertEquals(1, activeNodes.size());
        
        tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("salaboy", new QueryFilter());
        assertEquals(1, tasks.size());
    }
    
    @Test
    public void testTriggerSubProcessNodeInstance() {
        processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), "AdHocSubProcess");
        assertNotNull(processInstanceId);

        Collection<NodeInstanceDesc> activeNodes = processAdminService.getActiveNodeInstances(processInstanceId);
        assertNotNull(activeNodes);
        assertEquals(1, activeNodes.size());

        processService.signalProcessInstance(processInstanceId, "Hello1", null);

        activeNodes = processAdminService.getActiveNodeInstances(processInstanceId);
        assertNotNull(activeNodes);
        assertEquals(2, activeNodes.size());

        Collection<ProcessNode> processNodes = processAdminService.getProcessNodes(processInstanceId);
        ProcessNode taskNode = processNodes.stream().filter(p -> p.getNodeName().equals("Hello1")).findFirst().orElse(null);
        assertNotNull(taskNode);

        processAdminService.triggerNode(processInstanceId, taskNode.getNodeId());

        activeNodes = processAdminService.getActiveNodeInstances(processInstanceId);
        assertNotNull(activeNodes);
        assertEquals(3, activeNodes.size());

        Iterator<NodeInstanceDesc> iterator = activeNodes.iterator();
        NodeInstanceDesc nodeInstanceDesc= iterator.next();
        assertEquals("DynamicNode",nodeInstanceDesc.getNodeType());
        assertEquals("Hello",nodeInstanceDesc.getName());

        nodeInstanceDesc= iterator.next();
        assertEquals("HumanTaskNode",nodeInstanceDesc.getNodeType());
        assertEquals("Hello1",nodeInstanceDesc.getName());

        nodeInstanceDesc= iterator.next();
        assertEquals("HumanTaskNode",nodeInstanceDesc.getNodeType());
        assertEquals("Hello1",nodeInstanceDesc.getName());
    }

    @Test
    public void testReTriggerNodeInstance() {
        processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), "org.jbpm.writedocument");
        assertNotNull(processInstanceId);
        
        Collection<NodeInstanceDesc> activeNodes = processAdminService.getActiveNodeInstances(processInstanceId);
        assertNotNull(activeNodes);
        assertEquals(1, activeNodes.size());
        
        NodeInstanceDesc active = activeNodes.iterator().next();       
        assertEquals("Write a Document", active.getName());
        
        List<TaskSummary> tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("salaboy", new QueryFilter());
        assertEquals(1, tasks.size());
        TaskSummary task = tasks.get(0);

        // aborts the current node
        processAdminService.retriggerNodeInstance(processInstanceId, active.getId());
                
        activeNodes = processAdminService.getActiveNodeInstances(processInstanceId);
        assertNotNull(activeNodes);
        assertEquals(1, activeNodes.size());

        // this does not return aborted node instances
        Collection<NodeInstanceDesc> completedNodes = runtimeDataService.getProcessInstanceHistoryCompleted(processInstanceId, new QueryFilter());
        assertNotNull(completedNodes);
        assertEquals(1, completedNodes.size());

        final List<NodeInstanceDesc> nodeInstances = completedNodes.stream().filter(node -> node.getId().equals(active.getId())).collect(Collectors.toList());
        assertEquals(0, nodeInstances.size());

        NodeInstanceDesc activeRetriggered = activeNodes.iterator().next();        
        assertFalse(active.getId().longValue() == activeRetriggered.getId().longValue());
        
        tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("salaboy", new QueryFilter());
        assertEquals(1, tasks.size());
        TaskSummary taskRetriggered = tasks.get(0);
        
        assertFalse(task.getId().longValue() == taskRetriggered.getId().longValue());
    }
    
    @Test
    public void testCancelAndTriggerAnotherNode() {
        processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), "org.jbpm.writedocument");
        assertNotNull(processInstanceId);
        
        Collection<NodeInstanceDesc> activeNodes = processAdminService.getActiveNodeInstances(processInstanceId);
        assertNotNull(activeNodes);
        assertEquals(1, activeNodes.size());
        
        NodeInstanceDesc active = activeNodes.iterator().next();       
        assertEquals("Write a Document", active.getName());
        
        List<TaskSummary> tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("salaboy", new QueryFilter());
        assertEquals(1, tasks.size());
        
        processAdminService.cancelNodeInstance(processInstanceId, active.getId());
        
        activeNodes = processAdminService.getActiveNodeInstances(processInstanceId);
        assertNotNull(activeNodes);
        assertEquals(0, activeNodes.size());
        tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("salaboy", new QueryFilter());
        assertEquals(0, tasks.size());
        
        Collection<ProcessNode> processNodes = processAdminService.getProcessNodes(processInstanceId);
        ProcessNode writeDocNode = processNodes.stream().filter(pn -> pn.getNodeName().equals("Report")).findFirst().orElse(null);
        
        processAdminService.triggerNode(processInstanceId, writeDocNode.getNodeId());
        activeNodes = processAdminService.getActiveNodeInstances(processInstanceId);
        assertNotNull(activeNodes);
        assertEquals(0, activeNodes.size());
        
        tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("salaboy", new QueryFilter());
        assertEquals(0, tasks.size());
        
        ProcessInstanceDesc pi = runtimeDataService.getProcessInstanceById(processInstanceId);
        assertEquals(ProcessInstance.STATE_COMPLETED, pi.getState().intValue());
        
        processInstanceId = null;
    }
    
    @Test
    public void testTriggerLastActionNode() {
        processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), "org.jbpm.writedocument");
        assertNotNull(processInstanceId);
        
        Collection<NodeInstanceDesc> activeNodes = processAdminService.getActiveNodeInstances(processInstanceId);
        assertNotNull(activeNodes);
        assertEquals(1, activeNodes.size());
        
        NodeInstanceDesc active = activeNodes.iterator().next();       
        assertEquals("Write a Document", active.getName());
        
        List<TaskSummary> tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("salaboy", new QueryFilter());
        assertEquals(1, tasks.size());
                
        Collection<ProcessNode> processNodes = processAdminService.getProcessNodes(processInstanceId);
        ProcessNode writeDocNode = processNodes.stream().filter(pn -> pn.getNodeName().equals("Report")).findFirst().orElse(null);
        
        processAdminService.triggerNode(processInstanceId, writeDocNode.getNodeId());
        activeNodes = processAdminService.getActiveNodeInstances(processInstanceId);
        assertNotNull(activeNodes);
        assertEquals(0, activeNodes.size());
        
        tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("salaboy", new QueryFilter());
        assertEquals(0, tasks.size());
        
        ProcessInstanceDesc pi = runtimeDataService.getProcessInstanceById(processInstanceId);
        assertEquals(ProcessInstance.STATE_COMPLETED, pi.getState().intValue());
        
        processInstanceId = null;
    }
    
    @Test(timeout=10000)
    public void testUpdateTimer() throws Exception {
        processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), "IntermediateCatchEvent");
        assertNotNull(processInstanceId);
        long scheduleTime = System.currentTimeMillis();
        
        Collection<NodeInstanceDesc> activeNodes = processAdminService.getActiveNodeInstances(processInstanceId);
        assertNotNull(activeNodes);
        assertEquals(1, activeNodes.size());
        
        NodeInstanceDesc active = activeNodes.iterator().next();       
        assertEquals("timer", active.getName());
        
        Collection<TimerInstance> timers = processAdminService.getTimerInstances(processInstanceId);
        assertNotNull(timers);
        assertEquals(1, timers.size());
        
        TimerInstance timer = timers.iterator().next();
        assertNotNull(timer.getActivationTime());
        assertNotNull(timer.getDelay());
        assertNotNull(timer.getNextFireTime());
        assertNotNull(timer.getProcessInstanceId());
        assertNotNull(timer.getSessionId());
        assertNotNull(timer.getTimerId());
        assertNotNull(timer.getId());
        assertNotNull(timer.getTimerName());
        // thread sleep to test the different in the time timer spent after upgrade
        // not to wait for any job to be done
        Thread.sleep(1000);
        
        processAdminService.updateTimer(processInstanceId, timer.getId(), 3, 0, 0);
        
        CountDownListenerFactory.getExisting("processAdminService").waitTillCompleted();
        long fireTime = System.currentTimeMillis();        
        long expirationTime = fireTime - scheduleTime;
        //since the update of timer was including time already spent (thread sleep above) then it must wait less than 4 secs
        assertTrue(expirationTime < 4000);
       
        ProcessInstanceDesc pi = runtimeDataService.getProcessInstanceById(processInstanceId);
        assertEquals(ProcessInstance.STATE_COMPLETED, pi.getState().intValue());
        
        processInstanceId = null;
    }
    
    @Test(timeout=10000)
    public void testListSLATimer() throws Exception {
        processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), "UserTaskWithSLAOnTask");
        assertNotNull(processInstanceId);
        
        Collection<NodeInstanceDesc> activeNodes = processAdminService.getActiveNodeInstances(processInstanceId);
        assertNotNull(activeNodes);
        assertEquals(1, activeNodes.size());
        
        Collection<TimerInstance> timers = processAdminService.getTimerInstances(processInstanceId);
        assertNotNull(timers);
        assertEquals(1, timers.size());
        
        TimerInstance timer = timers.iterator().next();
        assertNotNull(timer.getActivationTime());
        assertNotNull(timer.getDelay());
        assertNotNull(timer.getNextFireTime());
        assertNotNull(timer.getProcessInstanceId());
        assertNotNull(timer.getSessionId());
        assertNotNull(timer.getTimerId());
        assertNotNull(timer.getId());
        assertNotNull(timer.getTimerName());
    }

    @Test(timeout=10000)
    public void testListSuspendUntilTimer() throws Exception {
        processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), "humanTaskWithSuspendUntil");
        assertNotNull(processInstanceId);

        List<Long> ids = this.runtimeDataService.getTasksByProcessInstanceId(processInstanceId);
        ids.forEach(id -> {
            this.userTaskService.suspend(id, "Administrator");
        });

        Collection<TimerInstance> timers = processAdminService.getTimerInstances(processInstanceId);
        assertNotNull(timers);
        assertEquals(1, timers.size());

        TimerInstance timer = timers.iterator().next();
        assertNotNull(timer.getActivationTime());
        assertEquals(2000L, timer.getDelay());
        assertNotNull(timer.getNextFireTime());
        assertNotNull(timer.getProcessInstanceId());
        assertNotNull(timer.getSessionId());
        assertNotNull(timer.getTimerId());
        assertTrue(timer.getId() >= 0);
        assertTrue(timer.getTimerName().startsWith("[SuspendUntil]"));

        processService.abortProcessInstance(processInstanceId);
    }

    @Test(timeout = 10000)
    public void testTimerName() throws Exception {
        processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), "org.jbpm.boundarytimer");
        assertNotNull(processInstanceId);

        Collection<TimerInstance> timers = processAdminService.getTimerInstances(processInstanceId);
        assertNotNull(timers);
        assertEquals(1, timers.size());

        TimerInstance timer = timers.iterator().next();
        assertEquals("usertask-timer", timer.getTimerName());
    }
    
    
        
    @Test(timeout=10000)
    public void testUpdateTimerRelative() throws Exception {
        processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), "IntermediateCatchEvent");
        assertNotNull(processInstanceId);
        long scheduleTime = System.currentTimeMillis();
        
        Collection<NodeInstanceDesc> activeNodes = processAdminService.getActiveNodeInstances(processInstanceId);
        assertNotNull(activeNodes);
        assertEquals(1, activeNodes.size());
        
        NodeInstanceDesc active = activeNodes.iterator().next();       
        assertEquals("timer", active.getName());
        
        Collection<TimerInstance> timers = processAdminService.getTimerInstances(processInstanceId);
        assertNotNull(timers);
        assertEquals(1, timers.size());
        
        TimerInstance timer = timers.iterator().next();
        assertNotNull(timer.getActivationTime());
        assertNotNull(timer.getDelay());
        assertNotNull(timer.getNextFireTime());
        assertNotNull(timer.getProcessInstanceId());
        assertNotNull(timer.getSessionId());
        assertNotNull(timer.getId());
        assertNotNull(timer.getTimerId());
        assertNotNull(timer.getTimerName());
        // thread sleep to test the different in the time timer spent after upgrade
        // not to wait for any job to be done
        Thread.sleep(1000);
        
        processAdminService.updateTimerRelative(processInstanceId, timer.getId(), 3, 0, 0);
        
        CountDownListenerFactory.getExisting("processAdminService").waitTillCompleted();
        long fireTime = System.currentTimeMillis();
        //since the update of timer was relative (to current time) then it must wait at least 3 secs
        long expirationTime = fireTime - scheduleTime;
        assertTrue(expirationTime > 3000);
       
        ProcessInstanceDesc pi = runtimeDataService.getProcessInstanceById(processInstanceId);
        assertEquals(ProcessInstance.STATE_COMPLETED, pi.getState().intValue());
        
        processInstanceId = null;
    }
    
    @Test
    public void testErrorHandlingOnScriptTask() {
        
        try {
            processService.startProcess(deploymentUnit.getIdentifier(), "BrokenScriptTask");
        } catch (Exception e) {
            // expected as this is broken script process
        }
        
        List<ExecutionError> errors = processAdminService.getErrors(true, new QueryContext());
        assertNotNull(errors);
        assertEquals(1, errors.size());
        
        ExecutionError error = errors.get(0);
        assertNotNull(error);
        assertFalse(error.isAcknowledged());
        
        processAdminService.acknowledgeError(error.getErrorId());
        
        errors = processAdminService.getErrors(true, new QueryContext());
        assertNotNull(errors);
        assertEquals(1, errors.size());
        
        error = errors.get(0);
        assertNotNull(error);
        assertTrue(error.isAcknowledged());
    }

    @Test
    public void testErrorByDeploymentId() {

        try {
            processService.startProcess(deploymentUnit.getIdentifier(), "BrokenScriptTask");
        } catch (Exception e) {
            // expected as this is broken script process
        }

        List<ExecutionError> errors = processAdminService.getErrors(true, new QueryContext());
        assertNotNull(errors);
        assertEquals(1, errors.size());

        ExecutionError error = errors.get(0);
        assertNotNull(error);

        // try non empty deploymentId
        String deploymentId = error.getDeploymentId();
        errors = processAdminService.getErrorsByDeploymentId(deploymentId, true, new QueryContext());
        assertNotNull(errors);
        assertEquals(1, errors.size());

        // try empty deploymentId
        errors = processAdminService.getErrorsByDeploymentId("empty-deployment-id", true, new QueryContext());
        assertNotNull(errors);
        assertEquals(0, errors.size());
    }
    /*
     * Helper methods 
     */
    @Override
    protected List<ObjectModel> getProcessListeners() {
        List<ObjectModel> listeners = super.getProcessListeners();
        
        listeners.add(new ObjectModel("mvel", "org.jbpm.kie.test.util.CountDownListenerFactory.get(\"processAdminService\", \"timer\", 1)"));
        
        return listeners;
    }
    
    protected boolean createDescriptor() {
        return true;
    }
}
