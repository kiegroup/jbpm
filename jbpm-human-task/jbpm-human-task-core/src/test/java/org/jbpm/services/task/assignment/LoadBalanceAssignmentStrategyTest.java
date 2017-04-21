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

import static org.junit.Assert.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jbpm.services.task.HumanTaskServiceFactory;
import org.jbpm.services.task.assignment.impl.strategy.LoadBalanceAssignmentStrategy;
import org.jbpm.services.task.assignment.impl.strategy.RoundRobinAssignmentStrategy;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.InternalTaskService;

import bitronix.tm.resource.jdbc.PoolingDataSource;

public class LoadBalanceAssignmentStrategyTest extends AbstractAssignmentTests {
    private PoolingDataSource pds;
    private EntityManagerFactory emf;
//    private static final Logger
    private static final String BASE_TASK_INFO = "with (new Task()) { priority = 55, taskData = (with (new TaskData()) { } ), ";
    private static final String MULTI_ACTOR_ASSIGNMENTS = ""
    		+ "peopleAssignments = (with (new PeopleAssignments()) { potentialOwners = [new User('Bobba Fet'), new User('Darth Vader'), new User('Luke Cage')],"
            + " businessAdministrators = [new User('Administrator')], } ),";
    private static final String MULTI_ACTOR_WITH_GROUP_ASSIGNMENTS = ""
    		+ "peopleAssignments = (with (new PeopleAssignments()) { potentialOwners = [new User('Bobba Fet'), new Group('Crusaders'), new User('Luke Cage')],"
    		+ " businessAdministrators = [new User('Administrator')], } ),";
    private static final String ADD_ACTOR_ASSIGNMENTS = ""
    		+ "peopleAssignments = (with (new PeopleAssignments()) { potentialOwners = [new User('Bobba Fet'), new User('Darth Vader'), new User('Luke Cage'), new User('Tony Stark')],"
    		+ " businessAdministrators = [new User('Administrator')], } ),";
    private static final String REMOVE_ACTOR_ASSIGNMENTS = ""
    		+ "peopleAssignments = (with (new PeopleAssignments()) { potentialOwners = [new User('Bobba Fet'), new User('Luke Cage'), new User('Tony Stark')],"
    		+ " businessAdministrators = [new User('Administrator')], } ),";


	@Before
	public void setUp() throws Exception {
        System.setProperty("org.jbpm.task.assignment.enabled", "true");
        System.setProperty("org.jbpm.task.assignment.strategy", "LoadBalance");
        System.setProperty("org.jbpm.task.assignment.loadbalance.calculator","org.jbpm.services.task.assignment.impl.TaskCountLoadCalculator");
        pds = setupPoolingDataSource();
        emf = Persistence.createEntityManagerFactory( "org.jbpm.services.task" );

        AssignmentServiceProvider.override(new LoadBalanceAssignmentStrategy());

        this.taskService = (InternalTaskService) HumanTaskServiceFactory.newTaskServiceConfigurator()
                .entityManagerFactory(emf)
                .getTaskService();

	}

	@After
	public void clean() throws Exception {
        System.clearProperty("org.jbpm.task.assignment.enabled");
        System.clearProperty("org.jbpm.task.assignment.strategy");
        System.clearProperty("org.jbpm.task.assignment.loadbalance.calculator");
        AssignmentServiceProvider.clear();
        if (emf != null) {
            emf.close();
        }
        if (pds != null) {
            pds.close();
        }
	}

	@Test
	public void testMultipleUser() {
        final String taskString = "(" +
        		BASE_TASK_INFO +	
                 MULTI_ACTOR_ASSIGNMENTS +
                "name = 'MultiActorRoundRobinTask'})";
        Task tasks[] = new Task[10];
        tasks[0] = createForCompletionTask(taskString, "Darth Vader", 3, "Bobba Fet","Darth Vader","Luke Cage");
        tasks[1] = createForCompletionTask(taskString, "Bobba Fet", 3, "Bobba Fet","Darth Vader","Luke Cage");
        tasks[2] = createForCompletionTask(taskString, "Luke Cage", 3, "Bobba Fet","Darth Vader","Luke Cage");
        completeTask(tasks[1]);
        // Expect that the "round robin" will circle back to the beginning of the list
        tasks[3] = createForCompletionTask(taskString, "Bobba Fet", 3, "Bobba Fet","Darth Vader","Luke Cage");
        completeTask(tasks[2]);
        createForCompletionTask(taskString,"Luke Cage", 3, "Bobba Fet","Darth Vader","Luke Cage");
	}

}
