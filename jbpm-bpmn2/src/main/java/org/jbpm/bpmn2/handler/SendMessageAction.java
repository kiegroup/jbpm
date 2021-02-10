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

import java.util.HashMap;
import java.util.Map;

import org.drools.core.WorkItemHandlerNotFoundException;
import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.process.instance.WorkItemManager;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.bpmn2.core.Message;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.impl.JavaAction;
import org.jbpm.workflow.core.node.ThrowNode;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.jbpm.workflow.instance.node.EndNodeInstance;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendMessageAction implements JavaAction {

    protected static final Logger logger = LoggerFactory.getLogger(SendMessageAction.class);

    private static final long serialVersionUID = 1L;
    private String varName;
    private Message message;

    public SendMessageAction(String varName, Message message) {
        this.varName = varName;
        this.message = message;
    }

    @Override
    public void execute(ProcessContext kcontext) throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        NodeInstance nodeInstance = kcontext.getNodeInstance();
        ((NodeInstanceImpl) nodeInstance).mapInputSetVariables((target,value) -> parameters.put(target, value));

        Object tVariable = parameters.get(varName);
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

        for(Map.Entry<String,Object> entry : parameters.entrySet()) {
            workItem.setParameter(entry.getKey(), entry.getValue());
        }

        if (tVariable != null) {
            workItem.setParameter("Message", tVariable);
        }
        try {
            ((WorkItemManager) kcontext.getKieRuntime().getWorkItemManager()).internalExecuteWorkItem(workItem);
        } catch (WorkItemHandlerNotFoundException e) {
            logger.warn("No Workitem found \"Send Task\" when trying to throw a message {}", tVariable);
        }
    }

    public String getVariable() {
        return varName;
    }

}
