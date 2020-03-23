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

import org.jbpm.test.persistence.scripts.ScriptsBase;
import org.junit.Assert;
import org.junit.Test;

import static org.jbpm.test.persistence.scripts.PersistenceUnit.DB_QUARTZ_VALIDATE;
import static org.jbpm.test.persistence.scripts.PersistenceUnit.DB_TESTING_VALIDATE;
import static org.jbpm.persistence.scripts.TestPersistenceContext.createAndInitContext;

/**
 * Contains tests that test DDL scripts.
 */
public class DDLScriptsTest extends ScriptsBase {
    /**
     * Tests that DB schema is created properly using DDL scripts.
     */
    @Test
    public void createAndDropSchemaUsingDDLs() throws Exception {
        executeScriptRunner(DB_DDL_SCRIPTS_RESOURCE_PATH, true);
        validateAndPersistProcess();
        validateQuartz();
        executeScriptRunner(DB_DDL_SCRIPTS_RESOURCE_PATH, false);
    }

    private void validateAndPersistProcess() {
        final TestPersistenceContext dbTestingContext = createAndInitContext(DB_TESTING_VALIDATE);
        try {
            dbTestingContext.startAndPersistSomeProcess(TEST_PROCESS_ID);
            Assert.assertTrue(dbTestingContext.getStoredProcessesCount() == 1);
        } finally {
            dbTestingContext.clean();
        }
    }

    private void validateQuartz() {
        final TestPersistenceContext dbquartzContext = createAndInitContext(DB_QUARTZ_VALIDATE);
        dbquartzContext.clean();
    }
}