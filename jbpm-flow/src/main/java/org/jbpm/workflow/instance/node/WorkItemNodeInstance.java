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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.drools.core.WorkItemHandlerNotFoundException;
import org.drools.core.process.instance.WorkItem;
import org.drools.core.process.instance.WorkItemManager;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.drools.mvel.MVELSafeHelper;
import org.jbpm.process.core.Context;
import org.jbpm.process.core.ContextContainer;
import org.jbpm.process.core.Work;
import org.jbpm.process.core.context.exception.ExceptionScope;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.core.impl.DataTransformerRegistry;
import org.jbpm.process.instance.ContextInstance;
import org.jbpm.process.instance.ContextInstanceContainer;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.process.instance.context.exception.ExceptionScopeInstance;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.process.instance.impl.ContextInstanceFactory;
import org.jbpm.process.instance.impl.ContextInstanceFactoryRegistry;
import org.jbpm.process.instance.impl.ProcessInstanceImpl;
import org.jbpm.util.PatternConstants;
import org.jbpm.workflow.core.node.Assignment;
import org.jbpm.workflow.core.node.DataAssociation;
import org.jbpm.workflow.core.node.Transformation;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.jbpm.workflow.instance.WorkflowRuntimeException;
import org.jbpm.workflow.instance.impl.NodeInstanceResolverFactory;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.kie.api.definition.process.Node;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieRuntime;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.DataTransformer;
import org.kie.api.runtime.process.EventListener;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessWorkItemHandlerException;
import org.kie.internal.runtime.manager.context.CaseContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runtime counterpart of a work item node.
 * 
 */
public class WorkItemNodeInstance extends StateBasedNodeInstance implements EventListener, ContextInstanceContainer {

    private static final long serialVersionUID = 510l;
    private static final Logger logger = LoggerFactory.getLogger(WorkItemNodeInstance.class);

    private static boolean variableStrictEnabled = Boolean.parseBoolean(System.getProperty("org.jbpm.variable.strict", "false"));
    private static List<String> defaultOutputVariables = Arrays.asList(new String[]{"ActorId"});

    // NOTE: ContetxInstances are not persisted as current functionality (exception scope) does not require it

    private Map<String, List<ContextInstance>> subContextInstances = new HashMap<String, List<ContextInstance>>();

    private long workItemId = -1;
    protected transient WorkItem workItem;
    
    private long exceptionHandlingProcessInstanceId = -1;

    private int triggerCount = 0;

    protected WorkItemNode getWorkItemNode() {
        return (WorkItemNode) getNode();
    }

    public WorkItem getWorkItem() {
        if (workItem == null && workItemId >= 0) {
            workItem = ((WorkItemManager) ((ProcessInstance) getProcessInstance())
                                                                                  .getKnowledgeRuntime().getWorkItemManager()).getWorkItem(workItemId);
        }
        return workItem;
    }

    public long getWorkItemId() {
        return workItemId;
    }

    public void internalSetWorkItemId(long workItemId) {
        this.workItemId = workItemId;
    }

    public void internalSetWorkItem(WorkItem workItem) {
        this.workItem = workItem;
    }

    public boolean isInversionOfControl() {
        // TODO WorkItemNodeInstance.isInversionOfControl
        return false;
    }

    @Override
    public void internalTrigger(final NodeInstance from, String type) {
        super.internalTrigger(from, type);
        // if node instance was cancelled, abort
        if (getNodeInstanceContainer().getNodeInstance(getId()) == null) {
            return;
        }

        WorkItemNode workItemNode = getWorkItemNode();
        createWorkItem(workItemNode);
        if (workItemNode.isWaitForCompletion()) {
            addWorkItemListener();
        }

        WorkItemManager workItemManager = (WorkItemManager) ((ProcessInstance) getProcessInstance()).getKnowledgeRuntime().getWorkItemManager();
        String deploymentId = (String) getProcessInstance().getKnowledgeRuntime().getEnvironment().get(EnvironmentName.DEPLOYMENT_ID);
        workItem.setDeploymentId(deploymentId);
        workItem.setNodeInstanceId(this.getId());
        workItem.setNodeId(getNodeId());

        processWorkItemHandler(() -> workItemManager.internalExecuteWorkItem((org.drools.core.process.instance.WorkItem) workItem));

        if (!workItemNode.isWaitForCompletion()) {
            triggerCompleted();
        }
        this.workItemId = workItem.getId();
    }

