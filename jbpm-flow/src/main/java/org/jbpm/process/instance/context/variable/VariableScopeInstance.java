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

package org.jbpm.process.instance.context.variable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.drools.core.ClassObjectFilter;
import org.drools.core.event.ProcessEventSupport;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.core.context.variable.VariableViolationException;
import org.jbpm.process.instance.ContextInstanceContainer;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.context.AbstractContextInstance;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.instance.node.CompositeContextNodeInstance;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.CaseData;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.rule.FactHandle;

/**
 * 
 */
public class VariableScopeInstance extends AbstractContextInstance {

    private static final long serialVersionUID = 510l;    
    
    private Map<String, Object> variables = new HashMap<String, Object>();
    private transient String variableIdPrefix = null;
    private transient String variableInstanceIdPrefix = null;

    public String getContextType() {
        return VariableScope.VARIABLE_SCOPE;
    }

    public Object getVariable(String name) {
                
        Object value = variables.get(name);
        if (value != null) {
            return value;
        }

        // support for processInstanceId and parentProcessInstanceId
        if ("processInstanceId".equals(name) && getProcessInstance() != null) {
            return getProcessInstance().getId();
        } else if ("parentProcessInstanceId".equals(name) && getProcessInstance() != null) {
            return getProcessInstance().getParentProcessInstanceId();
        }
        

        if (getProcessInstance() != null && getProcessInstance().getKnowledgeRuntime() != null) {
            // support for globals
            value = getProcessInstance().getKnowledgeRuntime().getGlobal(name);
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
        return Collections.unmodifiableMap(variables);
    }

    public void setVariable(String name, Object value) {
        if (name == null) {
            throw new IllegalArgumentException(
                "The name of a variable may not be null!");
        }
        Object oldValue = getVariable(name);
        if (oldValue == null) {
        	if (value == null) {
        		return;
        	}
        }
        // check if variable that is being set is readonly and has already been set
        if (oldValue != null && !oldValue.equals(value) && getVariableScope().isReadOnly(name)) {
            throw new VariableViolationException(getProcessInstance().getId(), name, "Variable '" + name + "' is already set and is marked as read only");
        }
        
        ProcessEventSupport processEventSupport = ((InternalProcessRuntime) getProcessInstance()
    		.getKnowledgeRuntime().getProcessRuntime()).getProcessEventSupport();
    	processEventSupport.fireBeforeVariableChanged(
			(variableIdPrefix == null ? "" : variableIdPrefix + ":") + name,
			(variableInstanceIdPrefix == null? "" : variableInstanceIdPrefix + ":") + name,
			oldValue, value, getVariableScope().tags(name), getProcessInstance(),
			getProcessInstance().getKnowledgeRuntime());
        internalSetVariable(name, value);
        processEventSupport.fireAfterVariableChanged(
			(variableIdPrefix == null ? "" : variableIdPrefix + ":") + name,
			(variableInstanceIdPrefix == null? "" : variableInstanceIdPrefix + ":") + name,
    		oldValue, value, getVariableScope().tags(name), getProcessInstance(),
			getProcessInstance().getKnowledgeRuntime());
    }
    
    public void internalSetVariable(String name, Object value) {
        if (name.startsWith(VariableScope.CASE_FILE_PREFIX)) {
            String nameInCaseFile = name.replaceFirst(VariableScope.CASE_FILE_PREFIX, "");            
            // store it under case file rather regular variables
            @SuppressWarnings("unchecked")
            Collection<CaseData> caseFiles = (Collection<CaseData>) getProcessInstance().getKnowledgeRuntime().getObjects(new ClassObjectFilter(CaseData.class));
            if (caseFiles.size() == 1) {
                CaseData caseFile = (CaseData) caseFiles.iterator().next();
                FactHandle factHandle = getProcessInstance().getKnowledgeRuntime().getFactHandle(caseFile);
                
                if (value == null) {
                    caseFile.remove(nameInCaseFile);
                } else {
                    caseFile.add(nameInCaseFile, value);
                }
                // case data fire rules only if the state is not pending (active)
                if (getProcessInstance().getState() != ProcessInstance.STATE_PENDING) {
                    getProcessInstance().getKnowledgeRuntime().update(factHandle, caseFile);
                    ((KieSession) getProcessInstance().getKnowledgeRuntime()).fireAllRules();
                }
                return;
            }
            
        }
        // not a case, store it in normal variables
    	variables.put(name, value);
    }
    
    public VariableScope getVariableScope() {
    	return (VariableScope) getContext();
    }
    
    public void setContextInstanceContainer(ContextInstanceContainer contextInstanceContainer) {
    	super.setContextInstanceContainer(contextInstanceContainer);
    	for (Variable variable : getVariableScope().getVariables()) {
            if (variable.getValue() != null) {
                setVariable(variable.getName(), variable.getValue());
            }
        }
    	if (contextInstanceContainer instanceof CompositeContextNodeInstance) {
    		this.variableIdPrefix = ((Node) ((CompositeContextNodeInstance) contextInstanceContainer).getNode()).getUniqueId();
    		this.variableInstanceIdPrefix = ((CompositeContextNodeInstance) contextInstanceContainer).getUniqueId();
    	}
	}
    
    public void enforceRequiredVariables() {
        VariableScope variableScope = getVariableScope();
        for (Variable variable : variableScope.getVariables()) {
            String name = variable.getName();
            if (variableScope.isRequired(name)) {  
                // check case file if it is prefixed
                if (name.startsWith(VariableScope.CASE_FILE_PREFIX)) {
                    if (!findCaseData(name)) {
                        throw new VariableViolationException(getProcessInstance().getId(), name, "Case file item '" + name + "' is required but not set");
                        
                    }
                    // otherwise check variables                    
                } else if (!hasData(variables.get(name))) {
                    throw new VariableViolationException(getProcessInstance().getId(), name, "Variable '" + name + "' is required but not set");
                }
                
            }
        }
    }
    
    protected boolean findCaseData(String name) {
        boolean found = false;
        String nameInCaseFile = name.replaceFirst(VariableScope.CASE_FILE_PREFIX, "");            
        // store it under case file rather regular variables
        @SuppressWarnings("unchecked")
        Collection<CaseData> caseFiles = (Collection<CaseData>) getProcessInstance().getKnowledgeRuntime().getObjects(new ClassObjectFilter(CaseData.class));
        if (caseFiles.size() == 1) {
            CaseData caseData = caseFiles.iterator().next();
            if (hasData(caseData.getData(nameInCaseFile))) {
                found = true;
            }
        }
        
        return found;
    }
    
    private boolean hasData(Object data) {
        return data != null && (!(data instanceof CharSequence) || !data.toString().trim().isEmpty());
    }
    
    public void setDefaultValues(VariableScope variableScope, VariableScopeInstance variableScopeInstance) {
        if (variableScope != null) {
            for (Variable variable : variableScope.getVariables()) {
                String name = variable.getName();
                Object defaultValue = variable.getMetaData("defaultValue");
                if (variableScopeInstance.getVariable(name) == null && defaultValue != null) {
                    variableScopeInstance.setVariable(name, variable.getType().readValue(defaultValue.toString()));
                }
            }
        }
    }
}
