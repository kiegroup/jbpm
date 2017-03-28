package org.jbpm.test.tasks.identity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jbpm.runtime.manager.impl.RuntimeEnvironmentBuilder;
import org.jbpm.services.task.identity.JBossUserGroupCallbackImpl;
import org.jbpm.test.JBPMHelper;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.manager.context.EmptyContext;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;

/**
 * This JUnit test is testing JBossUserGroupCallbackImpl with "," included in a Group ID of User Task
 */
public class JBossUserGroupCallbackTest extends JbpmJUnitBaseTestCase {

    private static EntityManagerFactory emf;
    
    @Test
    public void testProcess() throws Exception {

        System.setProperty("org.jbpm.ht.user.separator", "#");
        try {

            setup();

            RuntimeManager manager = getRuntimeManager("CustomSeparatorGroupIdUserTaskTest.bpmn");
            RuntimeEngine runtime = manager.getRuntimeEngine(EmptyContext.get());
            KieSession ksession = runtime.getKieSession();
            BitronixTransactionManager transactionManager = TransactionManagerServices.getTransactionManager();
            transactionManager.setTransactionTimeout(5000);
                                                            

            // start a new process instance
            Map<String, Object> params = new HashMap<String, Object>();
            ProcessInstance pi = ksession.startProcess("com.sample.bpmn.hello", params);
            System.out.println("A process instance started : pid = " + pi.getId());

            Assert.assertEquals(ProcessInstance.STATE_ACTIVE, pi.getState());
            
            TaskService taskService = runtime.getTaskService();

            
            {
                List<TaskSummary> list = taskService.getTasksAssignedAsPotentialOwner("john", "en-UK");
                System.out.println("Listing if the there are any tasks for john to complete: list= " + list);
                Assert.assertEquals(1, list.size());
                
                for (TaskSummary taskSummary : list) {
                    System.out.println("john starts a task : taskId = " + taskSummary.getId());
                    taskService.start(taskSummary.getId(), "john");
                    System.out.println("john started the task : taskId = " + taskSummary.getId() + ", which had assigned to Group/Owner: " + taskService.getTaskById(taskSummary.getId()).getPeopleAssignments().getPotentialOwners());
                    taskService.complete(taskSummary.getId(), "john", null);
                    System.out.println("john completed the task .");
                }
            }

            {
                List<TaskSummary> taskList = taskService.getTasksAssignedAsPotentialOwner("mary", "en-UK");
                Assert.assertEquals(1, taskList.size());
                for (TaskSummary taskSummary : taskList) {
                    System.out.println("mary starts a task : taskId = " + taskSummary.getId() + ", which had assigned to Group/Owner: " + taskService.getTaskById(taskSummary.getId()).getPeopleAssignments().getPotentialOwners());
                    taskService.start(taskSummary.getId(), "mary");
                    System.out.println("mary started the task : taskId = " + taskSummary.getId());
                    taskService.complete(taskSummary.getId(), "mary", null);
                    System.out.println("mary completed the task .");
                }
            }
            
    		assertProcessInstanceCompleted(pi.getId(), ksession);
    		System.out.println("Process Instance with id: '" + pi.getId() + "' , got completed successfully.");
    		
            manager.disposeRuntimeEngine(runtime);

        } catch (Throwable th) {
            th.printStackTrace();
        }

    }
    
    private static void setup() {
        // Uses H2 datasource
        JBPMHelper.startH2Server();
        JBPMHelper.setupDataSource();
        emf = Persistence.createEntityManagerFactory("org.jbpm.persistence.jpa", null);

    }

    private static RuntimeManager getRuntimeManager(String process) {


        Properties properties = new Properties();
        properties.setProperty("krisv", "krisvgg");
        properties.setProperty("mary", "maryg,g");
        properties.setProperty("john", "johngg");
        
        UserGroupCallback userGroupCallback = new JBossUserGroupCallbackImpl(properties);
        
        RuntimeEnvironment environment =
                RuntimeEnvironmentBuilder.getDefault()
                .persistence(true)
                .entityManagerFactory(emf)
                .userGroupCallback(userGroupCallback)
                .addAsset(ResourceFactory.newClassPathResource(process), ResourceType.BPMN2)
                .get();
        return RuntimeManagerFactory.Factory.get().newPerProcessInstanceRuntimeManager(environment);

    }

}