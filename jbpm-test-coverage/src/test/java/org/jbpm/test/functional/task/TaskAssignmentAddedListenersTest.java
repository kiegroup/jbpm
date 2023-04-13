/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.test.functional.task;

import org.apache.commons.lang3.mutable.MutableInt;
import org.jbpm.kie.services.impl.admin.commands.AddPeopleAssignmentsCommand;
import org.jbpm.services.task.events.DefaultTaskEventListener;
import org.jbpm.services.task.impl.model.UserImpl;
import org.jbpm.test.JbpmTestCase;
import org.junit.After;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskEvent;
import org.kie.api.task.TaskLifeCycleEventListener;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.OrganizationalEntity;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test listeners tied to adding assignments - RHPAM-4442
 */
public class TaskAssignmentAddedListenersTest extends JbpmTestCase {

    private KieSession ksession;
    private TaskService ts;


    List<OrganizationalEntity> beforePotentialOwners;
    List<OrganizationalEntity> beforeEntitiesToSet;
    List<OrganizationalEntity> afterPotentialOwners;
    List<OrganizationalEntity> afterEntitiesToSet;


    MutableInt triggeredBeforeListenerCounter;
    MutableInt triggeredAfterListenerCounter;




    private static final String PROCESS = "org/jbpm/test/functional/task/HumanTask-simple.bpmn2";
    private static final String PROCESS_ID = "org.jbpm.test.functional.task.HumanTask-simple";


    private void init() {
        createRuntimeManager(PROCESS);
        RuntimeEngine runtimeEngine = getRuntimeEngine();
        ksession = runtimeEngine.getKieSession();
        ts = runtimeEngine.getTaskService();
    }

    @After
    public void clenaup() {
        if (ksession != null) {
            ksession.dispose();
        }
        disposeRuntimeManager();
    }

    @Test
    public void testAddedAssignmentListenersThreeParamApiRegression() {
        DefaultTaskEventListener listener = new DefaultTaskEventListener() {
            @Override
            public void beforeTaskAssignmentsAddedEvent(TaskEvent event, AssignmentType type, List<OrganizationalEntity> entities) {
                triggeredBeforeListenerCounter.increment();
                beforePotentialOwners.addAll(event.getTask().getPeopleAssignments().getPotentialOwners());
                beforeEntitiesToSet.addAll(entities);
                logger.debug("before potentialOwners: " + beforePotentialOwners);
                logger.debug("before entities: " + beforeEntitiesToSet);
            }

            @Override
            public void afterTaskAssignmentsAddedEvent(TaskEvent event, AssignmentType type, List<OrganizationalEntity> entities) {
                triggeredAfterListenerCounter.increment();
                afterPotentialOwners.addAll(event.getTask().getPeopleAssignments().getPotentialOwners());
                afterEntitiesToSet.addAll(entities);

                logger.debug("after potentialOwners: " + afterPotentialOwners);
                logger.debug("after entities: " + afterEntitiesToSet);
            }

        };
        testRegression(listener);
    }

    @Test
    public void testAddedAssignmentListenersFourParamApiRegression() {

        TaskLifeCycleEventListener listener = new DefaultTaskEventListener() {
            @Override
            public void beforeTaskAssignmentsAddedEvent(TaskEvent event, AssignmentType type, List<OrganizationalEntity> entities, List<OrganizationalEntity> beforeChangeEntities) {
                triggeredBeforeListenerCounter.increment();
                beforePotentialOwners.addAll(event.getTask().getPeopleAssignments().getPotentialOwners());
                beforeEntitiesToSet.addAll(entities);
                logger.debug("before potentialOwners: " + beforePotentialOwners);
                logger.debug("before entities: " + beforeEntitiesToSet);
            }

            @Override
            public void afterTaskAssignmentsAddedEvent(TaskEvent event, AssignmentType type, List<OrganizationalEntity> entities, List<OrganizationalEntity> afterChangeEntities) {
                triggeredAfterListenerCounter.increment();
                afterPotentialOwners.addAll(event.getTask().getPeopleAssignments().getPotentialOwners());
                afterEntitiesToSet.addAll(entities);

                logger.debug("after potentialOwners: " + afterPotentialOwners);
                logger.debug("after entities: " + afterEntitiesToSet);
            }
        };

        testRegression(listener);
    }

