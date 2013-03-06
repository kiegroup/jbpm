/*
Copyright 2013 JBoss Inc

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

package org.jbpm.bpmn2;

import java.util.ArrayList;
import java.util.List;

import org.drools.process.instance.impl.WorkItemImpl;
import org.jbpm.bpmn2.objects.Person;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.KieBase;
import org.kie.event.process.DefaultProcessEventListener;
import org.kie.event.process.ProcessStartedEvent;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.process.ProcessInstance;
import org.kie.runtime.process.WorkItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartTest extends JbpmTestCase {

    private static final String startFolder = "start/";

    private StatefulKnowledgeSession ksession;

    private Logger logger = LoggerFactory.getLogger(StartTest.class);

    public StartTest() {

    }

    @BeforeClass
    public static void setup() throws Exception {
        if (PERSISTENCE) {
            setUpDataSource();
        }
    }

    @After
    public void dispose() {
        if (ksession != null) {
            ksession.dispose();
        }
    }

    @Test
    public void testConditionalStart() throws Exception {
        KieBase kbase = createKnowledgeBase(startFolder
                + "ConditionalStart.bpmn2");
        ksession = createKnowledgeSession(kbase);
        Person person = new Person();
        person.setName("jack");
        ksession.insert(person);
        ksession.fireAllRules();
        person = new Person();
        person.setName("john");
        ksession.insert(person);
        ksession.fireAllRules();
    }

    /**
     * FIXME when it's run without persistence, list contains only 4 identifiers
     * @throws Exception
     */
    @Test
    @RequirePersistence
    public void testTimerStart() throws Exception {
        KieBase kbase = createKnowledgeBase(startFolder + "TimerStart.bpmn2");
        ksession = createKnowledgeSession(kbase);
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
    public void testTimerStartCycleISO() throws Exception {
        KieBase kbase = createKnowledgeBase(startFolder + "TimerStartISO.bpmn2");
        ksession = createKnowledgeSession(kbase);
        final List<Long> list = new ArrayList<Long>();
        ksession.addEventListener(new DefaultProcessEventListener() {
            public void afterProcessStarted(ProcessStartedEvent event) {
                list.add(event.getProcessInstance().getId());
            }
        });
        Thread.sleep(250);
        assertEquals(0, list.size());
        for (int i = 0; i < 6; i++) {
            ksession.fireAllRules();
            Thread.sleep(1000);
        }
        assertEquals(6, list.size());
    }

    @Test
    public void testTimerStartDuration() throws Exception {
        KieBase kbase = createKnowledgeBase(startFolder
                + "TimerStartDuration.bpmn2");
        ksession = createKnowledgeSession(kbase);
        final List<Long> list = new ArrayList<Long>();
        ksession.addEventListener(new DefaultProcessEventListener() {
            public void afterProcessStarted(ProcessStartedEvent event) {
                list.add(event.getProcessInstance().getId());
            }
        });
        Thread.sleep(250);
        assertEquals(0, list.size());
        ksession.fireAllRules();

        Thread.sleep(3000);

        assertEquals(1, list.size());
    }

    @Test
    public void testTimerStartCron() throws Exception {
        KieBase kbase = createKnowledgeBase(startFolder
                + "TimerStartCron.bpmn2");
        ksession = createKnowledgeSession(kbase);
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
        KieBase kbase = createKnowledgeBase(startFolder + "SignalStart.bpmn2");
        ksession = createKnowledgeSession(kbase);
        final List<Long> list = new ArrayList<Long>();
        ksession.addEventListener(new DefaultProcessEventListener() {
            public void afterProcessStarted(ProcessStartedEvent event) {
                list.add(event.getProcessInstance().getId());
            }
        });
        ksession.signalEvent("MySignal", "NewValue");
        assertEquals(1, list.size());
    }

    @Test
    public void testSignalStartDynamic() throws Exception {
        KieBase kbase = createKnowledgeBase();
        ksession = createKnowledgeSession(kbase);
        KieBase kbase2 = createKnowledgeBase(startFolder + "SignalStart.bpmn2");
        kbase.getKiePackages().addAll(kbase2.getKiePackages());
        final List<Long> list = new ArrayList<Long>();
        ksession.addEventListener(new DefaultProcessEventListener() {
            public void afterProcessStarted(ProcessStartedEvent event) {
                list.add(event.getProcessInstance().getId());
            }
        });
        ksession.signalEvent("MySignal", "NewValue");
        assertEquals(1, list.size());
    }

    @Test
    public void testSignalToStartProcess() throws Exception {
        KieBase kbase = createKnowledgeBase(startFolder + "SignalStart.bpmn2",
                "event/IntermediateThrowEventSignal.bpmn2");
        ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler handler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                handler);
        final List<String> startedProcesses = new ArrayList<String>();
        ksession.addEventListener(new DefaultProcessEventListener() {

            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                startedProcesses.add(event.getProcessInstance().getProcessId());
            }
        });

        ProcessInstance processInstance2 = ksession
                .startProcess("SignalIntermediateEvent");
        assertProcessInstanceCompleted(processInstance2);
        assertEquals(2, startedProcesses.size());
    }

    @Test
    public void testMessageStart() throws Exception {
        KieBase kbase = createKnowledgeBase(startFolder + "MessageStart.bpmn2");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        ksession.signalEvent("Message-HelloMessage", "NewValue");
    }

    @Test
    public void testMultipleStartEventsRegularStart() throws Exception {
        KieBase kbase = createKnowledgeBase(startFolder
                + "MultipleStartEventProcess.bpmn2");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        ProcessInstance processInstance = ksession
                .startProcess("MultipleStartEvents");
        assertProcessInstanceActive(processInstance);
        ksession = restoreSession(ksession, true);
        WorkItem workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        assertEquals("john", workItem.getParameter("ActorId"));
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testMultipleStartEventsStartOnTimer() throws Exception {
        KieBase kbase = createKnowledgeBase(startFolder
                + "MultipleStartEventProcess.bpmn2");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        final List<Long> list = new ArrayList<Long>();
        ksession.addEventListener(new DefaultProcessEventListener() {
            public void afterProcessStarted(ProcessStartedEvent event) {
                list.add(event.getProcessInstance().getId());
            }
        });
        Thread.sleep(500);
        assertEquals(0, list.size());
        for (int i = 0; i < 5; i++) {
            ksession.fireAllRules();
            Thread.sleep(500);
        }
        assertEquals(5, list.size());
    }

    @Test
    public void testMultipleEventBasedStartEventsSignalStart() throws Exception {
        KieBase kbase = createKnowledgeBase(startFolder
                + "MultipleEventBasedStartEventProcess.bpmn2");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);

        final List<Long> list = new ArrayList<Long>();
        ksession.addEventListener(new DefaultProcessEventListener() {
            public void afterProcessStarted(ProcessStartedEvent event) {
                list.add(event.getProcessInstance().getId());
            }
        });

        ksession.signalEvent("startSignal", null);

        assertEquals(1, list.size());
        WorkItem workItem = workItemHandler.getWorkItem();
        long processInstanceId = ((WorkItemImpl) workItem)
                .getProcessInstanceId();

        ProcessInstance processInstance = ksession
                .getProcessInstance(processInstanceId);
        ksession = restoreSession(ksession, true);

        assertNotNull(workItem);
        assertEquals("john", workItem.getParameter("ActorId"));
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testMultipleEventBasedStartEventsStartOnTimer()
            throws Exception {
        KieBase kbase = createKnowledgeBase(startFolder
                + "MultipleEventBasedStartEventProcess.bpmn2");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        final List<Long> list = new ArrayList<Long>();
        ksession.addEventListener(new DefaultProcessEventListener() {
            public void afterProcessStarted(ProcessStartedEvent event) {
                list.add(event.getProcessInstance().getId());
            }
        });
        Thread.sleep(500);
        assertEquals(0, list.size());
        for (int i = 0; i < 5; i++) {
            ksession.fireAllRules();
            Thread.sleep(500);
        }
        assertEquals(5, list.size());
    }

}
