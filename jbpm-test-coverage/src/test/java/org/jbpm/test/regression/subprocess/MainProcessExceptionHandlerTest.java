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

package org.jbpm.test.regression.subprocess;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.junit.Test;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;

import static org.junit.Assert.assertTrue;

public class MainProcessExceptionHandlerTest extends JbpmTestCase {

    private static final String MAIN_PROCESS = "org/jbpm/test/regression/subprocess/MainProcessExceptionHandler.bpmn2";

    private static final String MAIN_SUBPROCESS = "org/jbpm/test/regression/subprocess/MainSubprocessExceptionHandler.bpmn2";

    private static final String MAIN_PROCESS_ID = "com.DealWithException";

    @Test
    public void testMainSubprocessExceptionHandler() {
        KieSession kieSession = createKSession(MAIN_PROCESS, MAIN_SUBPROCESS);
        Map<String, Object> params = new HashMap<>();
        params.put("launchOnParent", Boolean.FALSE);

        MutableBoolean error = new MutableBoolean(false);
        kieSession.addEventListener(new DefaultProcessEventListener() {
            @Override
            public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
                if(!"StartProcess".equals(event.getNodeInstance().getNodeName())) {
                    return;
                }
                if(!"com.DealWithException".equals(event.getProcessInstance().getProcessId())) {
                    return;
                }

                Exception e = (Exception) ((WorkflowProcessInstanceImpl) event.getProcessInstance()).getVariable("event");
                error.setValue(e != null);
            }
        });
        ProcessInstance pi = kieSession.startProcess(MAIN_PROCESS_ID, params);
        this.assertProcessInstanceAborted(pi.getId());
        assertTrue(error.getValue());
    }

}