    private void processWorkItemHandler(Runnable handler) {
        if (isInversionOfControl()) {
            ((ProcessInstance) getProcessInstance()).getKnowledgeRuntime()
                                                    .update(((ProcessInstance) getProcessInstance()).getKnowledgeRuntime().getFactHandle(this), this);
        } else {
            try {
                handler.run();
            } catch (WorkItemHandlerNotFoundException wihnfe) {
                getProcessInstance().setState(ProcessInstance.STATE_ABORTED);
                throw wihnfe;
            } catch (ProcessWorkItemHandlerException handlerException) {
                if (triggerCount++ < handlerException.getRetries() + 1) {
                    this.workItemId = workItem.getId();
                    handleWorkItemHandlerException(handlerException, workItem);
                } else {
                    throw handlerException;
                }
            } catch (Exception e) {
                String exceptionName = e.getClass().getName();
                ExceptionScopeInstance exceptionScopeInstance = (ExceptionScopeInstance) resolveContextInstance(ExceptionScope.EXCEPTION_SCOPE, exceptionName);
                if (exceptionScopeInstance == null) {
                    throw new WorkflowRuntimeException(this, getProcessInstance(), "Unable to execute Action: " + e.getMessage(), e);
                }
                // workItemId must be set otherwise cancel activity will not find the right work item
                this.workItemId = workItem.getId();
                exceptionScopeInstance.handleException(exceptionName, e);
            }
        }
    }

    protected WorkItem createWorkItem(WorkItemNode workItemNode) {
        Work work = workItemNode.getWork();
        workItem = new WorkItemImpl();
        workItem.setName(work.getName());
        workItem.setProcessInstanceId(getProcessInstance().getId());
        workItem.setParameters(new HashMap<>(work.getParameters()));
        // if there are any dynamic parameters add them
        if (dynamicParameters != null) {
            workItem.getParameters().putAll(dynamicParameters);
        }

        setWorkItemParameters(workItemNode);
        return workItem;
    }

    protected void setWorkItemParameters(WorkItemNode workItemNode) {
        setWorkItemParametersFromDataAssociations(workItemNode);
        setWorkItemParametersFromStringReplacement(workItemNode);
    }

    protected void setWorkItemParametersFromDataAssociations(WorkItemNode workItemNode) {
        for (Iterator<DataAssociation> iterator = workItemNode.getInAssociations().iterator(); iterator.hasNext();) {
            DataAssociation association = iterator.next();
            if (association.getTransformation() != null) {
                Transformation transformation = association.getTransformation();
                DataTransformer transformer = DataTransformerRegistry.get().find(transformation.getLanguage());
                if (transformer != null) {
                    Object parameterValue = transformer.transform(transformation.getCompiledExpression(), getSourceParameters(association));
                    if (parameterValue != null) {
                        workItem.setParameter(association.getTarget(), parameterValue);
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
                        logger.error("when trying to execute Work Item {}", workItemNode.getWork().getName());
                        logger.error("Continuing without setting parameter.");
                    }
                }
                if (parameterValue != null) {
                    workItem.setParameter(association.getTarget(), parameterValue);
                }
            } else {
                for (Iterator<Assignment> it = association.getAssignments().iterator(); it.hasNext();) {
                    handleAssignment(it.next());
                }
            }
        }

    }
	
    protected void setWorkItemParametersFromStringReplacement(WorkItemNode workItemNode) {
        for (Map.Entry<String, Object> entry : workItem.getParameters().entrySet()) {
            if (entry.getValue() instanceof String) {
                String s = (String) entry.getValue();
                Map<String, String> replacements = new HashMap<String, String>();
                Matcher matcher = PatternConstants.PARAMETER_MATCHER.matcher(s);
                while (matcher.find()) {
                    String paramName = matcher.group(1);
                    if (replacements.get(paramName) == null) {
                        VariableScopeInstance variableScopeInstance = (VariableScopeInstance) resolveContextInstance(VariableScope.VARIABLE_SCOPE, paramName);
                        if (variableScopeInstance != null) {
                            Object variableValue = variableScopeInstance.getVariable(paramName);
                            String variableValueString = variableValue == null ? "" : variableValue.toString();
                            replacements.put(paramName, variableValueString);
                        } else {
                            try {
                                Object variableValue = MVELSafeHelper.getEvaluator().eval(paramName, new NodeInstanceResolverFactory(this));
                                String variableValueString = variableValue == null ? "" : variableValue.toString();
                                replacements.put(paramName, variableValueString);
                            } catch (Throwable t) {
                                logger.error("Could not find variable scope for variable {}", paramName);
                                logger.error("when trying to replace variable in string for Work Item {}", workItemNode.getWork().getName());
                                logger.error("Continuing without setting parameter.");
                            }
                        }
                    }
                }

                for (Map.Entry<String, String> replacement : replacements.entrySet()) {
                    s = s.replace("#{" + replacement.getKey() + "}", replacement.getValue());
                }
                workItem.setParameter(entry.getKey(), s);

            }
        }
    }



