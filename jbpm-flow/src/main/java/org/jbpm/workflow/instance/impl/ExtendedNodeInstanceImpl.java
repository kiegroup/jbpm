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

package org.jbpm.workflow.instance.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.drools.core.spi.ProcessContext;
import org.drools.mvel.MVELSafeHelper;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.core.datatype.DataType;
import org.jbpm.process.core.impl.DataTransformerRegistry;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.process.instance.impl.Action;
import org.jbpm.process.instance.impl.AssignmentAction;
import org.jbpm.workflow.core.DroolsAction;
import org.jbpm.workflow.core.impl.ExtendedNodeImpl;
import org.jbpm.workflow.core.node.Assignment;
import org.jbpm.workflow.core.node.DataAssociation;
import org.jbpm.workflow.core.node.Transformation;
import org.kie.api.runtime.process.DataTransformer;
import org.kie.api.runtime.process.NodeInstance;

public abstract class ExtendedNodeInstanceImpl extends NodeInstanceImpl {

	private static final long serialVersionUID = 510l;
	
	public ExtendedNodeImpl getExtendedNode() {
		return (ExtendedNodeImpl) getNode();
	}
	
	public void internalTrigger(NodeInstance from, String type) {
		triggerEvent(ExtendedNodeImpl.EVENT_NODE_ENTER);
	}
	
    public void triggerCompleted(boolean remove) {
        triggerCompleted(org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE, remove);
    }
    
	protected void triggerCompleted(String type, boolean remove) {
		triggerEvent(ExtendedNodeImpl.EVENT_NODE_EXIT);
		super.triggerCompleted(type, remove);
	}
	
	protected void triggerEvent(String type) {
		ExtendedNodeImpl extendedNode = getExtendedNode();
		if (extendedNode == null) {
			return;
		}
		List<DroolsAction> actions = extendedNode.getActions(type);
		if (actions != null) {
			for (DroolsAction droolsAction: actions) {
			    Action action = (Action) droolsAction.getMetaData("Action");
				executeAction(action);
			}
		}
	}
    protected void mapOutputSetVariables(NodeInstance nodeInstance, List<DataAssociation> dataOututAssoctiation, Map<String, Object> ouputData) {
        this.mapOutputSetVariables(nodeInstance, dataOututAssoctiation, ouputData, (target, value) -> {});
    }
    protected void mapOutputSetVariables(NodeInstance nodeInstance, List<DataAssociation> dataOututAssoctiation, Map<String, Object> ouputData, BiConsumer<String, Object> parameterSet) {
        for (Iterator<DataAssociation> iterator = dataOututAssoctiation.iterator(); iterator.hasNext();) {
            DataAssociation association = iterator.next();
            if (association.getTransformation() != null) {
                Transformation transformation = association.getTransformation();
                DataTransformer transformer = DataTransformerRegistry.get().find(transformation.getLanguage());
                if (transformer != null) {
                    Object parameterValue = transformer.transform(transformation.getCompiledExpression(), ouputData);
                    VariableScopeInstance variableScopeInstance = (VariableScopeInstance) resolveContextInstance(VariableScope.VARIABLE_SCOPE, association.getTarget());
                    if (variableScopeInstance != null && parameterValue != null) {
                        variableScopeInstance.setVariable(association.getTarget(),
                                variableScopeInstance.getVariableScope().validateVariable(getProcessInstance()
                                        .getProcessName(), association.getTarget(), parameterValue));
                    } else {
                        logger.warn("Could not find variable scope for variable {}", association.getTarget());
                        logger.warn("when trying to complete Work Item {}", nodeInstance.getNodeName());
                        logger.warn("Continuing without setting variable.");
                    }
                    if (parameterValue != null) {
                        parameterSet.accept(association.getTarget(), parameterValue);
                    }
                }
            } else if (association.getAssignments() == null || association.getAssignments().isEmpty()) {
                VariableScopeInstance variableScopeInstance = (VariableScopeInstance) resolveContextInstance(VariableScope.VARIABLE_SCOPE, association.getTarget());
                if (variableScopeInstance != null) {
                    Object value = ouputData.get(association.getSources().get(0));
                    if (value == null) {
                        try {
                            value = MVELSafeHelper.getEvaluator().eval(association.getSources().get(0), new MapResolverFactory(ouputData));
                        } catch (Throwable t) {
                            // do nothing
                        }
                    }
                    Variable varDef = variableScopeInstance.getVariableScope().findVariable(association.getTarget());
                    DataType dataType = varDef.getType();
                    // exclude java.lang.Object as it is considered unknown type
                    if (!dataType.getStringType().endsWith("java.lang.Object") &&
                        !dataType.getStringType().endsWith("Object") && value instanceof String) {
                        value = dataType.readValue((String) value);
                    } else {
                        variableScopeInstance.getVariableScope().validateVariable(getProcessInstance().getProcessName(), association.getTarget(), value);
                    }
                    variableScopeInstance.setVariable(association.getTarget(), value);
                } else {
                    logger.warn("Could not find variable scope for variable {}", association.getTarget());
                    logger.warn("when trying to complete Work Item {}",nodeInstance.getNodeName());
                    logger.warn("Continuing without setting variable.");
                }

            } else {
                try {
                    for (Iterator<Assignment> it = association.getAssignments().iterator(); it.hasNext();) {
                        handleAssignment(it.next());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    
    protected void handleAssignment(Assignment assignment) {
        AssignmentAction action = (AssignmentAction) assignment.getMetaData("Action");
        try {
            ProcessContext context = new ProcessContext(getProcessInstance().getKnowledgeRuntime());
            context.setNodeInstance(this);
            action.execute(this, context);
        } catch (Exception e) {
            throw new RuntimeException("unable to execute Assignment", e);
        }
    }
	
}
