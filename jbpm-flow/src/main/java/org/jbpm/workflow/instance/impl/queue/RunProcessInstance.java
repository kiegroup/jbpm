package org.jbpm.workflow.instance.impl.queue;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.phreak.PropagationEntry.AbstractPropagationEntry;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.workflow.instance.NodeInstance;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.jbpm.workflow.instance.node.EventNodeInstanceInterface;
import org.kie.api.runtime.process.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunProcessInstance extends AbstractPropagationEntry {

    private static final Logger logger = LoggerFactory.getLogger(RunProcessInstance.class);

    private final Deque<ProcessActionTrigger> processActionQueue = new LinkedList<ProcessActionTrigger>();
    private boolean executing = false;

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
        } else {
            System.out.println( "X already executing" );
        }
    }

    private void internalExecute() {
        while( ! processActionQueue.isEmpty() ) {
            ProcessActionTrigger nextProcessActionTrigger = processActionQueue.pollLast();
            System.out.println( "! triggering: " + nextProcessActionTrigger.toString() );
            nextProcessActionTrigger.trigger();
        }
    }

    public void addNodeInstanceTrigger( NodeInstance nodeInstance, NodeInstance from, String type ) {
        addAction(new TriggerAction(nodeInstance, from, type));
    }

    public void addAndTriggerSignalEventAction( EventListener eventListener, String type, Object event ) {
        // override executing flag regardless of what we're doing
        boolean alreadyExecuting = executing;
        executing = true;
        eventListener.signalEvent(type, event);
        if( ! alreadyExecuting ) {
            internalExecute();
            executing = false;
        }
    }

    private void addAction( ProcessActionTrigger action ) {
        System.out.println("+ adding: " + action.toString());
        processActionQueue.add(action);
    }

    public void removeNodeInstanceTrigger( NodeInstance nodeInstance ) {
        Iterator<ProcessActionTrigger> triggerIter = processActionQueue.descendingIterator();
        while( triggerIter.hasNext() ) {
           ProcessActionTrigger trigger = triggerIter.next();
           if( trigger instanceof TriggerAction ) {
               String triggerUniqueId = trigger.getUniqueInstanceId();
               if( triggerUniqueId.equals(((NodeInstanceImpl) nodeInstance).getUniqueId()) ) {
                   triggerIter.remove();
                   break;
               }
           }
        }

    }

}
