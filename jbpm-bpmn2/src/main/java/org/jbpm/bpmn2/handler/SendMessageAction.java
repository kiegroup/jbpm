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

package org.jbpm.bpmn2.handler;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.process.instance.WorkItemManager;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.bpmn2.core.Message;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.impl.JavaAction;
import org.kie.api.runtime.process.ProcessContext;

public class SendMessageAction implements JavaAction {

    private static final long serialVersionUID = 1L;
    private String varName;
    private Message message;

    public SendMessageAction(String varName, Message message) {
        this.varName = varName;
        this.message = message;
    }

    @Override
    public void execute(ProcessContext kcontext) throws Exception {
        Object tVariable = VariableResolver.getVariable(kcontext, varName);
        ((InternalProcessRuntime) ((InternalKnowledgeRuntime) kcontext.getKieRuntime()).getProcessRuntime())
                .getProcessEventSupport().fireOnMessage(kcontext.getProcessInstance(), kcontext
                        .getNodeInstance(), kcontext.getKieRuntime(), message.getName(), tVariable);
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setName("Send Task");
        workItem.setProcessInstanceId(kcontext.getProcessInstance().getId());
        workItem.setParameter("MessageType", message.getType());
        workItem.setNodeInstanceId(kcontext.getNodeInstance().getId());
        workItem.setNodeId(kcontext.getNodeInstance().getNodeId());
        workItem.setDeploymentId((String) kcontext.getKieRuntime().getEnvironment().get("deploymentId"));
        if (tVariable != null) {
            workItem.setParameter("Message", tVariable);
        }
        ((WorkItemManager) kcontext.getKieRuntime().getWorkItemManager()).internalExecuteWorkItem(workItem);
    }


    public String getVariable() {
        return varName;
    }

}
