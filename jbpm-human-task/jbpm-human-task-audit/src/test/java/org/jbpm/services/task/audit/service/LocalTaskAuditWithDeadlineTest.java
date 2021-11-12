/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.services.task.audit.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.drools.core.time.TimerService;
import org.drools.core.time.impl.JDKTimerService;
import org.jbpm.process.core.timer.TimerServiceRegistry;
import org.jbpm.services.task.HumanTaskServiceFactory;
import org.jbpm.services.task.HumanTaskServicesBaseTest;
import org.jbpm.services.task.MvelFilePath;
import org.jbpm.services.task.TaskServiceRegistry;
import org.jbpm.services.task.audit.JPATaskLifeCycleEventListener;
import org.jbpm.services.task.audit.TaskAuditServiceFactory;
import org.jbpm.services.task.impl.TaskDeadlinesServiceImpl;
import org.jbpm.services.task.impl.factories.TaskFactory;
import org.jbpm.services.task.lifecycle.listeners.BAMTaskEventListener;
import org.jbpm.test.listener.task.CountDownTaskEventListener;
import org.kie.test.util.db.PoolingDataSourceWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.internal.query.QueryFilter;
import org.kie.internal.task.api.AuditTask;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.internal.task.api.model.InternalTask;

public class LocalTaskAuditWithDeadlineTest extends HumanTaskServicesBaseTest {

	private PoolingDataSourceWrapper pds;
	private EntityManagerFactory emf;
	
	protected TaskAuditService taskAuditService;
	
    private String timerServiceId;

	@Before
	public void setup() {

	    pds = setupPoolingDataSource();
		emf = Persistence.createEntityManagerFactory( "org.jbpm.services.task" );

		this.taskService = (InternalTaskService) HumanTaskServiceFactory.newTaskServiceConfigurator()
												.entityManagerFactory(emf)
												.listener(new JPATaskLifeCycleEventListener(true))
												.listener(new BAMTaskEventListener(true))
												.getTaskService();
                
        this.taskAuditService = TaskAuditServiceFactory.newTaskAuditServiceConfigurator().setTaskService(taskService).getTaskAuditService();
        TimerService globalTs = new JDKTimerService(3);
        timerServiceId = "null" + TimerServiceRegistry.TIMER_SERVICE_SUFFIX;
        // and register it in the registry under 'default' key
        TimerServiceRegistry.getInstance().registerTimerService(timerServiceId, globalTs);
        TaskServiceRegistry.instance().registerTaskService(null, taskService);
	}
	
	@After
	public void clean() {
        TaskServiceRegistry.instance().remove(null);
        TimerServiceRegistry.getInstance().remove(timerServiceId).shutdown();

		if (emf != null) {
			emf.close();
		}
		if (pds != null) {
			pds.close();
		}
	}
	  
