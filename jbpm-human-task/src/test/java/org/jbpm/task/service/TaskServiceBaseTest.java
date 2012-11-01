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

package org.jbpm.task.service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.UniqueConstraint;

import org.jbpm.task.BaseTest;
import org.jbpm.task.Group;
import org.jbpm.task.Status;
import org.jbpm.task.Task;
import org.jbpm.task.User;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.TaskClientHandler.TaskSummaryResponseHandler;
import org.jbpm.task.service.responsehandlers.BlockingAddTaskResponseHandler;
import org.jbpm.task.service.responsehandlers.BlockingGetTaskResponseHandler;
import org.jbpm.task.service.responsehandlers.BlockingTaskOperationResponseHandler;
import org.jbpm.task.service.responsehandlers.BlockingTaskSummaryResponseHandler;
import org.jbpm.task.utils.CollectionUtils;

public abstract class TaskServiceBaseTest extends BaseTest {

	protected TaskServer server;
    protected TaskClient client;

    protected void tearDown() throws Exception {
        client.disconnect();
        server.stop();
        super.tearDown();
    }
    
    public void testTasksOwnedQueryWithI18N() throws Exception {
        runTestTasksOwnedQueryWithI18N(client, users, groups);
    }
    
    @SuppressWarnings("unchecked")
    public static void runTestTasksOwnedQueryWithI18N(TaskClient client, Map<String, User> users, Map<String, Group> groups) { 
        Map<String, Object>  vars = fillVariables(users, groups);
        
        BlockingAllOpenTasksForUseResponseHandler getTaskshandler = new BlockingAllOpenTasksForUseResponseHandler();
        client.getTasksOwned( users.get( "peter" ).getId(), "en-UK", getTaskshandler );
        int peterSize = getTaskshandler.getResults().size();
        getTaskshandler = new BlockingAllOpenTasksForUseResponseHandler();
        client.getTasksOwned( users.get( "steve" ).getId(), "en-UK", getTaskshandler );
        int steveSize = getTaskshandler.getResults().size();
        getTaskshandler = new BlockingAllOpenTasksForUseResponseHandler();
        client.getTasksOwned( users.get( "darth" ).getId(), "en-UK", getTaskshandler );
        int darthSize = getTaskshandler.getResults().size();
        
        Reader reader = new InputStreamReader( TaskServiceBaseTest.class.getResourceAsStream(MvelFilePath.TasksOwned) );
        List<Task> tasks = (List<Task>) eval( reader,
                                              vars );
        for ( Task task : tasks ) {
            BlockingAddTaskResponseHandler responseHandler = new BlockingAddTaskResponseHandler();
            client.addTask( task, null, responseHandler );
        }

        // Test UK I18N  
        reader = new InputStreamReader( TaskServiceBaseTest.class.getResourceAsStream(MvelFilePath.TasksOwnedInEnglish) );
        Map<String, List<TaskSummary>> expected = (Map<String, List<TaskSummary>>) eval( reader,
                                                                                         vars );

        BlockingAllOpenTasksForUseResponseHandler responseHandler = new BlockingAllOpenTasksForUseResponseHandler();
        client.getTasksOwned( users.get( "peter" ).getId(),
                              "en-UK",
                              responseHandler );
        List<TaskSummary> actual = responseHandler.getResults();
        assertEquals( peterSize + 3,
                      actual.size() );
        assertTrue( CollectionUtils.equals( expected.get( "peter" ), 
                                            actual.subList(0, expected.get("peter").size()) ) );

        responseHandler = new BlockingAllOpenTasksForUseResponseHandler();
        client.getTasksOwned( users.get( "steve" ).getId(),
                              "en-UK",
                              responseHandler );
        actual = responseHandler.getResults();
        assertEquals( steveSize + 2,
                      actual.size() );
        assertTrue( CollectionUtils.equals( expected.get( "steve" ),
                                            actual.subList(0, expected.get("steve").size()) ) );

        responseHandler = new BlockingAllOpenTasksForUseResponseHandler();
        client.getTasksOwned( users.get( "darth" ).getId(),
                              "en-UK",
                              responseHandler );
        actual = responseHandler.getResults();
        assertEquals( darthSize + 1,
                      actual.size() );
        assertTrue( CollectionUtils.equals( expected.get( "darth" ),
                                            actual.subList(0, expected.get("darth").size()) ) );

        // Test DK I18N 
        reader = new InputStreamReader( TaskServiceBaseTest.class.getResourceAsStream(MvelFilePath.TasksOwnedInGerman) );
        expected = (Map<String, List<TaskSummary>>) eval( reader,
                                                          vars );

        responseHandler = new BlockingAllOpenTasksForUseResponseHandler();
        client.getTasksOwned( users.get( "peter" ).getId(),
                              "en-DK",
                              responseHandler );
        actual = responseHandler.getResults();
        assertEquals( peterSize + 3,
                      actual.size() );
        assertTrue( CollectionUtils.equals( expected.get( "peter" ),
                                            actual.subList(0, expected.get("peter").size()) ) );

        responseHandler = new BlockingAllOpenTasksForUseResponseHandler();
        client.getTasksOwned( users.get( "steve" ).getId(),
                              "en-DK",
                              responseHandler );
        actual = responseHandler.getResults();
        assertEquals( steveSize + 2,
                      actual.size() );
        assertTrue( CollectionUtils.equals( expected.get( "steve" ),
                                            actual.subList(0, expected.get("steve").size()) ) );

        responseHandler = new BlockingAllOpenTasksForUseResponseHandler();
        client.getTasksOwned( users.get( "darth" ).getId(),
                              "en-DK",
                              responseHandler );
        actual = responseHandler.getResults();
        assertEquals( darthSize + 1,
                      actual.size() );
        assertTrue( CollectionUtils.equals( expected.get( "darth" ),
                                            actual.subList(0, expected.get("darth").size()) ) );
    }
    
