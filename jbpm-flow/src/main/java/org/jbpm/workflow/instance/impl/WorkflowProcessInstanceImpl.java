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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.mvel.MVELSafeHelper;
import org.jbpm.process.core.ContextContainer;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.core.timer.BusinessCalendar;
import org.jbpm.process.core.timer.DateTimeUtils;
import org.jbpm.process.core.timer.Timer;
import org.jbpm.process.instance.ContextInstance;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.process.instance.impl.ProcessInstanceImpl;
import org.jbpm.process.instance.timer.TimerInstance;
import org.jbpm.util.PatternConstants;
import org.jbpm.workflow.core.DroolsAction;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.jbpm.workflow.core.node.ActionNode;
import org.jbpm.workflow.core.node.AsyncEventNode;
import org.jbpm.workflow.core.node.BoundaryEventNode;
import org.jbpm.workflow.core.node.CompositeContextNode;
import org.jbpm.workflow.core.node.DynamicNode;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.EventNode;
import org.jbpm.workflow.core.node.EventNodeInterface;
import org.jbpm.workflow.core.node.EventSubProcessNode;
import org.jbpm.workflow.core.node.ForEachNode;
import org.jbpm.workflow.core.node.StateBasedNode;
import org.jbpm.workflow.core.node.StateNode;
import org.jbpm.workflow.instance.NodeInstance;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.jbpm.workflow.instance.node.CompositeContextNodeInstance;
import org.jbpm.workflow.instance.node.CompositeNodeInstance;
import org.jbpm.workflow.instance.node.DynamicNodeInstance;
import org.jbpm.workflow.instance.node.EndNodeInstance;
import org.jbpm.workflow.instance.node.EventBasedNodeInstanceInterface;
import org.jbpm.workflow.instance.node.EventNodeInstance;
import org.jbpm.workflow.instance.node.EventNodeInstanceInterface;
import org.jbpm.workflow.instance.node.EventSubProcessNodeInstance;
import org.jbpm.workflow.instance.node.FaultNodeInstance;
import org.jbpm.workflow.instance.node.ForEachNodeInstance;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.NodeContainer;
import org.kie.api.definition.process.WorkflowProcess;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieRuntime;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.EventListener;
import org.kie.api.runtime.process.NodeInstanceContainer;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.kie.internal.runtime.manager.SessionNotFoundException;
import org.kie.internal.runtime.manager.context.CaseContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.mvel2.integration.VariableResolver;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.ImmutableDefaultFactory;
import org.mvel2.integration.impl.SimpleValueResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jbpm.workflow.instance.NodeInstance.CancelType.OBSOLETE;
import static org.jbpm.workflow.instance.impl.DummyEventListener.EMPTY_EVENT_LISTENER;

/**
 * Default implementation of a RuleFlow process instance.
 */
