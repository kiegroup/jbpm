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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jbpm.bpmn2.handler.SendTaskHandler;
import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.KieBase;
import org.kie.cdi.KBase;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.process.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(CDITestRunner.class)
public class EndTest extends JbpmTestCase {

    @Inject
    @KBase("end")
    private KieBase endBase;

    private StatefulKnowledgeSession ksession;

    private Logger logger = LoggerFactory.getLogger(EndTest.class);

    public EndTest() {

    }

    @BeforeClass
    public static void setup() throws Exception {
        if (PERSISTENCE) {
            setUpDataSource();
        }
    }

    @Before
    public void init() throws Exception {
        ksession = createKnowledgeSession(endBase);
    }

    @After
    public void dispose() {
        ksession.dispose();
    }

    @Test
    public void testImplicitEndParallel() throws Exception {
        ProcessInstance processInstance = ksession
                .startProcess("ParallelSplitEnd");
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testSignalEnd() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "MyValue");
        ksession.startProcess("SignalEndEvent", params);
    }

    @Test
    public void testMessageEnd() throws Exception {
        ksession.getWorkItemManager().registerWorkItemHandler("Send Task",
                new SendTaskHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "MyValue");
        ProcessInstance processInstance = ksession.startProcess(
                "MessageEndEvent", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testOnEntryExitScript() throws Exception {
        ksession.getWorkItemManager().registerWorkItemHandler("MyTask",
                new SystemOutWorkItemHandler());
        List<String> myList = new ArrayList<String>();
        ksession.setGlobal("list", myList);
        ProcessInstance processInstance = ksession
                .startProcess("OnEntryExitScriptProcess");
        assertProcessInstanceCompleted(processInstance);
        assertEquals(4, myList.size());
    }

    @Test
    public void testOnEntryExitNamespacedScript() throws Exception {
        ksession.getWorkItemManager().registerWorkItemHandler("MyTask",
                new SystemOutWorkItemHandler());
        List<String> myList = new ArrayList<String>();
        ksession.setGlobal("list", myList);
        ProcessInstance processInstance = ksession
                .startProcess("OnEntryExitNamespacedScriptProcess");
        assertProcessInstanceCompleted(processInstance);
        assertEquals(4, myList.size());
    }

    @Test
    public void testOnEntryExitMixedNamespacedScript() throws Exception {
        ksession.getWorkItemManager().registerWorkItemHandler("MyTask",
                new SystemOutWorkItemHandler());
        List<String> myList = new ArrayList<String>();
        ksession.setGlobal("list", myList);
        ProcessInstance processInstance = ksession
                .startProcess("OnEntryExitMixedNamespacedScriptProcess");
        assertProcessInstanceCompleted(processInstance);
        assertEquals(4, myList.size());
    }

    @Test
    public void testOnEntryExitScriptDesigner() throws Exception {
        ksession.getWorkItemManager().registerWorkItemHandler("MyTask",
                new SystemOutWorkItemHandler());
        List<String> myList = new ArrayList<String>();
        ksession.setGlobal("list", myList);
        ProcessInstance processInstance = ksession
                .startProcess("OnEntryExitDesignerScriptProcess");
        assertProcessInstanceCompleted(processInstance);
        assertEquals(4, myList.size());
    }

    @Test
    public void testCompensateEndEventProcess() throws Exception {
        ProcessInstance processInstance = ksession
                .startProcess("CompensateEndEvent");
        assertProcessInstanceCompleted(processInstance);
        assertNodeTriggered(processInstance.getId(), "StartProcess", "Task",
                "CompensateEvent", "CompensateEvent2", "Compensate", "EndEvent");
    }

}
