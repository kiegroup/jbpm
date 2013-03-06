package org.jbpm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.task.TaskService;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.test.JbpmTestCase;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.process.ProcessInstance;

/**
 * This is a sample file to test a process.
 */
public class ProcessHumanTaskTest extends JbpmTestCase {

    public ProcessHumanTaskTest() {
        super(true);
    }

    @BeforeClass
    public static void setup() throws Exception {
        setUpDataSource();
    }

    @Test
    public void testProcess() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("humantask.bpmn");
        TaskService taskService = getTaskService(ksession);

        ProcessInstance processInstance = ksession
                .startProcess("com.sample.bpmn.hello");

        assertProcessInstanceActive(processInstance);
        assertNodeTriggered(processInstance.getId(), "Start", "Task 1");

        // let john execute Task 1
        List<TaskSummary> list = taskService.getTasksAssignedAsPotentialOwner(
                "john", "en-UK");
        TaskSummary task = list.get(0);
        System.out.println("John is executing task " + task.getName());
        taskService.start(task.getId(), "john");
        taskService.complete(task.getId(), "john", null);

        assertNodeTriggered(processInstance.getId(), "Task 2");

        // let mary execute Task 2
        list = taskService.getTasksAssignedAsPotentialOwner("mary", "en-UK");
        task = list.get(0);
        System.out.println("Mary is executing task " + task.getName());
        taskService.start(task.getId(), "mary");
        taskService.complete(task.getId(), "mary", null);

        assertNodeTriggered(processInstance.getId(), "End");
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testProcessWithCreatedBy() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("humantaskwithcreatedby.bpmn");
        TaskService taskService = getTaskService(ksession);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", "krisv");
        ProcessInstance processInstance = ksession.startProcess(
                "com.sample.bpmn.hello.createdby", params);

        assertProcessInstanceActive(processInstance);
        assertNodeTriggered(processInstance.getId(), "Start", "Task 1");

        // let john execute Task 1
        List<TaskSummary> list = taskService.getTasksAssignedAsPotentialOwner(
                "john", "en-UK");
        TaskSummary task = list.get(0);
        assertEquals("mary", task.getCreatedBy().getId());
        System.out.println("John is executing task " + task.getName());
        taskService.start(task.getId(), "john");

        assertProcessInstanceActive(processInstance);
        long processInstanceId = processInstance.getId();
        System.out.println(processInstanceId);
        processInstance = ksession.getProcessInstance(processInstanceId);
        System.out.println(processInstance);
        
        taskService.complete(task.getId(), "john", null);

        assertNodeTriggered(processInstance.getId(), "Task 2");

        // let mary execute Task 2
        list = taskService.getTasksAssignedAsPotentialOwner("mary", "en-UK");
        task = list.get(0);
        assertEquals("krisv", task.getCreatedBy().getId());
        System.out.println("Mary is executing task " + task.getName());
        taskService.start(task.getId(), "mary");
        taskService.complete(task.getId(), "mary", null);

        assertNodeTriggered(processInstance.getId(), "End");
        assertProcessInstanceFinished(processInstance, ksession);
    }

}