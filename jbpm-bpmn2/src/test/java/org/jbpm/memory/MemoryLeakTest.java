package org.jbpm.memory;

import static org.drools.persistence.util.PersistenceUtil.*;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.RuleBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.common.AbstractRuleBase;
import org.drools.event.RuleBaseEventSupport;
import org.drools.event.process.DefaultProcessEventListener;
import org.drools.event.process.ProcessStartedEvent;
import org.drools.impl.KnowledgeBaseImpl;
import org.drools.io.ResourceFactory;
import org.drools.persistence.SingleSessionCommandService;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.persistence.jpa.processinstance.JPAWorkItemManagerFactory;
import org.drools.runtime.Environment;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.bpmn2.JbpmBpmn2TestCase.TestWorkItemHandler;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryLeakTest {

    private static final Logger logger = LoggerFactory.getLogger(MemoryLeakTest.class);

    private static HashMap<String, Object> testContext;
    private Environment env = null;

    private static final String PROCESS_NAME = "RuleTaskWithProcessInstance";
    
    @BeforeClass
    public static void beforeClass() {
        testContext = setupWithPoolingDataSource(JBPM_PERSISTENCE_UNIT_NAME);
    }

    @AfterClass
    public static void afterClass() {
        cleanUp(testContext);
    }

    @Before
    public void before() {
        env = createEnvironment(testContext);
    }

    @Test
    public void findEventSupportRegisteredInstancesTest() {
        // setup
        KnowledgeBase kbase = createKnowledgeBase();
        
        for( int i = 0; i < 3; ++i ) { 
            createKnowledgeSessionStartProcessEtc(kbase);
        }
        
        RuleBase ruleBase = ((KnowledgeBaseImpl) kbase).getRuleBase();
        RuleBaseEventSupport eventSupport = (RuleBaseEventSupport) getValueOfField("eventSupport", AbstractRuleBase.class, ruleBase);
        assertEquals( "Event listeners should have been detached", 0, eventSupport.getEventListeners().size());
    }
    
    private KnowledgeBase createKnowledgeBase() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("memory/BPMN2-RuleTaskWithInsertProcessInstance.bpmn2"), ResourceType.BPMN2);
        kbuilder.add(ResourceFactory.newClassPathResource("memory/ProcessInstanceRule.drl"), ResourceType.DRL);
        
        if (!kbuilder.getErrors().isEmpty()) {
            Iterator<KnowledgeBuilderError> errIter = kbuilder.getErrors().iterator();
            while( errIter.hasNext() ) { 
                KnowledgeBuilderError err = errIter.next();
                StringBuilder lines = new StringBuilder("");
                if( err.getLines().length > 0 ) { 
                    lines.append(err.getLines()[0]);
                    for( int i = 1; i < err.getLines().length; ++i ) { 
                        lines.append(", " + err.getLines()[i]);
                    }
                }
                logger.warn( err.getMessage() + " (" + lines.toString() + ")" );
            }
            throw new IllegalArgumentException("Errors while parsing knowledge base");
        }
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        
        return kbase;
    }

    private void createKnowledgeSessionStartProcessEtc(KnowledgeBase kbase) { 
        logger.info("session count=" + kbase.getStatefulKnowledgeSessions().size());
        
        StatefulKnowledgeSession ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, getKnowledgeSessionConfiguration(), env);
        addEventListenersToSession(ksession);
        
        /**
         * The following log line caused the memory leak. 
         * The specific (reverse-ordered) stack trace is the following: 
         * 
         *   MemoryLeakTest.createKnowledgeSessionStartProcessEtc(KnowledgeBase) calls kbase.getKieSessions()
         *   ..
         *   KnowledgeBaseImpl.getStatefulKnowledgeSessions() line: 186  
         *   StatefulKnowledgeSessionImpl.<init>(ReteooWorkingMemory, KnowledgeBase) line: 121   
         *   ReteooStatefulSession(AbstractWorkingMemory).setKnowledgeRuntime(InternalKnowledgeRuntime) line: 1268   
         *   ReteooStatefulSession(AbstractWorkingMemory).createProcessRuntime() line: 342   
         *   ProcessRuntimeFactory.newProcessRuntime(AbstractWorkingMemory) line: 12 
         *   ProcessRuntimeFactoryServiceImpl.newProcessRuntime(AbstractWorkingMemory) line: 1   
         *   ProcessRuntimeFactoryServiceImpl.newProcessRuntime(AbstractWorkingMemory) line: 10  
         *   ProcessRuntimeImpl.<init>(AbstractWorkingMemory) line: 84   
         *   ProcessRuntimeImpl.initProcessEventListeners() line: 215 
         *   
         * And ProcessRuntimeImpl.initProcessEventListeners() is what adds a new listener
         * to AbstractRuleBase.eventSupport.listeners via this line (235): 
         *   kruntime.getKnowledgeBase().addEventListener(knowledgeBaseListener);
         * 
         * The StatefulKnowledgeSessionImpl instance created in this .getStatefulKnowledgeSessions() 
         * method is obviously never disposed, which means that the listener is never removed. 
         * The listener then contains a link to a field (signalManager) of the ProcessRuntimeImpl, 
         * which contains a link to the StatefulKnowledgeSessionImpl instance created here. etc.. 
         */
        logger.info("session count=" + kbase.getStatefulKnowledgeSessions().size());
        
        TestWorkItemHandler handler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);
       
        try { 
            // create process instance, insert into session and start process
            Map<String, Object> processParams = new HashMap<String, Object>();
            String [] fireballVarHolder = new String[1];
            processParams.put("fireball", fireballVarHolder);
            ProcessInstance processInstance = ksession.createProcessInstance(PROCESS_NAME, processParams);
            ksession.insert(processInstance);
            ksession.startProcessInstance(processInstance.getId());
    
            // after the log line has been added, the DefaultProcessEventListener registered
            //  in the addEventListenersToSession() method no longer works?!?
            ksession.fireAllRules();
            
            // test process variables
            String [] procVar = (String []) ((WorkflowProcessInstance) processInstance).getVariable("fireball");
            assertEquals( "Rule task did NOT fire or complete.", "boom!", procVar[0] );
    
            // complete task and process
            Map<String, Object> results = new HashMap<String, Object>();
            results.put( "chaerg", new SerializableResult("zhrini", 302l, "F", "A", "T"));
            ksession.getWorkItemManager().completeWorkItem(handler.getWorkItem().getId(), results);
            
            assertNull( ksession.getProcessInstance(processInstance.getId()));
        } finally {
            // This should clean up all listeners, but doesn't -> see docs above
            ksession.dispose();
        }
        
    }

    private KnowledgeSessionConfiguration getKnowledgeSessionConfiguration() {
        Properties ksessionProperties;
        ksessionProperties = new Properties();
        ksessionProperties.put("drools.commandService", SingleSessionCommandService.class.getName());
        ksessionProperties.put("drools.processInstanceManagerFactory",
                "org.jbpm.persistence.processinstance.JPAProcessInstanceManagerFactory");
        ksessionProperties.setProperty("drools.workItemManagerFactory", JPAWorkItemManagerFactory.class.getName());
        ksessionProperties
                .put("drools.processSignalManagerFactory", "org.jbpm.persistence.processinstance.JPASignalManagerFactory");
        return KnowledgeBaseFactory.newKnowledgeSessionConfiguration(ksessionProperties);
    }

    private void addEventListenersToSession(StatefulKnowledgeSession session) {
        session.addEventListener(new DefaultProcessEventListener() {
            @Override
            public void afterProcessStarted(ProcessStartedEvent event) {
                logger.info(">>> Firing All the Rules after process started! " + event);
                ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            }
        });

    }
    
    private Object getValueOfField(String fieldname, Class<?> sourceClass, Object source ) {
        String sourceClassName = sourceClass.getName();
    
        Field field = null;
        try {
            field = sourceClass.getDeclaredField(fieldname);
            field.setAccessible(true);
        } catch (SecurityException e) {
            fail("Unable to retrieve " + fieldname + " field from " + sourceClassName + ": " + e.getCause());
        } catch (NoSuchFieldException e) {
            fail("Unable to retrieve " + fieldname + " field from " + sourceClassName + ": " + e.getCause());
        }
    
        assertNotNull("." + fieldname + " field is null!?!", field);
        Object fieldValue = null;
        try {
            fieldValue = field.get(source);
        } catch (IllegalArgumentException e) {
            fail("Unable to retrieve value of " + fieldname + " from " + sourceClassName + ": " + e.getCause());
        } catch (IllegalAccessException e) {
            fail("Unable to retrieve value of " + fieldname + " from " + sourceClassName + ": " + e.getCause());
        }
        return fieldValue;
    }

}
