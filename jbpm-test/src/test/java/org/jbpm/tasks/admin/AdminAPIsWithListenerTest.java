package org.jbpm.tasks.admin;

import static org.jbpm.persistence.util.PersistenceUtil.cleanUp;
import static org.jbpm.persistence.util.PersistenceUtil.setupWithPoolingDataSource;
import static org.kie.runtime.EnvironmentName.ENTITY_MANAGER_FACTORY;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jbpm.process.workitem.wsht.LocalHTWorkItemHandler;
import org.jbpm.task.Group;
import org.jbpm.task.User;
import org.jbpm.task.UserInfo;
import org.jbpm.task.admin.TaskCleanUpProcessEventListener;
import org.jbpm.task.admin.TasksAdmin;
import org.jbpm.task.identity.DefaultUserGroupCallbackImpl;
import org.jbpm.task.identity.UserGroupCallbackManager;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.DefaultUserInfo;
import org.jbpm.task.service.SendIcal;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.TaskServiceSession;
import org.jbpm.task.service.local.LocalTaskService;
import org.jbpm.test.JbpmTestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kie.KieBase;
import org.kie.SystemEventListenerFactory;
import org.kie.logger.KnowledgeRuntimeLoggerFactory;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.process.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminAPIsWithListenerTest extends JbpmTestCase {

    private static Logger logger = LoggerFactory
            .getLogger(AdminAPIsWithListenerTest.class);
    private HashMap<String, Object> context;
    private EntityManagerFactory emf;
    private EntityManagerFactory emfDomain;
    private EntityManagerFactory emfTasks;
    protected Map<String, User> users;
    protected Map<String, Group> groups;
    protected TaskService taskService;
    protected LocalTaskService localTaskService;
    protected TaskServiceSession taskSession;
    protected UserInfo userInfo;
    protected Properties conf;
    protected TasksAdmin admin;

    @Before
    public void setUp() throws Exception {
        context = setupWithPoolingDataSource("org.jbpm.runtime", false);
        emf = (EntityManagerFactory) context.get(ENTITY_MANAGER_FACTORY);
        
        setEntityManagerFactory(emf);
        setPersistence(true);

        conf = new Properties();
        conf.setProperty("mail.smtp.host", "localhost");
        conf.setProperty("mail.smtp.port", "1125");
        conf.setProperty("from", "from@domain.com");
        conf.setProperty("replyTo", "replyTo@domain.com");
        conf.setProperty("defaultLanguage", "en-UK");

        SendIcal.initInstance(conf);

        // Use persistence.xml configuration

        emfTasks = Persistence.createEntityManagerFactory("org.jbpm.task");

        admin = new TaskService(emfTasks,
                SystemEventListenerFactory.getSystemEventListener())
                .createTaskAdmin();

        UserGroupCallbackManager.getInstance().setCallback(
                new DefaultUserGroupCallbackImpl(
                        "classpath:/usergroups.properties"));

        userInfo = new DefaultUserInfo(null);

        taskService = new TaskService(emfTasks,
                SystemEventListenerFactory.getSystemEventListener(), null);
        taskSession = taskService.createSession();

        taskService.setUserinfo(userInfo);

        localTaskService = new LocalTaskService(taskService);

    }

    @After
    public void tearDown() throws Exception {
        cleanUp(context);

        if (localTaskService != null) {
            System.out.println("Disposing Local Task Service session");
            localTaskService.disconnect();
        }
        if (taskSession != null) {
            System.out.println("Disposing session");
            taskSession.dispose();
        }

        admin.dispose();

        if (emfTasks != null && emfTasks.isOpen()) {
            emfTasks.close();
        }
    }

    @Test
    public void automaticCleanUpTest() throws Exception {

        KieBase kbase = createKnowledgeBase("patient-appointment.bpmn");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
        LocalHTWorkItemHandler htHandler = new LocalHTWorkItemHandler(
                localTaskService, ksession);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                htHandler);
        ksession.addEventListener(new TaskCleanUpProcessEventListener(
                taskService));

        logger.info("### Starting process ###");
        Map<String, Object> parameters = new HashMap<String, Object>();

        ProcessInstance process = ksession.startProcess(
                "org.jbpm.PatientAppointment", parameters);
        long processInstanceId = process.getId();

        // The process is in the first Human Task waiting for its completion
        Assert.assertEquals(ProcessInstance.STATE_ACTIVE, process.getState());

        // gets frontDesk's tasks
        List<TaskSummary> frontDeskTasks = this.localTaskService
                .getTasksAssignedAsPotentialOwner("frontDesk", "en-UK");
        Assert.assertEquals(1, frontDeskTasks.size());

        // doctor doesn't have any task
        List<TaskSummary> doctorTasks = this.localTaskService
                .getTasksAssignedAsPotentialOwner("doctor", "en-UK");
        Assert.assertTrue(doctorTasks.isEmpty());

        // manager doesn't have any task
        List<TaskSummary> managerTasks = this.localTaskService
                .getTasksAssignedAsPotentialOwner("manager", "en-UK");
        Assert.assertTrue(managerTasks.isEmpty());

        this.localTaskService.start(frontDeskTasks.get(0).getId(), "frontDesk");

        this.localTaskService.complete(frontDeskTasks.get(0).getId(),
                "frontDesk", null);

        // Now doctor has 1 task
        doctorTasks = this.localTaskService.getTasksAssignedAsPotentialOwner(
                "doctor", "en-UK");
        Assert.assertEquals(1, doctorTasks.size());

        // No tasks for manager yet
        managerTasks = this.localTaskService.getTasksAssignedAsPotentialOwner(
                "manager", "en-UK");
        Assert.assertTrue(managerTasks.isEmpty());

        this.localTaskService.start(doctorTasks.get(0).getId(), "doctor");

        this.localTaskService.complete(doctorTasks.get(0).getId(), "doctor",
                null);

        // tasks for manager
        managerTasks = this.localTaskService.getTasksAssignedAsPotentialOwner(
                "manager", "en-UK");
        Assert.assertEquals(1, managerTasks.size());
        this.localTaskService.start(managerTasks.get(0).getId(), "manager");

        this.localTaskService.complete(managerTasks.get(0).getId(), "manager",
                null);

        // since persisted process instance is completed it should be null
        process = ksession.getProcessInstance(process.getId());
        Assert.assertNull(process);

        final EntityManager em = emfTasks.createEntityManager();
        Assert.assertEquals(0, em.createQuery("select t from Task t")
                .getResultList().size());
        Assert.assertEquals(0, em.createQuery("select i from I18NText i")
                .getResultList().size());
        Assert.assertEquals(0,
                em.createNativeQuery("select * from PeopleAssignments_BAs")
                        .getResultList().size());
        Assert.assertEquals(
                0,
                em.createNativeQuery(
                        "select * from PeopleAssignments_ExclOwners")
                        .getResultList().size());
        Assert.assertEquals(
                0,
                em.createNativeQuery(
                        "select * from PeopleAssignments_PotOwners")
                        .getResultList().size());
        Assert.assertEquals(
                0,
                em.createNativeQuery(
                        "select * from PeopleAssignments_Recipients")
                        .getResultList().size());
        Assert.assertEquals(
                0,
                em.createNativeQuery(
                        "select * from PeopleAssignments_Stakeholders")
                        .getResultList().size());
        Assert.assertEquals(0, em.createQuery("select c from Content c")
                .getResultList().size());
        em.close();
    }

    @Test
    public void automaticCleanUpTestAbortProcess() throws Exception {

        KieBase kbase = createKnowledgeBase("patient-appointment.bpmn");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
        LocalHTWorkItemHandler htHandler = new LocalHTWorkItemHandler(
                localTaskService, ksession);

        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                htHandler);
        ksession.addEventListener(new TaskCleanUpProcessEventListener(
                taskService));

        logger.info("### Starting process ###");
        Map<String, Object> parameters = new HashMap<String, Object>();

        ProcessInstance process = ksession.startProcess(
                "org.jbpm.PatientAppointment", parameters);
        long processInstanceId = process.getId();

        // The process is in the first Human Task waiting for its completion
        Assert.assertEquals(ProcessInstance.STATE_ACTIVE, process.getState());

        // gets frontDesk's tasks
        List<TaskSummary> frontDeskTasks = this.localTaskService
                .getTasksAssignedAsPotentialOwner("frontDesk", "en-UK");
        Assert.assertEquals(1, frontDeskTasks.size());

        // doctor doesn't have any task
        List<TaskSummary> doctorTasks = this.localTaskService
                .getTasksAssignedAsPotentialOwner("doctor", "en-UK");
        Assert.assertTrue(doctorTasks.isEmpty());

        // manager doesn't have any task
        List<TaskSummary> managerTasks = this.localTaskService
                .getTasksAssignedAsPotentialOwner("manager", "en-UK");
        Assert.assertTrue(managerTasks.isEmpty());

        this.localTaskService.start(frontDeskTasks.get(0).getId(), "frontDesk");

        this.localTaskService.complete(frontDeskTasks.get(0).getId(),
                "frontDesk", null);

        // Now doctor has 1 task
        doctorTasks = this.localTaskService.getTasksAssignedAsPotentialOwner(
                "doctor", "en-UK");
        Assert.assertEquals(1, doctorTasks.size());

        // No tasks for manager yet
        managerTasks = this.localTaskService.getTasksAssignedAsPotentialOwner(
                "manager", "en-UK");
        Assert.assertTrue(managerTasks.isEmpty());

        this.localTaskService.start(doctorTasks.get(0).getId(), "doctor");

        this.localTaskService.complete(doctorTasks.get(0).getId(), "doctor",
                null);

        // abort process instance
        ksession.abortProcessInstance(processInstanceId);
        // since persisted process instance is completed it should be null
        process = ksession.getProcessInstance(process.getId());
        Assert.assertNull(process);

        final EntityManager em = emfTasks.createEntityManager();

        Assert.assertEquals(0, em.createQuery("select t from Task t")
                .getResultList().size());
        Assert.assertEquals(0, em.createQuery("select i from I18NText i")
                .getResultList().size());
        Assert.assertEquals(0,
                em.createNativeQuery("select * from PeopleAssignments_BAs")
                        .getResultList().size());
        Assert.assertEquals(
                0,
                em.createNativeQuery(
                        "select * from PeopleAssignments_ExclOwners")
                        .getResultList().size());
        Assert.assertEquals(
                0,
                em.createNativeQuery(
                        "select * from PeopleAssignments_PotOwners")
                        .getResultList().size());
        Assert.assertEquals(
                0,
                em.createNativeQuery(
                        "select * from PeopleAssignments_Recipients")
                        .getResultList().size());
        Assert.assertEquals(
                0,
                em.createNativeQuery(
                        "select * from PeopleAssignments_Stakeholders")
                        .getResultList().size());
        Assert.assertEquals(0, em.createQuery("select c from Content c")
                .getResultList().size());
        em.close();
    }

}
