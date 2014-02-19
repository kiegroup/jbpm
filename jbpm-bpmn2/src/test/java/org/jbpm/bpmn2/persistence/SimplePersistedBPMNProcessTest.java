package org.jbpm.bpmn2.persistence;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.WorkingMemory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.command.Context;
import org.drools.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.command.impl.GenericCommand;
import org.drools.command.impl.KnowledgeCommandContext;
import org.drools.common.InternalKnowledgeRuntime;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.definition.process.Process;
import org.drools.event.ActivationCancelledEvent;
import org.drools.event.ActivationCreatedEvent;
import org.drools.event.AfterActivationFiredEvent;
import org.drools.event.AgendaGroupPoppedEvent;
import org.drools.event.AgendaGroupPushedEvent;
import org.drools.event.BeforeActivationFiredEvent;
import org.drools.event.RuleFlowGroupActivatedEvent;
import org.drools.event.RuleFlowGroupDeactivatedEvent;
import org.drools.event.rule.DebugAgendaEventListener;
import org.drools.impl.EnvironmentFactory;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.io.ResourceFactory;
import org.drools.persistence.SingleSessionCommandService;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkflowProcessInstance;
import org.jbpm.bpmn2.JbpmBpmn2TestCase;
import org.jbpm.bpmn2.SimpleBPMNProcessTest;
import org.jbpm.bpmn2.xml.BPMNDISemanticModule;
import org.jbpm.bpmn2.xml.BPMNSemanticModule;
import org.jbpm.bpmn2.xml.XmlBPMNProcessDumper;
import org.jbpm.compiler.xml.XmlProcessReader;
import org.jbpm.persistence.ProcessPersistenceContext;
import org.jbpm.persistence.ProcessPersistenceContextManager;
import org.jbpm.process.instance.impl.demo.DoNothingWorkItemHandler;
import org.jbpm.ruleflow.core.RuleFlowProcess;

public class SimplePersistedBPMNProcessTest extends JbpmBpmn2TestCase {

    public SimplePersistedBPMNProcessTest() {
        super(true);
    }
    
    
    public void testIntermediateCatchEventTimerCycleWithError() throws Exception {
        KnowledgeBase kbase = createKnowledgeBase("BPMN2-IntermediateCatchEventTimerCycleWithError.bpmn2");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new DoNothingWorkItemHandler());
        ProcessInstance processInstance = ksession
                .startProcess("IntermediateCatchEvent");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        // now wait for 1 second for timer to trigger
        Thread.sleep(1000);
        assertProcessInstanceActive(processInstance.getId(), ksession);

        final long piId = processInstance.getId();
        ksession.execute(new GenericCommand<Void>() {

            public Void execute(Context context) {
                StatefulKnowledgeSession ksession = ((KnowledgeCommandContext) context).getStatefulKnowledgesession();
                WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(piId);
                processInstance.setVariable("x", 0);
                return null;
            }
        });
        
        Thread.sleep(1000);
        assertProcessInstanceActive(processInstance.getId(), ksession);
        Thread.sleep(1000);
        assertProcessInstanceActive(processInstance.getId(), ksession);
        
