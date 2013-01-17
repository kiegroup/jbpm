package org.jbpm.persistence;

import java.util.List;

import org.jbpm.process.audit.JPAProcessInstanceDbLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.task.TaskService;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.test.JbpmJUnitTestCase;
import org.junit.Test;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.process.ProcessInstance;

public class StartProcessWithBusinessKeyTest extends JbpmJUnitTestCase {
	
	public StartProcessWithBusinessKeyTest() {
	    super(true);
        this.setPersistence(true);
	}
	
	@Test
    public void testProcessWithBusinessKey() {
        StatefulKnowledgeSession ksession = createKnowledgeSession("humantask.bpmn");
        TaskService taskService = getTaskService(ksession);
        
        ProcessInstance processInstance = ksession.startProcess("com.sample.bpmn.hello", "mybusinesskey", null);

        assertProcessInstanceActive(processInstance.getId(), ksession);
        assertNodeTriggered(processInstance.getId(), "Start", "Task 1");
        
        List<ProcessInstanceLog> logs = JPAProcessInstanceDbLog.findProcessInstancesByBusinessKey("mybusinesskey");
        assertNotNull(logs);
        assertEquals(1, logs.size());
        
        assertEquals(processInstance.getId(), logs.get(0).getProcessInstanceId());
        
        // let john execute Task 1
        List<TaskSummary> list = taskService.getTasksAssignedAsPotentialOwner("john", "en-UK");
        TaskSummary task = list.get(0);
        System.out.println("John is executing task " + task.getName());
        taskService.start(task.getId(), "john");
        taskService.complete(task.getId(), "john", null);

        assertNodeTriggered(processInstance.getId(), "Task 2");
        
        ProcessInstance processInstanceCopy = ksession.getProcessInstance("mybusinesskey");
        assertNotNull(processInstanceCopy);
        assertEquals(processInstance.getId(), processInstanceCopy.getId());
        
        // let mary execute Task 2
        list = taskService.getTasksAssignedAsPotentialOwner("mary", "en-UK");
        task = list.get(0);
        System.out.println("Mary is executing task " + task.getName());
        taskService.start(task.getId(), "mary");
        taskService.complete(task.getId(), "mary", null);

        assertNodeTriggered(processInstance.getId(), "End");
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
        
        logs = JPAProcessInstanceDbLog.findProcessInstancesByBusinessKey("mybusinesskey");
        assertNotNull(logs);
        assertEquals(1, logs.size());
        
        assertEquals(processInstance.getId(), logs.get(0).getProcessInstanceId());
        assertEquals(org.jbpm.process.instance.ProcessInstance.STATE_COMPLETED, logs.get(0).getStatus());
    }

	@Test
    public void testProcessWithBusinessKeyFailOnDuplicatedBusinessKey() {
        StatefulKnowledgeSession ksession = createKnowledgeSession("humantask.bpmn");
        TaskService taskService = getTaskService(ksession);
        
        ProcessInstance processInstance = ksession.startProcess("com.sample.bpmn.hello", "mybusinesskey", null);

        assertProcessInstanceActive(processInstance.getId(), ksession);
        assertNodeTriggered(processInstance.getId(), "Start", "Task 1");
        
        List<ProcessInstanceLog> logs = JPAProcessInstanceDbLog.findProcessInstancesByBusinessKey("mybusinesskey");
        assertNotNull(logs);
        assertEquals(1, logs.size());
        
        assertEquals(processInstance.getId(), logs.get(0).getProcessInstanceId());
        
        try {
            ksession.startProcess("com.sample.bpmn.hello", "mybusinesskey", null);
            fail("Cannot have duplicated business key running at the same time");
        } catch (Exception e) {
            
        }
        
        // let john execute Task 1
        List<TaskSummary> list = taskService.getTasksAssignedAsPotentialOwner("john", "en-UK");
        TaskSummary task = list.get(0);
        System.out.println("John is executing task " + task.getName());
        taskService.start(task.getId(), "john");
        taskService.complete(task.getId(), "john", null);

        assertNodeTriggered(processInstance.getId(), "Task 2");
        
        ProcessInstance processInstanceCopy = ksession.getProcessInstance("mybusinesskey");
        assertNotNull(processInstanceCopy);
        assertEquals(processInstance.getId(), processInstanceCopy.getId());
        
        // let mary execute Task 2
        list = taskService.getTasksAssignedAsPotentialOwner("mary", "en-UK");
        task = list.get(0);
        System.out.println("Mary is executing task " + task.getName());
        taskService.start(task.getId(), "mary");
        taskService.complete(task.getId(), "mary", null);

        assertNodeTriggered(processInstance.getId(), "End");
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
        
        logs = JPAProcessInstanceDbLog.findProcessInstancesByBusinessKey("mybusinesskey");
        assertNotNull(logs);
        assertEquals(1, logs.size());
        
        assertEquals(processInstance.getId(), logs.get(0).getProcessInstanceId());
        assertEquals(org.jbpm.process.instance.ProcessInstance.STATE_COMPLETED, logs.get(0).getStatus());
    }
	
