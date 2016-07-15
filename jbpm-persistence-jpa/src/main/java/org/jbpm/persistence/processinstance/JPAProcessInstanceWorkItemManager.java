package org.jbpm.persistence.processinstance;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.process.instance.WorkItem;
import org.drools.persistence.PersistenceContext;
import org.drools.persistence.PersistenceContextManager;
import org.drools.persistence.info.WorkItemInfo;
import org.drools.persistence.jpa.processinstance.JPAWorkItemManager;
import org.jbpm.process.instance.impl.WorkItemRemover;
import org.jbpm.workflow.instance.ProcessInstanceActionQueueExecutor;
import org.jbpm.workflow.instance.impl.queue.RemoveWorkItemAction;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.process.ProcessInstance;

/**
 * This class is a jBPM-specific work item manager that should be used whenever
 * 1. jBPM is in the classpath
 * 2. and persistence is *not* being used.
 */
public class JPAProcessInstanceWorkItemManager extends JPAWorkItemManager implements WorkItemRemover {

    public JPAProcessInstanceWorkItemManager(InternalKnowledgeRuntime kruntime) {
        super(kruntime);
    }

    @Override
    protected void signalEventAndRemoveWorkItem(ProcessInstance processInstance, String event, WorkItem workItem, WorkItemInfo workItemInfo) {
        // process instance may have finished already
        if (processInstance != null) {
            if( ((org.jbpm.process.instance.ProcessInstance) processInstance).isStackless() ) {
                ((ProcessInstanceActionQueueExecutor) processInstance).addNewExecutionQueueToStack( true );
                ((ProcessInstanceActionQueueExecutor) processInstance).addProcessInstanceAction(new RemoveWorkItemAction(this, workItem.getId(), workItemInfo));
                processInstance.signalEvent(event, workItem);
            } else {
                processInstance.signalEvent(event, workItem);
                removeWorkItem(workItemInfo, workItem.getId());
            }
        } else {
            removeWorkItem(workItemInfo, workItem.getId());
        }
    }

    @Override
    public void removeWorkItem(long workItemId) {
        WorkItemInfo workItemInfo = getWorkItemInfo(workItemId);
        removeWorkItem(workItemInfo, workItemId);
    }

    @Override
    public void removeWorkItem(Object workItemInfo, long workItemId) {
        Environment env = this.kruntime.getEnvironment();
        PersistenceContext context = ((PersistenceContextManager) env.get( EnvironmentName.PERSISTENCE_CONTEXT_MANAGER )).getCommandScopedPersistenceContext();

        context.remove((WorkItemInfo) workItemInfo);
        if (workItems != null) {
            workItems.remove(workItemId);
        }
    }
}
