package org.jbpm.workflow.instance.impl.queue;

import org.jbpm.process.instance.ProcessImplementationPart;
import org.jbpm.process.instance.impl.WorkItemRemover;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;

public class RemoveWorkItemAction implements ProcessInstanceAction {

    private WorkItemRemover workItemManager;
    private long workItemId;

    public RemoveWorkItemAction(WorkItemRemover workItemRemover, long workItemId) {
       this.workItemManager = workItemRemover;
       this.workItemId = workItemId;
    }

    @Override
    public void trigger() {
        workItemManager.removeWorkItem(workItemId);
    }

    @Override
    public boolean actsOn(ProcessImplementationPart instance) {
        if( instance instanceof WorkItemNodeInstance ) {
            return this.workItemId == ((WorkItemNodeInstance) instance).getWorkItemId();
        }
        return false;
    }

    @Override
    public String toString() {
        return workItemManager.getClass().getSimpleName() + ".removeWorkItem("
                + workItemId + ")";
    }

}
