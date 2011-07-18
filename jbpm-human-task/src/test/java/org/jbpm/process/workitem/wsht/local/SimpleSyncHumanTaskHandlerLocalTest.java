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
package org.jbpm.process.workitem.wsht.local;

import org.jbpm.process.workitem.wsht.AsyncWSHumanTaskHandler;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import java.util.List;
import java.util.Set;
import org.drools.process.instance.WorkItem;
import org.jbpm.task.query.TaskSummary;
import java.util.Map;
import org.drools.process.instance.impl.WorkItemImpl;
import javax.persistence.Persistence;
import javax.persistence.EntityManagerFactory;

import java.util.ArrayList;
import org.jbpm.process.workitem.wsht.WSHumanTaskHandler;
import org.jbpm.task.OrganizationalEntity;
import org.jbpm.task.PeopleAssignments;
import org.jbpm.task.Status;
import org.jbpm.task.Task;
import org.jbpm.task.TaskData;
import org.jbpm.task.User;
import org.jbpm.task.service.TaskServiceClientSync;
import org.jbpm.task.service.impl.TaskServiceClientSyncLocalImpl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * This test was created to test the APIs and to improve the user experience
 * There is no reason and no way to implement an Async Local Client (yet)
 * That's why this test create a Sync task Client and a Sync WSHumanTaskHandler
 * @author salaboy
 */
public class SimpleSyncHumanTaskHandlerLocalTest {
    // Sync Client
    private TaskServiceClientSync client;
    //Sync Handler
    private WSHumanTaskHandler handler;
    
  
    
    private EntityManagerFactory emf;
    private User user;
    private User admin;

    public SimpleSyncHumanTaskHandlerLocalTest() {
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
        
        //Sync Local Client
        client = new TaskServiceClientSyncLocalImpl(emf);
        
        //Create One User and One Administrator
        user = new User("salaboy");
        client.addUser(user);
        admin = new User("Administrator");
        client.addUser(admin);


    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void simpleAPILocalTest() throws InterruptedException, InterruptedException {
       
         //Creating the Task
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
        assertEquals(Status.Reserved, task.getTaskData().getStatus());
        assertNotNull(task);
        
        //Start the task
        client.start(task.getId(), "salaboy");
        task = client.getTask(task.getId());
        assertEquals(Status.InProgress, task.getTaskData().getStatus());
        //Complete the task
        client.complete(task.getId(), user.getId(), null);
        task = client.getTask(task.getId());
        assertEquals(Status.Completed, task.getTaskData().getStatus());
    }

    @Test
    public void simpleAPIWithWorkItemLocalTest() throws InterruptedException {

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