	@Test
    public void testProcessesWithSameBusinessKeyNotInParallel() {
        StatefulKnowledgeSession ksession = createKnowledgeSession("humantask.bpmn");
        TaskService taskService = getTaskService(ksession);
        
        ProcessInstance processInstance = ksession.startProcess("com.sample.bpmn.hello", "mybusinesskey", null);

        assertProcessInstanceActive(processInstance.getId(), ksession);
        assertNodeTriggered(processInstance.getId(), "Start", "Task 1");
        
        List<ProcessInstanceLog> logs = JPAProcessInstanceDbLog.findProcessInstancesByBusinessKey("mybusinesskey");
        assertNotNull(logs);
        assertEquals(1, logs.size());
        
        assertEquals(processInstance.getId(), logs.get(0).getProcessInstanceId());       
        
        // let john execute Task 1
        List<TaskSummary> list = taskService.getTasksAssignedAsPotentialOwner("john", "en-UK");
        TaskSummary task = list.get(0);
        System.out.println("John is executing task " + task.getName());
        taskService.start(task.getId(), "john");
        taskService.complete(task.getId(), "john", null);

        assertNodeTriggered(processInstance.getId(), "Task 2");
        
        ProcessInstance processInstanceCopy = ksession.getProcessInstance("mybusinesskey");
        assertNotNull(processInstanceCopy);
        assertEquals(processInstance.getId(), processInstanceCopy.getId());
        
        // let mary execute Task 2
        list = taskService.getTasksAssignedAsPotentialOwner("mary", "en-UK");
        task = list.get(0);
        System.out.println("Mary is executing task " + task.getName());
        taskService.start(task.getId(), "mary");
        taskService.complete(task.getId(), "mary", null);

        assertNodeTriggered(processInstance.getId(), "End");
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
        
        logs = JPAProcessInstanceDbLog.findProcessInstancesByBusinessKey("mybusinesskey");
        assertNotNull(logs);
        assertEquals(1, logs.size());
        
        assertEquals(processInstance.getId(), logs.get(0).getProcessInstanceId());
        assertEquals(org.jbpm.process.instance.ProcessInstance.STATE_COMPLETED, logs.get(0).getStatus());
        
        processInstance = ksession.startProcess("com.sample.bpmn.hello", "mybusinesskey", null);

        assertProcessInstanceActive(processInstance.getId(), ksession);
        assertNodeTriggered(processInstance.getId(), "Start", "Task 1");
        
        logs = JPAProcessInstanceDbLog.findProcessInstancesByBusinessKey("mybusinesskey");
        assertNotNull(logs);
        assertEquals(2, logs.size());
        
        assertEquals(processInstance.getId(), logs.get(1).getProcessInstanceId());       
        
        // let john execute Task 1
        list = taskService.getTasksAssignedAsPotentialOwner("john", "en-UK");
        task = list.get(0);
        System.out.println("John is executing task " + task.getName());
        taskService.start(task.getId(), "john");
        taskService.complete(task.getId(), "john", null);

        assertNodeTriggered(processInstance.getId(), "Task 2");
        
        processInstanceCopy = ksession.getProcessInstance("mybusinesskey");
        assertNotNull(processInstanceCopy);
        assertEquals(processInstance.getId(), processInstanceCopy.getId());
        
        // let mary execute Task 2
        list = taskService.getTasksAssignedAsPotentialOwner("mary", "en-UK");
        task = list.get(0);
        System.out.println("Mary is executing task " + task.getName());
        taskService.start(task.getId(), "mary");
        taskService.complete(task.getId(), "mary", null);

        assertNodeTriggered(processInstance.getId(), "End");
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
        
        logs = JPAProcessInstanceDbLog.findProcessInstancesByBusinessKey("mybusinesskey");
        assertNotNull(logs);
        assertEquals(2, logs.size());
        
        assertEquals(processInstance.getId(), logs.get(1).getProcessInstanceId());
        assertEquals(org.jbpm.process.instance.ProcessInstance.STATE_COMPLETED, logs.get(0).getStatus());
    }
}
