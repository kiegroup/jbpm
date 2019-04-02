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

import java.util.concurrent.atomic.AtomicInteger;

import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.workflow.instance.node.ActionNodeInstance;
import org.jbpm.workflow.instance.node.SubProcessNodeInstance;
import org.junit.Test;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import static org.junit.Assert.assertEquals;

public class SubprocessDoublesTest extends JbpmTestCase {

    public SubprocessDoublesTest() {
        super(true, true);
    }

    class ExternalTaskWorkItemHandlerTest implements WorkItemHandler {

        public void abortWorkItem(WorkItem workItem, WorkItemManager workItemManager) {

        }

        public void executeWorkItem(WorkItem workItem, WorkItemManager workItemManager) {
            _lastWorkItemId = workItem.getId();
        }

        long _lastWorkItemId = 0;

        public long getLastWorkItemId() {
            return _lastWorkItemId;
        }
    }

    @Test
    public void testSubprocessDoubles() {
        createRuntimeManager("org/jbpm/test/functional/subprocess/TestSubWorkflow.bpmn2",
                             "org/jbpm/test/functional/subprocess/TestWorkflow.bpmn2");

        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession ksession = runtimeEngine.getKieSession();
        WorkItemManager itemManager = ksession.getWorkItemManager();

        ExternalTaskWorkItemHandlerTest externalTask = new ExternalTaskWorkItemHandlerTest();
        itemManager.registerWorkItemHandler("GenericTask", externalTask);

        RuleFlowProcessInstance processInstance = (RuleFlowProcessInstance) ksession
                                                                                    .startProcess("TestProject.TestWorkflow");
        final AtomicInteger counter = new AtomicInteger(0);
        ksession.addEventListener(new DefaultProcessEventListener() {

            @Override
            public void afterNodeLeft(ProcessNodeLeftEvent event) {
                if (event.getNodeInstance() instanceof ActionNodeInstance) {
                    counter.incrementAndGet();
                }
            }
        });

        assertProcessInstanceActive(processInstance.getId());
        assertNodeActive(processInstance.getId(), ksession, "subprocess");

        SubProcessNodeInstance subprocessNode = (SubProcessNodeInstance) processInstance.getNodeInstances().toArray()[0];
        RuleFlowProcessInstance subprocessInstance = (RuleFlowProcessInstance) ksession
                                                                                       .getProcessInstance(subprocessNode.getProcessInstanceId());

        // This code simaulates "GET process/instance" rest api call.
        // Every call of getProcessInstance(id, true) causes one extra-call of firs WorkItem after subprocess.
        ksession.getProcessInstance(processInstance.getId(), true);

        assertNodeActive(subprocessInstance.getId(), ksession, "Task1");

        // This code simulates "POST workitem/{workItemId}/complete".
        itemManager.completeWorkItem(externalTask.getLastWorkItemId(), null);
        assertProcessInstanceCompleted(processInstance.getId());
        assertEquals(counter.get(), 1);
    }
}
