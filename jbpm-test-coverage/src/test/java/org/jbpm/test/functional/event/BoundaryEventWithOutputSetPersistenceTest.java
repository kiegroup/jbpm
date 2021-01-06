/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.test.functional.event;

import java.util.HashMap;
import java.util.Map;

import org.drools.core.process.instance.WorkItemHandler;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.entity.Person;
import org.jbpm.workflow.instance.node.CompositeContextNodeInstance;
import org.jbpm.workflow.instance.node.HumanTaskNodeInstance;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;



public class BoundaryEventWithOutputSetPersistenceTest extends JbpmTestCase {

    // General setup
    private static final Logger logger = LoggerFactory.getLogger(BoundaryEventWithOutputSetPersistenceTest.class);

    
    private final static String PROCESS_TIMER_FILE_NAME = "org/jbpm/test/functional/event/BoundaryTimerProcessWithOutputSet.bpmn2";
    private final static String PROCESS_NAME = "BoundaryTimerEventProcess";
    
    private final static String PROCESS_MESSAGE_FILE_NAME = "org/jbpm/test/functional/event/BoundaryMessageProcessWithOutputSet.bpmn2";
    private final static String PROCESS_MESSAGE_NAME = "BoundaryMessageEventProcess";
    
    private final static String PROCESS_SIGNAL_FILE_NAME = "org/jbpm/test/functional/event/BoundarySignalProcessWithOutputSet.bpmn2";
    private final static String PROCESS_SIGNAL_NAME = "BoundarySignalEventProcess";
    
    private final static String PROCESS_ERROR_FILE_NAME = "org/jbpm/test/functional/event/BoundaryErrorProcessWithOutputSet.bpmn2";
    private final static String PROCESS_ERROR_NAME = "BoundaryErrorEventProcess";
    
    private final static String PROCESS_ESCALATION_FILE_NAME = "org/jbpm/test/functional/event/BoundaryEscalationProcessWithOutputSet.bpmn2";
    private final static String PROCESS_ESCALATION_NAME = "BoundaryEscalationEventProcess";
    
    private final static String PROCESS_COMPENSATION_FILE_NAME = "org/jbpm/test/functional/event/BoundaryCompensationProcessWithOutputSet.bpmn2";
    private final static String PROCESS_COMPENSATION_NAME = "BoundaryCompensationEventProcess";
    
    private final static String PROCESS_CONDITIONAL_FILE_NAME = "org/jbpm/test/functional/event/BoundaryConditionalProcessWithOutputSet.bpmn2";
    private final static String PROCESS_CONDITIONAL_NAME = "BoundaryConditionalEventProcess";
    
    private final static String WORK_ITEM_HANDLER_TASK = "Human Task";
    private final static String WORK_ITEM_HANDLER_DATA = "DataCaptureTask";
    

    public BoundaryEventWithOutputSetPersistenceTest() { 
        super(true, true);
    }
        
    @Test
    public void testBoundaryEventTimerAndCompleteHumanTask() throws InterruptedException {
        
        createRuntimeManager(PROCESS_TIMER_FILE_NAME);

        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession ksession = runtimeEngine.getKieSession();
        long ksessionId = ksession.getIdentifier();
        assertTrue("session id not saved.", ksessionId > 0);
        
        HumanTaskMockHandler humanTaskMockHandler = new HumanTaskMockHandler();
        DataTaskMockHandler dataTaskMockHandler = new DataTaskMockHandler();
        // Register Human Task Handler
        ksession.getWorkItemManager().registerWorkItemHandler(WORK_ITEM_HANDLER_TASK, humanTaskMockHandler);
        ksession.getWorkItemManager().registerWorkItemHandler(WORK_ITEM_HANDLER_DATA, dataTaskMockHandler);
        // Start the process 
        ProcessInstance process = ksession.startProcess(PROCESS_NAME);
        long processId = process.getId();
        assertTrue("process id not saved", processId > 0);
        
        // The process is in the Human Task waiting for its completion
        int processState = process.getState();
        assertEquals("Expected process state to be " + processStateName[ProcessInstance.STATE_ACTIVE] + " not "
                + processStateName[processState], ProcessInstance.STATE_ACTIVE, processState);

        sleepAndVerifyTimerRuns();
        
        completeWork(ksession, humanTaskMockHandler);

        // The process completes
        process = ksession.getProcessInstance(process.getId());
        assertNull("Expected process to have been completed and removed", process);
        assertTrue(dataTaskMockHandler.getInputParameter("nodeInstance") instanceof HumanTaskNodeInstance);
        assertEquals(dataTaskMockHandler.getInputParameter("event"), 1L);
        assertEquals(dataTaskMockHandler.getInputParameter("signal"), "Timer-_2-500ms###1s-4");
        assertTrue(dataTaskMockHandler.getInputParameter("workItem") instanceof WorkItem);
    }

