/**
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.ruleflow.instance;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.impl.StatefulKnowledgeSessionImpl;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.process.instance.context.exception.ExceptionScopeInstance;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.instance.NodeInstance;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.jbpm.workflow.instance.impl.queue.NodeInstanceTriggerAction;
import org.jbpm.workflow.instance.impl.queue.ProcessInstanceAction;
import org.jbpm.workflow.instance.impl.queue.RunProcessInstance;
import org.jbpm.workflow.instance.node.EventNodeInstanceInterface;
import org.jbpm.workflow.instance.node.EntryActionExceptionHandlingNodeInstance;
import org.kie.api.runtime.process.EventListener;
import org.kie.api.runtime.process.EventSignallable;

public class RuleFlowProcessInstance extends WorkflowProcessInstanceImpl {

    private static final long serialVersionUID = 510l;

    private transient RunProcessInstance runProcessInstance = new RunProcessInstance();

    public RuleFlowProcessInstance() {
    }

    public RuleFlowProcess getRuleFlowProcess() {
        return (RuleFlowProcess) getProcess();
    }

    // STACKLESS

    public void internalStart( String trigger ) {
        StartNode startNode = getRuleFlowProcess().getStart(trigger);
        if( startNode != null ) {
            NodeInstance startNodeInstance = ((NodeInstance) createNodeInstance(startNode));
            if( isStackless() ) {
                runProcessInstance.addProcessActionAtBegin(new NodeInstanceTriggerAction(startNodeInstance, null, null));
                executeProcessInstance(runProcessInstance);
            } else {
                startNodeInstance.trigger(null, null);
            }
        }
    }

    // purely adding instances to an EXECUTING action

    @Override
    public void addNodeInstanceTrigger( NodeInstance nodeInstance, NodeInstance from, String type ) {
        runProcessInstance.addNodeInstanceTrigger(nodeInstance, from, type);
    }

    @Override
    public void addSignalEventAction( EventSignallable eventSignallable, String type, Object event ) {
        runProcessInstance.addSignalEventAction(eventSignallable, type, event);

    }

    @Override
    public void addAfterInternalTriggerAction( NodeInstance nodeInstance ) {
        runProcessInstance.addAfterInternalTriggerAction( nodeInstance);
    }

    @Override
    public void addProcessInstanceAction(ProcessInstanceAction processAction) {
        runProcessInstance.addAction(processAction);
    }

    @Override
    public void removeNodeInstanceTrigger( NodeInstance nodeInstance ) {

    }

    // TRIGGER EXECUTION

    @Override
    public void addNewExecutionQueueToStack(boolean forceNewQueue) {
        runProcessInstance.addNewQueue( forceNewQueue );
    }

    @Override
    public void triggerCompletedAndExecute(EventNodeInstanceInterface eventNodeInstance) {
        System.out.println( "| " + eventNodeInstance.getClass().getSimpleName() + ".triggerCompleted()" );
        eventNodeInstance.triggerCompleted();
        executeProcessInstance(runProcessInstance);
    }

    @Override
    // TODO: use this instead of triggerCompletedAndExecute
    public void executeQueue() {
        executeProcessInstance(runProcessInstance);
    }

    // DBG
    private final boolean debug = true;

    private void executeProcessInstance(RunProcessInstance runProcessInstance) {
        if( debug ) {
            runProcessInstance.execute();
        } else {
            ((StatefulKnowledgeSessionImpl) getKnowledgeRuntime()).addPropagation(runProcessInstance);
            InternalKnowledgeRuntime kRuntime = getKnowledgeRuntime();
            try {
                kRuntime.startOperation();
                kRuntime.executeQueuedActions();
            } finally {
                kRuntime.endOperation();
            }
        }
    }

}