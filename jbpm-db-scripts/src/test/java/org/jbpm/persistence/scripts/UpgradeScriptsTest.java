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

package org.jbpm.persistence.scripts;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collection;

import org.jbpm.test.persistence.scripts.DatabaseType;
import org.jbpm.test.persistence.scripts.DistributionType;
import org.jbpm.test.persistence.scripts.PersistenceUnit;
import org.jbpm.test.persistence.scripts.ScriptsBase;
import org.jbpm.test.persistence.scripts.util.ScriptFilter;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Arrays.asList;
import static org.jbpm.persistence.scripts.TestPersistenceContext.createAndInitContext;
import static org.jbpm.test.persistence.scripts.DatabaseType.DB2;
import static org.jbpm.test.persistence.scripts.DatabaseType.SYBASE;
import static org.jbpm.test.persistence.scripts.util.ScriptFilter.filter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

/**
 * Contains tests that test database upgrade scripts.
 */
@RunWith(Parameterized.class)
public class UpgradeScriptsTest extends ScriptsBase {

    private static final Logger logger = LoggerFactory.getLogger(UpgradeScriptsTest.class);

    private static final Long TEST_PROCESS_INSTANCE_ID = 1L;
    private static final Integer TEST_SESSION_ID = 1;
    private static final String DB_UPGRADE_SCRIPTS_RESOURCE_PATH = "/db/upgrade-scripts";
    private static final String DB_60_SCRIPTS_RESOURCE_PATH = "/ddl60";

    @Parameters(name = "{0}")
    static public Collection<Object> distributionTypes() {
        return asList(DistributionType.COMMUNITY, DistributionType.PRODUCT);
    }

    private DistributionType distributionType;

    public UpgradeScriptsTest(DistributionType distributionType) {
        this.distributionType = distributionType;
    }

    @BeforeClass
    public static void hasToBeTested() {
        // skip these upgrade tests for db2 (fails 'Call SYSPROC.ADMIN_CMD')
        // and sybase (no 6.0 scripts exist)
        TestPersistenceContext ctx = new TestPersistenceContext();
        DatabaseType dbType = ctx.getDatabaseType();
        assumeTrue(dbType!=DB2 && dbType!=SYBASE);
    }
    
    private void createSchema60UsingDDLs() throws IOException, SQLException {
        //create 6.0 schema
        executeScriptRunner(DB_60_SCRIPTS_RESOURCE_PATH, ScriptFilter.init(false, true));
    }
    
    private void dropFinalSchemaAfterUpgradingUsingDDLs() throws IOException, SQLException {
        //drop schema
        //need to drop constraints from 6.0 first
        executeScriptRunner(DB_60_SCRIPTS_RESOURCE_PATH, ScriptFilter.init(false, false));
        executeScriptRunner(DB_DDL_SCRIPTS_RESOURCE_PATH, ScriptFilter.init(false, false));
    }
    
    /**
     * Tests that DB schema is upgraded properly using database upgrade scripts.
     * @throws IOException
     */
    @Test
    public void testExecutingScripts() throws IOException, SQLException {
        logger.info("entering testExecutingScripts with type: {} ", distributionType);
        try {
            createSchema60UsingDDLs();
            executeScriptRunner(DB_UPGRADE_SCRIPTS_RESOURCE_PATH, ScriptFilter.init(false, true).setDistribution(distributionType));
            startAndPersistSomeProcess();
        }finally {
            dropFinalSchemaAfterUpgradingUsingDDLs();
        }
    }

