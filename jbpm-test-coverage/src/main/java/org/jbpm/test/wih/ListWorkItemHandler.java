/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.test.wih;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListWorkItemHandler implements WorkItemHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListWorkItemHandler.class);

    private final List<WorkItem> workItems;
    private final CountDownLatch latch;

    public ListWorkItemHandler() {
        workItems = Collections.synchronizedList(new ArrayList<WorkItem>());
        latch = new CountDownLatch(0);
    }

    public ListWorkItemHandler(int threads) {
        workItems = Collections.synchronizedList(new ArrayList<WorkItem>());
        this.latch = new CountDownLatch(threads);
    }

    public CountDownLatch getCountDown() {
        return this.latch;
    }

    public Collection<WorkItem> getWorkItems() {
        synchronized (workItems) {
            return Collections.unmodifiableCollection(new ArrayList<WorkItem>(workItems));
        }
    }

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        LOGGER.debug("executing: " + workItem.getId());
        synchronized (workItems) {
            workItems.add(workItem);
            latch.countDown();
        }
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        LOGGER.debug("aborting: " + workItem.getId());
        synchronized (workItems) {
            workItems.remove(workItem);
            latch.countDown();
        }
    }

}