        Integer xValue = ksession.execute(new GenericCommand<Integer>() {

            public Integer execute(Context context) {
                StatefulKnowledgeSession ksession = ((KnowledgeCommandContext) context).getStatefulKnowledgesession();
                WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(piId);
                return (Integer) processInstance.getVariable("x");
                
            }
        });
        assertEquals(new Integer(2), xValue);
        ksession.abortProcessInstance(processInstance.getId());
        assertProcessInstanceAborted(processInstance.getId(), ksession);
    }
    
    public void testCallActivityWithTimer() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
                .newKnowledgeBuilder();
        kbuilder.add(ResourceFactory
                .newClassPathResource("BPMN2-ParentProcess.bpmn2"), ResourceType.BPMN2);
        kbuilder.add(ResourceFactory
                .newClassPathResource("BPMN2-SubProcessWithTimer.bpmn2"),  ResourceType.BPMN2);
   
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        Environment env = EnvironmentFactory.newEnvironment();
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.jbpm.persistence.jpa");
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);

        StatefulKnowledgeSession ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        
        Map<String, Object> params = new HashMap<String, Object>();
        ProcessInstance processInstance = ksession.startProcess(
                "ParentProcess", params);
        
        ksession.getWorkItemManager().completeWorkItem(
                workItemHandler.getWorkItem().getId(), null);
        
        Map<String, Object> res = new HashMap<String, Object>();
        res.put("sleep", "2s");
        ksession.getWorkItemManager().completeWorkItem(
                workItemHandler.getWorkItem().getId(), res);
        
        int sessionId = ksession.getId();
        
        System.out.println("dispose");
        ksession.dispose();
        Thread.sleep(3000);
        
        ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(sessionId, kbase, null, env);
        Thread.sleep(3000);
        assertProcessInstanceCompleted(processInstance.getId(), ksession);

    }
    
    public void testProcesWithHumanTaskWithTimer() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
                .newKnowledgeBuilder();
        kbuilder.add(ResourceFactory
                .newClassPathResource("BPMN2-SubProcessWithTimer.bpmn2"),  ResourceType.BPMN2);
   
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        Environment env = EnvironmentFactory.newEnvironment();
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.jbpm.persistence.jpa");
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);

        StatefulKnowledgeSession ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        
        Map<String, Object> params = new HashMap<String, Object>();
        ProcessInstance processInstance = ksession.startProcess(
                "subproc", params);
        
        ksession.getWorkItemManager().completeWorkItem(
                workItemHandler.getWorkItem().getId(), null);
        
        int sessionId = ksession.getId();

        ksession.dispose();        
        Thread.sleep(3000);
        
        ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(sessionId, kbase, null, env);
        Thread.sleep(3000);
        assertProcessInstanceCompleted(processInstance.getId(), ksession);

    }
    
    public void testRuleTaskWithFacts() throws Exception {
        KnowledgeBuilderConfiguration conf = KnowledgeBuilderFactory
                .newKnowledgeBuilderConfiguration();
        ((PackageBuilderConfiguration) conf).initSemanticModules();
        ((PackageBuilderConfiguration) conf)
                .addSemanticModule(new BPMNSemanticModule());
        ((PackageBuilderConfiguration) conf)
                .addSemanticModule(new BPMNDISemanticModule());
        // ProcessDialectRegistry.setDialect("XPath", new XPathDialect());
        XmlProcessReader processReader = new XmlProcessReader(
                ((PackageBuilderConfiguration) conf).getSemanticModules(),
                getClass().getClassLoader());
        List<Process> processes = processReader
                .read(SimpleBPMNProcessTest.class
                        .getResourceAsStream("/BPMN2-RuleTaskWithFact.bpmn2"));
        assertNotNull(processes);
        assertEquals(1, processes.size());
        RuleFlowProcess p = (RuleFlowProcess) processes.get(0);
        assertNotNull(p);
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
                .newKnowledgeBuilder(conf);
        // logger.debug(XmlBPMNProcessDumper.INSTANCE.dump(p));
        kbuilder.add(ResourceFactory.newReaderResource(new StringReader(
                XmlBPMNProcessDumper.INSTANCE.dump(p))), ResourceType.BPMN2);
        kbuilder.add(
                ResourceFactory.newClassPathResource("BPMN2-RuleTask3.drl"),
                ResourceType.DRL);
        if (!kbuilder.getErrors().isEmpty()) {
            throw new IllegalArgumentException(
                    "Errors while parsing knowledge base");
        }
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        final StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        
        final org.drools.event.AgendaEventListener agendaEventListener = new org.drools.event.AgendaEventListener() {
            public void activationCreated(ActivationCreatedEvent event, WorkingMemory workingMemory){
                ksession.fireAllRules();
            }
            public void activationCancelled(ActivationCancelledEvent event, WorkingMemory workingMemory){
            }
            public void beforeActivationFired(BeforeActivationFiredEvent event, WorkingMemory workingMemory) {
            }
            public void afterActivationFired(AfterActivationFiredEvent event, WorkingMemory workingMemory) {
            }
            public void agendaGroupPopped(AgendaGroupPoppedEvent event, WorkingMemory workingMemory) {
            }

            public void agendaGroupPushed(AgendaGroupPushedEvent event, WorkingMemory workingMemory) {
            }
            public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event, WorkingMemory workingMemory) {
            }
            public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event, WorkingMemory workingMemory) {
                workingMemory.fireAllRules();
            }
            public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event, WorkingMemory workingMemory) {
            }
            public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event, WorkingMemory workingMemory) {
            }
        };
        ((StatefulKnowledgeSessionImpl)  ((KnowledgeCommandContext) ((CommandBasedStatefulKnowledgeSession) ksession)
                .getCommandService().getContext()).getStatefulKnowledgesession() )
                .session.addEventListener(agendaEventListener);
        ksession.addEventListener(new DebugAgendaEventListener());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "SomeString");
        ProcessInstance processInstance = ksession.startProcess("RuleTask", params);
        assertProcessInstanceCompleted(processInstance.getId(), ksession);

        params = new HashMap<String, Object>();

        try {
            processInstance = ksession.startProcess("RuleTask", params);

            fail("Should fail");
        } catch (Exception e) {
            e.printStackTrace();
        }

        params = new HashMap<String, Object>();
        params.put("x", "SomeString");
        processInstance = ksession.startProcess("RuleTask", params);
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
    }
    
    public void testEventTypesLifeCycle() throws Exception {
   	 // JBPM-4246
   	 KnowledgeBase kbase = createKnowledgeBase("BPMN2-IntermediateCatchSignalBetweenUserTasks.bpmn2");
   	 final StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
   	 TestWorkItemHandler handler = new TestWorkItemHandler();
   	 ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);
   	 ProcessInstance processInstance = ksession.startProcess("BPMN2-IntermediateCatchSignalBetweenUserTasks");
   	 
   	 int signalListSize = ksession.execute(new GenericCommand<Integer>() {
   	     public Integer execute(Context context) {
   	         SingleSessionCommandService commandService = (SingleSessionCommandService) ((CommandBasedStatefulKnowledgeSession) ksession)
   	                 .getCommandService();
   	         InternalKnowledgeRuntime kruntime = (InternalKnowledgeRuntime) commandService.getStatefulKnowledgeSession();
   	         ProcessPersistenceContextManager contextManager = (ProcessPersistenceContextManager) kruntime
   	                 .getEnvironment().get(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER);
   	         ProcessPersistenceContext pcontext = contextManager.getProcessPersistenceContext();
   	 
   	         List<Long> processInstancesToSignalList = pcontext.getProcessInstancesWaitingForEvent("MySignal");
   	         return processInstancesToSignalList.size();
   	     }
   	 });
   	 
   	 // Process instance is not waiting for signal
   	 assertEquals(0, signalListSize);
   	 WorkItem active = handler.getWorkItem();
   	 assertNotNull(active);
   	 ksession.getWorkItemManager().completeWorkItem(active.getId(), null);
   	 
   	 signalListSize = ksession.execute(new GenericCommand<Integer>() {
   	     public Integer execute(Context context) {
   	         SingleSessionCommandService commandService = (SingleSessionCommandService) ((CommandBasedStatefulKnowledgeSession) ksession)
   	                 .getCommandService();
   	         InternalKnowledgeRuntime kruntime = (InternalKnowledgeRuntime) commandService.getStatefulKnowledgeSession();
   	         ProcessPersistenceContextManager contextManager = (ProcessPersistenceContextManager) kruntime
   	                 .getEnvironment().get(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER);
   	         ProcessPersistenceContext pcontext = contextManager.getProcessPersistenceContext();
   	 
   	         List<Long> processInstancesToSignalList = pcontext.getProcessInstancesWaitingForEvent("MySignal");
   	         return processInstancesToSignalList.size();
   	     }
   	 });
   	 
   	 // Process instance is waiting for signal now
   	 assertEquals(1, signalListSize);
   	 
   	 ksession.signalEvent("MySignal", null);
   	 
   	 signalListSize = ksession.execute(new GenericCommand<Integer>() {
   	     public Integer execute(Context context) {
   	         SingleSessionCommandService commandService = (SingleSessionCommandService) ((CommandBasedStatefulKnowledgeSession) ksession)
   	                 .getCommandService();
   	         InternalKnowledgeRuntime kruntime = (InternalKnowledgeRuntime) commandService.getStatefulKnowledgeSession();
   	         ProcessPersistenceContextManager contextManager = (ProcessPersistenceContextManager) kruntime
   	                 .getEnvironment().get(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER);
   	         ProcessPersistenceContext pcontext = contextManager.getProcessPersistenceContext();
   	 
   	         List<Long> processInstancesToSignalList = pcontext.getProcessInstancesWaitingForEvent("MySignal");
   	         return processInstancesToSignalList.size();
   	     }
   	 });
   	 
   	 // Process instance is not waiting for signal
   	 assertEquals(0, signalListSize);
   	 active = handler.getWorkItem();
  	 assertNotNull(active);
   	 ksession.getWorkItemManager().completeWorkItem(active.getId(), null);
   	 
   	 
   	 ksession.dispose();
   	 
   }
}
