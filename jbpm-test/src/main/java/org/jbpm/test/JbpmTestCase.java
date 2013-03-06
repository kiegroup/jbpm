package org.jbpm.test;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;
import junit.framework.Assert;
import org.drools.ClockType;
import org.drools.SessionConfiguration;
import org.drools.audit.WorkingMemoryInMemoryLogger;
import org.drools.audit.event.LogEvent;
import org.drools.audit.event.RuleFlowNodeLogEvent;
import org.drools.core.util.DroolsStreamUtils;
import org.drools.impl.EnvironmentFactory;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;
import org.jbpm.process.audit.AuditLoggerFactory;
import org.jbpm.process.audit.JPAProcessInstanceDbLog;
import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.AuditLoggerFactory.Type;
import org.jbpm.process.instance.event.DefaultSignalManagerFactory;
import org.jbpm.process.instance.impl.DefaultProcessInstanceManagerFactory;
import org.jbpm.process.workitem.wsht.LocalHTWorkItemHandler;
import org.jbpm.task.TaskService;
import org.jbpm.task.identity.DefaultUserGroupCallbackImpl;
import org.jbpm.task.identity.UserGroupCallbackManager;
import org.jbpm.task.service.local.LocalTaskService;
import org.jbpm.task.utils.OnErrorAction;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.kie.KieBase;
import org.kie.KieServices;
import org.kie.KnowledgeBaseFactory;
import org.kie.SystemEventListenerFactory;
import org.kie.builder.KieBuilder;
import org.kie.builder.KieFileSystem;
import org.kie.builder.KieRepository;
import org.kie.builder.KnowledgeBuilderFactory;
import org.kie.builder.Message.Level;
import org.kie.definition.KiePackage;
import org.kie.definition.KnowledgePackage;
import org.kie.definition.process.Node;
import org.kie.io.Resource;
import org.kie.io.ResourceFactory;
import org.kie.io.ResourceType;
import org.kie.persistence.jpa.JPAKnowledgeService;
import org.kie.runtime.Environment;
import org.kie.runtime.EnvironmentName;
import org.kie.runtime.KieContainer;
import org.kie.runtime.KieSession;
import org.kie.runtime.KieSessionConfiguration;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.conf.ClockTypeOption;
import org.kie.runtime.process.NodeInstance;
import org.kie.runtime.process.NodeInstanceContainer;
import org.kie.runtime.process.ProcessInstance;
import org.kie.runtime.process.WorkItem;
import org.kie.runtime.process.WorkItemHandler;
import org.kie.runtime.process.WorkItemManager;
import org.kie.runtime.process.WorkflowProcessInstance;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.jbpm.test.JBPMHelper.createEnvironment;
import static org.jbpm.test.JBPMHelper.txStateName;

/**
 * Base test case for the jbpm-bpmn2 module.
 * 
 * Please keep this test class in the org.jbpm.bpmn2 package or otherwise give it a unique name.
 * 
 */
public abstract class JbpmTestCase extends Assert {

    protected final static String EOL = System.getProperty("line.separator");

    public static final boolean PERSISTENCE = Boolean.valueOf(System
            .getProperty("org.jbpm.test.persistence", "false"));

    private static boolean setupDataSource = false;
    private boolean sessionPersistence = false;
    private static H2Server server = new H2Server();
    private static org.jbpm.task.service.TaskService taskService;

    private TestWorkItemHandler workItemHandler = new TestWorkItemHandler();

    private WorkingMemoryInMemoryLogger logger;
    private Logger testLogger = null;

    @Rule
    public KnowledgeSessionCleanup ksessionCleanupRule = new KnowledgeSessionCleanup();
    protected static ThreadLocal<Set<StatefulKnowledgeSession>> knowledgeSessionSetLocal = KnowledgeSessionCleanup.knowledgeSessionSetLocal;

    private static EntityManagerFactory emf;
    private static PoolingDataSource ds;

    private RequirePersistence testReqPersistence;
    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            System.out.println(" >>> " + description.getMethodName() + " <<< ");

            try {
                testReqPersistence = description.getTestClass()
                        .getMethod(description.getMethodName())
                        .getAnnotation(RequirePersistence.class);
            } catch (Exception ex) {
                // ignore
            }

