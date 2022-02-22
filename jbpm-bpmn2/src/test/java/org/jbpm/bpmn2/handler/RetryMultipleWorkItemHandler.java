/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.bpmn2.handler;

import org.kie.api.runtime.process.ProcessWorkItemHandlerException;
import org.kie.api.runtime.process.ProcessWorkItemHandlerException.HandlingStrategy;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;


public class RetryMultipleWorkItemHandler implements WorkItemHandler {
    
    private String processId;
    private HandlingStrategy strategy;
    private int counter;
    
    private WorkItem workItem;

    public RetryMultipleWorkItemHandler(String processId, HandlingStrategy strategy) {
        super();
        this.processId = processId;
        this.strategy = strategy;
    }

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        this.workItem = workItem;
        int retries = Integer.parseInt(System.getProperty("org.jbpm.exception.handling_strategy.retry.count"));
        if (processId != null && strategy != null) {
            
            if (counter >= retries) {
                manager.completeWorkItem(workItem.getId(), workItem.getParameters());
            } else {
                counter++;
                throw new ProcessWorkItemHandlerException(processId, strategy, new RuntimeException("On purpose"));
            }
        }
        
        manager.completeWorkItem(workItem.getId(), null);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        this.workItem = workItem;

    }

    public WorkItem getWorkItem() {
        return workItem;
    }

    public int getCounter() {
        return counter;
    }
}
