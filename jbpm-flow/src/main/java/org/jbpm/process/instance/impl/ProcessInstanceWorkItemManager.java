package org.jbpm.process.instance.impl;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.process.instance.WorkItem;
import org.drools.core.process.instance.impl.DefaultWorkItemManager;
import org.jbpm.workflow.instance.ProcessInstanceActionQueueExecutor;
import org.jbpm.workflow.instance.impl.queue.RemoveWorkItemAction;
import org.kie.api.runtime.process.ProcessInstance;

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
    public void removeWorkItem(Object workItemInfo, long workItemId) {
        removeWorkItem(workItemId);
    }

    @Override
    public void removeWorkItem(long workItemId) {
        workItems.remove(workItemId);
    }
}
