package org.jbpm.process.instance.impl;

import org.drools.core.WorkItemHandlerNotFoundException;
import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.process.instance.WorkItem;
import org.drools.core.process.instance.impl.DefaultWorkItemManager;
import org.jbpm.workflow.instance.ProcessInstanceActionQueueExecutor;
import org.jbpm.workflow.instance.impl.queue.RemoveWorkItemAction;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItemHandler;

/**
 * This class is a jBPM-specific work item manager that should be used whenever
 * 1. jBPM is in the classpath
 * 2. and persistence is *not* being used.
 */
public class ProcessInstanceWorkItemManager extends DefaultWorkItemManager implements WorkItemRemover {

    public ProcessInstanceWorkItemManager(InternalKnowledgeRuntime kruntime) {
        super(kruntime);
    }

    @Override
    protected void signalEventAndRemoveWorkItem(ProcessInstance processInstance, String event, WorkItem workItem) {
        // process instance may have finished already
        if( processInstance != null ) {
            if( ((org.jbpm.process.instance.ProcessInstance) processInstance).isStackless() ) {
                ((ProcessInstanceActionQueueExecutor) processInstance).addProcessInstanceAction(new RemoveWorkItemAction(this, workItem.getId()));
//                ((ProcessInstanceActionQueueExecutor) processInstance).addNewExecutionQueueToStack(true);
                processInstance.signalEvent(event, workItem);
            } else {
                processInstance.signalEvent(event, workItem);
                removeWorkItem(workItem.getId());
            }
        } else {
            removeWorkItem(workItem.getId());
        }
    }

    @Override
    protected void internalAbortAndRemoveWorkItem( WorkItemHandler handler, WorkItem workItem ) {
        ProcessInstance processInstance = kruntime.getProcessInstance(workItem.getProcessInstanceId());
        if( processInstance == null || ! ((org.jbpm.process.instance.ProcessInstance) processInstance).isStackless() ) {
            if (handler != null) {
                handler.abortWorkItem(workItem, this);
            }
            removeWorkItem(workItem.getId());
        } else {
            // queue-based logic demans that the work item is removed via the queue and NOT immediately
            ((ProcessInstanceActionQueueExecutor) processInstance).addProcessInstanceAction(new RemoveWorkItemAction(this, workItem.getId()));
            if (handler != null) {
                handler.abortWorkItem(workItem, this);
            }
        }
        if( handler == null ) {
            throw new WorkItemHandlerNotFoundException( "Could not find work item handler for " + workItem.getName(),
                    workItem.getName() );
        }
    }

    @Override
    public void removeWorkItem(Object workItemInfo, long workItemId) {
        removeWorkItem(workItemId);
    }

    @Override
    public void removeWorkItem(long workItemId) {
        workItems.remove(workItemId);
    }
}
