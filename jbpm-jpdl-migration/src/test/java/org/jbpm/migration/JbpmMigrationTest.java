/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.migration;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

/**
 * Tests for the {@link JbpmMigration} API.
 */
public class JbpmMigrationTest {

    @Test
    public void validDefinition() throws Exception {
        final String jpdl = FileUtils.readFileToString(new File("src/test/resources/jpdl3/singleNode/processdefinition.xml"));
        assertThat(JbpmMigration.validateJpdl(jpdl), is(true));
        final String bpmn = JbpmMigration.transform(jpdl);
        assertThat(JbpmMigration.validateBpmn(bpmn), is(true));
    }
}
