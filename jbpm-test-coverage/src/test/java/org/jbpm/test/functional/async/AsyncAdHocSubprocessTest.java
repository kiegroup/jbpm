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

import org.jbpm.executor.ExecutorServiceFactory;
import org.jbpm.kie.services.impl.admin.commands.TriggerNodeCommand;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.listener.TrackingProcessEventListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.executor.ExecutorService;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;

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



}