    public void testPotentialOwnerQueries() {
        runTestPotentialOwnerQueries(client, users, groups);
    }
   
    @SuppressWarnings("unchecked")
    public static void runTestPotentialOwnerQueries(TaskClient client, Map<String, User> users, Map<String, Group> groups) { 
        Map<String, Object>  vars = fillVariables(users, groups);
        
        BlockingAllOpenTasksForUseResponseHandler getTasksHandler = new BlockingAllOpenTasksForUseResponseHandler();
        client.getTasksAssignedAsPotentialOwner(users.get( "bobba" ).getId(), "en-UK", getTasksHandler );
        List<TaskSummary> actual = getTasksHandler.getResults();
        int originalSize = actual.size();
        
        //Reader reader;
        Reader reader = new InputStreamReader( TaskServiceBaseTest.class.getResourceAsStream(MvelFilePath.TasksPotentialOwner) );
        List<Task> tasks = (List<Task>) eval( reader, vars );
        for ( Task task : tasks ) {
            BlockingAddTaskResponseHandler addTaskHandler = new BlockingAddTaskResponseHandler();
            client.addTask( task, null, addTaskHandler );
        }

        // Test UK I18N  
        getTasksHandler = new BlockingAllOpenTasksForUseResponseHandler();
        client.getTasksAssignedAsPotentialOwner(users.get( "bobba" ).getId(), "en-UK", getTasksHandler );
        actual = getTasksHandler.getResults();
        assertEquals( originalSize + 2, actual.size() );
    }

    public void testPeopleAssignmentQueries() {
        runTestPeopleAssignmentQueries(client, taskSession, users, groups);
    }
   
    @SuppressWarnings({"unchecked"})
    public static void runTestPeopleAssignmentQueries(TaskClient client, TaskServiceSession taskSession, Map<String, User> users, Map<String, Group> groups) { 
        Map<String, Object>  vars = fillVariables(users, groups);

        BlockingAllOpenTasksForUseResponseHandler getTasksHandler = new BlockingAllOpenTasksForUseResponseHandler();
        client.getTasksAssignedAsTaskInitiator( users.get( "darth" ).getId(), "en-UK", getTasksHandler );
        int darthSize = getTasksHandler.getResults().size();
        
        getTasksHandler = new BlockingAllOpenTasksForUseResponseHandler();
        client.getTasksAssignedAsExcludedOwner( users.get( "liz" ).getId(), "en-UK", getTasksHandler );
        int lizSize = getTasksHandler.getResults().size();
        
        getTasksHandler = new BlockingAllOpenTasksForUseResponseHandler();
        client.getTasksAssignedAsPotentialOwner( users.get( "bobba" ).getId(), "en-UK", getTasksHandler );
        int bobbaSize = getTasksHandler.getResults().size();
        
        getTasksHandler = new BlockingAllOpenTasksForUseResponseHandler();
        client.getTasksAssignedAsRecipient( users.get( "sly" ).getId(), "en-UK", getTasksHandler );
        int slySize = getTasksHandler.getResults().size();
        
        Reader reader = new InputStreamReader( TaskServiceBaseTest.class.getResourceAsStream(MvelFilePath.TasksOwned) );
        List<Task> tasks = (List<Task>) eval( reader,
                                              vars );
        for ( Task task : tasks ) {
            BlockingAddTaskResponseHandler addTaskHandler = new BlockingAddTaskResponseHandler();
            client.addTask(task, null, addTaskHandler);
        }

        reader = new InputStreamReader( TaskServiceBaseTest.class.getResourceAsStream( MvelFilePath.PeopleAssignmentQuerries ) );
        Map<String, List<TaskSummary>> expected = (Map<String, List<TaskSummary>>) eval( reader, vars );

        getTasksHandler = new BlockingAllOpenTasksForUseResponseHandler();
        client.getTasksAssignedAsTaskInitiator( users.get( "darth" ).getId(), "en-UK", getTasksHandler );
        List<TaskSummary> actual = getTasksHandler.getResults();
        assertEquals( darthSize + 1, actual.size() );
        assertTrue( CollectionUtils.equals( expected.get( "darth" ), actual.subList(0, 1) ) );

        getTasksHandler = new BlockingAllOpenTasksForUseResponseHandler();
        client.getTasksAssignedAsBusinessAdministrator( users.get( "steve" ).getId(), "en-UK", getTasksHandler );
        actual = getTasksHandler.getResults();
        assertTrue( CollectionUtils.equals( expected.get( "steve" ),
                                            actual.subList(0, expected.get("steve").size()) ) );

        getTasksHandler = new BlockingAllOpenTasksForUseResponseHandler();
        client.getTasksAssignedAsExcludedOwner( users.get( "liz" ).getId(), "en-UK", getTasksHandler );
        actual = getTasksHandler.getResults();
        assertEquals( lizSize + 2, actual.size() );
        assertTrue( CollectionUtils.equals( expected.get( "liz" ), actual.subList(0, expected.get("liz").size()) ) );

        getTasksHandler = new BlockingAllOpenTasksForUseResponseHandler();
        client.getTasksAssignedAsPotentialOwner( users.get( "bobba" ).getId(), "en-UK", getTasksHandler );
        actual = getTasksHandler.getResults();
        assertEquals( bobbaSize + 3, actual.size() );
        for( TaskSummary orig : expected.get("bobba") ) { 
            boolean matchFound = false;
            for( TaskSummary ts : actual ) { 
                if( orig.equals(ts) ) { 
                    matchFound = true;
                    break;
                }
            }
            assertTrue( matchFound );
        }

        getTasksHandler = new BlockingAllOpenTasksForUseResponseHandler();
        client.getTasksAssignedAsRecipient( users.get( "sly" ).getId(), "en-UK", getTasksHandler );
        actual = getTasksHandler.getResults();
        assertEquals( slySize + 1, actual.size() );
        assertTrue( CollectionUtils.equals( expected.get( "sly" ), actual.subList(0, expected.get("sly").size()) ) );
    }
    
