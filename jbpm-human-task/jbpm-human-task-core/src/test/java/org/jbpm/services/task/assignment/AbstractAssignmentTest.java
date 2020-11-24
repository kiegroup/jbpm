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

package org.jbpm.services.task.assignment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.services.task.HumanTaskServicesBaseTest;
import org.jbpm.services.task.impl.factories.TaskFactory;
import org.jbpm.test.listener.task.CountDownTaskEventListener;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.model.InternalTask;

public class AbstractAssignmentTest extends HumanTaskServicesBaseTest {

    protected void assertPotentialOwners(Task task, int expectedSize, String... expectedNames) {
        List<OrganizationalEntity> potentialOwners = task.getPeopleAssignments().getPotentialOwners();

        assertThat(potentialOwners).hasSize(expectedSize);

        if (expectedNames.length > 0) {
            assertThat(potentialOwners).as("No match for expected potential owner name")
                    .extracting(OrganizationalEntity::getId)
                    .contains(expectedNames);
        }
    }

    protected void assertActualOwner(Task task, String expectedActualOwner) {
        User actualOwner = task.getTaskData().getActualOwner();
        assertThat(actualOwner).as("No actual owner when expected").isNotNull();
        assertThat(actualOwner.getId()).as("Not matching actual owner").isEqualTo(expectedActualOwner);
    }

    protected void assertNoActualOwner(Task task) {
        assertThat(task.getTaskData().getActualOwner()).as("Actual owner present when not expected").isNull();
    }
    
    protected long createAndAssertTask(String taskExpression, String actualOwner, int expectedPotentialOwners,
            Map<String, Object> params, String... expectedPotentialOwnerNames) {
        Task task = TaskFactory.evalTask(new StringReader(taskExpression));
        assertPotentialOwners(task, expectedPotentialOwners);

        long taskId = taskService.addTask(task, params);

        task = taskService.getTaskById(taskId);
        assertPotentialOwners(task, expectedPotentialOwners, expectedPotentialOwnerNames);
        if (actualOwner != null) {
            assertActualOwner(task, actualOwner);
        } else
            assertNoActualOwner(task);

        return taskId;
    }

    protected long createAndAssertTask(String taskExpression, String actualOwner, int expectedPotentialOwners,
                                       String... expectedPotentialOwnerNames) {
        return createAndAssertTask(taskExpression, actualOwner, expectedPotentialOwners, Collections.emptyMap(), expectedPotentialOwnerNames);
    }
    
    protected long createAndAssertAfterDeadline(String str,
                                                String actualOwner,
                                                int expectedPotOwners,
                                                Map<String, Object> params,
                                                String... expectedPotOwnerNames) {
        CountDownTaskEventListener countDownListener = new CountDownTaskEventListener(1, true, false);
        addCountDownListener(countDownListener);

        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("now", new Date());

        Task task = (InternalTask) TaskFactory.evalTask(new StringReader(str), vars);

        taskService.addTask(task, params);

        // should have re-assigned by now
        countDownListener.waitTillCompleted();

        long taskId = task.getId();
        task = taskService.getTaskById(taskId);

        assertPotentialOwners(task, expectedPotOwners, expectedPotOwnerNames);
        if (actualOwner != null) {
            assertActualOwner(task, actualOwner);
        } else
            assertNoActualOwner(task);

        return taskId;
    }
    
    protected Task createForCompletionTask(String taskExpression, String actualOwner, int expectedPotOwners, String... expectedPotOwnerNames) {
    	Task task = TaskFactory.evalTask(new StringReader(taskExpression));
    	assertPotentialOwners(task, expectedPotOwners);
    	
    	taskService.addTask(task, new HashMap<String,Object>());
        assertPotentialOwners(task, expectedPotOwners, expectedPotOwnerNames);
        assertActualOwner(task, actualOwner);
    	
    	return task;
    }
    
    protected void assertNumberOfNonCompletedTasks(String user, int expectedNumber) {
    	int count = taskService.getTasksOwned(user, Arrays.asList(Status.Reserved,Status.Suspended,Status.InProgress), null).size();
    	assertEquals("Not matching number of non-completed tasks",expectedNumber,count);
    }
    
    protected void completeTask(Task task) {
    	taskService.start(task.getId(), task.getTaskData().getActualOwner().getId());
    	taskService.complete(task.getId(), task.getTaskData().getActualOwner().getId(), new HashMap<String,Object>());
    }
    
}
