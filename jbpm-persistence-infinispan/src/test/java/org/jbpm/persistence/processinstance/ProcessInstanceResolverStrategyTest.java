package org.jbpm.persistence.processinstance;

import static org.jbpm.persistence.util.PersistenceUtil.JBPM_PERSISTENCE_UNIT_NAME;
import static org.jbpm.persistence.util.PersistenceUtil.cleanUp;
import static org.jbpm.persistence.util.PersistenceUtil.setupWithPoolingDataSource;
import static org.kie.api.runtime.EnvironmentName.ENTITY_MANAGER_FACTORY;

import java.util.HashMap;
import java.util.Map;

import javax.transaction.UserTransaction;

import org.drools.core.io.impl.ClassPathResource;
import org.drools.core.marshalling.impl.ClassObjectMarshallingStrategyAcceptor;
import org.drools.core.marshalling.impl.SerializablePlaceholderResolverStrategy;
import org.drools.persistence.infinispan.marshaller.InfinispanPlaceholderResolverStrategy;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.jbpm.marshalling.impl.ProcessInstanceResolverStrategy;
import org.jbpm.persistence.processinstance.objects.NonSerializableClass;
import org.jbpm.persistence.util.PersistenceUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.api.io.ResourceType;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.internal.persistence.infinispan.InfinispanKnowledgeService;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessInstanceResolverStrategyTest {

    private static Logger logger = LoggerFactory.getLogger(ProcessInstanceResolverStrategyTest.class);
    
    private HashMap<String, Object> context;
    private StatefulKnowledgeSession ksession;

    private static final String RF_FILE = "SimpleProcess.rf";
    private final static String PROCESS_ID = "org.jbpm.persistence.TestProcess";
    private final static String VAR_NAME = "persistVar";
   
    @Before
    public void before() { 
        context = setupWithPoolingDataSource(JBPM_PERSISTENCE_UNIT_NAME, false);
        
        // load up the knowledge base
        Environment env = PersistenceUtil.createEnvironment(context);
        env.set(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES, new ObjectMarshallingStrategy[] {
                new ProcessInstanceResolverStrategy(),
                new InfinispanPlaceholderResolverStrategy(env),
                new SerializablePlaceholderResolverStrategy(ClassObjectMarshallingStrategyAcceptor.DEFAULT) }
                );
        KnowledgeBase kbase = loadKnowledgeBase();

        // create session
        ksession = InfinispanKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);
        Assert.assertTrue("Valid KnowledgeSession could not be created.", ksession != null && ksession.getId() > 0);
    }
    
    private KnowledgeBase loadKnowledgeBase() { 
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add( new ClassPathResource( RF_FILE ), ResourceType.DRF );
        KnowledgeBase kbase = kbuilder.newKnowledgeBase();
        return kbase;
    }
    
    
    @After
    public void after() {
        if( ksession != null ) { 
            ksession.dispose();
        }
        cleanUp(context);
    }

    @Test
    public void testWithDatabaseAndStartProcess() throws Exception {
        // Create variable
        Map<String, Object> params = new HashMap<String, Object>();
        NonSerializableClass processVar = new NonSerializableClass();
        processVar.setString("1234567890");
        params.put(VAR_NAME, processVar);
        params.put("logger", logger);

        // Persist variable
        DefaultCacheManager cm = (DefaultCacheManager) context.get(ENTITY_MANAGER_FACTORY);
        Cache<String, Object> cache = cm.getCache("jbpm-configured-cache");
        UserTransaction ut = (UserTransaction) cache.getAdvancedCache().getTransactionManager();
        ut.begin();
        processVar.setId("nonSerializable1");
        cache.put("nonSerializable1", processVar);
        ut.commit();

        // Generate, insert, and start process
        ProcessInstance processInstance = ksession.startProcess(PROCESS_ID, params);

        // Test resuls
        Assert.assertEquals(ProcessInstance.STATE_ACTIVE, processInstance.getState());
        processVar = (NonSerializableClass) ((WorkflowProcessInstance) processInstance).getVariable(VAR_NAME);
        Assert.assertNotNull(processVar);
    }

    @Test
    public void testWithDatabaseAndStartProcessInstance() throws Exception {
        // Create variable
        Map<String, Object> params = new HashMap<String, Object>();
        NonSerializableClass processVar = new NonSerializableClass();
        processVar.setString("1234567890");
        params.put(VAR_NAME, processVar);
    
        // Persist variable
        DefaultCacheManager cm = (DefaultCacheManager) context.get(ENTITY_MANAGER_FACTORY);
        Cache<String, Object> cache = cm.getCache("jbpm-configured-cache");
        UserTransaction ut = (UserTransaction) cache.getAdvancedCache().getTransactionManager();
        ut.begin();
        processVar.setId("nonSerializable2");
        cache.put("nonSerializable2", processVar);
        ut.commit();
    
        // Create process,
        ProcessInstance processInstance = ksession.createProcessInstance(PROCESS_ID, params);
        long processInstanceId = processInstance.getId();
        Assert.assertTrue(processInstanceId > 0);
        Assert.assertEquals(ProcessInstance.STATE_PENDING, processInstance.getState());

        // insert process,
        ut.begin();
        ksession.insert(processInstance);
   
        // and start process
        ksession.startProcessInstance(processInstanceId);
        ksession.fireAllRules();
        ut.commit();
    
        // Test results
        processInstance = ksession.getProcessInstance(processInstanceId);
        Assert.assertEquals(ProcessInstance.STATE_ACTIVE, processInstance.getState());
        processVar = (NonSerializableClass) ((WorkflowProcessInstance) processInstance).getVariable(VAR_NAME);
        Assert.assertNotNull(processVar);
    }

}
