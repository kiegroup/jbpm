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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.jbpm.test.persistence.scripts.util.TestsUtil;
import org.jbpm.test.persistence.scripts.util.ScriptFilter.Option;
import org.jbpm.test.persistence.scripts.util.ScriptFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.jbpm.test.persistence.scripts.TestPersistenceContextBase.createAndInitContext;
import static org.jbpm.test.persistence.scripts.util.ScriptFilter.filter;

public class ScriptsBase {

    protected static final String DB_DDL_SCRIPTS_RESOURCE_PATH = "/db/ddl-scripts";
    protected static final String TEST_PROCESS_ID = "minimalProcess";
    protected static final String PERSISTENCE_XML_PATH = "target/test-classes/META-INF/persistence.xml";
    protected static final String BACKUP_PERSISTENCE_XML_PATH = "target/test-classes/META-INF/persistence-backup.xml";

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
    
    @Before
    public void setUp() throws IOException {
        // Create a backup of the original persistence.xml for not losing placeholder for next tests
        Files.copy(Paths.get(PERSISTENCE_XML_PATH), Paths.get(BACKUP_PERSISTENCE_XML_PATH), REPLACE_EXISTING);
    }
    
    @After
    public void restoreBackup() throws IOException {
        Files.copy(Paths.get(BACKUP_PERSISTENCE_XML_PATH), Paths.get(PERSISTENCE_XML_PATH), StandardCopyOption.REPLACE_EXISTING);
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
    
    protected void replaceNewGeneratorMappingsValue(ScriptFilter script) throws IOException {
        if (script.hasOption(Option.NEW_GENERATOR_MAPPINGS_TRUE)) {
            Files.write(Paths.get(PERSISTENCE_XML_PATH), getUpdatedXml("true").getBytes());
        } else {
            Files.write(Paths.get(PERSISTENCE_XML_PATH), getUpdatedXml("false").getBytes());
        }
    }

    protected String getUpdatedXml(String placeholderValue) throws IOException {
        String templateXml = new String(Files.readAllBytes(Paths.get(BACKUP_PERSISTENCE_XML_PATH)));
        return templateXml.replace("${new_generator_mappings.value}", placeholderValue);
    }
}
