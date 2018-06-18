/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.test.functional.task;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jbpm.process.core.timer.BusinessCalendar;
import org.jbpm.process.core.timer.BusinessCalendarImpl;
import org.jbpm.services.task.events.DefaultTaskEventListener;
import org.jbpm.services.task.lifecycle.listeners.TaskLifeCycleEventListener;
import org.jbpm.test.JbpmTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.TaskEvent;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Task;

public class HumanTaskReassignmentTest extends JbpmTestCase {

    private static final String PROCESS_FILE = "org/jbpm/test/functional/task/TaskReassignmentTimeout.bpmn2";
    private static final String PROCESS_ID = "com.bpms.functional.bpmn2.task.TaskReassignmentTimeout";

    private static final String JOHN = "john";
    private static final String MARY = "mary";

    private KieSession ksession;
    private TaskService taskService;
    private RuntimeManager runtimeManager;
    private RuntimeEngine engine;
    
    final List<Long> list;

    public HumanTaskReassignmentTest() {
        super(true, true);
        list = new ArrayList<Long>();
    }

    @Before
    public void init() {
        list.clear();
        TaskLifeCycleEventListener listener = new TaskLifeCycleEventListener() {

            @Override
            public void afterTaskActivatedEvent(TaskEvent arg0) {
               
                
            }

            @Override
            public void afterTaskAddedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void afterTaskClaimedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void afterTaskCompletedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void afterTaskDelegatedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void afterTaskExitedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void afterTaskFailedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void afterTaskForwardedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void afterTaskNominatedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void afterTaskReleasedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void afterTaskResumedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void afterTaskSkippedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void afterTaskStartedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void afterTaskStoppedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void afterTaskSuspendedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void beforeTaskActivatedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void beforeTaskAddedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void beforeTaskClaimedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void beforeTaskCompletedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void beforeTaskDelegatedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void beforeTaskExitedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void beforeTaskFailedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void beforeTaskForwardedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void beforeTaskNominatedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void beforeTaskReleasedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void beforeTaskResumedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void beforeTaskSkippedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void beforeTaskStartedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void beforeTaskStoppedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void beforeTaskSuspendedEvent(TaskEvent arg0) {
                
                
            }

            @Override
            public void beforeTaskUpdatedEvent(TaskEvent event) {
                
                
            }

            @Override
            public void afterTaskUpdatedEvent(TaskEvent event) {
                
                
            }

            @Override
            public void beforeTaskReassignedEvent(TaskEvent event) {
                
                list.add(event.getTask().getId());
            }

            @Override
            public void afterTaskReassignedEvent(TaskEvent event) {
                
                list.add(event.getTask().getId());
            }

            @Override
            public void beforeTaskNotificationEvent(TaskEvent event) {
                
                
            }

            @Override
            public void afterTaskNotificationEvent(TaskEvent event) {
                
                
            }

            @Override
            public void afterTaskInputVariableChangedEvent(TaskEvent event, Map<String, Object> variables) {
                
                
            }

            @Override
            public void afterTaskOutputVariableChangedEvent(TaskEvent event, Map<String, Object> variables) {
                
                
            }
            
        };
        addTaskEventListener(listener);
        runtimeManager = createRuntimeManager(PROCESS_FILE);
        engine = getRuntimeEngine();
        ksession = engine.getKieSession();
        taskService = engine.getTaskService();
    }
    
    @After
    public void cleanup() {
    	runtimeManager.disposeRuntimeEngine(engine);
    	runtimeManager.close();
    }

    private void testTimeout(boolean businessHour) throws InterruptedException {
        long pid = ksession.startProcess(PROCESS_ID).getId();
        long taskId = taskService.getTasksByProcessInstanceId(pid).get(0);
        String potOwner = getTaskPotentialOwner(taskId);
        assertEquals(JOHN, potOwner);
        Thread.sleep(2000);
        potOwner = getTaskPotentialOwner(taskId);
        assertEquals(businessHour ? MARY : JOHN, potOwner);
        
        ksession.abortProcessInstance(pid);
    }

    @Test
    public void testTimeout() throws InterruptedException {
        testTimeout(true);
    }

    @Test
    public void testTimeoutBusinessHour() throws InterruptedException {
        configureBusinessCalendar(true);
        testTimeout(true);
    }

    @Test
    public void testTimeoutNonBusinessHour() throws InterruptedException {
        configureBusinessCalendar(false);
        testTimeout(false);
    }
    
    @Test
    public void testTimeoutWithEventListener() throws InterruptedException {
        
        testTimeout(true);
        
        assertEquals(2, list.size());
    }

    private String getTaskPotentialOwner(long taskId) {
        Task task = taskService.getTaskById(taskId);
        assertNotNull(task);

        List<OrganizationalEntity> potentialOwners = task.getPeopleAssignments().getPotentialOwners();
        assertFalse(potentialOwners.isEmpty());
        return potentialOwners.get(0).getId();
    }

    private void configureBusinessCalendar(boolean businessHour) {
        Properties configuration = new Properties();

        if (businessHour) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, -1);
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

            configuration.setProperty(BusinessCalendarImpl.START_HOUR, "0");
            configuration.setProperty(BusinessCalendarImpl.END_HOUR, "24");
            configuration.setProperty(BusinessCalendarImpl.HOURS_PER_DAY, "24");
            configuration.setProperty(BusinessCalendarImpl.DAYS_PER_WEEK, "7");
            configuration.setProperty(BusinessCalendarImpl.WEEKEND_DAYS, Integer.toString(dayOfWeek));
        } else {
            Date today = new Date();

            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, 1);
            Date tomorrow = c.getTime();

            String dateFormat = "yyyy-MM-dd";
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            configuration.setProperty(BusinessCalendarImpl.HOLIDAYS, sdf.format(today) + "," + sdf.format(tomorrow));
            configuration.setProperty(BusinessCalendarImpl.HOLIDAY_DATE_FORMAT, dateFormat);
        }

        BusinessCalendar businessCalendar = new BusinessCalendarImpl(configuration);
        ksession.getEnvironment().set("jbpm.business.calendar", businessCalendar);
    }

}
