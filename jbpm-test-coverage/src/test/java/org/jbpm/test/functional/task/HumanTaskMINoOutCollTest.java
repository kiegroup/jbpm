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
package org.jbpm.test.functional.task;

import org.jbpm.services.task.identity.JBossUserGroupCallbackImpl;
import org.jbpm.services.task.wih.util.LocalHTWorkItemHandlerUtil;
import org.jbpm.test.JbpmTestCase;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;

public class HumanTaskMINoOutCollTest extends JbpmTestCase {

    private static final String TASK_MI_NO_OUTCOLL = "org/jbpm/test/functional/task/HumanTaskMiNoOutputColl.bpmn2";
    private static final String MI_TASK_ID = "com.sample.humantask.mitask";
    private static final String USER_GROUP_RES = "classpath:/usergroups.properties";

    private KieSession kieSession;
    private TaskService taskService;

    public HumanTaskMINoOutCollTest() {
        super(true,
              true);
    }

    @Before
    public void init() throws Exception {
        createRuntimeManager(TASK_MI_NO_OUTCOLL);
        RuntimeEngine re = getRuntimeEngine();
        kieSession = re.getKieSession();
        taskService = LocalHTWorkItemHandlerUtil.registerLocalHTWorkItemHandler(kieSession,
                                                                                getEmf(),
                                                                                new JBossUserGroupCallbackImpl(USER_GROUP_RES));
    }

    @Test
    public void testExecuteMiTask() {
        ProcessInstance processInstance = kieSession.startProcess(MI_TASK_ID);
        assertProcessInstanceNotActive(processInstance.getId(),
                                       kieSession);
        assertProcessInstanceCompleted(processInstance.getId());
    }
}