    public void triggerCompleted(WorkItem workItem) {
        this.workItem = workItem;
        WorkItemNode workItemNode = getWorkItemNode();

        if (workItemNode != null && workItem.getState() == WorkItem.COMPLETED) {
            updateVariablesFromResult(workItem, workItemNode);
        }
        // handle dynamic nodes
        if (getNode() == null) {
            setMetaData("NodeType", workItem.getName());

            mapDynamicOutputData(workItem.getResults());
        }
        if (isInversionOfControl()) {
            KieRuntime kruntime = ((ProcessInstance) getProcessInstance()).getKnowledgeRuntime();
            kruntime.update(kruntime.getFactHandle(this), this);
        } else {
            triggerCompleted();
        }
    }

    protected void updateVariablesFromResult(WorkItem workItem, WorkItemNode workItemNode) {
        validateWorkItemResultVariable(getProcessInstance().getProcessName(), workItemNode.getOutAssociations(), workItem);
        mapOutputSetVariables(this, getWorkItemNode().getOutAssociations(), workItem.getResults(), (target, value) -> workItem.setParameter(target, value));
    }
    

    @Override
    public void cancel(CancelType cancelType) {
        WorkItem workItem = getWorkItem();
        if (workItem != null &&
            workItem.getState() != WorkItem.COMPLETED &&
            workItem.getState() != WorkItem.ABORTED) {
            try {
                ((WorkItemManager) ((ProcessInstance) getProcessInstance())
                                                                           .getKnowledgeRuntime().getWorkItemManager()).internalAbortWorkItem(workItemId);
            } catch (WorkItemHandlerNotFoundException wihnfe) {
                getProcessInstance().setState(ProcessInstance.STATE_ABORTED);
                throw wihnfe;
            }
        }
        
        if (exceptionHandlingProcessInstanceId > -1) {
            ProcessInstance processInstance = null;
            KieRuntime kruntime = getKieRuntimeForExceptionSubprocess();
            processInstance = (ProcessInstance) kruntime.getProcessInstance(exceptionHandlingProcessInstanceId);

            if (processInstance != null) {
                processInstance.setState(ProcessInstance.STATE_ABORTED);
            }
        }
        super.cancel(cancelType);
    }

    public void addEventListeners() {
        super.addEventListeners();
        addWorkItemListener();
        addExceptionProcessListener();
    }

    private void addWorkItemListener() {
        getProcessInstance().addEventListener("workItemCompleted", this, false);
        getProcessInstance().addEventListener("workItemAborted", this, false);
    }

    public void removeEventListeners() {
        super.removeEventListeners();
        getProcessInstance().removeEventListener("workItemCompleted", this, false);
        getProcessInstance().removeEventListener("workItemAborted", this, false);
    }

    @Override
    public void signalEvent(String type, Object event) {
        if ("workItemCompleted".equals(type)) {
            workItemCompleted((WorkItem) event);
        } else if ("workItemAborted".equals(type)) {
            workItemAborted((WorkItem) event);
        } else if (("processInstanceCompleted:" + exceptionHandlingProcessInstanceId).equals(type)) {
            exceptionHandlingCompleted((ProcessInstance) event, null);
        } else if (type.equals("RuleFlow-Activate" + getProcessInstance().getProcessId() + "-" + getNode().getMetaData().get("UniqueId"))) {
            trigger(null, org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE);
        } else {
            super.signalEvent(type, event);
        }
    }

    public String[] getEventTypes() {
        if (exceptionHandlingProcessInstanceId > -1) {
            return new String[] {"workItemCompleted", "processInstanceCompleted:" + exceptionHandlingProcessInstanceId };
        } else {
            return new String[]{"workItemCompleted"};
        }
    }

    public void workItemAborted(WorkItem workItem) {
        if (workItemId == workItem.getId() || (workItemId == -1 && getWorkItem().getId() == workItem.getId())) {
            removeEventListeners();
            triggerCompleted(workItem);
        }
    }

    public void workItemCompleted(WorkItem workItem) {
        if (workItemId == workItem.getId() || (workItemId == -1 && getWorkItem().getId() == workItem.getId())) {
            removeEventListeners();
            triggerCompleted(workItem);
        }
    }

