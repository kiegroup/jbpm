/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jbpm.task.performance;

import bitronix.tm.resource.jdbc.PoolingDataSource;
import java.io.StringReader;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import org.jbpm.services.task.HumanTaskServicesBaseTest;
import org.jbpm.services.task.audit.impl.model.api.GroupAuditTask;
import org.jbpm.services.task.audit.impl.model.api.HistoryAuditTask;
import org.jbpm.services.task.audit.impl.model.api.UserAuditTask;
import org.jbpm.services.task.audit.query.Filter;
import org.jbpm.services.task.audit.query.QueryComparator;
import org.jbpm.services.task.audit.query.QueryResult;
import org.jbpm.services.task.audit.service.ActualOwnerFilter;
import org.jbpm.services.task.audit.service.CreationTimeComparator;
import org.jbpm.services.task.audit.service.DueDateFilter;
import org.jbpm.services.task.audit.service.StatusComparator;
import org.jbpm.services.task.audit.service.StatusFilter;
import org.jbpm.services.task.audit.service.TaskAuditService;
import org.jbpm.services.task.impl.factories.TaskFactory;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;

public abstract class HTPerformanceBaseTest extends HumanTaskServicesBaseTest {

    protected PoolingDataSource pds;
    protected EntityManagerFactory emf;
    
    @Inject
    protected TaskAuditService taskAuditService;

    @Test
    public void testBasicUserTaskAddingAndQuering() throws Exception {
        long beforeAddTime = System.currentTimeMillis();
        int amount = 1000;
        System.out.println("Adding " + amount + " tasks...." + beforeAddTime);
        UserTransaction ut = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
        ut.begin();

        for (int i = 0; i < amount; i++) {
            String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), "
                    + "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [new User('salaboy' )],"
                    + "businessAdministrators = [ new User('Administrator') ], }), names = [ new I18NText( 'en-UK', 'Task #" + i + "')] })";

            Task task = (Task) TaskFactory.evalTask(new StringReader(str));
            taskService.addTask(task, new HashMap<String, Object>());
        }
        ut.commit();
        System.out.println("Finish Adding tasks...." + (System.currentTimeMillis() - beforeAddTime));
        long beforeQueryTime = System.currentTimeMillis();
        System.out.println("Querying tasks...." + beforeQueryTime);
        List<TaskSummary> tasksAssignedAsPotentialOwner = taskService.getTasksAssignedAsPotentialOwner("salaboy", "en-UK");
        System.out.println("Finishing query tasks...." + (System.currentTimeMillis() - beforeQueryTime));
        Assert.assertEquals(1000, tasksAssignedAsPotentialOwner.size());

        long beforeAuditTime = System.currentTimeMillis();
        System.out.println("Querying Audit tasks...." + beforeAuditTime);
        List<UserAuditTask> allUserAuditTasks = taskAuditService.getAllUserAuditTasks("salaboy",0,0);
        System.out.println("After query audit tasks...." + (System.currentTimeMillis() - beforeAuditTime));

