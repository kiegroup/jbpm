package org.jbpm.workflow.instance.impl.queue;

import org.jbpm.process.instance.impl.WorkItemRemover;

public class RemoveWorkItemAction implements ProcessInstanceAction {

    private WorkItemRemover workItemManager;
    private long workItemId;
    private transient Object workItemInfo;

    public RemoveWorkItemAction(WorkItemRemover workItemRemover, long workItemId) {
       this.workItemManager = workItemRemover;
       this.workItemId = workItemId;
    }

    public RemoveWorkItemAction(WorkItemRemover workItemRemover, long workItemId, Object workItemInfo) {
       this(workItemRemover, workItemId);
       this.workItemInfo = workItemInfo;
    }

    @Override
    public void trigger() {
        if( workItemInfo != null ) {
            workItemManager.removeWorkItem(workItemInfo, workItemId);
        } else {
            workItemManager.removeWorkItem(workItemId);
        }
    }

    @Override
    public String getUniqueInstanceId() {
        // DBG Auto-generated method stub
        return null;
    }

}
