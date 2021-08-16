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

package org.jbpm.test.persistence.scripts;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.drools.core.impl.KnowledgeBaseFactory;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.persistence.jta.JtaTransactionManager;
import org.jbpm.test.persistence.scripts.util.SQLCommandUtil;
import org.jbpm.test.persistence.scripts.util.SQLScriptUtil;
import org.jbpm.test.persistence.scripts.util.ScriptFilter;
import org.jbpm.test.persistence.scripts.util.ScriptFilter.Option;
import org.jbpm.test.persistence.scripts.util.TestsUtil;
import org.jbpm.test.persistence.util.PersistenceUtil;
import org.jbpm.test.persistence.util.ProcessCreatorForHelp;
import org.kie.api.KieBase;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.internal.persistence.jpa.JPAKnowledgeService;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jbpm.test.persistence.scripts.DatabaseType.SQLSERVER;
import static org.jbpm.test.persistence.scripts.DatabaseType.SQLSERVER2008;
import static org.jbpm.test.persistence.scripts.DatabaseType.SYBASE;

/**
 * Central context class that hides persistence from tests, so there is no need to work with persistence in the tests
 * (transactions etc).
 */
public class TestPersistenceContextBase {

    protected static final String DATASOURCE_PROPERTIES = "/datasource.properties";
    private static final Logger logger = LoggerFactory.getLogger(TestPersistenceContextBase.class);
    protected HashMap<String, Object> context;
    protected EntityManagerFactory entityManagerFactory;
    protected JtaTransactionManager transactionManager;
    protected Environment environment;

    protected Properties dataSourceProperties;
    protected final DatabaseType databaseType;

    public TestPersistenceContextBase() {
        this.dataSourceProperties = PersistenceUtil.getDatasourceProperties();
        this.databaseType = TestsUtil.getDatabaseType(dataSourceProperties);
    }

    public static TestPersistenceContextBase createAndInitContext(PersistenceUnit persistenceUnit) {
        TestPersistenceContextBase testPersistenceContextBase = new TestPersistenceContextBase();
        testPersistenceContextBase.init(persistenceUnit);
        return testPersistenceContextBase;
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    /**
     * Initializes persistence context from specified persistence unit.
     *
     * @param persistenceUnit Persistence unit which is used to initialize this persistence context.
     */
    public void init(final PersistenceUnit persistenceUnit) {
        try {
            context = PersistenceUtil.setupWithPoolingDataSource(persistenceUnit.getName(), persistenceUnit
                    .getDataSourceName());
            entityManagerFactory = (EntityManagerFactory) context.get(EnvironmentName.ENTITY_MANAGER_FACTORY);
            environment = PersistenceUtil.createEnvironment(context);
            Object tm = this.environment.get(EnvironmentName.TRANSACTION_MANAGER);
            transactionManager = new JtaTransactionManager(environment.get(EnvironmentName.TRANSACTION),
                                                           environment.get(EnvironmentName.TRANSACTION_SYNCHRONIZATION_REGISTRY),
                                                           tm);
        } catch (RuntimeException ex) {
            // log the whole exception stacktrace as for some reason junit is not able to do so and only prints
            // the highest level exception, which makes debugging very hard
            logger.error("Failed to initialize persistence unit {}", persistenceUnit, ex);
            if (entityManagerFactory != null) {
                entityManagerFactory.close();
            }
            throw ex;
        }
    }

    /**
     * Cleans up this persistence context. Closes all instances that need to be closed.
     */
    public void clean() {
        PersistenceUtil.cleanUp(context);
    }

    /**
     * Executes SQL scripts from specified root SQL scripts folder. Selects appropriate scripts from root folder
     * by using dialect that is defined in datasource.properties file.
     *
     * @param scriptsRootFolder Root folder containing folders with SQL scripts for all supported database systems.
     * @param scriptFilter       indicates the filter to apply, including springboot or not scripts and create/drop scripts
     * @throws IOException
     */
    public void executeScripts(final File scriptsRootFolder, ScriptFilter scriptFilter) throws IOException, SQLException {
        testIsInitialized();
        executeScripts(scriptsRootFolder, scriptFilter, (DataSource) context.get(PersistenceUtil.DATASOURCE), null);
    }

    /**
     * Executes SQL scripts from specified root SQL scripts folder. Selects appropriate scripts from root folder
     * by using dialect that is defined in datasource.properties file.
     *
     * @param scriptsRootFolder Root folder containing folders with SQL scripts for all supported database systems.
     * @param scriptFilter       indicates the filter to apply, including springboot or not scripts and create/drop scripts
     * @param dataSource        Datasource where scripts will be executed.
     * @param defaultSchema     Default database schema to be set prior to running scripts
     * @throws IOException
     */
    public void executeScripts(final File scriptsRootFolder, ScriptFilter scriptFilter,
                               DataSource dataSource, String defaultSchema) throws IOException, SQLException {
        final File[] sqlScripts = TestsUtil.getDDLScriptFilesByDatabaseType(scriptsRootFolder, databaseType, scriptFilter);
        if (sqlScripts.length == 0 && scriptFilter.hasOption(Option.DISALLOW_EMPTY_RESULTS)) {
            throw new RuntimeException("No create sql files found for db type "
                                               + databaseType + " in folder " + scriptsRootFolder.getAbsolutePath());
        }
        final Connection connection = dataSource.getConnection();
        try {
            connection.setAutoCommit(false);
            if (defaultSchema != null && !defaultSchema.isEmpty()) {
                connection.setSchema(defaultSchema);
            }
            for (File script : sqlScripts) {
                logger.info("Executing script {}", script.getName());
                final List<String> scriptCommands = SQLScriptUtil.getCommandsFromScript(script, databaseType);
                for (String command : scriptCommands) {
                    logger.debug("query {} ", command);
                    final PreparedStatement statement = preparedStatement(connection, command);
                    executeStatement(scriptFilter.hasOption(Option.THROW_ON_SCRIPT_ERROR), statement);
                }
            }
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw new RuntimeException(ex.getMessage(), ex);
        } finally {
            connection.close();
        }
    }

    private PreparedStatement preparedStatement(final Connection conn, String command) throws SQLException {
        final PreparedStatement statement;
        if (databaseType == SQLSERVER || databaseType == SQLSERVER2008 || databaseType == SYBASE) {
            statement = conn.prepareStatement(SQLCommandUtil.preprocessCommandSqlServer(command, dataSourceProperties));
        } else {
            statement = conn.prepareStatement(command);
        }
        return statement;
    }

    private void executeStatement(boolean createFiles, final PreparedStatement statement) throws SQLException {
        try {
            statement.execute();
            statement.close();
        } catch (SQLException ex) {
            if (createFiles) {
                throw ex;
            } else //Consume exceptions for dropping files
            {
                logger.warn("Dropping statement failed: {} ", ex.getMessage());
            }
        }
    }

    /**
     * Starts and persists a basic simple process using current database entities.
     *
     * @param processId Process identifier. This identifier is also used to generate KieBase
     *                  (process with this identifier is part of generated KieBase).
     */
    public void startAndPersistSomeProcess(final String processId) {
        testIsInitialized();
        final StatefulKnowledgeSession session;
        final KieBase kbase = createKieBase(processId);

        session = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, environment);
        session.startProcess(processId);
    }

