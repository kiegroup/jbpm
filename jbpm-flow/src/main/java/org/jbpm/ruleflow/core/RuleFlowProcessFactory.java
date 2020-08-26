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

package org.jbpm.ruleflow.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.jbpm.process.core.context.exception.ActionExceptionHandler;
import org.jbpm.process.core.context.exception.ExceptionHandler;
import org.jbpm.process.core.context.swimlane.Swimlane;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.datatype.DataType;
import org.jbpm.process.core.validation.ProcessValidationError;
import org.jbpm.ruleflow.core.validation.RuleFlowProcessValidator;
import org.jbpm.workflow.core.impl.DroolsConsequenceAction;
import org.kie.api.definition.process.Process;
import org.kie.api.fluent.ProcessBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RuleFlowProcessFactory extends RuleFlowNodeContainerFactory<ProcessBuilder, ProcessBuilder> implements ProcessBuilder {
    
    private static final Logger logger = LoggerFactory.getLogger(RuleFlowProcessFactory.class);

    public static RuleFlowProcessFactory createProcess(String id) {
        return new RuleFlowProcessFactory(id);
    }

    protected RuleFlowProcessFactory(String id) {
        super(null, null, new RuleFlowProcess(), id);
    }
    
    @Override
    protected void setId(Object node, Object id) {
        getRuleFlowProcess().setId((String) id);
    }

    protected RuleFlowProcess getRuleFlowProcess() {
        return (RuleFlowProcess) node;
    }

    @Override
    public RuleFlowProcessFactory name(String name) {
    	getRuleFlowProcess().setName(name);
        return this;
    }

    @Override
    public RuleFlowProcessFactory setMetadata(String name, Object value) {
        getRuleFlowProcess().setMetaData(name, value);
        return this;
    }

    @Override
    public ProcessBuilder done() {
        return this;
    }

    @Override
    public RuleFlowProcessFactory dynamic(boolean dynamic) {
        getRuleFlowProcess().setDynamic(dynamic);
        return this;
    }

    @Override
    public RuleFlowProcessFactory version(String version) {
    	getRuleFlowProcess().setVersion(version);
        return this;
    }

    @Override
    public RuleFlowProcessFactory packageName(String packageName) {
    	getRuleFlowProcess().setPackageName(packageName);
        return this;
    }

    public RuleFlowProcessFactory imports(String... imports) {
    	getRuleFlowProcess().setImports(new HashSet<String>(Arrays.asList(imports)));
        return this;
    }
    
    public RuleFlowProcessFactory functionImports(String... functionImports) {
    	getRuleFlowProcess().setFunctionImports(Arrays.asList(functionImports));
        return this;
    }
    
    @Override
    public RuleFlowProcessFactory globals(Map<String, String> globals) {
    	getRuleFlowProcess().setGlobals(globals);
        return this;
    }
    
    @Override
    public RuleFlowProcessFactory global(String name, String type) {
    	Map<String, String> globals = getRuleFlowProcess().getGlobals();
    	if (globals == null) {
    		globals = new HashMap<String, String>();
    		getRuleFlowProcess().setGlobals(globals);
    	}
    	globals.put(name, type);
    	return this;
    }

    public RuleFlowProcessFactory variable(String name, DataType type) {
    	return variable(name, type, null);
    }
    
    public RuleFlowProcessFactory variable(String name, DataType type, Object value) {
    	return variable(name, type, value, null, null);
    }
    
    public RuleFlowProcessFactory variable(String name, DataType type, String metaDataName, Object metaDataValue) {
    	return variable(name, type, null, metaDataName, metaDataValue);
    }
    
    public RuleFlowProcessFactory variable(String name, DataType type, Object value, String metaDataName, Object metaDataValue) {
    	Variable variable = new Variable();
    	variable.setName(name);
    	variable.setType(type);
    	variable.setValue(value);
    	if (metaDataName != null && metaDataValue != null) {
    		variable.setMetaData(metaDataName, metaDataValue);
    	}
    	getRuleFlowProcess().getVariableScope().getVariables().add(variable);
    	return this;
    }
    
    @Override
    public RuleFlowProcessFactory swimlane(String name) {
    	Swimlane swimlane = new Swimlane();
    	swimlane.setName(name);
    	getRuleFlowProcess().getSwimlaneContext().addSwimlane(swimlane);
    	return this;
    }
    
    @Override
    public RuleFlowProcessFactory exceptionHandler(String exception, ExceptionHandler exceptionHandler) {
    	getRuleFlowProcess().getExceptionScope().setExceptionHandler(exception, exceptionHandler);
    	return this;
    }
    
    @Override
    public RuleFlowProcessFactory exceptionHandler(String exception, String dialect, String action) {
    	ActionExceptionHandler exceptionHandler = new ActionExceptionHandler();
    	exceptionHandler.setAction(new DroolsConsequenceAction(dialect, action));
    	return exceptionHandler(exception, exceptionHandler);
    }
    
    public RuleFlowProcessFactory validate() {
        ProcessValidationError[] errors = RuleFlowProcessValidator.getInstance().validateProcess(getRuleFlowProcess());
        for (ProcessValidationError error : errors) {
            logger.error(error.toString());
        }
        if (errors.length > 0) {
            throw new RuntimeException("Process could not be validated !");
        }
        return this;
    }
    

    public RuleFlowProcess getProcess() {
        return getRuleFlowProcess();
    }

    @Override
    public ProcessBuilder imports(Collection<String> imports) {
        getRuleFlowProcess().setImports(new HashSet<>(imports));
        return this;
    }

    @Override
    public ProcessBuilder functionImports(Collection<String> functionImports) {
        getRuleFlowProcess().setFunctionImports(new ArrayList<>(functionImports));
        return this;
    }

    @Override
    public Process build() {
        return validate().getProcess();
    }
}

