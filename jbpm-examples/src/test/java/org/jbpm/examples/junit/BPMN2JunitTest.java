package org.jbpm.examples.junit;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.jbpm.bpmn2.handler.ServiceTaskHandler;
import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.junit.Test;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.process.ProcessInstance;
import org.kie.runtime.process.WorkflowProcessInstance;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BPMN2JunitTest extends SharedBPMN2JUnitTest {
    
    public BPMN2JunitTest() {

    }
    
    @Test
    public void testExclusiveSplitXPathAdvanced() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("ExclusiveSplitXPathAdvanced.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("Email", new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element hi = doc.createElement("hi");
        Element ho = doc.createElement("ho");
        hi.appendChild(ho);
        Attr attr = doc.createAttribute("value");
        ho.setAttributeNode(attr);
        attr.setValue("a");
        params.put("x", hi);
        params.put("y", "Second");
        ProcessInstance processInstance = ksession.startProcess("ExclusiveSplitXPathAdvanced", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testExclusiveSplitXPathAdvanced2() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("ExclusiveSplitXPathAdvancedVarsNotSignaled.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("Email", new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element hi = doc.createElement("hi");
        Element ho = doc.createElement("ho");
        hi.appendChild(ho);
        Attr attr = doc.createAttribute("value");
        ho.setAttributeNode(attr);
        attr.setValue("a");
        params.put("x", hi);
        params.put("y", "Second");
        ProcessInstance processInstance = ksession.startProcess("ExclusiveSplitXPathAdvancedVarsNotSignaled", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testExclusiveSplitXPathAdvancedWithVars() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("ExclusiveSplitXPathAdvancedWithVars.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("Email", new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element hi = doc.createElement("hi");
        Element ho = doc.createElement("ho");
        hi.appendChild(ho);
        Attr attr = doc.createAttribute("value");
        ho.setAttributeNode(attr);
        attr.setValue("a");
        params.put("x", hi);
        params.put("y", "Second");
        ProcessInstance processInstance = ksession.startProcess("ExclusiveSplitXPathAdvancedWithVars", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testServiceTask() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("ServiceProcess.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("Service Task", new ServiceTaskHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("s", "john");
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance)
            ksession.startProcess("ServiceProcess", params);
        assertProcessInstanceFinished(processInstance, ksession);
        assertEquals("Hello john!", processInstance.getVariable("s"));
    }

    @Test
    public void testXpathExpression() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("XpathExpression.bpmn2");
        Document document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().parse(new ByteArrayInputStream(
                "<instanceMetadata><user approved=\"false\" /></instanceMetadata>".getBytes()));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("instanceMetadata", document);
        ProcessInstance processInstance = ksession.startProcess("XPathExpression", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testXORGateway() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("XORGateway.bpmn2");
        Document document = DocumentBuilderFactory.newInstance()
        .newDocumentBuilder().parse(new ByteArrayInputStream(
                "<instanceMetadata><user approved=\"false\" /></instanceMetadata>".getBytes()));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("instanceMetadata", document);
        params.put("startMessage", DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(new ByteArrayInputStream(
                        "<task subject='foobar2'/>".getBytes())).getFirstChild());
        ProcessInstance processInstance = ksession.startProcess("XORGateway", params);
        assertProcessInstanceCompleted(processInstance);
    }
	
}