    @Test(timeout=10000)
    public void testDelayedReassignmentOnDeadline() throws Exception {
        CountDownTaskEventListener countDownListener = new CountDownTaskEventListener(1, true, false);
        addCountDownListener(countDownListener);
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("now", new Date());
    
        Reader reader = new InputStreamReader(getClass().getResourceAsStream(MvelFilePath.DeadlineWithReassignment));
        Task task = (InternalTask) TaskFactory.evalTask(reader, vars);

        Map<String, Object> inputVars = new HashMap<String, Object>();
        inputVars.put("NotCompletedReassign", "[users:Tony Stark,Bobba Fet,Jabba Hutt|groups:]@[500ms]");
        taskService.addTask(task, inputVars);
        long taskId = task.getId();

        taskService.claim(taskId, "Tony Stark");
    
        task = taskService.getTaskById(taskId);
        List<OrganizationalEntity> potentialOwners = (List<OrganizationalEntity>) task.getPeopleAssignments().getPotentialOwners();
        List<String> ids = new ArrayList<String>(potentialOwners.size());
        for (OrganizationalEntity entity : potentialOwners) {
            ids.add(entity.getId());
        }
        assertTrue(ids.contains("Tony Stark"));
        assertTrue(ids.contains("Luke Cage"));
        
        List<AuditTask> tasks = taskAuditService.getAllAuditTasks(new QueryFilter());
        assertEquals(1, tasks.size());
        
        AuditTask auditTask = tasks.get(0);
        
        //Check if countdown listener has not expired yet
        if (countDownListener.latchCount() != 0) {
            assertEquals(Status.Reserved.toString(), auditTask.getStatus());
            assertEquals("Tony Stark", auditTask.getActualOwner());
        }
        
        // should have re-assigned by now
        countDownListener.waitTillCompleted();
        
        task = taskService.getTaskById(taskId);
        assertNull(task.getTaskData().getActualOwner());
        assertEquals(Status.Ready, task.getTaskData().getStatus());
        potentialOwners = (List<OrganizationalEntity>) task.getPeopleAssignments().getPotentialOwners();
    
        ids = new ArrayList<String>(potentialOwners.size());
        for (OrganizationalEntity entity : potentialOwners) {
            ids.add(entity.getId());
        }
        assertTrue(ids.contains("Bobba Fet"));
        assertTrue(ids.contains("Jabba Hutt"));
        
        tasks = taskAuditService.getAllAuditTasks(new QueryFilter());
        assertEquals(1, tasks.size());
        
        auditTask = tasks.get(0);
        assertEquals(Status.Ready.toString(), auditTask.getStatus());
        assertEquals("", auditTask.getActualOwner());
    
    }

    @Test(timeout=10000)
    public void testDelayedReassignmentOnDeadlineISOFormat() throws Exception {
        CountDownTaskEventListener countDownListener = new CountDownTaskEventListener(1, true, false);
        addCountDownListener(countDownListener);
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("now", new Date());

        Reader reader = new InputStreamReader(getClass().getResourceAsStream(MvelFilePath.DeadlineWithReassignment));
        Task task = (InternalTask) TaskFactory.evalTask(reader, vars);

        Map<String, Object> inputVars = new HashMap<String, Object>();
        inputVars.put("NotCompletedReassign", "[users:Tony Stark,Bobba Fet,Jabba Hutt|groups:]@[PT500MS]");
        taskService.addTask(task, inputVars);
        long taskId = task.getId();

        taskService.claim(taskId, "Tony Stark");

        task = taskService.getTaskById(taskId);
        List<OrganizationalEntity> potentialOwners = (List<OrganizationalEntity>) task.getPeopleAssignments().getPotentialOwners();
        List<String> ids = new ArrayList<String>(potentialOwners.size());
        for (OrganizationalEntity entity : potentialOwners) {
            ids.add(entity.getId());
        }
        assertTrue(ids.contains("Tony Stark"));
        assertTrue(ids.contains("Luke Cage"));

        List<AuditTask> tasks = taskAuditService.getAllAuditTasks(new QueryFilter());
        assertEquals(1, tasks.size());

        AuditTask auditTask = tasks.get(0);
        
        //Check if countdown listener has not expired yet
        if (countDownListener.latchCount() != 0) {
            assertEquals(Status.Reserved.toString(), auditTask.getStatus());
            assertEquals("Tony Stark", auditTask.getActualOwner());
        }

        // should have re-assigned by now
        countDownListener.waitTillCompleted();

        task = taskService.getTaskById(taskId);
        assertNull(task.getTaskData().getActualOwner());
        assertEquals(Status.Ready, task.getTaskData().getStatus());
        potentialOwners = (List<OrganizationalEntity>) task.getPeopleAssignments().getPotentialOwners();

        ids = new ArrayList<String>(potentialOwners.size());
        for (OrganizationalEntity entity : potentialOwners) {
            ids.add(entity.getId());
        }
        assertTrue(ids.contains("Bobba Fet"));
        assertTrue(ids.contains("Jabba Hutt"));

        tasks = taskAuditService.getAllAuditTasks(new QueryFilter());
        assertEquals(1, tasks.size());

        auditTask = tasks.get(0);
        assertEquals(Status.Ready.toString(), auditTask.getStatus());
        assertEquals("", auditTask.getActualOwner());

    }
}
