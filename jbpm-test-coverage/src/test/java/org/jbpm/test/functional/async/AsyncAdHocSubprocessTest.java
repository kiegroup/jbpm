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

package org.jbpm.test.functional.async;

import java.util.concurrent.CountDownLatch;

import org.jbpm.executor.ExecutorServiceFactory;
import org.jbpm.kie.services.impl.admin.commands.TriggerNodeCommand;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.listener.TrackingProcessEventListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.executor.ExecutorService;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jbpm.test.tools.TrackingListenerAssert.assertProcessStarted;
import static org.jbpm.test.tools.TrackingListenerAssert.assertTriggeredAndLeft;

/**
 * process1: start -> catch signal -> first time exception -> end process2:
 * start -> async end signal --- should repeat when fails
 */
public class AsyncAdHocSubprocessTest extends JbpmTestCase {

    private static final String ADHOC_ASYNC =
            "org/jbpm/test/functional/async/AsyncAdHocSubprocess.bpmn2";

    private static final String ADHOC_ASYNC_ID =
            "org.jbpm.test.functional.async.AsyncAdHocSubprocess";

    private static final String ERROR_MULTILEVEL_MAIN_PROCESS =
            "org/jbpm/test/functional/async/Level1P.bpmn2";

    private static final String ERROR_MULTILEVEL_CHILD_PROCESS =
            "org/jbpm/test/functional/async/Level2C.bpmn2";

    private static final String ERROR_MULTILEVEL_CHILD_CHILD_PROCESS =
            "org/jbpm/test/functional/async/Level3SC.bpmn2";

    private static final String ERROR_MULTILEVEL_EXCEPTION_PROCESS =
            "org/jbpm/test/functional/async/Exception.bpmn2";

    private static final String ERROR_MULTILEVEL_MAIN_PROCESS_ID =
            "nested-abort.level1P";

    private static final String ERROR_ASYNC_CHILD_EXCEPTION_PROCESS =
            "org/jbpm/test/functional/async/MainProcess.bpmn2";

    private static final String ERROR_ASYNC_MAIN_EXCEPTION_PROCESS =
            "org/jbpm/test/functional/async/SubProcess.bpmn2";

    private static final String ERROR_ASYNC_MAIN_EXCEPTION_PROCESS_ID =
            "cascade-abort.MainProcess";
    
    private ExecutorService executorService;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        executorService = ExecutorServiceFactory.newExecutorService(getEmf());
        executorService.setInterval(1);
        executorService.init();
        addEnvironmentEntry("ExecutorService", executorService);

    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();

        executorService.destroy();
    }

    @Test
    public void testAdHocSubAsyncProcessAuto() throws Exception {
        final long WAIT_ACTION = 10000L;

        KieSession kieSession = createKSession(ADHOC_ASYNC);

        TrackingProcessEventListener eventListener = new TrackingProcessEventListener();
        kieSession.addEventListener(eventListener);



        ProcessInstance pi = (ProcessInstance) kieSession.startProcess(ADHOC_ASYNC_ID);
        long id = pi.getId();

        eventListener.waitForProcessToStart(WAIT_ACTION);
        assertProcessStarted(eventListener, ADHOC_ASYNC_ID);
        eventListener.waitForNodeToBeLeft("start", WAIT_ACTION);
        assertTriggeredAndLeft(eventListener, "start");

        kieSession.execute(new TriggerNodeCommand(id, 3));
        eventListener.waitForNodeToBeLeft("script1", WAIT_ACTION);
        assertTriggeredAndLeft(eventListener, "script1");

        kieSession.abortProcessInstance(id);
        eventListener.waitForProcessToAbort(WAIT_ACTION);
        assertProcessInstanceAborted(id);
    }

    @Test(timeout = 30000)
    public void testErrorMultilevelActivity() throws Exception {

        
        
        KieSession ksession = createKSession(ERROR_MULTILEVEL_MAIN_PROCESS, ERROR_MULTILEVEL_CHILD_PROCESS, ERROR_MULTILEVEL_CHILD_CHILD_PROCESS, ERROR_MULTILEVEL_EXCEPTION_PROCESS);
        final CountDownLatch latch = new CountDownLatch(1);
        ksession.addEventListener(new DefaultProcessEventListener() {
            @Override
            public void afterProcessCompleted(ProcessCompletedEvent event) {
                if(ERROR_MULTILEVEL_MAIN_PROCESS_ID.equals(event.getProcessInstance().getProcessId())) {
                    latch.countDown();
                }
            }
        });
        ksession.getWorkItemManager().registerWorkItemHandler("Rest", new RestWorkItemHandler());
        ProcessInstance pi = ksession.startProcess(ERROR_MULTILEVEL_MAIN_PROCESS_ID);
        latch.await();

        Thread.sleep(1000L);
        getLogService().findProcessInstances()
                       .stream()
                       .filter(e -> !e.getProcessId().equals("nested-abort.exception"))
                       .forEach(e -> {
                           assertThat(e.getStatus(), is(ProcessInstance.STATE_ABORTED));
                       });
        assertProcessInstanceAborted(pi.getId());
 
    }

    

    @Test(timeout = 30000)
    public void testErrorAsyncActivity() throws Exception {

        KieSession ksession = createKSession(ERROR_ASYNC_CHILD_EXCEPTION_PROCESS,  ERROR_ASYNC_MAIN_EXCEPTION_PROCESS);
        final CountDownLatch latch = new CountDownLatch(1);
        ksession.addEventListener(new DefaultProcessEventListener() {
            @Override
            public void afterProcessCompleted(ProcessCompletedEvent event) {
                if(ERROR_ASYNC_MAIN_EXCEPTION_PROCESS_ID.equals(event.getProcessInstance().getProcessId())) {
                    latch.countDown();
                }
            }
        });
        ksession.getWorkItemManager().registerWorkItemHandler("Rest", new RestWorkItemHandler(true));
        ProcessInstance pi = ksession.startProcess(ERROR_ASYNC_MAIN_EXCEPTION_PROCESS_ID);
        latch.await();

        Thread.sleep(1000L);
        getLogService().findProcessInstances().forEach(e -> assertThat(e.getStatus(), is(ProcessInstance.STATE_ABORTED)) );
        assertProcessInstanceAborted(pi.getId());
 
    }

    static class RestWorkItemHandler implements WorkItemHandler {

        
        private boolean throwException;

        public RestWorkItemHandler() {
            this(false);
        }
        
        public RestWorkItemHandler(boolean throwException) {
            this.throwException = throwException;
        }
        
        @Override
        public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
            if(throwException) {
                throw new RuntimeException();
            }
            manager.completeWorkItem(workItem.getId(), emptyMap());
        }

        @Override
        public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
         // empty
        }

    }
}
