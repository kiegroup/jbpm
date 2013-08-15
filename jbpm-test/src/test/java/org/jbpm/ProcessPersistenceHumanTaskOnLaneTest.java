package org.jbpm;

import java.util.ArrayList;
import java.util.List;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.task.Status;
import org.jbpm.task.TaskService;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.UserGroupCallback;
import org.jbpm.task.service.UserGroupCallbackManager;
import org.jbpm.test.JbpmJUnitTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ProcessPersistenceHumanTaskOnLaneTest extends JbpmJUnitTestCase {

    public ProcessPersistenceHumanTaskOnLaneTest() {
        super(true);
        setPersistence(true);
    }
    
    @Before
    public void setup() {
    	UserGroupCallbackManager.getInstance().setCallback(new UserGroupCallback() {
			
			public List<String> getGroupsForUser(String userId, List<String> groupIds,
					List<String> allExistingGroupIds) {
				List<String> groups = new ArrayList<String>();
		        groups.add("PM");
		        groups.add("HR");
				return groups;
			}
			
			public boolean existsUser(String userId) {
				return true;
			}
			
			public boolean existsGroup(String groupId) {
				return true;
			}
		});
    }
    
    @After
    public void cleanup() {
    	UserGroupCallbackManager.resetCallback();
    }

    @Test
    public void testProcess() throws Exception {
    	
        StatefulKnowledgeSession ksession = createKnowledgeSession("HumanTaskOnLane.bpmn2");
        TaskService taskService = getTaskService(ksession);

        ProcessInstance processInstance = ksession.startProcess("UserTask");

        assertProcessInstanceActive(processInstance.getId(), ksession);

        // simulating a system restart
        ksession = restoreSession(ksession, true);
        taskService = getTaskService(ksession);

        // let john execute Task 1
        String taskUser = "john";
        String locale = "en-UK";
        List<String> groups = new ArrayList<String>();
        groups.add("PM");
        List<TaskSummary> list = taskService.getTasksAssignedAsPotentialOwner(taskUser, groups, locale);
        assertEquals(2, list.size());
        
        TaskSummary task = list.get(0);
        taskService.claim(task.getId(), taskUser);
        taskService.start(task.getId(), taskUser);
        taskService.complete(task.getId(), taskUser, null);



        // simulating a system restart
        ksession = restoreSession(ksession, true);
        taskService = getTaskService(ksession);
        List<Status> reservedOnly = new ArrayList<Status>();
        reservedOnly.add(Status.Reserved);

        
        list = taskService.getTasksAssignedAsPotentialOwnerByStatus(taskUser, reservedOnly, locale);
        assertEquals(1, list.size());
        
        task = list.get(0);
        taskService.start(task.getId(), taskUser);
        taskService.complete(task.getId(), taskUser, null);


        assertProcessInstanceCompleted(processInstance.getId(), ksession);

    }


}