    /**
     * Tests that DB schema is upgraded properly using database task_assigning_tables upgrade scripts.
     * @throws IOException
     * @throws SQLException
     */
    @Test
    public void testTaskAssigningScripts() throws IOException, SQLException {
        logger.info("entering testTaskAssigningScripts with type: {} ", distributionType);
        try {
            createSchema60UsingDDLs();
            // We need to create optional table "PlanningTask" beforehand
            ScriptFilter taskAssigningTable = filter("task_assigning_tables_")
                                                .exclude("drop")
                                                .setOptions(ScriptFilter.Option.DISALLOW_EMPTY_RESULTS, ScriptFilter.Option.THROW_ON_SCRIPT_ERROR);
            executeScriptRunner(DB_DDL_SCRIPTS_RESOURCE_PATH, taskAssigningTable);
            executeScriptRunner(DB_UPGRADE_SCRIPTS_RESOURCE_PATH, ScriptFilter.init(false, true).setDistribution(distributionType));
            ScriptFilter scriptFilter = filter("task_assigning_tables.sql").
                                        setDistribution(distributionType).
                                        setOptions(ScriptFilter.Option.THROW_ON_SCRIPT_ERROR);
            executeScriptRunner(DB_UPGRADE_SCRIPTS_RESOURCE_PATH, scriptFilter);
            startAndPersistSomeProcess();
        } finally {
            dropFinalSchemaAfterUpgradingUsingDDLs();
            // Drop optional table "PlanningTask" if exists
            executeScriptRunner(DB_DDL_SCRIPTS_RESOURCE_PATH, filter("task_assigning_tables_drop_"));
        }
    }

    private void startAndPersistSomeProcess() {
        final TestPersistenceContext dbTestingContext = createAndInitContext(PersistenceUnit.DB_TESTING_VALIDATE);
        try {
            dbTestingContext.startAndPersistSomeProcess(TEST_PROCESS_ID);
            assertEquals(1, dbTestingContext.getStoredProcessesCount());
        } finally {
            dbTestingContext.clean();
        }
    }

    /**
     * Tests that persisted process is not destroyed by upgrade scripts.
     * @throws IOException
     * @throws ParseException
     * @throws SQLException
     */
    @Test
    public void testPersistedProcess() throws IOException, SQLException {
        logger.debug("entering testPersistedProcess with type: {}", distributionType);
        try {
            createSchema60UsingDDLs();
            upgradeDbWithOldProcessAndTask(distributionType);
            validateProcess();
        } finally {
            dropFinalSchemaAfterUpgradingUsingDDLs();
        }
    }

    private void upgradeDbWithOldProcessAndTask(DistributionType type) throws IOException, SQLException {
        final TestPersistenceContext scriptRunnerContext = createAndInitContext(PersistenceUnit.SCRIPT_RUNNER);
        try {
            scriptRunnerContext.persistOldProcessAndSession(TEST_SESSION_ID, TEST_PROCESS_ID, TEST_PROCESS_INSTANCE_ID);
            scriptRunnerContext.createSomeTask();
            // Execute upgrade scripts.
            scriptRunnerContext.executeScripts(new File(getClass().getResource(DB_UPGRADE_SCRIPTS_RESOURCE_PATH).getFile()), 
                                               ScriptFilter.init(false, true).setDistribution(type));
        } finally {
            scriptRunnerContext.clean();
        }
    }

    private void validateProcess() {
        final TestPersistenceContext dbTestingContext = new TestPersistenceContext();
        dbTestingContext.init(PersistenceUnit.DB_TESTING_VALIDATE);
        try {
            assertEquals(1, dbTestingContext.getStoredProcessesCount());
            assertEquals(1, dbTestingContext.getStoredSessionsCount());

            final StatefulKnowledgeSession persistedSession = dbTestingContext.loadPersistedSession(
                    TEST_SESSION_ID.longValue(), TEST_PROCESS_ID);
            assertNotNull(persistedSession);

            // Start another process.
            persistedSession.startProcess(TEST_PROCESS_ID);
            assertEquals(2, dbTestingContext.getStoredProcessesCount());

            // Load old process instance.
            ProcessInstance processInstance = persistedSession.getProcessInstance(TEST_PROCESS_INSTANCE_ID);
            assertNotNull(processInstance);

            persistedSession.signalEvent("test", null);
            processInstance = persistedSession.getProcessInstance(TEST_PROCESS_INSTANCE_ID);
            Assert.assertNull(processInstance);
            assertEquals(0, dbTestingContext.getStoredProcessesCount());

            persistedSession.dispose();
            persistedSession.destroy();
            assertEquals(0, dbTestingContext.getStoredSessionsCount());
        } finally {
            dbTestingContext.clean();
        }
    }
}
