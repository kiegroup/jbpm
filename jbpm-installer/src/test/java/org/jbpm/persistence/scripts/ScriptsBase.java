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

package org.jbpm.persistence.scripts;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.jbpm.persistence.scripts.util.TestsUtil;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jbpm.persistence.scripts.TestPersistenceContext.createAndInitContext;

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
        ScriptsBase sb = new ScriptsBase();
        sb.executeScriptRunner(DB_DDL_SCRIPTS_RESOURCE_PATH, false);
    }

    protected void executeScriptRunner(String resourcePath, boolean dropFilesExcluded, String type) throws IOException, SQLException {
        final TestPersistenceContext scriptRunnerContext = createAndInitContext(PersistenceUnit.SCRIPT_RUNNER);
        try {
            scriptRunnerContext.executeScripts(new File(getClass().getResource(resourcePath).getFile()), dropFilesExcluded, type);
        } finally {
            scriptRunnerContext.clean();
        }
    }
    
    protected void executeScriptRunner(String resourcePath, boolean dropFilesExcluded) throws IOException, SQLException {
        final TestPersistenceContext scriptRunnerContext = createAndInitContext(PersistenceUnit.SCRIPT_RUNNER);
        try {
            scriptRunnerContext.executeScripts(new File(getClass().getResource(resourcePath).getFile()), dropFilesExcluded);
        } finally {
            scriptRunnerContext.clean();
        }
    }
}