public abstract class WorkflowProcessInstanceImpl extends ProcessInstanceImpl
        implements WorkflowProcessInstance,
                   org.jbpm.workflow.instance.NodeInstanceContainer {

    private static final long serialVersionUID = 510l;
    private static final Logger logger = LoggerFactory.getLogger(WorkflowProcessInstanceImpl.class);

	private final List<NodeInstance> nodeInstances = new ArrayList<NodeInstance>();;

	private AtomicLong singleNodeInstanceCounter = new AtomicLong(-1);

	private Map<String, List<EventListener>> eventListeners = new HashMap<>();
	private Map<String, List<EventListener>> externalEventListeners = new HashMap<>();

	private List<String> completedNodeIds = new ArrayList<>();
	private List<String> activatingNodeIds;
	private Map<String, Integer> iterationLevels = new HashMap<>();
	private int currentLevel;
	private boolean persisted = false;
	private Object faultData;

    private boolean signalCompletion = true;

    private String deploymentId;
    private String correlationKey;

	private Date startDate;
	
	private int slaCompliance = SLA_NA;
	private Date slaDueDate;
	private long slaTimerId = -1;

	private AgendaFilter agendaFilter;



    @Override
    public NodeContainer getNodeContainer() {
        return getWorkflowProcess();
    }

    @Override
	public void addNodeInstance(final NodeInstance nodeInstance) {
	    if (nodeInstance.getId() == -1) {
            // assign new id only if it does not exist as it might already be set by marshalling
            // it's important to keep same ids of node instances as they might be references e.g. exclusive group
    	    long id = singleNodeInstanceCounter.incrementAndGet();
    		((NodeInstanceImpl) nodeInstance).setId(id);
	    }
		this.nodeInstances.add(nodeInstance);
	}

    @Override
    public int getLevelForNode(String uniqueID) {
        if ("true".equalsIgnoreCase(System.getProperty("jbpm.loop.level.disabled"))) {
            return 1;
        }

        Integer value = iterationLevels.get(uniqueID);
        if (value == null && currentLevel == 0) {
            value = 1;
        } else if ((value == null && currentLevel > 0) || (value != null && currentLevel > 0 && value > currentLevel)) {
            value = currentLevel;
        } else {
            value++;
        }

        iterationLevels.put(uniqueID, value);
        return value;
    }

    @Override
    public void removeNodeInstance(final NodeInstance nodeInstance) {
        if (((NodeInstanceImpl) nodeInstance).isInversionOfControl()) {
            getKnowledgeRuntime().delete(
                    getKnowledgeRuntime().getFactHandle(nodeInstance));
        }
        this.nodeInstances.remove(nodeInstance);
    }

    @Override
    public Collection<org.kie.api.runtime.process.NodeInstance> getNodeInstances() {
        return new ArrayList<>(getNodeInstances(false));
    }

    @Override
    public Collection<NodeInstance> getNodeInstances(boolean recursive) {
        Collection<NodeInstance> result = nodeInstances;
        if (recursive) {
            result = new ArrayList<>(result);
            for (Iterator<NodeInstance> iterator = nodeInstances.iterator(); iterator
                    .hasNext(); ) {
                NodeInstance nodeInstance = iterator.next();
                if (nodeInstance instanceof NodeInstanceContainer) {
                    result.addAll(((org.jbpm.workflow.instance.NodeInstanceContainer) nodeInstance).getNodeInstances(true));
                }
            }
        }
        return Collections.unmodifiableCollection(result);
    }

    @Override
	public NodeInstance getNodeInstance(long nodeInstanceId) {
		for (NodeInstance nodeInstance : nodeInstances) {
			if (nodeInstance.getId() == nodeInstanceId) {
				return nodeInstance;
			}
		}
		return null;
	}

    public NodeInstance getNodeInstanceByNodeId(long nodeId, boolean recursive) {

        for (NodeInstance nodeInstance : getNodeInstances(recursive)) {
            if (nodeInstance.getNodeId() == nodeId) {
                return nodeInstance;
            }
        }
        return null;
    }

	@Override
	public NodeInstance getNodeInstance(long nodeInstanceId, boolean recursive) {
		for (NodeInstance nodeInstance : getNodeInstances(recursive)) {
			if (nodeInstance.getId() == nodeInstanceId) {
				return nodeInstance;
			}
		}
		return null;
	}

    public List<String> getActiveNodeIds() {
        List<String> result = new ArrayList<>();
        addActiveNodeIds(this, result);
        return result;
    }

    private void addActiveNodeIds(NodeInstanceContainer container, List<String> result) {
        for (org.kie.api.runtime.process.NodeInstance nodeInstance : container.getNodeInstances()) {
            result.add(((NodeImpl) nodeInstance.getNode()).getUniqueId());
            if (nodeInstance instanceof NodeInstanceContainer) {
                addActiveNodeIds((NodeInstanceContainer) nodeInstance, result);
            }
        }
    }

    @Override
    public NodeInstance getFirstNodeInstance(final long nodeId) {
        for (final Iterator<NodeInstance> iterator = this.nodeInstances
                .iterator(); iterator.hasNext(); ) {
            final NodeInstance nodeInstance = iterator.next();
            if (nodeInstance.getNodeId() == nodeId && nodeInstance.getLevel() == getCurrentLevel()) {
                return nodeInstance;
            }
        }
        return null;
    }

    public List<NodeInstance> getNodeInstances(final long nodeId) {
        List<NodeInstance> result = new ArrayList<>();
        for (final Iterator<NodeInstance> iterator = this.nodeInstances
                .iterator(); iterator.hasNext(); ) {
            final NodeInstance nodeInstance = iterator.next();
            if (nodeInstance.getNodeId() == nodeId) {
                result.add(nodeInstance);
            }
        }
        return result;
    }

	public List<NodeInstance> getNodeInstances(final long nodeId, final List<NodeInstance> currentView) {
		List<NodeInstance> result = new ArrayList<>();
		for (final Iterator<NodeInstance> iterator = currentView.iterator(); iterator.hasNext();) {
			final NodeInstance nodeInstance = iterator.next();
			if (nodeInstance.getNodeId() == nodeId) {
				result.add(nodeInstance);
			}
		}
		return result;
	}

    @Override
    public NodeInstance getNodeInstance(final Node node) {
        return getNodeInstance(node, true);
    }

    public NodeInstance getNodeInstance(final Node node, boolean wrap) {
	    Node actualNode = node;
	    // async continuation handling
        if (node instanceof AsyncEventNode) {
            actualNode = ((AsyncEventNode) node).getActualNode();
        } else if (wrap && useAsync(node)) {
            actualNode = new AsyncEventNode(node);
        }


        NodeInstanceFactory conf = NodeInstanceFactoryRegistry.getInstance(getKnowledgeRuntime().getEnvironment()).getProcessNodeInstanceFactory(actualNode);
        if (conf == null) {
            throw new IllegalArgumentException("Illegal node type: "
                                                       + node.getClass());
        }
        NodeInstanceImpl nodeInstance = (NodeInstanceImpl) conf.getNodeInstance(actualNode, this, this);

        if (nodeInstance == null) {
            throw new IllegalArgumentException("Illegal node type: "
                                                       + node.getClass());
        }
        if (nodeInstance.isInversionOfControl()) {
            getKnowledgeRuntime().insert(nodeInstance);
        }
        return nodeInstance;
    }

	public long getNodeInstanceCounter() {
		return singleNodeInstanceCounter.get();
	}

	public void internalSetNodeInstanceCounter(long nodeInstanceCounter) {
	    this.singleNodeInstanceCounter = new AtomicLong(nodeInstanceCounter);
	}

	public AtomicLong internalGetNodeInstanceCounter() {
		return this.singleNodeInstanceCounter;
	}

    public WorkflowProcess getWorkflowProcess() {
        return (WorkflowProcess) getProcess();
    }

    @Override
    public Object getVariable(String name) {
        // for disconnected process instances, try going through the variable scope instances
        // (as the default variable scope cannot be retrieved as the link to the process could
        // be null and the associated working memory is no longer accessible)
        if (getKnowledgeRuntime() == null) {
            List<ContextInstance> variableScopeInstances =
                    getContextInstances(VariableScope.VARIABLE_SCOPE);
            if (variableScopeInstances != null && variableScopeInstances.size() == 1) {
                for (ContextInstance contextInstance : variableScopeInstances) {
                    Object value = ((VariableScopeInstance) contextInstance).getVariable(name);
                    if (value != null) {
                        return value;
                    }
                }
            }
            return null;
        }
        // else retrieve the variable scope
        VariableScopeInstance variableScopeInstance = (VariableScopeInstance)
                getContextInstance(VariableScope.VARIABLE_SCOPE);
        if (variableScopeInstance == null) {
            return null;
        }
        return variableScopeInstance.getVariable(name);
    }






    public Object getVariable(String name, List<ContextInstance> variableScopeInstances) {

        if (variableScopeInstances != null) {
            for (ContextInstance contextInstance : variableScopeInstances) {
                Object value = ((VariableScopeInstance) contextInstance).getVariable(name);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    public Map<String, Object> getVariables() {
        // for disconnected process instances, try going through the variable scope instances
        // (as the default variable scope cannot be retrieved as the link to the process could
        // be null and the associated working memory is no longer accessible)
        if (getKnowledgeRuntime() == null) {
            List<ContextInstance> variableScopeInstances =
                    getContextInstances(VariableScope.VARIABLE_SCOPE);
            if (variableScopeInstances == null) {
                return null;
            }
            Map<String, Object> result = new HashMap<>();
            for (ContextInstance contextInstance : variableScopeInstances) {
                Map<String, Object> variables =
                        ((VariableScopeInstance) contextInstance).getVariables();
                result.putAll(variables);
            }
            return result;
        }
        // else retrieve the variable scope
        VariableScopeInstance variableScopeInstance = (VariableScopeInstance)
                getContextInstance(VariableScope.VARIABLE_SCOPE);
        if (variableScopeInstance == null) {
            return null;
        }
        return variableScopeInstance.getVariables();
    }

    @Override
    public void setVariable(String name, Object value) {
        VariableScope variableScope = (VariableScope) ((ContextContainer) getProcess()).getDefaultContext(VariableScope.VARIABLE_SCOPE);
        VariableScopeInstance variableScopeInstance = (VariableScopeInstance)
                getContextInstance(VariableScope.VARIABLE_SCOPE);
        if (variableScopeInstance == null) {
            throw new IllegalArgumentException("No variable scope found.");
        }
        variableScopeInstance.setVariable(name, variableScope.validateVariable(getProcessName(), name, value));
    }

    @Override
    public void setState(final int state, String outcome, Object faultData) {
        this.faultData = faultData;
        setState(state, outcome);
    }

    @Override
    public void setState(final int state, String outcome) {
        if(getMetaData().containsKey("SUB_PROCESS_INTERRUPTION") || getState() == ProcessInstance.STATE_COMPLETED || getState() == ProcessInstance.STATE_ABORTED) {
            // avoid duplication calls
            return;
        }

        // TODO move most of this to ProcessInstanceImpl
        if (state == ProcessInstance.STATE_COMPLETED
                || state == ProcessInstance.STATE_ABORTED) {
            if (this.slaCompliance == SLA_PENDING) {
                if (System.currentTimeMillis() > slaDueDate.getTime()) {
                    // completion of the process instance is after expected SLA due date, mark it accordingly
                    this.slaCompliance = SLA_VIOLATED;
                } else {
                    this.slaCompliance = state == ProcessInstance.STATE_COMPLETED ? SLA_MET : SLA_ABORTED;
                }
            }

            InternalKnowledgeRuntime kruntime = getKnowledgeRuntime();
            InternalProcessRuntime processRuntime = (InternalProcessRuntime) kruntime.getProcessRuntime();
            processRuntime.getProcessEventSupport().fireBeforeProcessCompleted(this, kruntime);
            // JBPM-8094 - set state after event
            super.setState(state, outcome);

            // deactivate all node instances of this process instance
            while (!nodeInstances.isEmpty()) {
                NodeInstance nodeInstance = nodeInstances.get(0);
                if (state == STATE_COMPLETED) {
                    nodeInstance.cancel(OBSOLETE);
                } else {
                    nodeInstance.cancel();
                }
            }
            if (this.slaTimerId > -1) {
                processRuntime.getTimerManager().cancelTimer(this.getId(), this.slaTimerId);
                logger.debug("SLA Timer {} has been canceled", this.slaTimerId);
            }
            removeEventListeners();
            processRuntime.getProcessInstanceManager().removeProcessInstance(this);
            processRuntime.getProcessEventSupport().fireAfterProcessCompleted(this, kruntime);

            if (isSignalCompletion()) {
                RuntimeManager manager = (RuntimeManager) kruntime.getEnvironment().get(EnvironmentName.RUNTIME_MANAGER);
                if (getParentProcessInstanceId() > 0 && manager != null) {

                    org.kie.api.runtime.manager.Context<?> context = ProcessInstanceIdContext.get(getParentProcessInstanceId());
                    String caseId = (String) kruntime.getEnvironment().get(EnvironmentName.CASE_ID);
                    if (caseId != null) {
                        context = CaseContext.get(caseId);
                    }

                    RuntimeEngine runtime = null;
                    try {
                        runtime = manager.getRuntimeEngine(context);
                        KieRuntime managedkruntime = runtime.getKieSession();
                        managedkruntime.signalEvent("processInstanceCompleted:" + getId(), this);
                    } catch (SessionNotFoundException e) {
                        logger.debug("Could not found find parent process instance id {} for signaling completion", context.getContextId());
                    } finally {
                        if(runtime != null) {
                            manager.disposeRuntimeEngine(runtime);
                        }
                    }

                } else {
                    processRuntime.getSignalManager().signalEvent("processInstanceCompleted:" + getId(), this);
                }
            }
        } else {
            super.setState(state, outcome);
        }
    }

    @Override
    public void setState(final int state) {
        setState(state, null);
    }

    @Override
    public void disconnect() {
        removeEventListeners();
        unregisterExternalEventNodeListeners();

        for (NodeInstance nodeInstance : nodeInstances) {
            if (nodeInstance instanceof EventBasedNodeInstanceInterface) {
                ((EventBasedNodeInstanceInterface) nodeInstance).removeEventListeners();
            }
        }
        super.disconnect();
    }

    @Override
	public void reconnect() {
        validate();
	    super.reconnect();
		for (NodeInstance nodeInstance : nodeInstances) {
			if (nodeInstance instanceof EventBasedNodeInstanceInterface) {
				((EventBasedNodeInstanceInterface) nodeInstance)
						.addEventListeners();
			}
		}
		registerExternalEventNodeListeners();
	}

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WorkflowProcessInstance");
        sb.append(getId());
        sb.append(" [processId=");
        sb.append(getProcessId());
        sb.append(",state=");
        sb.append(getState());
        sb.append("]");
        return sb.toString();
    }

    @Override
    public void start() {
        start(null);
    }

    @Override
	public void start(String trigger) {
		synchronized (this) {
			this.startDate = new Date();
			registerExternalEventNodeListeners();
			// activate timer event sub processes
	        Node[] nodes = getNodeContainer().getNodes();
	        for (Node node : nodes) {
	            if (node instanceof EventSubProcessNode) {
	                Map<Timer, DroolsAction> timers = ((EventSubProcessNode) node).getTimers();
	                if (timers != null && !timers.isEmpty()) {
	                    EventSubProcessNodeInstance eventSubprocess = (EventSubProcessNodeInstance) getNodeInstance(node);
	                    eventSubprocess.trigger(null, org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE);
	                }
	            }
	        }
			super.start(trigger);
						
		}
	}



	@Override
    public void configureSLA() {
	    String slaDueDateExpression = (String) getProcess().getMetaData().get("customSLADueDate");
        if (slaDueDateExpression != null) {
            TimerInstance timer = configureSLATimer(slaDueDateExpression);
            if (timer != null) {
                this.slaTimerId = timer.getId();
                this.slaDueDate = new Date(System.currentTimeMillis() + timer.getDelay());
                this.slaCompliance = SLA_PENDING;
                logger.debug("SLA for process instance {} is PENDING with due date {}", this.getId(), this.slaDueDate);
            }
        }
    }
	public TimerInstance configureSLATimer(String slaDueDateExpression) {
	    return this.configureSLATimer(slaDueDateExpression, null);
	}
	
    public TimerInstance configureSLATimer(String slaDueDateExpression, String timerName) {
        return configureTimer(slaDueDateExpression, timerName, useTimerSLATracking());
    }

    public TimerInstance configureTimer(String timerExpression, String timerName, boolean trackTimer) {
        // setup SLA if provided
        String timerResolvedExpression = resolveVariable(timerExpression);
        if (timerResolvedExpression == null || timerResolvedExpression.trim().isEmpty()) {
            logger.debug("Timer due date expression resolved to no value '{}'", timerResolvedExpression);
            return null;
        }
        logger.debug("Configure timer {} due date is set to {}", timerName, timerResolvedExpression);
        InternalKnowledgeRuntime kruntime = getKnowledgeRuntime();
        long duration = -1;
        if (kruntime != null && kruntime.getEnvironment().get("jbpm.business.calendar") != null) {
            BusinessCalendar businessCalendar = (BusinessCalendar) kruntime.getEnvironment().get("jbpm.business.calendar");

            duration = businessCalendar.calculateBusinessTimeAsDuration(timerResolvedExpression);
        } else {
            duration = DateTimeUtils.parseDuration(timerResolvedExpression);
        }

        TimerInstance timerInstance = new TimerInstance();
        timerInstance.setId(-1);
        timerInstance.setDelay(duration);
        timerInstance.setPeriod(0);
        timerInstance.setName(timerName);

        if(trackTimer) {
            ((InternalProcessRuntime) kruntime.getProcessRuntime()).getTimerManager().registerTimer(timerInstance, this);
        }
        return timerInstance;
    }

    public void cancelTimer(long timerId) {
        ((InternalProcessRuntime) getKnowledgeRuntime().getProcessRuntime()).getTimerManager().cancelTimer(this.getId(), timerId);
    }

    protected void registerExternalEventNodeListeners() {
        for (Node node : getWorkflowProcess().getNodes()) {
            if (node instanceof EventNode && "external".equals(((EventNode) node).getScope())) {
                String eventType = ((EventNode) node).getType();
                addEventListener(eventType, EMPTY_EVENT_LISTENER, true);
                if (isVariableExpression(eventType)) {
                    addEventListener(resolveVariable(eventType), EMPTY_EVENT_LISTENER, true);
                }
            } else if (node instanceof EventSubProcessNode) {
                List<String> events = ((EventSubProcessNode) node).getEvents();
                for (String type : events) {
                    addEventListener(type, EMPTY_EVENT_LISTENER, true);
                    if (isVariableExpression(type)) {
                        addEventListener(resolveVariable(type), EMPTY_EVENT_LISTENER, true);
                    }
                }
            } else if (node instanceof DynamicNode && ((DynamicNode) node).getActivationEventName() != null) {
                addEventListener(((DynamicNode) node).getActivationEventName(), EMPTY_EVENT_LISTENER, true);
            }
        }
        if (getWorkflowProcess().getMetaData().containsKey("Compensation")) {
            addEventListener("Compensation", new CompensationEventListener(this), true);
        }
    }

    private void unregisterExternalEventNodeListeners() {
        for (Node node : getWorkflowProcess().getNodes()) {
            if (node instanceof EventNode && "external".equals(((EventNode) node).getScope())) {
                String eventType = ((EventNode) node).getType();
                removeEventListener(eventType, EMPTY_EVENT_LISTENER, true);
                if (isVariableExpression(eventType)) {
                    removeEventListener(resolveVariable(eventType), EMPTY_EVENT_LISTENER, true);
                }
            }
        }
    }

    private void handleSLAViolation() {
        if (slaCompliance == SLA_PENDING) {

            InternalKnowledgeRuntime kruntime = getKnowledgeRuntime();
            InternalProcessRuntime processRuntime = (InternalProcessRuntime) kruntime.getProcessRuntime();
            processRuntime.getProcessEventSupport().fireBeforeSLAViolated(this, kruntime);
            logger.debug("SLA violated on process instance {}", getId());
            this.slaCompliance = SLA_VIOLATED;
            this.slaTimerId = -1;
            processRuntime.getProcessEventSupport().fireAfterSLAViolated(this, kruntime);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void signalEvent(String type, Object event) {
        logger.debug("Signal {} received with data {} in process instance {}", type, event, getId());
        synchronized (this) {
            if (getState() != ProcessInstance.STATE_ACTIVE) {
                return;
            }

            if ("timerTriggered".equals(type)) {
                TimerInstance timer = (TimerInstance) event;
                if (timer.getId() == slaTimerId) {
                    handleSLAViolation();
                    // no need to pass the event along as it was purely for SLA tracking
                    return;
                }
            }
            if ("slaViolation".equals(type)) {
                handleSLAViolation();
                // no need to pass the event along as it was purely for SLA tracking
                return;
            }



            try {
                List<NodeInstance> currentView = getNodeInstances().stream().map(e -> (NodeInstance) e)
                        .collect(Collectors.toList());
                this.activatingNodeIds = new ArrayList<>();
                List<EventListener> listeners = eventListeners.get(type);
                if (listeners != null) {
                    for (EventListener listener : listeners) {
                        listener.signalEvent(type, event);
                    }
                }
                listeners = externalEventListeners.get(type);
                if (listeners != null) {
                    for (EventListener listener : listeners) {
                        listener.signalEvent(type, event);
                    }
                }

                signal(currentView, (node) -> this.getNodeInstance(node), () -> this.getWorkflowProcess().getNodes(),
                        type, event);

                if (((org.jbpm.workflow.core.WorkflowProcess) getWorkflowProcess()).isDynamic()) {
                    for (Node node : getWorkflowProcess().getNodes()) {
                        if (type.equals(node.getName()) && node.getIncomingConnections().isEmpty()) {
                            NodeInstance nodeInstance = getNodeInstance(node);
                            if (event != null) {
                                Map<String, Object> dynamicParams = new HashMap<>();
                                if (event instanceof Map) {
                                    dynamicParams.putAll((Map<String, Object>) event);
                                } else {
                                    dynamicParams.put("Data", event);
                                }
                                nodeInstance.setDynamicParameters(dynamicParams);
                            }

                            nodeInstance.trigger(null, org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE);
                        }
                    }
                }
            } finally {
                if (this.activatingNodeIds != null) {
                    this.activatingNodeIds.clear();
                    this.activatingNodeIds = null;
                }
            }
        }
    }

    private void signal(List<NodeInstance> currentView, Function<Node, NodeInstance> nodeInstanceSupplier,
            Supplier<Node[]> resolveNodes, String type, Object event) {
        for (Node node : resolveNodes.get()) {
            if (node instanceof EventNodeInterface
                    && ((EventNodeInterface) node).acceptsEvent(type, event, getEventFilterResolver(this, node, currentView))) {
                if (node instanceof EventNode && ((EventNode) node).getFrom() == null) {
                    EventNodeInstance eventNodeInstance = (EventNodeInstance) nodeInstanceSupplier.apply(node);
                    eventNodeInstance.signalEvent(type, event);
                } else {
                    if (node instanceof EventSubProcessNode && (resolveVariables(((EventSubProcessNode) node).getEvents()).contains(type))) {
                        EventSubProcessNodeInstance eventNodeInstance = (EventSubProcessNodeInstance) nodeInstanceSupplier.apply(node);
                        eventNodeInstance.signalEvent(type, event);
                    }
                    if (node instanceof DynamicNode && type.equals(((DynamicNode) node).getActivationEventName())) {
                        DynamicNodeInstance dynamicNodeInstance = (DynamicNodeInstance) nodeInstanceSupplier.apply(node);
                        dynamicNodeInstance.signalEvent(type, event);
                    } else {
                        List<NodeInstance> nodeInstances = getNodeInstances(node.getId(), currentView);
                        if (nodeInstances != null && !nodeInstances.isEmpty()) {
                            for (NodeInstance nodeInstance : nodeInstances) {
                                ((EventNodeInstanceInterface) nodeInstance).signalEvent(type, event);
                            }
                        }
                    }
                }
            }
        }

    }

    public Function<String, Object> getEventFilterResolver(NodeInstanceContainer container, Node node, List<NodeInstance> currentView) {
        if (node instanceof DynamicNode) {
            // special handling for dynamic node to allow to resolve variables from individual node instances of the dynamic node
            // instead of just relying on process instance's variables
            return (varExpresion) -> {
                List<NodeInstance> nodeInstances = getNodeInstances(node.getId(), currentView);
                if (nodeInstances != null && !nodeInstances.isEmpty()) {
                    StringBuilder st = new StringBuilder();
                    for (NodeInstance ni : nodeInstances) {
                        String result = resolveVariable(varExpresion, new NodeInstanceResolverFactory(ni));
                        st.append(result).append("###");
                    }
                    return st.toString();
                } else {
                    NodeInstanceImpl instance = (NodeInstanceImpl) getNodeInstance(node.getId(), true);
                    if (instance != null) {
                        return instance.getVariable(varExpresion);
                    }
                    return null;
                }
            };
        } else if(node instanceof BoundaryEventNode) { 
            return (varExpresion) -> {
                Function<String, Object> getScopedVariable;
                if(container instanceof CompositeContextNodeInstance) {
                    getScopedVariable = (name) ->  getVariable(name, ((CompositeContextNodeInstance) container).getContextInstances(VariableScope.VARIABLE_SCOPE));
                } else if (container instanceof WorkflowProcessInstanceImpl) {
                    getScopedVariable = (name) -> ((WorkflowProcessInstanceImpl) container).getVariable(name);
                } else {
                    getScopedVariable = null;
                }
                Object value = getScopedVariable.apply(varExpresion);
                if(value != null) {
                    return value;
                }

                VariableResolverFactory resolverFactory = new ImmutableDefaultFactory() {

                    @Override
                    public boolean isResolveable(String varName) {
                        return getScopedVariable.apply(varName) != null;
                    }

                    @Override
                    public VariableResolver getVariableResolver(String varName) {
                        return new SimpleValueResolver(getScopedVariable.apply(varName));
                    }
                };
                return resolveExpressionVariable(varExpresion, resolverFactory).orElse(null);
            };


        } else if (node instanceof ForEachNode) {
            return (varExpression) -> {
                try {
                    // for each can have multiple outcomes 1 per item of the list so it should be computed like that
                    List<Object> outcome = new ArrayList<>();
                    for (NodeInstance item : getNodeInstances(true)) {
                        if (item.getNodeId() == node.getId() && item instanceof ForEachNodeInstance) {
                            for (org.kie.api.runtime.process.NodeInstance nodeInstance : ((ForEachNodeInstance) item)
                                    .getNodeInstances()) {
                                if (nodeInstance instanceof CompositeContextNodeInstance) {
                                    resolveExpressionVariable(varExpression,
                                            new NodeInstanceResolverFactory(
                                                    (CompositeContextNodeInstance) nodeInstance))
                                            .ifPresent(outcome::add);
                                }
                            }
                        }
                    }
                    return outcome.toArray();
                } catch (Throwable t) {
                    return new Object[0];
                }
            };
        } else if (node instanceof EventSubProcessNode || node instanceof StateNode)  {
            return (varName) -> {
                return resolveExpressionVariable(varName, new ProcessInstanceResolverFactory(this)).orElse(null);
            };
        } else if (node instanceof CompositeContextNode) { 
            return (varExpression) -> {
                List<NodeInstance> nodeInstances = getNodeInstances(node.getId(), currentView);
                List<Object> outcome = new ArrayList<>();
                if (nodeInstances != null && !nodeInstances.isEmpty()) {
                    for(NodeInstance nodeInstance : nodeInstances) {
                        Object resolvedValue = resolveExpressionVariable(varExpression, new NodeInstanceResolverFactory(nodeInstance)).orElse(null);
                        if(resolvedValue != null) {
                            outcome.add(resolvedValue);
                        }
                    }
                }
                return outcome.toArray();
            };
        } else {
            return (varName) -> {
                return resolveExpressionVariable(varName, new ProcessInstanceResolverFactory(this)).orElse(null);
            };
        }
    }


    private void validate() {
	    InternalRuntimeManager manager = (InternalRuntimeManager) getKnowledgeRuntime().getEnvironment().get("RuntimeManager");
        if (manager != null) {
            // check if process instance is owned by the same manager as the one owning ksession
            if (hasDeploymentId() && !manager.getIdentifier().equals(getDeploymentId())) {
                throw new IllegalStateException("Process instance " + getId() + " is owned by another deployment " +
                        getDeploymentId() + " != " + manager.getIdentifier());
            }
        }
    }

    protected List<String> resolveVariables(List<String> events) {
        return events.stream().map(this::resolveVariable).collect(Collectors.toList());
    }

    private String resolveVariable(String s) {
        return resolveVariable(s, new ProcessInstanceResolverFactory(this));
    }

    private String resolveVariable(String s, VariableResolverFactory factory) {
        Map<String, Object> replacements = new HashMap<>();
        Matcher matcher = PatternConstants.PARAMETER_MATCHER.matcher(s);
        while (matcher.find()) {
            String paramName = matcher.group(1);
            if (replacements.get(paramName) == null) {
                Optional<Object> resolvedValue = resolveExpressionVariable(paramName, factory);
                replacements.put(paramName, resolvedValue.orElse(paramName));
            }
        }
        for (Map.Entry<String, Object> replacement : replacements.entrySet()) {
            s = s.replace("#{" + replacement.getKey() + "}", replacement.getValue().toString());
        }
        return s;
    }

    private Optional<Object> resolveExpressionVariable(String paramName, VariableResolverFactory factory) {
        try {
            // just in case is not an expression
            if(factory.isResolveable(paramName)) {
                return Optional.of(factory.getVariableResolver(paramName).getValue());
            }
            return Optional.ofNullable(MVELSafeHelper.getEvaluator().eval(paramName, factory));
        } catch (Throwable t) {
            logger.error("Could not find variable scope for variable {}", paramName);
            return Optional.empty();
        }
    }
    @Override
    public void addEventListener(String type, EventListener listener, boolean external) {
        Map<String, List<EventListener>> eventListeners = external ? this.externalEventListeners : this.eventListeners;
        List<EventListener> listeners = eventListeners.computeIfAbsent(type, listenerType -> {
            final List<EventListener> newListenersList = new CopyOnWriteArrayList<>();
            if (external) {
                ((InternalProcessRuntime) getKnowledgeRuntime().getProcessRuntime())
                        .getSignalManager().addEventListener(listenerType, this);
            }
            return newListenersList;
        });
        listeners.add(listener);
    }

    @Override
    public void removeEventListener(String type, EventListener listener, boolean external) {
        Map<String, List<EventListener>> eventListeners = external ? this.externalEventListeners : this.eventListeners;
        List<EventListener> listeners = eventListeners.get(type);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                eventListeners.remove(type);
                if (external) {
                    ((InternalProcessRuntime) getKnowledgeRuntime().getProcessRuntime())
                            .getSignalManager().removeEventListener(type, this);
                }
            }
        } else {
            eventListeners.remove(type);
        }
    }

    private void removeEventListeners() {
        for (String type : externalEventListeners.keySet()) {
            ((InternalProcessRuntime) getKnowledgeRuntime().getProcessRuntime())
                    .getSignalManager().removeEventListener(type, this);
        }
    }

    @Override
    public String[] getEventTypes() {
        return externalEventListeners.keySet().stream().map(this::resolveVariable).collect(Collectors.toList()).toArray(new String[externalEventListeners.size()]);
    }

    @Override
    public void nodeInstanceCompleted(NodeInstance nodeInstance, String outType) {
        Node nodeInstanceNode = nodeInstance.getNode();
        if (nodeInstanceNode != null) {
            Object compensationBoolObj = nodeInstanceNode.getMetaData().get("isForCompensation");
            boolean isForCompensation = compensationBoolObj != null && (Boolean) compensationBoolObj;
            if (isForCompensation) {
                return;
            }
        }
        if (nodeInstance instanceof FaultNodeInstance || nodeInstance instanceof EndNodeInstance ||
                ((org.jbpm.workflow.core.WorkflowProcess) getWorkflowProcess()).isDynamic()
                || nodeInstance instanceof CompositeNodeInstance) {
            if (((org.jbpm.workflow.core.WorkflowProcess) getProcess()).isAutoComplete() && canComplete()) {
                setState(ProcessInstance.STATE_COMPLETED);
            }
        } else {
            throw new IllegalArgumentException(
                    "Completing a node instance that has no outgoing connection is not supported.");
        }
    }

    private boolean canComplete() {
        if (nodeInstances.isEmpty()) {
            return true;
        } else {
            int eventSubprocessCounter = 0;
            for (NodeInstance nodeInstance : nodeInstances) {
                Node node = nodeInstance.getNode();
                if (node instanceof EventSubProcessNode) {
                    if (((EventSubProcessNodeInstance) nodeInstance).getNodeInstances().isEmpty()) {
                        eventSubprocessCounter++;
                    }
                } else {
                    return false;
                }
            }
            return eventSubprocessCounter == nodeInstances.size();
        }
    }

    public void addCompletedNodeId(String uniqueId) {
        this.completedNodeIds.add(uniqueId.intern());
    }

    public List<String> getCompletedNodeIds() {
        return new ArrayList<>(this.completedNodeIds);
    }

    @Override
    public int getCurrentLevel() {
        return currentLevel;
    }

    @Override
    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    public Map<String, Integer> getIterationLevels() {
        return iterationLevels;
    }

	public boolean isPersisted() {
		return persisted;
	}

	public void setPersisted(boolean persisted) {
		this.persisted = persisted;
	}

    public void addActivatingNodeId(String uniqueId) {
        if (this.activatingNodeIds == null) {
            return;
        }
        this.activatingNodeIds.add(uniqueId.intern());
    }

    public List<String> getActivatingNodeIds() {
        if (this.activatingNodeIds == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(this.activatingNodeIds);
    }

    @Override
    public Object getFaultData() {
        return faultData;
    }

    @Override
    public boolean isSignalCompletion() {
        return signalCompletion;
    }

    @Override
    public void setSignalCompletion(boolean signalCompletion) {
        this.signalCompletion = signalCompletion;
    }

    @Override
    public String getDeploymentId() {
        return deploymentId;
    }

    @Override
    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getCorrelationKey() {
        if (correlationKey == null && getMetaData().get("CorrelationKey") != null) {
            this.correlationKey = ((CorrelationKey) getMetaData().get("CorrelationKey")).toExternalForm();
        }
        return correlationKey;
    }

    public void setCorrelationKey(String correlationKey) {
        this.correlationKey = correlationKey;
    }

    @Override
    public Date getStartDate() {
        return startDate;
    }

    public void internalSetStartDate(Date startDate) {
        if (this.startDate == null) {
            this.startDate = startDate;
        }
    }

    protected boolean hasDeploymentId() {
        return this.deploymentId != null && !this.deploymentId.isEmpty();
    }

    protected boolean useAsync(final Node node) {
        if (!(node instanceof EventSubProcessNode) && (node instanceof ActionNode || node instanceof StateBasedNode || node instanceof EndNode)) {
            boolean asyncMode = Boolean.parseBoolean((String) node.getMetaData().get("customAsync"));
            if (asyncMode) {
                return asyncMode;
            }

            return Boolean.parseBoolean((String) getKnowledgeRuntime().getEnvironment().get("AsyncMode"));
        }

        return false;
    }

    protected boolean useTimerSLATracking() {

        String mode = (String) getKnowledgeRuntime().getEnvironment().get("SLATimerMode");
        if (mode == null) {
            return true;
        }

        return Boolean.parseBoolean(mode);
    }

    @Override
    public int getSlaCompliance() {
        return slaCompliance;
    }

    public void internalSetSlaCompliance(int slaCompliance) {
        this.slaCompliance = slaCompliance;
    }

    @Override
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

    private boolean isVariableExpression(String eventType) {
        if (eventType == null) {
            return false;
        }
        Matcher matcher = PatternConstants.PARAMETER_MATCHER.matcher(eventType);
        return matcher.find();
    }

    @Override
    public AgendaFilter getAgendaFilter() {
        return agendaFilter;
    }

    @Override
    public void setAgendaFilter( AgendaFilter agendaFilter ) {
        this.agendaFilter = agendaFilter;
    }
}
