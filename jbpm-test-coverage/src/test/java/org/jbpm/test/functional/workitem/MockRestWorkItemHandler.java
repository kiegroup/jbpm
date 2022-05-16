/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.test.functional.workitem;

import java.util.HashMap;

import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;

public class MockRestWorkItemHandler extends AbstractLogOrThrowWorkItemHandler {

    public MockRestWorkItemHandler() {
        this.handlingProcessId = null;
        this.handlingStrategy = null;
    }

    public MockRestWorkItemHandler(String handlingProcessId,
                                     String handlingStrategy) {
        this.handlingProcessId = handlingProcessId;
        this.handlingStrategy = handlingStrategy;
    }

    @Override
    public void executeWorkItem( WorkItem workItem, WorkItemManager manager ) {
        String urlStr = (String) workItem.getParameter("Url");
        if ("https://www.google.com/".equals(urlStr)) {
            manager.completeWorkItem(workItem.getId(), new HashMap<String, Object>());
        } else {
            handleException(new RuntimeException("Mock REST exception"));
        }
    }

    @Override
    public void abortWorkItem( WorkItem workItem, WorkItemManager manager ) {
        // TODO Auto-generated method stub

    }
}
