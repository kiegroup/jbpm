package org.jbpm.bpmn2.exception;

import static junit.framework.Assert.*;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.*;

import org.drools.SessionConfiguration;
import org.drools.command.impl.*;
import org.drools.exception.ExceptionHandlingInterceptor;
import org.drools.impl.EnvironmentFactory;
import org.jbpm.bpmn2.JbpmBpmn2TestCase.TestWorkItemHandler;
import org.jbpm.process.instance.event.DefaultSignalManagerFactory;
import org.jbpm.process.instance.impl.DefaultProcessInstanceManagerFactory;
import org.jbpm.process.instance.impl.demo.DoNothingWorkItemHandler;
import org.jbpm.workflow.exception.ExceptionConstants;
import org.jbpm.workflow.exception.ExceptionProcessEventListener;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.junit.Test;
import org.kie.KnowledgeBase;
import org.kie.builder.KnowledgeBuilder;
import org.kie.builder.KnowledgeBuilderFactory;
import org.kie.io.ResourceFactory;
import org.kie.io.ResourceType;
import org.kie.runtime.Environment;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.process.*;
import org.kie.runtime.process.EventListener;

public class ExceptionHandlingTest {

    protected final static String START_PROCESS_PROCESS = "BPMN2-ExceptionHandling-ScriptException.bpmn2";
    protected final static String SIGNAL_EVENT_PROCESS = "BPMN2-ExceptionHandling-ScriptExceptionSignalStart.bpmn2";
    protected final static String SIGNAL_EVENT_INSTANCE_PROCESS = "BPMN2-ExceptionHandling-ScriptExceptionSignalProcessInstance.bpmn2";
    protected final static String WORK_ITEM_PROCESS = "BPMN2-ExceptionHandling-WorkItemException.bpmn2";
    protected final static String PROCESS_ID = "ExceptionHandling";

    protected final static String exceptionErrorCode = "code";
    protected final static String eventSignalReference = "signalRef";
    
    protected final static String errorHandled = "Error handled";
    protected final static String taskCompleted = "Script task completed.";
    
    
    /**
     * HELPER METHODS
     */

