/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.persistence.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.drools.persistence.mapdb.MapDBEnvironmentName;
import org.jbpm.persistence.PersistentProcessInstance;
import org.jbpm.persistence.mapdb.MapDBProcessInstance;
import org.jbpm.persistence.mapdb.PersistentProcessInstanceSerializer;
import org.jbpm.persistence.mapdb.ProcessInstanceKeySerializer;
import org.jbpm.persistence.mapdb.ProcessKey;
import org.jbpm.persistence.mapdb.util.MapDBProcessPersistenceUtil;
import org.jbpm.test.util.AbstractBaseTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSession;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.mapdb.BTreeMap;
import org.mapdb.DB;

/**
 * This test looks at the behavior of the  {@link JPAProcessInstanceManager} 
 * with regards to created (but not started) process instances 
 * and whether the process instances are available or not after creation.
 */
public class GetProcessInstancesTest extends AbstractBaseTest {
    
    private HashMap<String, Object> context;
    
    private Environment env;
    private KieBase kbase;
    private long sessionId;
    
    @Before
    public void setUp() throws Exception {
        context = MapDBProcessPersistenceUtil.setupMapDB();
        env = MapDBProcessPersistenceUtil.createEnvironment(context);

        kbase = createBase();
        KieSession ksession = KieServices.Factory.get().getStoreServices().
        		newKieSession(kbase, null, env);
        sessionId = ksession.getIdentifier();
        ksession.dispose();
    }

    @After
    public void tearDown() throws Exception {
    	MapDBProcessPersistenceUtil.cleanUp(context);
    }

    @Test
    public void getEmptyProcessInstances() throws Exception {
        KieSession ksession = reloadKnowledgeSession();
        assertEquals(0, ksession.getProcessInstances().size());
        ksession.dispose();
    }

    @Test
    public void create2ProcessInstances() throws Exception {
        long[] processId = new long[2];

        KieSession ksession = reloadKnowledgeSession();
        ksession.getWorkItemManager().registerWorkItemHandler("MyWork", TestWorkItemHandler.getInstance());
        processId[0] = ksession.createProcessInstance("org.jbpm.processinstance.helloworld", null).getId();
        processId[1] = ksession.createProcessInstance("org.jbpm.processinstance.helloworld", null).getId();
        ksession.dispose();

        assertProcessInstancesExist(processId);
    }

    @Test
    public void create2ProcessInstancesInsideTransaction() throws Exception {
        long[] processId = new long[2];

        UserTransaction ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
        ut.begin();
        
        KieSession ksession = reloadKnowledgeSession();
        processId[0] = ksession.createProcessInstance("org.jbpm.processinstance.helloworld", null).getId();
        processId[1] = ksession.createProcessInstance("org.jbpm.processinstance.helloworld", null).getId();
        assertEquals(2, ksession.getProcessInstances().size());
        
        // process instance manager cache flushed on tx
        ut.commit();
        assertEquals(0, ksession.getProcessInstances().size());

        ksession = reloadKnowledgeSession(ksession);
        assertEquals(0, ksession.getProcessInstances().size());
        ksession.dispose();
        
        assertProcessInstancesExist(processId);
    }

    @Test
    public void noProcessInstancesLeftAfterRollback() throws Exception {
        long[] notProcess = new long[2];

        UserTransaction ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
        ut.begin();
        
        KieSession ksession = reloadKnowledgeSession();
        notProcess[0] = ksession.createProcessInstance("org.jbpm.processinstance.helloworld", null).getId();
        notProcess[1] = ksession.createProcessInstance("org.jbpm.processinstance.helloworld", null).getId();
        assertEquals(2, ksession.getProcessInstances().size());
        
        ut.rollback();
        // Validate that proc inst mgr cache is also flushed on rollback
        assertEquals(0, ksession.getProcessInstances().size());

        ksession = reloadKnowledgeSession(ksession);
        assertEquals(0, ksession.getProcessInstances().size());
        ksession.dispose();
        
        assertProcessInstancesNotExist(notProcess);
    }

