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

package org.jbpm.process.core.context.variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jbpm.process.core.Context;
import org.jbpm.process.core.context.AbstractContext;
import org.jbpm.process.core.datatype.DataType;
import org.jbpm.process.core.datatype.impl.type.ObjectDataType;

/**
 * 
 */
public class VariableScope extends AbstractContext {
	
	private static boolean variableStrictEnabled = Boolean.parseBoolean(System.getProperty("org.jbpm.variable.strict", "false"));

    public static final String VARIABLE_SCOPE = "VariableScope";
    public static final String CASE_FILE_PREFIX = "caseFile_";
    
    private static final long serialVersionUID = 510l;
    
    private List<Variable> variables;
    
    public VariableScope() {
        this.variables = new ArrayList<Variable>();
    }
    
    @Override
    public String getType() {
        return VariableScope.VARIABLE_SCOPE;
    }
    
    public List<Variable> getVariables() {
        return this.variables;
    }

    public void setVariables(final List<Variable> variables) {
        if ( variables == null ) {
            throw new IllegalArgumentException( "Variables is null" );
        }
        this.variables = variables;
    }

    public String[] getVariableNames() {
        final String[] result = new String[this.variables.size()];
        if (this.variables != null) {
            for ( int i = 0; i < this.variables.size(); i++ ) {
                result[i] = this.variables.get( i ).getName();
            }
        }
        return result;
    }

    public Variable findVariable(String variableName) {
        for (Variable variable: getVariables()) {
            if (variable.getName().equals(variableName)) {
                return variable;
            }
        }
        if (variableName.startsWith(CASE_FILE_PREFIX) && variableName.indexOf(".") == -1) {
            Variable caseVariable = new Variable();
            caseVariable.setName(CASE_FILE_PREFIX+variableName);
            caseVariable.setType(new ObjectDataType());
            return caseVariable;
        }
        
        return null;
    }

    @Override
    public Context resolveContext(Object param) {
        if (param instanceof String) {
            return findVariable((String) param) == null ? null : this;
        }
        throw new IllegalArgumentException(
            "VariableScopes can only resolve variable names: " + param);
    }
    
    public Object validateVariable(String processName, String name, Object value) {
        Variable var = findVariable(name);
        if (var == null) {
            if (variableStrictEnabled) {
                throw new IllegalArgumentException("Variable '" + name + "' is not defined in process " + processName);
            }
        } else if (value != null) {
            DataType type = var.getType();
            boolean isRightType = type == null || type.verifyDataType(value);
            if (!isRightType ) {
                if (variableStrictEnabled) {
                    throw new IllegalArgumentException("Variable '" + name + "' has incorrect data type expected:" + var
                            .getType().getStringType() + " actual:" + value.getClass().getName());
				} else if (value instanceof String) {
					value = type.valueOf(value.toString());
				}
            }
        }
        return value;
    }
	
	public boolean isReadOnly(String name) {
        Variable v = findVariable(name);

        if (v != null) {
            return v.hasTag(Variable.READONLY_TAG);
        }
        return false;
    }

    public boolean isRequired(String name) {
        Variable v = findVariable(name);

        if (v != null) {
            return v.hasTag(Variable.REQUIRED_TAG);
        }
        return false;
    }
    
    public List<String> tags(String name) {
        Variable v = findVariable(name);
        
        if (v != null) {
            return v.getTags();
        }
        return Collections.emptyList();
    }
	
	/*
	 * mainly for test coverage to easily switch between settings 
	 */
	public static void setVariableStrictOption(boolean turnedOn) {
		variableStrictEnabled = turnedOn;
	}

}
