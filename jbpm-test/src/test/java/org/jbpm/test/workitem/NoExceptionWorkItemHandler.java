package org.jbpm.test.workitem;

import org.jbpm.process.workitem.AbstractLogOrThrowWorkItemHandler;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;


public class NoExceptionWorkItemHandler extends AbstractLogOrThrowWorkItemHandler {

    @Override
    public void executeWorkItem( WorkItem workItem, WorkItemManager manager ) {
        // doNothing
        manager.completeWorkItem(workItem.getId(), null);
        
    }

    @Override
    public void abortWorkItem( WorkItem workItem, WorkItemManager manager ) {
        // TODO Auto-generated method stub
        
    }

}
