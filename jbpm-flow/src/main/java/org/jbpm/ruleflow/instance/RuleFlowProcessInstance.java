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
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import org.jbpm.process.instance.ProcessImplementationPart;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.instance.NodeInstance;
import org.jbpm.workflow.instance.impl.DummyEventListener;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.jbpm.workflow.instance.impl.queue.AfterInternalTriggerAction;
import org.jbpm.workflow.instance.impl.queue.NodeInstanceTriggerAction;
import org.jbpm.workflow.instance.impl.queue.ProcessInstanceAction;
import org.jbpm.workflow.instance.impl.queue.SignalEventAction;
import org.jbpm.workflow.instance.node.queue.ComplexInternalTriggerNodeInstance;
import org.kie.api.definition.process.Node;
import org.kie.api.runtime.process.EventSignallable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuleFlowProcessInstance extends WorkflowProcessInstanceImpl {

    private static final long serialVersionUID = 510l;

    protected static final Logger logger = LoggerFactory.getLogger(RuleFlowProcessInstance.class);

    public RuleFlowProcess getRuleFlowProcess() {
        return (RuleFlowProcess) getProcess();
    }

    public void internalStart( String trigger ) {
        boolean queueBased = isQueueBased();
        
        // Queue-Based Case Management ( add actions to queue )
    	List<Node> autoStartNodes = getRuleFlowProcess().getAutoStartNodes();
    	if( ! autoStartNodes.isEmpty() ) { 
    	    ListIterator<Node> iter = autoStartNodes.listIterator(autoStartNodes.size());
    	    while( iter.hasPrevious() ) { 
    	        Node autoStartNode = iter.previous();
    	        addProcessInstanceAction(new SignalEventAction(this, autoStartNode.getName(), null));
    	    }
    	    // Not sure if this is stricly necessary.. 
    	    // But it conforms to the idea behind the queue-based architecture
    	    addNewExecutionQueueToStack(false);
    	}
    	
        StartNode startNode = getRuleFlowProcess().getStart(trigger);
        if( startNode != null ) {
            NodeInstance startNodeInstance = ((NodeInstance) createNodeInstance(startNode));
            if( queueBased ) {
                addProcessInstanceAction(new NodeInstanceTriggerAction(startNodeInstance, null, null));
            } else {
                startNodeInstance.trigger(null, null);
            }
        }
       
        if( queueBased ) { 
            // maybe just a start, maybe just case-mgmt, maybe both..
            executeQueue();
        }
       
        // Recursive Case Management 
        if( ! queueBased ) { 
            autoStartNodes.forEach(autoStartNode -> signalEvent(autoStartNode.getName(), null));
        }
    }

    protected boolean executing = false;
    protected boolean stopProcessingCurrentQueueAndGetNewestQueue = false;
    protected final Stack<Deque<ProcessInstanceAction>> processActionQueueStack = new Stack<Deque<ProcessInstanceAction>>();

    @Override
    public synchronized void executeQueue() {
        if( ! executing ) {
            executing = true;
            internalExecuteQueue();
            executing = false;
        }
    }

    /**
     * This method contains the main execution logic used in queue-based execution
     */
    protected synchronized void internalExecuteQueue() {
        while( ! processActionQueueStack.isEmpty() ) {
            Deque<ProcessInstanceAction> processActionQueue = processActionQueueStack.peek();
            stopProcessingCurrentQueueAndGetNewestQueue = false;
            while( ! processActionQueue.isEmpty() ) {
                ProcessInstanceAction nextProcessAction = processActionQueue.pollFirst();
                nextProcessAction.trigger();
                if( stopProcessingCurrentQueueAndGetNewestQueue ) {
                    break;
                }
            }
            if( ! stopProcessingCurrentQueueAndGetNewestQueue ) {
                if( ! processActionQueueStack.isEmpty() ) {
                    processActionQueueStack.pop();
                }
            }
        }
    }

    @Override
    public void abortQueueExecution() {
        stopProcessingCurrentQueueAndGetNewestQueue = false;
        processActionQueueStack.clear();
    }

    @Override
    public void addNewExecutionQueueToStack(boolean forceNewQueue) {
        // we check the boolean here so that we don't redundantly add new queues when only 1 new queue is needed
        if( ! stopProcessingCurrentQueueAndGetNewestQueue || forceNewQueue ) {
            processActionQueueStack.add(new LinkedList<>());
            stopProcessingCurrentQueueAndGetNewestQueue = true;
        }
    }

    @Override
    public void addNodeInstanceTriggerAction(NodeInstance nodeInstance, NodeInstance from, String type) {
        addProcessInstanceAction(new NodeInstanceTriggerAction(nodeInstance, from, type));
    }

    @Override
    public void addSignalEventAction( EventSignallable eventSignallable, String type, Object event ) {
        if( eventSignallable instanceof DummyEventListener ) {
            return;
        }
        addProcessInstanceAction(new SignalEventAction(eventSignallable, type, event));
    }

    @Override
    public void addAfterInternalTriggerAction( ComplexInternalTriggerNodeInstance nodeInstance ) {
        addProcessInstanceAction(new AfterInternalTriggerAction(nodeInstance));
    }

    @Override
    public void addProcessInstanceAction(ProcessInstanceAction action) {
        Deque<ProcessInstanceAction> processInstActionQueue = processActionQueueStack.peek();
        processInstActionQueue.addFirst(action);;
    }

    @Override
    public void removeAssociatedAction(ProcessImplementationPart implInstance) {
        Iterator<Deque<ProcessInstanceAction>> queueIter = processActionQueueStack.iterator();
        while( queueIter.hasNext() ) {
            Iterator<ProcessInstanceAction> actionIter = queueIter.next().iterator();
            while( actionIter.hasNext() ) {
                ProcessInstanceAction trigger = actionIter.next();
                if( trigger.actsOn(implInstance) ) {
                    actionIter.remove();
                }
            }
        }

    }

}