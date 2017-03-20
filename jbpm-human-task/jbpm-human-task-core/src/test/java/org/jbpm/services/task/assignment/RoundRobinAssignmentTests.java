/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.services.task.assignment;

import bitronix.tm.resource.jdbc.PoolingDataSource;
import org.jbpm.services.task.HumanTaskServiceFactory;
import org.jbpm.services.task.assignment.impl.strategy.RoundRobinAssignmentStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.internal.task.api.InternalTaskService;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class RoundRobinAssignmentTests extends AbstractAssignmentTests {
    private PoolingDataSource pds;
    private EntityManagerFactory emf;

    @Before
    public void setup() {
        System.setProperty("org.jbpm.task.assignment.enabled", "true");
        System.setProperty("org.jbpm.task.assignment.strategy", "RoundRobin");
        pds = setupPoolingDataSource();
        emf = Persistence.createEntityManagerFactory( "org.jbpm.services.task" );

        AssignmentServiceProvider.override(new RoundRobinAssignmentStrategy());

        this.taskService = (InternalTaskService) HumanTaskServiceFactory.newTaskServiceConfigurator()
                .entityManagerFactory(emf)
                .getTaskService();

    }

    @After
    public void clean() {
        System.clearProperty("org.jbpm.task.assignment.enabled");
        AssignmentServiceProvider.clear();
        if (emf != null) {
            emf.close();
        }
        if (pds != null) {
            pds.close();
        }
    }

    @Test
    public void testMultiActorRoundRobin() {
        final String taskString = "(with (new Task()) { priority = 55, taskData = (with (new TaskData()) { } ), " +
                "peopleAssignments = (with (new PeopleAssignments()) { potentialOwners = [new User('Bobba Fet'), new User('Darth Vader'), new User('Luke Cage')]," +
                "   businessAdministrators = [new User('Administrator')], } )," +
                "name = 'MultiActorRoundRobinTask'})";
        createAndAssertTask(taskString, "Bobba Fet", 3, "Bobba Fet","Darth Vader","Luke Cage");
        createAndAssertTask(taskString, "Darth Vader", 3, "Bobba Fet","Darth Vader","Luke Cage");
        createAndAssertTask(taskString, "Luke Cage", 3, "Bobba Fet","Darth Vader","Luke Cage");
        // Expect that the "round robin" will circle back to the beginning of the list
        createAndAssertTask(taskString, "Bobba Fet", 3, "Bobba Fet","Darth Vader","Luke Cage");
    }

    @Test
    public void testMultiActorWithGroupRoundRobin() {
        final String taskString = "(with (new Task()) { priority = 55, taskData = (with (new TaskData()) { } ), " +
                "peopleAssignments = (with (new PeopleAssignments()) { potentialOwners = [new User('Bobba Fet'), new Group('Crusaders'), new User('Luke Cage')]," +
                "   businessAdministrators = [new User('Administrator')], } )," +
                "name = 'MultiActorWithGroupRoundRobinTask'})";
        createAndAssertTask(taskString, "Bobba Fet", 3, "Bobba Fet","Crusaders","Luke Cage");
        createAndAssertTask(taskString, "Luke Cage", 3, "Bobba Fet","Crusaders","Luke Cage");
        createAndAssertTask(taskString, "Darth Vader", 3, "Bobba Fet","analyst","Luke Cage");
    }
}
