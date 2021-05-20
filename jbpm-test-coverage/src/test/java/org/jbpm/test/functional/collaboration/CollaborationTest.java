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

package org.jbpm.test.functional.collaboration;

import java.util.Collections;

import org.jbpm.test.JbpmTestCase;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;

import static org.junit.Assert.assertEquals;

public class CollaborationTest extends JbpmTestCase {

    public CollaborationTest() {
        super(true, true);
    }

    @Test
    public void testBoundaryMessageCollaboration () {
        addWorkItemHandler("Human Task", getTestWorkItemHandler());
        createRuntimeManager("org/jbpm/test/functional/collaboration/Collaboration-BoundaryMessage.bpmn2");
        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession ksession = runtimeEngine.getKieSession();
        Long pid = ksession.startProcess("collaboration.BoundaryMessage", Collections.singletonMap("MessageId", "2")).getId();
        ksession.signalEvent("Message-collaboration", new Message("1", "example"), pid);
        assertProcessInstanceActive(pid);
        ksession.signalEvent("Message-collaboration", new Message("2", "example"), pid);
        assertProcessInstanceCompleted(pid);
    }

    @Test
    public void testStartMessageCollaboration () {
        addWorkItemHandler("Human Task", getTestWorkItemHandler());
        createRuntimeManager("org/jbpm/test/functional/collaboration/Collaboration-StartMessage.bpmn2");
        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession ksession = runtimeEngine.getKieSession();
        ksession.signalEvent("Message-collaboration", new Message("1", "example"));
        assertEquals(1, getLogService().findProcessInstances().size());
    }

    @Test
    public void testStartMessageCollaborationNoMatch () {
        addWorkItemHandler("Human Task", getTestWorkItemHandler());
        createRuntimeManager("org/jbpm/test/functional/collaboration/Collaboration-StartMessage.bpmn2");
        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession ksession = runtimeEngine.getKieSession();
        ksession.signalEvent("Message-collaboration", new Message("2", "example"));
        assertEquals(0, getLogService().findProcessInstances().size());
    }

    @Test
    public void testIntermediateMessageCollaboration () {
        addWorkItemHandler("Human Task", getTestWorkItemHandler());
        createRuntimeManager("org/jbpm/test/functional/collaboration/Collaboration-IntermediateMessage.bpmn2");
        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession ksession = runtimeEngine.getKieSession();
        Long pid = ksession.startProcess("collaboration.IntermediateMessage", Collections.singletonMap("MessageId", "2")).getId();
        ksession.signalEvent("Message-collaboration", new Message("1", "example"), pid);
        assertProcessInstanceActive(pid);
        ksession.signalEvent("Message-collaboration", new Message("2", "example"), pid);
        assertProcessInstanceCompleted(pid);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidIntermediateMessageCollaboration () {
        addWorkItemHandler("Human Task", getTestWorkItemHandler());
        createRuntimeManager("org/jbpm/test/functional/collaboration/Collaboration-IntermediateMessage.bpmn2");
        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession ksession = runtimeEngine.getKieSession();
        Long pid = ksession.startProcess("collaboration.IntermediateMessage", Collections.singletonMap("MessageId", "2")).getId();
        ksession.signalEvent("Message-collaboration", new Message(null, "example"), pid);
        assertProcessInstanceActive(pid);
    }
}
