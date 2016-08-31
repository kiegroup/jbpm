package org.jbpm.persistence.processinstance;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.process.instance.WorkItem;
import org.drools.persistence.info.WorkItemInfo;
import org.drools.persistence.jpa.processinstance.JPAWorkItemManager;
import org.jbpm.process.instance.impl.WorkItemRemover;
import org.jbpm.workflow.instance.ProcessInstanceActionQueueExecutor;
import org.jbpm.workflow.instance.impl.queue.RemoveWorkItemAction;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItemHandler;
import org.slf4j.LoggerFactory;

/**
 * This class is a jBPM-specific work item manager that should be used whenever
 * 1. jBPM is in the classpath
 * 2. and persistence *is* being used.
 */
public class JPAProcessInstanceWorkItemManager extends JPAWorkItemManager implements WorkItemRemover {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JPAProcessInstanceWorkItemManager.class);

    public JPAProcessInstanceWorkItemManager(InternalKnowledgeRuntime kruntime) {
        super(kruntime);
    }

    @Override
    protected void signalEventAndRemoveWorkItem(ProcessInstance processInstance, String event, WorkItem workItem, WorkItemInfo workItemInfo) {
        // process instance may have finished already
        if (processInstance != null) {
            if( ((org.jbpm.process.instance.ProcessInstance) processInstance).isQueueBased() ) {
                ((ProcessInstanceActionQueueExecutor) processInstance).addNewExecutionQueueToStack( true );
                ((ProcessInstanceActionQueueExecutor) processInstance).addProcessInstanceAction(new RemoveWorkItemAction(this, workItem.getId()));
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
    protected void internalAbortAndRemoveWorkItem( WorkItemHandler handler, WorkItemInfo workItemInfo, WorkItem workItem ) {
        ProcessInstance processInstance = kruntime.getProcessInstance(workItem.getProcessInstanceId());
        if( processInstance == null || ! ((org.jbpm.process.instance.ProcessInstance) processInstance).isQueueBased() ) {
            if (handler != null) {
                handler.abortWorkItem(workItem, this);
            }
            removeWorkItem(workItem.getId());
        } else {
            // queue-based logic demands that the work item is removed via the queue and NOT immediately
            ProcessInstanceActionQueueExecutor queueExecutor = (ProcessInstanceActionQueueExecutor) processInstance;
            queueExecutor.addNewExecutionQueueToStack(false); // in the case that cancel/abort is called..
            queueExecutor.addProcessInstanceAction(new RemoveWorkItemAction(this, workItem.getId()));
            // in the case that cance/abort is called, the queue is not executing yet..
            queueExecutor.executeQueue();

            if (handler != null) {
                handler.abortWorkItem(workItem, this);
            }
        }
        if( handler == null ) {
            /** This is the default logic we've been using since.. forever?
             *
             * Basically, the use case  here is that a WorkItemNodeInstance is cancelled
             * 1. After the process instance has been restored (via persistence)
             * 2. while the WorkItemHandler has *not* been (re)registered
             *
             * In which case, we basically said "Oo.. it's being cancelled anyways, no need to throw
             * an exception.."
             *
             *In order to preserve the default behavior, I've left this in..
             */
            if( workItems != null ) {
                throwWorkItemHandlerNotFoundException(workItem);
            } else {
                // Added because well, some users might want to know this..
                logger.error("Unable to abort work item \"" + workItem.getName() + "\" because no WorkItemHandler implementation is registered for it");
            }
        }
    }

    @Override
    public void removeWorkItem(long workItemId) {
        WorkItemInfo workItemInfo = getWorkItemInfo(workItemId);
        if( workItemInfo != null ) {
            super.removeWorkItem(workItemInfo, workItemId);
        }
    }

}