    @Test
    public void noProcessInstancesLeftWithPreTxKSessionAndRollback() throws Exception {
        long[] notProcess = new long[4];

        KieSession ksession = reloadKnowledgeSession();
        
        UserTransaction ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
        ut.begin();
        
        notProcess[0] = ksession.createProcessInstance("org.jbpm.processinstance.helloworld", null).getId();
        notProcess[1] = ksession.createProcessInstance("org.jbpm.processinstance.helloworld", null).getId();
        
        ut.rollback();
        // Validate that proc inst mgr cache is also flushed on rollback
        assertEquals(0, ksession.getProcessInstances().size());

        ksession = reloadKnowledgeSession(ksession);
        assertEquals(0, ksession.getProcessInstances().size());
        ksession.dispose();
        
        assertProcessInstancesNotExist(notProcess);
    }

   
    /**
     * Helper functions
     */
    
    private void assertProcessInstancesExist(long[] processId) {
        KieSession ksession = reloadKnowledgeSession();

        for (long id : processId) {
            assertNotNull("Process instance " + id + " should not exist!", ksession.getProcessInstance(id));
        }
    }

    private void assertProcessInstancesNotExist(long[] processId) {
        KieSession ksession = reloadKnowledgeSession();

        for (long id : processId) {
            assertNull(ksession.getProcessInstance(id));
        }
    }

    private KieBase createBase() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("processinstance/HelloWorld.rf"), ResourceType.DRF);
        assertFalse(kbuilder.getErrors().toString(), kbuilder.hasErrors());

        return kbuilder.newKnowledgeBase();
    }
    
    private KieSession reloadKnowledgeSession() {
        return KieServices.Factory.get().getStoreServices().loadKieSession(sessionId, kbase, null, env);
    }

    private KieSession reloadKnowledgeSession(KieSession ksession) {
        ksession.dispose();
        return reloadKnowledgeSession();
    }
    
    @Test
    public void testMapDB() {
    	DB db = (DB) context.get(MapDBEnvironmentName.DB_OBJECT);
    	ProcessKey key = new ProcessKey(1L, new String[] { "eventA", "eventB" }, null);
    	MapDBProcessInstance value = new MapDBProcessInstance();
    	value.setId(1L);
    	value.setEventTypes(new HashSet<>(Arrays.asList("eventA", "eventB")));
    	value.setStartDate(new Date());
    	value.setLastModificationDate(new Date());
    	value.setProcessId("process_1");
    	value.setProcessInstanceByteArray(new byte[] { 1,2,3,4,5,6,7,8,9 });
    	BTreeMap<ProcessKey, PersistentProcessInstance> map = db.treeMap(
    			"processInstance", 
    			new ProcessInstanceKeySerializer(), 
    			new PersistentProcessInstanceSerializer()).
    			createOrOpen();
		map.put(key, value);
    	Assert.assertEquals(1, map.prefixSubMap(new ProcessKey(1L, (String[]) null, null)).size());
    	Assert.assertEquals(1, map.prefixSubMap(new ProcessKey(1L, new String[] { "eventA", "eventB" }, null)).size());
    	Assert.assertEquals(1, map.prefixSubMap(new ProcessKey(1L, new String[] { "eventA" }, null)).size());
    	Assert.assertEquals(1, map.prefixSubMap(new ProcessKey(1L, new String[] { "eventB" }, null)).size());
    	Assert.assertEquals(0, map.prefixSubMap(new ProcessKey(2L, (String[]) null, null)).size());
    	Assert.assertEquals(1, map.subMap(new ProcessKey(Long.MIN_VALUE, (String[]) null, null), new ProcessKey(Long.MAX_VALUE, (String[]) null, null)).size());
    	Assert.assertEquals(1, map.subMap(new ProcessKey(Long.MIN_VALUE, new String[] { "eventA", "eventB" }, null), new ProcessKey(Long.MAX_VALUE, new String[] { "eventA", "eventB" }, null)).size());
    }
}