    @Test
    public void testBoundaryEventMessageAndCompleteHumanTask() throws InterruptedException {
        
        createRuntimeManager(PROCESS_MESSAGE_FILE_NAME);

        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession ksession = runtimeEngine.getKieSession();
        long ksessionId = ksession.getIdentifier();
        assertTrue("session id not saved.", ksessionId > 0);
        
        HumanTaskMockHandler humanTaskMockHandler = new HumanTaskMockHandler();
        DataTaskMockHandler dataTaskMockHandler = new DataTaskMockHandler();
        // Register Human Task Handler
        ksession.getWorkItemManager().registerWorkItemHandler(WORK_ITEM_HANDLER_TASK, humanTaskMockHandler);
        ksession.getWorkItemManager().registerWorkItemHandler(WORK_ITEM_HANDLER_DATA, dataTaskMockHandler);
        // Start the process 
        ProcessInstance process = ksession.startProcess(PROCESS_MESSAGE_NAME);
        long processId = process.getId();
        assertTrue("process id not saved", processId > 0);
        
        // The process is in the Human Task waiting for its completion
        int processState = process.getState();
        assertEquals("Expected process state to be " + processStateName[ProcessInstance.STATE_ACTIVE] + " not "
                + processStateName[processState], ProcessInstance.STATE_ACTIVE, processState);

        // we signal a boundary event
        ksession.signalEvent("Message-MyMessage",  new Person());
        
        completeWork(ksession, humanTaskMockHandler);

        // The process completes
        process = ksession.getProcessInstance(process.getId());
        assertNull("Expected process to have been completed and removed", process);
        assertTrue(dataTaskMockHandler.getInputParameter("nodeInstance") instanceof HumanTaskNodeInstance);
        assertTrue(dataTaskMockHandler.getInputParameter("event") instanceof Person);
        assertEquals(dataTaskMockHandler.getInputParameter("signal"), "Message-MyMessage");
    }
    
    @Test
    public void testBoundaryEventSignalAndCompleteHumanTask() throws InterruptedException {
        
        createRuntimeManager(PROCESS_SIGNAL_FILE_NAME);

        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession ksession = runtimeEngine.getKieSession();
        long ksessionId = ksession.getIdentifier();
        assertTrue("session id not saved.", ksessionId > 0);
        
        HumanTaskMockHandler humanTaskMockHandler = new HumanTaskMockHandler();
        DataTaskMockHandler dataTaskMockHandler = new DataTaskMockHandler();
        // Register Human Task Handler
        ksession.getWorkItemManager().registerWorkItemHandler(WORK_ITEM_HANDLER_TASK, humanTaskMockHandler);
        ksession.getWorkItemManager().registerWorkItemHandler(WORK_ITEM_HANDLER_DATA, dataTaskMockHandler);
        // Start the process 
        ProcessInstance process = ksession.startProcess(PROCESS_SIGNAL_NAME);
        long processId = process.getId();
        assertTrue("process id not saved", processId > 0);
        
        // The process is in the Human Task waiting for its completion
        int processState = process.getState();
        assertEquals("Expected process state to be " + processStateName[ProcessInstance.STATE_ACTIVE] + " not "
                + processStateName[processState], ProcessInstance.STATE_ACTIVE, processState);

        // we signal a boundary event
        ksession.signalEvent("MySignal",  new Person());
        
        completeWork(ksession, humanTaskMockHandler);

        // The process completes
        process = ksession.getProcessInstance(process.getId());
        assertNull("Expected process to have been completed and removed", process);
        assertTrue(dataTaskMockHandler.getInputParameter("nodeInstance") instanceof HumanTaskNodeInstance);
        assertTrue(dataTaskMockHandler.getInputParameter("event") instanceof Person);
        assertEquals(dataTaskMockHandler.getInputParameter("signal"), "MySignal");
    }
    
