/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

import org.jbpm.test.persistence.scripts.util.ScriptFilter;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.jbpm.test.persistence.scripts.DatabaseType.POSTGRESQL;
import static org.jbpm.test.persistence.scripts.DatabaseType.ORACLE;
import static org.junit.Assume.assumeTrue;

import org.jbpm.test.persistence.scripts.DatabaseType;

/**
 * Contains tests that test springboot DDL scripts for postgresql and oracle only.
 */
public class SpringbootDDLScriptsTest extends DDLScriptsTest {
    
    @BeforeClass
    public static void hasToBeTested() {
        // execute these springboot tests only for postgresql and oracle
        TestPersistenceContext ctx = new TestPersistenceContext();
        DatabaseType dbType = ctx.getDatabaseType();
        assumeTrue(dbType==POSTGRESQL || dbType==ORACLE);
    }
    
    /**
     * Tests that DB schema is created properly using Springboot DDL scripts.
     */
    @Test
    @Override
    public void createAndDropSchemaUsingDDLs() throws Exception {
        executeScriptRunner(DB_DDL_SCRIPTS_RESOURCE_PATH, ScriptFilter.init(true, true));
        validateAndPersistProcess();
        executeScriptRunner(DB_DDL_SCRIPTS_RESOURCE_PATH, ScriptFilter.init(true, false));
    }
}