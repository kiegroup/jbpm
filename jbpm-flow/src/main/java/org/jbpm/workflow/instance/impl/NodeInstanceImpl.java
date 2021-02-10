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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.process.instance.WorkItem;
import org.drools.core.spi.ProcessContext;
import org.drools.mvel.MVELSafeHelper;
import org.jbpm.process.core.Context;
import org.jbpm.process.core.ContextContainer;
import org.jbpm.process.core.context.exception.ExceptionScope;
import org.jbpm.process.core.context.exclusive.ExclusiveGroup;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.core.datatype.DataType;
import org.jbpm.process.core.impl.DataTransformerRegistry;
import org.jbpm.process.instance.ContextInstance;
import org.jbpm.process.instance.ContextInstanceContainer;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.process.instance.context.exception.ExceptionScopeInstance;
import org.jbpm.process.instance.context.exclusive.ExclusiveGroupInstance;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.process.instance.impl.Action;
import org.jbpm.process.instance.impl.AssignmentAction;
import org.jbpm.process.instance.impl.ConstraintEvaluator;
import org.jbpm.process.instance.impl.NoOpExecutionErrorHandler;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.jbpm.workflow.core.node.Assignment;
import org.jbpm.workflow.core.node.DataAssociation;
import org.jbpm.workflow.core.node.ThrowNode;
import org.jbpm.workflow.core.node.Transformation;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.jbpm.workflow.instance.WorkflowRuntimeException;
import org.jbpm.workflow.instance.node.ActionNodeInstance;
import org.jbpm.workflow.instance.node.CompositeNodeInstance;
import org.kie.api.definition.process.Connection;
import org.kie.api.definition.process.Node;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.process.DataTransformer;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.NodeInstanceContainer;
import org.kie.internal.runtime.error.ExecutionErrorHandler;
import org.kie.internal.runtime.error.ExecutionErrorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jbpm.workflow.instance.NodeInstance.CancelType.ABORTED;
import static org.jbpm.workflow.instance.NodeInstance.CancelType.ERROR;
import static org.jbpm.workflow.instance.NodeInstance.CancelType.OBSOLETE;

/**
 * Default implementation of a RuleFlow node instance.
 * 
 */
public abstract class NodeInstanceImpl implements org.jbpm.workflow.instance.NodeInstance, Serializable {

    public static final String UNIQUE_ID = "UniqueId";

	private static final long serialVersionUID = 510l;
	protected static final Logger logger = LoggerFactory.getLogger(NodeInstanceImpl.class);
	
	private long id = -1;
    private long nodeId;
    private WorkflowProcessInstance processInstance;
    private org.jbpm.workflow.instance.NodeInstanceContainer nodeInstanceContainer;
    private Map<String, Object> metaData = new HashMap<String, Object>();
    private int level;
    protected Date triggerTime;
    protected int slaCompliance = ProcessInstance.SLA_NA;
    protected Date slaDueDate;
    protected long slaTimerId = -1;
    private boolean aborted = false;
    protected transient CancelType cancelType;
    
    protected transient Map<String, Object> dynamicParameters;


