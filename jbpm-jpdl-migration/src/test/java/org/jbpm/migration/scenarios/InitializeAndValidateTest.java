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
package org.jbpm.migration.scenarios;

import org.assertj.core.api.Assertions;
import org.jbpm.migration.JbpmMigrationRuntimeTest;
import org.junit.Test;

/**
 * Insurance - InitializeAndValidateProcess scenario.
 */
@org.junit.Ignore("look at JIRA https://issues.jboss.org/browse/JBPM-4313")
public class InitializeAndValidateTest extends JbpmMigrationRuntimeTest {
    public static final String definition =
            "org/jbpm/migration/scenarios/insuranceInitializeAndValidateProcess/processdefinition.xml";

    public static final String processId = "Insurance_InitializeValidatingProcess_Process";

    @Test
    public void testBpmn() {
        prepareProcess(definition);
        Assertions.assertThat(kbase).isNotNull();
    }
}