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

import java.util.HashMap;

import org.jbpm.executor.ExecutorServiceFactory;
import org.jbpm.kie.services.impl.admin.commands.TriggerNodeCommand;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.wih.FirstErrorWorkItemHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.executor.ExecutorService;
import org.kie.api.runtime.KieSession;


public class TriggerAsyncNodeTest extends JbpmTestCase {


    private static final String PROCESS_TRIGGER_ASYNC = "org.jbpm.test.functional.async.TriggerAsyncNodeCompletion";
    private static final String BPMN_TRIGGER_ASYNC = "org/jbpm/test/functional/async/TriggerAsyncNodeCompletion.bpmn2";

    private ExecutorService executorService;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        executorService = ExecutorServiceFactory.newExecutorService(getEmf());
        executorService.setInterval(1);
        executorService.setRetries(0);
        executorService.init();
        addEnvironmentEntry("ExecutorService", executorService);
        addWorkItemHandler("Rest", new FirstErrorWorkItemHandler());
        addProcessEventListener(new DefaultProcessEventListener() {
            @Override
            public void afterProcessCompleted(ProcessCompletedEvent event) {
                System.out.println("PROCESS COMPLETED");
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();

        executorService.destroy();
    }

    @Test
    public void testTriggerAsyncNode () throws Exception {
        KieSession ksession = createKSession(BPMN_TRIGGER_ASYNC);
        long pid = ksession.startProcess(PROCESS_TRIGGER_ASYNC, new HashMap<>()).getId();
        Thread.sleep(1000L);
        ksession.execute(new TriggerNodeCommand(pid, 1));
        Thread.sleep(1000L);
        assertProcessInstanceCompleted(pid);
    }


}