    public String getNodeName() {
        Node node = getNode();
        if (node == null) {
            String nodeName = "[Dynamic]";
            WorkItem workItem = getWorkItem();
            if (workItem != null) {
                nodeName += " " + workItem.getParameter("TaskName");
            }
            return nodeName;
        }
        return super.getNodeName();
    }

    @Override
    public List<ContextInstance> getContextInstances(String contextId) {
        return this.subContextInstances.get(contextId);
    }

    @Override
    public void addContextInstance(String contextId, ContextInstance contextInstance) {
        List<ContextInstance> list = this.subContextInstances.get(contextId);
        if (list == null) {
            list = new ArrayList<ContextInstance>();
            this.subContextInstances.put(contextId, list);
        }
        list.add(contextInstance);
    }

    @Override
    public void removeContextInstance(String contextId, ContextInstance contextInstance) {
        List<ContextInstance> list = this.subContextInstances.get(contextId);
        if (list != null) {
            list.remove(contextInstance);
        }
    }

    @Override
    public ContextInstance getContextInstance(String contextId, long id) {
        List<ContextInstance> contextInstances = subContextInstances.get(contextId);
        if (contextInstances != null) {
            for (ContextInstance contextInstance : contextInstances) {
                if (contextInstance.getContextId() == id) {
                    return contextInstance;
                }
            }
        }
        return null;
    }

    @Override
    public ContextInstance getContextInstance(Context context) {
        ContextInstanceFactory conf = ContextInstanceFactoryRegistry.INSTANCE.getContextInstanceFactory(context);
        if (conf == null) {
            throw new IllegalArgumentException("Illegal context type (registry not found): " + context.getClass());
        }
        ContextInstance contextInstance = (ContextInstance) conf.getContextInstance(context, this, (ProcessInstance) getProcessInstance());
        if (contextInstance == null) {
            throw new IllegalArgumentException("Illegal context type (instance not found): " + context.getClass());
        }
        return contextInstance;
    }

    @Override
    public ContextContainer getContextContainer() {
        return getWorkItemNode();
    }

    protected Map<String, Object> getSourceParameters(DataAssociation association) {
        Map<String, Object> parameters = new HashMap<String, Object>();
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

        return parameters;
    }

    public void validateWorkItemResultVariable(String processName, List<DataAssociation> outputs, WorkItem workItem) {
        // in case work item results are skip validation as there is no notion of mandatory data outputs
        if (!variableStrictEnabled || workItem.getResults().isEmpty()) {
            return;
        }

        List<String> outputNames = new ArrayList<String>();
        for (DataAssociation association : outputs) {
            if (association.getSources() != null) {
                outputNames.add(association.getSources().get(0));
            }
            if (association.getAssignments() != null) {
                for (Iterator<Assignment> it = association.getAssignments().iterator(); it.hasNext();) {
                    outputNames.add(it.next().getFrom());
                }
            }
        }

        for (String outputName : workItem.getResults().keySet()) {
            if (!outputNames.contains(outputName) && !defaultOutputVariables.contains(outputName)) {
                throw new IllegalArgumentException("Data output '" + outputName + "' is not defined in process '" + processName + "' for task '" + workItem.getParameter("NodeName") + "'");
            }
        }
    }
    
    /*
     * Work item handler exception handling 
     */
    

    private void handleWorkItemHandlerException(ProcessWorkItemHandlerException handlerException, WorkItem workItem) {
        Map<String, Object> parameters = new HashMap<>();
        
        parameters.put("DeploymentId", workItem.getDeploymentId());
        parameters.put("ProcessInstanceId", workItem.getProcessInstanceId());
        parameters.put("WorkItemId", workItem.getId());
        parameters.put("NodeInstanceId", this.getId());
        parameters.put("ErrorMessage", handlerException.getMessage());        
        parameters.put("Error", handlerException);  
        
        // add all parameters of the work item to the newly started process instance
        parameters.putAll(workItem.getParameters());
        
        KieRuntime kruntime = getKieRuntimeForSubprocess();

        ProcessInstance processInstance = ( ProcessInstance ) kruntime.createProcessInstance(handlerException.getProcessId(), parameters);
        
        this.exceptionHandlingProcessInstanceId = processInstance.getId(); 
        ((ProcessInstanceImpl) processInstance).setMetaData("ParentProcessInstanceId", getProcessInstance().getId());
        ((ProcessInstanceImpl) processInstance).setMetaData("ParentNodeInstanceId", getUniqueId());
        
        ((ProcessInstanceImpl) processInstance).setParentProcessInstanceId(getProcessInstance().getId());
        ((ProcessInstanceImpl) processInstance).setSignalCompletion(true);


        kruntime.startProcessInstance(processInstance.getId());

        if (processInstance.getState() == ProcessInstance.STATE_COMPLETED || processInstance.getState() == ProcessInstance.STATE_ABORTED) {
            exceptionHandlingCompleted(processInstance, handlerException);
        } else {
            addExceptionProcessListener();
        }
    }

