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

package org.jbpm.bpmn2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.drools.core.event.DefaultProcessEventListener;
import org.jbpm.bpmn2.core.Association;
import org.jbpm.bpmn2.core.DataStore;
import org.jbpm.bpmn2.core.Definitions;
import org.jbpm.bpmn2.xml.ProcessHandler;
import org.jbpm.process.core.datatype.impl.type.ObjectDataType;
import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.kie.api.KieBase;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(Parameterized.class)
public class DataTest extends JbpmBpmn2TestCase {

    private class CounterProcessEventListener extends DefaultProcessEventListener {

        private int counter = 0;

        protected void incCounter() {
            counter++;
        }

        public int getCounter() {
            return counter;
        }

        public void afterVariableChanged(ProcessVariableChangedEvent event) {
            assertNotEquals(event.getOldValue(), event.getNewValue());
            if (event.getOldValue() != null) {
                incCounter();
            }
        }

        public void beforeVariableChanged(ProcessVariableChangedEvent event) {
            assertNotEquals(event.getOldValue(), event.getNewValue());
            if (event.getOldValue() != null) {
                incCounter();
            }
        }
    }

    public static class Person implements Serializable {

        private static final long serialVersionUID = 1L;
        private String name;
        private Address address;

        public Person(String name, Address address) {
            this.name = name;
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


        public void setAddress(Address address) {
            this.address = address;
        }

        public Address getAddress() {
            return address;
        }

        @Override
        public int hashCode() {
            return Objects.hash(address, name);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Person)) {
                return false;
            }
            Person other = (Person) obj;
            return Objects.equals(address, other.address) && Objects.equals(name, other.name);
        }

