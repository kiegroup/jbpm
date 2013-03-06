package org.jbpm.bpmn2.persistence;

import java.util.ArrayList;
import java.util.List;

import org.drools.WorkingMemory;
import org.drools.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.command.impl.KnowledgeCommandContext;
import org.drools.event.ActivationCancelledEvent;
import org.drools.event.ActivationCreatedEvent;
import org.drools.event.AfterActivationFiredEvent;
import org.drools.event.AgendaEventListener;
import org.drools.event.AgendaGroupPoppedEvent;
import org.drools.event.AgendaGroupPushedEvent;
import org.drools.event.BeforeActivationFiredEvent;
import org.drools.event.RuleFlowGroupActivatedEvent;
import org.drools.event.RuleFlowGroupDeactivatedEvent;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.jbpm.bpmn2.JbpmTestCase;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.KieBase;
import org.kie.event.process.DefaultProcessEventListener;
import org.kie.event.process.ProcessStartedEvent;
import org.kie.runtime.Environment;
import org.kie.runtime.KieSessionConfiguration;
import org.kie.runtime.StatefulKnowledgeSession;

public class TimerCycleOnBinaryPackageTest extends JbpmTestCase {

    private static final String resDir = "manual/";
    
    private StatefulKnowledgeSession ksession;

    public TimerCycleOnBinaryPackageTest() {
        super(true);
    }

    @BeforeClass
    public static void setup() throws Exception {
        setUpDataSource();
    }

    @After
    public void dispose() {
        if (ksession != null) {
            ksession.dispose();
        }
    }
    
    @Test
    public void testStartTimerCycleFromDisc() throws Exception {
        KieBase kbase = createKnowledgeBaseFromDisc(resDir
                + "StartTimerCycle.bpmn2");
        ksession = createKnowledgeSession(kbase);

        int sessionId = ksession.getId();
        KieSessionConfiguration config = ksession.getSessionConfiguration();
        Environment env = ksession.getEnvironment();

        final List<Long> list = new ArrayList<Long>();
        ksession.addEventListener(new DefaultProcessEventListener() {
            public void afterProcessStarted(ProcessStartedEvent event) {
                list.add(event.getProcessInstance().getId());
            }
        });

        ((StatefulKnowledgeSessionImpl) ((KnowledgeCommandContext) ((CommandBasedStatefulKnowledgeSession) ksession)
                .getCommandService().getContext()).getKieSession()).session
                .addEventListener(new TriggerRulesEventListener(ksession));

        ksession.fireAllRules();

        Thread.sleep(5000);

        assertEquals(2, list.size());
        System.out.println("dispose");
        // ksession.dispose();

        // FIXME DROOLS-48
        // ksession = reloadSession(ksession, sessionId, kbase, config, env, true);

        final List<Long> list2 = new ArrayList<Long>();
        ksession.addEventListener(new DefaultProcessEventListener() {
            public void beforeProcessStarted(ProcessStartedEvent event) {
                list2.add(event.getProcessInstance().getId());
            }
        });

        ((StatefulKnowledgeSessionImpl) ((KnowledgeCommandContext) ((CommandBasedStatefulKnowledgeSession) ksession)
                .getCommandService().getContext()).getKieSession()).session
                .addEventListener(new TriggerRulesEventListener(ksession));

        ksession.fireAllRules();

        Thread.sleep(5000);

        assertEquals(3, list2.size());
    }

    @Test
    public void testStartTimerCycleFromClassPath() throws Exception {
        KieBase kbase = createKnowledgeBase(resDir + "StartTimerCycle.bpmn2");
        ksession = createKnowledgeSession(kbase);

        int sessionId = ksession.getId();
        KieSessionConfiguration config = ksession.getSessionConfiguration();
        Environment env = ksession.getEnvironment();

        final List<Long> list = new ArrayList<Long>();
        ksession.addEventListener(new DefaultProcessEventListener() {
            public void afterProcessStarted(ProcessStartedEvent event) {
                list.add(event.getProcessInstance().getId());
            }
        });

        ((StatefulKnowledgeSessionImpl) ((KnowledgeCommandContext) ((CommandBasedStatefulKnowledgeSession) ksession)
                .getCommandService().getContext()).getKieSession()).session
                .addEventListener(new TriggerRulesEventListener(ksession));

        ksession.fireAllRules();

        Thread.sleep(5000);

        assertEquals(2, list.size());
        System.out.println("dispose");
        // ksession.dispose();

        // FIXME DROOLS-48
        // ksession = reloadSession(ksession, sessionId, kbase, config, env, true);

        final List<Long> list2 = new ArrayList<Long>();
        ksession.addEventListener(new DefaultProcessEventListener() {
            public void beforeProcessStarted(ProcessStartedEvent event) {
                list2.add(event.getProcessInstance().getId());
            }
        });

        ((StatefulKnowledgeSessionImpl) ((KnowledgeCommandContext) ((CommandBasedStatefulKnowledgeSession) ksession)
                .getCommandService().getContext()).getKieSession()).session
                .addEventListener(new TriggerRulesEventListener(ksession));

        ksession.fireAllRules();

        Thread.sleep(5000);

        assertEquals(3, list2.size());
    }

