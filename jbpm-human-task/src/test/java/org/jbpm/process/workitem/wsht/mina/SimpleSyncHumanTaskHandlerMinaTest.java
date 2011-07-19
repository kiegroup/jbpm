/*
 * Copyright 2011 JBoss Inc..
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
import java.util.Set;
import org.drools.process.instance.impl.WorkItemImpl;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import org.jbpm.task.query.TaskSummary;
import java.util.ArrayList;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.jbpm.process.workitem.wsht.WSHumanTaskHandler;
import org.jbpm.task.OrganizationalEntity;
import org.jbpm.task.PeopleAssignments;
import org.jbpm.task.Status;
import org.jbpm.task.Task;
import org.jbpm.task.TaskData;
import org.jbpm.task.User;
import org.jbpm.task.service.impl.TaskServiceClientSyncImpl;
import org.jbpm.task.service.TaskServer;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.TaskServiceClientSync;
import org.jbpm.task.service.TaskServiceSession;
import org.jbpm.task.service.mina.MinaTaskClientConnector;
import org.jbpm.task.service.mina.MinaTaskClientHandler;
import org.jbpm.task.service.mina.MinaTaskServer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.drools.SystemEventListenerFactory;
import static org.junit.Assert.*;

/**
 *
 * @author salaboy
 */
public class SimpleSyncHumanTaskHandlerMinaTest {

    private TaskServiceClientSync client;
    private WSHumanTaskHandler handler;
    private TaskServer server;
    protected TaskService taskService;
    private EntityManagerFactory emf;
    private User user;
    private User admin;

    public SimpleSyncHumanTaskHandlerMinaTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws InterruptedException {
        emf = Persistence.createEntityManagerFactory("org.jbpm.task");
        taskService = new TaskService(emf, SystemEventListenerFactory.getSystemEventListener());
        TaskServiceSession createSession = taskService.createSession();
        
        user = new User("salaboy");
        createSession.addUser(user);
        admin = new User("Administrator");
        createSession.addUser(admin);
        
        server = new MinaTaskServer(taskService);
        new Thread(server).start();
        
        System.out.println("Waiting for the Mina Server to come up");
        while (!server.isRunning()) {
            System.out.print(".");
            Thread.sleep(50);
        }

        client = new TaskServiceClientSyncImpl(new MinaTaskClientConnector("myClient",
                new MinaTaskClientHandler(SystemEventListenerFactory.getSystemEventListener())));
        client.connect("127.0.0.1", 9123);
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("Stoping the Mina Server");
        handler.dispose();
        client.disconnect();
        server.stop();
    }

    @Test
    public void simpleAPIRemoteMinaTest() {

        handler = new WSHumanTaskHandler();

        handler.setClient(client);


        Task task = new Task();
        TaskData data = new TaskData();
        data.setActualOwner(user);
        PeopleAssignments peopleAssignments = new PeopleAssignments();
        peopleAssignments.setPotentialOwners(new ArrayList<OrganizationalEntity>() {

            {
                add(user);
            }
        });
        task.setPeopleAssignments(peopleAssignments);
        task.setTaskData(data);
        //Add the task
        client.addTask(task, null);
        assertEquals("1", task.getId().toString());

        task = client.getTask(task.getId());
        assertEquals(Status.Reserved, client.getTask(task.getId()).getTaskData().getStatus());
        assertNotNull(task);
        client.start(task.getId(), "salaboy");

        assertEquals(Status.InProgress, client.getTask(task.getId()).getTaskData().getStatus());

        client.complete(task.getId(), user.getId(), null);
        assertEquals(Status.Completed, client.getTask(task.getId()).getTaskData().getStatus());
    }

    @Test
    public void simpleAPIWithWorkItemRemoteMinaTest() throws InterruptedException {

        handler = new WSHumanTaskHandler();
        handler.setClient(client);

        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setName("Human Task");
        workItem.setParameter("TaskName", "TaskName");
        workItem.setParameter("Comment", "Comment");
        workItem.setParameter("Priority", "10");
        workItem.setParameter("ActorId", "salaboy");
        workItem.setProcessInstanceId(10);
        handler.executeWorkItem(workItem, manager);

        List<TaskSummary> tasks = client.getTasksAssignedAsPotentialOwner("salaboy", "en-UK");
        assertEquals(1, tasks.size());

        Task task = client.getTask(tasks.get(0).getId());
        assertEquals(Status.Reserved, task.getTaskData().getStatus());

        client.start(task.getId(), "salaboy");
        task = client.getTask(tasks.get(0).getId());
        assertEquals(Status.InProgress, task.getTaskData().getStatus());

        client.complete(tasks.get(0).getId(), user.getId(), null);
        task = client.getTask(tasks.get(0).getId());
        assertEquals(Status.Completed, task.getTaskData().getStatus());


        assertTrue(manager.waitTillCompleted(1000));
    }

    private class TestWorkItemManager implements WorkItemManager {

        private volatile boolean completed;
        private volatile boolean aborted;
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

        public void internalExecuteWorkItem(WorkItem workItem) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void internalAddWorkItem(WorkItem workItem) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void internalAbortWorkItem(long id) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Set<WorkItem> getWorkItems() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public WorkItem getWorkItem(long id) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void clear() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
