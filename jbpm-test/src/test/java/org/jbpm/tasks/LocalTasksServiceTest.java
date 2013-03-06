package org.jbpm.tasks;

import static org.jbpm.persistence.util.PersistenceUtil.cleanUp;
import static org.jbpm.persistence.util.PersistenceUtil.setupWithPoolingDataSource;
import static org.kie.runtime.EnvironmentName.ENTITY_MANAGER_FACTORY;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jbpm.persistence.objects.MockUserInfo;
import org.jbpm.process.workitem.wsht.LocalHTWorkItemHandler;
import org.jbpm.process.workitem.wsht.SyncWSHumanTaskHandler;
import org.jbpm.task.Group;
import org.jbpm.task.User;
import org.jbpm.task.identity.DefaultUserGroupCallbackImpl;
import org.jbpm.task.identity.UserGroupCallbackManager;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.SendIcal;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.TaskServiceSession;
import org.jbpm.task.service.local.LocalTaskService;
import org.jbpm.test.JbpmTestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.KieBase;
import org.kie.SystemEventListenerFactory;
import org.kie.logger.KnowledgeRuntimeLoggerFactory;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.process.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalTasksServiceTest extends JbpmTestCase {

    private static Logger logger = LoggerFactory
            .getLogger(LocalTasksServiceTest.class);
    private HashMap<String, Object> context;
    private EntityManagerFactory emf;
    private EntityManagerFactory emfTasks;
    protected Map<String, User> users;
    protected Map<String, Group> groups;
    protected TaskService taskService;
    protected LocalTaskService localTaskService;
    protected TaskServiceSession taskSession;
    protected MockUserInfo userInfo;
    protected Properties conf;

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

        emfTasks = Persistence.createEntityManagerFactory("org.jbpm.task");

        userInfo = new MockUserInfo();

        taskService = new TaskService(emfTasks,
                SystemEventListenerFactory.getSystemEventListener(), null);
        taskSession = taskService.createSession();

        taskService.setUserinfo(userInfo);

        localTaskService = new LocalTaskService(taskService);

        UserGroupCallbackManager.getInstance().setCallback(
                new DefaultUserGroupCallbackImpl(
                        "classpath:/usergroups.properties"));
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
        if (emfTasks != null && emfTasks.isOpen()) {
            emfTasks.close();
        }
    }

    @Test
    public void groupTaskQueryTest() throws Exception {

        Properties userGroups = new Properties();
        userGroups.setProperty("salaboy", "");
        userGroups.setProperty("john", "PM");
        userGroups.setProperty("mary", "HR");

        UserGroupCallbackManager.getInstance().setCallback(
                new DefaultUserGroupCallbackImpl(userGroups));

        KieBase kbase = createKnowledgeBase("Evaluation2.bpmn");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
        LocalHTWorkItemHandler htHandler = new LocalHTWorkItemHandler(localTaskService, ksession);
        htHandler.setLocal(true);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                htHandler);
        logger.info("### Starting process ###");
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("employee", "salaboy");
        ProcessInstance process = ksession.startProcess(
                "com.sample.evaluation", parameters);
        long processInstanceId = process.getId();

        // The process is in the first Human Task waiting for its completion
        Assert.assertEquals(ProcessInstance.STATE_ACTIVE, process.getState());

        // gets salaboy's tasks
        List<TaskSummary> salaboysTasks = this.localTaskService
                .getTasksAssignedAsPotentialOwner("salaboy", "en-UK");
        Assert.assertEquals(1, salaboysTasks.size());

        this.localTaskService.start(salaboysTasks.get(0).getId(), "salaboy");

        this.localTaskService.complete(salaboysTasks.get(0).getId(), "salaboy",
                null);

        List<TaskSummary> pmsTasks = this.localTaskService
                .getTasksAssignedAsPotentialOwner("john", "en-UK");

        Assert.assertEquals(1, pmsTasks.size());

        List<TaskSummary> hrsTasks = this.localTaskService
                .getTasksAssignedAsPotentialOwner("mary", "en-UK");

        Assert.assertEquals(1, hrsTasks.size());

    }

}
