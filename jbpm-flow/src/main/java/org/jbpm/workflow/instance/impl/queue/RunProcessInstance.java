package org.jbpm.workflow.instance.impl.queue;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.phreak.PropagationEntry.AbstractPropagationEntry;
import org.jbpm.workflow.instance.NodeInstance;
import org.jbpm.workflow.instance.impl.DummyEventListener;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.jbpm.workflow.instance.node.EntryActionExceptionHandlingNodeInstance;
import org.kie.api.runtime.process.EventSignallable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunProcessInstance extends AbstractPropagationEntry {

    private static final Logger logger = LoggerFactory.getLogger(RunProcessInstance.class);

    private final Stack<Deque<ProcessInstanceAction>> processActionQueueStack = new Stack<Deque<ProcessInstanceAction>>();
    private boolean executing = false;
    private boolean newQueueAddedToStack = false;

    public RunProcessInstance() {
    }

    @Override
    public void execute( InternalWorkingMemory wm ) {
        execute();
    }

    public void execute() {
        if( ! executing ) {
            executing = true;
            internalExecute();
            executing = false;
        }
    }

    private void internalExecute() {
        while( ! processActionQueueStack.isEmpty() ) {
            Deque<ProcessInstanceAction> processActionQueue = processActionQueueStack.peek();
            newQueueAddedToStack = false;
            while( ! processActionQueue.isEmpty() ) {
                ProcessInstanceAction nextProcessAction = processActionQueue.pollLast();
                log( "! [" + nextProcessAction.toString() );
                nextProcessAction.trigger();
                log( "]" );
                if( newQueueAddedToStack ) {
                    break;
                }
            }
            if( ! newQueueAddedToStack ) {
                log( processActionQueueStack.size() + ")");
                processActionQueueStack.pop();
            }
        }
    }

    public void addProcessActionAtBegin( NodeInstanceTriggerAction action ) {
        log("+ adding as first: " + action.toString());
        internalAddAction(action);
    }

    public void addNodeInstanceTrigger( NodeInstance nodeInstance, NodeInstance from, String type ) {
        addAction(new NodeInstanceTriggerAction(nodeInstance, from, type));
    }

    public void addSignalEventAction( EventSignallable eventSignallable, String type, Object event ) {
        if( eventSignallable instanceof DummyEventListener ) {
            return;
        }
        addAction(new SignalEventAction(eventSignallable, type, event));
    }

    public void addAfterInternalTriggerAction( NodeInstance nodeInstance ) {
        addAction(new AfterInternalTriggerAction(nodeInstance));
    }

    public void addAfterExceptionHandleAction( EntryActionExceptionHandlingNodeInstance nodeInstance, NodeInstance from, String type ) {
        addAction(new AfterEntryActionsAction(nodeInstance, from, type));
    }

    public void addAction( ProcessInstanceAction action ) {
        log("+ adding: " + action.toString());

        // DBG
        assert action != null : "Added null process action to queue?1?";
        internalAddAction(action);
    }

    private void internalAddAction( ProcessInstanceAction action ) {
        Deque<ProcessInstanceAction> processInstActionQueue = processActionQueueStack.peek();
        processInstActionQueue.addFirst(action);
    }

    public void addNewQueue(boolean forceNewQueue) {
        if( ! newQueueAddedToStack || forceNewQueue ) {
            processActionQueueStack.add(new LinkedList<>());
            newQueueAddedToStack = true;
            log( "(" + processActionQueueStack.size() );
        }
    }

    public void removeNodeInstanceTrigger( NodeInstance nodeInstance ) {
        Iterator<Deque<ProcessInstanceAction>> triggerIter = processActionQueueStack.iterator();
        while( triggerIter.hasNext() ) {
            Iterator<ProcessInstanceAction> queueIter = triggerIter.next().iterator();
            while( queueIter.hasNext() ) {
                ProcessInstanceAction trigger = queueIter.next();
                if( trigger instanceof NodeInstanceTriggerAction ) {
                    String triggerUniqueId = trigger.getUniqueInstanceId();
                    if( triggerUniqueId.equals(((NodeInstanceImpl) nodeInstance).getUniqueId()) ) {
                        triggerIter.remove();
                        break;
                    }
                }
            }
        }

    }

    private void log(String message) {
        System.out.println( message );
    }
}
