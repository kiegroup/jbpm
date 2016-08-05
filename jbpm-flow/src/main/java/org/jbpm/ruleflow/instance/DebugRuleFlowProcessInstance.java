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

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.impl.StatefulKnowledgeSessionImpl;
import org.drools.core.phreak.PropagationEntry.AbstractPropagationEntry;
import org.jbpm.process.instance.ProcessImplementationPart;
import org.jbpm.process.instance.impl.ProcessInstanceImpl;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.instance.NodeInstance;
import org.jbpm.workflow.instance.ProcessInstanceActionQueueExecutor;
import org.jbpm.workflow.instance.impl.DummyEventListener;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.jbpm.workflow.instance.impl.queue.AfterInternalTriggerAction;
import org.jbpm.workflow.instance.impl.queue.NodeInstanceTriggerAction;
import org.jbpm.workflow.instance.impl.queue.ProcessInstanceAction;
import org.jbpm.workflow.instance.impl.queue.SignalEventAction;
import org.jbpm.workflow.instance.node.queue.ComplexInternalTriggerNodeInstance;
import org.kie.api.runtime.process.EventSignallable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugRuleFlowProcessInstance extends RuleFlowProcessInstance {

    private static final long serialVersionUID = 610L;

    protected void internalExecuteQueue() {
        while( ! processActionQueueStack.isEmpty() ) {
            Deque<ProcessInstanceAction> processActionQueue = processActionQueueStack.peek();
            stopProcessingCurrentQueueAndGetNewestQueue = false;
            while( ! processActionQueue.isEmpty() ) {
                ProcessInstanceAction nextProcessAction = processActionQueue.pollFirst();
                log( "! [" + nextProcessAction.toString() );
                nextProcessAction.trigger();
                log( "  ]" );
                if( stopProcessingCurrentQueueAndGetNewestQueue ) {
                    break;
                }
            }
            if( ! stopProcessingCurrentQueueAndGetNewestQueue ) {
                log( processActionQueueStack.size() + ")");
                processActionQueueStack.pop();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.jbpm.workflow.instance.ProcessInstanceActionQueueExecutor#addProcessInstanceAction(org.jbpm.workflow.instance.impl.queue.ProcessInstanceAction)
     */
    @Override
    public void addProcessInstanceAction(ProcessInstanceAction action) {
        log("+ adding: " + action.toString());

        assert action != null : "Added null process action to queue?1?";
        Deque<ProcessInstanceAction> processInstActionQueue = processActionQueueStack.peek();
        processInstActionQueue.addFirst(action);
    }

    /* (non-Javadoc)
     * @see org.jbpm.workflow.instance.ProcessInstanceActionQueueExecutor#removeAssociatedAction(org.jbpm.process.instance.ProcessImplementationPart)
     */
    @Override
    public void removeAssociatedAction(ProcessImplementationPart implInstance) {
        Iterator<Deque<ProcessInstanceAction>> queueIter = processActionQueueStack.iterator();
        while( queueIter.hasNext() ) {
            Iterator<ProcessInstanceAction> actionIter = queueIter.next().iterator();
            while( actionIter.hasNext() ) {
                ProcessInstanceAction trigger = actionIter.next();
                if( trigger.actsOn(implInstance) ) {
                    log("- removing: " + trigger);
                    actionIter.remove();
                }
            }
        }

    }

    /* (non-Javadoc)
     * @see org.jbpm.workflow.instance.ProcessInstanceActionQueueExecutor#addNewExecutionQueueToStack(boolean)
     */
    @Override
    public void addNewExecutionQueueToStack(boolean forceNewQueue) {
        if( ! stopProcessingCurrentQueueAndGetNewestQueue || forceNewQueue ) {
            processActionQueueStack.add(new LinkedList<>());
            stopProcessingCurrentQueueAndGetNewestQueue = true;
            log( "(" + processActionQueueStack.size() );
        }
    }

    // DBG
    private void log(String message) {
        System.out.println( message );
    }

}