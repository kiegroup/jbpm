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
import java.util.Map;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.drools.process.core.datatype.impl.type.ObjectDataType;
import org.jbpm.bpmn2.core.Association;
import org.jbpm.bpmn2.core.DataStore;
import org.jbpm.bpmn2.core.Definitions;
import org.jbpm.process.instance.impl.demo.DoNothingWorkItemHandler;
import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.KieBase;
import org.kie.cdi.KBase;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.process.ProcessInstance;
import org.kie.runtime.process.WorkItem;
import org.kie.runtime.process.WorkItemHandler;
import org.kie.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(CDITestRunner.class)
public class DataTest extends JbpmTestCase {

    @Inject
    @KBase("data")
    private KieBase dataBase;

    private StatefulKnowledgeSession ksession;

    private Logger logger = LoggerFactory.getLogger(DataTest.class);

    public DataTest() {

    }

    @BeforeClass
    public static void setup() throws Exception {
        if (PERSISTENCE) {
            setUpDataSource();
        }
    }

    @Before
    public void init() throws Exception {
        ksession = createKnowledgeSession(dataBase);
    }

    @After
    public void dispose() {
        if (ksession != null) {
            ksession.dispose();
        }
    }

    @Test
    public void testImport() throws Exception {
        ProcessInstance processInstance = ksession.startProcess("Import");
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testDataObject() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employee", "UserId-12345");
        ProcessInstance processInstance = ksession.startProcess("DataObject",
                params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testDataStore() throws Exception {
        ProcessInstance processInstance = ksession.startProcess("DataStore");
        Definitions def = (Definitions) processInstance.getProcess()
                .getMetaData().get("Definitions");
        assertNotNull(def.getDataStores());
        assertTrue(def.getDataStores().size() == 1);
        DataStore dataStore = def.getDataStores().get(0);
        assertEquals("employee", dataStore.getId());
        assertEquals("employeeStore", dataStore.getName());
        assertEquals(String.class.getCanonicalName(),
                ((ObjectDataType) dataStore.getType()).getClassName());
    }

    @Test
    public void testAssociation() throws Exception {
        ProcessInstance processInstance = ksession.startProcess("Association");
        Definitions def = (Definitions) processInstance.getProcess()
                .getMetaData().get("Definitions");
        assertNotNull(def.getAssociations());
        assertTrue(def.getAssociations().size() == 1);
        Association assoc = def.getAssociations().get(0);
        assertEquals("_1234", assoc.getId());
        assertEquals("_1", assoc.getSourceRef());
        assertEquals("_2", assoc.getTargetRef());
    }

    @Test
    public void testUserTaskWithDataStoreScenario() throws Exception {
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new DoNothingWorkItemHandler());
        ksession.startProcess("UserTaskWithDataStore");
        // we can't test further as user tasks are asynchronous.
    }

    @Test
    public void testEvaluationProcess() throws Exception {
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler(
                "RegisterRequest", new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employee", "UserId-12345");
        ProcessInstance processInstance = ksession.startProcess(
                "EvaluationProcess", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testEvaluationProcess2() throws Exception {
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employee", "UserId-12345");
        ProcessInstance processInstance = ksession.startProcess(
                "EvaluationProcess2", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testEvaluationProcess3() throws Exception {
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler(
                "RegisterRequest", new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employee", "john2");
        ProcessInstance processInstance = ksession.startProcess(
                "EvaluationProcess3", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testXpathExpression() throws Exception {
        Document document = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(
                        "<instanceMetadata><user approved=\"false\" /></instanceMetadata>"
                                .getBytes()));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("instanceMetadata", document);
        ProcessInstance processInstance = ksession.startProcess(
                "XPathExpression", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testDataInputAssociations() throws Exception {
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new WorkItemHandler() {
                    public void abortWorkItem(WorkItem manager,
                            WorkItemManager mgr) {

                    }

                    public void executeWorkItem(WorkItem workItem,
                            WorkItemManager mgr) {
                        assertEquals("hello world",
                                workItem.getParameter("coId"));
                    }
                });
        Document document = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream("<user hello='hello world' />"
                        .getBytes()));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("instanceMetadata", document.getFirstChild());
        ProcessInstance processInstance = ksession.startProcess(
                "DataInputAssociations", params);
    }

    @Test
    public void testDataInputAssociationsWithStringObject() throws Exception {
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new WorkItemHandler() {

                    public void abortWorkItem(WorkItem manager,
                            WorkItemManager mgr) {

                    }

                    public void executeWorkItem(WorkItem workItem,
                            WorkItemManager mgr) {
                        assertEquals("hello", workItem.getParameter("coId"));
                    }

                });
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("instanceMetadata", "hello");
        ProcessInstance processInstance = ksession.startProcess(
                "DataInputAssociationsStringObject", params);
    }

    /**
     * FIXME
     * 
     * @throws Exception
     */
    @Test
    @Ignore
    public void testDataInputAssociationsWithLazyLoading() throws Exception {
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new WorkItemHandler() {

                    public void abortWorkItem(WorkItem manager,
                            WorkItemManager mgr) {

                    }

                    public void executeWorkItem(WorkItem workItem,
                            WorkItemManager mgr) {
                        assertEquals("mydoc", ((Element) workItem
                                .getParameter("coId")).getNodeName());
                        assertEquals("mynode", ((Element) workItem
                                .getParameter("coId")).getFirstChild()
                                .getNodeName());
                        assertEquals("user",
                                ((Element) workItem.getParameter("coId"))
                                        .getFirstChild().getFirstChild()
                                        .getNodeName());
                        assertEquals("hello world",
                                ((Element) workItem.getParameter("coId"))
                                        .getFirstChild().getFirstChild()
                                        .getAttributes().getNamedItem("hello")
                                        .getNodeValue());
                    }

                });
        Document document = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream("<user hello='hello world' />"
                        .getBytes()));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("instanceMetadata", document.getFirstChild());
        ProcessInstance processInstance = ksession.startProcess(
                "DataInputAssociationsLazyCreating", params);
    }

    @Test
    public void testDataInputAssociationsWithString() throws Exception {
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new WorkItemHandler() {

                    public void abortWorkItem(WorkItem manager,
                            WorkItemManager mgr) {

                    }

                    public void executeWorkItem(WorkItem workItem,
                            WorkItemManager mgr) {
                        assertEquals("hello", workItem.getParameter("coId"));
                    }

                });
        ProcessInstance processInstance = ksession.startProcess(
                "DataInputAssociationsString", null);
    }

    @Test
    public void testDataInputAssociationsWithStringWithoutQuotes()
            throws Exception {
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new WorkItemHandler() {

                    public void abortWorkItem(WorkItem manager,
                            WorkItemManager mgr) {

                    }

                    public void executeWorkItem(WorkItem workItem,
                            WorkItemManager mgr) {
                        assertEquals("hello", workItem.getParameter("coId"));
                    }

                });
        ProcessInstance processInstance = ksession.startProcess(
                "DataInputAssociationsStringNoQuotes", null);
    }

    @Test
    public void testDataInputAssociationsWithXMLLiteral() throws Exception {
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new WorkItemHandler() {

                    public void abortWorkItem(WorkItem manager,
                            WorkItemManager mgr) {

                    }

                    public void executeWorkItem(WorkItem workItem,
                            WorkItemManager mgr) {
                        assertEquals("id", ((org.w3c.dom.Node) workItem
                                .getParameter("coId")).getNodeName());
                        assertEquals("some text", ((org.w3c.dom.Node) workItem
                                .getParameter("coId")).getFirstChild()
                                .getTextContent());
                    }

                });
        ProcessInstance processInstance = ksession.startProcess(
                "DataInputAssociationsXMLLiteral", null);
    }

    /**
     * FIXME
     * 
     * @throws Exception
     */
    @Test
    @Ignore
    public void testDataInputAssociationsWithTwoAssigns() throws Exception {
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new WorkItemHandler() {

                    public void abortWorkItem(WorkItem manager,
                            WorkItemManager mgr) {

                    }

                    public void executeWorkItem(WorkItem workItem,
                            WorkItemManager mgr) {
                        assertEquals("foo", ((Element) workItem
                                .getParameter("Comment")).getNodeName());
                        // assertEquals("mynode", ((Element)
                        // workItem.getParameter("Comment")).getFirstChild().getNodeName());
                        // assertEquals("user", ((Element)
                        // workItem.getParameter("Comment")).getFirstChild().getFirstChild().getNodeName());
                        // assertEquals("hello world", ((Element)
                        // workItem.getParameter("coId")).getFirstChild().getFirstChild().getAttributes().getNamedItem("hello").getNodeValue());
                    }

                });
        Document document = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream("<user hello='hello world' />"
                        .getBytes()));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("instanceMetadata", document.getFirstChild());
        ProcessInstance processInstance = ksession.startProcess(
                "DataInputAssociationsTwoAssigns", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testDataOutputAssociationsforHumanTask() throws Exception {
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new WorkItemHandler() {

                    public void abortWorkItem(WorkItem manager,
                            WorkItemManager mgr) {

                    }

                    public void executeWorkItem(WorkItem workItem,
                            WorkItemManager mgr) {
                        DocumentBuilderFactory factory = DocumentBuilderFactory
                                .newInstance();
                        DocumentBuilder builder;
                        try {
                            builder = factory.newDocumentBuilder();
                        } catch (ParserConfigurationException e) {
                            // TODO Auto-generated catch block
                            // e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                        final Map<String, Object> results = new HashMap<String, Object>();

                        // process metadata
                        org.w3c.dom.Document processMetadaDoc = builder
                                .newDocument();
                        org.w3c.dom.Element processMetadata = processMetadaDoc
                                .createElement("previoustasksowner");
                        processMetadaDoc.appendChild(processMetadata);
                        // org.w3c.dom.Element procElement =
                        // processMetadaDoc.createElement("previoustasksowner");
                        processMetadata
                                .setAttribute("primaryname", "my_result");
                        // processMetadata.appendChild(procElement);
                        results.put("output", processMetadata);

                        mgr.completeWorkItem(workItem.getId(), results);
                    }

                });
        Map<String, Object> params = new HashMap<String, Object>();
        ProcessInstance processInstance = ksession.startProcess(
                "DataOutputAssociationsHumanTask", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testDataOutputAssociations() throws Exception {
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new WorkItemHandler() {

                    public void abortWorkItem(WorkItem manager,
                            WorkItemManager mgr) {

                    }

                    public void executeWorkItem(WorkItem workItem,
                            WorkItemManager mgr) {
                        try {
                            Document document = DocumentBuilderFactory
                                    .newInstance()
                                    .newDocumentBuilder()
                                    .parse(new ByteArrayInputStream(
                                            "<user hello='hello world' />"
                                                    .getBytes()));
                            Map<String, Object> params = new HashMap<String, Object>();
                            params.put("output", document.getFirstChild());
                            mgr.completeWorkItem(workItem.getId(), params);
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }

                    }

                });
        ProcessInstance processInstance = ksession.startProcess(
                "DataOutputAssociations", null);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testDataOutputAssociationsXmlNode() throws Exception {
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new WorkItemHandler() {

                    public void abortWorkItem(WorkItem manager,
                            WorkItemManager mgr) {

                    }

                    public void executeWorkItem(WorkItem workItem,
                            WorkItemManager mgr) {
                        try {
                            Document document = DocumentBuilderFactory
                                    .newInstance()
                                    .newDocumentBuilder()
                                    .parse(new ByteArrayInputStream(
                                            "<user hello='hello world' />"
                                                    .getBytes()));
                            Map<String, Object> params = new HashMap<String, Object>();
                            params.put("output", document.getFirstChild());
                            mgr.completeWorkItem(workItem.getId(), params);
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }

                    }

                });
        ProcessInstance processInstance = ksession.startProcess(
                "DataOutputAssociationsXMLNode", null);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testStringStructureRef() {
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        ProcessInstance processInstance = ksession.startProcess("StringStructureRef");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);

        Map<String, Object> res = new HashMap<String, Object>();
        res.put("testHT", "test value");
        ksession.getWorkItemManager().completeWorkItem(
                workItemHandler.getWorkItem().getId(), res);
        
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testBooleanStructureRef() {
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        ProcessInstance processInstance = ksession.startProcess("BooleanStructureRef");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);

        Map<String, Object> res = new HashMap<String, Object>();
        res.put("testHT", "true");
        ksession.getWorkItemManager().completeWorkItem(
                workItemHandler.getWorkItem().getId(), res);

        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testIntegerStructureRef() {
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        ProcessInstance processInstance = ksession.startProcess("IntegerStructureRef");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);

        Map<String, Object> res = new HashMap<String, Object>();
        res.put("testHT", "25");
        ksession.getWorkItemManager().completeWorkItem(
                workItemHandler.getWorkItem().getId(), res);

        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testFloatStructureRef() {
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        ProcessInstance processInstance = ksession.startProcess("FloatStructureRef");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);

        Map<String, Object> res = new HashMap<String, Object>();
        res.put("testHT", "5.5");
        ksession.getWorkItemManager().completeWorkItem(
                workItemHandler.getWorkItem().getId(), res);

        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testObjectStructureRef() {
        
        String personAsXml = "<org.jbpm.bpmn2.objects.Person><id>1</id><name>john</name></org.jbpm.bpmn2.objects.Person>";
        
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        ProcessInstance processInstance = ksession.startProcess("ObjectStructureRef");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);

        Map<String, Object> res = new HashMap<String, Object>();
        res.put("testHT", personAsXml);
        ksession.getWorkItemManager().completeWorkItem(
                workItemHandler.getWorkItem().getId(), res);

        assertProcessInstanceFinished(processInstance, ksession);
    }

}
