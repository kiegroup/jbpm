/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.test.functional.task;


import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.drools.core.audit.WorkingMemoryInMemoryLogger;
import org.jbpm.test.JbpmTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.task.TaskService;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.internal.task.api.TaskModelProvider;

public class BulkHumanTaskTest extends JbpmTestCase {

    private static final String PROCESS_FILE = "org/jbpm/test/functional/task/BulkTask.bpmn2";
    private static final String PROCESS_ID = "com.bpms.functional.bpmn2.task.BulkTask";

    private static final String SALABOY = "salaboy";
    private static final String NEW_USER = "newUser";

    public BulkHumanTaskTest() {
        super(true, true);
    }

    @Before
    public void init() {
        manager = createRuntimeManager(Strategy.PROCESS_INSTANCE, PROCESS_ID, PROCESS_FILE);
        // create administrator
        RuntimeEngine engine = getRuntimeEngine(ProcessInstanceIdContext.get());
        TaskService taskService  = engine.getTaskService();
        ((InternalTaskService) taskService).addUser(TaskModelProvider.getFactory().newUser("Administrator"));
        ((InternalTaskService) taskService).addGroup(TaskModelProvider.getFactory().newGroup("admins"));
        ((InternalTaskService) taskService).addGroup(TaskModelProvider.getFactory().newGroup("users"));
        ((InternalTaskService) taskService).addGroup(TaskModelProvider.getFactory().newGroup("Administrators"));
        manager.disposeRuntimeEngine(engine);
    }
    @After
    public void destroy() {

        manager.close();
    }

    public class CompleteTask implements Runnable {
        private CountDownLatch latch;

        public CompleteTask(CountDownLatch latch) {
            this.latch = latch;
        }
        
        @Override
        public void run() {
            RuntimeEngine engine = getRuntimeEngine(ProcessInstanceIdContext.get());
            KieSession ksession = engine.getKieSession();
            WorkingMemoryInMemoryLogger logger = new WorkingMemoryInMemoryLogger(ksession);
            TaskService taskService  = engine.getTaskService();
            try {
                Long pid = ksession.startProcess(PROCESS_ID, Collections.singletonMap("user", NEW_USER)).getId();
                Long tid = taskService.getTasksByProcessInstanceId(pid).get(0);
                taskService.start(tid, SALABOY);
                latch.countDown();
                latch.await();
                taskService.complete(tid, SALABOY, Collections.emptyMap());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                synchronized (System.out) {
                    System.out.println("PROCESS -----8<-------");

                    logger.getLogEvents().forEach(e -> System.out.println(e));
                    System.out.println("PROCESS -----8<-------");
                }
                manager.disposeRuntimeEngine(engine);
            }
        }
        
    }

    @Test
    public void testTimeout() throws InterruptedException { 
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        
        CountDownLatch latch = new CountDownLatch(10);
        for(int i = 0; i < 10; i++) {
            executorService.submit(new CompleteTask(latch));
        }
        executorService.awaitTermination(10, TimeUnit.SECONDS);

    }

}
