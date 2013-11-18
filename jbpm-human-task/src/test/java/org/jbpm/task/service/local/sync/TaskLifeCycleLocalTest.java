/**
 * Copyright 2010 JBoss Inc
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
package org.jbpm.task.service.local.sync;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.jbpm.task.Task;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.base.sync.TaskLifeCycleBaseSyncTest;
import org.jbpm.task.service.local.LocalTaskService;

public class TaskLifeCycleLocalTest extends TaskLifeCycleBaseSyncTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
       
        client = new LocalTaskService(taskService);
    }
    
    public void testModifyTaskName() {
        // JBPM-4148
    	Map<String, Object> vars = fillVariables(users, groups);
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [new User('Bobba Fet')  ],businessAdministrators = [ new User('Administrator') ], }),";
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
        
        Task task = (Task) eval(new StringReader(str), vars);
        client.addTask(task, null);
        
        
        List<TaskSummary> tasks = client.getTasksAssignedAsPotentialOwner("Bobba Fet", "en-UK");        
        assertEquals(1, tasks.size());
        assertEquals("This is my task name", tasks.get(0).getName());
        
        Task newTask = client.getTask(tasks.get(0).getId());
        newTask.getNames().get(0).setText("New task name");
        
        List<TaskSummary> newTasks = client.getTasksAssignedAsPotentialOwner("Bobba Fet", "en-UK");        
        assertEquals(1, newTasks.size());
        assertEquals("New task name", newTasks.get(0).getName());
    }

}
