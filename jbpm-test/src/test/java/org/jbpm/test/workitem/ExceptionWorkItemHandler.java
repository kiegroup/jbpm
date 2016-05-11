package org.jbpm.test.workitem;

import org.jbpm.process.workitem.AbstractLogOrThrowWorkItemHandler;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;


public class ExceptionWorkItemHandler extends AbstractLogOrThrowWorkItemHandler  {

    @Override
    public void executeWorkItem( WorkItem workItem, WorkItemManager manager ) {
        String name = workItem.getParameter( "name" ).toString();
        if ("xiabai".equals( name ) ){
            manager.completeWorkItem(workItem.getId(), null);
        }
        else {
            throwExceptionSoThatWorkItemIsNOTCompleted(workItem);
        }
        
    }

    @Override
    public void abortWorkItem( WorkItem workItem, WorkItemManager manager ) {
        // TODO Auto-generated method stub
        
    }
    
    private void throwExceptionSoThatWorkItemIsNOTCompleted(WorkItem workItem) {
        throw new RuntimeException("Did not complete work item " + workItem.getName() + "/" + workItem.getId()
            + " from node " + ((org.drools.core.process.instance.WorkItem) workItem).getNodeId());
    }

}
