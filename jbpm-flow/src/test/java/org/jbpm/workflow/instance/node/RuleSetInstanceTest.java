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

package org.jbpm.workflow.instance.node;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.core.datatype.DataType;
import org.jbpm.process.core.datatype.impl.type.BooleanDataType;
import org.jbpm.process.core.datatype.impl.type.FloatDataType;
import org.jbpm.process.core.datatype.impl.type.IntegerDataType;
import org.jbpm.process.core.datatype.impl.type.ObjectDataType;
import org.jbpm.process.core.datatype.impl.type.StringDataType;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.test.util.AbstractBaseTest;
import org.jbpm.workflow.core.node.Assignment;
import org.jbpm.workflow.core.node.DataAssociation;
import org.jbpm.workflow.core.node.RuleSetNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(Parameterized.class)
public class RuleSetInstanceTest extends AbstractBaseTest {
	
    public void addLogger() { 
        logger = LoggerFactory.getLogger(this.getClass());
    }

    RuleSetNodeInstance ruleSetNodeInstance;
    RuleSetNode mockRuleSetNode;
    VariableScopeInstance variableScopeInstance;
    VariableScope variableScope;

    @Parameters( name = "{index}: Actual: <{0}> Expected: <{1}> Type: <{3}>" )
    public static Collection<Object[]> data() {
        String className = "org.jbpm.workflow.instance.node.RuleSetInstanceTest$MyDataObject";

        Map<String, Object> sourceMap = new HashMap<>();
        sourceMap.put("message", "abc");
        sourceMap.put("status", true);
        sourceMap.put("code", 1);

        MyDataObject targetDataObject = new MyDataObject("abc", true, 1);

        return Arrays.asList(new Object[][] {
                {"abc", "abc", new StringDataType(), "java.lang.String"},
                {true, true, new BooleanDataType(), "java.lang.Boolean"},
                {"true", true, new BooleanDataType(), "java.lang.Boolean"},
                {123, 123, new IntegerDataType(), "java.lang.Integer"},
                {"123", 123, new IntegerDataType(), "java.lang.Integer"},
                {12.3, 12.3, new ObjectDataType(), "java.lang.Double"},
                {"12.3", 12.3F, new FloatDataType(), "java.lang.Float"},
                {sourceMap, targetDataObject, new ObjectDataType(className), className},
        });
    }

    private final String sourceName;
    private final String targetName;
    private final Object sourceObject;
    private final Object targetObject;
    private final DataType variableDataType;
    private final String targetDataType;

    public RuleSetInstanceTest(Object sourceObject, Object targetObject, DataType variableDataType, String targetDataType) {
        this.sourceName = "sourceName";
        this.targetName = "targetName";
        this.sourceObject = sourceObject;
        this.targetObject = targetObject;
        this.variableDataType = variableDataType;
        this.targetDataType = targetDataType;
    }

    @Before
    public void setup() {
        ruleSetNodeInstance = spy(RuleSetNodeInstance.class);

        mockRuleSetNode = mock(RuleSetNode.class);
        doReturn(mockRuleSetNode).when(ruleSetNodeInstance).getRuleSetNode();

        variableScopeInstance = mock(VariableScopeInstance.class);
        doReturn(variableScopeInstance).when(ruleSetNodeInstance).resolveContextInstance(any(), any());
    }
    
    @Test
    public void testProcessOutputs() {
        List<Assignment> assignments = new ArrayList<>();
        DataAssociation dataAssociation = new DataAssociation(sourceName, targetName, assignments, null);
        List<DataAssociation> dataAssociations = new ArrayList<>();
        dataAssociations.add(dataAssociation);

        doReturn(dataAssociations).when(mockRuleSetNode).getOutAssociations();

        Variable variable = new Variable();
        variable.setName(targetName);
        variable.setType(variableDataType);
        List<Variable> variables = new ArrayList<>();
        variables.add(variable);

        variableScope = new VariableScope();
        variableScope.setVariables(variables);
        doReturn(variableScope).when(variableScopeInstance).getVariableScope();

        Map<String, Object> outputs = new HashMap<>();
        outputs.put(sourceName, sourceObject);
        ruleSetNodeInstance.processOutputs(outputs);

        ArgumentCaptor<String> targetCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);

        verify(variableScopeInstance, times(1)).setVariable(targetCaptor.capture(), valueCaptor.capture());

        assertThat(targetCaptor.getValue()).isEqualTo(targetName);
        assertThat(valueCaptor.getValue().getClass().getTypeName()).isEqualTo(targetDataType);
        assertThat(valueCaptor.getValue()).isEqualToComparingFieldByField(targetObject);
    }

    public static class MyDataObject {
        private String message;
        private Boolean status;
        private Integer code;

        public MyDataObject() {
        }

        public MyDataObject(String message, Boolean status, Integer code) {
            this.message = message;
            this.status = status;
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Boolean getStatus() {
            return status;
        }

        public void setStatus(Boolean status) {
            this.status = status;
        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return "MyDataObject{" +
                    "message='" + message + '\'' +
                    ", status=" + status +
                    ", code=" + code +
                    '}';
        }
    }
}
