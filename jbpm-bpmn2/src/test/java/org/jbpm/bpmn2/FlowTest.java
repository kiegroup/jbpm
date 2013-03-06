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

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilderFactory;

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
import org.kie.runtime.process.WorkItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(CDITestRunner.class)
public class FlowTest extends JbpmTestCase {

    @Inject
    @KBase("flow")
    private KieBase flowBase;

    private StatefulKnowledgeSession ksession;

    private Logger logger = LoggerFactory.getLogger(FlowTest.class);

    public FlowTest() {

    }

    @BeforeClass
    public static void setup() throws Exception {
        if (PERSISTENCE) {
            setUpDataSource();
        }
    }

    @Before
    public void init() throws Exception {
        ksession = createKnowledgeSession(flowBase);
    }

    @After
    public void dispose() {
        if (ksession != null) {
            ksession.dispose();
        }
    }

    @Test
    public void testExclusiveSplit() throws Exception {
        ksession.getWorkItemManager().registerWorkItemHandler("Email",
                new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "First");
        params.put("y", "Second");
        ProcessInstance processInstance = ksession.startProcess(
                "ExclusiveSplit", params);
        
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testExclusiveSplitXPathAdvanced() throws Exception {
        ksession.getWorkItemManager().registerWorkItemHandler("Email",
                new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        Element hi = doc.createElement("hi");
        Element ho = doc.createElement("ho");
        hi.appendChild(ho);
        Attr attr = doc.createAttribute("value");
        ho.setAttributeNode(attr);
        attr.setValue("a");
        params.put("x", hi);
        params.put("y", "Second");
        ProcessInstance processInstance = ksession.startProcess(
                "ExclusiveSplitXPathAdvanced", params);
        
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testExclusiveSplitXPathAdvanced2() throws Exception {
        ksession.getWorkItemManager().registerWorkItemHandler("Email",
                new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        Element hi = doc.createElement("hi");
        Element ho = doc.createElement("ho");
        hi.appendChild(ho);
        Attr attr = doc.createAttribute("value");
        ho.setAttributeNode(attr);
        attr.setValue("a");
        params.put("x", hi);
        params.put("y", "Second");
        ProcessInstance processInstance = ksession.startProcess(
                "ExclusiveSplitXPathAdvancedVarsNotSignaled", params);
        
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testExclusiveSplitXPathAdvancedWithVars() throws Exception {
        ksession.getWorkItemManager().registerWorkItemHandler("Email",
                new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        Element hi = doc.createElement("hi");
        Element ho = doc.createElement("ho");
        hi.appendChild(ho);
        Attr attr = doc.createAttribute("value");
        ho.setAttributeNode(attr);
        attr.setValue("a");
        params.put("x", hi);
        params.put("y", "Second");
        ProcessInstance processInstance = ksession.startProcess(
                "ExclusiveSplitXPathAdvancedWithVars", params);
        
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testExclusiveSplitPriority() throws Exception {
        ksession.getWorkItemManager().registerWorkItemHandler("Email",
                new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "First");
        params.put("y", "Second");
        ProcessInstance processInstance = ksession.startProcess(
                "ExclusiveSplitPriority", params);
        
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testExclusiveSplitDefault() throws Exception {
        ksession.getWorkItemManager().registerWorkItemHandler("Email",
                new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "NotFirst");
        params.put("y", "Second");
        ProcessInstance processInstance = ksession.startProcess(
                "ExclusiveSplitDefault", params);
        
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testInclusiveSplit() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", 15);
        ProcessInstance processInstance = ksession.startProcess(
                "InclusiveSplit", params);
        
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testInclusiveSplitAndJoin() throws Exception {
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", 15);
        ProcessInstance processInstance = ksession.startProcess(
                "InclusiveSplitAndJoin", params);

        List<WorkItem> activeWorkItems = workItemHandler.getWorkItems();

        assertEquals(2, activeWorkItems.size());
        restoreSession(ksession, true);

        for (WorkItem wi : activeWorkItems) {
            ksession.getWorkItemManager().completeWorkItem(wi.getId(), null);
        }
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testInclusiveSplitAndJoinLoop() throws Exception {
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", 21);
        ProcessInstance processInstance = ksession.startProcess(
                "InclusiveSplitAndJoinLoop", params);

        List<WorkItem> activeWorkItems = workItemHandler.getWorkItems();

        assertEquals(3, activeWorkItems.size());
        restoreSession(ksession, true);

        for (WorkItem wi : activeWorkItems) {
            ksession.getWorkItemManager().completeWorkItem(wi.getId(), null);
        }
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testInclusiveSplitAndJoinLoop2() throws Exception {
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", 21);
        ProcessInstance processInstance = ksession.startProcess(
                "InclusiveSplitAndJoinLoop2", params);

        List<WorkItem> activeWorkItems = workItemHandler.getWorkItems();

        assertEquals(3, activeWorkItems.size());
        restoreSession(ksession, true);

        for (WorkItem wi : activeWorkItems) {
            ksession.getWorkItemManager().completeWorkItem(wi.getId(), null);
        }
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testInclusiveSplitAndJoinNested() throws Exception {
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", 15);
        ProcessInstance processInstance = ksession.startProcess(
                "InclusiveSplitAndJoinNested", params);

        List<WorkItem> activeWorkItems = workItemHandler.getWorkItems();

        assertEquals(2, activeWorkItems.size());
        restoreSession(ksession, true);

        for (WorkItem wi : activeWorkItems) {
            ksession.getWorkItemManager().completeWorkItem(wi.getId(), null);
        }

        activeWorkItems = workItemHandler.getWorkItems();
        assertEquals(2, activeWorkItems.size());
        restoreSession(ksession, true);

        for (WorkItem wi : activeWorkItems) {
            ksession.getWorkItemManager().completeWorkItem(wi.getId(), null);
        }
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testInclusiveSplitAndJoinEmbedded() throws Exception {
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", 15);
        ProcessInstance processInstance = ksession.startProcess(
                "InclusiveSplitAndJoinEmbedded", params);

        List<WorkItem> activeWorkItems = workItemHandler.getWorkItems();

        assertEquals(2, activeWorkItems.size());
        restoreSession(ksession, true);

        for (WorkItem wi : activeWorkItems) {
            ksession.getWorkItemManager().completeWorkItem(wi.getId(), null);
        }
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testInclusiveSplitAndJoinWithParallel() throws Exception {
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", 25);
        ProcessInstance processInstance = ksession.startProcess(
                "InclusiveSplitAndJoinWithParallel", params);

        List<WorkItem> activeWorkItems = workItemHandler.getWorkItems();

        assertEquals(4, activeWorkItems.size());
        restoreSession(ksession, true);

        for (WorkItem wi : activeWorkItems) {
            ksession.getWorkItemManager().completeWorkItem(wi.getId(), null);
        }
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testInclusiveSplitAndJoinWithEnd() throws Exception {
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", 25);
        ProcessInstance processInstance = ksession.startProcess(
                "InclusiveSplitAndJoinWithEnd", params);

        List<WorkItem> activeWorkItems = workItemHandler.getWorkItems();

        assertEquals(3, activeWorkItems.size());
        restoreSession(ksession, true);

        for (int i = 0; i < 2; i++) {
            ksession.getWorkItemManager().completeWorkItem(
                    activeWorkItems.get(i).getId(), null);
        }
        assertProcessInstanceActive(processInstance);

        ksession.getWorkItemManager().completeWorkItem(
                activeWorkItems.get(2).getId(), null);
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testInclusiveSplitAndJoinWithTimer() throws Exception {
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", 15);
        ProcessInstance processInstance = ksession.startProcess(
                "InclusiveSplitAndJoinWithTimer", params);

        List<WorkItem> activeWorkItems = workItemHandler.getWorkItems();

        assertEquals(1, activeWorkItems.size());
        ksession.getWorkItemManager().completeWorkItem(
                activeWorkItems.get(0).getId(), null);
        Thread.sleep(2000);
        assertProcessInstanceActive(processInstance);

        activeWorkItems = workItemHandler.getWorkItems();
        assertEquals(2, activeWorkItems.size());
        ksession.getWorkItemManager().completeWorkItem(
                activeWorkItems.get(0).getId(), null);
        assertProcessInstanceActive(processInstance);

        ksession.getWorkItemManager().completeWorkItem(
                activeWorkItems.get(1).getId(), null);
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testInclusiveSplitAndJoinExtraPath() throws Exception {
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", 25);
        ProcessInstance processInstance = ksession.startProcess(
                "InclusiveSplitAndJoinExtraPath", params);

        ksession.signalEvent("signal", null);

        List<WorkItem> activeWorkItems = workItemHandler.getWorkItems();

        assertEquals(4, activeWorkItems.size());
        restoreSession(ksession, true);

        for (int i = 0; i < 3; i++) {
            ksession.getWorkItemManager().completeWorkItem(
                    activeWorkItems.get(i).getId(), null);
        }
        assertProcessInstanceActive(processInstance);

        ksession.getWorkItemManager().completeWorkItem(
                activeWorkItems.get(3).getId(), null);
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testInclusiveSplitDefault() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", -5);
        ProcessInstance processInstance = ksession.startProcess(
                "InclusiveSplitDefault", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testXORGateway() throws Exception {
        Document document = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(
                        "<instanceMetadata><user approved=\"false\" /></instanceMetadata>"
                                .getBytes()));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("instanceMetadata", document);
        params.put(
                "startMessage",
                DocumentBuilderFactory
                        .newInstance()
                        .newDocumentBuilder()
                        .parse(new ByteArrayInputStream(
                                "<task subject='foobar2'/>".getBytes()))
                        .getFirstChild());
        ProcessInstance processInstance = ksession.startProcess("XORGateway",
                params);
        
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testLane() throws Exception {
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        ProcessInstance processInstance = ksession.startProcess("Lane");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        WorkItem workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        assertEquals("john", workItem.getParameter("ActorId"));
        Map<String, Object> results = new HashMap<String, Object>();
        results.put("ActorId", "mary");
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(),
                results);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        assertEquals("mary", workItem.getParameter("ActorId"));
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
        assertProcessInstanceFinished(processInstance, ksession);
    }

}
