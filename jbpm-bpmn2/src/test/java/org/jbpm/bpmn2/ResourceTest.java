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

import java.util.Properties;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.KieBase;
import org.kie.KnowledgeBaseFactory;
import org.kie.runtime.KieSessionConfiguration;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.process.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceTest extends JbpmTestCase {

    private static final String resourceFolder = "activity/";

    private StatefulKnowledgeSession ksession;

    private Logger logger = LoggerFactory.getLogger(ResourceTest.class);

    public ResourceTest() {

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
    public void testResourceType() throws Exception {
        KieBase kbase = createKnowledgeBase(resourceFolder + "MinimalProcess.bpmn2");
        Properties properties = new Properties();
        properties.put("drools.processInstanceManagerFactory", "org.jbpm.process.instance.impl.DefaultProcessInstanceManagerFactory");
        properties.put("drools.processSignalManagerFactory", "org.jbpm.process.instance.event.DefaultSignalManagerFactory");
        KieSessionConfiguration config = KnowledgeBaseFactory.newKnowledgeSessionConfiguration(properties);
        ksession = createKnowledgeSession(kbase, config, null);
        ksession.startProcess("Minimal");
    }

    @Test
    public void testMultipleProcessInOneFile() throws Exception {
        KieBase kbase = createKnowledgeBase("manual/MultipleProcessInOneFile.bpmn2");
        Properties properties = new Properties();
        properties.put("drools.processInstanceManagerFactory", "org.jbpm.process.instance.impl.DefaultProcessInstanceManagerFactory");
        properties.put("drools.processSignalManagerFactory", "org.jbpm.process.instance.event.DefaultSignalManagerFactory");
        KieSessionConfiguration config = KnowledgeBaseFactory.newKnowledgeSessionConfiguration(properties);
        ksession = createKnowledgeSession(kbase, config, null);
        ProcessInstance processInstance = ksession.startProcess("EvaluationProcess");
        assertNotNull(processInstance);
        ProcessInstance processInstance2 = ksession.startProcess("SimpleProcess");
        assertNotNull(processInstance2);
    }

}
