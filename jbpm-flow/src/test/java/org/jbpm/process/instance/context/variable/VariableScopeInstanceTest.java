/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.process.instance.context.variable;

import org.drools.core.event.ProcessEventSupport;
import org.drools.core.impl.StatefulKnowledgeSessionImpl;
import org.jbpm.process.core.Context;
import org.jbpm.process.core.context.variable.SimpleValueReference;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableInstance;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.core.datatype.DataType;
import org.jbpm.process.core.datatype.impl.type.IntegerDataType;
import org.jbpm.process.core.impl.ProcessImpl;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.fail;

public class VariableScopeInstanceTest {

    @Before
    public void setUp() {
        System.setProperty("org.jbpm.variable.strict", "true");
    }

    @Test
    public void useUndeclaredVariableInstance() {
        VariableScopeInstance vsi = createVariableScopeInstance();
        VariableInstance<Object> v = vsi.getVariableInstance("my-var");

        assertThat(v)
                .as("it should be possible to get an undeclared variable")
                .isNotNull();

        v.set("my-value");
        assertThat(v.get())
                .as("it should be possible to set a value to a fresh variable")
                .isEqualTo("my-value");
    }

    @Test
    public void assignUndeclaredVariable() {
        VariableScopeInstance vsi = createVariableScopeInstance();
        SimpleValueReference<String> ref = new SimpleValueReference<>("bar");
        assertThatThrownBy(() -> vsi.assignVariableInstance("foo", ref))
                .as("Should throw an IllegalArgumentException: missing variable definition")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void assignMismatchedType() {
        VariableScope scope = new VariableScope();
        // declare a variable
        scope.setVariables(asList(
                createVariable("my-var", new IntegerDataType())));

        VariableScopeInstance vsi = createVariableScopeInstance(scope);

        assertThatThrownBy(() -> vsi.assignVariableInstance("my-var", new SimpleValueReference<>("bar")))
                .as("Should throw an IllegalArgumentException: wrong variable type")
                .isInstanceOf(IllegalArgumentException.class);

        SimpleValueReference<Integer> ref = new SimpleValueReference<>(100);
        vsi.assignVariableInstance("my-var", ref);
        assertThat(vsi.getVariableInstance("my-var").getReference()).isSameAs(ref);
    }

    private Variable createVariable(String name, DataType dataType) {
        Variable variable = new Variable(name);
        variable.setType(dataType);
        return variable;
    }

    private VariableScopeInstance createVariableScopeInstance() {
        return createVariableScopeInstance(new VariableScope());
    }

    private VariableScopeInstance createVariableScopeInstance(VariableScope variableScope) {
        VariableScopeInstance vsi = new VariableScopeInstance() {
            @Override
            protected ProcessEventSupport getProcessEventSupport() {
                return new ProcessEventSupport();
            }

            @Override
            public VariableScope getVariableScope() {
                return variableScope;
            }
        };
        RuleFlowProcessInstance ruleFlowProcessInstance = new RuleFlowProcessInstance();
        ProcessImpl process = new ProcessImpl();
        process.setName("my-process");
        ruleFlowProcessInstance.setProcess(process);
        vsi.setProcessInstance(ruleFlowProcessInstance);
        return vsi;
    }
}