    @Test
    public void testAddedAssignmentListenersFourParamApi() {
        triggeredBeforeListenerCounter = new MutableInt(0);
        triggeredAfterListenerCounter = new MutableInt(0);

        List<OrganizationalEntity> beforeChangeEntitiesResult = new ArrayList<>();
        List<OrganizationalEntity> afterChangeEntitiesResult = new ArrayList<>();

        TaskLifeCycleEventListener listener = new DefaultTaskEventListener() {
            @Override
            public void beforeTaskAssignmentsAddedEvent(TaskEvent event, AssignmentType type, List<OrganizationalEntity> entities, List<OrganizationalEntity> beforeChangeEntities) {
                triggeredBeforeListenerCounter.increment();
                beforeChangeEntitiesResult.addAll(beforeChangeEntities);
                logger.debug("beforeChangeEntities: " + beforeChangeEntities);
            }

            @Override
            public void afterTaskAssignmentsAddedEvent(TaskEvent event, AssignmentType type, List<OrganizationalEntity> entities, List<OrganizationalEntity> afterChangeEntities) {
                triggeredAfterListenerCounter.increment();
                afterChangeEntitiesResult.addAll(afterChangeEntities);
                logger.debug("afterChangeEntities: " + afterChangeEntities);

            }
        };

        addTaskEventListener(listener);

        init();

        ProcessInstance pi = ksession.startProcess(PROCESS_ID);

        long pid = pi.getId();

        assertProcessInstanceActive(pi.getId(), ksession);
        assertNodeTriggered(pi.getId(),  "Start", "Task");

        long taskId = ts.getTasksByProcessInstanceId(pid).get(0);

        UserImpl john = new UserImpl("john");
        UserImpl mary = new UserImpl("mary");
        UserImpl jim = new UserImpl("jim");

        //add assignment
        ts.execute(new AddPeopleAssignmentsCommand("Administrator", taskId, 0, new UserImpl[]{john}, false));
        assertThat(beforeChangeEntitiesResult).isEmpty();

        assertThat(afterChangeEntitiesResult).hasSize(1);
        assertThat(afterChangeEntitiesResult).contains(john);

        clearLists(beforeChangeEntitiesResult, afterChangeEntitiesResult);

        // Add one more without removing existing
        ts.execute(new AddPeopleAssignmentsCommand("Administrator", taskId, 0, new UserImpl[]{mary}, false));
        assertThat(beforeChangeEntitiesResult).hasSize(1);
        assertThat(beforeChangeEntitiesResult).contains(john).doesNotContain(mary);

        assertThat(afterChangeEntitiesResult).hasSize(2);
        assertThat(afterChangeEntitiesResult).contains(john, mary);

        clearLists(beforeChangeEntitiesResult, afterChangeEntitiesResult);

        // Add one more but with removing existing ones
        ts.execute(new AddPeopleAssignmentsCommand("Administrator", taskId, 0, new UserImpl[]{jim}, true));
        assertThat(beforeChangeEntitiesResult).hasSize(2);
        assertThat(beforeChangeEntitiesResult).contains(john, mary).doesNotContain(jim);

        assertThat(afterChangeEntitiesResult).hasSize(1);
        assertThat(afterChangeEntitiesResult).contains(jim).doesNotContain(john, mary);

        clearLists(beforeChangeEntitiesResult, afterChangeEntitiesResult);

        assertThat(triggeredBeforeListenerCounter.getValue()).isEqualTo(3);
        assertThat(triggeredAfterListenerCounter.getValue()).isEqualTo(3);
    }

    private void testRegression(TaskLifeCycleEventListener listener) {
        triggeredBeforeListenerCounter = new MutableInt(0);
        triggeredAfterListenerCounter = new MutableInt(0);

        beforePotentialOwners = new ArrayList<>();
        beforeEntitiesToSet = new ArrayList<>();
        afterPotentialOwners = new ArrayList<>();
        afterEntitiesToSet = new ArrayList<>();

        addTaskEventListener(listener);

        init();

        ProcessInstance pi = ksession.startProcess(PROCESS_ID);
        long pid = pi.getId();

        assertProcessInstanceActive(pi.getId(), ksession);
        assertNodeTriggered(pi.getId(),  "Start", "Task");

        long taskId = ts.getTasksByProcessInstanceId(pid).get(0);

        UserImpl john = new UserImpl("john");
        UserImpl mary = new UserImpl("mary");
        UserImpl jim = new UserImpl("jim");

        //add assignment
        ts.execute(new AddPeopleAssignmentsCommand("Administrator", taskId, 0, new UserImpl[]{john}, false));
        assertThat(beforePotentialOwners).hasSize(1);
        assertThat(beforePotentialOwners).contains(john);

        assertThat(beforeEntitiesToSet).hasSize(1);
        assertThat(beforeEntitiesToSet).contains(john);

        assertThat(afterPotentialOwners).hasSize(1);
        assertThat(afterPotentialOwners).contains(john);

        assertThat(afterEntitiesToSet).hasSize(1);
        assertThat(afterEntitiesToSet).contains(john);

        clearLists(beforePotentialOwners, beforeEntitiesToSet, afterPotentialOwners, afterEntitiesToSet);

        // Add one more without removing existing
        ts.execute(new AddPeopleAssignmentsCommand("Administrator", taskId, 0, new UserImpl[]{mary}, false));
        assertThat(beforePotentialOwners).hasSize(2);
        assertThat(beforePotentialOwners).contains(john, mary);

        assertThat(beforeEntitiesToSet).hasSize(1);
        assertThat(beforeEntitiesToSet).contains(mary);

        assertThat(afterPotentialOwners).hasSize(2);
        assertThat(afterPotentialOwners).contains(john, mary);

        assertThat(afterEntitiesToSet).hasSize(1);
        assertThat(afterEntitiesToSet).contains(mary);

        clearLists(beforePotentialOwners, beforeEntitiesToSet, afterPotentialOwners, afterEntitiesToSet);

        // Add one more but with removing existing ones
        ts.execute(new AddPeopleAssignmentsCommand("Administrator", taskId, 0, new UserImpl[]{jim}, true));
        assertThat(beforePotentialOwners).hasSize(1);
        assertThat(beforePotentialOwners).contains(jim).doesNotContain(john, mary);

        assertThat(beforeEntitiesToSet).hasSize(1);
        assertThat(beforeEntitiesToSet).contains(jim).doesNotContain(john, mary);

        assertThat(afterPotentialOwners).isEmpty();

        assertThat(afterEntitiesToSet).hasSize(1);
        assertThat(afterEntitiesToSet).contains(jim).doesNotContain(john, mary);

        clearLists(beforePotentialOwners, beforeEntitiesToSet, afterPotentialOwners, afterEntitiesToSet);

        assertThat(triggeredBeforeListenerCounter.getValue()).isEqualTo(3);
        assertThat(triggeredAfterListenerCounter.getValue()).isEqualTo(3);
    }

    @SafeVarargs
    private final void clearLists(List<OrganizationalEntity>... listsToClear) {
        for (List<OrganizationalEntity> l : listsToClear) {
            l.clear();
            assertThat(l).isEmpty();
        }
    }
}
