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

package org.jbpm.casemgmt.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.jbpm.casemgmt.api.generator.CasePrefixCannotBeGeneratedException;
import org.jbpm.casemgmt.api.model.instance.CaseFileInstance;
import org.jbpm.casemgmt.impl.util.AbstractCaseServicesBaseTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CaseIdGeneratorTest extends AbstractCaseServicesBaseTest {

    @Override
    protected List<String> getProcessDefinitionFiles() {
        List<String> processes = new ArrayList<String>();
        processes.add("cases/EmptyCaseWithCaseIdPrefix1.bpmn2");
        processes.add("cases/EmptyCaseWithCaseIdPrefix2.bpmn2");
        processes.add("cases/EmptyCaseWithCaseIdPrefix3.bpmn2");
        processes.add("cases/EmptyCaseWithCaseIdPrefix4.bpmn2");
        processes.add("cases/EmptyCase.bpmn2");
        return processes;
    }

    @Test
    public void testStartEmptyCaseWithIdCaseExpression() {
        Map<String, Object> data = new HashMap<>();
        data.put("class", "test");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), "EmptyCaseWithIdCaseExpression", data);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), "EmptyCaseWithIdCaseExpression", caseFile);
        assertNotNull(caseId);
        assertEquals("EmptyCaseWithIdCaseExpression-0000000001", caseId);
        caseService.cancelCase(caseId);

    }

    @Test
    public void testStartEmptyCaseWithOverridingSequence() {
        Map<String, Object> data = new HashMap<>();
        data.put("CORRELATION_KEY", "0101010101");
        data.put("IS_PREFIX_SEQUENCE", false);
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), "EmptyCaseWithIdCaseExpressionAndEmptyPrefixExpression", data);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), "EmptyCaseWithIdCaseExpressionAndEmptyPrefixExpression", caseFile);
        assertNotNull(caseId);
        assertEquals("0101010101", caseId);
        caseService.cancelCase(caseId);
    }

    @Test
    public void testStartEmptyCaseWithIdCaseExpressionWithOverridingMetadata() {
        Map<String, Object> data = new HashMap<>();
        data.put("CORRELATION_KEY", "0101010101");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), "EmptyCaseWithCaseIdPrefix4", data);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), "EmptyCaseWithCaseIdPrefix4", caseFile);
        assertNotNull(caseId);
        assertEquals("0101010101", caseId);
        caseService.cancelCase(caseId);
    }

    @Test
    public void testErrorWhenNoVariableIsSupplied() {
        Map<String, Object> data = new HashMap<>();
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), "EmptyCaseWithCaseIdPrefix4", data);
        Assertions.assertThatThrownBy(() -> caseService.startCase(deploymentUnit.getIdentifier(), "EmptyCaseWithCaseIdPrefix4", caseFile)).isInstanceOf(CasePrefixCannotBeGeneratedException.class);

    }

    @Test
    public void testStartEmptyCaseImplicitVariableWithExpression() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Test with Expressions");
        data.put("type", "type1");
        data.put("color", "blue");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), "EmptyCaseWithIdCaseExpressionAndPrefixExpression", data);

        String caseId1 = caseService.startCase(deploymentUnit.getIdentifier(), "EmptyCaseWithIdCaseExpressionAndPrefixExpression", caseFile);
        assertNotNull(caseId1);
        assertEquals("EmptyCaseWithIdCaseExpressionAndPrefixExpression-TYPE1-0000000001", caseId1);
        caseService.cancelCase(caseId1);

        data.put("type", "3type");
        data.put("color", "green");
        String caseId2 = caseService.startCase(deploymentUnit.getIdentifier(), "EmptyCaseWithIdCaseExpressionAndPrefixExpression", caseFile);
        assertNotNull(caseId2);
        assertEquals("EmptyCaseWithIdCaseExpressionAndPrefixExpression-3TYPE-0000000001", caseId2);
        caseService.cancelCase(caseId2);

        data.put("type", "type1");
        String caseId3 = caseService.startCase(deploymentUnit.getIdentifier(), "EmptyCaseWithIdCaseExpressionAndPrefixExpression", caseFile);
        assertNotNull(caseId3);
        assertEquals("EmptyCaseWithIdCaseExpressionAndPrefixExpression-TYPE1-0000000002", caseId3);
        caseService.cancelCase(caseId3);
    }
}
