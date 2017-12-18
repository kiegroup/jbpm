package org.jbpm.test.wih;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

public class FirstErrorWorkItemHandler implements WorkItemHandler {

    private List<Long> processedWorkItems = new ArrayList<Long>();

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        long processInstanceId = workItem.getProcessInstanceId();
        if (!processedWorkItems.contains(processInstanceId)) {
            processedWorkItems.add(processInstanceId);
            throw new RuntimeException("Error");
        }
        manager.completeWorkItem(workItem.getId(), new HashMap<String, Object>());
        processedWorkItems.remove(processInstanceId);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        manager.abortWorkItem(workItem.getId());
    }

}