            if (testLogger == null) {
                testLogger = LoggerFactory.getLogger(getClass());
            }
        };

        protected void finished(Description description) {
            System.out.println("");
        };
    };

    @Before
    public void checkTest() {
        if (testReqPersistence != null
                && testReqPersistence.value() != sessionPersistence) {
            System.out.println("skipped - persistence required = "
                    + testReqPersistence.value() + " but currently set = "
                    + sessionPersistence + " " + testReqPersistence.comment());
            Assume.assumeTrue(false);
        }
    }

    public JbpmTestCase() {
        this(PERSISTENCE);
    }

    public JbpmTestCase(boolean sessionPersistance) {
        System.setProperty("jbpm.user.group.mapping",
                "classpath:/usergroups.properties");
        System.setProperty("jbpm.usergroup.callback",
                "org.jbpm.task.identity.DefaultUserGroupCallbackImpl");
        this.sessionPersistence = sessionPersistance;
    }

    public static PoolingDataSource setupPoolingDataSource() {
        PoolingDataSource pds = new PoolingDataSource();
        pds.setUniqueName("jdbc/testDS1");
        pds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
        pds.setMaxPoolSize(5);
        pds.setAllowLocalTransactions(true);
        pds.getDriverProperties().put("user", "sa");
        pds.getDriverProperties().put("password", "");
        pds.getDriverProperties().put("url",
                "jdbc:h2:tcp://localhost/~/jbpm-db");
        pds.getDriverProperties().put("driverClassName", "org.h2.Driver");
        pds.init();
        return pds;
    }

    public void setPersistence(boolean sessionPersistence) {
        this.sessionPersistence = sessionPersistence;
    }

    public boolean isPersistence() {
        return sessionPersistence;
    }

    public static void setUpDataSource() throws Exception {
        setupDataSource = true;
        server.start();
        ds = setupPoolingDataSource();
        emf = Persistence
                .createEntityManagerFactory("org.jbpm.persistence.jpa");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (setupDataSource) {
            taskService = null;
            if (emf != null) {
                try {
                    emf.close();
                } catch (Exception ex) {
                    // ignore
                }
                emf = null;
            }
            if (ds != null) {
                try {
                    ds.close();
                } catch (Exception ex) {
                    // ignore
                }
                ds = null;
            }
            server.stop();
            DeleteDbFiles.execute("~", "jbpm-db", true);

            // Clean up possible transactions
            Transaction tx = TransactionManagerServices.getTransactionManager()
                    .getCurrentTransaction();
            if (tx != null) {
                int testTxState = tx.getStatus();
                if (testTxState != Status.STATUS_NO_TRANSACTION
                        && testTxState != Status.STATUS_ROLLEDBACK
                        && testTxState != Status.STATUS_COMMITTED) {
                    try {
                        tx.rollback();
                    } catch (Throwable t) {
                        // do nothing..
                    }
                    Assert.fail("Transaction had status "
                            + txStateName[testTxState]
                            + " at the end of the test.");
                }
            }
        }
    }

    protected KieBase createKnowledgeBase(String... process) throws Exception {
        Resource[] resources = new Resource[process.length];
        for (int i = 0; i < process.length; ++i) {
            String p = process[i];
            resources[i] = (ResourceFactory.newClassPathResource(p));
        }
        return createKnowledgeBaseFromResources(resources);
    }

    protected KieBase createKnowledgeBaseFromResources(Resource... process)
            throws Exception {

        KieServices ks = KieServices.Factory.get();
        KieRepository kr = ks.getRepository();
        if (process.length > 0) {
            KieFileSystem kfs = ks.newKieFileSystem();

            for (Resource p : process) {
                kfs.write(p);
            }

            KieBuilder kb = ks.newKieBuilder(kfs);

            kb.buildAll(); // kieModule is automatically deployed to KieRepository
                           // if successfully built.

            if (kb.getResults().hasMessages(Level.ERROR)) {
                throw new RuntimeException("Build Errors:\n"
                        + kb.getResults().toString());
            }
        }

        KieContainer kContainer = ks.newKieContainer(kr.getDefaultReleaseId());
        return kContainer.getKieBase();
    }

    protected KieBase createKnowledgeBaseFromDisc(String process) throws Exception {
        KieServices ks = KieServices.Factory.get();
        KieRepository kr = ks.getRepository();
        KieFileSystem kfs = ks.newKieFileSystem();
            
        Resource res = ResourceFactory.newClassPathResource(process);
        kfs.write(res);

        KieBuilder kb = ks.newKieBuilder(kfs);

        kb.buildAll(); // kieModule is automatically deployed to KieRepository
                       // if successfully built.

        if (kb.getResults().hasMessages(Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n"
                    + kb.getResults().toString());
        }

        KieContainer kContainer = ks.newKieContainer(kr.getDefaultReleaseId());
        KieBase kbase =  kContainer.getKieBase();
        
        File packageFile = null;
        for (KiePackage pkg : kbase.getKiePackages() ) {
            packageFile = new File(System.getProperty("java.io.tmpdir") + File.separator + pkg.getName()+".pkg");
            FileOutputStream out = new FileOutputStream(packageFile);
            try {
                DroolsStreamUtils.streamOut(out, pkg);
            } finally {
                out.close();
            }
            
            // store first package only
            break;
        }
        
        kfs.delete(res.getSourcePath());
        kfs.write(ResourceFactory.newFileResource(packageFile));

        kb = ks.newKieBuilder(kfs);
        kb.buildAll(); // kieModule is automatically deployed to KieRepository
                       // if successfully built.

        if (kb.getResults().hasMessages(Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n"
                    + kb.getResults().toString());
        }
        
        kContainer = ks.newKieContainer(kr.getDefaultReleaseId());
        kbase =  kContainer.getKieBase();
        
        return kbase;
        
    }

    protected KieBase createKnowledgeBaseGuvnor(String... packages)
            throws Exception {
        return createKnowledgeBaseGuvnor(false,
                "http://localhost:8080/drools-guvnor", "admin", "admin",
                packages);
    }

    protected KieBase createKnowledgeBaseGuvnorAssets(String pkg,
            String... assets) throws Exception {
        return createKnowledgeBaseGuvnor(false,
                "http://localhost:8080/drools-guvnor", "admin", "admin", pkg,
                assets);
    }

    protected KieBase createKnowledgeBaseGuvnor(boolean dynamic, String url,
            String username, String password, String pkg, String... assets)
            throws Exception {
        String changeSet = "<change-set xmlns='http://drools.org/drools-5.0/change-set'"
                + EOL
                + "            xmlns:xs='http://www.w3.org/2001/XMLSchema-instance'"
                + EOL
                + "            xs:schemaLocation='http://drools.org/drools-5.0/change-set http://anonsvn.jboss.org/repos/labs/labs/jbossrules/trunk/drools-api/src/main/resources/change-set-1.0.0.xsd' >"
                + EOL + "    <add>" + EOL;
        for (String a : assets) {
            if (a.indexOf(".bpmn") >= 0) {
                a = a.substring(0, a.indexOf(".bpmn"));
            }
            changeSet += "        <resource source='"
                    + url
                    + "/rest/packages/"
                    + pkg
                    + "/assets/"
                    + a
                    + "/binary' type='BPMN2' basicAuthentication=\"enabled\" username=\""
                    + username + "\" password=\"" + password + "\" />" + EOL;
        }
        changeSet += "    </add>" + EOL + "</change-set>";

        KieServices ks = KieServices.Factory.get();
        KieRepository kr = ks.getRepository();
        KieFileSystem kfs = ks.newKieFileSystem();

        kfs.write(ResourceFactory.newByteArrayResource(changeSet.getBytes()));

        KieBuilder kb = ks.newKieBuilder(kfs);

        kb.buildAll(); // kieModule is automatically deployed to KieRepository
                       // if successfully built.
        if (kb.getResults().hasMessages(Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n"
                    + kb.getResults().toString());
        }

        KieContainer kContainer = ks.newKieContainer(kr.getDefaultReleaseId());
        return kContainer.getKieBase();

    }

    protected KieBase createKnowledgeBaseGuvnor(boolean dynamic, String url,
            String username, String password, String... packages)
            throws Exception {
        String changeSet = "<change-set xmlns='http://drools.org/drools-5.0/change-set'"
                + EOL
                + "            xmlns:xs='http://www.w3.org/2001/XMLSchema-instance'"
                + EOL
                + "            xs:schemaLocation='http://drools.org/drools-5.0/change-set http://anonsvn.jboss.org/repos/labs/labs/jbossrules/trunk/drools-api/src/main/resources/change-set-1.0.0.xsd' >"
                + EOL + "    <add>" + EOL;
        for (String p : packages) {
            changeSet += "        <resource source='"
                    + url
                    + "/rest/packages/"
                    + p
                    + "/binary' type='PKG' basicAuthentication=\"enabled\" username=\""
                    + username + "\" password=\"" + password + "\" />" + EOL;
        }
        changeSet += "    </add>" + EOL + "</change-set>";
        KieServices ks = KieServices.Factory.get();
        KieRepository kr = ks.getRepository();
        KieFileSystem kfs = ks.newKieFileSystem();

        kfs.write(ResourceFactory.newByteArrayResource(changeSet.getBytes()));

        KieBuilder kb = ks.newKieBuilder(kfs);

        kb.buildAll(); // kieModule is automatically deployed to KieRepository
                       // if successfully built.
        if (kb.getResults().hasMessages(Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n"
                    + kb.getResults().toString());
        }

        KieContainer kContainer = ks.newKieContainer(kr.getDefaultReleaseId());
        return kContainer.getKieBase();
    }

    protected StatefulKnowledgeSession createKnowledgeSession(KieBase kbase)
            throws Exception {
        return createKnowledgeSession(kbase, null, null);
    }

    protected StatefulKnowledgeSession createKnowledgeSession(KieBase kbase,
            Environment env) throws Exception {
        return createKnowledgeSession(kbase, null, env);
    }

    protected StatefulKnowledgeSession createKnowledgeSession(KieBase kbase,
            KieSessionConfiguration conf, Environment env) throws Exception {
        StatefulKnowledgeSession result;
        if (conf == null) {
            conf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        }
        // Do NOT use the Pseudo clock yet..
        // conf.setOption( ClockTypeOption.get( ClockType.PSEUDO_CLOCK.getId() )
        // );

        if (sessionPersistence) {
            if (env == null) {
                env = createEnvironment(emf);
            }
            result = JPAKnowledgeService.newStatefulKnowledgeSession(kbase,
                    conf, env);
            AuditLoggerFactory.newInstance(Type.JPA, result, null);
            JPAProcessInstanceDbLog.setEnvironment(result.getEnvironment());
        } else {
            if (env == null) {
                env = EnvironmentFactory.newEnvironment();
                env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
            }

            Properties defaultProps = new Properties();
            defaultProps.setProperty("drools.processSignalManagerFactory",
                    DefaultSignalManagerFactory.class.getName());
            defaultProps.setProperty("drools.processInstanceManagerFactory",
                    DefaultProcessInstanceManagerFactory.class.getName());
            conf = new SessionConfiguration(defaultProps);

            result = (StatefulKnowledgeSession) kbase.newKieSession(conf, env);
            logger = new WorkingMemoryInMemoryLogger(result);
        }
        if (knowledgeSessionSetLocal.get() != null) {
            knowledgeSessionSetLocal.get().add(result);
        }
        return result;
    }

    protected StatefulKnowledgeSession createKnowledgeSession(String... process)
            throws Exception {
        KieBase kbase = createKnowledgeBase(process);
        return createKnowledgeSession(kbase);
    }

    protected StatefulKnowledgeSession restoreSession(
            StatefulKnowledgeSession ksession, boolean noCache) {
        if (sessionPersistence) {
            Environment env = null;
            if (noCache) {
                env = createEnvironment(emf);
            } else {
                env = ksession.getEnvironment();
            }
            ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(
                    ksession.getId(), ksession.getKieBase(),
                    ksession.getSessionConfiguration(), env);
            AuditLoggerFactory.newInstance(Type.JPA, ksession, null);
        }
        return ksession;
    }

    protected StatefulKnowledgeSession reloadSession(
            StatefulKnowledgeSession ksession, boolean noCache)
            throws SystemException {
        return reloadSession(ksession, ksession.getId(), ksession.getKieBase(),
                ksession.getSessionConfiguration(), ksession.getEnvironment(),
                noCache);
    }

    protected StatefulKnowledgeSession reloadSession(
            StatefulKnowledgeSession ksession, int sessionId, KieBase kbase,
            KieSessionConfiguration config, Environment env, boolean noCache)
            throws SystemException {
        if (sessionPersistence) {
            Transaction tx = TransactionManagerServices.getTransactionManager()
                    .getCurrentTransaction();
            if (tx != null) {
                int txStatus = tx.getStatus();
                assertTrue("Current transaction state is "
                        + txStateName[txStatus],
                        tx.getStatus() == Status.STATUS_NO_TRANSACTION);
            }
            if (noCache || env == null) {
                emf.close();
                env = EnvironmentFactory.newEnvironment();
                emf = Persistence
                        .createEntityManagerFactory("org.jbpm.persistence.jpa");
                env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
                env.set(EnvironmentName.TRANSACTION_MANAGER,
                        TransactionManagerServices.getTransactionManager());
                JPAProcessInstanceDbLog.setEnvironment(env);
            }
            taskService = null;
            ksession.dispose();

            // reload knowledge session
            ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(
                    sessionId, kbase, config, env);
            KnowledgeSessionCleanup.knowledgeSessionSetLocal.get()
                    .add(ksession);
            AuditLoggerFactory.newInstance(Type.JPA, ksession, null);
            return ksession;
        } else {
            return ksession;
        }
    }

    public StatefulKnowledgeSession loadSession(int id, String... process)
            throws Exception {
        KieBase kbase = createKnowledgeBase(process);

        final KieSessionConfiguration config = KnowledgeBaseFactory
                .newKnowledgeSessionConfiguration();
        config.setOption(ClockTypeOption.get(ClockType.PSEUDO_CLOCK.getId()));

        StatefulKnowledgeSession ksession = JPAKnowledgeService
                .loadStatefulKnowledgeSession(id, kbase, config,
                        createEnvironment(emf));
        KnowledgeSessionCleanup.knowledgeSessionSetLocal.get().add(ksession);
        AuditLoggerFactory.newInstance(Type.JPA, ksession, null);

        return ksession;
    }

    public Object getVariableValue(String name, long processInstanceId,
            KieSession ksession) {
        return ((WorkflowProcessInstance) ksession
                .getProcessInstance(processInstanceId)).getVariable(name);
    }

    public void assertProcessInstanceCompleted(ProcessInstance processInstance) {
        assertTrue(processInstance.getState() == ProcessInstance.STATE_COMPLETED);
    }

    public void assertProcessInstanceAborted(ProcessInstance processInstance) {
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ABORTED);
    }

    public void assertProcessInstanceActive(ProcessInstance processInstance) {
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE ||
                processInstance.getState() == ProcessInstance.STATE_PENDING);
    }

    public void assertProcessInstanceFinished(ProcessInstance processInstance, KieSession ksession) {
        assertNull(ksession.getProcessInstance(processInstance.getId()));
    }

    public void assertNodeActive(long processInstanceId, KieSession ksession,
            String... name) {
        List<String> names = new ArrayList<String>();
        for (String n : name) {
            names.add(n);
        }
        ProcessInstance processInstance = ksession
                .getProcessInstance(processInstanceId);
        if (processInstance instanceof WorkflowProcessInstance) {
            assertNodeActive((WorkflowProcessInstance) processInstance, names);
        }
        if (!names.isEmpty()) {
            String s = names.get(0);
            for (int i = 1; i < names.size(); i++) {
                s += ", " + names.get(i);
            }
            fail("Node(s) not active: " + s);
        }
    }

    private void assertNodeActive(NodeInstanceContainer container,
            List<String> names) {
        for (NodeInstance nodeInstance : container.getNodeInstances()) {
            String nodeName = nodeInstance.getNodeName();
            if (names.contains(nodeName)) {
                names.remove(nodeName);
            }
            if (nodeInstance instanceof NodeInstanceContainer) {
                assertNodeActive((NodeInstanceContainer) nodeInstance, names);
            }
        }
    }

    public void assertNodeTriggered(long processInstanceId, String... nodeNames) {
        List<String> names = getNotTriggeredNodes(processInstanceId, nodeNames);
        if (!names.isEmpty()) {
            String s = names.get(0);
            for (int i = 1; i < names.size(); i++) {
                s += ", " + names.get(i);
            }
            fail("Node(s) not executed: " + s);
        }
    }

    public void assertNotNodeTriggered(long processInstanceId,
            String... nodeNames) {
        List<String> names = getNotTriggeredNodes(processInstanceId, nodeNames);
        assertTrue(Arrays.equals(names.toArray(), nodeNames));
    }

    private List<String> getNotTriggeredNodes(long processInstanceId,
            String... nodeNames) {
        List<String> names = new ArrayList<String>();
        for (String nodeName : nodeNames) {
            names.add(nodeName);
        }
        if (sessionPersistence) {
            List<NodeInstanceLog> logs = JPAProcessInstanceDbLog
                    .findNodeInstances(processInstanceId);
            if (logs != null) {
                for (NodeInstanceLog l : logs) {
                    String nodeName = l.getNodeName();
                    if ((l.getType() == NodeInstanceLog.TYPE_ENTER || l
                            .getType() == NodeInstanceLog.TYPE_EXIT)
                            && names.contains(nodeName)) {
                        names.remove(nodeName);
                    }
                }
            }
        } else {
            for (LogEvent event : logger.getLogEvents()) {
                if (event instanceof RuleFlowNodeLogEvent) {
                    String nodeName = ((RuleFlowNodeLogEvent) event)
                            .getNodeName();
                    if (names.contains(nodeName)) {
                        names.remove(nodeName);
                    }
                }
            }
        }
        return names;
    }

    protected void clearHistory() {
        if (sessionPersistence) {
            JPAProcessInstanceDbLog.clear();
        } else {
            logger.clear();
        }
    }

    public TestWorkItemHandler getTestWorkItemHandler() {
        return workItemHandler;
    }

    public static class TestWorkItemHandler implements WorkItemHandler {

        private List<WorkItem> workItems = new ArrayList<WorkItem>();

        public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
            workItems.add(workItem);
        }

        public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        }

        public WorkItem getWorkItem() {
            if (workItems.size() == 0) {
                return null;
            }
            if (workItems.size() == 1) {
                WorkItem result = workItems.get(0);
                this.workItems.clear();
                return result;
            } else {
                throw new IllegalArgumentException(
                        "More than one work item active");
            }
        }

        public List<WorkItem> getWorkItems() {
            List<WorkItem> result = new ArrayList<WorkItem>(workItems);
            workItems.clear();
            return result;
        }

    }

    public void assertProcessVarExists(ProcessInstance process,
            String... processVarNames) {
        WorkflowProcessInstanceImpl instance = (WorkflowProcessInstanceImpl) process;
        List<String> names = new ArrayList<String>();
        for (String nodeName : processVarNames) {
            names.add(nodeName);
        }

        for (String pvar : instance.getVariables().keySet()) {
            if (names.contains(pvar)) {
                names.remove(pvar);
            }
        }

        if (!names.isEmpty()) {
            String s = names.get(0);
            for (int i = 1; i < names.size(); i++) {
                s += ", " + names.get(i);
            }
            fail("Process Variable(s) do not exist: " + s);
        }

    }

    public void assertNodeExists(ProcessInstance process, String... nodeNames) {
        WorkflowProcessInstanceImpl instance = (WorkflowProcessInstanceImpl) process;
        List<String> names = new ArrayList<String>();
        for (String nodeName : nodeNames) {
            names.add(nodeName);
        }

        for (Node node : instance.getNodeContainer().getNodes()) {
            if (names.contains(node.getName())) {
                names.remove(node.getName());
            }
        }

        if (!names.isEmpty()) {
            String s = names.get(0);
            for (int i = 1; i < names.size(); i++) {
                s += ", " + names.get(i);
            }
            fail("Node(s) do not exist: " + s);
        }
    }

    public void assertNumOfIncommingConnections(ProcessInstance process,
            String nodeName, int num) {
        assertNodeExists(process, nodeName);
        WorkflowProcessInstanceImpl instance = (WorkflowProcessInstanceImpl) process;
        for (Node node : instance.getNodeContainer().getNodes()) {
            if (node.getName().equals(nodeName)) {
                if (node.getIncomingConnections().size() != num) {
                    fail("Expected incomming connections: " + num + " - found "
                            + node.getIncomingConnections().size());
                } else {
                    break;
                }
            }
        }
    }

    public void assertNumOfOutgoingConnections(ProcessInstance process,
            String nodeName, int num) {
        assertNodeExists(process, nodeName);
        WorkflowProcessInstanceImpl instance = (WorkflowProcessInstanceImpl) process;
        for (Node node : instance.getNodeContainer().getNodes()) {
            if (node.getName().equals(nodeName)) {
                if (node.getOutgoingConnections().size() != num) {
                    fail("Expected outgoing connections: " + num + " - found "
                            + node.getOutgoingConnections().size());
                } else {
                    break;
                }
            }
        }
    }

    public void assertVersionEquals(ProcessInstance process, String version) {
        WorkflowProcessInstanceImpl instance = (WorkflowProcessInstanceImpl) process;
        if (!instance.getWorkflowProcess().getVersion().equals(version)) {
            fail("Expected version: " + version + " - found "
                    + instance.getWorkflowProcess().getVersion());
        }
    }

    public void assertProcessNameEquals(ProcessInstance process, String name) {
        WorkflowProcessInstanceImpl instance = (WorkflowProcessInstanceImpl) process;
        if (!instance.getWorkflowProcess().getName().equals(name)) {
            fail("Expected name: " + name + " - found "
                    + instance.getWorkflowProcess().getName());
        }
    }

    public void assertPackageNameEquals(ProcessInstance process,
            String packageName) {
        WorkflowProcessInstanceImpl instance = (WorkflowProcessInstanceImpl) process;
        if (!instance.getWorkflowProcess().getPackageName().equals(packageName)) {
            fail("Expected package name: " + packageName + " - found "
                    + instance.getWorkflowProcess().getPackageName());
        }
    }

    public Object eval(Reader reader, Map vars) {
        try {
            return eval(toString(reader), vars);
        } catch (IOException e) {
            throw new RuntimeException("Exception Thrown", e);
        }
    }

    private String toString(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder(1024);
        int charValue;

        while ((charValue = reader.read()) != -1) {
            sb.append((char) charValue);
        }
        return sb.toString();
    }

    public Object eval(String str, Map vars) {

        ParserContext context = new ParserContext();
        context.addPackageImport("org.jbpm.task");
        context.addPackageImport("org.jbpm.task.service");
        context.addPackageImport("org.jbpm.task.query");
        context.addPackageImport("java.util");

        vars.put("now", new Date());
        return MVEL.executeExpression(MVEL.compileExpression(str, context),
                vars);
    }

    public void setEntityManagerFactory(EntityManagerFactory emf) {
        JbpmTestCase.emf = emf;
    }

    public void setPoolingDataSource(PoolingDataSource ds) {
        JbpmTestCase.ds = ds;
    }

    public TaskService getTaskService(StatefulKnowledgeSession ksession) {
        taskService = new org.jbpm.task.service.TaskService(emf,
                SystemEventListenerFactory.getSystemEventListener());

        UserGroupCallbackManager.getInstance().setCallback(
                new DefaultUserGroupCallbackImpl(
                        "classpath:/usergroups.properties"));
        LocalTaskService localTaskService = new LocalTaskService(taskService);
        LocalHTWorkItemHandler humanTaskHandler = new LocalHTWorkItemHandler(
                localTaskService, ksession, OnErrorAction.RETHROW);
        humanTaskHandler.connect();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                humanTaskHandler);
        return localTaskService;
    }

    public org.jbpm.task.service.TaskService getService() {
        return new org.jbpm.task.service.TaskService(emf,
                SystemEventListenerFactory.getSystemEventListener());
    }

    private static class H2Server {
        private Server server;

        public synchronized void start() {
            if (server == null || !server.isRunning(false)) {
                try {
                    DeleteDbFiles.execute("~", "jbpm-db", true);
                    server = Server.createTcpServer(new String[0]);
                    server.start();
                } catch (SQLException e) {
                    throw new RuntimeException(
                            "Cannot start h2 server database", e);
                }
            }
        }

        public synchronized void finalize() throws Throwable {
            stop();
            super.finalize();
        }

        public void stop() {
            if (server != null) {
                server.stop();
                server.shutdown();
                DeleteDbFiles.execute("~", "jbpm-db", true);
                server = null;
            }
        }
    }

}
