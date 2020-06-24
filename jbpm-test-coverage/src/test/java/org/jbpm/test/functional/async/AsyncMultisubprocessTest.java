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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.assertj.core.api.Assertions;
import org.jbpm.executor.ExecutorServiceFactory;
import org.jbpm.test.JbpmTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.executor.ExecutorService;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.test.util.db.PoolingDataSourceWrapper;

/**
 * process1: start -> catch signal -> first time exception -> end process2:
 * start -> async end signal --- should repeat when fails
 */
public class AsyncMultisubprocessTest extends JbpmTestCase {

    private static CountDownLatch latch = new CountDownLatch(1);

    private static final String BPMN_AICS = "org/jbpm/test/functional/async/AsyncSubMultiprocess.bpmn2";
    private static final String PROCESS_AICS = "TestSubMult";

    private ExecutorService executorService;
    PoolingDataSourceWrapper ds;

    @Before
    @Override
    public void setUp() throws Exception {
        setupDataSource = true;
        super.setUp();

        executorService = ExecutorServiceFactory.newExecutorService(getEmf());
        executorService.setInterval(1);
        executorService.init();

        addEnvironmentEntry("ExecutorService", executorService);
        addEnvironmentEntry("AsyncMode", new String("true"));

        addProcessEventListener(new DefaultProcessEventListener() {

            @Override
            public void afterProcessCompleted(ProcessCompletedEvent event) {
                latch.countDown();
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();

        executorService.destroy();
    }

    @Test()
    public void testCorrectProcessStateAfterExceptionEndSignal() throws Exception {
        final String USER_ID = "rhpamAdmin";
        KieSession ksession = createKSession(BPMN_AICS);

        Map<String, Object> properties = new HashMap<>();
        properties.put("number_reviews", 4);
        properties.put("approvalsRequired", 4);
        properties.put("employee", "egonzale");

        ProcessInstance processInstance = ksession.startProcess(PROCESS_AICS, properties);
        long pid1 = processInstance.getId();

        TaskService taskService = getRuntimeEngine().getTaskService();
        List<Long> taskIds = taskService.getTasksByProcessInstanceId(pid1);
        while (taskIds.size() < 4) {
            taskIds = taskService.getTasksByProcessInstanceId(pid1);
            Thread.sleep(100);
        }
        for (Long taskId : taskIds) {
            taskService.claim(taskId, USER_ID);
            taskService.start(taskId, USER_ID);
            taskService.complete(taskId, USER_ID, Collections.singletonMap("results", Boolean.TRUE));
        }
        latch.await();
        processInstance = ksession.getProcessInstance(pid1);
        Assertions.assertThat(processInstance).isNull();
    }
}
