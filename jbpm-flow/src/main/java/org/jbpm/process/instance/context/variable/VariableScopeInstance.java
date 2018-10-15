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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.drools.core.ClassObjectFilter;
import org.drools.core.event.ProcessEventSupport;
import org.jbpm.process.core.context.variable.CaseVariableInstance;
import org.jbpm.process.core.context.variable.ReferenceVariableInstance;
import org.jbpm.process.core.context.variable.ValueReference;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableInstance;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.ContextInstanceContainer;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.process.instance.context.AbstractContextInstance;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.instance.node.CompositeContextNodeInstance;
import org.kie.api.runtime.process.CaseData;

/**
 *
 */
public class VariableScopeInstance extends AbstractContextInstance {

    private static final long serialVersionUID = 510l;

    private Map<String, VariableInstance<?>> variables = new HashMap<>();
    private String variableIdPrefix = null;
    private String variableInstanceIdPrefix = null;

    public String getContextType() {
        return VariableScope.VARIABLE_SCOPE;
    }

    public Object getVariable(String name) {
        VariableInstance<Object> variableInstance = getVariableInstance(name);

        if (variableInstance != null) {
            Object value = variableInstance.get();
            if (value != null) {
                return value;
            }
        }

        // support for processInstanceId and parentProcessInstanceId
        if ("processInstanceId".equals(name) && getProcessInstance() != null) {
            return getProcessInstance().getId();
        } else if ("parentProcessInstanceId".equals(name) && getProcessInstance() != null) {
            return getProcessInstance().getParentProcessInstanceId();
        }

        if (getProcessInstance() != null && getProcessInstance().getKnowledgeRuntime() != null) {
            // support for globals
            Object value = getProcessInstance().getKnowledgeRuntime().getGlobal(name);
            if (value != null) {
                return value;
            }
            // support for case file data
            @SuppressWarnings("unchecked")
            Collection<CaseData> caseFiles = (Collection<CaseData>) getProcessInstance().getKnowledgeRuntime().getObjects(new ClassObjectFilter(CaseData.class));
            if (caseFiles.size() == 1) {
                CaseData caseFile = caseFiles.iterator().next();
                // check if there is case file prefix and if so remove it before checking case file data
                final String lookUpName = name.startsWith(VariableScope.CASE_FILE_PREFIX) ? name.replaceFirst(VariableScope.CASE_FILE_PREFIX, "") : name;
                if (caseFile != null) {
                    return caseFile.getData(lookUpName);
                }
            }
        }

        return null;
    }

    public Map<String, Object> getVariables() {
        Map<String, Object> result = new HashMap<>();
        variables.values().stream()
                .filter(v -> !(v instanceof CaseVariableInstance))
                .forEach(v -> result.put(v.name(), v.getReference().get()));
        return Collections.unmodifiableMap(result);
    }

    public void setVariable(String name, Object value) {
        if (name == null) {
            throw new IllegalArgumentException(
                    "The name of a variable may not be null!");
        }

        VariableInstance<Object> variableInstance = getVariableInstance(name);
        if (variableInstance == null) {
            if (value == null) {
                return;
            } else {
                variableInstance = addVariableInstance(name);
            }
        }
        variableInstance.set(value);
    }

    public void internalSetVariable(String name, Object value) {
        // skip checks, go straight to value reference
        getVariableInstance(name).getReference().set(value);
    }

    public <T> VariableInstance<T> getVariableInstance(String name) {
        VariableInstance<T> variableInstance = (VariableInstance<T>) variables.get(name);
        if (variableInstance == null) {
            variableInstance = addVariableInstance(name);
            if (variableInstance == null) {
                return null;
            }
        }
        return variableInstance;
    }

    private <T> VariableInstance<T> addVariableInstance(String name) {
        if (variables.containsKey(name)) {
            throw new IllegalStateException(String.format(
                    "Cannot add variable named '%s'. The same variable already exists.", name));
        }
        VariableInstance<T> variableInstance = createVariableInstance(new Variable(name));
        variables.put(name, variableInstance);
        return variableInstance;
    }

