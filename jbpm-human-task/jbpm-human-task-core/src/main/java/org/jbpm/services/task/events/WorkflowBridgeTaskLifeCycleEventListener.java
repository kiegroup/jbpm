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

package org.jbpm.services.task.events;

import org.jbpm.workflow.instance.node.HumanTaskNodeInstance;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.task.TaskEvent;
import org.kie.api.task.model.Task;
import org.kie.internal.runtime.manager.InternalRuntimeEngine;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

/*
 * For suspend the operation should be in ready, reserved or in progress
 * for reactivate it needs to move back from resume or exit -> operations.dsl
 */
public class WorkflowBridgeTaskLifeCycleEventListener extends DefaultTaskEventListener {

    private RuntimeManager runtimeManager;

    public WorkflowBridgeTaskLifeCycleEventListener(RuntimeManager runtimeManager) {
        this.runtimeManager = runtimeManager;
    }

    @Override
    public void afterTaskSuspendedEvent(TaskEvent event) {
        String suspendUntilExpression = (String) event.getMetadata().get(HumanTaskNodeInstance.SUSPEND_UNTIL_PARAMETER);
        executeWork(event.getTask(), suspendUntilExpression, HumanTaskNodeInstance.SUSPEND_SIGNAL);
    }

    @Override
    public void afterTaskResumedEvent(TaskEvent event) {
        String suspendUntilExpression = (String) event.getMetadata().get(HumanTaskNodeInstance.SUSPEND_UNTIL_PARAMETER);
        executeWork(event.getTask(), suspendUntilExpression, HumanTaskNodeInstance.ACTIVATE_SIGNAL);
    }

    public void executeWork(Task task, String suspendUntilExpression, String signal) {
        Long processInstanceId = task.getTaskData().getProcessInstanceId();
        InternalRuntimeEngine runtimeEngine = (InternalRuntimeEngine) runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
        try {
            KieSession kieSession = runtimeEngine.getKieSession();
            WorkItem workItem = ((org.drools.core.process.instance.WorkItemManager) kieSession.getWorkItemManager()).getWorkItem(task.getTaskData().getWorkItemId());
            workItem.getParameters().put(HumanTaskNodeInstance.SUSPEND_UNTIL_PARAMETER, suspendUntilExpression);
            kieSession.signalEvent(signal, workItem, processInstanceId);
        } finally {
            runtimeManager.disposeRuntimeEngine(runtimeEngine);
        }
    }


}
