package org.jbpm.persistence.map.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.common.AbstractRuleBase;
import org.drools.impl.InternalKnowledgeBase;
import org.drools.persistence.info.SessionInfo;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.persistence.map.EnvironmentBuilder;
import org.drools.process.instance.WorkItemHandler;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;
import org.jbpm.persistence.ProcessStorage;
import org.jbpm.persistence.ProcessStorageEnvironmentBuilder;
import org.jbpm.persistence.processinstance.ProcessInstanceInfo;
import org.junit.Assert;
import org.junit.Test;

public class MapPersistenceTest {

    @Test
    public void startProcessInPersistentEnvironment() {
        String processId = "minimalProcess";

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        ((AbstractRuleBase) ((InternalKnowledgeBase) kbase).getRuleBase())
                .addProcess( ProcessCreatorForHelp.newShortestProcess( processId ) );
        
        ProcessStorage storage = getSimpleProcessStorage();

        StatefulKnowledgeSession crmPersistentSession = createSession(kbase, storage);

        crmPersistentSession.startProcess(processId);

        crmPersistentSession.dispose();
    }

    private ProcessStorage getSimpleProcessStorage() {
        return new ProcessStorage() {

            private Map<Long, SessionInfo> ksessions = new HashMap<Long, SessionInfo>();
            private Map<Long, ProcessInstanceInfo> processes = new HashMap<Long, ProcessInstanceInfo>();

            public void saveOrUpdate(SessionInfo ksessionInfo) {
                ksessionInfo.update();
                ksessions.put( ksessionInfo.getId(), ksessionInfo );
            }

            public SessionInfo findSessionInfo(Long id) {
                return ksessions.get( id );
            }

            public ProcessInstanceInfo findProcessInstanceInfo(Long processInstanceId) {
                return processes.get( processInstanceId );
            }

            public void saveOrUpdate(ProcessInstanceInfo processInstanceInfo) {
                processInstanceInfo.update();
                processes.put( processInstanceInfo.getId(), processInstanceInfo );
            }

            public long getNextProcessInstanceId() {
                return processes.size()+1;
            }

            public void removeProcessInstanceInfo(Long id) {
                processes.remove( id );
            }

            public List<Long> getProcessInstancesWaitingForEvent(String type) {
                List<Long> processInstancesWaitingForEvent = new ArrayList<Long>();
                for ( ProcessInstanceInfo processInstanceInfo : processes.values() ) {
                    if( processInstanceInfo.getEventTypes().contains( type ) )
                        processInstancesWaitingForEvent.add( processInstanceInfo.getId() );
                }
                return processInstancesWaitingForEvent;
            }
        };
    }
    
    @Test
    public void createProcessStartItDisposeAndLoadItAgain() {
        String processId = "minimalProcess";
        String workName = "MyWork";

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        ((AbstractRuleBase) ((InternalKnowledgeBase) kbase).getRuleBase())
                .addProcess( ProcessCreatorForHelp.newProcessWithOneWork( processId,
                                                                          workName ) );

        ProcessStorage storage = getSimpleProcessStorage();
        StatefulKnowledgeSession crmPersistentSession = createSession(kbase, storage);

        DummyWorkItemHandler handler = new DummyWorkItemHandler();
        crmPersistentSession.getWorkItemManager()
            .registerWorkItemHandler(workName, handler);

        long process1Id = crmPersistentSession.startProcess(processId).getId();

        crmPersistentSession = disposeAndReloadSession(crmPersistentSession, kbase, storage);
        crmPersistentSession.getWorkItemManager().registerWorkItemHandler(workName, handler);

        long workItemId = handler.getLatestWorkItem().getId();

        crmPersistentSession.getWorkItemManager().completeWorkItem(workItemId, null);

        Assert.assertNotNull(crmPersistentSession);

        Assert.assertNull( crmPersistentSession.getProcessInstance( process1Id ) );

    }
    
    private StatefulKnowledgeSession createSession(KnowledgeBase kbase,
                                                   ProcessStorage storage) {
        
        EnvironmentBuilder envBuilder = new ProcessStorageEnvironmentBuilder( storage );
        Environment env = KnowledgeBaseFactory.newEnvironment();
        //FIXME temporary usage of this constants
        env.set( EnvironmentName.TRANSACTION_MANAGER,
                 envBuilder.getTransactionManager() );
        env.set( EnvironmentName.ENTITY_MANAGER_FACTORY,
                 envBuilder.getPersistenceContextManager() );

        return JPAKnowledgeService.newStatefulKnowledgeSession( kbase,
                                                                null,
                                                                env );
    }

    private StatefulKnowledgeSession disposeAndReloadSession(StatefulKnowledgeSession ksession,
                                                             KnowledgeBase kbase,
                                                             ProcessStorage storage) {
        long sessionId = ksession.getId();
        ksession.dispose();
        EnvironmentBuilder envBuilder = new ProcessStorageEnvironmentBuilder( storage );
        Environment env = KnowledgeBaseFactory.newEnvironment();
        //FIXME temporary usage of this constants
        env.set( EnvironmentName.TRANSACTION_MANAGER,
                 envBuilder.getTransactionManager() );
        env.set( EnvironmentName.ENTITY_MANAGER_FACTORY,
                 envBuilder.getPersistenceContextManager() );
        
        return JPAKnowledgeService.loadStatefulKnowledgeSession( sessionId, kbase, null, env );
    }

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
