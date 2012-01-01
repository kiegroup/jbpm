package org.jbpm.bpmn2;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.definition.process.Node;
import org.drools.definition.process.Process;
import org.drools.impl.KnowledgeBaseFactoryServiceImpl;
import org.drools.io.ResourceFactory;
import org.jbpm.bpmn2.xml.BPMNDISemanticModule;
import org.jbpm.bpmn2.xml.BPMNExtensionsSemanticModule;
import org.jbpm.bpmn2.xml.BPMNSemanticModule;
import org.jbpm.bpmn2.xml.XmlBPMNProcessDumper;
import org.jbpm.compiler.xml.XmlProcessReader;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.jbpm.workflow.core.node.TaskDeadline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserTaskDeadlineTest extends JbpmJUnitTestCase {

    private Logger logger = LoggerFactory.getLogger(SimpleBPMNProcessTest.class);
    
    public void testMinimalProcess() throws Exception {
       KnowledgeBase kbase = createKnowledgeBase("BPMN2-UserTaskDeadlines.bpmn2");
       Process p = kbase.getProcess("UserTask");
       Node[] nodes = ((RuleFlowProcess)p).getNodes();
       
       assertEquals(3, nodes.length);
       for (Node n : nodes) {
           
           if (n instanceof HumanTaskNode) {
               assertEquals(2, ((HumanTaskNode) n).getWork().getParameters().size());
               List<TaskDeadline> deadlines = (List<TaskDeadline>) ((HumanTaskNode) n).getWork().getParameter("Deadlines");
               assertNotNull(deadlines);
               assertEquals(1, deadlines.size());
               
               assertEquals(1, deadlines.get(0).getNotifications().size());
               assertEquals(1, deadlines.get(0).getReassignments().size());
               
               assertNotNull(deadlines.get(0).getExpires());
               assertNotNull(deadlines.get(0).getType());
               break;
           }
       
       }

    }
    
    private KnowledgeBase createKnowledgeBase(String process) throws Exception {
        KnowledgeBaseFactory
                .setKnowledgeBaseServiceFactory(new KnowledgeBaseFactoryServiceImpl());
        KnowledgeBuilderConfiguration conf = KnowledgeBuilderFactory
                .newKnowledgeBuilderConfiguration();
        ((PackageBuilderConfiguration) conf).initSemanticModules();
        ((PackageBuilderConfiguration) conf)
                .addSemanticModule(new BPMNSemanticModule());
        ((PackageBuilderConfiguration) conf)
                .addSemanticModule(new BPMNDISemanticModule());
        ((PackageBuilderConfiguration) conf)
                .addSemanticModule(new BPMNExtensionsSemanticModule());
        // ProcessDialectRegistry.setDialect("XPath", new XPathDialect());
        XmlProcessReader processReader = new XmlProcessReader(
                ((PackageBuilderConfiguration) conf).getSemanticModules(),
                getClass().getClassLoader());
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
                .newKnowledgeBuilder(conf);
        List<Process> processes = processReader
                .read(SimpleBPMNProcessTest.class.getResourceAsStream("/"
                        + process));
        for (Process p : processes) {
            RuleFlowProcess ruleFlowProcess = (RuleFlowProcess) p;
            logger.debug(XmlBPMNProcessDumper.INSTANCE
                    .dump(ruleFlowProcess));
            kbuilder.add(ResourceFactory.newReaderResource(new StringReader(
                    XmlBPMNProcessDumper.INSTANCE.dump(ruleFlowProcess))),
                    ResourceType.BPMN2);
        }
        kbuilder.add(ResourceFactory
                .newReaderResource(new InputStreamReader(
                        SimpleBPMNProcessTest.class.getResourceAsStream("/"
                                + process))), ResourceType.BPMN2);
        if (!kbuilder.getErrors().isEmpty()) {
            for (KnowledgeBuilderError error : kbuilder.getErrors()) {
                logger.error(error.toString());
            }
            throw new IllegalArgumentException(
                    "Errors while parsing knowledge base");
        }
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        return kbase;
    }
}
