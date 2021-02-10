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

package org.jbpm.bpmn2.mappings;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jbpm.bpmn2.JbpmBpmn2TestCase;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.MessageEvent;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.runtime.KieSession;


public class ThrowEventMappingTest  extends JbpmBpmn2TestCase {

    @BeforeClass
    public static void setup() throws Exception {
        setUpDataSource();
    }

    @Test
    public void testEndNodeMapping() throws Exception {
        KieBase kbase = createKnowledgeBase("mappings/EndNodeMapping.bpmn2");
        KieSession ksession = createKnowledgeSession(kbase);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean ok = new AtomicBoolean(false);
        ksession.addEventListener(new DefaultProcessEventListener() {

            @Override
            public void onMessage(MessageEvent event) {
                ok.set("HOLA".equals(event.getMessage()));
                latch.countDown();
            }
        });

        ksession.startProcess("EndNodeMapping", Collections.singletonMap("procVar", "HOLA"));
        latch.await();
        Assert.assertTrue(ok.get());
    }


    @Test
    public void testIntermediateCatchNodeMapping() throws Exception {
        KieBase kbase = createKnowledgeBase("mappings/CatchNodeMapping.bpmn2");
        KieSession ksession = createKnowledgeSession(kbase);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean ok = new AtomicBoolean(false);
        ksession.addEventListener(new DefaultProcessEventListener() {

            @Override
            public void afterProcessCompleted(ProcessCompletedEvent event) {
                String actualValue = getProcessVarValue(event.getProcessInstance(), "procVar");
                ok.set("HOLA".equals(actualValue));
                assertProcessVarValue(event.getProcessInstance(), "procVar", "HOLA");
                latch.countDown();
            }
        });
        ksession.startProcess("CatchNodeMapping");
        ksession.signalEvent("Message-message", "HOLA");
        latch.await();
        Assert.assertTrue(ok.get());
    }
}
