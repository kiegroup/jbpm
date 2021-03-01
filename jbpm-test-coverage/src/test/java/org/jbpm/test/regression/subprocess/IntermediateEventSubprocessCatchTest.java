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

package org.jbpm.test.regression.subprocess;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jbpm.test.JbpmTestCase;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;

public class IntermediateEventSubprocessCatchTest extends JbpmTestCase {


    private static final String INTERMEDIATE_CATCH_EVENT_SUBPROCESS_PROCESS = "org/jbpm/test/regression/subprocess/IntermediateEventSubprocessCatch.bpmn2";
    private static final String INTERMEDIATE_CATCH_EVENT_SUBPROCESS_PROCESS_ID = "IntermediateEventSubprocessCatch";

    private KieSession ksession;


    @Before
    public void init() throws Exception {
        ksession = createKSession(INTERMEDIATE_CATCH_EVENT_SUBPROCESS_PROCESS);
    }

    @Test(timeout = 30000)
    public void testIntermediateCatchEventSubprocess() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        ksession.addEventListener(new DefaultProcessEventListener() {
            @Override
            public void afterProcessCompleted(ProcessCompletedEvent event) {
                latch.countDown();
            }
        });
        ProcessInstance pi = ksession.startProcess(INTERMEDIATE_CATCH_EVENT_SUBPROCESS_PROCESS_ID, Collections.singletonMap("id", "1"));
        latch.await(5000L, TimeUnit.MILLISECONDS);
        this.assertProcessInstanceCompleted(pi.getId());
    }

}