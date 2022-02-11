/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.jbpm.executor.ExecutorServiceFactory;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.functional.event.MyFact;
import org.jbpm.test.wih.FirstErrorWorkItemHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.executor.ExecutorService;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

/**
 * process1: start -> catch signal -> first time exception -> end
 * AsyncSignalEventCommand should be repeated when fails
 */
public class BatchIntermediateCatchSignalTest extends JbpmTestCase {

    private static final String CONDITION_CATCH = "org/jbpm/test/functional/async/ConditionEventGatewayTest.bpmn2";
    private static final String CONDITION_CATCH_ID = "org.jbpm.test.functional.async.eventgatewaytest";

    private ExecutorService executorService;
    private CountDownLatch latch;

    @BeforeClass
    public static void init () {
        System.setProperty("org.kie.jbpm.signal.batch.threshold", "100");
    }

    public static void destroy () {
        System.clearProperty("org.kie.jbpm.signal.batch.threshold");
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        executorService = ExecutorServiceFactory.newExecutorService(getEmf());
        executorService.setInterval(0);
        executorService.setThreadPoolSize(3);
        addEnvironmentEntry("ExecutorService", executorService);
        addWorkItemHandler("SyncError", new FirstErrorWorkItemHandler());
        addProcessEventListener(new DefaultProcessEventListener() {
            @Override
            public void afterProcessCompleted(ProcessCompletedEvent event) {
                latch.countDown();
            }
        });
        executorService.init();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();

        executorService.destroy();
    }

    @Test //(timeout = 20000)
    public void testBatchSignalManager() throws Exception{

        int count = 100;
        latch = new CountDownLatch(count);

        List<Long> pids = new ArrayList<>();
        RuntimeManager rm = createRuntimeManager(Strategy.PROCESS_INSTANCE, (String) null, CONDITION_CATCH);

        for (int i = 0; i < count; i++) {
             RuntimeEngine engine = getRuntimeEngine(ProcessInstanceIdContext.get());
             pids.add(engine.getKieSession().startProcess(CONDITION_CATCH_ID).getId());
             rm.disposeRuntimeEngine(engine);
        }

        MyFact myFact = new MyFact();
        myFact.setConditionA(true);
        rm.signalEvent("signalA", myFact);

        latch.await();
        for (long p : pids) {
            RuntimeEngine engine = rm.getRuntimeEngine(ProcessInstanceIdContext.get(p));
            try {
                engine.getKieSession();
                Assert.fail();
            } catch(org.kie.internal.runtime.manager.SessionNotFoundException e) {
                // do nothing this is correct
            } finally {
                rm.disposeRuntimeEngine(engine);
            }
        }

    }

}