        @Override
        public String toString() {
            return "Person [name=" + name + ", address=" + address + "]";
        }
        
        
    }

    public static class Address implements Serializable {

        private static final long serialVersionUID = 1L;
        private String city;
        private String country;

        public Address(String city, String country) {
            this.city = city;
            this.country = country;
        }


        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }


        @Override
        public int hashCode() {
            return Objects.hash(city, country);
        }


        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Address)) {
                return false;
            }
            Address other = (Address) obj;
            return Objects.equals(city, other.city) && Objects.equals(country, other.country);
        }


        @Override
        public String toString() {
            return "Address [city=" + city + ", country=" + country + "]";
        }
        
        
    }


    @Parameters
    public static Collection<Object[]> persistence() {
        Object[][] data = new Object[][]{{false}, {true}};
        return Arrays.asList(data);
    };



    private static final Logger logger = LoggerFactory.getLogger(DataTest.class);

    private StatefulKnowledgeSession ksession;

    public DataTest(boolean persistence) {
        super(persistence);
    }

    @BeforeClass
    public static void setup() throws Exception {
        setUpDataSource();
    }

    @After
    public void dispose() {
        if (ksession != null) {
            ksession.dispose();
            ksession = null;
        }
    }

    @Test
    public void testImport() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-Import.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ProcessInstance processInstance = ksession.startProcess("Import");
        assertProcessInstanceCompleted(processInstance);

    }

    @Test
    public void testDataObject() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-DataObject.bpmn2");
        ksession = createKnowledgeSession(kbase);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employee", "UserId-12345");
        ProcessInstance processInstance = ksession.startProcess("Evaluation",
                params);
        assertProcessInstanceCompleted(processInstance);

    }

    @Test
    public void testDataStore() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-DataStore.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ProcessInstance processInstance = ksession.startProcess("Evaluation");
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
        KieBase kbase = createKnowledgeBase("BPMN2-Association.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ProcessInstance processInstance = ksession.startProcess("Evaluation");
        List<Association> associations = (List<Association>) processInstance.getProcess().getMetaData().get(
                ProcessHandler.ASSOCIATIONS);
        assertNotNull(associations);
        assertTrue(associations.size() == 1);
        Association assoc = associations.get(0);
        assertEquals("_1234", assoc.getId());
        assertEquals("_1", assoc.getSourceRef());
        assertEquals("_2", assoc.getTargetRef());

    }

    @Test
    public void testEvaluationProcess() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-EvaluationProcess.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler(
                "RegisterRequest", new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employee", "UserId-12345");
        ProcessInstance processInstance = ksession.startProcess("Evaluation",
                params);
        assertProcessInstanceCompleted(processInstance);

    }

    @Test
    public void testEvaluationProcess2() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-EvaluationProcess2.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employee", "UserId-12345");
        ProcessInstance processInstance = ksession.startProcess(
                "com.sample.evaluation", params);
        assertProcessInstanceCompleted(processInstance);

    }

    @Test
    public void testEvaluationProcess3() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-EvaluationProcess3.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler(
                "RegisterRequest", new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employee", "john2");
        ProcessInstance processInstance = ksession.startProcess("Evaluation",
                params);
        assertProcessInstanceCompleted(processInstance);

    }

    @Test
    public void testXpathExpression() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-XpathExpression.bpmn2");
        ksession = createKnowledgeSession(kbase);
        Document document = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(
                        "<instanceMetadata><user approved=\"false\" /></instanceMetadata>"
                                .getBytes()));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("instanceMetadata", document);
        ProcessInstance processInstance = ksession.startProcess("XPathProcess",
                params);
        assertProcessInstanceCompleted(processInstance);

    }

    @Test
    public void testDataInputAssociations() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-DataInputAssociations.bpmn2");
        ksession = createKnowledgeSession(kbase);
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
        ProcessInstance processInstance = ksession.startProcess("process",
                params);

    }

    @Test
    public void testDataInputAssociationsWithStringObject() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-DataInputAssociations-string-object.bpmn2");
        ksession = createKnowledgeSession(kbase);
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
        ProcessInstance processInstance = ksession.startProcess("process",
                params);

    }


    @Test
    public void testDataInputAssociationsWithPojoPartial() throws Exception {
        internalTestDataInputAssociationWithPojo("BPMN2-DataInputAssociations-Pojo.bpmn2");
    }

    @Test
    public void testDataInputAssociationsWithPojoWithoutSource() throws Exception {
        internalTestDataInputAssociationWithPojo("BPMN2-DataInputAssociationsWithoutSource.bpmn2");
    }

    @Test
    public void testDataInputAssociationsWithPojoComplete() throws Exception {
        internalTestDataInputAssociationWithPojo("BPMN2-DataInputAssociations-Pojo-Complete.bpmn2");
    }

    private void internalTestDataInputAssociationWithPojo(String bpmnFile) throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper(bpmnFile);
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new WorkItemHandler() {

                    public void abortWorkItem(WorkItem manager,
                                              WorkItemManager mgr) {

                    }

                    public void executeWorkItem(WorkItem workItem,
                                                WorkItemManager mgr) {
                        assertEquals("Sevilla", workItem.getParameter("coId"));
                    }

                });
        ksession.startProcess("process", Collections.singletonMap("instanceMetadata", new Person("Javierito",
                new Address("Sevilla", "Spain"))));
    }

    @Test
    public void testDataInputAssociationsWithPojoNull() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-DataInputAssociations-Pojo-Complete.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new WorkItemHandler() {

                    public void abortWorkItem(WorkItem manager,
                                              WorkItemManager mgr) {

                    }

                    public void executeWorkItem(WorkItem workItem,
                                                WorkItemManager mgr) {
                        assertNull(workItem.getParameter("coId"));
                    }

                });
        ksession.startProcess("process", Collections.singletonMap("instanceMetadata", new Person("Javierito", null)));
    }

    /**
     * TODO testDataInputAssociationsWithLazyLoading
     */
    @Test
    @Ignore
    public void testDataInputAssociationsWithLazyLoading() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-DataInputAssociations-lazy-creating.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new WorkItemHandler() {

                    public void abortWorkItem(WorkItem manager,
                                              WorkItemManager mgr) {

                    }

                    public void executeWorkItem(WorkItem workItem,
                                                WorkItemManager mgr) {
                        Object coIdParamObj = workItem.getParameter("coId");
                        assertEquals("mydoc", ((Element) coIdParamObj).getNodeName());
                        assertEquals("mynode", ((Element) workItem.getParameter("coId")).getFirstChild().getNodeName());
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
        ProcessInstance processInstance = ksession.startProcess("process",
                params);

    }

    @Test
    public void testDataInputAssociationsWithString() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-DataInputAssociations-string.bpmn2");
        ksession = createKnowledgeSession(kbase);
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
        ProcessInstance processInstance = ksession
                .startProcess("process");

    }

    @Test
    public void testDataInputAssociationsWithStringWithoutQuotes() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-DataInputAssociations-string-no-quotes.bpmn2");
        ksession = createKnowledgeSession(kbase);
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
        ProcessInstance processInstance = ksession
                .startProcess("process");

    }

    @Test
    public void testDataInputAssociationsWithXMLLiteral() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-DataInputAssociations-xml-literal.bpmn2");
        ksession = createKnowledgeSession(kbase);
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
        ProcessInstance processInstance = ksession
                .startProcess("process");

    }

    /**
     * TODO testDataInputAssociationsWithTwoAssigns
     */
    @Test
    @Ignore
    public void testDataInputAssociationsWithTwoAssigns() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-DataInputAssociations-two-assigns.bpmn2");
        ksession = createKnowledgeSession(kbase);
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
        ProcessInstance processInstance = ksession.startProcess("process",
                params);

    }

    @Test
    public void testDataOutputAssociationsforHumanTask() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-DataOutputAssociations-HumanTask.bpmn2");
        ksession = createKnowledgeSession(kbase);
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
        ProcessInstance processInstance = ksession.startProcess("process",
                params);

    }

    @Test
    public void testDataOutputAssociations() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-DataOutputAssociations.bpmn2");
        ksession = createKnowledgeSession(kbase);
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
        ProcessInstance processInstance = ksession
                .startProcess("process");

    }

    @Test
    public void testDataOutputAssociationsWithPojo() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-DataOutputAssociations-Pojo.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new WorkItemHandler() {

                    public void abortWorkItem(WorkItem manager,
                                              WorkItemManager mgr) {

                    }

                    public void executeWorkItem(WorkItem workItem,
                                                WorkItemManager mgr) {
                        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(
                                workItem.getProcessInstanceId());
                        Person person = (Person) processInstance.getVariable("instanceMetadata");
                        assertNotNull(person);
                        assertEquals("Napoleon", person.getName());
                        assertEquals("Paris", person.getAddress().getCity());
                        assertEquals("France", person.getAddress().getCountry());
                        mgr.completeWorkItem(workItem.getId(), Collections.singletonMap("output", new Person(
                                "Javierito", new Address("Sevilla", "Spain"))));
                        person = (Person) processInstance.getVariable("instanceMetadata");
                        assertEquals("Napoleon", person.getName());
                        assertEquals("Sevilla", person.getAddress().getCity());
                        assertEquals("Spain", person.getAddress().getCountry());
                    }

                });
        ksession.startProcess("process", Collections.singletonMap("instanceMetadata", new Person("Napoleon",
                        new Address("Paris", "France"))));
    }

    @Test
    public void testDataInputAssociationsStunnerWithPojo() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-DataInputAssociationStunner.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new WorkItemHandler() {

                    public void abortWorkItem(WorkItem manager,
                                              WorkItemManager mgr) {}

                    public void executeWorkItem(WorkItem workItem,
                                                WorkItemManager mgr) {
                        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession
                                .getProcessInstance(
                                        workItem.getProcessInstanceId());
                        Person person = (Person) processInstance.getVariable("instanceMetadata");
                        assertNotNull(person);
                        assertEquals("Napoleon", person.getName());
                        assertEquals("Paris", person.getAddress().getCity());
                        assertEquals("France", person.getAddress().getCountry());
                        mgr.completeWorkItem(workItem.getId(), Collections.emptyMap());
                        Address address = (Address) workItem.getParameter("output");
                        assertEquals("Paris", address.getCity());
                        assertEquals("France", address.getCountry());
                    }

                });

        ksession.startProcess("process", Collections.singletonMap("instanceMetadata", new Person("Napoleon",
                new Address("Paris", "France"))));
    }

    @Test
    public void testDataOutputAssociationsStunnerWithPojo() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-DataOutputAssociationStunner.bpmn2");
        ksession = createKnowledgeSession(kbase);
        CounterProcessEventListener listener = new CounterProcessEventListener();
        ksession.addEventListener(listener);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new WorkItemHandler() {

                    public void abortWorkItem(WorkItem manager,
                                              WorkItemManager mgr) {

                    }

                    public void executeWorkItem(WorkItem workItem,
                                                WorkItemManager mgr) {
                        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(
                                workItem.getProcessInstanceId());
                        Person person = (Person) processInstance.getVariable("instanceMetadata");
                        assertNotNull(person);
                        assertEquals("Napoleon", person.getName());
                        assertEquals("Paris", person.getAddress().getCity());
                        assertEquals("France", person.getAddress().getCountry());
                        mgr.completeWorkItem(workItem.getId(), Collections.singletonMap("output", new Address("Sevilla",
                                "Spain")));
                        person = (Person) processInstance.getVariable("instanceMetadata");
                        assertEquals("Napoleon", person.getName());
                        assertEquals("Sevilla", person.getAddress().getCity());
                        assertEquals("Spain", person.getAddress().getCountry());
                    }

                });
        ksession.startProcess("process", Collections.singletonMap("instanceMetadata", new Person("Napoleon",
                new Address("Paris", "France"))));
        assertEquals(2, listener.getCounter());
    }

    @Test
    public void testDataOutputAssociationsWithPojoEmptyFrom() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-DataOutputAssociations-Pojo-EmptyFrom.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new WorkItemHandler() {

                    public void abortWorkItem(WorkItem manager,
                                              WorkItemManager mgr) {

                    }

                    public void executeWorkItem(WorkItem workItem,
                                                WorkItemManager mgr) {
                        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(
                                workItem.getProcessInstanceId());
                        Person person = (Person) processInstance.getVariable("instanceMetadata");
                        assertNotNull(person);
                        assertEquals("Napoleon", person.getName());
                        assertEquals("Paris", person.getAddress().getCity());
                        assertEquals("France", person.getAddress().getCountry());
                        mgr.completeWorkItem(workItem.getId(), Collections.singletonMap("instanceAddress", new Address(
                                "Sevilla", "Spain")));
                        person = (Person) processInstance.getVariable("instanceMetadata");
                        assertEquals("Napoleon", person.getName());
                        assertEquals("Sevilla", person.getAddress().getCity());
                        assertEquals("Spain", person.getAddress().getCountry());
                    }

                });
        ksession.startProcess("process", Collections.singletonMap("instanceMetadata", new Person("Napoleon",
                new Address("Paris", "France"))));
    }

    @Test
    public void testDataOutputAssociationsWithPojoList() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-DataOutputAssociations-PojoList.bpmn2");
        ksession = createKnowledgeSession(kbase);
        CounterProcessEventListener listener = new CounterProcessEventListener();
        ksession.addEventListener(listener);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new WorkItemHandler() {

                    public void abortWorkItem(WorkItem manager,
                                              WorkItemManager mgr) {

                    }

                    public void executeWorkItem(WorkItem workItem,
                                                WorkItemManager mgr) {
                        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(
                                workItem.getProcessInstanceId());
                        List<Person> persons = (List<Person>) processInstance.getVariable("instanceMetadata");
                        assertNotNull(persons);
                        Person person = persons.get(0);
                        assertEquals("Javierito", person.getName());
                        assertEquals("Paris", person.getAddress().getCity());
                        assertEquals("France", person.getAddress().getCountry());
                        mgr.completeWorkItem(workItem.getId(), Collections.singletonMap("output", Arrays.asList(
                                new Person("Javierito", new Address("Paris", "France")), new Person("Napoleon",
                                        new Address("Sevilla", "Spain")))));
                        persons = (List<Person>) processInstance.getVariable("instanceMetadata");
                        assertNotNull(persons);
                        person = persons.get(0);
                        assertEquals("Javierito", person.getName());
                        assertEquals("Sevilla", person.getAddress().getCity());
                        assertEquals("Spain", person.getAddress().getCountry());
                    }

                });
        ksession.startProcess("process", Collections.singletonMap("instanceMetadata", Arrays.asList(new Person(
                "Javierito", new Address("Paris", "France")), new Person("Napoleon", new Address("Madrid",
                        "Spain")))));
        assertEquals(2, listener.getCounter());
    }

    @Test
    public void testDataOutputAssociationsWithPojoMap() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-DataOutputAssociations-Map.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new WorkItemHandler() {
                    public void abortWorkItem(WorkItem manager,
                                              WorkItemManager mgr) {
                    }

                    public void executeWorkItem(WorkItem workItem,
                                                WorkItemManager mgr) {
                        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(
                                workItem.getProcessInstanceId());
                        @SuppressWarnings("unchecked")
                        Map<String, Object> person = (Map<String, Object>) processInstance.getVariable(
                                "instanceMetadata");
                        assertNotNull(person);
                        assertEquals("Javierito", person.get("name"));
                        Address address = (Address) person.get("address");
                        assertEquals("Paris", address.getCity());
                        assertEquals("France", address.getCountry());
                        mgr.completeWorkItem(workItem.getId(), Collections.singletonMap("output", Arrays.asList(
                                new Person("Javierito", new Address("Paris", "France")), new Person("Napoleon",
                                        new Address("Sevilla", "Spain")))));
                        person = (Map<String, Object>) processInstance.getVariable(
                                "instanceMetadata");
                        assertEquals("Javierito", person.get("name"));
                        address = (Address) person.get("address");
                        assertEquals("Sevilla", address.getCity());
                        assertEquals("Spain", address.getCountry());
                    }

                });
        Map<String, Object> person = new HashMap<>();
        person.put("name", "Javierito");
        person.put("address", new Address("Paris", "France"));
        ksession.startProcess("process", Collections.singletonMap("instanceMetadata", person));
    }


    @Test
    public void testDataOutputAssociationsXmlNode() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-DataOutputAssociations-xml-node.bpmn2");
        ksession = createKnowledgeSession(kbase);
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
        ProcessInstance processInstance = ksession
                .startProcess("process");

    }

}
