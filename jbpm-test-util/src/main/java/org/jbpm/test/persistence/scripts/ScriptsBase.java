/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
import java.sql.SQLException;

import javax.sql.DataSource;

import org.jbpm.test.persistence.scripts.util.TestsUtil;
import org.jbpm.test.persistence.scripts.util.ScriptFilter;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jbpm.test.persistence.scripts.TestPersistenceContextBase.createAndInitContext;
import static org.jbpm.test.persistence.scripts.util.ScriptFilter.filter;

public class ScriptsBase {

    protected static final String DB_DDL_SCRIPTS_RESOURCE_PATH = "/db/ddl-scripts";
    protected static final String TEST_PROCESS_ID = "minimalProcess";
    
    private static final Logger logger = LoggerFactory.getLogger(ScriptsBase.class);

    public ScriptsBase() {
    }
    
    @Rule
    public TestRule watcher = new TestWatcher() {
       protected void starting(Description description) {
          logger.info(">>>> Starting test: " + description.getMethodName());
       }
    };

    @BeforeClass
    public static void cleanUp() throws IOException, SQLException {
        logger.info("Running with Hibernate " + org.hibernate.Version.getVersionString());
        TestsUtil.clearSchema();
        executeScriptRunner(DB_DDL_SCRIPTS_RESOURCE_PATH, ScriptFilter.init(false, false));
        // Drop optional table "PlanningTask" if exists
        executeScriptRunner(DB_DDL_SCRIPTS_RESOURCE_PATH, filter("task_assigning_tables_drop_"));
    }

    public static void executeScriptRunner(String resourcePath, ScriptFilter scriptFilter) throws IOException, SQLException {
        final TestPersistenceContextBase scriptRunnerContext = createAndInitContext(PersistenceUnit.SCRIPT_RUNNER);
        try {
            scriptRunnerContext.executeScripts(new File(ScriptsBase.class.getResource(resourcePath).getFile()), scriptFilter);
        } finally {
            scriptRunnerContext.clean();
        }
    }

    public static void executeScriptRunner(String resourcePath, ScriptFilter scriptFilter,
                                           DataSource dataSource, String defaultSchema) throws IOException, SQLException {
        final TestPersistenceContextBase scriptRunnerContext = new TestPersistenceContextBase();
        try {
            scriptRunnerContext.executeScripts(new File(ScriptsBase.class.getResource(resourcePath).getFile()),
                                               scriptFilter, dataSource, defaultSchema);
        } finally {
            scriptRunnerContext.clean();
        }
    }
}