    public VariableScope getVariableScope() {
        return (VariableScope) getContext();
    }

    public void setContextInstanceContainer(ContextInstanceContainer contextInstanceContainer) {
        super.setContextInstanceContainer(contextInstanceContainer);
        if (contextInstanceContainer instanceof CompositeContextNodeInstance) {
            this.variableIdPrefix = ((Node) ((CompositeContextNodeInstance) contextInstanceContainer).getNode()).getUniqueId();
            this.variableInstanceIdPrefix = ((CompositeContextNodeInstance) contextInstanceContainer).getUniqueId();
        }

        createVariableInstances();
    }

    private void createVariableInstances() {
        getVariableScope().getVariables().stream()
                .map(this::createVariableInstance)
                .forEach(v -> variables.put(v.name(), v));
    }

    private <T> VariableInstance<T> createCaseVariableInstance(Variable variable) {
        String name = variable.getName();
        if (name.startsWith(VariableScope.CASE_FILE_PREFIX)) {
            return new CaseVariableInstance<>(this, variable);
        } else {
            return null;
        }
    }

    private <T> VariableInstance<T> createVariableInstance(Variable variable) {
        VariableInstance<T> caseVar = createCaseVariableInstance(variable);
        if (caseVar != null) {
            return caseVar;
        } else {
            String name = variable.getName();
            return new ReferenceVariableInstance<>(
                    variable,
                    new Handler<>(
                            getProcessEventSupport(),
                            getProcessInstance(),
                            name,
                            prefixed(name),
                            instancePrefixed(name)
                    ));
        }
    }

    protected ProcessEventSupport getProcessEventSupport() {
        return ((InternalProcessRuntime) getProcessInstance()
                .getKnowledgeRuntime()
                .getProcessRuntime())
                .getProcessEventSupport();
    }

    private static class Handler<T> implements ReferenceVariableInstance.OnSetHandler<T> {

        transient final ProcessEventSupport processEventSupport;
        transient final ProcessInstance processInstance;
        final String name;
        private final String prefixed;
        private final String instancePrefixed;

        public Handler(ProcessEventSupport processEventSupport, ProcessInstance processInstance, String name, String prefixed, String instancePrefixed) {
            this.processEventSupport = processEventSupport;
            this.processInstance = processInstance;
            this.name = name;
            this.prefixed = prefixed;
            this.instancePrefixed = instancePrefixed;
        }

        @Override
        public void before(Object oldValue, Object newValue) {
            processEventSupport.fireBeforeVariableChanged(
                    prefixed,
                    instancePrefixed,
                    oldValue,
                    newValue,
                    processInstance,
                    processInstance.getKnowledgeRuntime());
        }

        @Override
        public void after(Object oldValue, Object newValue) {
            processEventSupport.fireAfterVariableChanged(
                    prefixed,
                    instancePrefixed,
                    oldValue,
                    newValue,
                    processInstance,
                    processInstance.getKnowledgeRuntime());
        }
    }

    /**
     * Assign a value reference to a declared variable instance.
     * @throws IllegalArgumentException if the given variableName has not been declared in the process
     */
    public VariableInstance<?> assignVariableInstance(String variableName, ValueReference reference) {
        VariableScope variableScope = getVariableScope();
        Variable variable = variableScope.findVariable(variableName);
        if (variable == null) {
            variable = new Variable(variableName);
            variable.setMetaData("on-the-fly", true);
        }
        variableScope.validateVariable(
                getProcessInstance().getProcessName(),
                variableName,
                reference.get());
        VariableInstance<?> instance = getVariableInstance(variableName);
        instance.setReference(reference);
        return instance;
    }

    private String instancePrefixed(String name) {
        return (variableInstanceIdPrefix == null ? "" : variableInstanceIdPrefix + ":") + name;
    }

    private String prefixed(String name) {
        return (variableIdPrefix == null ? "" : variableIdPrefix + ":") + name;
    }
}