    /**
     * Loads persisted session from database.
     *
     * @param sessionId           Unique identifier of the session.
     * @param processIdForKieBase Process identifier for KieBase generation. A KieBase is generated for
     *                            loaded session and this KieBase contains process with this identifier.
     * @return Session that is stored in database.
     */
    public StatefulKnowledgeSession loadPersistedSession(final Long sessionId, final String processIdForKieBase) {
        testIsInitialized();
        return JPAKnowledgeService.loadStatefulKnowledgeSession(sessionId, createKieBase(processIdForKieBase),
                                                                null, environment);
    }

    /**
     * Reads stored processes count from database.
     *
     * @return Stored processes count.
     */
    public int getStoredProcessesCount() {
        return getStoredEntitiesCount("ProcessInstanceInfo");
    }

    /**
     * Reads stored sessions count from database.
     *
     * @return Stored sessions count.
     */
    public int getStoredSessionsCount() {
        return getStoredEntitiesCount("SessionInfo");
    }

    /**
     * Reads stored entities count from database.
     *
     * @param entityClassName Class name of entity.
     * @return Stored entities count.
     */
    private int getStoredEntitiesCount(final String entityClassName) {
        testIsInitialized();
        final boolean txOwner = transactionManager.begin();
        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            final List entitiesList = entityManager.createQuery(new StringBuffer("SELECT p FROM ").append(entityClassName).append(" p").toString())
                    .getResultList();
            if (entitiesList == null) {
                return 0;
            } else {
                return entitiesList.size();
            }
        } catch (Exception ex) {
            logger.error("Error while getting store entities count for entity {}", entityClassName, ex);
            transactionManager.rollback(txOwner);
            throw new RuntimeException(ex.getMessage(), ex);
        } finally {
            entityManager.close();
            transactionManager.commit(txOwner);
        }
    }

    /**
     * Checks if this persistence context is initialized.
     */
    protected void testIsInitialized() {
        if (context == null) {
            throw new IllegalStateException("TestContext is not initialized! Call TestContext.init() before using it.");
        }
    }

    /**
     * Creates very basic KieBase that contains processes with specified processIds.
     *
     * @param processIds ProcessIds of processes that are contained within resulting KieBase.
     * @return Basic KieBase.
     */
    private KieBase createKieBase(final String... processIds) {
        final KieBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        for (String processId : processIds) {
            ((KnowledgeBaseImpl) kbase).addProcess(ProcessCreatorForHelp.newSimpleEventProcess(processId, "test"));
        }
        return kbase;
    }
}
