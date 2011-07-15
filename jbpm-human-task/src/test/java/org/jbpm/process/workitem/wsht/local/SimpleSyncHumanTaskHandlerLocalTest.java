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

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.TaskServer;
import org.jbpm.task.service.TaskServiceSession;
import org.jbpm.task.service.mina.MinaTaskServer;
import org.drools.SystemEventListenerFactory;
import org.jbpm.task.service.mina.MinaTaskClientHandler;
import org.jbpm.task.service.TaskClientImpl;
import java.util.ArrayList;
import org.jbpm.process.workitem.wsht.SyncWSHumanTaskHandler;
import org.jbpm.task.OrganizationalEntity;
import org.jbpm.task.PeopleAssignments;
import org.jbpm.task.Status;
import org.jbpm.task.Task;
import org.jbpm.task.TaskData;
import org.jbpm.task.User;
import org.jbpm.task.service.TaskServiceClient;
import org.jbpm.task.service.TaskServiceClientLocalImpl;
import org.jbpm.task.service.mina.MinaTaskClientConnector;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author salaboy
 */
public class SimpleSyncHumanTaskHandlerLocalTest {

    private TaskServiceClient client;
    private SyncWSHumanTaskHandler handler;
    private TaskServer server;
    protected TaskService taskService;
    private EntityManagerFactory emf;
    private User user;
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
        taskService = new TaskService(emf , SystemEventListenerFactory.getSystemEventListener());
        TaskServiceSession createSession = taskService.createSession();
        user = new User("salaboy");
        createSession.addUser(user);
        server = new MinaTaskServer(taskService);
        Thread thread = new Thread(server);
        thread.start();
        System.out.println("Waiting for the Mina Server to come up");
        while (!server.isRunning()) {
            System.out.print(".");
            Thread.sleep(50);
        }

    }

    @After
    public void tearDown() throws Exception {
        System.out.println("Stoping the Mina Server");
        handler.dispose();
        client.disconnect();
        server.stop();
        
    }

    @Test
    public void simpleAPILocalTest() {


        client = new TaskServiceClientLocalImpl(emf);

        handler = new SyncWSHumanTaskHandler();

        handler.setClient(client);

//        final User user = new User("salaboy");
//        client.addUser(user);



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
        client.start(task.getId(), "salaboy");
        assertEquals(Status.InProgress, task.getTaskData().getStatus());

        client.complete(task.getId(), user.getId(), null);
        assertEquals(Status.Completed, task.getTaskData().getStatus());
    }

    @Test
    public void simpleAPIRemoteTest() {


        client = new TaskClientImpl(new MinaTaskClientConnector("myClient",
                new MinaTaskClientHandler(SystemEventListenerFactory.getSystemEventListener())));
        client.connect("127.0.0.1", 9123);
        
        handler = new SyncWSHumanTaskHandler();

        handler.setClient(client);

        final User user = new User("salaboy");
        client.addUser(user);



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
}
