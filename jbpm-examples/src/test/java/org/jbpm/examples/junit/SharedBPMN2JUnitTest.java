/**
 * Copyright 2010 JBoss Inc
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

package org.jbpm.examples.junit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.bpmn2.handler.ReceiveTaskHandler;
import org.jbpm.bpmn2.handler.SendTaskHandler;
import org.jbpm.process.instance.impl.demo.DoNothingWorkItemHandler;
import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.jbpm.test.JbpmTestCase;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.KieBase;
import org.kie.event.process.DefaultProcessEventListener;
import org.kie.event.process.ProcessStartedEvent;
import org.kie.event.process.ProcessVariableChangedEvent;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.process.ProcessInstance;
import org.kie.runtime.process.WorkItem;
import org.kie.runtime.process.WorkItemHandler;
import org.kie.runtime.process.WorkItemManager;
import org.kie.runtime.process.WorkflowProcessInstance;

public abstract class SharedBPMN2JUnitTest extends JbpmTestCase {
    
    private static String RESOURCES = "junit/";
    
    public SharedBPMN2JUnitTest() {
        
    }
    
    public SharedBPMN2JUnitTest(String resources) {
        RESOURCES = resources;
    }

    @BeforeClass
    public static void setup() throws Exception {
        if (PERSISTENCE) {
            setUpDataSource();
        }
    }
    
    @Override
    protected StatefulKnowledgeSession createKnowledgeSession(String... process)
            throws Exception {
        String[] paths = null;
        if (process != null) {
            paths = new String[process.length];
            for (int i=0; i<process.length; ++i) {
                paths[i] = RESOURCES + process[i];
            }
        }
        return super.createKnowledgeSession(paths);
    }

    @Test
    public void testMinimalProcess() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("MinimalProcess.bpmn2");
        ProcessInstance processInstance = ksession.startProcess("Minimal");
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testCompositeProcessWithDIGraphical() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("CompositeProcessWithDIGraphical.bpmn2");
        ProcessInstance processInstance = ksession.startProcess("Composite");
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testScriptTask() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("ScriptTask.bpmn2");
        ProcessInstance processInstance = ksession.startProcess("ScriptTask");
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testImport() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("Import.bpmn2");
        ProcessInstance processInstance = ksession.startProcess("Import");
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testRuleTask() throws Exception {
        System.out.println("Loading process BPMN2-RuleTask.bpmn2"); 
        StatefulKnowledgeSession ksession = createKnowledgeSession("RuleTask.bpmn2", "RuleTask.drl");
        List<String> list = new ArrayList<String>();
        ksession.setGlobal("list", list);
        ProcessInstance processInstance = ksession.startProcess("RuleTask");
        assertProcessInstanceActive(processInstance);
        ksession.fireAllRules();
        assertTrue(list.size() == 1);
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testDataObject() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("DataObject.bpmn2");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employee", "UserId-12345");
        ProcessInstance processInstance = ksession.startProcess("Evaluation", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testEvaluationProcess() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("EvaluationProcess.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("RegisterRequest", new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employee", "UserId-12345");
        ProcessInstance processInstance = ksession.startProcess("Evaluation", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testEvaluationProcess2() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("EvaluationProcess2.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employee", "UserId-12345");
        ProcessInstance processInstance = ksession.startProcess("com.sample.evaluation", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testEvaluationProcess3() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("EvaluationProcess3.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("RegisterRequest", new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employee", "john2");
        ProcessInstance processInstance = ksession.startProcess("Evaluation", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testUserTask() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("UserTask.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        ProcessInstance processInstance = ksession.startProcess("UserTask");
        assertProcessInstanceActive(processInstance);
        ksession = restoreSession(ksession, true);
        WorkItem workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        assertEquals("john", workItem.getParameter("ActorId"));
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testLane() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("Lane.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        ProcessInstance processInstance = ksession.startProcess("UserTask");
        assertProcessInstanceActive(processInstance);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        WorkItem workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        assertEquals("john", workItem.getParameter("ActorId"));
        Map<String, Object> results = new HashMap<String, Object>();
        results.put("ActorId", "mary");
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), results);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        assertEquals("mary", workItem.getParameter("ActorId"));
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testExclusiveSplit() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("ExclusiveSplit.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("Email", new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "First");
        params.put("y", "Second");
        ProcessInstance processInstance = ksession.startProcess("com.sample.test", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testExclusiveSplitPriority() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("ExclusiveSplitPriority.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("Email", new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "First");
        params.put("y", "Second");
        ProcessInstance processInstance = ksession.startProcess("com.sample.test", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testExclusiveSplitDefault() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("ExclusiveSplitDefault.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("Email", new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "NotFirst");
        params.put("y", "Second");
        ProcessInstance processInstance = ksession.startProcess("com.sample.test", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testInclusiveSplit() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("InclusiveSplit.bpmn2");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", 15);
        ProcessInstance processInstance = ksession.startProcess("com.sample.test", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testInclusiveSplitDefault() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("InclusiveSplitDefault.bpmn2");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", -5);
        ProcessInstance processInstance = ksession.startProcess("com.sample.test", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testEventBasedSplit() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("EventBasedSplit.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("Email1", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("Email2", new SystemOutWorkItemHandler());
        // Yes
        ProcessInstance processInstance = ksession.startProcess("com.sample.test");
        assertProcessInstanceActive(processInstance);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Email1", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("Email2", new SystemOutWorkItemHandler());
        ksession.signalEvent("Yes", "YesValue", processInstance.getId());
        assertProcessInstanceFinished(processInstance, ksession);
        // No
        processInstance = ksession.startProcess("com.sample.test");
        assertProcessInstanceActive(processInstance);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Email1", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("Email2", new SystemOutWorkItemHandler());
        ksession.signalEvent("No", "NoValue", processInstance.getId());
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testEventBasedSplitBefore() throws Exception {
        // signaling before the split is reached should have no effect
        StatefulKnowledgeSession ksession = createKnowledgeSession("EventBasedSplit.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("Email1", new DoNothingWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("Email2", new DoNothingWorkItemHandler());
        // Yes
        ProcessInstance processInstance = ksession.startProcess("com.sample.test");
        assertProcessInstanceActive(processInstance);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Email1", new DoNothingWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("Email2", new DoNothingWorkItemHandler());
        ksession.signalEvent("Yes", "YesValue", processInstance.getId());
        assertProcessInstanceActive(processInstance);
        // No
        processInstance = ksession.startProcess("com.sample.test");
        assertProcessInstanceActive(processInstance);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Email1", new DoNothingWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("Email2", new DoNothingWorkItemHandler());
        ksession.signalEvent("No", "NoValue", processInstance.getId());
        assertProcessInstanceActive(processInstance);
    }

    @Test
    public void testEventBasedSplitAfter() throws Exception {
        // signaling the other alternative after one has been selected should have no effect
        StatefulKnowledgeSession ksession = createKnowledgeSession("EventBasedSplit.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("Email1", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("Email2", new DoNothingWorkItemHandler());
        // Yes
        ProcessInstance processInstance = ksession.startProcess("com.sample.test");
        assertProcessInstanceActive(processInstance);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Email1", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("Email2", new DoNothingWorkItemHandler());
        ksession.signalEvent("Yes", "YesValue", processInstance.getId());
        assertProcessInstanceActive(processInstance);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Email1", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("Email2", new DoNothingWorkItemHandler());
        // No
        ksession.signalEvent("No", "NoValue", processInstance.getId());
    }

    @Test
    public void testEventBasedSplit2() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("EventBasedSplit2.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("Email1", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("Email2", new SystemOutWorkItemHandler());
        // Yes
        ProcessInstance processInstance = ksession.startProcess("com.sample.test");
        assertProcessInstanceActive(processInstance);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Email1", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("Email2", new SystemOutWorkItemHandler());
        ksession.signalEvent("Yes", "YesValue", processInstance.getId());
        assertProcessInstanceFinished(processInstance, ksession);
        Thread.sleep(800);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Email1", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("Email2", new SystemOutWorkItemHandler());
        ksession.fireAllRules();
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Email1", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("Email2", new SystemOutWorkItemHandler());
        // Timer
        processInstance = ksession.startProcess("com.sample.test");
        assertProcessInstanceActive(processInstance);
        Thread.sleep(800);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Email1", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("Email2", new SystemOutWorkItemHandler());
        ksession.fireAllRules();
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testEventBasedSplit3() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("EventBasedSplit3.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("Email1", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("Email2", new SystemOutWorkItemHandler());
        Person jack = new Person();
        jack.setName("Jack");
        // Yes
        ProcessInstance processInstance = ksession.startProcess("com.sample.test");
        assertProcessInstanceActive(processInstance);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Email1", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("Email2", new SystemOutWorkItemHandler());
        ksession.signalEvent("Yes", "YesValue", processInstance.getId());
        assertProcessInstanceFinished(processInstance, ksession);
        // Condition
        processInstance = ksession.startProcess("com.sample.test");
        assertProcessInstanceActive(processInstance);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Email1", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("Email2", new SystemOutWorkItemHandler());
        ksession.insert(jack);
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testEventBasedSplit4() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("EventBasedSplit4.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("Email1", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("Email2", new SystemOutWorkItemHandler());
        // Yes
        ProcessInstance processInstance = ksession.startProcess("com.sample.test");
        assertProcessInstanceActive(processInstance);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Email1", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("Email2", new SystemOutWorkItemHandler());
        ksession.signalEvent("Message-YesMessage", "YesValue", processInstance.getId());
        assertProcessInstanceFinished(processInstance, ksession);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Email1", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("Email2", new SystemOutWorkItemHandler());
        // No
        processInstance = ksession.startProcess("com.sample.test");
        ksession.signalEvent("Message-NoMessage", "NoValue", processInstance.getId());
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testEventBasedSplit5() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("EventBasedSplit5.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("Email1", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("Email2", new SystemOutWorkItemHandler());
        ReceiveTaskHandler receiveTaskHandler = new ReceiveTaskHandler(ksession);
        ksession.getWorkItemManager().registerWorkItemHandler("Receive Task", receiveTaskHandler);
        // Yes
        ProcessInstance processInstance = ksession.startProcess("com.sample.test");
        assertProcessInstanceActive(processInstance);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Email1", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("Email2", new SystemOutWorkItemHandler());
        receiveTaskHandler.setKnowledgeRuntime(ksession);
        ksession.getWorkItemManager().registerWorkItemHandler("Receive Task", receiveTaskHandler);
        receiveTaskHandler.messageReceived("YesMessage", "YesValue");
        assertProcessInstanceFinished(processInstance, ksession);
        receiveTaskHandler.messageReceived("NoMessage", "NoValue");
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Email1", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("Email2", new SystemOutWorkItemHandler());
        receiveTaskHandler.setKnowledgeRuntime(ksession);
        ksession.getWorkItemManager().registerWorkItemHandler("Receive Task", receiveTaskHandler);
        // No
        processInstance = ksession.startProcess("com.sample.test");
        receiveTaskHandler.messageReceived("NoMessage", "NoValue");
        assertProcessInstanceFinished(processInstance, ksession);
        receiveTaskHandler.messageReceived("YesMessage", "YesValue");
    }

    @Test
    public void testCallActivity() throws Exception {
        System.out.println("Loading process BPMN2-CallActivity.bpmn2"); 
        StatefulKnowledgeSession ksession = createKnowledgeSession("CallActivity.bpmn2", "CallActivitySubProcess.bpmn2");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "oldValue");
        ProcessInstance processInstance = ksession.startProcess("ParentProcess", params);
        assertProcessInstanceCompleted(processInstance);
        assertEquals("new value", ((WorkflowProcessInstance) processInstance).getVariable("y"));
    }

    @Test
    public void testSubProcess() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("SubProcess.bpmn2");
        ksession.addEventListener(new DefaultProcessEventListener() {
            public void afterProcessStarted(ProcessStartedEvent event) {
                System.out.println(event);
            }
            public void beforeVariableChanged(ProcessVariableChangedEvent event) {
                System.out.println(event);
            }
            public void afterVariableChanged(ProcessVariableChangedEvent event) {
                System.out.println(event);
            }
        });
        ProcessInstance processInstance = ksession.startProcess("SubProcess");
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testMultiInstanceLoopCharacteristicsProcess() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("MultiInstanceLoopCharacteristicsProcess.bpmn2");
        Map<String, Object> params = new HashMap<String, Object>();
        List<String> myList = new ArrayList<String>();
        myList.add("First Item");
        myList.add("Second Item");
        params.put("list", myList);
        ProcessInstance processInstance = ksession.startProcess("MultiInstanceLoopCharacteristicsProcess", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testEscalationBoundaryEvent() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("EscalationBoundaryEvent.bpmn2");
        ProcessInstance processInstance = ksession.startProcess("EscalationBoundaryEvent");
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testEscalationBoundaryEventInterrupting() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("EscalationBoundaryEventInterrupting.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("MyTask", new DoNothingWorkItemHandler());
        ProcessInstance processInstance = ksession.startProcess("EscalationBoundaryEvent");
        assertProcessInstanceCompleted(processInstance);
        // TODO: check for cancellation of task
    }

    @Test
    public void testErrorBoundaryEvent() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("ErrorBoundaryEventInterrupting.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("MyTask", new DoNothingWorkItemHandler());
        ProcessInstance processInstance = ksession.startProcess("ErrorBoundaryEvent");
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testTimerBoundaryEventDuration() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("TimerBoundaryEventDuration.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("MyTask", new DoNothingWorkItemHandler());
        ProcessInstance processInstance = ksession.startProcess("TimerBoundaryEvent");
        assertProcessInstanceActive(processInstance);
        Thread.sleep(1000);
        ksession = restoreSession(ksession, true);
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testTimerBoundaryEventCycle1() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("TimerBoundaryEventCycle1.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("MyTask", new DoNothingWorkItemHandler());
        ProcessInstance processInstance = ksession.startProcess("TimerBoundaryEvent");
        assertProcessInstanceActive(processInstance);
        Thread.sleep(1000);
        ksession = restoreSession(ksession, true);
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testTimerBoundaryEventCycle2() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("TimerBoundaryEventCycle2.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("MyTask", new DoNothingWorkItemHandler());
        ProcessInstance processInstance = ksession.startProcess("TimerBoundaryEvent");
        assertProcessInstanceActive(processInstance);
        Thread.sleep(1000);
        assertProcessInstanceActive(processInstance);
        Thread.sleep(1000);
        assertProcessInstanceActive(processInstance);
        ksession.abortProcessInstance(processInstance.getId());
        Thread.sleep(1000);
    }

    @Test
    public void testTimerBoundaryEventInterrupting() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("TimerBoundaryEventInterrupting.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("MyTask", new DoNothingWorkItemHandler());
        ProcessInstance processInstance = ksession.startProcess("TimerBoundaryEvent");
        assertProcessInstanceActive(processInstance);
        Thread.sleep(1000);
        ksession = restoreSession(ksession, true);
        System.out.println("Firing timer");
        ksession.fireAllRules();
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testAdHocSubProcess() throws Exception {
        System.out.println("Loading process BPMN2-AdHocSubProcess.bpmn2");
        StatefulKnowledgeSession ksession = createKnowledgeSession("AdHocSubProcess.bpmn2", "AdHocSubProcess.drl");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        ProcessInstance processInstance = ksession.startProcess("AdHocSubProcess");
        assertProcessInstanceActive(processInstance);
        WorkItem workItem = workItemHandler.getWorkItem();
        assertNull(workItem);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        ksession.fireAllRules();
        System.out.println("Signaling Hello2");
        ksession.signalEvent("Hello2", null, processInstance.getId());
        workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
    }

    @Test
    public void testAdHocSubProcessAutoComplete() throws Exception {
        System.out.println("Loading process BPMN2-AdHocSubProcessAutoComplete.bpmn2");
        StatefulKnowledgeSession ksession = createKnowledgeSession("AdHocSubProcessAutoComplete.bpmn2", "AdHocSubProcess.drl");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        ProcessInstance processInstance = ksession.startProcess("AdHocSubProcess");
        assertProcessInstanceActive(processInstance);
        WorkItem workItem = workItemHandler.getWorkItem();
        assertNull(workItem);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        ksession.fireAllRules();
        workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testAdHocProcess() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("AdHocProcess.bpmn2");
        ProcessInstance processInstance = ksession.startProcess("AdHocProcess");
        assertProcessInstanceActive(processInstance);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new DoNothingWorkItemHandler());
        System.out.println("Triggering node");
        ksession.signalEvent("Task1", null, processInstance.getId());
        assertProcessInstanceActive(processInstance);
        ksession.signalEvent("User1", null, processInstance.getId());
        assertProcessInstanceActive(processInstance);
        ksession.insert(new Person());
        ksession.signalEvent("Task3", null, processInstance.getId());
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testIntermediateCatchEventSignal() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("IntermediateCatchEventSignal.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new DoNothingWorkItemHandler());
        ProcessInstance processInstance = ksession.startProcess("IntermediateCatchEvent");
        assertProcessInstanceActive(processInstance);
        ksession = restoreSession(ksession, true);
        // now signal process instance
        ksession.signalEvent("MyMessage", "SomeValue", processInstance.getId());
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testIntermediateCatchEventMessage() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("IntermediateCatchEventMessage.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new DoNothingWorkItemHandler());
        ProcessInstance processInstance = ksession.startProcess("IntermediateCatchEvent");
        assertProcessInstanceActive(processInstance);
        ksession = restoreSession(ksession, true);
        // now signal process instance
        ksession.signalEvent("Message-HelloMessage", "SomeValue", processInstance.getId());
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testIntermediateCatchEventTimerDuration() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("IntermediateCatchEventTimerDuration.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new DoNothingWorkItemHandler());
        ProcessInstance processInstance = ksession.startProcess("IntermediateCatchEvent");
        assertProcessInstanceActive(processInstance);
        // now wait for 1 second for timer to trigger
        Thread.sleep(1000);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new DoNothingWorkItemHandler());
        ksession.fireAllRules();
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testIntermediateCatchEventTimerCycle1() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("IntermediateCatchEventTimerCycle1.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new DoNothingWorkItemHandler());
        ProcessInstance processInstance = ksession.startProcess("IntermediateCatchEvent");
        assertProcessInstanceActive(processInstance);
        // now wait for 1 second for timer to trigger
        Thread.sleep(1000);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new DoNothingWorkItemHandler());
        ksession.fireAllRules();
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testIntermediateCatchEventTimerCycle2() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("IntermediateCatchEventTimerCycle2.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new DoNothingWorkItemHandler());
        ProcessInstance processInstance = ksession.startProcess("IntermediateCatchEvent");
        assertProcessInstanceActive(processInstance);
        // now wait for 1 second for timer to trigger
        Thread.sleep(1000);
        assertProcessInstanceActive(processInstance);
        Thread.sleep(1000);
        assertProcessInstanceActive(processInstance);
        ksession.abortProcessInstance(processInstance.getId());
        Thread.sleep(1000);
    }

    @Test
    public void testIntermediateCatchEventCondition() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("IntermediateCatchEventCondition.bpmn2");
        ProcessInstance processInstance = ksession.startProcess("IntermediateCatchEvent");
        assertProcessInstanceActive(processInstance);
        ksession = restoreSession(ksession, true);
        // now activate condition
        Person person = new Person();
        person.setName("Jack");
        ksession.insert(person);
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testErrorEndEventProcess() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("ErrorEndEvent.bpmn2");
        ProcessInstance processInstance = ksession.startProcess("ErrorEndEvent");
        assertProcessInstanceAborted(processInstance);
    }

    @Test
    public void testEscalationEndEventProcess() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("EscalationEndEvent.bpmn2");
        ProcessInstance processInstance = ksession.startProcess("EscalationEndEvent");
        assertProcessInstanceAborted(processInstance);
    }

    @Test
    public void testEscalationIntermediateThrowEventProcess() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("IntermediateThrowEventEscalation.bpmn2");
        ProcessInstance processInstance = ksession.startProcess("EscalationIntermediateThrowEvent");
        assertProcessInstanceAborted(processInstance);
    }

    @Test
    public void testCompensateIntermediateThrowEventProcess() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("IntermediateThrowEventCompensate.bpmn2");
        ProcessInstance processInstance = ksession.startProcess("CompensateIntermediateThrowEvent");
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testCompensateEndEventProcess() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("CompensateEndEvent.bpmn2");
        ProcessInstance processInstance = ksession.startProcess("CompensateEndEvent");
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testSendTask() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("SendTask.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("Send Task", new SendTaskHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("s", "john");
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance)
            ksession.startProcess("SendTask", params);
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testReceiveTask() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("ReceiveTask.bpmn2");
        ReceiveTaskHandler receiveTaskHandler = new ReceiveTaskHandler(ksession);
        ksession.getWorkItemManager().registerWorkItemHandler("Receive Task", receiveTaskHandler);
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance)
            ksession.startProcess("ReceiveTask");
        assertEquals(ProcessInstance.STATE_ACTIVE, processInstance.getState());
        ksession = restoreSession(ksession, true);
        receiveTaskHandler.messageReceived("HelloMessage", "Hello john!");
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testConditionalStart() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("ConditionalStart.bpmn2");
        Person person = new Person();
        person.setName("jack");
        ksession.insert(person);
        ksession.fireAllRules();
        person = new Person();
        person.setName("john");
        ksession.insert(person);
        ksession.fireAllRules();
    }

    @Test
    public void testTimerStart() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("TimerStart.bpmn2");
        final List<Long> list = new ArrayList<Long>();
        ksession.addEventListener(new DefaultProcessEventListener() {
            public void afterProcessStarted(ProcessStartedEvent event) {
                list.add(event.getProcessInstance().getId());
            }
        });
        Thread.sleep(250);
        assertEquals(0, list.size());
        for (int i = 0; i < 5; i++) {
            ksession.fireAllRules();
            Thread.sleep(500);
        }
        assertEquals(5, list.size());
    }

    @Test
    public void testTimerStartCron() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("TimerStartCron.bpmn2");
        final List<Long> list = new ArrayList<Long>();
        ksession.addEventListener(new DefaultProcessEventListener() {
            public void afterProcessStarted(ProcessStartedEvent event) {
                list.add(event.getProcessInstance().getId());
            }
        });
        Thread.sleep(500);
        for (int i = 0; i < 5; i++) {
            ksession.fireAllRules();
            Thread.sleep(1000);
        }
        assertEquals(6, list.size());
    }

    @Test
    public void testSignalStart() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("SignalStart.bpmn2");
        final List<Long> list = new ArrayList<Long>();
        ksession.addEventListener(new DefaultProcessEventListener() {
            public void afterProcessStarted(ProcessStartedEvent event) {
                list.add(event.getProcessInstance().getId());
            }
        });
        ksession.signalEvent("MyStartSignal", "NewValue");
        assertEquals(1, list.size());
    }

    @Test
    public void testSignalStartDynamic() throws Exception {
        KieBase kbase = createKnowledgeBase();
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        KieBase kbase2 = createKnowledgeBase(RESOURCES + "SignalStart.bpmn2");
        kbase.getKiePackages().addAll(kbase2.getKiePackages());
        final List<Long> list = new ArrayList<Long>();
        ksession.addEventListener(new DefaultProcessEventListener() {
            public void afterProcessStarted(ProcessStartedEvent event) {
                list.add(event.getProcessInstance().getId());
            }
        });
        ksession.signalEvent("MyStartSignal", "NewValue");
        assertEquals(1, list.size());
    }

    @Test
    public void testSignalEnd() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("SignalEndEvent.bpmn2");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "MyValue");
        ksession.startProcess("SignalEndEvent", params);
    }

    @Test
    public void testMessageStart() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("MessageStart.bpmn2");
        ksession.signalEvent("Message-HelloMessage", "NewValue");
    }

    @Test
    public void testMessageEnd() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("MessageEndEvent.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("Send Task", new SendTaskHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "MyValue");
        ProcessInstance processInstance = ksession.startProcess("MessageEndEvent", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testMessageIntermediateThrow() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("IntermediateThrowEventMessage.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("Send Task", new SendTaskHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "MyValue");
        ProcessInstance processInstance = ksession.startProcess("MessageIntermediateEvent", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testSignalIntermediateThrow() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("IntermediateThrowEventSignal.bpmn2");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "MyValue");
        ProcessInstance processInstance = ksession.startProcess("SignalIntermediateEvent", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testNoneIntermediateThrow() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("IntermediateThrowEventNone.bpmn2");
        ProcessInstance processInstance = ksession.startProcess("NoneIntermediateEvent", null);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testOnEntryExitScript() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("OnEntryExitScriptProcess.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("MyTask", new SystemOutWorkItemHandler());
        List<String> myList = new ArrayList<String>();
        ksession.setGlobal("list", myList);
        ProcessInstance processInstance = ksession.startProcess("OnEntryExitScriptProcess");
        assertProcessInstanceCompleted(processInstance);
        assertEquals(4, myList.size());
    }
    
    public class TestWorkItemHandler implements WorkItemHandler {
        
        private List<WorkItem> workItems = new ArrayList<WorkItem>();
        
        public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
            workItems.add(workItem);
        }
        
        public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        }
        
        public WorkItem getWorkItem() {
            if (workItems.size() == 0) {
                return null;
            }
            if (workItems.size() == 1) {
                WorkItem result = workItems.get(0);
                this.workItems.clear();
                return result;
            } else {
                throw new IllegalArgumentException("More than one work item active");
            }
        }
        
        public List<WorkItem> getWorkItems() {
            List<WorkItem> result = new ArrayList<WorkItem>(workItems);
            workItems.clear();
            return result;
        }
        
    }
	
}