    @Test
    public void testBoundaryErrorSignalAndCompleteHumanTask() throws InterruptedException {
        
        createRuntimeManager(PROCESS_ERROR_FILE_NAME);

        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession ksession = runtimeEngine.getKieSession();
        long ksessionId = ksession.getIdentifier();
        assertTrue("session id not saved.", ksessionId > 0);
        
        HumanTaskMockHandler humanTaskMockHandler = new HumanTaskMockHandler(true);
        DataTaskMockHandler dataTaskMockHandler = new DataTaskMockHandler();
        // Register Human Task Handler
        ksession.getWorkItemManager().registerWorkItemHandler(WORK_ITEM_HANDLER_TASK, humanTaskMockHandler);
        ksession.getWorkItemManager().registerWorkItemHandler(WORK_ITEM_HANDLER_DATA, dataTaskMockHandler);
        // Start the process 
        ProcessInstance process = ksession.startProcess(PROCESS_ERROR_NAME);
        long processId = process.getId();
        assertTrue("process id not saved", processId > 0);

        ksession.abortProcessInstance(processId);
        // The process completes
        process = ksession.getProcessInstance(process.getId());
        assertNull("Expected process to have been completed and removed", process);
        assertTrue(dataTaskMockHandler.getInputParameter("nodeInstance") instanceof HumanTaskNodeInstance);
        assertTrue(dataTaskMockHandler.getInputParameter("event") instanceof java.lang.RuntimeException);
        assertEquals(dataTaskMockHandler.getInputParameter("signal"), "Error-_2-401");
    }
    
    
    @Test
    public void testBoundaryEscalationSignalAndCompleteHumanTask() throws InterruptedException {
        
        createRuntimeManager(PROCESS_ESCALATION_FILE_NAME);

        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession ksession = runtimeEngine.getKieSession();
        long ksessionId = ksession.getIdentifier();
        assertTrue("session id not saved.", ksessionId > 0);
        
        HumanTaskMockHandler humanTaskMockHandler = new HumanTaskMockHandler(true);
        DataTaskMockHandler dataTaskMockHandler = new DataTaskMockHandler();
        // Register Human Task Handler
        ksession.getWorkItemManager().registerWorkItemHandler(WORK_ITEM_HANDLER_TASK, humanTaskMockHandler);
        ksession.getWorkItemManager().registerWorkItemHandler(WORK_ITEM_HANDLER_DATA, dataTaskMockHandler);
        // Start the process 
        ProcessInstance process = ksession.startProcess(PROCESS_ESCALATION_NAME);
        long processId = process.getId();
        assertTrue("process id not saved", processId > 0);
        
        // The process is in the Human Task waiting for its completion
        int processState = process.getState();
        assertEquals("Expected process state to be " + processStateName[ProcessInstance.STATE_COMPLETED] + " not "
                + processStateName[processState], ProcessInstance.STATE_COMPLETED, processState);

        
        // The process completes
        process = ksession.getProcessInstance(process.getId());
        assertNull("Expected process to have been completed and removed", process);
        assertTrue(dataTaskMockHandler.getInputParameter("nodeInstance") instanceof CompositeContextNodeInstance);
        assertEquals(dataTaskMockHandler.getInputParameter("signal"), "Escalation-_2-201");
        assertNull(dataTaskMockHandler.getInputParameter("event"));
    }
    
    @Test
    public void testBoundaryCompensationSignalAndCompleteHumanTask() throws InterruptedException {
        
        createRuntimeManager(PROCESS_COMPENSATION_FILE_NAME);

        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession ksession = runtimeEngine.getKieSession();
        long ksessionId = ksession.getIdentifier();
        assertTrue("session id not saved.", ksessionId > 0);
        
        HumanTaskMockHandler humanTaskMockHandler = new HumanTaskMockHandler();
        DataTaskMockHandler dataTaskMockHandler = new DataTaskMockHandler();
        // Register Human Task Handler
        ksession.getWorkItemManager().registerWorkItemHandler(WORK_ITEM_HANDLER_TASK, humanTaskMockHandler);
        ksession.getWorkItemManager().registerWorkItemHandler(WORK_ITEM_HANDLER_DATA, dataTaskMockHandler);
        // Start the process 
        ProcessInstance process = ksession.startProcess(PROCESS_COMPENSATION_NAME);
        long processId = process.getId();
        assertTrue("process id not saved", processId > 0);
        
        // The process is in the Human Task waiting for its completion
        int processState = process.getState();
        assertEquals("Expected process state to be " + processStateName[ProcessInstance.STATE_ACTIVE] + " not "
                + processStateName[processState], ProcessInstance.STATE_ACTIVE, processState);

        completeWork(ksession, humanTaskMockHandler);
        ksession.signalEvent("Compensation", "_2");
        
        
        completeWork(ksession, humanTaskMockHandler);
        // The process completes
        process = ksession.getProcessInstance(process.getId());
        assertNull("Expected process to have been completed and removed", process);
        assertNull(dataTaskMockHandler.getInputParameter("nodeInstance"));
        assertEquals(dataTaskMockHandler.getInputParameter("event"), "_2");
        assertEquals(dataTaskMockHandler.getInputParameter("signal"), "Compensation");
    }
    