    public CancelType getCancelType() {
        return cancelType;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

    public void setNodeId(final long nodeId) {
        this.nodeId = nodeId;
    }

    public long getNodeId() {
        return this.nodeId;
    }
    
    public String getNodeName() {
    	Node node = getNode();
    	return node == null ? "" : node.getName();
    }
    
    public int getLevel() {
        return this.level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }

    public void setProcessInstance(final WorkflowProcessInstance processInstance) {
        this.processInstance = processInstance;
    }

    public WorkflowProcessInstance getProcessInstance() {
        return this.processInstance;
    }

    public NodeInstanceContainer getNodeInstanceContainer() {
        return this.nodeInstanceContainer;
    }
    
    public void setNodeInstanceContainer(NodeInstanceContainer nodeInstanceContainer) {
        this.nodeInstanceContainer = (org.jbpm.workflow.instance.NodeInstanceContainer) nodeInstanceContainer;
        if (nodeInstanceContainer != null) {
            this.nodeInstanceContainer.addNodeInstance(this);
        }
    }

    public Node getNode() {
    	try {
    		return ((org.jbpm.workflow.core.NodeContainer)
				this.nodeInstanceContainer.getNodeContainer()).internalGetNode( this.nodeId );
    	} catch (IllegalArgumentException e) {
    		throw new IllegalArgumentException(
				"Unknown node id: " + this.nodeId 
				+ " for node instance " + getUniqueId()
				+ " for process instance " + this.processInstance, e);
    	}
    }
    
    public boolean isInversionOfControl() {
        return false;
    }

    public final void cancel() {
        cancel(ABORTED);
    }
    
    public void cancel(CancelType cancelType) {
        this.cancelType = cancelType;
        boolean hidden = false;
        Node node = getNode();
    	if (node != null && node.getMetaData().get("hidden") != null) {
    		hidden = true;
    	}
    	if (!hidden) {
    		InternalKnowledgeRuntime kruntime = getProcessInstance().getKnowledgeRuntime();
        	((InternalProcessRuntime) kruntime.getProcessRuntime())
        		.getProcessEventSupport().fireBeforeNodeLeft(this, kruntime);
        }
        nodeInstanceContainer.removeNodeInstance(this);
        if (!hidden) {
            InternalKnowledgeRuntime kruntime = getProcessInstance().getKnowledgeRuntime();
            ((InternalProcessRuntime) kruntime.getProcessRuntime())
                    .getProcessEventSupport().fireAfterNodeLeft(this, kruntime);
        }
    }
    public final void trigger(NodeInstance from, String type) {
        trigger(from, type, Collections.emptyMap());
    }


    public final void trigger(NodeInstance from, String type, Map<String, Object> data) {
    	boolean hidden = false;
    	if (getNode().getMetaData().get("hidden") != null) {
    		hidden = true;
    	}
    	
    	if (from != null) {
            this.metaData.put("IncomingConnection", from.getNode().getMetaData().get(UNIQUE_ID));
    	}
    	if (dynamicParameters != null) {
            for (Entry<String, Object> entry : dynamicParameters.entrySet()) {
                setVariable(entry.getKey(), entry.getValue());
            }
        }
    	configureSla();
    	
    	InternalKnowledgeRuntime kruntime = getProcessInstance().getKnowledgeRuntime();
    	if (!hidden) {
    		((InternalProcessRuntime) kruntime.getProcessRuntime())
    			.getProcessEventSupport().fireBeforeNodeTriggered(this, kruntime);
    	}
        try {
            getExecutionErrorHandler().processing(this);
            internalTrigger(from, type, data);
        }
        catch (WorkflowRuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new WorkflowRuntimeException(this, getProcessInstance(), e);
        }
        if (!hidden) {
        	((InternalProcessRuntime) kruntime.getProcessRuntime())
        		.getProcessEventSupport().fireAfterNodeTriggered(this, kruntime);
        }
    }

    public void internalTrigger(NodeInstance from, String type) {
        
    }

    public void internalTrigger(NodeInstance from, String type, Map<String, Object> data) {
        internalTrigger(from, type);
    }
    /**
     * This method is used in both instances of the {@link ExtendedNodeInstanceImpl}
     * and {@link ActionNodeInstance} instances in order to handle 
     * exceptions thrown when executing actions.
     * 
     * @param action An {@link Action} instance.
     */
    protected void executeAction(Action action) {
        ProcessContext context = new ProcessContext(getProcessInstance().getKnowledgeRuntime());
        context.setNodeInstance(this);
        try {
            action.execute(context);
        } catch (Exception e) {
            String exceptionName = e.getClass().getName();
            ExceptionScopeInstance exceptionScopeInstance = (ExceptionScopeInstance)
                resolveContextInstance(ExceptionScope.EXCEPTION_SCOPE, exceptionName);
            if (exceptionScopeInstance == null) {
                throw new WorkflowRuntimeException(this, getProcessInstance(), "Unable to execute Action: " + e.getMessage(), e);
            }
            
            exceptionScopeInstance.handleException(exceptionName, e);
            cancel(ERROR);
        }
    }
    
    protected void triggerCompleted(String type, boolean remove) {
        getExecutionErrorHandler().processed(this);
        Node node = getNode();
        if (node != null) {
            String uniqueId = (String) node.getMetaData().get(UNIQUE_ID);
	    	if( uniqueId == null ) { 
	    	    uniqueId = ((NodeImpl) node).getUniqueId();
	    	}
	    	((WorkflowProcessInstanceImpl) processInstance).addCompletedNodeId(uniqueId);
	    	((WorkflowProcessInstanceImpl) processInstance).getIterationLevels().remove(uniqueId);
        }

        // if node instance was cancelled, or containing container instance was cancelled
    	if ((getNodeInstanceContainer().getNodeInstance(getId()) == null)
    			|| (((org.jbpm.workflow.instance.NodeInstanceContainer) getNodeInstanceContainer()).getState() != ProcessInstance.STATE_ACTIVE)) {
    		return;
    	}
    	
        if (remove) {
            ((org.jbpm.workflow.instance.NodeInstanceContainer) getNodeInstanceContainer())
            	.removeNodeInstance(this);
        }

        List<Connection> connections = null;
        if (node != null) {
        	if ("true".equals(System.getProperty("jbpm.enable.multi.con")) && ((NodeImpl) node).getConstraints().size() > 0) {
        		int priority = Integer.MAX_VALUE;
        		connections = ((NodeImpl)node).getDefaultOutgoingConnections();
                boolean found = false;
            	List<NodeInstanceTrigger> nodeInstances = 
            		new ArrayList<NodeInstanceTrigger>();
                List<Connection> outgoingCopy = new ArrayList<Connection>(connections);
                while (!outgoingCopy.isEmpty()) {
                    priority = Integer.MAX_VALUE;
                    Connection selectedConnection = null;
                    ConstraintEvaluator selectedConstraint = null;
                    for ( final Iterator<Connection> iterator = outgoingCopy.iterator(); iterator.hasNext(); ) {
                        final Connection connection = (Connection) iterator.next();
                        ConstraintEvaluator constraint = (ConstraintEvaluator) ((NodeImpl)node).getConstraint( connection );
    
                        if ( constraint != null  
                                && constraint.getPriority() < priority
                                && !constraint.isDefault() ) {
                            priority = constraint.getPriority();
                            selectedConnection = connection;
                            selectedConstraint = constraint;
                        }
                    }
                    if (selectedConstraint == null) {
                    	break;
                    }
                    if (selectedConstraint.evaluate( this,
                                                     selectedConnection,
                                                     selectedConstraint ) ) {
                        nodeInstances.add(new NodeInstanceTrigger(followConnection(selectedConnection), selectedConnection.getToType()));
                        found = true;
                    }
                    outgoingCopy.remove(selectedConnection);
                }
                for (NodeInstanceTrigger nodeInstance: nodeInstances) {
    	        	// stop if this process instance has been aborted / completed
                	if (((org.jbpm.workflow.instance.NodeInstanceContainer) getNodeInstanceContainer()).getState() != ProcessInstance.STATE_ACTIVE) {
    	        		return;
    	        	}
    	    		triggerNodeInstance(nodeInstance.getNodeInstance(), nodeInstance.getToType());
    	        }
                if ( !found ) {
                	for ( final Iterator<Connection> iterator = connections.iterator(); iterator.hasNext(); ) {
                        final Connection connection = (Connection) iterator.next();
                        ConstraintEvaluator constraint = (ConstraintEvaluator) ((NodeImpl)node).getConstraint( connection );
                        if ( constraint.isDefault() ) {
                        	triggerConnection(connection);
                        	found = true;
                            break;
                        }
                    }
                }
                if ( !found ) {
                    throw new IllegalArgumentException( "Uncontrolled flow node could not find at least one valid outgoing connection " + getNode().getName() );
                }   
                return;
        	} else {
        		connections = node.getOutgoingConnections(type); 
        	}
        }
        if (connections == null || connections.isEmpty() ) {
        	boolean hidden = false;
        	Node currentNode = getNode();
        	if (currentNode != null && currentNode.getMetaData().get("hidden") != null) {
        		hidden = true;
        	}
        	InternalKnowledgeRuntime kruntime = getProcessInstance().getKnowledgeRuntime();
        	if (!hidden) {
        		((InternalProcessRuntime) kruntime.getProcessRuntime())
        			.getProcessEventSupport().fireBeforeNodeLeft(this, kruntime);
        	}
        	// notify container
            ((org.jbpm.workflow.instance.NodeInstanceContainer) getNodeInstanceContainer())
        		.nodeInstanceCompleted(this, type);
            if (!hidden) {
            	((InternalProcessRuntime) kruntime.getProcessRuntime())
            		.getProcessEventSupport().fireAfterNodeLeft(this, kruntime);
            }
        } else {
        	Map<org.jbpm.workflow.instance.NodeInstance, String> nodeInstances = 
        		new HashMap<org.jbpm.workflow.instance.NodeInstance, String>();
        	for (Connection connection: connections) {
        		nodeInstances.put(followConnection(connection), connection.getToType());
        	}
        	for (Map.Entry<org.jbpm.workflow.instance.NodeInstance, String> nodeInstance: nodeInstances.entrySet()) {
	        	// stop if this process instance has been aborted / completed
	        	if (((org.jbpm.workflow.instance.NodeInstanceContainer) getNodeInstanceContainer()).getState() != ProcessInstance.STATE_ACTIVE) {
	        		return;
	        	}
	    		triggerNodeInstance(nodeInstance.getKey(), nodeInstance.getValue());
	        }
        }
    }
    
    protected org.jbpm.workflow.instance.NodeInstance followConnection(Connection connection) {
    	// check for exclusive group first
    	NodeInstanceContainer parent = getNodeInstanceContainer();
    	if (parent instanceof ContextInstanceContainer) {
    		List<ContextInstance> contextInstances = ((ContextInstanceContainer) parent).getContextInstances(ExclusiveGroup.EXCLUSIVE_GROUP);
    		if (contextInstances != null) {
    			for (ContextInstance contextInstance: new ArrayList<ContextInstance>(contextInstances)) {
    				ExclusiveGroupInstance groupInstance = (ExclusiveGroupInstance) contextInstance;
    				if (groupInstance.containsNodeInstance(this)) {
    					for (NodeInstance nodeInstance: groupInstance.getNodeInstances()) {
    						if (nodeInstance != this) {
                                ((org.jbpm.workflow.instance.NodeInstance) nodeInstance).cancel(OBSOLETE);
    						}
    					}
    					((ContextInstanceContainer) parent).removeContextInstance(ExclusiveGroup.EXCLUSIVE_GROUP, contextInstance);
    				}
    				
    			}
    		}
    	}
    	return (org.jbpm.workflow.instance.NodeInstance)
    		((org.jbpm.workflow.instance.NodeInstanceContainer) getNodeInstanceContainer())
            	.getNodeInstance(connection.getTo());
    }

    protected void triggerNodeInstance(org.jbpm.workflow.instance.NodeInstance nodeInstance, String type) {
        triggerNodeInstance(nodeInstance, type, true);
    }

    protected NodeInstance getFrom() {
        return this;
    }

    protected void triggerNodeInstance(org.jbpm.workflow.instance.NodeInstance nodeInstance, String type, boolean fireEvents) {
        triggerTime = new Date();
    	boolean hidden = false;
    	if (getNode().getMetaData().get("hidden") != null) {
    		hidden = true;
    	}
    	InternalKnowledgeRuntime kruntime = getProcessInstance().getKnowledgeRuntime();
        // trigger next node
        this.metaData.put("OutgoingConnection", nodeInstance.getNode().getMetaData().get(UNIQUE_ID));

    	if (!hidden && fireEvents) {
    		((InternalProcessRuntime) kruntime.getProcessRuntime())
    			.getProcessEventSupport().fireBeforeNodeLeft(this, kruntime);
    	}

        nodeInstance.trigger(getFrom(), type);

        if (!hidden && fireEvents) {
        	((InternalProcessRuntime) kruntime.getProcessRuntime())
        		.getProcessEventSupport().fireAfterNodeLeft(this, kruntime);
        }
    }
    
    protected void triggerConnection(Connection connection) {
    	triggerNodeInstance(followConnection(connection), connection.getToType());
    }
    
    public void retrigger(boolean remove) {
    	if (remove) {
    		cancel();
        }
    	triggerNode(getNodeId(), remove == false);
    }

    public void triggerNode(long nodeId) {
        triggerNode(nodeId, true);
    }
    
    public void triggerNode(long nodeId, boolean fireEvents) {
    	org.jbpm.workflow.instance.NodeInstance nodeInstance = (org.jbpm.workflow.instance.NodeInstance)
    		((org.jbpm.workflow.instance.NodeInstanceContainer) getNodeInstanceContainer())
            	.getNodeInstance(getNode().getNodeContainer().getNode(nodeId));
    	triggerNodeInstance(nodeInstance, org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE, fireEvents);
    }
    
    public Context resolveContext(String contextId, Object param) {
        if (getNode() == null) {
            return null;
        }
        return ((NodeImpl) getNode()).resolveContext(contextId, param);
    }
    
    public ContextInstance resolveContextInstance(String contextId, Object param) {
        Context context = resolveContext(contextId, param);
        if (context == null) {
            return null;
        }
        ContextInstanceContainer contextInstanceContainer
        	= getContextInstanceContainer(context.getContextContainer());
        if (contextInstanceContainer == null) {
        	throw new IllegalArgumentException(
    			"Could not find context instance container for context");
        }
        return contextInstanceContainer.getContextInstance(context);
    }
    
    private ContextInstanceContainer getContextInstanceContainer(ContextContainer contextContainer) {
    	ContextInstanceContainer contextInstanceContainer = null; 
		if (this instanceof ContextInstanceContainer) {
        	contextInstanceContainer = (ContextInstanceContainer) this;
        } else {
        	contextInstanceContainer = getEnclosingContextInstanceContainer(this);
        }
        while (contextInstanceContainer != null) {
    		if (contextInstanceContainer.getContextContainer() == contextContainer) {
    			return contextInstanceContainer;
    		}
    		contextInstanceContainer = getEnclosingContextInstanceContainer(
				(NodeInstance) contextInstanceContainer);
    	}
        return null;
    }
    
    private ContextInstanceContainer getEnclosingContextInstanceContainer(NodeInstance nodeInstance) {
    	NodeInstanceContainer nodeInstanceContainer = nodeInstance.getNodeInstanceContainer();
    	while (true) {
    		if (nodeInstanceContainer instanceof ContextInstanceContainer) {
    			return (ContextInstanceContainer) nodeInstanceContainer;
    		}
    		if (nodeInstanceContainer instanceof NodeInstance) {
    			nodeInstanceContainer = ((NodeInstance) nodeInstanceContainer).getNodeInstanceContainer();
    		} else {
    			return null;
    		}
    	}
    }
    
    public Object getVariable(String variableName) {
    	VariableScopeInstance variableScope = (VariableScopeInstance)
    		resolveContextInstance(VariableScope.VARIABLE_SCOPE, variableName);
    	if (variableScope == null) {
    		variableScope = (VariableScopeInstance) ((ProcessInstance) 
    			getProcessInstance()).getContextInstance(VariableScope.VARIABLE_SCOPE);
    	}
    	return variableScope.getVariable(variableName);
    }
    
    public void setVariable(String variableName, Object value) {
    	VariableScopeInstance variableScope = (VariableScopeInstance)
    		resolveContextInstance(VariableScope.VARIABLE_SCOPE, variableName);
    	if (variableScope == null) {
    		variableScope = (VariableScopeInstance) getProcessInstance().getContextInstance(VariableScope.VARIABLE_SCOPE);
    		if (variableScope.getVariableScope().findVariable(variableName) == null) {
    			variableScope = null;
    		}
    	}
    	if (variableScope == null) {
    		logger.error("Could not find variable {}", variableName);
    		logger.error("Using process-level scope");
    		variableScope = (VariableScopeInstance) ((ProcessInstance) 
    			getProcessInstance()).getContextInstance(VariableScope.VARIABLE_SCOPE);
    	}
    	variableScope.setVariable(variableName, value);
    }

    public String getUniqueId() {
    	String result = "" + getId();
    	NodeInstanceContainer parent = getNodeInstanceContainer();
    	while (parent instanceof CompositeNodeInstance) {
    		CompositeNodeInstance nodeInstance = (CompositeNodeInstance) parent;
    		result = nodeInstance.getId() + ":" + result;
    		parent = nodeInstance.getNodeInstanceContainer();
    	}
    	return result;
    }
    
    public Map<String, Object> getMetaData() {
        return this.metaData;
    }
    
	public Object getMetaData(String name) {
		return this.metaData.get(name);
	}

    public void setMetaData(String name, Object data) {
        this.metaData.put(name, data);
    }
    
    protected class NodeInstanceTrigger {
    	private org.jbpm.workflow.instance.NodeInstance nodeInstance;
    	private String toType;
    	public NodeInstanceTrigger(org.jbpm.workflow.instance.NodeInstance nodeInstance, String toType) {
    		this.nodeInstance = nodeInstance;
    		this.toType = toType;
    	}
    	public org.jbpm.workflow.instance.NodeInstance getNodeInstance() {
    		return nodeInstance;
    	}
    	public String getToType() {
    		return toType;
    	}
    }
    
    public void setDynamicParameters(Map<String, Object> dynamicParameters) {
        this.dynamicParameters = dynamicParameters;
    }
    
    protected ExecutionErrorHandler getExecutionErrorHandler() {
        ExecutionErrorManager errorManager = (ExecutionErrorManager) getProcessInstance().getKnowledgeRuntime().getEnvironment().get(EnvironmentName.EXEC_ERROR_MANAGER);
        if (errorManager == null) {
            return new NoOpExecutionErrorHandler();
        }
        return errorManager.getHandler();
    }
    
    protected void configureSla() {
        
    }
    
    public int getSlaCompliance() {
        return slaCompliance;
    }
    
    public void internalSetSlaCompliance(int slaCompliance) {
        this.slaCompliance = slaCompliance;
    }
    
    public Date getSlaDueDate() {
        return slaDueDate;
    }
    
    public void internalSetSlaDueDate(Date slaDueDate) {
        this.slaDueDate = slaDueDate;
    }
    
    public Long getSlaTimerId() {
        return slaTimerId;
    }
    
    public void internalSetSlaTimerId(Long slaTimerId) {
        this.slaTimerId = slaTimerId;
    }

    public Date getTriggerTime() {
        return triggerTime;
    }

    public boolean isAborted() {
        return aborted;
    }

    public void setAborted(boolean aborted) {
        this.aborted = aborted;
        this.cancelType = aborted ? ABORTED : null;
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

                        variableScopeInstance.getVariableScope().validateVariable(getProcessInstance().getProcessName(), association.getTarget(), parameterValue);

                        variableScopeInstance.setVariable(association.getTarget(), parameterValue);
                    } else {
                        logger.warn("Could not find variable scope for variable {}", association.getTarget());
                        logger.warn("when trying to complete NodeInstance {}", nodeInstance.getNodeName());
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

    public void mapInputSetVariables(BiConsumer<String, Object> parameterSet) {
        if(getNode() instanceof ThrowNode) {
            ThrowNode throwNode = (ThrowNode) getNode();
            mapInputSetVariables(this, throwNode.getInDataAssociations(), getSourceParameters(throwNode.getInDataAssociations()), parameterSet);
        }
    }

    protected void mapInputSetVariables(NodeInstance nodeInstance, List<DataAssociation> dataInputAssociations, Map<String, Object> inputData, BiConsumer<String, Object> parameterSet) {

        for (Iterator<DataAssociation> iterator = dataInputAssociations.iterator(); iterator.hasNext();) {
            DataAssociation association = iterator.next();
            if (association.getTransformation() != null) {
                Transformation transformation = association.getTransformation();
                DataTransformer transformer = DataTransformerRegistry.get().find(transformation.getLanguage());
                if (transformer != null) {
                    Object parameterValue = transformer.transform(transformation.getCompiledExpression(), inputData);
                    if (parameterValue != null) {
                        parameterSet.accept(association.getTarget(), parameterValue);
                    }
                }
            } else if (association.getAssignments() == null || association.getAssignments().isEmpty()) {
                Object parameterValue = null;
                VariableScopeInstance variableScopeInstance = (VariableScopeInstance) resolveContextInstance(VariableScope.VARIABLE_SCOPE, association.getSources().get(0));
                if (variableScopeInstance != null) {
                    parameterValue = variableScopeInstance.getVariable(association.getSources().get(0));
                } else {
                    try {
                        parameterValue = MVELSafeHelper.getEvaluator().eval(association.getSources().get(0), new NodeInstanceResolverFactory(this));
                    } catch (Throwable t) {
                        logger.error("Could not find variable scope for variable {}", association.getSources().get(0));
                        logger.error("when trying to execute Work Item {}", nodeInstance.getNodeName());
                        logger.error("Continuing without setting parameter.");
                    }
                }
                if (parameterValue != null) {
                    parameterSet.accept(association.getTarget(), parameterValue);
                }
            } else {
                for (Iterator<Assignment> it = association.getAssignments().iterator(); it.hasNext();) {
                    handleAssignment(it.next());
                }
            }
        }

    }

    public Map<String, Object> getSourceParameters(List<DataAssociation> associations) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        for(DataAssociation association : associations) {
            for (String sourceParam : association.getSources()) {
                Object parameterValue = null;
                VariableScopeInstance variableScopeInstance = (VariableScopeInstance) resolveContextInstance(VariableScope.VARIABLE_SCOPE, sourceParam);
                if (variableScopeInstance != null) {
                    parameterValue = variableScopeInstance.getVariable(sourceParam);
                } else {
                    try {
                        parameterValue = MVELSafeHelper.getEvaluator().eval(sourceParam, new NodeInstanceResolverFactory(this));
                    } catch (Throwable t) {
                        logger.warn("Could not find variable scope for variable {}", sourceParam);
                    }
                }
                if (parameterValue != null) {
                    parameters.put(association.getTarget(), parameterValue);
                }
            }
        }

        return parameters;
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
