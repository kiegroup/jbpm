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
package org.jbpm.process.workitem.wsht.mina;

import java.util.List;
import java.util.Map;

import org.drools.SystemEventListenerFactory;
import org.drools.process.instance.impl.WorkItemImpl;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import org.jbpm.process.workitem.wsht.AsyncWSHumanTaskHandler;
import org.jbpm.process.workitem.wsht.CommandBasedWSHumanTaskHandler;
import org.jbpm.task.BaseTest;
import org.jbpm.task.Status;
import org.jbpm.task.TestStatefulKnowledgeSession;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.TaskClient;
import org.jbpm.task.service.TaskServer;
import org.jbpm.task.service.mina.MinaTaskClientConnector;
import org.jbpm.task.service.mina.MinaTaskClientHandler;
import org.jbpm.task.service.mina.MinaTaskServer;
import org.jbpm.task.service.responsehandlers.BlockingTaskOperationResponseHandler;
import org.jbpm.task.service.responsehandlers.BlockingTaskSummaryResponseHandler;

public class WSHumanTaskMultipleHandlersMinaTest extends BaseTest {

    protected TestStatefulKnowledgeSession ksession = new TestStatefulKnowledgeSession();
    private TaskClient client = null;
    private TaskServer server;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        server = new MinaTaskServer(taskService);
        logger.debug("Waiting for the MinaTask Server to come up");
        try {
            startTaskServerThread(server, false);
        } catch (Exception e) {
            startTaskServerThread(server, true);
        }
        
        
        
    }

    protected void tearDown() throws Exception {
        this.client.disconnect();
        server.stop();
        super.tearDown();
    }
    
    public void testAsyncMultipleHandlers() throws Exception {
        client = new TaskClient(new MinaTaskClientConnector("client 1",
                new MinaTaskClientHandler(SystemEventListenerFactory.getSystemEventListener())));
                
        SingleCallbackWorkItemManager manager = new SingleCallbackWorkItemManager();
        
        AsyncWSHumanTaskHandler handler = new AsyncWSHumanTaskHandler(client, ksession);
        handler.setConnection("127.0.0.1", 9123);
        handler.setManager(manager);
        
        ksession.setWorkItemManager(manager);
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setName("Human Task");
        workItem.setParameter("TaskName", "TaskName");
        workItem.setParameter("Comment", "Comment");
        workItem.setParameter("Priority", "10");
        workItem.setParameter("ActorId", "Darth Vader");
        workItem.setProcessInstanceId(10);
        handler.executeWorkItem(workItem, manager);
 
        
        Thread.sleep(500);

        BlockingTaskSummaryResponseHandler responseHandler = new BlockingTaskSummaryResponseHandler();
        client.getTasksAssignedAsPotentialOwner("Darth Vader", "en-UK", responseHandler);
        List<TaskSummary> tasks = responseHandler.getResults();
        assertEquals(1, tasks.size());
        TaskSummary task = tasks.get(0);
        assertEquals("TaskName", task.getName());
        assertEquals(10, task.getPriority());
        assertEquals("Comment", task.getDescription());
        assertEquals(Status.Reserved, task.getStatus());
        assertEquals("Darth Vader", task.getActualOwner().getId());
        assertEquals(10, task.getProcessInstanceId());

        BlockingTaskOperationResponseHandler operationResponseHandler = new BlockingTaskOperationResponseHandler();
        client.start(task.getId(), "Darth Vader", operationResponseHandler);
        operationResponseHandler.waitTillDone(5000);
        
        handler.dispose();

        client = new TaskClient(new MinaTaskClientConnector("client 2",
                new MinaTaskClientHandler(SystemEventListenerFactory.getSystemEventListener())));
        AsyncWSHumanTaskHandler handler2 = new AsyncWSHumanTaskHandler(client, ksession);
        handler2.setConnection("127.0.0.1", 9123);
        handler2.setManager(manager);
        handler2.connect();
        Thread.sleep(500);
        operationResponseHandler = new BlockingTaskOperationResponseHandler();
        client.complete(task.getId(), "Darth Vader", null, operationResponseHandler);
        operationResponseHandler.waitTillDone(5000);
        Thread.sleep(2000);
        handler2.dispose();
        client.disconnect();
        
        assertFalse(manager.isError());
    }
    
    public void testComandMultipleHandlers() throws Exception {
        client = new TaskClient(new MinaTaskClientConnector("client 4",
                new MinaTaskClientHandler(SystemEventListenerFactory.getSystemEventListener())));
        client.connect("127.0.0.1", 9123);
                
        SingleCallbackWorkItemManager manager = new SingleCallbackWorkItemManager();
        
        CommandBasedWSHumanTaskHandler handler = new CommandBasedWSHumanTaskHandler(ksession);
        handler.setConnection("127.0.0.1", 9123);
        
        ksession.setWorkItemManager(manager);
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setName("Human Task");
        workItem.setParameter("TaskName", "TaskName");
        workItem.setParameter("Comment", "Comment");
        workItem.setParameter("Priority", "10");
        workItem.setParameter("ActorId", "Darth Vader");
        workItem.setProcessInstanceId(10);
        handler.executeWorkItem(workItem, manager);
 
        
        Thread.sleep(500);

        BlockingTaskSummaryResponseHandler responseHandler = new BlockingTaskSummaryResponseHandler();
        client.getTasksAssignedAsPotentialOwner("Darth Vader", "en-UK", responseHandler);
        List<TaskSummary> tasks = responseHandler.getResults();
        assertEquals(1, tasks.size());
        TaskSummary task = tasks.get(0);
        assertEquals("TaskName", task.getName());
        assertEquals(10, task.getPriority());
        assertEquals("Comment", task.getDescription());
        assertEquals(Status.Reserved, task.getStatus());
        assertEquals("Darth Vader", task.getActualOwner().getId());
        assertEquals(10, task.getProcessInstanceId());

        BlockingTaskOperationResponseHandler operationResponseHandler = new BlockingTaskOperationResponseHandler();
        client.start(task.getId(), "Darth Vader", operationResponseHandler);
        operationResponseHandler.waitTillDone(5000);
        
        handler.dispose();
        client.disconnect();

        client = new TaskClient(new MinaTaskClientConnector("client 5",
                new MinaTaskClientHandler(SystemEventListenerFactory.getSystemEventListener())));
        client.connect("127.0.0.1", 9123);
        Thread.sleep(500);
        
        CommandBasedWSHumanTaskHandler handler2 = new CommandBasedWSHumanTaskHandler(ksession);
        handler2.setConnection("127.0.0.1", 9123);
        handler2.connect();
        Thread.sleep(500);
        
        operationResponseHandler = new BlockingTaskOperationResponseHandler();
        client.complete(task.getId(), "Darth Vader", null, operationResponseHandler);
        operationResponseHandler.waitTillDone(5000);
        Thread.sleep(2000);
        handler2.dispose();
        client.disconnect();
        
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
                return;
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
                return;
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