    @Test
    public void testStartTimerCycleFromDiscDRL() throws Exception {
        KieBase kbase = createKnowledgeBaseFromDisc(resDir
                + "StartTimerCycle.drl");
        ksession = createKnowledgeSession(kbase);

        int sessionId = ksession.getId();
        KieSessionConfiguration config = ksession.getSessionConfiguration();
        Environment env = ksession.getEnvironment();

        final List<String> list = new ArrayList<String>();
        ksession.setGlobal("list", list);

        ((StatefulKnowledgeSessionImpl) ((KnowledgeCommandContext) ((CommandBasedStatefulKnowledgeSession) ksession)
                .getCommandService().getContext()).getKieSession()).session
                .addEventListener(new TriggerRulesEventListener(ksession));

        ksession.fireAllRules();

        Thread.sleep(5000);

        assertEquals(2, list.size());
        System.out.println("dispose");
        // ksession.dispose();

        // FIXME DROOLS-48
        // ksession = reloadSession(ksession, sessionId, kbase, config, env, true);

        final List<String> list2 = new ArrayList<String>();
        ksession.setGlobal("list", list2);

        ((StatefulKnowledgeSessionImpl) ((KnowledgeCommandContext) ((CommandBasedStatefulKnowledgeSession) ksession)
                .getCommandService().getContext()).getKieSession()).session
                .addEventListener(new TriggerRulesEventListener(ksession));

        ksession.fireAllRules();

        Thread.sleep(5000);

        assertEquals(3, list2.size());
    }

    @Test
    public void testStartTimerCycleFromClasspathDRL() throws Exception {
        // load up the knowledge base
        KieBase kbase = createKnowledgeBase(resDir + "StartTimerCycle.drl");
        ksession = createKnowledgeSession(kbase);

        int sessionId = ksession.getId();
        KieSessionConfiguration config = ksession.getSessionConfiguration();
        Environment env = ksession.getEnvironment();

        final List<String> list = new ArrayList<String>();
        ksession.setGlobal("list", list);

        ((StatefulKnowledgeSessionImpl) ((KnowledgeCommandContext) ((CommandBasedStatefulKnowledgeSession) ksession)
                .getCommandService().getContext()).getKieSession()).session
                .addEventListener(new TriggerRulesEventListener(ksession));

        ksession.fireAllRules();

        Thread.sleep(5000);

        assertEquals(2, list.size());
        System.out.println("dispose");
        // ksession.dispose();

        // FIXME DROOLS-48
        // ksession = reloadSession(ksession, sessionId, kbase, config, env, true);

        final List<String> list2 = new ArrayList<String>();
        ksession.setGlobal("list", list2);

        ((StatefulKnowledgeSessionImpl) ((KnowledgeCommandContext) ((CommandBasedStatefulKnowledgeSession) ksession)
                .getCommandService().getContext()).getKieSession()).session
                .addEventListener(new TriggerRulesEventListener(ksession));

        ksession.fireAllRules();

        Thread.sleep(5000);

        assertEquals(3, list2.size());
    }

    private static class TriggerRulesEventListener implements
            AgendaEventListener {

        private StatefulKnowledgeSession ksession;

        public TriggerRulesEventListener(StatefulKnowledgeSession ksession) {
            this.ksession = ksession;
        }

        public void activationCreated(ActivationCreatedEvent event,
                WorkingMemory workingMemory) {
            ksession.fireAllRules();
        }

        public void activationCancelled(ActivationCancelledEvent event,
                WorkingMemory workingMemory) {
        }

        public void beforeActivationFired(BeforeActivationFiredEvent event,
                WorkingMemory workingMemory) {
        }

        public void afterActivationFired(AfterActivationFiredEvent event,
                WorkingMemory workingMemory) {
        }

        public void agendaGroupPopped(AgendaGroupPoppedEvent event,
                WorkingMemory workingMemory) {
        }

        public void agendaGroupPushed(AgendaGroupPushedEvent event,
                WorkingMemory workingMemory) {
        }

        public void beforeRuleFlowGroupActivated(
                RuleFlowGroupActivatedEvent event, WorkingMemory workingMemory) {
        }

        public void afterRuleFlowGroupActivated(
                RuleFlowGroupActivatedEvent event, WorkingMemory workingMemory) {
            workingMemory.fireAllRules();
        }

        public void beforeRuleFlowGroupDeactivated(
                RuleFlowGroupDeactivatedEvent event, WorkingMemory workingMemory) {
        }

        public void afterRuleFlowGroupDeactivated(
                RuleFlowGroupDeactivatedEvent event, WorkingMemory workingMemory) {
        }

    }
}