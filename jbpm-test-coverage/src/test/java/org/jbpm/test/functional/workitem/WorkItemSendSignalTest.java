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

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import org.jbpm.executor.ExecutorServiceFactory;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.test.JbpmTestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.executor.ExecutorService;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.runtime.manager.context.EmptyContext;

public class WorkItemSendSignalTest extends JbpmTestCase {

    private static final String PROCESS_ID_SENDER = "org.jbpm.test.functional.workitem.Signal-Sender";
    private static final String PROCESS_ID_RECEIVER = "org.jbpm.test.functional.workitem.Signal-Receiver";
    private ExecutorService executorService;

    public WorkItemSendSignalTest() {
        super( true, true );
    }
    
    @Before
    public void setup() {
        executorService = buildExecutorService();
    }

    @After
    public void teardown() {
        executorService.destroy();
    }

    @Test
    public void testSendAsyncSignalFromWorkItemHandler() throws Exception {
        SendSignalWorkItemHandler handler = new SendSignalWorkItemHandler();
        addWorkItemHandler("SendSignalWorkitem", handler);
        addEnvironmentEntry("ExecutorService", executorService);
        manager = createRuntimeManager(Strategy.PROCESS_INSTANCE, null, 
                                       "org/jbpm/test/functional/workitem/WorkItem-SignalIntermediateCatch.bpmn2",
                                       "org/jbpm/test/functional/workitem/WorkItem-SendSignal.bpmn2"
                                       );
        handler.setRuntimeManager(manager);

        RuntimeEngine runtimeEngineReceiver = getRuntimeEngine( EmptyContext.get() );
        KieSession kieSessionReceiver = runtimeEngineReceiver.getKieSession();
        ProcessInstance piReceiver = kieSessionReceiver.startProcess( PROCESS_ID_RECEIVER );
        manager.disposeRuntimeEngine(runtimeEngineReceiver);

        RuntimeEngine runtimeEngineSender = getRuntimeEngine( EmptyContext.get() );
        KieSession kieSessionSender = runtimeEngineSender.getKieSession();
        kieSessionSender.startProcess( PROCESS_ID_SENDER, Collections.singletonMap("OtherProcessInstanceId", Long.toString(piReceiver.getId())));
        manager.disposeRuntimeEngine(runtimeEngineSender);
        activeEngines.clear();

        
        Thread.sleep(1000L);


        // check all process are finished
        EntityManager em = getEmf().createEntityManager();
        List<ProcessInstanceLog> logs = em.createQuery("SELECT o FROM ProcessInstanceLog o", ProcessInstanceLog.class).getResultList();
        Assert.assertTrue(logs.stream().allMatch(e -> e.getStatus() == ProcessInstance.STATE_COMPLETED));
        em.close();

    }

    private ExecutorService buildExecutorService() {
        ExecutorService es = ExecutorServiceFactory.newExecutorService(getEmf());
        es.init();

        return es;
    }
}