    public void testCompleteTaskByProcessInstanceId() {
        Map<String, Object> vars = fillVariables(users, groups);
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { processInstanceId=99} ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId(); 
        
        str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { processInstanceId=500} ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'Another task')] })";
        addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task secondTask = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( secondTask, null, addTaskResponseHandler );
        
        BlockingTaskSummaryResponseHandler taskSummaryHandler = new BlockingTaskSummaryResponseHandler();
        client.getTasksByStatusByProcessId(99L, Collections.singletonList(Status.Ready), "en-UK", taskSummaryHandler);
        
        List<TaskSummary> tasks = taskSummaryHandler.getResults();
        assertEquals(1, tasks.size());
        assertEquals(taskId, tasks.get(0).getId());
        
        // Go straight from Ready to Inprogress
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.start( taskId, users.get( "darth" ).getId(), responseHandler );
        responseHandler.waitTillDone(5000);
        
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals(  Status.InProgress, task1.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task1.getTaskData().getActualOwner() );  
        
        // Check is Complete
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.complete( taskId, users.get( "darth" ).getId(), null, responseHandler ); 
        responseHandler.waitTillDone(5000);
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task2 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Completed, task2.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task2.getTaskData().getActualOwner() );                  
    }
    
    public void testCompleteTaskByProcessInstanceIdTaskname() {
        Map<String, Object> vars = fillVariables(users, groups);
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { processInstanceId=99} ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId(); 
        
        str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { processInstanceId=500} ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'Another task')] })";
        addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task secondTask = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( secondTask, null, addTaskResponseHandler );
        
        BlockingTaskSummaryResponseHandler taskSummaryHandler = new BlockingTaskSummaryResponseHandler();
        client.getTasksByStatusByProcessIdByTaskName(99L, Collections.singletonList(Status.Ready), "This is my task name",  "en-UK", taskSummaryHandler);
        
        List<TaskSummary> tasks = taskSummaryHandler.getResults();
        assertEquals(1, tasks.size());
        assertEquals(taskId, tasks.get(0).getId());
        
        // Go straight from Ready to Inprogress
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.start( taskId, users.get( "darth" ).getId(), responseHandler );
        responseHandler.waitTillDone(5000);
        
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals(  Status.InProgress, task1.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task1.getTaskData().getActualOwner() );  
        
        // Check is Complete
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.complete( taskId, users.get( "darth" ).getId(), null, responseHandler ); 
        responseHandler.waitTillDone(5000);
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task2 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Completed, task2.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task2.getTaskData().getActualOwner() );                  
    }

	public static class BlockingAllOpenTasksForUseResponseHandler
        implements
        TaskSummaryResponseHandler {
        private volatile List<TaskSummary> results;
        private volatile RuntimeException error;

        public synchronized void execute(List<TaskSummary> results) {
            this.results = results;
            notifyAll();
        }

        public synchronized List<TaskSummary> getResults() {
            if ( results == null ) {
                try {
                    wait( 3000 );
                } catch ( InterruptedException e ) {
                    e.printStackTrace();
                }
            }

            if ( results == null ) {
                throw new RuntimeException( "Timeout : unable to retrieve results" );
            }

            return results;

        }

        public boolean isDone() {
            synchronized ( results ) {
                return results != null;                
            }
        }

        public void setError(RuntimeException error) {
            this.error = error;            
        }
        
        public RuntimeException getError() {
            return error;
        }

    }

}
