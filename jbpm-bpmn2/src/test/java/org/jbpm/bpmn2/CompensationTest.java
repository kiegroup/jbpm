/**
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.bpmn2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.bpmn2.objects.TestWorkItemHandler;
import org.jbpm.bpmn2.test.RequiresQueueBased;
import org.jbpm.process.core.context.exception.CompensationScope;
import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.jbpm.process.test.TestProcessEventListener;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.kie.api.KieBase;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;

@RunWith(Parameterized.class)
public class CompensationTest extends JbpmBpmn2TestCase {

    @Parameters(name="{3}")
    public static Collection<Object[]> parameters() {
        return getTestOptions(TestOption.EXCEPT_FOR_LOCKING);
    };

    private KieSession ksession;

    public CompensationTest(boolean persistence, boolean locking, boolean queueBasedExecution, String name) {
        super(persistence, locking, queueBasedExecution, name);
    }

    @BeforeClass
    public static void setup() throws Exception {
        setUpDataSource();
    }

    @Before
    public void prepare() {
        clearHistory();
    }

    @After
    public void dispose() {
        if (ksession != null) {
            ksession.dispose();
            ksession = null;
        }
    }

    /**
     * TESTS
     */

    @Test
    public void compensationViaIntermediateThrowEventProcess() throws Exception {
        KieSession ksession = createKnowledgeSession("compensation/BPMN2-Compensation-IntermediateThrowEvent.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "0");
        ProcessInstance processInstance = ksession.startProcess("CompensateIntermediateThrowEvent", params);

        ksession.getWorkItemManager().completeWorkItem(workItemHandler.getWorkItem().getId(), null);

        // compensation activity (assoc. with script task) signaled *after* script task
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
        assertProcessVarValue(processInstance, "x", "1" );
    }
    
    @Test
    public void compensationTwiceViaSignal() throws Exception {
        KieSession ksession = createKnowledgeSession("compensation/BPMN2-Compensation-IntermediateThrowEvent.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "0");
        String processId = "CompensateIntermediateThrowEvent";
        ProcessInstance processInstance = ksession.startProcess(processId, params);
        
        // twice
        ksession.signalEvent("Compensation", CompensationScope.IMPLICIT_COMPENSATION_PREFIX + processId, processInstance.getId());
        ksession.getWorkItemManager().completeWorkItem(workItemHandler.getWorkItem().getId(), null);

        // compensation activity (assoc. with script task) signaled *after* script task
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
        assertProcessVarValue(processInstance, "x", "2");
    }
    
    @Test
    public void compensationViaEventSubProcess() throws Exception {
        KieSession ksession = createKnowledgeSession("compensation/BPMN2-Compensation-EventSubProcess.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
 
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "0");
        ProcessInstance processInstance = ksession.startProcess("CompensationEventSubProcess", params);

        assertProcessInstanceActive(processInstance.getId(), ksession);
        ksession.getWorkItemManager().completeWorkItem(workItemHandler.getWorkItem().getId(), null);
        
        assertProcessVarValue(processInstance, "x", "1");
    }
    
    @Test
    public void compensationOnlyAfterAssociatedActivityHasCompleted() throws Exception {
        KieSession ksession = createKnowledgeSession("compensation/BPMN2-Compensation-UserTaskBeforeAssociatedActivity.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "0");
        ProcessInstance processInstance = ksession.startProcess("CompensateIntermediateThrowEvent", params);
        
        // should NOT cause compensation since compensated activity has not yet completed (or started)! 
        ksession.signalEvent("Compensation", "_3", processInstance.getId());
        
        // user task -> script task (associated with compensation) --> intermeidate throw compensation event
        ksession.getWorkItemManager().completeWorkItem(workItemHandler.getWorkItem().getId(), null);
        
        // compensation activity (assoc. with script task) signaled *after* to-compensate script task
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
        assertProcessVarValue(processInstance, "x", "1");
    }
    
    @Test
    public void orderedCompensation() throws Exception { 
        KieSession ksession = createKnowledgeSession("compensation/BPMN2-Compensation-ParallelOrderedCompensation-IntermediateThrowEvent.bpmn2");
        TestProcessEventListener procEventListener = new TestProcessEventListener();
        procEventListener.useNodeName();
        ksession.addEventListener(procEventListener);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "");
        ProcessInstance processInstance = ksession.startProcess("CompensateParallelOrdered", params);
        List<WorkItem> workItems = workItemHandler.getWorkItems();
        List<Long> workItemIds = new ArrayList<Long>();
        for( WorkItem workItem : workItems ) {
           if( "taskThr".equals(workItem.getParameter("NodeName")) )  {
               workItemIds.add(workItem.getId());
           }
        }
        for( WorkItem workItem : workItems ) {
           if( "taskTwo".equals(workItem.getParameter("NodeName")) )  {
               workItemIds.add(workItem.getId());
           }
        }
        for( WorkItem workItem : workItems ) {
           if( "taskOne".equals(workItem.getParameter("NodeName")) )  {
               workItemIds.add(workItem.getId());
           }
        }
        for( Long id : workItemIds ) { 
            ksession.getWorkItemManager().completeWorkItem(id, null);
        }

        StringBuffer compOrder = new StringBuffer();
        for( String event : procEventListener.getEventHistory()) {
            if( event.startsWith("bnt-") ) {
                if( event.endsWith("one") || event.endsWith("two") || event.endsWith("thr") || event.endsWith("fou")) {
                   compOrder.append(event.substring(event.indexOf('-')+1)).append(":");
                }
            }
        }

        // user task -> script task (associated with compensation) --> intermeidate throw compensation event
        String xVal = getProcessVarValue(processInstance, "x");
        assertTrue( "Compensation did not happen!", xVal != null && ! xVal.isEmpty() );
        // Compensation happens in the *REVERSE* order of completion
        // Ex: if the order is 3, 17, 282, then compensation should happen in the order of 282, 17, 3
        assertEquals("Compensation did not fire in the same order as the associated activities completed.", compOrder.toString(), xVal );
    }
    
    @Test
    public void compensationInSubSubProcesses() throws Exception {
        KieSession ksession = createKnowledgeSession("compensation/BPMN2-Compensation-InSubSubProcess.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "0");
        ProcessInstance processInstance = ksession.startProcess("CompensateSubSubSub", params);

        ksession.signalEvent("Compensation", "_C-2", processInstance.getId());
        
        ksession.getWorkItemManager().completeWorkItem(workItemHandler.getWorkItem().getId(), null);
        ksession.getWorkItemManager().completeWorkItem(workItemHandler.getWorkItem().getId(), null);
        ksession.getWorkItemManager().completeWorkItem(workItemHandler.getWorkItem().getId(), null);

        // compensation activity (assoc. with script task) signaled *after* script task
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
        assertProcessVarValue(processInstance, "x", "2");
    }
    
    @Test
    public void specificCompensationOfASubProcess() throws Exception {
        KieSession ksession = createKnowledgeSession("compensation/BPMN2-Compensation-ThrowSpecificForSubProcess.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", 1);
        ProcessInstance processInstance = ksession.startProcess("CompensationSpecificSubProcess", params);
        
        // compensation activity (assoc. with script task) signaled *after* to-compensate script task
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
        if( ! isPersistence() ) { 
            assertProcessVarValue(processInstance, "x", null);
        } else { 
            assertProcessVarValue(processInstance, "x", "");
        }
    }
    
    @Test
    @RequiresQueueBased
    public void compensationViaCancellation() throws Exception {
        KieSession ksession = createKnowledgeSession("compensation/BPMN2-Compensation-IntermediateThrowEvent.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "0");
        ProcessInstance processInstance = ksession.startProcess("CompensateIntermediateThrowEvent", params);

        ksession.signalEvent("Cancel", null, processInstance.getId());
        ksession.getWorkItemManager().completeWorkItem(workItemHandler.getWorkItem().getId(), null);

        // compensation activity (assoc. with script task) signaled *after* script task
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
        assertProcessVarValue(processInstance, "x", "1");
    }
    
    @Test
    public void compensationInvokingSubProcess() throws Exception {
    	KieSession ksession = createKnowledgeSession("compensation/BPMN2-UserTaskCompensation.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new SystemOutWorkItemHandler());
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("compensation", "True");
        ProcessInstance processInstance = ksession.startProcess("UserTaskCompensation", params);
        
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
        assertProcessVarValue(processInstance, "compensation", "compensation");
    }

    // needs exception-scope based persistence that queue-based will provide
    @Test
    @Ignore
    @RequiresQueueBased
    public void compensationViaUserTask() throws Exception {
        KieSession ksession = createKnowledgeSession("compensation/BPMN2-Compensation-UserTask.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "0");
        ProcessInstance processInstance = ksession.startProcess("CompensateIntermediateThrowEvent", params);

        // should NOT cause compensation since compensated activity has not yet completed (or started)!
        ksession.signalEvent("Compensation", "_3", processInstance.getId());

        assertProcessInstanceActive(processInstance.getId(), ksession);

        // user task -> script task (associated with compensation) --> intermediate throw compensation event
        ksession.getWorkItemManager().completeWorkItem(workItemHandler.getWorkItem().getId(), null);

        assertProcessInstanceActive(processInstance.getId(), ksession);

        // compensation task
        ksession.getWorkItemManager().completeWorkItem(workItemHandler.getWorkItem().getId(), null);

        assertProcessInstanceCompleted(processInstance.getId(), ksession);
    }

    // needs exception-scope based persistence that queue-based will provide
    @Test
    @Ignore
    @RequiresQueueBased
    public void testEventSubprocessTaskError() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-EventSubprocessErrorWithTask.bpmn2");
        final List<Long> executednodes = new ArrayList<Long>();
        ProcessEventListener listener = new DefaultProcessEventListener() {

            @Override
            public void afterNodeLeft(ProcessNodeLeftEvent event) {
                if (event.getNodeInstance().getNodeName()
                        .equals("Script Task 1")) {
                    executednodes.add(event.getNodeInstance().getId());
                }
            }

        };
        ksession = createKnowledgeSession(kbase);
        ksession.addEventListener(listener);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        ProcessInstance processInstance = ksession.startProcess("eventSubprocessErrorWithTask");
        assertProcessInstanceActive(processInstance);

        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        ksession.addEventListener(listener);

        WorkItem workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);

        workItem = workItemHandler.getWorkItem();
        assertNotNull("Null workitem", workItem);
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);

        assertProcessInstanceFinished(processInstance, ksession);

    }

}
