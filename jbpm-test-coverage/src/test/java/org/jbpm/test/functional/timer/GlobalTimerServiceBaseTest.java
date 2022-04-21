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

package org.jbpm.test.functional.timer;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.naming.InitialContext;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

import org.drools.core.impl.EnvironmentFactory;
import org.drools.core.time.TimerService;
import org.drools.core.time.impl.TimerJobInstance;
import org.drools.persistence.api.TransactionManager;
import org.jbpm.persistence.JpaProcessPersistenceContextManager;
import org.jbpm.persistence.jta.ContainerManagedTransactionManager;
import org.jbpm.process.core.timer.GlobalSchedulerService;
import org.jbpm.process.core.timer.TimerServiceRegistry;
import org.jbpm.process.core.timer.impl.GlobalTimerService;
import org.jbpm.process.core.timer.impl.GlobalTimerService.GlobalJobHandle;
import org.jbpm.runtime.manager.impl.AbstractRuntimeManager;
import org.jbpm.runtime.manager.impl.SimpleRuntimeEnvironment;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.jbpm.services.task.identity.JBossUserGroupCallbackImpl;
import org.jbpm.services.task.impl.TaskDeadlinesServiceImpl;
import org.jbpm.services.task.persistence.JPATaskPersistenceContextManager;
import org.jbpm.test.listener.process.NodeLeftCountDownProcessEventListener;
import org.jbpm.test.listener.task.CountDownTaskEventListener;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.jbpm.workflow.instance.node.HumanTaskNodeInstance;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.command.ExecutableCommand;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.BeforeMatchFiredEvent;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.Context;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.command.RegistryContext;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.manager.SessionNotFoundException;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class GlobalTimerServiceBaseTest extends TimerBaseTest{
    
    protected static final Logger logger = LoggerFactory.getLogger(GlobalTimerServiceBaseTest.class);
    
    protected GlobalSchedulerService globalScheduler;
    protected RuntimeManager manager;
    protected RuntimeEnvironment environment;
    
    protected EntityManagerFactory emf;
   
    protected abstract RuntimeManager getManager(RuntimeEnvironment environment, boolean waitOnStart);

    public void cleanup() {
        if (manager != null) {
            manager.close();
        }
        if (environment != null) {
            EntityManagerFactory emf = ((SimpleRuntimeEnvironment) environment).getEmf();
            if (emf != null) {
                emf.close();
            }
        }
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        TaskDeadlinesServiceImpl.dispose();
    }

    @Test(timeout=60000)
    public void testIntermediateTimerWithGlobalTestService() {
        NodeLeftCountDownProcessEventListener countDownListener = new NodeLeftCountDownProcessEventListener("timer", 3);
        // prepare listener to assert results
        final List<Long> timerExpirations = new ArrayList<Long>();
        ProcessEventListener listener = new DefaultProcessEventListener(){

            @Override
            public void afterNodeLeft(ProcessNodeLeftEvent event) {
                if (event.getNodeInstance().getNodeName().equals("timer")) {
                    timerExpirations.add(event.getProcessInstance().getId());
                }
            }
            
        };
        
        environment = RuntimeEnvironmentBuilder.Factory.get()
    			.newDefaultBuilder()
    			.entityManagerFactory(emf)
                .addAsset(ResourceFactory.newClassPathResource("org/jbpm/test/functional/timer/IntermediateCatchEventTimerCycle3.bpmn2"), ResourceType.BPMN2)
                .schedulerService(globalScheduler)
                .registerableItemsFactory(new TestRegisterableItemsFactory(listener, countDownListener))
                .get();


        manager = getManager(environment, true);

        RuntimeEngine runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get());
        KieSession ksession = runtime.getKieSession();
        
        
        ProcessInstance processInstance = ksession.startProcess("IntermediateCatchEvent");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        // dispose session to force session to be reloaded on timer expiration
        manager.disposeRuntimeEngine(runtime);
        // let's wait to ensure no more timers are expired and triggered
        countDownListener.waitTillCompleted();
        
        try {
            runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstance.getId()));
            ksession = runtime.getKieSession();
    
            
            processInstance = ksession.getProcessInstance(processInstance.getId());        
            assertNull(processInstance);
        } catch (SessionNotFoundException e) {
            // expected for PerProcessInstanceManagers since process instance is completed
        }
        
        
        assertEquals(3, timerExpirations.size());
        manager.disposeRuntimeEngine(runtime);
    }
    
    @Test(timeout=60000)
    public void testTimerStart() {
        NodeLeftCountDownProcessEventListener countDownListener = new NodeLeftCountDownProcessEventListener("StartProcess", 5);
        // prepare listener to assert results
        final List<Long> timerExpirations = new ArrayList<Long>();
        ProcessEventListener listener = new DefaultProcessEventListener(){

            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                timerExpirations.add(event.getProcessInstance().getId());
            }

        };
        
        environment = RuntimeEnvironmentBuilder.Factory.get()
    			.newDefaultBuilder()
    			.entityManagerFactory(emf)
                .addAsset(ResourceFactory.newClassPathResource("org/jbpm/test/functional/timer/TimerStart2.bpmn2"), ResourceType.BPMN2)
                .schedulerService(globalScheduler)
                .registerableItemsFactory(new TestRegisterableItemsFactory(listener, countDownListener))
                .get();
        
        manager = getManager(environment, false);
        RuntimeEngine runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get());
        KieSession ksession = runtime.getKieSession();
        
        assertEquals(0, timerExpirations.size());
       
        countDownListener.waitTillCompleted();
        
        manager.disposeRuntimeEngine(runtime);
        assertEquals(5, timerExpirations.size());

    }

    @Test@Ignore
    public void testTimerRule() throws Exception {
        int badNumTimers = 6;
        final CountDownLatch timerCompleted = new CountDownLatch(badNumTimers);
        // prepare listener to assert results
        final List<String> timerExpirations = new ArrayList<String>();
        AgendaEventListener listener = new DefaultAgendaEventListener(){

            @Override
            public void beforeMatchFired(BeforeMatchFiredEvent event) {
                timerExpirations.add(event.getMatch().getRule().getId());
                timerCompleted.countDown();
            }

        };
        
        environment = RuntimeEnvironmentBuilder.Factory.get()
    			.newDefaultBuilder()
    			.entityManagerFactory(emf)
                .addAsset(ResourceFactory.newClassPathResource("org/jbpm/test/functional/timer/timer-rules.drl"), ResourceType.DRL)
                .schedulerService(globalScheduler)
                .registerableItemsFactory(new TestRegisterableItemsFactory(listener))
                .get();
        
        manager = getManager(environment, true);
        RuntimeEngine runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get());
        
        assertEquals(0, timerExpirations.size());
        boolean didNotWait = timerCompleted.await(6, TimeUnit.SECONDS);
        assertTrue("Too many timers elapsed: " + (badNumTimers - timerCompleted.getCount()), ! didNotWait );
        
        manager.disposeRuntimeEngine(runtime);
        assertEquals(5, timerExpirations.size());
    }
    
    @Test(timeout=60000)
    public void testIntermediateTimerWithHTAfterWithGlobalTestService() {
        NodeLeftCountDownProcessEventListener countDownListener = new NodeLeftCountDownProcessEventListener("timer", 3);
        // prepare listener to assert results
        final List<Long> timerExpirations = new ArrayList<Long>();
        ProcessEventListener listener = new DefaultProcessEventListener(){

            @Override
            public void afterNodeLeft(ProcessNodeLeftEvent event) {
                if (event.getNodeInstance().getNodeName().equals("timer")) {
                    timerExpirations.add(event.getProcessInstance().getId());
                }
            }
            
        };
        Properties properties= new Properties();
        properties.setProperty("mary", "HR");
        properties.setProperty("john", "HR");
        UserGroupCallback userGroupCallback = new JBossUserGroupCallbackImpl(properties);

        environment = RuntimeEnvironmentBuilder.Factory.get()
    			.newDefaultBuilder()
    			.entityManagerFactory(emf)
                .addAsset(ResourceFactory.newClassPathResource("org/jbpm/test/functional/timer/IntermediateCatchEventTimerCycleWithHT.bpmn2"), ResourceType.BPMN2)
                .schedulerService(globalScheduler)
                .registerableItemsFactory(new TestRegisterableItemsFactory(listener, countDownListener))
                .userGroupCallback(userGroupCallback)
                .get();
       
        manager = getManager(environment, true);
        RuntimeEngine runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get());
        KieSession ksession = runtime.getKieSession();
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "R3/PT1S");
        ProcessInstance processInstance = ksession.startProcess("IntermediateCatchEvent", params);
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        logger.debug("Disposed after start");
        // dispose session to force session to be reloaded on timer expiration
        manager.disposeRuntimeEngine(runtime);
        
        countDownListener.waitTillCompleted();
        countDownListener.reset(1);
        
        runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstance.getId()));
        ksession = runtime.getKieSession();
        
        // get tasks
        List<Status> statuses = new ArrayList<Status>();
        statuses.add(Status.Reserved);
        List<TaskSummary> tasks = runtime.getTaskService().getTasksAssignedAsPotentialOwnerByStatus("john", statuses, "en-UK");
        assertNotNull(tasks);
        assertEquals(3, tasks.size());
        
        for (TaskSummary task : tasks) {
            runtime.getTaskService().start(task.getId(), "john");
            runtime.getTaskService().complete(task.getId(), "john", null);
        }
        
        
        processInstance = ksession.getProcessInstance(processInstance.getId());        
        assertNull(processInstance);
        // let's wait to ensure no more timers are expired and triggered
        countDownListener.waitTillCompleted(3000);
        
        manager.disposeRuntimeEngine(runtime);

        assertEquals(3, timerExpirations.size());
    }
    
    @Test(timeout=60000)
    public void testIntermediateTimerWithHTBeforeWithGlobalTestService() {
        NodeLeftCountDownProcessEventListener countDownListener = new NodeLeftCountDownProcessEventListener("timer", 3);
        // prepare listener to assert results
        final List<Long> timerExpirations = new ArrayList<Long>();
        ProcessEventListener listener = new DefaultProcessEventListener(){

            @Override
            public void afterNodeLeft(ProcessNodeLeftEvent event) {
                if (event.getNodeInstance().getNodeName().equals("timer")) {
                    timerExpirations.add(event.getProcessInstance().getId());
                    
                }
            }
            
        };
        Properties properties= new Properties();
        properties.setProperty("mary", "HR");
        properties.setProperty("john", "HR");
        UserGroupCallback userGroupCallback = new JBossUserGroupCallbackImpl(properties);
        environment = RuntimeEnvironmentBuilder.Factory.get()
    			.newDefaultBuilder()
    			.entityManagerFactory(emf)
                .addAsset(ResourceFactory.newClassPathResource("org/jbpm/test/functional/timer/IntermediateCatchEventTimerCycleWithHT2.bpmn2"), ResourceType.BPMN2)
                .schedulerService(globalScheduler)
                .registerableItemsFactory(new TestRegisterableItemsFactory(listener, countDownListener))
                .userGroupCallback(userGroupCallback)
                .get();
                
        manager = getManager(environment, true);
        RuntimeEngine runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get());
        KieSession ksession = runtime.getKieSession();
        
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "R3/PT1S");
        ProcessInstance processInstance = ksession.startProcess("IntermediateCatchEvent", params);
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        
        // get tasks
        List<Status> statuses = new ArrayList<Status>();
        statuses.add(Status.Reserved);
        List<TaskSummary> tasks = runtime.getTaskService().getTasksAssignedAsPotentialOwnerByStatus("john", statuses, "en-UK");
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        
        for (TaskSummary task : tasks) {
            runtime.getTaskService().start(task.getId(), "john");
            runtime.getTaskService().complete(task.getId(), "john", null);
        }
        // dispose session to force session to be reloaded on timer expiration
        manager.disposeRuntimeEngine(runtime);
        // now wait for 1 second for first timer to trigger

        countDownListener.waitTillCompleted();
        countDownListener.reset(1);
        try {
            runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstance.getId()));
            ksession = runtime.getKieSession();
    
            
            processInstance = ksession.getProcessInstance(processInstance.getId());        
            assertNull(processInstance);
        } catch (SessionNotFoundException e) {
            // expected for PerProcessInstanceManagers since process instance is completed
        }
        // let's wait to ensure no more timers are expired and triggered
        countDownListener.waitTillCompleted(3000);
   
        manager.disposeRuntimeEngine(runtime);

        assertEquals(3, timerExpirations.size());
    }
    
    @Test(timeout=60000)
    public void testIntermediateTimerWithGlobalTestServiceRollback() throws Exception {
        
        environment = RuntimeEnvironmentBuilder.Factory.get()
    			.newDefaultBuilder()
    			.entityManagerFactory(emf)
                .addAsset(ResourceFactory.newClassPathResource("org/jbpm/test/functional/timer/IntermediateCatchEventTimerCycle3.bpmn2"), ResourceType.BPMN2)
                .schedulerService(globalScheduler)
                .get();
        manager = getManager(environment, true);

        RuntimeEngine runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get());
        KieSession ksession = runtime.getKieSession();
        long ksessionId = ksession.getIdentifier();

        ProcessInstance processInstance;
        UserTransaction ut = InitialContext.doLookup("java:comp/UserTransaction");
        try {
            ut.begin();
            processInstance = ksession.startProcess("IntermediateCatchEvent");
        } finally {
            ut.rollback();
        }

        manager.disposeRuntimeEngine(runtime);
        try {
            // two types of checks as different managers will treat it differently
            // per process instance will fail on getting runtime
            runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstance.getId()));
            // where singleton and per request will return runtime but there should not be process instance
            processInstance = runtime.getKieSession().getProcessInstance(processInstance.getId());
            assertNull(processInstance);
        } catch (SessionNotFoundException e) {
            
        }

        TimerService timerService = TimerServiceRegistry.getInstance().get(manager.getIdentifier()+TimerServiceRegistry.TIMER_SERVICE_SUFFIX);
        Collection<TimerJobInstance> timerInstances = timerService.getTimerJobInstances(ksessionId);
        assertNotNull(timerInstances);
        assertEquals(0, timerInstances.size());
        
        if (runtime != null) {
            manager.disposeRuntimeEngine(runtime);
        }
    }
    
    @Test(timeout=60000)
    public void testIntermediateTimerWithHTBeforeWithGlobalTestServiceRollback() throws Exception {
        
        // prepare listener to assert results
        Properties properties= new Properties();
        properties.setProperty("mary", "HR");
        properties.setProperty("john", "HR");
        UserGroupCallback userGroupCallback = new JBossUserGroupCallbackImpl(properties);
        environment = RuntimeEnvironmentBuilder.Factory.get()
    			.newDefaultBuilder()
    			.entityManagerFactory(emf)
                .addAsset(ResourceFactory.newClassPathResource("org/jbpm/test/functional/timer/IntermediateCatchEventTimerCycleWithHT2.bpmn2"), ResourceType.BPMN2)
                .schedulerService(globalScheduler)
                .userGroupCallback(userGroupCallback)
                .get();
        
        manager = getManager(environment, true);
        RuntimeEngine runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get());
        KieSession ksession = runtime.getKieSession();
        
        
        long ksessionId = ksession.getIdentifier();
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "R3/PT1S");
        ProcessInstance processInstance = ksession.startProcess("IntermediateCatchEvent", params);
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        
        // get tasks
        List<Status> statuses = new ArrayList<Status>();
        statuses.add(Status.Reserved);
        List<TaskSummary> tasks = runtime.getTaskService().getTasksAssignedAsPotentialOwnerByStatus("john", statuses, "en-UK");
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        
        TaskSummary task = tasks.get(0);
        runtime.getTaskService().start(task.getId(), "john");
        UserTransaction ut = InitialContext.doLookup("java:comp/UserTransaction");
        try {
            ut.begin();
            runtime.getTaskService().complete(task.getId(), "john", null);
        } finally {
            ut.rollback();
        }
        
        processInstance = ksession.getProcessInstance(processInstance.getId());
        Collection<NodeInstance> activeNodes = ((WorkflowProcessInstance)processInstance).getNodeInstances();
        assertNotNull(activeNodes);
        assertEquals(1, activeNodes.size());
        assertTrue(activeNodes.iterator().next() instanceof HumanTaskNodeInstance);

        TimerService timerService = TimerServiceRegistry.getInstance().get(manager.getIdentifier()+TimerServiceRegistry.TIMER_SERVICE_SUFFIX);
        Collection<TimerJobInstance> timerInstances = timerService.getTimerJobInstances(ksessionId);
        assertNotNull(timerInstances);
        assertEquals(0, timerInstances.size());
        
        // clean up
        ksession.abortProcessInstance(processInstance.getId());
        
        manager.disposeRuntimeEngine(runtime);

    }
    
    @Test(timeout=60000)
    public void testIntermediateBoundaryTimerWithGlobalTestServiceRollback() throws Exception {
        Properties properties= new Properties();
        properties.setProperty("mary", "HR");
        properties.setProperty("john", "HR");
        UserGroupCallback userGroupCallback = new JBossUserGroupCallbackImpl(properties);
        environment = RuntimeEnvironmentBuilder.Factory.get()
    			.newDefaultBuilder()
    			.entityManagerFactory(emf)
                .addAsset(ResourceFactory.newClassPathResource("org/jbpm/test/functional/timer/HumanTaskWithBoundaryTimer.bpmn"), ResourceType.BPMN2)
                .schedulerService(globalScheduler)
                .userGroupCallback(userGroupCallback)
                .get();

        manager = getManager(environment, true);

        RuntimeEngine runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get());
        KieSession ksession = runtime.getKieSession();
        long ksessionId = ksession.getIdentifier();

        ProcessInstance processInstance;
        UserTransaction ut = InitialContext.doLookup("java:comp/UserTransaction");
        try {
            ut.begin();
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("test", "john");
            processInstance = ksession.startProcess("PROCESS_1", params);
        } finally {
            ut.rollback();
        }
        manager.disposeRuntimeEngine(runtime);
        try {
            // two types of checks as different managers will treat it differently
            // per process instance will fail on getting runtime
            runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstance.getId()));
            // where singleton and per request will return runtime but there should not be process instance
            processInstance = runtime.getKieSession().getProcessInstance(processInstance.getId());
            assertNull(processInstance);
        } catch (SessionNotFoundException e) {
            
        }

        TimerService timerService = TimerServiceRegistry.getInstance().get(manager.getIdentifier()+TimerServiceRegistry.TIMER_SERVICE_SUFFIX);
        Collection<TimerJobInstance> timerInstances = timerService.getTimerJobInstances(ksessionId);
        assertNotNull(timerInstances);
        assertEquals(0, timerInstances.size());
        
        if (runtime != null) {
            manager.disposeRuntimeEngine(runtime);
        }
    }

    @Test(timeout = 60000)
    public void testHumanTaskDeadlineWithGlobalTimerService() throws Exception {
        CountDownTaskEventListener countDownTaskEventListener = new CountDownTaskEventListener(1, true, false);
        environment = RuntimeEnvironmentBuilder.Factory.get()
    			.newDefaultBuilder()
    			.entityManagerFactory(emf)
                .addAsset(ResourceFactory.newClassPathResource("org/jbpm/test/functional/timer/HumanTaskWithDeadlines.bpmn"), ResourceType.BPMN2)
                .schedulerService(globalScheduler)
                .registerableItemsFactory(new TestRegisterableItemsFactory(countDownTaskEventListener))
                .get();

        manager = getManager(environment, true);

        RuntimeEngine runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get());
        KieSession ksession = runtime.getKieSession();

        ProcessInstance processInstance = ksession.startProcess("htdeadlinetest");
        assertEquals(ProcessInstance.STATE_ACTIVE, processInstance.getState());
        
        List<TaskSummary> krisTasks = runtime.getTaskService().getTasksAssignedAsPotentialOwner("krisv", "en-UK");
        assertEquals(1, krisTasks.size());
        List<TaskSummary> johnTasks = runtime.getTaskService().getTasksAssignedAsPotentialOwner("john", "en-UK");
        assertEquals(0, johnTasks.size());
        List<TaskSummary> maryTasks = runtime.getTaskService().getTasksAssignedAsPotentialOwner("mary", "en-UK");
        assertEquals(0, maryTasks.size());
        
        manager.disposeRuntimeEngine(runtime);        
        
        // now wait for first reassignment
        countDownTaskEventListener.waitTillCompleted();
        runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstance.getId()));
        
        krisTasks = runtime.getTaskService().getTasksAssignedAsPotentialOwner("krisv", "en-UK");
        assertEquals(0, krisTasks.size());
        johnTasks = runtime.getTaskService().getTasksAssignedAsPotentialOwner("john", "en-UK");
        assertEquals(1, johnTasks.size());
        maryTasks = runtime.getTaskService().getTasksAssignedAsPotentialOwner("mary", "en-UK");
        assertEquals(0, maryTasks.size());
        
        runtime.getTaskService().start(johnTasks.get(0).getId(), "john");
        manager.disposeRuntimeEngine(runtime);
        
        
        // now wait for 2 more seconds for second reassignment
        Thread.sleep(2000);
        runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstance.getId()));
        
        krisTasks = runtime.getTaskService().getTasksAssignedAsPotentialOwner("krisv", "en-UK");
        assertEquals(0, krisTasks.size());
        johnTasks = runtime.getTaskService().getTasksAssignedAsPotentialOwner("john", "en-UK");
        assertEquals(1, johnTasks.size());
        maryTasks = runtime.getTaskService().getTasksAssignedAsPotentialOwner("mary", "en-UK");
        assertEquals(0, maryTasks.size());
        manager.disposeRuntimeEngine(runtime);
        
        // now wait for 1 seconds to make sure that reassignment did not happen any more since task was already started
        countDownTaskEventListener.reset(1);
        countDownTaskEventListener.waitTillCompleted();
        
        runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstance.getId()));
        krisTasks = runtime.getTaskService().getTasksAssignedAsPotentialOwner("krisv", "en-UK");
        assertEquals(0, krisTasks.size());
        johnTasks = runtime.getTaskService().getTasksAssignedAsPotentialOwner("john", "en-UK");
        assertEquals(0, johnTasks.size());
        maryTasks = runtime.getTaskService().getTasksAssignedAsPotentialOwner("mary", "en-UK");
        assertEquals(1, maryTasks.size());
        runtime.getTaskService().start(maryTasks.get(0).getId(), "mary");
        runtime.getTaskService().complete(maryTasks.get(0).getId(), "mary", null);
        manager.disposeRuntimeEngine(runtime);
        
        // now wait for 2 seconds to make sure that reassignment did not happen any more since task was completed
        countDownTaskEventListener.reset(1);
        countDownTaskEventListener.waitTillCompleted(2000);
        
        try {
            runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstance.getId()));
            ksession = runtime.getKieSession();
            
            processInstance = ksession.getProcessInstance(processInstance.getId());        
            assertNull(processInstance);
        } catch (SessionNotFoundException e) {
            // this can be thrown for per process instance strategy as instance has already been completed
        }

        manager.disposeRuntimeEngine(runtime);
        
    }
    
    @Test(timeout = 60000)
    public void testHumanTaskDeadlineWithGlobalTimerServiceMultipleInstances() throws Exception {
        CountDownTaskEventListener countDownTaskEventListener = new CountDownTaskEventListener(1, true, false);
        environment = RuntimeEnvironmentBuilder.Factory.get()
    			.newDefaultBuilder()
    			.entityManagerFactory(emf)
                .addAsset(ResourceFactory.newClassPathResource("org/jbpm/test/functional/timer/HumanTaskWithDeadlines.bpmn"), ResourceType.BPMN2)
                .schedulerService(globalScheduler)
                .registerableItemsFactory(new TestRegisterableItemsFactory(countDownTaskEventListener))
                .get();

        manager = getManager(environment, true);

        RuntimeEngine runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get());
        KieSession ksession = runtime.getKieSession();

        ProcessInstance processInstance = ksession.startProcess("htdeadlinetest");
        final Long processInstanceId = processInstance.getId();
        manager.disposeRuntimeEngine(runtime);

        assertEquals(ProcessInstance.STATE_ACTIVE, processInstance.getState());
        
        runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
        List<TaskSummary> krisTasks = runtime.getTaskService().getTasksAssignedAsPotentialOwner("krisv", "en-UK")
                .stream().filter(task -> task.getProcessInstanceId().equals(processInstanceId)).collect(toList());
        assertEquals(1, krisTasks.size());
        List<TaskSummary> johnTasks = runtime.getTaskService().getTasksAssignedAsPotentialOwner("john", "en-UK")
                .stream().filter(task -> task.getProcessInstanceId().equals(processInstanceId)).collect(toList());
        assertEquals(0, johnTasks.size());
        List<TaskSummary> maryTasks = runtime.getTaskService().getTasksAssignedAsPotentialOwner("mary", "en-UK")
                .stream().filter(task -> task.getProcessInstanceId().equals(processInstanceId)).collect(toList());
        assertEquals(0, maryTasks.size());
        manager.disposeRuntimeEngine(runtime);
        
        // now wait for first reassignment
        countDownTaskEventListener.waitTillCompleted();
        runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
        krisTasks = runtime.getTaskService().getTasksAssignedAsPotentialOwner("krisv", "en-UK")
                .stream().filter(task -> task.getProcessInstanceId().equals(processInstanceId)).collect(toList());
        assertEquals(0, krisTasks.size());
        johnTasks = runtime.getTaskService().getTasksAssignedAsPotentialOwner("john", "en-UK")
                .stream().filter(task -> task.getProcessInstanceId().equals(processInstanceId)).collect(toList());
        assertEquals(1, johnTasks.size());
        maryTasks = runtime.getTaskService().getTasksAssignedAsPotentialOwner("mary", "en-UK")
                .stream().filter(task -> task.getProcessInstanceId().equals(processInstanceId)).collect(toList());
        assertEquals(0, maryTasks.size());

        runtime.getTaskService().start(johnTasks.get(0).getId(), "john");
        manager.disposeRuntimeEngine(runtime);

        // now wait for 2 seconds for the second reassignment
        Thread.sleep(2000);
        
        runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
        krisTasks = runtime.getTaskService().getTasksAssignedAsPotentialOwner("krisv", "en-UK")
                .stream().filter(task -> task.getProcessInstanceId().equals(processInstanceId)).collect(toList());
        assertEquals(0, krisTasks.size());
        johnTasks = runtime.getTaskService().getTasksAssignedAsPotentialOwner("john", "en-UK")
                .stream().filter(task -> task.getProcessInstanceId().equals(processInstanceId)).collect(toList());
        assertEquals(1, johnTasks.size());
        maryTasks = runtime.getTaskService().getTasksAssignedAsPotentialOwner("mary", "en-UK")
                .stream().filter(task -> task.getProcessInstanceId().equals(processInstanceId)).collect(toList());
        assertEquals(0, maryTasks.size());
        manager.disposeRuntimeEngine(runtime);

        // now wait to make sure that reassignment did not happen any more since task was already started
        countDownTaskEventListener.reset(1);
        countDownTaskEventListener.waitTillCompleted();
        
        runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
        krisTasks = runtime.getTaskService().getTasksAssignedAsPotentialOwner("krisv", "en-UK")
                .stream().filter(task -> task.getProcessInstanceId().equals(processInstanceId)).collect(toList());
        assertEquals(0, krisTasks.size());
        johnTasks = runtime.getTaskService().getTasksAssignedAsPotentialOwner("john", "en-UK")
                .stream().filter(task -> task.getProcessInstanceId().equals(processInstanceId)).collect(toList());
        assertEquals(0, johnTasks.size());
        maryTasks = runtime.getTaskService().getTasksAssignedAsPotentialOwner("mary", "en-UK")
                .stream().filter(task -> task.getProcessInstanceId().equals(processInstanceId)).collect(toList());
        assertEquals(1, maryTasks.size());
        runtime.getTaskService().start(maryTasks.get(0).getId(), "mary");
        runtime.getTaskService().complete(maryTasks.get(0).getId(), "mary", null);
        manager.disposeRuntimeEngine(runtime);
        
        // now wait for 2 seconds to make sure that reassignment did not happen any more since task was completed
        countDownTaskEventListener.reset(1);
        countDownTaskEventListener.waitTillCompleted(2000);
        try {
	        runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
	        ksession = runtime.getKieSession();
	        
	        processInstance = ksession.getProcessInstance(processInstanceId);
	        assertNull(processInstance);
        } catch (SessionNotFoundException e) {
        	// this can be thrown for per process instance strategy as instance has already been completed
        }

        manager.disposeRuntimeEngine(runtime);
        
    }
    
    @Test(timeout=60000)
    public void testIntermediateTimerWithGlobalTestServiceSimulateCMT() throws Exception {
        // prepare listener to assert results
        final List<Long> timerExpirations = new ArrayList<Long>();
        ProcessEventListener listener = new DefaultProcessEventListener(){

            @Override
            public void afterNodeLeft(ProcessNodeLeftEvent event) {
                if (event.getNodeInstance().getNodeName().equals("timer")) {
                    timerExpirations.add(event.getProcessInstance().getId());
                }
            }

        };
        
        Properties properties= new Properties();
        properties.setProperty("mary", "HR");
        properties.setProperty("john", "HR");
        UserGroupCallback userGroupCallback = new JBossUserGroupCallbackImpl(properties);
        EntityManagerFactory emf = EntityManagerFactoryManager.get().getOrCreate("org.jbpm.test.persistence");
        TransactionManager tm = new ContainerManagedTransactionManager();
        Environment env = EnvironmentFactory.newEnvironment();
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
        env.set(EnvironmentName.TRANSACTION_MANAGER, tm);
        environment = RuntimeEnvironmentBuilder.Factory.get()
    			.newDefaultBuilder()
    			.entityManagerFactory(emf)
                .addAsset(ResourceFactory.newClassPathResource("org/jbpm/test/functional/timer/IntermediateCatchEventTimerCycleWithHT2.bpmn2"), ResourceType.BPMN2)
                .addEnvironmentEntry(EnvironmentName.TRANSACTION_MANAGER, tm)
                .addEnvironmentEntry(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER, new JpaProcessPersistenceContextManager(env))
        		.addEnvironmentEntry(EnvironmentName.TASK_PERSISTENCE_CONTEXT_MANAGER, new JPATaskPersistenceContextManager(env))                
                .schedulerService(globalScheduler)
                .registerableItemsFactory(new TestRegisterableItemsFactory(listener))
                .userGroupCallback(userGroupCallback)
                .get();

        RuntimeEngine runtime;
        KieSession ksession;
        ProcessInstance processInstance;
        UserTransaction ut = InitialContext.doLookup("java:comp/UserTransaction");
        try {
            ut.begin();
            manager = getManager(environment, true);

            runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get());
            ksession = runtime.getKieSession();

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("x", "R3/PT1S");
            processInstance = ksession.startProcess("IntermediateCatchEvent", params);
            manager.disposeRuntimeEngine(runtime);
            ut.commit();
        } catch (Exception ex) {
            ut.rollback();
            throw ex;
        }

        assertEquals(ProcessInstance.STATE_ACTIVE, processInstance.getState());

        ut = InitialContext.doLookup("java:comp/UserTransaction");
        try {
            ut.begin();
            runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstance.getId()));
            // get tasks

            List<Status> statuses = new ArrayList<Status>();
            statuses.add(Status.Reserved);
            List<TaskSummary> tasks = runtime.getTaskService().getTasksAssignedAsPotentialOwnerByStatus("john", statuses, "en-UK");

            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            for (TaskSummary task : tasks) {
                runtime.getTaskService().start(task.getId(), "john");
                runtime.getTaskService().complete(task.getId(), "john", null);
            }
            manager.disposeRuntimeEngine(runtime);
            ut.commit();
        } catch (Exception ex) {
            ut.rollback();
            throw ex;
        }

        // now wait for more than 3 seconds for all timers to be triggered
        Thread.sleep(5000);

        ut = InitialContext.doLookup("java:comp/UserTransaction");
        try {
            ut.begin();
            try {
                runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstance.getId()));
                ksession = runtime.getKieSession();

                processInstance = ksession.getProcessInstance(processInstance.getId());
                assertNull(processInstance);

                manager.disposeRuntimeEngine(runtime);
            } catch (SessionNotFoundException e) {
                // expected for PerProcessInstanceManagers since process instance is completed
            }
            ut.commit();
        } catch (Exception ex) {
            ut.rollback();
            throw ex;
        }

        // let's wait to ensure no more timers are expired and triggered
        Thread.sleep(5000);
        assertEquals(3, timerExpirations.size());
    }
    
    @Test(timeout=60000)
    public void testTimerFailureAndRetrigger() {
        NodeLeftCountDownProcessEventListener countDownListener = new NodeLeftCountDownProcessEventListener("Timer_1m", 3);        
        final List<Long> timerExpirations = new ArrayList<Long>();
        ProcessEventListener listener = new DefaultProcessEventListener(){

            @Override
            public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
                if (event.getNodeInstance().getNodeName().equals("Timer_1m")) {
                    timerExpirations.add(event.getNodeInstance().getId());
                }
            }
        };
        
        environment = RuntimeEnvironmentBuilder.Factory.get()
                .newDefaultBuilder()
                .entityManagerFactory(emf)
                .addAsset(ResourceFactory.newClassPathResource("org/jbpm/test/functional/timer/helloretrigger.bpmn2"), ResourceType.BPMN2)
                .schedulerService(globalScheduler)
                .registerableItemsFactory(new TestRegisterableItemsFactory(listener, countDownListener))
                .get();
        
        manager = getManager(environment, false);
        RuntimeEngine runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get());
        KieSession ksession = runtime.getKieSession();
        
        Map<String, Object> params = new HashMap<>();
        ProcessInstance pi = ksession.startProcess("rescheduletimer.helloretrigger", params);
        assertEquals("Process instance should be active", ProcessInstance.STATE_ACTIVE, pi.getState());
        manager.disposeRuntimeEngine(runtime);
        
        final long processInstanceId = pi.getId();
        // let the timer (every 2 sec) fire three times as third will fail on gateway
        countDownListener.waitTillCompleted(8000);
        assertEquals("There should be only 3 nodes as there third is failing", 3, timerExpirations.size());
        
        runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
        ksession = runtime.getKieSession();
        
        ksession.execute(new ExecutableCommand<Void>() {

            @Override
            public Void execute(Context context) {
                KieSession ksession = ((RegistryContext) context).lookup( KieSession.class );
                ProcessInstance pi = (ProcessInstance) ksession.getProcessInstance(processInstanceId);
                
                ((WorkflowProcessInstance) pi).setVariable("fixed", true);
                return null;
            }
        });
        manager.disposeRuntimeEngine(runtime);
        
        countDownListener.reset(1);
        countDownListener.waitTillCompleted(5000);
        
        assertEquals("There should be 3 expirations as the failing one should finally proceed", 3, timerExpirations.size());
        try {
            runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
            ksession = runtime.getKieSession();
                
            pi = ksession.getProcessInstance(processInstanceId);        
            assertNull(pi);
        } catch (SessionNotFoundException e) {
            // expected for PerProcessInstanceManagers since process instance is completed
        }
        
        ((AbstractRuntimeManager)manager).close(true);
                      
    }

    @Test(timeout = 60000)
    public void testTimerStartMemoryLeak() {
        NodeLeftCountDownProcessEventListener countDownListener = new NodeLeftCountDownProcessEventListener("StartProcess", 5);
        // prepare listener to assert results
        final List<Long> timerExpirations = new ArrayList<Long>();
        ProcessEventListener listener = new DefaultProcessEventListener() {

            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                timerExpirations.add(event.getProcessInstance().getId());
            }

        };

        environment = RuntimeEnvironmentBuilder.Factory.get()
                .newDefaultBuilder()
                .entityManagerFactory(emf)
                .addAsset(ResourceFactory.newClassPathResource("org/jbpm/test/functional/timer/TimerStart2.bpmn2"), ResourceType.BPMN2)
                .schedulerService(globalScheduler)
                .registerableItemsFactory(new TestRegisterableItemsFactory(listener, countDownListener))
                .get();

        manager = getManager(environment, false);
        RuntimeEngine runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get());
        KieSession ksession = runtime.getKieSession();

        assertEquals(0, timerExpirations.size());

        countDownListener.waitTillCompleted();

        manager.disposeRuntimeEngine(runtime);
        assertEquals(5, timerExpirations.size());

        TimerServiceRegistry timerServiceRegistry = TimerServiceRegistry.getInstance();
        GlobalTimerService timerService = (GlobalTimerService) timerServiceRegistry.get(manager.getIdentifier() + TimerServiceRegistry.TIMER_SERVICE_SUFFIX);
        ConcurrentHashMap<Long, List<GlobalJobHandle>> timerJobsPerSession = timerService.getTimerJobsPerSession();

        assertEquals(0, timerJobsPerSession.size());

    }
    
    
    public static void cleanupSingletonSessionId() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        if (tempDir.exists()) {
            
            String[] jbpmSerFiles = tempDir.list(new FilenameFilter() {
                
                @Override
                public boolean accept(File dir, String name) {
                    
                    return name.endsWith("-jbpmSessionId.ser");
                }
            });
            for (String file : jbpmSerFiles) {
                
                new File(tempDir, file).delete();
            }
        }
    }
}