    protected KnowledgeBase createKnowledgeBase(String processFile) throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource(processFile), ResourceType.BPMN2);
        return kbuilder.newKnowledgeBase();
    }

    protected StatefulKnowledgeSession createKnowledgeSession(KnowledgeBase kbase) {
        Properties defaultProps = new Properties();
        defaultProps.setProperty("drools.processSignalManagerFactory", DefaultSignalManagerFactory.class.getName());
        defaultProps.setProperty("drools.processInstanceManagerFactory", DefaultProcessInstanceManagerFactory.class.getName());
        SessionConfiguration sessionConfig = new SessionConfiguration(defaultProps);

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession(sessionConfig, EnvironmentFactory.newEnvironment());

        return ksession;
    }

    protected StatefulKnowledgeSession createExceptionHandlingKnowledgeSession(KnowledgeBase kbase) {
        Properties defaultProps = new Properties();
        defaultProps.setProperty("drools.processSignalManagerFactory", DefaultSignalManagerFactory.class.getName());
        defaultProps.setProperty("drools.processInstanceManagerFactory", DefaultProcessInstanceManagerFactory.class.getName());
        SessionConfiguration sessionConfig = new SessionConfiguration(defaultProps);
        
        // configure exception catching
        Environment env = EnvironmentFactory.newEnvironment();
        env.set(ExceptionConstants.EXCEPTION_ERROR_CODE, exceptionErrorCode);
        
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession(sessionConfig, env);
        ksession.addEventListener(new ExceptionProcessEventListener());
        
        DefaultCommandService commandService = new DefaultCommandService(new FixedKnowledgeCommandContext( null, null, null, ksession, null ));
        CommandBasedStatefulKnowledgeSession commandBasedSession = new CommandBasedStatefulKnowledgeSession(commandService);
        commandBasedSession.addInterceptor(new ExceptionHandlingInterceptor());
        
        return commandBasedSession;
    }
    
    protected StatefulKnowledgeSession restoreSession(StatefulKnowledgeSession ksession) { 
        return ksession;
    }
    
    protected void verifyExceptionWasHandledOnce(ProcessInstance processInstance, TestOutStream testOut) { 
        if( processInstance != null ) { 
            assertNotNull("ProcessInstance is null.", processInstance );
            assertTrue("state: " + processInstance.getState(), processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        }
        assertTrue( "Test output should only contain one line.", testOut.getOutput().size() == 1 );
        assertTrue( "Test output should contain: '" + errorHandled + "'.", testOut.getOutput().get(0).startsWith(errorHandled));
    }

    protected void verifySecondLineOfTestOutput(String output2ndLine, TestOutStream testOut) { 
        assertTrue( "Test output should contain two lines.", testOut.getOutput().size() == 2 );
        assertTrue( "Test output, 2nd line,  should be: '" + errorHandled + "'.", testOut.getOutput().get(1).startsWith(output2ndLine));
    }

    protected void verifyProcessInstanceHasCompleted(Object processInstanceObject) { 
        ProcessInstance processInstance = (ProcessInstance) processInstanceObject; 
        assertTrue("state: " + processInstance.getState(), processInstance.getState() == ProcessInstance.STATE_COMPLETED);
    }

    private static class TestOutStream extends PrintStream {
    
        private List<String> output = new ArrayList<String>();
        
        public TestOutStream(OutputStream out) {
            super(out);
        } 
        
        @Override
        public void println(String x) {
            output.add(x);
        }
        
        public List<String> getOutput() { 
            return output;
        }
    
    }


    /**
     * TESTS
     */

    @Test
    public void testSignalEventExceptionHandlingWithRetry() throws Exception {
        KnowledgeBase kbase = createKnowledgeBase(SIGNAL_EVENT_PROCESS);
        StatefulKnowledgeSession ksession = createExceptionHandlingKnowledgeSession(kbase);
        
        TestOutStream testOut = new TestOutStream(System.out);
        System.setOut(testOut);

        // ORIGINAL START
        ksession.signalEvent("StartSignal", null);
        
        verifyExceptionWasHandledOnce(null, testOut);

        // RETRY
        long processInstanceId =  Long.valueOf(testOut.getOutput().get(0).substring(15));
        ksession.signalEvent(ExceptionConstants.RETRY_EVENT_TYPE, processInstanceId, processInstanceId );
        
        verifySecondLineOfTestOutput(errorHandled, testOut);
        
        ProcessInstance processInstance = ksession.getProcessInstance(processInstanceId);
        assertTrue("State is not active, but " + processInstance.getState(), processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        
        // Test that only 1 Retry listener is registered (and the first one has indeed removed itself).
        WorkflowProcessInstanceImpl processInstanceImpl = (WorkflowProcessInstanceImpl) processInstance;
        Field eventListenersField = WorkflowProcessInstanceImpl.class.getDeclaredField("eventListeners");
        eventListenersField.setAccessible(true);
        Map<String, List<EventListener>> eventListeners = (Map<String, List<EventListener>>) eventListenersField.get(processInstanceImpl);
        assertNotNull( "The WorkflowProcessInstanceImpl#eventListeners field is null.", eventListeners);
        List<EventListener> retryEventListeners = eventListeners.get(ExceptionConstants.RETRY_EVENT_TYPE);
        assertTrue( "No event listeners registered for ExceptionConstants.RETRY_EVENT_TYPE.", 
                retryEventListeners != null && retryEventListeners.size() > 0 );
        assertTrue( retryEventListeners.size() + " retry event listeners registered instead of just 1.", 
                retryEventListeners.size() == 1 );
    }
    
    @Test
    public void testSignalEventProcessInstanceExceptionHandling() throws Exception {
        KnowledgeBase kbase = createKnowledgeBase(SIGNAL_EVENT_INSTANCE_PROCESS);
        StatefulKnowledgeSession ksession = createExceptionHandlingKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new DoNothingWorkItemHandler());
        
        TestOutStream testOut = new TestOutStream(System.out);
        System.setOut(testOut);

        ProcessInstance processInstance = ksession.startProcess(PROCESS_ID);
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        assertTrue( "Test output should be empty.", testOut.getOutput().size() == 0 );
        
        // for when persistence is used
        ksession = restoreSession(ksession);
        
        // now signal process instance
        ksession.signalEvent(eventSignalReference, null, processInstance.getId());
        
        verifyExceptionWasHandledOnce(processInstance, testOut);
        
        long processInstanceId =  Long.valueOf(testOut.getOutput().get(0).substring(15));
        ksession.signalEvent(ExceptionConstants.RETRY_EVENT_TYPE, processInstanceId, processInstanceId );
        
        verifySecondLineOfTestOutput(errorHandled, testOut);
        
        assertTrue("State is not active, but " + processInstance.getState(), processInstance.getState() == ProcessInstance.STATE_ACTIVE);
    }
    
    @Test
    public void testStartProcessExceptionHandling() throws Exception {
        KnowledgeBase kbase = createKnowledgeBase(START_PROCESS_PROCESS);
        StatefulKnowledgeSession ksession = createExceptionHandlingKnowledgeSession(kbase);

        TestOutStream testOut = new TestOutStream(System.out);
        System.setOut(testOut);
        
        ProcessInstance processInstance = ksession.startProcess(PROCESS_ID);
        
        verifyExceptionWasHandledOnce(processInstance, testOut);
        
        ((WorkflowProcessInstance) processInstance).setVariable("x", new Object());
        ksession.signalEvent(ExceptionConstants.RETRY_EVENT_TYPE, processInstance.getId(), processInstance.getId());
        
        verifySecondLineOfTestOutput(taskCompleted, testOut);
        
        assertTrue("State should be completed, not: " + processInstance.getState(), 
                processInstance.getState() == ProcessInstance.STATE_COMPLETED);
        assertNull("ProcessInstance should no longer be present in session.", 
                ksession.getProcessInstance(processInstance.getId()));
    }

    @Test
    public void testStartProcessInstanceExceptionHandling() throws Exception {
        KnowledgeBase kbase = createKnowledgeBase(START_PROCESS_PROCESS);
        StatefulKnowledgeSession ksession = createExceptionHandlingKnowledgeSession(kbase);
        
        TestOutStream testOut = new TestOutStream(System.out);
        System.setOut(testOut);
        
        ProcessInstance processInstance = ksession.createProcessInstance(PROCESS_ID, null);
        processInstance = ksession.startProcessInstance(processInstance.getId());

        verifyExceptionWasHandledOnce(processInstance, testOut);
        
        assertTrue("state: " + processInstance.getState(), processInstance.getState() == ProcessInstance.STATE_ACTIVE);
    }

    @Test
    public void testCompleteWorkItemExceptionHandling() throws Exception {
        KnowledgeBase kbase = createKnowledgeBase(WORK_ITEM_PROCESS);
        StatefulKnowledgeSession ksession = createExceptionHandlingKnowledgeSession(kbase);
        
        TestOutStream testOut = new TestOutStream(System.out);
        System.setOut(testOut);
        
        // Setup process
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);

        Map<String, Object> params = new HashMap<String, Object>();
        List<String> list = new ArrayList<String>(); // empty list
        params.put("list", list);
        ProcessInstance processInstance = ksession.startProcess(PROCESS_ID, params);

        for (WorkItem wi : workItemHandler.getWorkItems()) {
            ksession.getWorkItemManager().completeWorkItem(wi.getId(), null);
        }

        // Test handling
        verifyExceptionWasHandledOnce(processInstance, testOut);
        
        // This should do NOTHING! (The HumanTaskNodeInstance failed, remember?)
        for (WorkItem wi : workItemHandler.getWorkItems()) {
            ksession.getWorkItemManager().completeWorkItem(wi.getId(), null);
        }
        verifyExceptionWasHandledOnce(processInstance, testOut);
        
        // Fix exception
        list.add("value");
        
        // Retry
        ksession.signalEvent(ExceptionConstants.RETRY_EVENT_TYPE, processInstance.getId(), processInstance.getId());

        verifyExceptionWasHandledOnce(processInstance, testOut);

        // Complete workItem now that HumanTaskNodeInstance has succeeded!
        for (WorkItem wi : workItemHandler.getWorkItems()) {
            ksession.getWorkItemManager().completeWorkItem(wi.getId(), null);
        }
        verifySecondLineOfTestOutput(taskCompleted, testOut);

        verifyProcessInstanceHasCompleted(processInstance);
    }

    @Test
    public void testAbortWorkItemExceptionHandling() throws Exception {
        KnowledgeBase kbase = createKnowledgeBase(WORK_ITEM_PROCESS);
        StatefulKnowledgeSession ksession = createExceptionHandlingKnowledgeSession(kbase);
        
        TestOutStream testOut = new TestOutStream(System.out);
        System.setOut(testOut);
        
        // Setup process
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);

        Map<String, Object> params = new HashMap<String, Object>();
        List<String> list = new ArrayList<String>(); // empty list
        params.put("list", list);
        ProcessInstance processInstance = ksession.startProcess(PROCESS_ID, params);

        for (WorkItem wi : workItemHandler.getWorkItems()) {
            ksession.getWorkItemManager().abortWorkItem(wi.getId());
        }

        verifyExceptionWasHandledOnce(processInstance, testOut);
        
        // This should do NOTHING! (The HumanTaskNodeInstance failed, remember?)
        for (WorkItem wi : workItemHandler.getWorkItems()) {
            ksession.getWorkItemManager().completeWorkItem(wi.getId(), null);
        }
        verifyExceptionWasHandledOnce(processInstance, testOut);
        
        // Fix exception
        list.add("value");
        
        // Retry
        ksession.signalEvent(ExceptionConstants.RETRY_EVENT_TYPE, processInstance.getId(), processInstance.getId());
       
        verifyExceptionWasHandledOnce(processInstance, testOut);

        // Complete workItem now that HumanTaskNodeInstance has succeeded!
        for (WorkItem wi : workItemHandler.getWorkItems()) {
            ksession.getWorkItemManager().abortWorkItem(wi.getId());
        }
        
        verifySecondLineOfTestOutput(taskCompleted, testOut);

        verifyProcessInstanceHasCompleted(processInstance);
    }
}