    @Test
    public void testBoundaryConditionalSignalAndCompleteHumanTask() throws InterruptedException {
        
        createRuntimeManager(PROCESS_CONDITIONAL_FILE_NAME);

        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession ksession = runtimeEngine.getKieSession();
        long ksessionId = ksession.getIdentifier();
        assertTrue("session id not saved.", ksessionId > 0);
        
        HumanTaskMockHandler humanTaskMockHandler = new HumanTaskMockHandler();
        DataTaskMockHandler dataTaskMockHandler = new DataTaskMockHandler();
        // Register Human Task Handler
        ksession.getWorkItemManager().registerWorkItemHandler(WORK_ITEM_HANDLER_TASK, humanTaskMockHandler);
        ksession.getWorkItemManager().registerWorkItemHandler(WORK_ITEM_HANDLER_DATA, dataTaskMockHandler);
        // Start the process 
        ProcessInstance process = ksession.startProcess(PROCESS_CONDITIONAL_NAME);
        long processId = process.getId();
        assertTrue("process id not saved", processId > 0);
        
        // The process is in the Human Task waiting for its completion
        int processState = process.getState();
        assertEquals("Expected process state to be " + processStateName[ProcessInstance.STATE_ACTIVE] + " not "
                + processStateName[processState], ProcessInstance.STATE_ACTIVE, processState);

        ksession.signalEvent("RuleFlowStateEvent-" + PROCESS_CONDITIONAL_NAME + "-4-_2" , new org.jbpm.test.entity.Person(1L, "john"));
       
        completeWork(ksession, humanTaskMockHandler);

        // The process completes
        process = ksession.getProcessInstance(process.getId());
        assertNull("Expected process to have been completed and removed", process);
        assertTrue(dataTaskMockHandler.getInputParameter("nodeInstance") instanceof HumanTaskNodeInstance);
        assertTrue(dataTaskMockHandler.getInputParameter("event") instanceof Person);
        assertEquals(dataTaskMockHandler.getInputParameter("signal"), "RuleFlowStateEvent-BoundaryConditionalEventProcess-4-_2");
        assertTrue(dataTaskMockHandler.getInputParameter("workItem") instanceof WorkItem);
    }
    
    private void completeWork(KieSession ksession, HumanTaskMockHandler humanTaskMockHandler) {
        assertTrue("The work item task handler does not have a work item!", humanTaskMockHandler.workItem != null);
        long workItemId = humanTaskMockHandler.workItem.getId();
        assertTrue("work item id not saved", workItemId > 0);
        
        // The Human Task is completed
        Map<String, Object> results = new HashMap<String, Object>();
        try {
            ksession.getWorkItemManager().completeWorkItem(workItemId, results);
        } catch (Exception e) {
            logger.warn("Work item could not be completed!");
            e.printStackTrace();
            fail(e.getClass().getSimpleName() + " thrown when completing work item: " + e.getMessage());
        }
    }

    private void sleepAndVerifyTimerRuns()  { 
        try {
            int sleep = 3000;
            logger.debug("Sleeping {} seconds", sleep / 1000);
            Thread.sleep(sleep);
            logger.debug("Awake!");
        } catch(InterruptedException e) {
            logger.error("System was interrupted");
            fail("Junit was interrupted");
        }

    }



    private static class HumanTaskMockHandler implements WorkItemHandler {

        private WorkItemManager workItemManager;
        private WorkItem workItem;

        private boolean error;
        
        public HumanTaskMockHandler() {
            this(false);
        }
        
        public HumanTaskMockHandler(boolean error) {
            this.error = error;
        }
        
        public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
            this.workItem = workItem;
            this.workItemManager = manager;

            if(error) {
                throw new RuntimeException("this is an error");
            }
            logger.debug("Work completed!");
        }

        public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
            this.workItemManager.abortWorkItem(workItem.getId());
            logger.debug("Work aborted.");
        }
       
    }
    
    private static class DataTaskMockHandler implements WorkItemHandler {

        private WorkItemManager workItemManager;
        private WorkItem workItem;

        
        public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
            this.workItem = workItem;
            this.workItemManager = manager;

            logger.debug("Work completed!");
        }

        public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
            this.workItemManager.abortWorkItem(workItem.getId());
            logger.debug("Work aborted.");
        }
               
        public Object getInputParameter(String name) {
            return workItem.getParameter(name);
        }
    }
}
