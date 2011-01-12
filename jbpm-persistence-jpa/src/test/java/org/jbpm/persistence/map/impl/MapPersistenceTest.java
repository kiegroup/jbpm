package org.jbpm.persistence.map.impl;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.common.AbstractRuleBase;
import org.drools.impl.InternalKnowledgeBase;
import org.drools.process.instance.WorkItemHandler;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.junit.Assert;
import org.junit.Test;

public abstract class MapPersistenceTest {

    @Test
    public void startProcessInPersistentEnvironment() {
        String processId = "minimalProcess";

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        ((AbstractRuleBase) ((InternalKnowledgeBase) kbase).getRuleBase())
                .addProcess( ProcessCreatorForHelp.newShortestProcess( processId ) );
        
        StatefulKnowledgeSession crmPersistentSession = createSession(kbase);

        crmPersistentSession.startProcess(processId);

        crmPersistentSession.dispose();
    }

    @Test
    public void createProcessStartItDisposeAndLoadItAgain() {
        String processId = "minimalProcess";
        String workName = "MyWork";

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        ((AbstractRuleBase) ((InternalKnowledgeBase) kbase).getRuleBase())
                .addProcess( ProcessCreatorForHelp.newProcessWithOneWork( processId,
                                                                          workName ) );

        StatefulKnowledgeSession crmPersistentSession = createSession(kbase);

        DummyWorkItemHandler handler = new DummyWorkItemHandler();
        crmPersistentSession.getWorkItemManager()
            .registerWorkItemHandler(workName, handler);

        long process1Id = crmPersistentSession.startProcess(processId).getId();

        crmPersistentSession = disposeAndReloadSession(crmPersistentSession, kbase);
        crmPersistentSession.getWorkItemManager().registerWorkItemHandler(workName, handler);

        long workItemId = handler.getLatestWorkItem().getId();

        crmPersistentSession.getWorkItemManager().completeWorkItem(workItemId, null);

        Assert.assertNotNull(crmPersistentSession);

        Assert.assertNull( crmPersistentSession.getProcessInstance( process1Id ) );

    }
    
    @Test
    public void signalEventTest() {
        String processId = "signalProcessTest";
        String eventType = "myEvent";
        RuleFlowProcess process = ProcessCreatorForHelp.newSimpleEventProcess( processId,
                                                         eventType );

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        ((AbstractRuleBase) ((InternalKnowledgeBase) kbase).getRuleBase()).addProcess( process );
        
        StatefulKnowledgeSession crmPersistentSession = createSession(kbase);

        RuleFlowProcessInstance processInstance = (RuleFlowProcessInstance) crmPersistentSession.startProcess( processId );
        long processInstanceId = processInstance.getId();
        Assert.assertEquals( ProcessInstance.STATE_ACTIVE,
                             processInstance.getState() );

        crmPersistentSession = createSession(kbase);

        crmPersistentSession.signalEvent( eventType,
                              null );
        processInstance = (RuleFlowProcessInstance) crmPersistentSession.getProcessInstance( processInstanceId );

        Assert.assertNull( processInstance );
    }

    protected abstract StatefulKnowledgeSession createSession(KnowledgeBase kbase);
    
    protected abstract StatefulKnowledgeSession disposeAndReloadSession(StatefulKnowledgeSession crmPersistentSession,
                                                                        KnowledgeBase kbase);
    
    private static class DummyWorkItemHandler
        implements
        WorkItemHandler {

        private WorkItem latestWorkItem;

        public void executeWorkItem(WorkItem workItem,
                                    WorkItemManager manager) {
            this.setLatestWorkItem( workItem );
        }

        public void abortWorkItem(WorkItem workItem,
                                  WorkItemManager manager) {
        }

        public void setLatestWorkItem(WorkItem latestWorkItem) {
            this.latestWorkItem = latestWorkItem;
        }

        public WorkItem getLatestWorkItem() {
            return latestWorkItem;
        }
    }
}
