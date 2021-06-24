/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jbpm.persistence.api.integration.InstanceView;
import org.jbpm.persistence.api.integration.model.ProcessInstanceView;
import org.jbpm.persistence.api.integration.model.TaskInstanceView;
import org.jbpm.services.task.assignment.AssignmentServiceProvider;
import org.jbpm.services.task.assignment.AssignmentServiceRegistry;
import org.jbpm.services.task.assignment.impl.AssignmentImpl;
import org.jbpm.services.task.assignment.impl.AssignmentServiceImpl;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.persistence.processinstance.objects.TestEventEmitter;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskContext;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.internal.task.api.assignment.Assignment;
import org.kie.internal.task.api.assignment.AssignmentStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;


/**
 * This is a sample file to test a process.
 */
public class ProcessHumanTaskTest extends JbpmTestCase {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessHumanTaskTest.class);
	
	public ProcessHumanTaskTest() {
		super(true, false);
	}

	@Test
	public void testProcess() {
	    createRuntimeManager("org/jbpm/test/functional/task/humantask.bpmn");
	    RuntimeEngine runtimeEngine = getRuntimeEngine();
		KieSession ksession = runtimeEngine.getKieSession();
        TaskService taskService = runtimeEngine.getTaskService();
		
		ProcessInstance processInstance = ksession.startProcess("com.sample.bpmn.hello");

		assertProcessInstanceActive(processInstance.getId(), ksession);
		assertNodeTriggered(processInstance.getId(), "Start", "Task 1");
		
		// let john execute Task 1
		List<TaskSummary> list = taskService.getTasksAssignedAsPotentialOwner("john", "en-UK");
		TaskSummary task = list.get(0);
		logger.info("John is executing task {}", task.getName());
		taskService.start(task.getId(), "john");
		taskService.complete(task.getId(), "john", null);

		assertNodeTriggered(processInstance.getId(), "Task 2");
		
		// let mary execute Task 2
		list = taskService.getTasksAssignedAsPotentialOwner("mary", "en-UK");
		task = list.get(0);
		logger.info("Mary is executing task {}", task.getName());
		taskService.start(task.getId(), "mary");
		taskService.complete(task.getId(), "mary", null);

		assertNodeTriggered(processInstance.getId(), "End");
		assertProcessInstanceNotActive(processInstance.getId(), ksession);
	}
	
    @Test
    public void testProcessWithHumanTaskAndCustomAssigmentStrategy() {
        createRuntimeManager("org/jbpm/test/functional/task/HumanTaskWithCustomAssignmentStrategy.bpmn2");
        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession ksession = runtimeEngine.getKieSession();
        TaskService taskService = runtimeEngine.getTaskService();
        AssignmentServiceRegistry.get().addStrategy(new AssignmentStrategy() {

            @Override
            public String getIdentifier() {
                return "CustomStrategy";
            }

            @Override
            public Assignment apply(Task task, TaskContext context, String excludedUser) {
                return new AssignmentImpl("doctor");
            }
            
        });
        AssignmentServiceProvider.get().setEnabled(true);
        try {
            ProcessInstance processInstance = ksession.startProcess("humanTaskWithCustomStrategy", Collections.singletonMap("processHTInput", "CustomStrategy"));
    
            List<Long> listIds = taskService.getTasksByProcessInstanceId(processInstance.getId());
            List<Task> list = listIds.stream().map(taskService::getTaskById).collect(Collectors.toList());
            Task task = list.get(0);
            // john is potential owner but our custom strategy is overriding it.
            logger.info("doctor is executing task {}", task.getName());
            taskService.start(task.getId(), "doctor");
            taskService.complete(task.getId(), "doctor", null);
            assertProcessInstanceNotActive(processInstance.getId(), ksession);

        } finally {
            AssignmentServiceRegistry.get().reset();
            ((AssignmentServiceImpl) AssignmentServiceProvider.get()).reset();
        }
    }
	
	
    @Test
    public void testProcessWithCreatedBy() {
        
        createRuntimeManager("org/jbpm/test/functional/task/humantaskwithcreatedby.bpmn");
        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession ksession = runtimeEngine.getKieSession();
        TaskService taskService = runtimeEngine.getTaskService();
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("person", "krisv");
        ProcessInstance processInstance = ksession.startProcess("com.sample.bpmn.hello.createdby", params);

        assertProcessInstanceActive(processInstance.getId(), ksession);
        assertNodeTriggered(processInstance.getId(), "Start", "Task 1");
        
        // let john execute Task 1
        List<TaskSummary> list = taskService.getTasksAssignedAsPotentialOwner("john", "en-UK");
        TaskSummary task = list.get(0);
        assertEquals("mary", task.getCreatedById());
        logger.info("John is executing task {}", task.getName());
        taskService.start(task.getId(), "john");
        taskService.complete(task.getId(), "john", null);

        assertNodeTriggered(processInstance.getId(), "Task 2");
        
        // let mary execute Task 2
        list = taskService.getTasksAssignedAsPotentialOwner("mary", "en-UK");
        task = list.get(0);
        assertEquals("krisv", task.getCreatedById());
        logger.info("Mary is executing task {}", task.getName());
        taskService.start(task.getId(), "mary");
        taskService.complete(task.getId(), "mary", null);

        assertNodeTriggered(processInstance.getId(), "End");
        assertProcessInstanceNotActive(processInstance.getId(), ksession);
    }
    
    @Test
    public void testProcessRequestStrategy() {
        createRuntimeManager(Strategy.REQUEST, "manager", "org/jbpm/test/functional/task/humantask.bpmn");
        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession ksession = runtimeEngine.getKieSession();
        TaskService taskService = runtimeEngine.getTaskService();
        
        ProcessInstance processInstance = ksession.startProcess("com.sample.bpmn.hello");

        assertProcessInstanceActive(processInstance.getId(), ksession);
        assertNodeTriggered(processInstance.getId(), "Start", "Task 1");
        
        // let john execute Task 1
        List<TaskSummary> list = taskService.getTasksAssignedAsPotentialOwner("john", "en-UK");
        TaskSummary task = list.get(0);
        logger.info("John is executing task {}", task.getName());
        taskService.start(task.getId(), "john");
        taskService.complete(task.getId(), "john", null);

        assertNodeTriggered(processInstance.getId(), "Task 2");
        
        // let mary execute Task 2
        list = taskService.getTasksAssignedAsPotentialOwner("mary", "en-UK");
        task = list.get(0);
        logger.info("Mary is executing task {}", task.getName());
        taskService.start(task.getId(), "mary");
        taskService.complete(task.getId(), "mary", null);

        assertNodeTriggered(processInstance.getId(), "End");
        assertProcessInstanceNotActive(processInstance.getId(), ksession);
    }

    @Test
    public void testProcessProcessInstanceStrategy() {
        RuntimeManager manager = createRuntimeManager(Strategy.PROCESS_INSTANCE, "manager", "org/jbpm/test/functional/task/humantask.bpmn");
        RuntimeEngine runtimeEngine = getRuntimeEngine(ProcessInstanceIdContext.get());
        KieSession ksession = runtimeEngine.getKieSession();
        TaskService taskService = runtimeEngine.getTaskService();
        
        long ksessionID = ksession.getIdentifier();
        ProcessInstance processInstance = ksession.startProcess("com.sample.bpmn.hello");

        assertProcessInstanceActive(processInstance.getId(), ksession);
        assertNodeTriggered(processInstance.getId(), "Start", "Task 1");
        
        manager.disposeRuntimeEngine(runtimeEngine);
        runtimeEngine = getRuntimeEngine(ProcessInstanceIdContext.get(processInstance.getId()));
        
        ksession = runtimeEngine.getKieSession();
        taskService = runtimeEngine.getTaskService();
        
        assertEquals(ksessionID, ksession.getIdentifier());
        
        // let john execute Task 1
        List<TaskSummary> list = taskService.getTasksAssignedAsPotentialOwner("john", "en-UK");
        TaskSummary task = list.get(0);
        logger.info("John is executing task {}", task.getName());
        taskService.start(task.getId(), "john");
        taskService.complete(task.getId(), "john", null);

        assertNodeTriggered(processInstance.getId(), "Task 2");
        
        // let mary execute Task 2
        list = taskService.getTasksAssignedAsPotentialOwner("mary", "en-UK");
        task = list.get(0);
        logger.info("Mary is executing task {}", task.getName());
        taskService.start(task.getId(), "mary");
        taskService.complete(task.getId(), "mary", null);

        assertNodeTriggered(processInstance.getId(), "End");
        assertProcessInstanceNotActive(processInstance.getId(), ksession);
        manager.disposeRuntimeEngine(runtimeEngine);
    }

    @Test
    public void testProcessAndTaskIntegrationWithEventManager() {
        sessionPersistence = true; // so JPAProcessInstanceManager is used
        TestEventEmitter.clear();
        createRuntimeManager("org/jbpm/test/functional/task/humantask.bpmn");
        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession ksession = runtimeEngine.getKieSession();
        TaskService taskService = runtimeEngine.getTaskService();

        ProcessInstance processInstance = ksession.startProcess("com.sample.bpmn.hello");

        assertProcessInstanceActive(processInstance.getId(), ksession);

        // let john execute Task 1
        List<TaskSummary> list = taskService.getTasksAssignedAsPotentialOwner("john", "en-UK");
        TaskSummary task = list.get(0);
        logger.info("John is executing task {}", task.getName());
        taskService.start(task.getId(), "john");
        taskService.complete(task.getId(), "john", null);

        // let mary execute Task 2
        list = taskService.getTasksAssignedAsPotentialOwner("mary", "en-UK");
        task = list.get(0);
        logger.info("Mary is executing task {}", task.getName());
        taskService.start(task.getId(), "mary");
        taskService.complete(task.getId(), "mary", null);

        List<InstanceView<?>> events = TestEventEmitter.getEvents();

        List<InstanceView<?>> piEvents = events
                                        .stream()
                                        .filter(instanceView -> instanceView instanceof ProcessInstanceView)
                                        .collect(Collectors.toList());

        List<InstanceView<?>> tiEvents = events
                                        .stream()
                                        .filter(instanceView -> instanceView instanceof TaskInstanceView)
                                        .collect(Collectors.toList());

        assertEquals(9, events.size());
        assertEquals(1, piEvents.size());
        assertEquals(2, tiEvents.size());

        assertNodeTriggered(processInstance.getId(), "End");
        assertProcessInstanceNotActive(processInstance.getId(), ksession);
    }
}
