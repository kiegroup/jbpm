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
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.impl.JavaAction;
import org.jbpm.process.instance.impl.util.VariableUtil;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessContext;

public class SendSignalAction implements JavaAction {

    private static final long serialVersionUID = 1L;
    private String signalName;
    private String varName;
    private boolean isAsync;
    private NodeImpl node;

    public SendSignalAction(NodeImpl node, String variable, String signalName, boolean isAsync) {
        this.node = node;
        this.varName = variable;
        this.signalName = signalName;
        this.isAsync = isAsync;
    }

    @Override
    public void execute(ProcessContext kcontext) throws Exception {
        Object tVariable = VariableResolver.getVariable(kcontext, varName);
        ((InternalProcessRuntime) ((InternalKnowledgeRuntime) kcontext.getKieRuntime()).getProcessRuntime())
                .getProcessEventSupport().fireOnSignal(kcontext.getProcessInstance(), kcontext.getNodeInstance(),
                        kcontext.getKieRuntime(), signalName, tVariable);
        String scope = (String) node.getMetaData("customScope");
        String signalType = VariableUtil.resolveVariable(isAsync ? "ASYNC-" + signalName : signalName, kcontext
                .getNodeInstance());
        if ("processInstance".equalsIgnoreCase(scope)) {
            kcontext.getProcessInstance().signalEvent(signalType, tVariable);
        } else if ("runtimeManager".equalsIgnoreCase(scope) || "project".equalsIgnoreCase(scope)) {
            ((RuntimeManager) kcontext.getKieRuntime().getEnvironment().get("RuntimeManager")).signalEvent(signalType,
                    tVariable);
        } else if ("external".equalsIgnoreCase(scope)) {
            WorkItemImpl workItem = new WorkItemImpl();
            workItem.setName("External Send Task");
            workItem.setNodeInstanceId(kcontext.getNodeInstance().getId());
            workItem.setProcessInstanceId(kcontext.getProcessInstance().getId());
            workItem.setNodeId(kcontext.getNodeInstance().getNodeId());
            workItem.setDeploymentId((String) kcontext.getKieRuntime().getEnvironment().get("deploymentId"));
            workItem.setParameter("Signal", signalType);
            workItem.setParameter("SignalProcessInstanceId", kcontext.getVariable("SignalProcessInstanceId"));
            workItem.setParameter("SignalWorkItemId", kcontext.getVariable("SignalWorkItemId"));
            workItem.setParameter("SignalDeploymentId", kcontext.getVariable("SignalDeploymentId"));
            if (tVariable != null) {
                workItem.setParameter("Data", tVariable);
            }
            ((WorkItemManager) kcontext.getKieRuntime().getWorkItemManager()).internalExecuteWorkItem(workItem);
        } else {
            kcontext.getKieRuntime().signalEvent(signalType, tVariable);
        }
    }

    public String getVariable() {
        return varName;
    }

    public String getSignalName() {
        return signalName;
    }
}
