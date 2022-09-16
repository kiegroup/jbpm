/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.test.functional.workitem;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.core.process.instance.WorkItemManager;
import org.drools.persistence.api.PersistenceContext;
import org.drools.persistence.jpa.JpaPersistenceContext;
import org.jbpm.persistence.JpaProcessPersistenceContextManager;
import org.jbpm.test.JbpmTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class RetrySyncWorkItemByJPATest extends JbpmTestCase {

    private static final String RETRY_WORKITEM_JPA_PROCESS_ID = "org.jbpm.test.retryWorkitem-jpa";

    private boolean pessimistic;

    public RetrySyncWorkItemByJPATest(boolean pessimistic) {
        super( true, true );
        this.pessimistic = pessimistic;
    }

    @Parameterized.Parameters(name = "pessimistic locking={0}")
    public static Collection<Object[]> pessimisticLocking() {
        Object[][] data = new Object[][] { { false }, { true } };
        return Arrays.asList(data);
    }

    @Test
    public void workItemRecoveryTestByJPA() {
        addEnvironmentEntry(EnvironmentName.USE_PESSIMISTIC_LOCKING, pessimistic);
        addWorkItemHandler("ExceptionWorkitem", new ExceptionWorkItemHandler());
        createRuntimeManager( "org/jbpm/test/functional/workitem/retry-workitem-jpa.bpmn2" );
        RuntimeEngine runtimeEngine = getRuntimeEngine();

        KieSession kieSession = runtimeEngine.getKieSession();
        Environment env = kieSession.getEnvironment();
        kieSession.getEnvironment().set(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER, new MyJpaProcessPersistenceContextManager(env));

        ProcessInstance pi = kieSession.startProcess( RETRY_WORKITEM_JPA_PROCESS_ID );
        TaskService taskService = runtimeEngine.getTaskService();

        assertProcessInstanceActive( pi.getId() );
        assertNodeTriggered( pi.getId(), "lockingNode" );
        assertNodeActive( pi.getId(), kieSession, "lockingNode" );

        List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner( "john", "en-UK" );
        assertEquals( 1, tasks.size() );
        TaskSummary taskSummary = tasks.get( 0 );

        taskService.start( taskSummary.getId(), "john" );
        taskService.complete( taskSummary.getId(), "john", null );

        ProcessInstance processInstance = kieSession.getProcessInstance(pi.getId() );
        Collection<NodeInstance> nis = ((org.kie.api.runtime.process.WorkflowProcessInstance) processInstance).getNodeInstances();
        retryWorkItem( (org.drools.core.process.instance.WorkItemManager) kieSession.getWorkItemManager(), nis );
        assertProcessInstanceCompleted( processInstance.getId() );
    }

    private void retryWorkItem( WorkItemManager workItemManager, Collection<NodeInstance> nis ) {
        for ( NodeInstance di : nis ) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put( "exception", "no" );
            workItemManager.retryWorkItem( di.getId(), map );
        }
    }

    private class MyJpaProcessPersistenceContextManager extends JpaProcessPersistenceContextManager {

        public MyJpaProcessPersistenceContextManager(Environment env) {
            super(env);
        }

        @Override
        public PersistenceContext getCommandScopedPersistenceContext() {
            return new JpaPersistenceContext(getCommandScopedEntityManager(), true, pessimistic, null, txm );
        }
    }
}