/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.process.workitem.wsht;

import java.util.List;
import java.util.Map;

import org.drools.process.instance.impl.WorkItemImpl;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import org.jbpm.task.BaseTest;
import org.jbpm.task.Status;
import org.jbpm.task.TaskService;
import org.jbpm.task.TestStatefulKnowledgeSession;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.local.LocalTaskService;

public class WSHumanTaskMultipleHandlersLocalSyncTest extends BaseTest {

    protected TestStatefulKnowledgeSession ksession = new TestStatefulKnowledgeSession();
    private TaskService client = null;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        this.client.disconnect();
        super.tearDown();
    }
    
    public void testLocalMultipleHandlers() throws Exception {
        client = new LocalTaskService(taskService);
                
        SyncWSHumanTaskHandler handler = new SyncWSHumanTaskHandler(client, ksession);
        handler.setLocal(true);
        handler.connect();
        
        
        SingleCallbackWorkItemManager manager = new SingleCallbackWorkItemManager();
        ksession.setWorkItemManager(manager);
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setName("Human Task");
        workItem.setParameter("TaskName", "TaskName");
        workItem.setParameter("Comment", "Comment");
        workItem.setParameter("Priority", "10");
        workItem.setParameter("ActorId", "Darth Vader");
        workItem.setProcessInstanceId(10);
        handler.executeWorkItem(workItem, manager);
 
        
        List<TaskSummary> tasks = client.getTasksAssignedAsPotentialOwner("Darth Vader", "en-UK");
        assertEquals(1, tasks.size());
        TaskSummary task = tasks.get(0);
        assertEquals("TaskName", task.getName());
        assertEquals(10, task.getPriority());
        assertEquals("Comment", task.getDescription());
        assertEquals(Status.Reserved, task.getStatus());
        assertEquals("Darth Vader", task.getActualOwner().getId());
        assertEquals(10, task.getProcessInstanceId());

        client.start(task.getId(), "Darth Vader");
        
        handler.dispose();

        client = new LocalTaskService(taskService);
        SyncWSHumanTaskHandler handler2 = new SyncWSHumanTaskHandler(client, ksession);
        handler2.setLocal(true);
        handler2.connect();
        
        client.complete(task.getId(), "Darth Vader", null);
        
        handler2.dispose();
        client.disconnect();
        
        assertFalse("work item manager has errors (was completed/aborted more than once)", manager.isError());
    }
    
    public void testSyncMultipleHandlers() throws Exception {
        client = new LocalTaskService(taskService);
                
        SyncWSHumanTaskHandler handler = new SyncWSHumanTaskHandler(client, ksession);
        handler.connect();
        
        
        SingleCallbackWorkItemManager manager = new SingleCallbackWorkItemManager();
        ksession.setWorkItemManager(manager);
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setName("Human Task");
        workItem.setParameter("TaskName", "TaskName");
        workItem.setParameter("Comment", "Comment");
        workItem.setParameter("Priority", "10");
        workItem.setParameter("ActorId", "Darth Vader");
        workItem.setProcessInstanceId(10);
        handler.executeWorkItem(workItem, manager);
 
        
        List<TaskSummary> tasks = client.getTasksAssignedAsPotentialOwner("Darth Vader", "en-UK");
        assertEquals(1, tasks.size());
        TaskSummary task = tasks.get(0);
        assertEquals("TaskName", task.getName());
        assertEquals(10, task.getPriority());
        assertEquals("Comment", task.getDescription());
        assertEquals(Status.Reserved, task.getStatus());
        assertEquals("Darth Vader", task.getActualOwner().getId());
        assertEquals(10, task.getProcessInstanceId());

        client.start(task.getId(), "Darth Vader");
        
        handler.dispose();

        client = new LocalTaskService(taskService);
        SyncWSHumanTaskHandler handler2 = new SyncWSHumanTaskHandler(client, ksession);
        handler2.connect();
        
        client.complete(task.getId(), "Darth Vader", null);
        
        handler2.dispose();
        client.disconnect();
        Thread.sleep(1000);
        assertFalse("work item manager has errors (was completed/aborted more than once)", manager.isError());
    }
    
    
    private class SingleCallbackWorkItemManager implements WorkItemManager {

        private volatile boolean completed;
        private volatile boolean aborted;
        private volatile boolean error;
        private volatile Map<String, Object> results;

        public synchronized boolean waitTillCompleted(long time) {
            if (!isCompleted()) {
                try {
                    wait(time);
                } catch (InterruptedException e) {
                    // swallow and return state of completed
                }
            }

            return isCompleted();
        }

        public synchronized boolean waitTillAborted(long time) {
            if (!isAborted()) {
                try {
                    wait(time);
                } catch (InterruptedException e) {
                    // swallow and return state of aborted
                }
            }

            return isAborted();
        }

        public void abortWorkItem(long id) {
            if (this.aborted || this.completed) {
                this.error = true;
                throw new RuntimeException("work item manager has errors (was completed/aborted more than once)");
            }
            setAborted(true);
        }

        public synchronized boolean isAborted() {
            return aborted;
        }

        private synchronized void setAborted(boolean aborted) {
            this.aborted = aborted;
            notifyAll();
        }

        public void completeWorkItem(long id, Map<String, Object> results) {
            if (this.aborted || this.completed) {
                this.error = true;
                throw new RuntimeException("work item manager has errors (was completed/aborted more than once)");
            }
            this.results = results;
            setCompleted(true);
        }

        private synchronized void setCompleted(boolean completed) {
            this.completed = completed;
            notifyAll();
        }

        public synchronized boolean isCompleted() {
            return completed;
        }

        public Map<String, Object> getResults() {
            return results;
        }

        public void registerWorkItemHandler(String workItemName, WorkItemHandler handler) {
        }

        public boolean isError() {
            return error;
        }

        public void setError(boolean error) {
            this.error = error;
        }
    }
}
