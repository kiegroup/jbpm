/*
 * Copyright 2012 JBoss Inc
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
package org.jbpm.bpmn2.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.List;

import javax.naming.InitialContext;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.UserTransaction;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.impl.EnvironmentFactory;
import org.drools.io.ResourceFactory;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.process.audit.JPAProcessInstanceDbLog;
import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;

public class BrokenScriptPersistenceTest {
    
    private PoolingDataSource ds = new PoolingDataSource();
    private StatefulKnowledgeSession ksession;
    private EntityManagerFactory emf;
    
    @Before
    public void setUp() throws Exception {
        ds.setUniqueName("jdbc/testDS1");
        ds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
        ds.setMaxPoolSize(3);
        ds.setAllowLocalTransactions(true);
        ds.getDriverProperties().put("user", "sa");
        ds.getDriverProperties().put("password", "");
        ds.getDriverProperties().put("url", "jdbc:h2:tcp://localhost/runtime/jbpm");
        ds.getDriverProperties().put("driverClassName", "org.h2.Driver");
        ds.init();
        JPAProcessInstanceDbLog.clear();
        KnowledgeBase kbase = readKnowledgeBase("brokenscript.bpmn2");
        Environment env = EnvironmentFactory.newEnvironment();
        emf = Persistence
                .createEntityManagerFactory("org.jbpm.persistence.jpa");
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
        env.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());

        ksession = JPAKnowledgeService
                .newStatefulKnowledgeSession(kbase, null, env);
        new JPAWorkingMemoryDbLogger(ksession);
    }
    
    @After
    public void tearDown() {
        // clean up db for another test run
        try {
            UserTransaction ut = InitialContext.doLookup("java:comp/UserTransaction");
            ut.begin();
            emf.createEntityManager().createNativeQuery("delete from processinstanceinfo").executeUpdate();
            ut.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ksession.dispose();
        emf.close();
        JPAProcessInstanceDbLog.clear();
        ds.close();
    }

    @Test
    public void testBrokenScript() throws Exception {

        
        try {
            ksession.startProcess("defaultPackage.brokenscript");
        
        } catch (Exception e) {
//            e.printStackTrace();
        }

        List<ProcessInstanceLog> log = JPAProcessInstanceDbLog.findProcessInstances("defaultPackage.brokenscript");
        assertNotNull(log);
        try {
            ksession.getProcessInstance(log.get(0).getProcessInstanceId());
            fail("should be null pointer on accessing byte array");
        } catch (Exception e) {
            // should be null pointer on accessing byte array
        }
        assertEquals(0, log.size());

    }
    
    private KnowledgeBase readKnowledgeBase(String process) throws Exception {

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource(process), ResourceType.BPMN2);

        return kbuilder.newKnowledgeBase();
    }
    
}