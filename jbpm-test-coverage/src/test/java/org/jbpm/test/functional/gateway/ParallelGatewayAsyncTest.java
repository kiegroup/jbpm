/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.test.functional.gateway;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.executor.ExecutorServiceFactory;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.wih.ListWorkItemHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.executor.ExecutorService;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;

import static org.junit.Assert.assertNull;

/**
 * Parallel gateway execution test. 2x parallel fork, 1x join
 */
public class ParallelGatewayAsyncTest extends JbpmTestCase {

    private static final String PARALLEL_GATEWAY_ASYNC = "org/jbpm/test/functional/gateway/ParallelGatewayAsync.bpmn";
    private static final String PARALLEL_GATEWAY_ASYNC_ID = "org.jbpm.test.functional.gateway.ParallelGatewayAsync";

    private ExecutorService executorService;
    private KieSession kieSession;
    private ListWorkItemHandler wih;

    
    
    public ParallelGatewayAsyncTest() {
        super(true, true);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        executorService = ExecutorServiceFactory.newExecutorService(getEmf());
        executorService.setInterval(1);
        executorService.init();
        addEnvironmentEntry("AsyncMode", "true");
        addEnvironmentEntry("ExecutorService", executorService);
        wih = new ListWorkItemHandler(); 
        addWorkItemHandler("Human Task", wih);
        kieSession = createKSession(PARALLEL_GATEWAY_ASYNC);
    }

    @After
    public void tearDown() throws Exception {

        executorService.clearAllErrors();
        executorService.clearAllRequests();
        executorService.destroy();
        super.tearDown();
    }

    /**
     * Simple parallel gateway test.
     */
    @Test(timeout = 30000)
    public void testParallelGatewayAsync() throws Exception {
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("useHT", Boolean.TRUE);
        inputs.put("mode", "1");
        ProcessInstance pi = kieSession.startProcess(PARALLEL_GATEWAY_ASYNC_ID, inputs);
        Thread.sleep(3000L);
        wih.getWorkItems().forEach(e -> kieSession.getWorkItemManager().completeWorkItem(e.getId(), e.getParameters()));

        Thread.sleep(1000L);
        assertNull(kieSession.getProcessInstance(pi.getId()));

    }

}
