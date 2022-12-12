/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.event.emitters.elasticsearch;

import static org.jbpm.test.persistence.util.PersistenceUtil.JBPM_PERSISTENCE_UNIT_NAME;
import static org.jbpm.test.persistence.util.PersistenceUtil.cleanUp;
import static org.jbpm.test.persistence.util.PersistenceUtil.createEnvironment;
import static org.jbpm.test.persistence.util.PersistenceUtil.setupWithPoolingDataSource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.drools.core.io.impl.ClassPathResource;
import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.persistence.jpa.JPAKnowledgeService;
import org.kie.internal.runtime.StatefulKnowledgeSession;

public class ElasticsearchEventEmitterProcessTest {

    private static Server server;

    private static List<String> responseCollector = new ArrayList<>();

    private HashMap<String, Object> context;
    private Environment env;

    @BeforeClass
    public static void initialize() throws Exception {

        FakeElasticSearchRESTApplication application = new FakeElasticSearchRESTApplication(responseCollector);
        RuntimeDelegate delegate = RuntimeDelegate.getInstance();

        JAXRSServerFactoryBean bean = delegate.createEndpoint(application, JAXRSServerFactoryBean.class);
        String url = "http://localhost:9998" + bean.getAddress();
        bean.setAddress(url);
        server = bean.create();
        server.start();

        System.setProperty("org.jbpm.event.emitters.elasticsearch.url", url);
    }

    @AfterClass
    public static void destroy() throws Exception {
        if (server != null) {
            server.stop();
            server.destroy();
        }

        System.clearProperty("org.jbpm.event.emitters.elasticsearch.url");
    }

    @Before
    public void setUp() throws Exception {

        responseCollector.clear();
        context = setupWithPoolingDataSource(JBPM_PERSISTENCE_UNIT_NAME);
        env = createEnvironment(context);
    }

    @After
    public void tearDown() throws Exception {
        cleanUp(context);
    }

    @Test(timeout=10000)
    public void testIntegrationWithEventManager() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(new ClassPathResource("WorkItemsProcess.rf"), ResourceType.DRF);
        InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addPackages(kbuilder.getKnowledgePackages());

        StatefulKnowledgeSession ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);
        ksession.getWorkItemManager().registerWorkItemHandler("MyWork", new SystemOutWorkItemHandler());
        ProcessInstance processInstance = ksession.startProcess("org.drools.test.TestProcess");
        ksession.insert("TestString");
        assertEquals(ProcessInstance.STATE_COMPLETED, processInstance.getState());

        while (responseCollector.isEmpty()) {
            Thread.sleep(100);
        }

        assertNotNull(responseCollector);
        assertEquals(1, responseCollector.size());

        String response = responseCollector.get(0);
        // check process instance creation event
        assertTrue("Process index event missing", response.contains("\"index\""));
        // check process instance end event
        assertTrue("Process state 2 event missing", response.contains("\"state\":2"));
    }

    @Test(timeout=10000)
    public void testIntegrationWithEventManagerSubProcess() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(new ClassPathResource("parent.bpmn"), ResourceType.BPMN2);
        kbuilder.add(new ClassPathResource("child.bpmn"), ResourceType.BPMN2);
        InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addPackages(kbuilder.getKnowledgePackages());

        StatefulKnowledgeSession ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);
        ProcessInstance processInstance = ksession.startProcess("parent");
        assertEquals(ProcessInstance.STATE_COMPLETED, processInstance.getState());

        while (responseCollector.isEmpty()) {
            Thread.sleep(100);
        }

        assertNotNull(responseCollector);
        assertEquals(1, responseCollector.size());

        String response = responseCollector.get(0);
        // check process instance creation event
        assertTrue("Process index event missing", response.contains("\"index\""));
        // check process instance end event
        assertTrue("Process state 2 event missing", response.contains("\"state\":2"));
        // check both parent and child process events have been sent
        assertTrue("Process event for child process missing", response.contains("\"processId\":\"child\""));
        assertTrue("Process event for parent process missing", response.contains("\"processId\":\"parent\""));
    }
}