        Assert.assertEquals(1000, allUserAuditTasks.size());

    }

    @Test
    public void testBasicGroupTaskAddingAndQueringAndClaiming() throws Exception {
        long beforeAddTime = System.currentTimeMillis();
        int amount = 1000;
        System.out.println("Adding " + amount + " tasks...." + beforeAddTime);

        for (int i = 0; i < amount; i++) {
            String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), "
                    + "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [new Group('Knights Templer' )],"
                    + "businessAdministrators = [ new User('Administrator') ], }), names = [ new I18NText( 'en-UK', 'Task #" + i + "')] })";

            Task task = (Task) TaskFactory.evalTask(new StringReader(str));
            taskService.addTask(task, new HashMap<String, Object>());
        }

        System.out.println("Finish Adding tasks...." + (System.currentTimeMillis() - beforeAddTime));
        long beforeQueryTime = System.currentTimeMillis();
        System.out.println("Querying tasks...." + beforeQueryTime);
        List<TaskSummary> tasksAssignedByGroup = taskService.getTasksAssignedByGroup("Knights Templer", "en-UK");
        System.out.println("Finishing query tasks...." + (System.currentTimeMillis() - beforeQueryTime));
        Assert.assertEquals(1000, tasksAssignedByGroup.size());

        long beforeAuditTime = System.currentTimeMillis();
        System.out.println("Querying Group Audit tasks...." + beforeAuditTime);
        List<GroupAuditTask> allGroupAuditTasks = taskAuditService.getAllGroupAuditTasks("Knights Templer",0,0);
        System.out.println("After query Group audit tasks...." + (System.currentTimeMillis() - beforeAuditTime));

        Assert.assertEquals(1000, allGroupAuditTasks.size());

        for (TaskSummary ts : tasksAssignedByGroup) {
            taskService.claim(ts.getId(), "salaboy");
        }

        beforeAuditTime = System.currentTimeMillis();
        System.out.println("Querying Audit user tasks...." + beforeAuditTime);
        List<UserAuditTask> allUserAuditTasks = taskAuditService.getAllUserAuditTasks("salaboy",0,0);
        System.out.println("After query user audit tasks...." + (System.currentTimeMillis() - beforeAuditTime));

        Assert.assertEquals(1000, allUserAuditTasks.size());
        
        long beforeStartingTime = System.currentTimeMillis();
        System.out.println("Before starting  tasks...." +  beforeStartingTime);
        for (TaskSummary ts : tasksAssignedByGroup) {
            taskService.start(ts.getId(), "salaboy");
        }
        System.out.println("After starting  tasks...." + (System.currentTimeMillis() - beforeStartingTime));
       
        
        long beforeCompletingTime = System.currentTimeMillis();
        System.out.println("Before completing  tasks...." +  beforeCompletingTime);
        for (TaskSummary ts : tasksAssignedByGroup) {
            taskService.complete(ts.getId(), "salaboy", null);
        }
        System.out.println("After completing  tasks...." + (System.currentTimeMillis() - beforeCompletingTime));
       
        beforeQueryTime = System.currentTimeMillis();
        System.out.println("Before query history  tasks...." +  beforeQueryTime);
        List<HistoryAuditTask> allHistoryAuditTasks = taskAuditService.getAllHistoryAuditTasks(0,0);
        System.out.println("After query history  tasks...." + (System.currentTimeMillis() - beforeQueryTime));
        Assert.assertEquals(1000, allHistoryAuditTasks.size());
        
        beforeQueryTime = System.currentTimeMillis();
        System.out.println("Before query history by user tasks...." +  beforeQueryTime);
        allHistoryAuditTasks = taskAuditService.getAllHistoryAuditTasksByUser("salaboy",0,0);
        System.out.println("After query history by user  tasks...." + (System.currentTimeMillis() - beforeQueryTime));
        Assert.assertEquals(1000, allHistoryAuditTasks.size());
        
        
    }

    @Test
    public void testDifferentUserTasksQueries() throws Exception {
        long beforeAddTime = System.currentTimeMillis();
        int amount = 5000;
        String[] users = {"salaboy", "krisv", "mary"};
       
        for (String user : users) {
            System.out.println("Adding " + amount + " tasks for " + user + " - " + beforeAddTime);
            for (int i = 0; i < amount; i++) {
                String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), "
                        + "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [new User('" + user + "' ), new Group('Crusaders')],"
                        + "businessAdministrators = [ new User('Administrator') ], }), names = [ new I18NText( 'en-UK', 'Task #" + i + "')] })";
                Task task = (Task) TaskFactory.evalTask(new StringReader(str));
                taskService.addTask(task, new HashMap<String, Object>());
            }
            System.out.println("Finish Adding (" + amount + ") tasks for." + user + " - " + (System.currentTimeMillis() - beforeAddTime));
        }
        long beforeAuditTime = 0;

        long beforeQueryTime = System.currentTimeMillis();
        System.out.println("Querying tasks...." + beforeQueryTime);
        List<TaskSummary> tasksAssignedByGroup = taskService.getTasksAssignedByGroup("Crusaders", "en-UK");
        System.out.println("Finishing query tasks...." + (System.currentTimeMillis() - beforeQueryTime));

        Assert.assertEquals(amount * users.length, tasksAssignedByGroup.size());

        beforeAuditTime = System.currentTimeMillis();
        System.out.println("Querying group Audit tasks ...." + beforeAuditTime);

        List<GroupAuditTask> allAuditTasks = taskAuditService.getAllGroupAuditTasks("Crusaders",0,0);
        System.out.println("After query group audit tasks ..." + (System.currentTimeMillis() - beforeAuditTime));

        Assert.assertEquals(amount * users.length, allAuditTasks.size());

        beforeAuditTime = System.currentTimeMillis();

        List<GroupAuditTask> allCrusadersAuditTasks = taskAuditService.getAllGroupAuditTasks("Crusaders",0,0);
        Assert.assertEquals(amount * users.length, allCrusadersAuditTasks.size());
        System.out.println(" claiming tasks" + beforeAuditTime);
        for (int j = 0; j < users.length; j++) {
            int amoutPerUser = (amount * (j + 1));
            int start = j * amount;
            for (int i = start; i < amoutPerUser; i++) {
                GroupAuditTask groupTask = allCrusadersAuditTasks.get(i);
                taskService.claim(groupTask.getTaskId(), users[j]);
            }
        }
        System.out.println("After claim audit tasks" + (System.currentTimeMillis() - beforeAuditTime));

        for (String user : users) {
            beforeAuditTime = System.currentTimeMillis();
            System.out.println("Querying Audit user tasks for " + user + "...." + beforeAuditTime);
            List<UserAuditTask> allClaimedAuditTasks = taskAuditService.getAllUserAuditTasks(user,0,0);
            System.out.println("After query user audit tasks for " + user + "...." + " amount = " +allClaimedAuditTasks.size() +  " ..." + (System.currentTimeMillis() - beforeAuditTime));
            Assert.assertEquals(amount, allClaimedAuditTasks.size());

        }
    }
    
    @Test
    public void testBasicUserMultiGroupTaskAddingAndQuering() throws Exception {
        long beforeAddTime = System.currentTimeMillis();
        int amount = 1000;
        System.out.println("Adding " + amount + " tasks...." + beforeAddTime);
        
        for (int i = 0; i < amount; i++) {
            String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), "
                    + "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [ new Group('Crusaders'), new Group('Knights Templer') ],"
                    + "businessAdministrators = [ new User('Administrator') ], }), names = [ new I18NText( 'en-UK', 'Task #" + i + "')] })";

            Task task = (Task) TaskFactory.evalTask(new StringReader(str));
            taskService.addTask(task, new HashMap<String, Object>());
        }
        
        System.out.println("Finish Adding tasks...." + (System.currentTimeMillis() - beforeAddTime));
        
        long beforeQueryTime = System.currentTimeMillis();
        System.out.println("Querying tasks...." + beforeQueryTime);
        List<TaskSummary> tasksAssignedByGroup = taskService.getTasksAssignedByGroup("Crusaders", "en-UK");
        System.out.println("Finishing query tasks...." + (System.currentTimeMillis() - beforeQueryTime));
        Assert.assertEquals(1000, tasksAssignedByGroup.size());
        
        beforeQueryTime = System.currentTimeMillis();
        System.out.println("Querying tasks...." + beforeQueryTime);
        tasksAssignedByGroup = taskService.getTasksAssignedByGroup("Knights Templer", "en-UK");
        System.out.println("Finishing query tasks...." + (System.currentTimeMillis() - beforeQueryTime));
        Assert.assertEquals(1000, tasksAssignedByGroup.size());

    }

    @Test
    public void testQueryPerformance() throws Exception {
        long beforeAddTime = System.currentTimeMillis();
        int amount = 1000;
        int loops = 10;
        System.out.println("Adding " + amount + " tasks...." + beforeAddTime);
        UserTransaction ut = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
        ut.begin();

        Calendar cal = Calendar.getInstance();
        cal.set(2016,Calendar.DECEMBER,25);
        long[] exp = new long[2];
        exp[0] = cal.getTime().getTime();
        cal.set(2017,Calendar.DECEMBER,25);
        exp[1] = cal.getTime().getTime();

        for (int i = 0; i < amount; i++) {
            String str = "(with (new Task()) { priority = 55, " +
                "taskData = (with( new TaskData()) {expirationTime =  new Date(" + exp[i%2] + "), " +
                "createdOn = new Date(" + (System.currentTimeMillis() + (i*3600l *1000)) + ")} ),"
                + "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [new User('salaboy' )],"
                + "businessAdministrators = [ new User('Administrator') ], }), names = [ new I18NText( 'en-UK', 'Task #" + i + "')] })";
            Task task = (Task) TaskFactory.evalTask(new StringReader(str));
            taskService.addTask(task, new HashMap<String, Object>());
            if (i%1000 == 0) {
                ut.commit();
                ut.begin();
            }
        }

        ut.commit();
        long start = System.currentTimeMillis();

        //Search for owner salaboy
        Filter owner = new ActualOwnerFilter("salaboy");
        for (int i = 0; i < loops;i++) {
            QueryResult<UserAuditTask> qr = taskAuditService.getUserAuditTasks(
                0, Integer.MAX_VALUE, null,owner);
            Assert.assertEquals(amount, qr.getTotal());
            Assert.assertEquals(amount, qr.size());
        }

        System.out.println(
            " query: \"+actualOwner:salaboy\"" +
                " result-size="+ amount + " read-full-result=true, repeated 10 times, cumulative time-> "
                + (System.currentTimeMillis() - start) + "ms, average search time: " + (1f*(System.currentTimeMillis() - start)/loops)  + " ms/page");
       // Assert.assertTrue(5000 >= System.currentTimeMillis() - start);
       start = System.currentTimeMillis();
       Filter state = new StatusFilter(Status.Reserved.name());
        for (int i = 0; i < loops;i++) {
            QueryResult<UserAuditTask> qr = taskAuditService.getUserAuditTasks(
                0, Integer.MAX_VALUE, null,owner, state);
            Assert.assertEquals(amount, qr.getTotal());
            Assert.assertEquals(amount, qr.size());
        }

        System.out.println(
            " query: \"+actualOwner:salaboy +status:Status.Reserved\"" +
                " result-size="+ amount + " read-full-result=true, repeated 10 times, cumulative time-> "
                + (System.currentTimeMillis() - start) + "ms, average search time: " + (1f*(System.currentTimeMillis() - start)/loops)  + " ms/page");

        StatusComparator statusComparator = new StatusComparator(
            QueryComparator.Direction.DESCENDING);
        for (int i = 0; i < loops;i++) {
            QueryResult<UserAuditTask> qr = taskAuditService.getUserAuditTasks(
                0, Integer.MAX_VALUE, statusComparator,owner, state);
            Assert.assertEquals(amount, qr.getTotal());
            Assert.assertEquals(amount, qr.size());
        }


        start = System.currentTimeMillis();
        int pages = 0;
        for (int i =  0 ; i < loops ; i++) {
            int j = 0;
            while (j < amount) {
                long inner_start = System.currentTimeMillis();
                j += taskAuditService.getUserAuditTasks(j,20, null, owner).size();
                pages++;
                Assert.assertTrue("Single page delivered in more that 100 ms", 100 >= (System.currentTimeMillis() - inner_start));
            }
        }
        System.out.println(
            " query: \"+actualOwner:salaboy\" page-size=20, " +
                "result-size="+ amount + " read-full-result=true, repeated 10 times, cumulative time-> "
                + (System.currentTimeMillis() - start) + "ms, average search time: " + (1f*(System.currentTimeMillis() - start)/(loops*pages))  + " ms/page");
        Assert.assertTrue(5000 >= System.currentTimeMillis() - start);

        //sort in inverted creation order
        QueryComparator<UserAuditTask> qc = new CreationTimeComparator<UserAuditTask>();
        start = System.currentTimeMillis();
        pages =0;
        for (int i = 0; i <10; i++) {
            int found = 0;

            int j = 0;
            while (j < amount) {
                long inner_start = System.currentTimeMillis();
                QueryResult<UserAuditTask> qr =
                    taskAuditService.getUserAuditTasks(j, 20, qc, owner);
                j += qr.size();
                pages++;
                Assert.assertTrue("Single page delivered in more that 100 ms",
                    100 >= (System.currentTimeMillis() - inner_start));
                Assert.assertEquals(amount, qr.getTotal());
                for (UserAuditTask auditTask : qr) {
                    Assert.assertTrue(auditTask.getTaskId() + found++ == 1000);
                }
            }
        }
        System.out.println(
            " query: \"+actualOwner:salaboy\" sort:createdOn DESCENDING page-size=20, " +
                "result-size="+ amount + " read-full-result=true, repeated 10 times, cumulative time-> "
                + (System.currentTimeMillis() - start) + "ms, average search time: " + (1f*(System.currentTimeMillis() - start)/(loops*pages))  + " ms/page");


        Assert.assertTrue(5000 >= System.currentTimeMillis() - start);

        //sort in creation order, filter on owner and expiration december 2016
        qc = new CreationTimeComparator<UserAuditTask>(QueryComparator.Direction.ASCENDING);
        cal.set(2016,Calendar.DECEMBER, 1);
        Date r1 = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, 31);
        Date r2 = cal.getTime();
        Filter expires = new DueDateFilter<UserAuditTask>(r1,r2);
        start = System.currentTimeMillis();
        pages =0;
        for (int i = 0; i <10; i++) {
            int found = 0;

            int j = 0;
            while (j < amount/2) {
                long inner_start = System.currentTimeMillis();
                QueryResult<UserAuditTask> qr =
                    taskAuditService.getUserAuditTasks(j, 20, qc, expires, owner);
                j += qr.size();
                pages++;
                Assert.assertTrue("Single page delivered in more that 100 ms",
                    100 >= (System.currentTimeMillis() - inner_start));
                Assert.assertEquals(amount/2, qr.getTotal());
                for (UserAuditTask auditTask : qr) {
                    Assert.assertEquals(auditTask.getTaskId(), 1 + 2*found++);
                }
            }
        }
        System.out.println(
            " query: \"+actualOwner:salaboy +dueDate[2016-12-01 TO 2016-12-31]\" sort:createdOn ASCENDING page-size=20, " +
                "result-size="+ amount + " read-full-result=true, repeated 10 times, cumulative time-> "
                + (System.currentTimeMillis() - start) + "ms, average search time: " + (1f*(System.currentTimeMillis() - start)/(loops*pages))  + " ms/page");
        Assert.assertTrue(5000 >= System.currentTimeMillis() - start);


    }


}
