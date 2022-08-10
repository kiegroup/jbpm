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

package org.jbpm.services.task.audit.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.assertj.core.api.Assertions;
import org.jbpm.services.task.HumanTaskServicesBaseTest;
import org.jbpm.services.task.audit.commands.DeleteAuditEventsCommand;
import org.jbpm.services.task.audit.commands.DeleteBAMTaskSummariesCommand;
import org.jbpm.services.task.audit.commands.GetAuditEventsCommand;
import org.jbpm.services.task.audit.commands.GetBAMTaskSummariesCommand;
import org.jbpm.services.task.audit.impl.model.AuditTaskImpl;
import org.jbpm.services.task.audit.impl.model.BAMTaskSummaryImpl;
import org.jbpm.services.task.audit.service.objects.Person;
import org.jbpm.services.task.impl.model.I18NTextImpl;
import org.jbpm.services.task.utils.TaskFluent;
import org.junit.Test;
import org.kie.api.task.model.I18NText;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.query.QueryFilter;
import org.kie.internal.task.api.AuditTask;
import org.kie.internal.task.api.TaskVariable;
import org.kie.internal.task.api.TaskVariable.VariableType;
import org.kie.internal.task.api.model.InternalTaskData;
import org.kie.internal.task.api.model.TaskEvent;
import org.kie.internal.task.api.model.TaskEvent.TaskEventType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class TaskAuditBaseTest extends HumanTaskServicesBaseTest {

    Timestamp today = getToday();
    Timestamp tomorrow = getTomorrow();

    @Inject
    protected TaskAuditService taskAuditService;

    @Test
    public void testComplete() {
        long initTimeMs = new Date().getTime();
        Task task = new TaskFluent().setName("This is my task name")
                .addPotentialGroup("Knights Templer")
                .setAdminUser("Administrator")
                .getTask();

        taskService.addTask(task, new HashMap<String, Object>());
        long taskId = task.getId();

        List<TaskSummary> allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("salaboy", null, null, null);
        assertEquals(1, allGroupAuditTasks.size());
        assertTrue(allGroupAuditTasks.get(0).getStatusId().equals("Ready"));

        taskService.claim(taskId, "Darth Vader");

        allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("salaboy", null, null, null);
        assertEquals(0, allGroupAuditTasks.size());

        allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("Darth Vader", null, null, null);
        assertEquals(1, allGroupAuditTasks.size());
        assertTrue(allGroupAuditTasks.get(0).getStatusId().equals("Reserved"));

        taskService.release(taskId, "Darth Vader");

        allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("salaboy", null, null, null);
        assertEquals(1, allGroupAuditTasks.size());
        assertTrue(allGroupAuditTasks.get(0).getStatusId().equals("Ready"));

        taskService.claim(taskId, "Darth Vader");

        allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("salaboy", null, null, null);
        assertEquals(0, allGroupAuditTasks.size());

        allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("Darth Vader", null, null, null);
        assertEquals(1, allGroupAuditTasks.size());
        assertTrue(allGroupAuditTasks.get(0).getStatusId().equals("Reserved"));

        // Go straight from Ready to Inprogress
        taskService.start(taskId, "Darth Vader");

        Task task1 = taskService.getTaskById(taskId);
        assertEquals(Status.InProgress, task1.getTaskData().getStatus());
        assertEquals("Darth Vader", task1.getTaskData().getActualOwner().getId());

        // Check is Complete
        taskService.complete(taskId, "Darth Vader", null);

        Task task2 = taskService.getTaskById(taskId);
        assertEquals(Status.Completed, task2.getTaskData().getStatus());
        assertEquals("Darth Vader", task2.getTaskData().getActualOwner().getId());

        List<TaskEvent> allTaskEvents = taskService.execute(new GetAuditEventsCommand(taskId, new QueryFilter(0, 0)));
        assertEquals(7, allTaskEvents.size());

        // test DeleteAuditEventsCommand
        int numFirstTaskEvents = allTaskEvents.size();
        task = new TaskFluent().setName("This is my task name 2")
                .addPotentialGroup("Knights Templer")
                .setAdminUser("Administrator")
                .getTask();
        taskService.addTask(task, new HashMap<String, Object>());
        long secondTaskId = task.getId();
        taskService.claim(secondTaskId, "Darth Vader");
        taskService.start(secondTaskId, "Darth Vader");
        taskService.complete(secondTaskId, "Darth Vader", null);

        allTaskEvents = taskService.execute(new GetAuditEventsCommand());
        int numTaskEvents = allTaskEvents.size();
        assertTrue("Expected more than " + numFirstTaskEvents + " events: " + numTaskEvents,
                numTaskEvents > numFirstTaskEvents);

        taskService.execute(new DeleteAuditEventsCommand(taskId));
        allTaskEvents = taskService.execute(new GetAuditEventsCommand());
        // +2 activate events.
        assertEquals(numTaskEvents - numFirstTaskEvents + 2, allTaskEvents.size());

        taskService.execute(new DeleteAuditEventsCommand());
        allTaskEvents = taskService.execute(new GetAuditEventsCommand());
        assertEquals(0, allTaskEvents.size());

        // test get/delete BAM Task summaries commands
        List<BAMTaskSummaryImpl> bamTaskList = taskService.execute(new GetBAMTaskSummariesCommand());
        assertEquals("BAM Task Summary list size: ", 2, bamTaskList.size());

        taskService.execute(new DeleteBAMTaskSummariesCommand(taskId));
        bamTaskList = taskService.execute(new GetBAMTaskSummariesCommand());
        assertEquals("BAM Task Summary list size after delete (task id: " + taskId + ") : ", 1, bamTaskList.size());

        bamTaskList = taskService.execute(new GetBAMTaskSummariesCommand(secondTaskId));
        assertEquals("BAM Task Summary list size after delete (task id: " + taskId + ") : ", 1, bamTaskList.size());

        taskService.execute(new DeleteBAMTaskSummariesCommand());
        bamTaskList = taskService.execute(new GetBAMTaskSummariesCommand());
        assertEquals("BAM Task Summary list size after delete (task id: " + taskId + ") : ", 0, bamTaskList.size());

        List<AuditTask> allHistoryAuditTasks = taskAuditService.getAllAuditTasks(new QueryFilter(0, 0));
        assertEquals(2, allHistoryAuditTasks.size());

        // test last modification date was generated
        long currentTimeMs = new Date().getTime();
        for(AuditTask at : allHistoryAuditTasks){
            Date modDate = ((AuditTaskImpl)at).getLastModificationDate();
            assertNotNull(modDate);
            long modDateMs = modDate.getTime();
            assertTrue("Task " + at.getTaskId() + " modification date is not too much in the past", modDateMs >= initTimeMs);
            assertTrue("Task " + at.getTaskId() + " modification date is not in the future", modDateMs <= currentTimeMs);
        }
    }

    @Test
    public void testOnlyActiveTasks() {
        Task task = new TaskFluent().setName("This is my task name")
                .addPotentialUser("salaboy")
                .setAdminUser("Administrator")
                .getTask();
        taskService.addTask(task, new HashMap<String, Object>());

        List<TaskSummary> allActiveTasks = taskService.getTasksAssignedAsPotentialOwner("salaboy", null, null, null);
        assertEquals(1, allActiveTasks.size());
        assertTrue(allActiveTasks.get(0).getStatusId().equals("Reserved"));
        QueryFilter queryFilter = new QueryFilter(0, 0);
        Map<String, Object> params = new HashMap<String, Object>();
        List<String> statuses = new ArrayList<String>();
        statuses.add(Status.Reserved.toString());
        params.put("statuses", statuses);
        queryFilter.setParams(params);
        List<AuditTask> allActiveAuditTasksByUser = taskAuditService.getAllAuditTasksByStatus("salaboy",
                queryFilter);
        assertEquals(1, allActiveAuditTasksByUser.size());
        assertTrue(allActiveAuditTasksByUser.get(0).getStatus().equals("Reserved"));

        statuses = new ArrayList<String>();
        statuses.add(Status.Completed.toString());
        params.put("statuses", statuses);
        queryFilter.setParams(params);
        allActiveAuditTasksByUser = taskAuditService.getAllAuditTasksByStatus("salaboy",
                queryFilter);
        assertEquals(0, allActiveAuditTasksByUser.size());

    }

    @Test
    public void testGroupTasks() {
        Task task = new TaskFluent().setName("This is my task name")
                .addPotentialUser("salaboy")
                .addPotentialUser("krisv")
                .addPotentialGroup("Knights Templer")
                .setAdminUser("Administrator")
                .getTask();
        taskService.addTask(task, new HashMap<String, Object>());

        List<TaskSummary> allGroupTasks = taskService.getTasksAssignedAsPotentialOwner("salaboy", null, null, null);
        assertEquals(1, allGroupTasks.size());
        assertTrue(allGroupTasks.get(0).getStatusId().equals("Ready"));

        List<AuditTask> allGroupAuditTasksByUser = taskAuditService.getAllGroupAuditTasksByUser("salaboy",
                new QueryFilter(0, 0));
        assertEquals(1, allGroupAuditTasksByUser.size());
        assertTrue(allGroupAuditTasksByUser.get(0).getStatus().equals("Ready"));
    }

    @Test
    public void testAdminTasks() {
        Task task = new TaskFluent().setName("This is my task name")
                .setAdminUser("salaboy")
                .getTask();

        taskService.addTask(task, new HashMap<String, Object>());

        List<TaskSummary> allAdminTasks = taskService.getTasksAssignedAsBusinessAdministrator("salaboy", null);
        assertEquals(1, allAdminTasks.size());

        List<AuditTask> allAdminAuditTasksByUser = taskAuditService.getAllAdminAuditTasksByUser("salaboy",
                new QueryFilter(0, 0));
        assertEquals(1, allAdminAuditTasksByUser.size());
    }


    @Test
    public void testExitAfterClaim() {
        // One potential owner, should go straight to state Reserved
        Task task = new TaskFluent().setName("This is my task name 2")
                .addPotentialGroup("Knights Templer")
                .setAdminUser("Administrator")
                .getTask();
        taskService.addTask(task, new HashMap<String, Object>());
        long taskId = task.getId();

        List<TaskSummary> allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("salaboy", null, null, null);
        assertEquals(1, allGroupAuditTasks.size());
        assertTrue(allGroupAuditTasks.get(0).getStatusId().equals("Ready"));

        taskService.claim(taskId, "Darth Vader");

        allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("salaboy", null, null, null);
        assertEquals(0, allGroupAuditTasks.size());

        allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("Darth Vader", null, null, null);
        assertEquals(1, allGroupAuditTasks.size());
        assertTrue(allGroupAuditTasks.get(0).getStatusId().equals("Reserved"));

        taskService.exit(taskId, "Administrator");

        List<AuditTask> allHistoryAuditTasks = taskAuditService.getAllAuditTasks(new QueryFilter(0, 0));
        assertEquals(1, allHistoryAuditTasks.size());

        allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("salaboy", null, null, null);
        assertEquals(0, allGroupAuditTasks.size());
    }

    @Test
    public void testExitBeforeClaim() {
        Task task = new TaskFluent().setName("This is my task name 2")
                .addPotentialGroup("Knights Templer")
                .setAdminUser("Administrator")
                .getTask();
        taskService.addTask(task, new HashMap<String, Object>());
        long taskId = task.getId();

        List<TaskSummary> allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("salaboy", null, null, null);
        assertEquals(1, allGroupAuditTasks.size());
        assertTrue(allGroupAuditTasks.get(0).getStatusId().equals("Ready"));

        taskService.exit(taskId, "Administrator");

        List<AuditTask> allHistoryAuditTasks = taskAuditService.getAllAuditTasks(new QueryFilter(0, 0));
        assertEquals(1, allHistoryAuditTasks.size());

        allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("salaboy", null, null, null);
        assertEquals(0, allGroupAuditTasks.size());
    }

    private void testDescriptionUpdate(String oldDescription,
                                       String newDescription,
                                       boolean changeExpected,
                                       String expectedMessage) {
        Task task = new TaskFluent()
                .setDescription(oldDescription)
                .setAdminUser("Administrator")
                .getTask();
        taskService.addTask(task, new HashMap<String, Object>());
        long taskId = task.getId();

        List<I18NText> descriptions = new ArrayList<I18NText>();
        descriptions.add(new I18NTextImpl("", newDescription));
        taskService.setDescriptions(taskId, descriptions);

        task = taskService.getTaskById(taskId);
        Assertions.assertThat(task.getDescription()).isEqualTo(newDescription);

        List<AuditTask> auditTasks = taskAuditService.getAllAuditTasks(new QueryFilter());
        Assertions.assertThat(auditTasks).hasSize(1);
        Assertions.assertThat(auditTasks.get(0).getDescription()).isEqualTo(newDescription);

        List<TaskEvent> taskEvents = taskAuditService.getAllTaskEvents(taskId, new QueryFilter());
        if (changeExpected) {
            Assertions.assertThat(taskEvents).hasSize(2);
            Assertions.assertThat(taskEvents.get(1).getMessage()).isEqualTo(expectedMessage);
        } else {
            Assertions.assertThat(taskEvents).hasSize(1);
        }
    }

    @Test
    public void testDescriptionUpdateSame() {
        testDescriptionUpdate("description",
                              "description",
                              false,
                              "");
    }

    @Test
    public void testDescriptionUpdateDifferent() {
        testDescriptionUpdate("old description",
                              "new description",
                              true,
                              "Updated Description {From: 'old description' to: 'new description'}");
    }

    @Test
    public void testDescriptionUpdateToNull() {
        testDescriptionUpdate("old description",
                              null,
                              true,
                              "Updated Description {From: 'old description' to: ''}");
    }

    @Test
    public void testDescriptionUpdateToEmpty() {
        testDescriptionUpdate("old description",
                              "",
                              true,
                              "Updated Description {From: 'old description' to: ''}");
    }

    @Test
    public void testDescriptionUpdateFromNull() {
        testDescriptionUpdate(null,
                              "new description",
                              true,
                              "Updated Description {From: '' to: 'new description'}");
    }

    @Test
    public void testDescriptionUpdateFromEmpty() {
        testDescriptionUpdate("",
                              "new description",
                              true,
                              "Updated Description {From: '' to: 'new description'}");
    }
    
    private void testLongTaskDescription(String oldDescription,
            String newDescription,
            String expectedDescription,
            boolean changeExpected,
            String expectedMessage) {
        Task task = new TaskFluent()
                .setDescription(oldDescription)
                .setAdminUser("Administrator")
                .getTask();
        taskService.addTask(task, new HashMap<String, Object>());
        long taskId = task.getId();

        List<I18NText> descriptions = new ArrayList<I18NText>();
        descriptions.add(new I18NTextImpl("", newDescription));
        taskService.setDescriptions(taskId, descriptions);

        task = taskService.getTaskById(taskId);
        Assertions.assertThat(task.getDescription()).isEqualTo(expectedDescription);

        List<AuditTask> auditTasks = taskAuditService.getAllAuditTasks(new QueryFilter());
        Assertions.assertThat(auditTasks).hasSize(1);
        Assertions.assertThat(auditTasks.get(0).getDescription()).isEqualTo(expectedDescription);

        List<TaskEvent> taskEvents = taskAuditService.getAllTaskEvents(taskId, new QueryFilter());
        if (changeExpected) {
            Assertions.assertThat(taskEvents).hasSize(2);
            Assertions.assertThat(taskEvents.get(1).getMessage()).isEqualTo(expectedMessage);
        } else {
            Assertions.assertThat(taskEvents).hasSize(1);
        }
    }

    
    
    @Test
    public void testLongTaskDescriptionUpdateFromEmpty() {
        System.setProperty("org.jbpm.ht.task.description.length", "255");

        testLongTaskDescription("",
                                "Long task description to test the jira https://issues.redhat.com/browse/RHPAM-4445Long task description to test the jira https://issues.redhat.com/browse/RHPAM-4445Long task description to test the jira https://issues.redhat.com/browse/RHPAM-44459999999999999",
                                "Long task description to test the jira https://issues.redhat.com/browse/RHPAM-4445Long task description to test the jira https://issues.redhat.com/browse/RHPAM-4445Long task description to test the jira https://issues.redhat.com/browse/RHPAM-4445999999999",
                                true,
                                "Updated Description {From: '' to: 'Long task description to test the jira https://issues.redhat.com/browse/RHPAM-4445Long task description to test the jira https://issues.redhat.com/browse/RHPAM-4445Long task description to test the jira https://issues.'}");
        System.clearProperty("org.jbpm.ht.task.description.length");
    }

    @Test
    public void testLongTaskDescriptionUpdateDifferent() {
        System.setProperty("org.jbpm.ht.task.description.length", "255");
        testLongTaskDescription("old Description",
                                "Long task description to test the jira https://issues.redhat.com/browse/RHPAM-4445Long task description to test the jira https://issues.redhat.com/browse/RHPAM-4445Long task description to test the jira https://issues.redhat.com/browse/RHPAM-44459999999999999",
                                "Long task description to test the jira https://issues.redhat.com/browse/RHPAM-4445Long task description to test the jira https://issues.redhat.com/browse/RHPAM-4445Long task description to test the jira https://issues.redhat.com/browse/RHPAM-4445999999999",
                                true,
                                "Updated Description {From: 'old Description' to: 'Long task description to test the jira https://issues.redhat.com/browse/RHPAM-4445Long task description to test the jira https://issues.redhat.com/browse/RHPAM-4445Long task description to test the jira '}");
        System.clearProperty("org.jbpm.ht.task.description.length");
    }

    private void testNameUpdate(String oldName,
                                String newName,
                                boolean changeExpected,
                                String expectedMessage) {
        Task task = new TaskFluent()
                .setName(oldName)
                .setAdminUser("Administrator")
                .getTask();
        taskService.addTask(task, new HashMap<String, Object>());
        long taskId = task.getId();

        List<I18NText> taskNames = new ArrayList<I18NText>();
        taskNames.add(new I18NTextImpl("", newName));
        taskService.setTaskNames(taskId, taskNames);

        task = taskService.getTaskById(taskId);
        Assertions.assertThat(task.getName()).isEqualTo(newName);

        List<AuditTask> auditTasks = taskAuditService.getAllAuditTasks(new QueryFilter());
        Assertions.assertThat(auditTasks).hasSize(1);
        Assertions.assertThat(auditTasks.get(0).getName()).isEqualTo(newName);

        List<TaskEvent> taskEvents = taskAuditService.getAllTaskEvents(taskId, new QueryFilter());
        if (changeExpected) {
            Assertions.assertThat(taskEvents).hasSize(2);
            Assertions.assertThat(taskEvents.get(1).getMessage()).isEqualTo(expectedMessage);

        } else {
            Assertions.assertThat(taskEvents).hasSize(1);
        }
    }

    @Test
    public void testNameUpdateSame() {
        testNameUpdate("name",
                       "name",
                       false,
                       "");
    }

    @Test
    public void testNameUpdateDifferent() {
        testNameUpdate("old name",
                       "new name",
                       true,
                       "Updated Name {From: 'old name' to: 'new name'}");
    }

    @Test
    public void testNameUpdateToNull() {
        testNameUpdate("old name",
                       null,
                       true,
                       "Updated Name {From: 'old name' to: ''}");
    }

    @Test
    public void testNameUpdateToEmpty() {
        testNameUpdate("old name",
                       "",
                       true,
                       "Updated Name {From: 'old name' to: ''}");
    }

    @Test
    public void testNameUpdateFromNull() {
        testNameUpdate(null,
                       "new name",
                       true,
                       "Updated Name {From: '' to: 'new name'}");
    }

    @Test
    public void testNameUpdateFromEmpty() {
        testNameUpdate("",
                       "new name",
                       true,
                       "Updated Name {From: '' to: 'new name'}");
    }

    private void testPriorityUpdate(int oldPriority,
                                    int newPriority,
                                    boolean changeExpected) {
        Task task = new TaskFluent()
                .setPriority(oldPriority)
                .setAdminUser("Administrator")
                .getTask();
        taskService.addTask(task, new HashMap<String, Object>());
        long taskId = task.getId();

        taskService.setPriority(taskId, newPriority);

        task = taskService.getTaskById(taskId);
        Assertions.assertThat(task.getPriority()).isEqualTo(newPriority);

        List<AuditTask> auditTasks = taskAuditService.getAllAuditTasks(new QueryFilter());
        Assertions.assertThat(auditTasks).hasSize(1);
        Assertions.assertThat(auditTasks.get(0).getPriority()).isEqualTo(newPriority);

        List<TaskEvent> taskEvents = taskAuditService.getAllTaskEvents(taskId, new QueryFilter());
        if (changeExpected) {
            Assertions.assertThat(taskEvents).hasSize(2);
            Assertions.assertThat(taskEvents.get(1).getMessage()).isEqualTo("Updated Priority {From: '0' to: '10'}");
        } else {
            Assertions.assertThat(taskEvents).hasSize(1);
        }
    }

    @Test
    public void testPriorityUpdateSame() {
        testPriorityUpdate(0, 0, false);
    }

    @Test
    public void testPriorityUpdateDifferent() {
        testPriorityUpdate(0, 10, true);
    }

    private void testDueDateUpdate(Date oldDate,
                                   Date newDate,
                                   boolean changeExpected,
                                   String expectedMessage) {
        Task task = new TaskFluent()
                .setDueDate(oldDate)
                .setAdminUser("Administrator")
                .getTask();
        taskService.addTask(task, new HashMap<String, Object>());
        long taskId = task.getId();

        taskService.setExpirationDate(taskId, newDate);

        task = taskService.getTaskById(taskId);
        Assertions.assertThat(task.getTaskData().getExpirationTime()).isEqualTo(newDate);

        List<AuditTask> auditTasks = taskAuditService.getAllAuditTasks(new QueryFilter());
        Assertions.assertThat(auditTasks).hasSize(1);
        Assertions.assertThat(auditTasks.get(0).getDueDate()).isEqualTo(newDate);

        List<TaskEvent> taskEvents = taskAuditService.getAllTaskEvents(taskId, new QueryFilter());
        if (changeExpected) {
            Assertions.assertThat(taskEvents).hasSize(2);
            Assertions.assertThat(taskEvents.get(1).getMessage()).isEqualTo(expectedMessage);
        } else {
            Assertions.assertThat(taskEvents).hasSize(1);
        }
    }

    private Timestamp getToday() {
        return new Timestamp(new Date().getTime());
    }

    private Timestamp getTomorrow() {
        Calendar c = Calendar.getInstance();
        c.setTime(getToday());
        c.add(Calendar.DATE, 1);
        return new Timestamp(c.getTimeInMillis());
    }

    @Test
    public void testDueDateUpdateSame() {
        testDueDateUpdate(today,
                          today,
                          false,
                          "");
    }

    @Test
    public void testDueDateUpdateDifferent() {
        String fromDate = (today != null ? new Date(today.getTime()).toString() : null);
        String toDate = (tomorrow != null ? tomorrow.toString() : "");
        testDueDateUpdate(today,
                          tomorrow,
                          true,
                          "Updated DueDate {From: '" + fromDate + "' to: '" + toDate + "'}");
    }

    @Test
    public void testDueDateUpdateFromNull() {
        String toDate = (tomorrow != null ? tomorrow.toString() : "");
        testDueDateUpdate(null,
                          tomorrow,
                          true,
                          "Updated DueDate {From: '' to: '"+ toDate + "'}");
    }

    @Test
    public void testDueDateUpdateToNull() {
        String fromDate = (today != null ? new Date(today.getTime()).toString() : null);
        testDueDateUpdate(today,
                          null,
                          true,
                          "Updated DueDate {From: '" + fromDate + "' to: ''}");
    }

    @Test
    public void testVariableIndexInputAndOutput() {
        Task task = new TaskFluent().setName("This is my task name")
                .addPotentialGroup("Knights Templer")
                .setAdminUser("Administrator")
                .getTask();

        Map<String, Object> inputVariables = new HashMap<String, Object>();
        inputVariables.put("firstVariable", "string content");
        inputVariables.put("number", 1234);

        taskService.addTask(task, inputVariables);
        long taskId = task.getId();

        List<TaskSummary> allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("salaboy", null, null, null);
        assertEquals(1, allGroupAuditTasks.size());
        assertTrue(allGroupAuditTasks.get(0).getStatusId().equals("Ready"));

        List<TaskVariable> inputVars = taskAuditService.taskVariableQuery()
                .taskId(taskId).intersect().type(VariableType.INPUT).build().getResultList();
        assertNotNull(inputVars);
        assertEquals(2, inputVars.size());

        Map<String, String> vars = collectVariableNameAndValue(inputVars);

        assertTrue(vars.containsKey("firstVariable"));
        assertTrue(vars.containsKey("number"));

        assertEquals("string content", vars.get("firstVariable"));
        assertEquals("1234", vars.get("number"));

        taskService.claim(taskId, "Darth Vader");

        allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("salaboy", null, null, null);
        assertEquals(0, allGroupAuditTasks.size());

        allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("Darth Vader", null, null, null);
        assertEquals(1, allGroupAuditTasks.size());
        assertTrue(allGroupAuditTasks.get(0).getStatusId().equals("Reserved"));

        taskService.start(taskId, "Darth Vader");

        Task task1 = taskService.getTaskById(taskId);
        assertEquals(Status.InProgress, task1.getTaskData().getStatus());
        assertEquals("Darth Vader", task1.getTaskData().getActualOwner().getId());

        Map<String, Object> outputVariables = new HashMap<String, Object>();
        outputVariables.put("reply", "updated content");
        outputVariables.put("age", 25);

        // Check is Complete
        taskService.complete(taskId, "Darth Vader", outputVariables);

        List<TaskVariable> outputVars = taskAuditService.taskVariableQuery()
                .taskId(taskId).intersect().type(VariableType.OUTPUT).build().getResultList();
        assertNotNull(outputVars);
        assertEquals(2, outputVars.size());

        Map<String, String> outvars = collectVariableNameAndValue(outputVars);

        assertTrue(outvars.containsKey("reply"));
        assertTrue(outvars.containsKey("age"));

        assertEquals("updated content", outvars.get("reply"));
        assertEquals("25", outvars.get("age"));
    }

    @Test
    public void testVariableIndexInputAndUpdateOutput() {
        Task task = new TaskFluent().setName("This is my task name")
                .addPotentialGroup("Knights Templer")
                .setAdminUser("Administrator")
                .getTask();

        Map<String, Object> inputVariables = new HashMap<String, Object>();
        inputVariables.put("firstVariable", "string content");
        inputVariables.put("number", 1234);

        taskService.addTask(task, inputVariables);
        long taskId = task.getId();

        List<TaskSummary> allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("salaboy", null, null, null);
        assertEquals(1, allGroupAuditTasks.size());
        assertTrue(allGroupAuditTasks.get(0).getStatusId().equals("Ready"));

        List<TaskVariable> inputVars = taskAuditService.taskVariableQuery()
                .taskId(taskId).intersect().type(VariableType.INPUT).build().getResultList();
        assertNotNull(inputVars);
        assertEquals(2, inputVars.size());

        Map<String, String> vars = collectVariableNameAndValue(inputVars);

        assertTrue(vars.containsKey("firstVariable"));
        assertTrue(vars.containsKey("number"));

        assertEquals("string content", vars.get("firstVariable"));
        assertEquals("1234", vars.get("number"));

        taskService.claim(taskId, "Darth Vader");

        allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("salaboy", null, null, null);
        assertEquals(0, allGroupAuditTasks.size());

        allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("Darth Vader", null, null, null);
        assertEquals(1, allGroupAuditTasks.size());
        assertTrue(allGroupAuditTasks.get(0).getStatusId().equals("Reserved"));

        taskService.start(taskId, "Darth Vader");

        Task task1 = taskService.getTaskById(taskId);
        assertEquals(Status.InProgress, task1.getTaskData().getStatus());
        assertEquals("Darth Vader", task1.getTaskData().getActualOwner().getId());

        // update task output
        Map<String, Object> outputVariables = new HashMap<String, Object>();
        outputVariables.put("reply", "updated content");
        outputVariables.put("age", 25);

        taskService.addOutputContentFromUser(taskId, "Darth Vader", outputVariables);

        List<TaskVariable> outputVars = taskAuditService.taskVariableQuery()
                .taskId(taskId).intersect().type(VariableType.OUTPUT).build().getResultList();
        assertNotNull(outputVars);
        assertEquals(2, outputVars.size());

        Map<String, String> outvars = collectVariableNameAndValue(outputVars);

        assertTrue(outvars.containsKey("reply"));
        assertTrue(outvars.containsKey("age"));

        assertEquals("updated content", outvars.get("reply"));
        assertEquals("25", outvars.get("age"));

        // Check is Complete
        outputVariables = new HashMap<String, Object>();
        outputVariables.put("reply", "completed content");
        outputVariables.put("age", 44);
        outputVariables.put("reason", "rework, please");

        taskService.complete(taskId, "Darth Vader", outputVariables);

        outputVars = taskAuditService.taskVariableQuery()
                .taskId(taskId).intersect().type(VariableType.OUTPUT).build().getResultList();
        assertNotNull(outputVars);
        assertEquals(3, outputVars.size());

        outvars = collectVariableNameAndValue(outputVars);

        assertTrue(outvars.containsKey("reply"));
        assertTrue(outvars.containsKey("age"));
        assertTrue(outvars.containsKey("reason"));

        assertEquals("completed content", outvars.get("reply"));
        assertEquals("44", outvars.get("age"));
        assertEquals("rework, please", outvars.get("reason"));
    }

    @Test
    public void testVariableIndexInputAndOutputWithCustomIdexer() {
        Task task = new TaskFluent().setName("This is my task name")
                .addPotentialGroup("Knights Templer")
                .setAdminUser("Administrator")
                .getTask();

        Map<String, Object> inputVariables = new HashMap<String, Object>();
        inputVariables.put("firstVariable", "string content");
        inputVariables.put("person", new Person("john", 25));

        taskService.addTask(task, inputVariables);
        long taskId = task.getId();

        List<TaskSummary> allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("salaboy", null, null, null);
        assertEquals(1, allGroupAuditTasks.size());
        assertTrue(allGroupAuditTasks.get(0).getStatusId().equals("Ready"));

        List<TaskVariable> inputVars = taskAuditService.taskVariableQuery()
                .taskId(taskId).intersect().type(VariableType.INPUT).build().getResultList();
        assertNotNull(inputVars);
        assertEquals(3, inputVars.size());

        Map<String, String> vars = collectVariableNameAndValue(inputVars);

        assertTrue(vars.containsKey("firstVariable"));
        assertTrue(vars.containsKey("person.name"));
        assertTrue(vars.containsKey("person.age"));

        assertEquals("string content", vars.get("firstVariable"));
        assertEquals("john", vars.get("person.name"));
        assertEquals("25", vars.get("person.age"));

        taskService.claim(taskId, "Darth Vader");

        allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("salaboy", null, null, null);
        assertEquals(0, allGroupAuditTasks.size());

        allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("Darth Vader", null, null, null);
        assertEquals(1, allGroupAuditTasks.size());
        assertTrue(allGroupAuditTasks.get(0).getStatusId().equals("Reserved"));

        taskService.start(taskId, "Darth Vader");

        Task task1 = taskService.getTaskById(taskId);
        assertEquals(Status.InProgress, task1.getTaskData().getStatus());
        assertEquals("Darth Vader", task1.getTaskData().getActualOwner().getId());

        Map<String, Object> outputVariables = new HashMap<String, Object>();
        outputVariables.put("reply", "updated content");
        outputVariables.put("person", new Person("mary", 28));

        // Check is Complete
        taskService.complete(taskId, "Darth Vader", outputVariables);

        List<TaskVariable> outputVars = taskAuditService.taskVariableQuery()
                .taskId(taskId).intersect().type(VariableType.OUTPUT).build().getResultList();
        assertNotNull(outputVars);
        assertEquals(3, outputVars.size());

        Map<String, String> outvars = collectVariableNameAndValue(outputVars);

        assertTrue(outvars.containsKey("reply"));
        assertTrue(vars.containsKey("person.name"));
        assertTrue(vars.containsKey("person.age"));

        assertEquals("updated content", outvars.get("reply"));
        assertEquals("mary", outvars.get("person.name"));
        assertEquals("28", outvars.get("person.age"));
    }

    @Test
    public void testSearchTasksByVariable() {
        Task task = new TaskFluent().setName("This is my task name")
                .addPotentialGroup("Knights Templer")
                .setAdminUser("Administrator")
                .getTask();

        Map<String, Object> inputVariables = new HashMap<String, Object>();
        inputVariables.put("firstVariable", "string content");
        inputVariables.put("number", 1234);

        taskService.addTask(task, inputVariables);
        long taskId = task.getId();

        List<TaskSummary> allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("salaboy", null, null, null);
        assertEquals(1, allGroupAuditTasks.size());
        assertTrue(allGroupAuditTasks.get(0).getStatusId().equals("Ready"));

        List<TaskVariable> inputVars = taskAuditService.taskVariableQuery()
                .taskId(taskId).intersect().type(VariableType.INPUT).build().getResultList();
        assertNotNull(inputVars);
        assertEquals(2, inputVars.size());

        Map<String, String> vars = collectVariableNameAndValue(inputVars);

        assertTrue(vars.containsKey("firstVariable"));
        assertTrue(vars.containsKey("number"));

        assertEquals("string content", vars.get("firstVariable"));
        assertEquals("1234", vars.get("number"));

        List<TaskSummary> tasksByVariable = taskService.taskSummaryQuery("salaboy")
                .variableName("firstVariable").build().getResultList();
        assertNotNull(tasksByVariable);
        assertEquals(1, tasksByVariable.size());

        // search by unauthorized user
        tasksByVariable = taskService.taskSummaryQuery("WinterMute")
                .variableName("fistVariable").build().getResultList();
        assertNotNull(tasksByVariable);
        assertEquals(0, tasksByVariable.size());

        // search by not existing variable
        tasksByVariable = taskService.taskSummaryQuery("salaboy")
                .variableName("notexistingVariable").build().getResultList();
        assertNotNull(tasksByVariable);
        assertEquals(0, tasksByVariable.size());

        // search by variable name with wildcard
        tasksByVariable = taskService.taskSummaryQuery("salaboy").regex()
                .variableName("first*").build().getResultList();
        assertNotNull(tasksByVariable);
        assertNotNull(tasksByVariable);
        assertEquals(1, tasksByVariable.size());
    }

    @Test
    public void testSearchTasksByVariableNameAndValue() {
        Task task = new TaskFluent().setName("This is my task name")
                .addPotentialGroup("Knights Templer")
                .setAdminUser("Administrator")
                .getTask();

        Map<String, Object> inputVariables = new HashMap<String, Object>();

        String userId = "salaboy";
        String varName = "firstVariable";
        String varValue = "string content";
        inputVariables.put(varName, varValue);
        inputVariables.put("number", 1234);

        taskService.addTask(task, inputVariables);
        long taskId = task.getId();

        List<TaskSummary> allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner(userId, null, null, null);
        assertEquals(1, allGroupAuditTasks.size());
        assertTrue(allGroupAuditTasks.get(0).getStatusId().equals("Ready"));

        List<TaskVariable> inputVars = taskAuditService.taskVariableQuery()
                .taskId(taskId).build().getResultList();
        assertNotNull(inputVars);
        assertEquals(2, inputVars.size());

        Map<String, String> vars = collectVariableNameAndValue(inputVars);

        assertTrue(vars.containsKey(varName));
        assertTrue(vars.containsKey("number"));

        assertEquals(varValue, vars.get(varName));
        assertEquals("1234", vars.get("number"));

        List<TaskSummary> tasksByVariable = taskService.taskSummaryQuery(userId)
                .variableName(varName).and().variableValue(varValue).build().getResultList();
        assertNotNull(tasksByVariable);
        assertEquals(1, tasksByVariable.size());

        // search with value wild card
        tasksByVariable = taskService.taskSummaryQuery(userId)
                .variableName(varName).and().regex().variableValue("string*").build().getResultList();
        assertNotNull(tasksByVariable);
        assertEquals(1, tasksByVariable.size());

        //search with name and value wild card
        tasksByVariable = taskService.taskSummaryQuery(userId)
                .regex().variableName("first*").and().variableValue("string*").build().getResultList();
        assertNotNull(tasksByVariable);
        assertEquals(1, tasksByVariable.size());

        // search with unauthorized user
        tasksByVariable = taskService.taskSummaryQuery("WinterMute")
                .regex().variableName(varName).and().variableValue(varValue).build().getResultList();
        assertNotNull(tasksByVariable);
        assertEquals(0, tasksByVariable.size());

        // search with non existing variable
        tasksByVariable = taskService.taskSummaryQuery(userId)
                .regex().variableName("nonexistingvariable").and().variableValue(varValue).build().getResultList();
        assertNotNull(tasksByVariable);
        assertEquals(0, tasksByVariable.size());

        // search with not matching value
        tasksByVariable = taskService.taskSummaryQuery(userId)
                .regex().variableName(varName).and().variableValue("updated content").build().getResultList();
        assertNotNull(tasksByVariable);
        assertEquals(0, tasksByVariable.size());
    }

    @Test
    public void testVariableIndexInputAndOutputWitlLongText() {
        Task task = new TaskFluent().setName("This is my task name")
                .addPotentialGroup("Knights Templer")
                .setAdminUser("Administrator")
                .getTask();

        Map<String, Object> inputVariables = new HashMap<String, Object>();
        inputVariables.put("firstVariable", "string content");
        inputVariables.put("number", 1234);

        taskService.addTask(task, inputVariables);
        long taskId = task.getId();

        List<TaskSummary> allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("salaboy", null, null, null);
        assertEquals(1, allGroupAuditTasks.size());
        assertTrue(allGroupAuditTasks.get(0).getStatusId().equals("Ready"));

        List<TaskVariable> inputVars = taskAuditService.taskVariableQuery()
                .taskId(taskId).intersect().type(VariableType.INPUT).build().getResultList();
        assertNotNull(inputVars);
        assertEquals(2, inputVars.size());

        Map<String, String> vars = collectVariableNameAndValue(inputVars);

        assertTrue(vars.containsKey("firstVariable"));
        assertTrue(vars.containsKey("number"));

        assertEquals("string content", vars.get("firstVariable"));
        assertEquals("1234", vars.get("number"));

        taskService.claim(taskId, "Darth Vader");

        allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("salaboy", null, null, null);
        assertEquals(0, allGroupAuditTasks.size());

        allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("Darth Vader", null, null, null);
        assertEquals(1, allGroupAuditTasks.size());
        assertTrue(allGroupAuditTasks.get(0).getStatusId().equals("Reserved"));

        taskService.start(taskId, "Darth Vader");

        Task task1 = taskService.getTaskById(taskId);
        assertEquals(Status.InProgress, task1.getTaskData().getStatus());
        assertEquals("Darth Vader", task1.getTaskData().getActualOwner().getId());

        String reply = "Just a short part of the reply";
        String veryLongReply = reply;

        for (int i = 0; i < 15; i++) {
            veryLongReply += reply;
        }

        Map<String, Object> outputVariables = new HashMap<String, Object>();
        outputVariables.put("reply", veryLongReply);
        outputVariables.put("age", 25);

        // Check is Complete
        taskService.complete(taskId, "Darth Vader", outputVariables);

        List<TaskVariable> outputVars = taskAuditService.taskVariableQuery()
                .taskId(taskId).intersect().type(VariableType.OUTPUT).build().getResultList();
        assertNotNull(outputVars);
        assertEquals(2, outputVars.size());

        Map<String, String> outvars = collectVariableNameAndValue(outputVars);

        assertTrue(outvars.containsKey("reply"));
        assertTrue(outvars.containsKey("age"));

        assertEquals(veryLongReply, outvars.get("reply"));
        assertEquals("25", outvars.get("age"));
    }

    @Test
    public void testVariableIndexInputAndOutputWitlLongTextTrimmed() {
        System.setProperty("org.jbpm.task.var.log.length", "10");
        try {
            Task task = new TaskFluent().setName("This is my task name")
                    .addPotentialGroup("Knights Templer")
                    .setAdminUser("Administrator")
                    .getTask();

            Map<String, Object> inputVariables = new HashMap<String, Object>();
            inputVariables.put("firstVariable", "string content");
            inputVariables.put("number", 1234);

            taskService.addTask(task, inputVariables);
            long taskId = task.getId();

            List<TaskSummary> allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("salaboy", null, null, null);
            assertEquals(1, allGroupAuditTasks.size());
            assertTrue(allGroupAuditTasks.get(0).getStatusId().equals("Ready"));

            List<TaskVariable> inputVars = taskAuditService.taskVariableQuery()
                    .taskId(taskId).intersect().type(VariableType.INPUT).build().getResultList();
            assertNotNull(inputVars);
            assertEquals(2, inputVars.size());

            Map<String, String> vars = collectVariableNameAndValue(inputVars);

            assertTrue(vars.containsKey("firstVariable"));
            assertTrue(vars.containsKey("number"));
            // the variable was longer that 10 so it had to be trimmed
            assertEquals("string con", vars.get("firstVariable"));
            assertEquals("1234", vars.get("number"));

            taskService.claim(taskId, "Darth Vader");

            allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("salaboy", null, null, null);
            assertEquals(0, allGroupAuditTasks.size());

            allGroupAuditTasks = taskService.getTasksAssignedAsPotentialOwner("Darth Vader", null, null, null);
            assertEquals(1, allGroupAuditTasks.size());
            assertTrue(allGroupAuditTasks.get(0).getStatusId().equals("Reserved"));

            taskService.start(taskId, "Darth Vader");

            Task task1 = taskService.getTaskById(taskId);
            assertEquals(Status.InProgress, task1.getTaskData().getStatus());
            assertEquals("Darth Vader", task1.getTaskData().getActualOwner().getId());

            String reply = "Just a short part of the reply";
            String veryLongReply = reply;

            for (int i = 0; i < 15; i++) {
                veryLongReply += reply;
            }

            Map<String, Object> outputVariables = new HashMap<String, Object>();
            outputVariables.put("reply", veryLongReply);
            outputVariables.put("age", 25);

            // Check is Complete
            taskService.complete(taskId, "Darth Vader", outputVariables);

            List<TaskVariable> outputVars = taskAuditService.taskVariableQuery()
                    .taskId(taskId).intersect().type(VariableType.OUTPUT).build().getResultList();
            assertNotNull(outputVars);
            assertEquals(2, outputVars.size());

            Map<String, String> outvars = collectVariableNameAndValue(outputVars);

            assertTrue(outvars.containsKey("reply"));
            assertTrue(outvars.containsKey("age"));

            assertEquals("Just a sho", outvars.get("reply"));
            assertEquals("25", outvars.get("age"));
        } finally {
            System.clearProperty("org.jbpm.task.var.log.length");
        }
    }

    @Test
    public void testLifeCycleWithBAM() {
        Task task = new TaskFluent().setName("This is my task name")
                .addPotentialGroup("Knights Templer")
                .setAdminUser("Administrator")
                .getTask();

        taskService.addTask(task, new HashMap<String, Object>());
        long taskId = task.getId();

        List<AuditTask> allGroupAuditTasks = taskAuditService.getAllGroupAuditTasksByUser("Knights Templer", new QueryFilter());
        assertEquals(1, allGroupAuditTasks.size());
        assertTrue(allGroupAuditTasks.get(0).getStatus().equals("Ready"));
        assertBAMTask(taskId, "Ready");

        taskService.claim(taskId, "Darth Vader");

        allGroupAuditTasks = taskAuditService.getAllAuditTasksByUser("Darth Vader", new QueryFilter());
        assertEquals(1, allGroupAuditTasks.size());
        assertEquals("Reserved", allGroupAuditTasks.get(0).getStatus());
        assertBAMTask(taskId, "Reserved");

        taskService.release(taskId, "Darth Vader");

        allGroupAuditTasks = taskAuditService.getAllGroupAuditTasksByUser("Knights Templer", new QueryFilter());
        assertEquals(1, allGroupAuditTasks.size());
        assertTrue(allGroupAuditTasks.get(0).getStatus().equals("Ready"));
        assertBAMTask(taskId, "Ready");

        taskService.claim(taskId, "Darth Vader");

        allGroupAuditTasks = taskAuditService.getAllAuditTasksByUser("Darth Vader", new QueryFilter());;
        assertEquals(1, allGroupAuditTasks.size());
        assertEquals("Reserved", allGroupAuditTasks.get(0).getStatus());
        assertBAMTask(taskId, "Reserved");

        // Go straight from Ready to Inprogress
        taskService.start(taskId, "Darth Vader");

        allGroupAuditTasks = taskAuditService.getAllAuditTasksByUser("Darth Vader", new QueryFilter());;
        assertEquals(1, allGroupAuditTasks.size());
        assertEquals("InProgress", allGroupAuditTasks.get(0).getStatus());
        assertBAMTask(taskId, "InProgress");

        taskService.stop(taskId, "Darth Vader");

        allGroupAuditTasks = taskAuditService.getAllAuditTasksByUser("Darth Vader", new QueryFilter());;
        assertEquals(1, allGroupAuditTasks.size());
        assertEquals("Reserved", allGroupAuditTasks.get(0).getStatus());
        assertBAMTask(taskId, "Reserved");

        taskService.start(taskId, "Darth Vader");

        allGroupAuditTasks = taskAuditService.getAllAuditTasksByUser("Darth Vader", new QueryFilter());;
        assertEquals(1, allGroupAuditTasks.size());
        assertEquals("InProgress", allGroupAuditTasks.get(0).getStatus());
        assertBAMTask(taskId, "InProgress");

        // Check is Complete
        taskService.complete(taskId, "Darth Vader", null);

        allGroupAuditTasks = taskAuditService.getAllAuditTasksByUser("Darth Vader", new QueryFilter());;
        assertEquals(1, allGroupAuditTasks.size());
        assertEquals("Completed", allGroupAuditTasks.get(0).getStatus());
        assertBAMTask(taskId, "Completed");

        List<TaskEvent> taskEvents = taskAuditService.getAllTaskEvents(taskId, new QueryFilter());

        Assertions.assertThat(taskEvents).hasSize(9);

        Assertions.assertThat(taskEvents.get(0).getType()).isEqualTo(TaskEventType.ADDED);
        Assertions.assertThat(taskEvents.get(0).getUserId()).isNull();

        Assertions.assertThat(taskEvents.get(1).getType()).isEqualTo(TaskEventType.ACTIVATED);
        Assertions.assertThat(taskEvents.get(1).getUserId()).isNull();

        Assertions.assertThat(taskEvents.get(2).getType()).isEqualTo(TaskEventType.CLAIMED);
        Assertions.assertThat(taskEvents.get(2).getUserId()).isEqualTo("Darth Vader");

        Assertions.assertThat(taskEvents.get(3).getType()).isEqualTo(TaskEventType.RELEASED);
        Assertions.assertThat(taskEvents.get(3).getUserId()).isEqualTo("Darth Vader");

        Assertions.assertThat(taskEvents.get(4).getType()).isEqualTo(TaskEventType.CLAIMED);
        Assertions.assertThat(taskEvents.get(4).getUserId()).isEqualTo("Darth Vader");

        Assertions.assertThat(taskEvents.get(5).getType()).isEqualTo(TaskEventType.STARTED);
        Assertions.assertThat(taskEvents.get(5).getUserId()).isEqualTo("Darth Vader");

        Assertions.assertThat(taskEvents.get(6).getType()).isEqualTo(TaskEventType.STOPPED);
        Assertions.assertThat(taskEvents.get(6).getUserId()).isEqualTo("Darth Vader");

        Assertions.assertThat(taskEvents.get(7).getType()).isEqualTo(TaskEventType.STARTED);
        Assertions.assertThat(taskEvents.get(7).getUserId()).isEqualTo("Darth Vader");

        Assertions.assertThat(taskEvents.get(8).getType()).isEqualTo(TaskEventType.COMPLETED);
        Assertions.assertThat(taskEvents.get(8).getUserId()).isEqualTo("Darth Vader");
    }

    @Test
    public void testLifeCycleWithBAMEndWithExited() {
        Task task = new TaskFluent().setName("This is my task name")
                .addPotentialGroup("Knights Templer")
                .setAdminUser("Administrator")
                .getTask();

        taskService.addTask(task, new HashMap<String, Object>());
        long taskId = task.getId();

        List<AuditTask> allGroupAuditTasks = taskAuditService.getAllGroupAuditTasksByUser("Knights Templer", new QueryFilter());
        assertEquals(1, allGroupAuditTasks.size());
        assertTrue(allGroupAuditTasks.get(0).getStatus().equals("Ready"));
        assertBAMTask(taskId, "Ready");

        taskService.claim(taskId, "Darth Vader");

        allGroupAuditTasks = taskAuditService.getAllAuditTasksByUser("Darth Vader", new QueryFilter());
        assertEquals(1, allGroupAuditTasks.size());
        assertEquals("Reserved", allGroupAuditTasks.get(0).getStatus());
        assertBAMTask(taskId, "Reserved");

        taskService.release(taskId, "Darth Vader");

        allGroupAuditTasks = taskAuditService.getAllGroupAuditTasksByUser("Knights Templer", new QueryFilter());
        assertEquals(1, allGroupAuditTasks.size());
        assertTrue(allGroupAuditTasks.get(0).getStatus().equals("Ready"));
        assertBAMTask(taskId, "Ready");

        taskService.claim(taskId, "Darth Vader");

        allGroupAuditTasks = taskAuditService.getAllAuditTasksByUser("Darth Vader", new QueryFilter());;
        assertEquals(1, allGroupAuditTasks.size());
        assertEquals("Reserved", allGroupAuditTasks.get(0).getStatus());
        assertBAMTask(taskId, "Reserved");

        // Go straight from Ready to Inprogress
        taskService.start(taskId, "Darth Vader");

        allGroupAuditTasks = taskAuditService.getAllAuditTasksByUser("Darth Vader", new QueryFilter());;
        assertEquals(1, allGroupAuditTasks.size());
        assertEquals("InProgress", allGroupAuditTasks.get(0).getStatus());
        assertBAMTask(taskId, "InProgress");

        taskService.exit(taskId, "Administrator");

        allGroupAuditTasks = taskAuditService.getAllAuditTasks(new QueryFilter());;
        assertEquals(1, allGroupAuditTasks.size());
        assertEquals("Exited", allGroupAuditTasks.get(0).getStatus());
        assertBAMTask(taskId, "Exited");

    }

    @Test
    public void testLifeCycleWithBAMEndWithObsolete() {
        Task task = new TaskFluent().setName("This is my task name")
                .addPotentialGroup("Knights Templer")
                .setAdminUser("Administrator")
                .getTask();
        ((InternalTaskData)task.getTaskData()).setSkipable(true);

        taskService.addTask(task, new HashMap<String, Object>());
        long taskId = task.getId();

        List<AuditTask> allGroupAuditTasks = taskAuditService.getAllGroupAuditTasksByUser("Knights Templer", new QueryFilter());
        assertEquals(1, allGroupAuditTasks.size());
        assertTrue(allGroupAuditTasks.get(0).getStatus().equals("Ready"));
        assertBAMTask(taskId, "Ready");

        taskService.claim(taskId, "Darth Vader");

        allGroupAuditTasks = taskAuditService.getAllAuditTasksByUser("Darth Vader", new QueryFilter());
        assertEquals(1, allGroupAuditTasks.size());
        assertEquals("Reserved", allGroupAuditTasks.get(0).getStatus());
        assertBAMTask(taskId, "Reserved");

        taskService.release(taskId, "Darth Vader");

        allGroupAuditTasks = taskAuditService.getAllGroupAuditTasksByUser("Knights Templer", new QueryFilter());
        assertEquals(1, allGroupAuditTasks.size());
        assertTrue(allGroupAuditTasks.get(0).getStatus().equals("Ready"));
        assertBAMTask(taskId, "Ready");

        taskService.claim(taskId, "Darth Vader");

        allGroupAuditTasks = taskAuditService.getAllAuditTasksByUser("Darth Vader", new QueryFilter());;
        assertEquals(1, allGroupAuditTasks.size());
        assertEquals("Reserved", allGroupAuditTasks.get(0).getStatus());
        assertBAMTask(taskId, "Reserved");

        // Go straight from Ready to Inprogress
        taskService.start(taskId, "Darth Vader");

        allGroupAuditTasks = taskAuditService.getAllAuditTasksByUser("Darth Vader", new QueryFilter());;
        assertEquals(1, allGroupAuditTasks.size());
        assertEquals("InProgress", allGroupAuditTasks.get(0).getStatus());
        assertBAMTask(taskId, "InProgress");

        taskService.skip(taskId, "Darth Vader");

        allGroupAuditTasks = taskAuditService.getAllAuditTasksByUser("Darth Vader", new QueryFilter());;
        assertEquals(1, allGroupAuditTasks.size());
        assertEquals("Obsolete", allGroupAuditTasks.get(0).getStatus());
        assertBAMTask(taskId, "Obsolete");

    }

    @Test
    public void testLifeCycleWithBAMEndWithFailed() {
        Task task = new TaskFluent().setName("This is my task name")
                .addPotentialGroup("Knights Templer")
                .setAdminUser("Administrator")
                .getTask();

        taskService.addTask(task, new HashMap<String, Object>());
        long taskId = task.getId();

        List<AuditTask> allGroupAuditTasks = taskAuditService.getAllGroupAuditTasksByUser("Knights Templer", new QueryFilter());
        assertEquals(1, allGroupAuditTasks.size());
        assertTrue(allGroupAuditTasks.get(0).getStatus().equals("Ready"));
        assertBAMTask(taskId, "Ready");

        taskService.claim(taskId, "Darth Vader");

        allGroupAuditTasks = taskAuditService.getAllAuditTasksByUser("Darth Vader", new QueryFilter());
        assertEquals(1, allGroupAuditTasks.size());
        assertEquals("Reserved", allGroupAuditTasks.get(0).getStatus());
        assertBAMTask(taskId, "Reserved");

        taskService.release(taskId, "Darth Vader");

        allGroupAuditTasks = taskAuditService.getAllGroupAuditTasksByUser("Knights Templer", new QueryFilter());
        assertEquals(1, allGroupAuditTasks.size());
        assertTrue(allGroupAuditTasks.get(0).getStatus().equals("Ready"));
        assertBAMTask(taskId, "Ready");

        taskService.claim(taskId, "Darth Vader");

        allGroupAuditTasks = taskAuditService.getAllAuditTasksByUser("Darth Vader", new QueryFilter());;
        assertEquals(1, allGroupAuditTasks.size());
        assertEquals("Reserved", allGroupAuditTasks.get(0).getStatus());
        assertBAMTask(taskId, "Reserved");

        // Go straight from Ready to Inprogress
        taskService.start(taskId, "Darth Vader");

        allGroupAuditTasks = taskAuditService.getAllAuditTasksByUser("Darth Vader", new QueryFilter());;
        assertEquals(1, allGroupAuditTasks.size());
        assertEquals("InProgress", allGroupAuditTasks.get(0).getStatus());
        assertBAMTask(taskId, "InProgress");

        taskService.fail(taskId, "Darth Vader", null);

        allGroupAuditTasks = taskAuditService.getAllAuditTasksByUser("Darth Vader", new QueryFilter());;
        assertEquals(1, allGroupAuditTasks.size());
        assertEquals("Failed", allGroupAuditTasks.get(0).getStatus());
        assertBAMTask(taskId, "Failed");

    }

    @Test
    public void testUpdateTaskContentEvents() {
        Task task = new TaskFluent().setName("This is my task name")
                .addPotentialGroup("Knights Templer")
                .setAdminUser("Administrator")
                .getTask();

        taskService.addTask(task, new HashMap<String, Object>());
        long taskId = task.getId();

        assertAuditTaskInfoGroup("Knights Templer", "Ready", taskId);

        taskService.claim(taskId, "Darth Vader");

        assertAuditTaskInfo("Darth Vader", "Reserved", taskId);

        // Go straight from Ready to Inprogress
        taskService.start(taskId, "Darth Vader");

        assertAuditTaskInfo("Darth Vader", "InProgress", taskId);

        Map<String, Object> params = new HashMap<>();
        params.put("test", "value");
        taskService.addOutputContentFromUser(taskId, "Darth Vader", params);

        // Check is Complete
        params.clear();
        params.put("test", "updated");
        taskService.complete(taskId, "Darth Vader", params);

        assertAuditTaskInfo("Darth Vader", "Completed", taskId);

        List<TaskEvent> taskEvents = taskAuditService.getAllTaskEvents(taskId, new QueryFilter());

        Assertions.assertThat(taskEvents).hasSize(7);

        Assertions.assertThat(taskEvents.get(0).getType()).isEqualTo(TaskEventType.ADDED);
        Assertions.assertThat(taskEvents.get(0).getUserId()).isNull();

        Assertions.assertThat(taskEvents.get(1).getType()).isEqualTo(TaskEventType.ACTIVATED);
        Assertions.assertThat(taskEvents.get(1).getUserId()).isNull();

        Assertions.assertThat(taskEvents.get(2).getType()).isEqualTo(TaskEventType.CLAIMED);
        Assertions.assertThat(taskEvents.get(2).getUserId()).isEqualTo("Darth Vader");

        Assertions.assertThat(taskEvents.get(3).getType()).isEqualTo(TaskEventType.STARTED);
        Assertions.assertThat(taskEvents.get(3).getUserId()).isEqualTo("Darth Vader");
        // first update of data explicitly
        Assertions.assertThat(taskEvents.get(4).getType()).isEqualTo(TaskEventType.UPDATED);
        Assertions.assertThat(taskEvents.get(4).getUserId()).isEqualTo("Darth Vader");
        // next update of data through complete
        Assertions.assertThat(taskEvents.get(5).getType()).isEqualTo(TaskEventType.UPDATED);
        Assertions.assertThat(taskEvents.get(5).getUserId()).isEqualTo("Darth Vader");

        Assertions.assertThat(taskEvents.get(6).getType()).isEqualTo(TaskEventType.COMPLETED);
        Assertions.assertThat(taskEvents.get(6).getUserId()).isEqualTo("Darth Vader");
    }

    @Test
    public void testForwardTaskWithMsgEvents() {
        Task task = new TaskFluent().setName("This is my task name")
                .setAdminUser("Administrator")
                .addPotentialUser("Administrator")
                .addPotentialUser("Darth Vader")
                .getTask();

        taskService.addTask(task, new HashMap<String, Object>());
        long taskId = task.getId();

        taskService.claim(taskId, "Administrator");

        assertAuditTaskInfo("Administrator", "Reserved", taskId);

        taskService.forward(taskId, "Administrator", "Darth Vader");

        List<TaskEvent> taskEvents = taskAuditService.getAllTaskEvents(taskId, new QueryFilter());

        Assertions.assertThat(taskEvents).hasSize(4);

        Assertions.assertThat(taskEvents.get(0).getType()).isEqualTo(TaskEventType.ADDED);
        Assertions.assertThat(taskEvents.get(0).getUserId()).isNull();

        Assertions.assertThat(taskEvents.get(1).getType()).isEqualTo(TaskEventType.ACTIVATED);
        Assertions.assertThat(taskEvents.get(1).getUserId()).isNull();

        Assertions.assertThat(taskEvents.get(2).getType()).isEqualTo(TaskEventType.CLAIMED);
        Assertions.assertThat(taskEvents.get(2).getUserId()).isEqualTo("Administrator");

        Assertions.assertThat(taskEvents.get(3).getType()).isEqualTo(TaskEventType.FORWARDED);
        Assertions.assertThat(taskEvents.get(3).getUserId()).isEqualTo("Administrator");
        Assertions.assertThat(taskEvents.get(3).getMessage()).isNotNull();
        Assertions.assertThat(taskEvents.get(3).getMessage()).contains("Darth Vader");
    }

    @Test
    public void testAddTaskWithEventDate() {
        Task task = new TaskFluent().setName("This is my task name")
                .setAdminUser("Administrator")
                .addPotentialUser("Darth Vader")
                .getTask();

        taskService.addTask(task, new HashMap<String, Object>());
        long taskId = task.getId();

        List<TaskEvent> taskEvents = taskAuditService.getAllTaskEvents(taskId, new QueryFilter());
        Assertions.assertThat(taskEvents).hasSize(2);
        Assertions.assertThat(taskEvents.get(0).getType()).isEqualTo(TaskEventType.ADDED);
        Assertions.assertThat(taskEvents.get(0).getUserId()).isNull();

        List<AuditTask> allGroupAuditTasks = taskAuditService.getAllAuditTasks(new QueryFilter());
        assertEquals(1, allGroupAuditTasks.size());
        AuditTaskImpl auditTask = (AuditTaskImpl)allGroupAuditTasks.get(0);
        assertNotNull(auditTask.getLastModificationDate());
    }

    @Test
    public void testPagination() {
        for (int i = 0; i < 10; i++) {
            Task task = new TaskFluent().setName("My Task " + (i + 1))
                    .addPotentialGroup("Knights Templer")
                    .setAdminUser("Administrator")
                    .getTask();
            taskService.addTask(task, new HashMap<>());
        }

        List<AuditTask> auditTasks = taskAuditService.getAllAuditTasks(new QueryFilter(0, -1));

        Assertions.assertThat(auditTasks).hasSize(10);

        auditTasks = taskAuditService.getAllAuditTasks(new QueryFilter(0, 0));

        Assertions.assertThat(auditTasks).hasSize(10);

        auditTasks = taskAuditService.getAllAuditTasks(new QueryFilter(0, 3));

        Assertions.assertThat(auditTasks).hasSize(3);

        auditTasks = taskAuditService.getAllAuditTasks(new QueryFilter(2, 5));

        Assertions.assertThat(auditTasks).hasSize(5);
        // There is no ordering out of the box, so at least check for duplicates
        Assertions.assertThat(auditTasks).extracting(AuditTask::getName).doesNotHaveDuplicates();
    }

    protected Map<String, String> collectVariableNameAndValue(List<TaskVariable> variables) {
        Map<String, String> nameValue = new HashMap<String, String>();

        for (TaskVariable taskVar : variables) {
            nameValue.put(taskVar.getName(), taskVar.getValue());
        }

        return nameValue;
    }

    protected void assertBAMTask(long taskId, String expectedStatus) {
        EntityManager em = getEntityManager();

        BAMTaskSummaryImpl task = (BAMTaskSummaryImpl) em.createQuery("select bt from BAMTaskSummaryImpl bt where bt.taskId = :taskId")
                .setParameter("taskId", taskId)
                .getSingleResult();

        assertNotNull(task);
        assertEquals(taskId, task.getTaskId());
        assertEquals(expectedStatus, task.getStatus());

        em.close();
    }

    protected void assertAuditTaskInfoGroup(String group, String status, Long taskId) {
        List<AuditTask> allGroupAuditTasks = taskAuditService.getAllGroupAuditTasksByUser(group, new QueryFilter());
        assertEquals(1, allGroupAuditTasks.size());
        assertTrue(allGroupAuditTasks.get(0).getStatus().equals(status));
        assertBAMTask(taskId, status);
    }

    protected void assertAuditTaskInfo(String user, String status, Long taskId) {
        List<AuditTask> allGroupAuditTasks = taskAuditService.getAllAuditTasksByUser(user, new QueryFilter());
        assertEquals(1, allGroupAuditTasks.size());
        assertTrue(allGroupAuditTasks.get(0).getStatus().equals(status));
        assertBAMTask(taskId, status);
    }

    protected abstract EntityManager getEntityManager();

}
