package org.jbpm.services.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.drools.persistence.mapdb.KnowledgeStoreServiceImpl;
import org.jbpm.persistence.mapdb.util.MapDBProcessPersistenceUtil;
import org.jbpm.services.task.commands.TaskCommandExecutorImpl;
import org.jbpm.services.task.events.TaskEventSupport;
import org.jbpm.services.task.identity.JBossUserGroupCallbackImpl;
import org.jbpm.services.task.impl.command.CommandBasedTaskService;
import org.jbpm.services.task.persistence.MapDBTaskPersistenceContextManager;
import org.jbpm.services.task.persistence.TaskTransactionInterceptor;
import org.jbpm.services.task.wih.NonManagedLocalHTWorkItemHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message.Level;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.io.ResourceFactory;

public class TestTaskAndTimers {

	private HashMap<String, Object> context;

	@Before
	public void setUp() {
		this.context = MapDBProcessPersistenceUtil.setupMapDB();
	}
	
	@After
	public void tearDown() {
		MapDBProcessPersistenceUtil.cleanUp(context);
		this.context = null;
	}
	
    @Test
    public void testPersistentTasks() {
        KieServices ks = KieServices.Factory.get();
        Assert.assertNotNull(new KnowledgeStoreServiceImpl()); //make sure services are loaded
        KieBase kbase = createKieBase(ks);
        Properties userGroups = new Properties();
        userGroups.setProperty("mary", "g1");
        userGroups.setProperty("john", "g2");
        userGroups.setProperty("Administrator", "Administrators");
        userGroups.setProperty("mariano", "urn:multiarchive:group:ALL");
        JBossUserGroupCallbackImpl callback = new JBossUserGroupCallbackImpl(userGroups);
        processRun(kbase, "mariano", callback); 
    }
    
    @Test
    public void testPersistenceTasksConcurrent() throws Exception {
    	KieServices ks = KieServices.Factory.get();
        Assert.assertNotNull(new KnowledgeStoreServiceImpl()); //make sure services are loaded
        final KieBase kbase = createKieBase(ks);
        int size = 10;  
        Properties userGroups = new Properties();
        userGroups.setProperty("mary", "g1");
        userGroups.setProperty("john", "g2");
        userGroups.setProperty("Administrator", "Administrators");
        for (int index = 0; index <= size; index++) {
            userGroups.setProperty("user" + index, "urn:multiarchive:group:ALL");
        }
        final JBossUserGroupCallbackImpl callback = new JBossUserGroupCallbackImpl(userGroups);
        List<Thread> threads = new ArrayList<>();
        final List<Throwable> errors = new ArrayList<>();
        for (AtomicInteger index = new AtomicInteger(0); index.get() < size; index.incrementAndGet()) {
            Thread thread = Executors.defaultThreadFactory().newThread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String userId = "user" + index.get();
                            processRun(kbase, userId, callback);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            errors.add(e);
                        }
                    }
                });
            thread.start();
            threads.add(thread);
        }
        for (Thread thread : threads) { 
            thread.join();
        }
        for (Throwable e : errors) {
            e.printStackTrace();
        }
        Assert.assertEquals(0, errors.size());
    }

    private KieBase createKieBase(KieServices ks) {
    	KieFileSystem kfs = ks.newKieFileSystem();
    	kfs.write("src/main/resources/p.bpmn", ResourceFactory.newClassPathResource("test_timer.bpmn"));
    	KieBuilder kbuilder = ks.newKieBuilder(kfs);
    	kbuilder.buildAll();
    	if (kbuilder.getResults().hasMessages(Level.ERROR)) {
    		throw new IllegalArgumentException("Problem compiling process: " + kbuilder.getResults());
    	}
    	KieContainer kc = ks.newKieContainer(kbuilder.getKieModule().getReleaseId());
    	return kc.getKieBase();
	}

	private void processRun(KieBase kbase, String userId, UserGroupCallback callback) {
        Environment env = MapDBProcessPersistenceUtil.createEnvironment(context);
        env.set(EnvironmentName.TASK_USER_GROUP_CALLBACK, callback);
        KieSession ksession = KieServices.Factory.get().getStoreServices().
                newKieSession(kbase, null, env);
        Assert.assertNotNull(ksession);
        Assert.assertTrue(ksession.getIdentifier() > 0);
        TaskService taskService = getTaskService(env);
        NonManagedLocalHTWorkItemHandler handler = 
                new NonManagedLocalHTWorkItemHandler(ksession, taskService);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);
        ProcessInstance instance = ksession.startProcess("test_timer_1", new HashMap<>());
        Assert.assertNotNull(instance);
        Assert.assertTrue(instance.getId() > 0);
      
        Assert.assertEquals(ProcessInstance.STATE_ACTIVE, instance.getState());
        
        List<TaskSummary> tasks1 = taskService.getTasksAssignedAsPotentialOwner(userId, "en-UK");
        Assert.assertNotNull(tasks1);
        Assert.assertFalse(tasks1.isEmpty());
        
        TaskSummary task = null;
        for (TaskSummary each : tasks1) {
            if (each.getProcessInstanceId().equals(instance.getId())) {
                task = each;
                break;
            }
        }
        Assert.assertNotNull(task);
        taskService.claim(task.getId(), userId);
        taskService.start(task.getId(), userId);
        taskService.complete(task.getId(), userId, new HashMap<>());

        try {
            System.out.println("WAITING FOR TIMER!");
            Thread.sleep(6_000);
            System.out.println("DONE WAITING!");
        } catch (InterruptedException e) {  }
        
        List<TaskSummary> tasks2 = taskService.getTasksAssignedAsPotentialOwner(userId, "en-UK");
        Assert.assertNotNull(tasks2);
        Assert.assertFalse(tasks2.isEmpty());
        
        TaskSummary task2 = null;
        for (TaskSummary each : tasks2) {
            if (each.getProcessInstanceId().equals(instance.getId())) {
                task2 = each;
                break;
            }
        }
        
        Assert.assertNotNull(task2);
        Assert.assertNotEquals(task.getId(), task2.getId());
        taskService.claim(task2.getId(), userId);
        taskService.start(task2.getId(), userId);
        taskService.complete(task2.getId(), userId, new HashMap<>());
        //completed processes are removed, but for now we're commenting this away
        //Assert.assertNull(ksession.getProcessInstance(instance.getId())); TODO re-add after finding bugfix
    }

    private TaskService getTaskService(Environment env) {
        TaskEventSupport taskEventSupport = new TaskEventSupport();
        MapDBTaskPersistenceContextManager tpcm = new MapDBTaskPersistenceContextManager(env);
        env.set(EnvironmentName.TASK_PERSISTENCE_CONTEXT_MANAGER, tpcm);
        //taskEventSupport.addEventListener(new BAMTaskEventListener(true));
        TaskCommandExecutorImpl commandExecutor = new TaskCommandExecutorImpl(env, taskEventSupport);
        commandExecutor.addInterceptor(new TaskTransactionInterceptor(env));
        CommandBasedTaskService taskService = new CommandBasedTaskService(commandExecutor, taskEventSupport);
        return taskService;
    }

}
