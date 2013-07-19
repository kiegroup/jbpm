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

package org.jbpm.bpmn2;

import java.util.Properties;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.process.WorkflowProcess;
import org.drools.impl.EnvironmentFactory;
import org.drools.io.ResourceFactory;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.bpmn2.xml.XmlBPMNProcessDumper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

public class BPMN2Test extends JbpmBpmn2TestCase {
	
    private static Logger logger = LoggerFactory.getLogger(BPMN2Test.class);
    
	public void testResourceType() {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		kbuilder.add(ResourceFactory.newClassPathResource("BPMN2-MinimalProcess.bpmn2"), ResourceType.BPMN2);
		KnowledgeBase kbase = kbuilder.newKnowledgeBase();
		Properties properties = new Properties();
		properties.put("drools.processInstanceManagerFactory", "org.jbpm.process.instance.impl.DefaultProcessInstanceManagerFactory");
		properties.put("drools.processSignalManagerFactory", "org.jbpm.process.instance.event.DefaultSignalManagerFactory");
		KnowledgeSessionConfiguration config = KnowledgeBaseFactory.newKnowledgeSessionConfiguration(properties);
		StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession(config, EnvironmentFactory.newEnvironment());
		ksession.startProcess("Minimal");
	}

    public void testMultipleProcessInOneFile() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("BPMN2-MultipleProcessInOneFile.xml"), ResourceType.BPMN2);
        KnowledgeBase kbase = kbuilder.newKnowledgeBase();
        Properties properties = new Properties();
        properties.put("drools.processInstanceManagerFactory", "org.jbpm.process.instance.impl.DefaultProcessInstanceManagerFactory");
        properties.put("drools.processSignalManagerFactory", "org.jbpm.process.instance.event.DefaultSignalManagerFactory");
        KnowledgeSessionConfiguration config = KnowledgeBaseFactory.newKnowledgeSessionConfiguration(properties);
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession(config, EnvironmentFactory.newEnvironment());
        ProcessInstance processInstance = ksession.startProcess("Evaluation");
        assertNotNull(processInstance);
        ProcessInstance processInstance2 = ksession.startProcess("Simple");
        assertNotNull(processInstance2);
    }

    @Test
    public void testConditionExpression() throws Exception {
        // JBPM-4069 : XmlBPMNProcessDumper.dump() misses conditionExpression in sequenceFlow
        String filename = "JBPM-4069_Gateway.bpmn2";
        String original = BPMN2XMLTest.slurp(this.getClass().getResourceAsStream("/" + filename));
        
        KnowledgeBase kbase = createKnowledgeBase(filename);
        WorkflowProcess process = (WorkflowProcess)kbase.getProcess("GatewayTest");
        String result = XmlBPMNProcessDumper.INSTANCE.dump(process, XmlBPMNProcessDumper.META_DATA_USING_DI);
        
        // Compare original with result using XMLUnit
        Diff diff = new Diff(original, result);
        
        diff.overrideDifferenceListener(new DifferenceListener() {
            
            public int differenceFound(Difference diff) {
                String nodeName = diff.getTestNodeDetail().getNode().getNodeName();
                
                if (nodeName.equals("conditionExpression") || nodeName.equals("language")) {
                    logger.info(diff.toString());
                    return RETURN_ACCEPT_DIFFERENCE;
                }
                
                return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
            }

            @Override
            public void skippedComparison(Node one, Node two) { 
                logger.info("{} : {}", one.getLocalName(), two.getLocalName()) ;
            }
            
        });
        
        assertTrue("Original and generated output is not the same.", diff.identical());
   }
}