    private void exceptionHandlingCompleted(ProcessInstance processInstance, ProcessWorkItemHandlerException handlerException) {
        this.exceptionHandlingProcessInstanceId = -1;
        if (handlerException == null) {
            handlerException = (ProcessWorkItemHandlerException) ((WorkflowProcessInstance)processInstance).getVariable("Error");
        }
                
        switch (handlerException.getStrategy()) {
            case ABORT:
                ((WorkItemManager) ((ProcessInstance) getProcessInstance())
                        .getKnowledgeRuntime().getWorkItemManager()).abortWorkItem(getWorkItem().getId());
                break;
            case RETHROW:
                String exceptionName = handlerException.getCause().getClass().getName();
                ExceptionScopeInstance exceptionScopeInstance = (ExceptionScopeInstance) resolveContextInstance(ExceptionScope.EXCEPTION_SCOPE, exceptionName);
                if (exceptionScopeInstance == null) {
                    throw new WorkflowRuntimeException(this, getProcessInstance(), "Unable to execute work item " + handlerException.getMessage(), handlerException.getCause());
                }
               
                exceptionScopeInstance.handleException(exceptionName, handlerException.getCause());
                break;
            case RETRY:
                Map<String, Object> parameters = new HashMap<>(getWorkItem().getParameters());
                
                parameters.putAll(((WorkflowProcessInstanceImpl)processInstance).getVariables());
                WorkItemManager workItemManager = ((WorkItemManager) ((ProcessInstance) getProcessInstance()).getKnowledgeRuntime().getWorkItemManager());
                processWorkItemHandler(() -> workItemManager.retryWorkItem(getWorkItem().getId(), parameters));
                break;
            case COMPLETE:
                
                ((WorkItemManager) ((ProcessInstance) getProcessInstance())
                        .getKnowledgeRuntime().getWorkItemManager()).completeWorkItem(getWorkItem().getId(), 
                                ((WorkflowProcessInstanceImpl)processInstance).getVariables());                
                break;
            default:
                break;
        }
        
    }

    public void addExceptionProcessListener() {
        if (exceptionHandlingProcessInstanceId > -1) {
            getProcessInstance().addEventListener("processInstanceCompleted:" + exceptionHandlingProcessInstanceId, this, true);
        }
    }

    public void removeExceptionProcessListeners() {
        super.removeEventListeners();
        getProcessInstance().removeEventListener("processInstanceCompleted:" + exceptionHandlingProcessInstanceId, this, true);
    }
    
    public long getExceptionHandlingProcessInstanceId() {
        return exceptionHandlingProcessInstanceId;
    }
    
    public void internalSetProcessInstanceId(long processInstanceId) {
        this.exceptionHandlingProcessInstanceId = processInstanceId;
    }

    
    protected KieRuntime getKieRuntimeForExceptionSubprocess() {
        return getKieRuntimeForSubprocess(ProcessInstanceIdContext.get(getExceptionHandlingProcessInstanceId()));
        
    }

    protected KieRuntime getKieRuntimeForSubprocess() {
        return getKieRuntimeForSubprocess(ProcessInstanceIdContext.get());
    }
    protected KieRuntime getKieRuntimeForSubprocess(org.kie.api.runtime.manager.Context<?> context) {
        KieRuntime kruntime = ((ProcessInstance) getProcessInstance()).getKnowledgeRuntime();
        RuntimeManager manager = (RuntimeManager) kruntime.getEnvironment().get(EnvironmentName.RUNTIME_MANAGER);
        if (manager != null) {
            org.kie.api.runtime.manager.Context<?> newContext = context;
            String caseId = (String) kruntime.getEnvironment().get(EnvironmentName.CASE_ID);
            if (caseId != null) {
                newContext = CaseContext.get(caseId);
            }

            RuntimeEngine runtime = manager.getRuntimeEngine(newContext);
            kruntime = (KieRuntime) runtime.getKieSession();
        }

        return kruntime;
    }
    
    /*
     * mainly for test coverage to easily switch between settings 
     */
    public static void setVariableStrictOption(boolean turnedOn) {
        variableStrictEnabled = turnedOn;
    }
}
