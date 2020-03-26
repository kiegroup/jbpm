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
package org.jbpm.test.regression.event;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jbpm.test.JbpmTestCase;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;

import static org.junit.Assert.assertFalse;


public class SubProcessWithTimerStartEventTest extends JbpmTestCase{
    
    public SubProcessWithTimerStartEventTest() {
        super(true, true);
    }
  
    @Test
    public void simpleSupportProcessTest() throws Exception {
        createRuntimeManager("org/jbpm/test/regression/event/SubProcessWithTimerStartEvent.bpmn2");
        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession ksession = runtimeEngine.getKieSession();
        final CountDownLatch end = new CountDownLatch(1);
        final AtomicBoolean reached = new AtomicBoolean(false);
        ksession.addEventListener(new DefaultProcessEventListener() {

            @Override
            public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
                String name = event.getNodeInstance().getNode().getName();

                if ("ScriptTask".equals(name)) {
                    reached.set(true);
                    end.countDown();
                } else if ("end2".equals(name)) {
                    end.countDown();
                }
            }

        });
  
        
        Map<String, Object> params = new HashMap<>();
        ProcessInstance processInstance = ksession.startProcess("SubProcessWithTimerStartEvent", params);


        end.await();
        assertFalse(reached.get());
        Assert.assertNull(ksession.getProcessInstance(processInstance.getId()));

    }
}
