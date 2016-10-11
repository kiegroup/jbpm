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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.jbpm.migration.Validator.ProcessLanguage;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Tests for the process definition validator.
 */
public class ValidatorTest {

    @Test
    public void validJpdlDefinition() throws Exception {
        final File jpdl = new File("src/test/resources/jpdl3/singleTask/processdefinition.xml");
        final Document jpdlDoc = Validator.loadDefinition(jpdl);
        assertThat(jpdlDoc, is(notNullValue()));
        assertThat(Validator.validateDefinition(jpdlDoc, ProcessLanguage.JPDL), is(true));
    }

    @Test
    public void validBpmnDefinition() throws Exception {
        final File bpmn = new File("src/test/resources/bpmn/SingleTask.bpmn.xml");
        final Document bpmnDoc = Validator.loadDefinition(bpmn);
        assertThat(bpmnDoc, is(notNullValue()));
        assertThat(Validator.validateDefinition(bpmnDoc, ProcessLanguage.BPMN), is(true));
    }
}
