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

package org.jbpm.marshalling.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.marshalling.impl.MarshallerReaderContext;
import org.drools.core.marshalling.impl.MarshallerWriteContext;
import org.drools.core.marshalling.impl.PersisterEnums;
import org.drools.core.util.StringUtils;
import org.jbpm.process.core.Context;
import org.jbpm.process.core.context.exclusive.ExclusiveGroup;
import org.jbpm.process.core.context.swimlane.SwimlaneContext;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.ContextInstance;
import org.jbpm.process.instance.context.exclusive.ExclusiveGroupInstance;
import org.jbpm.process.instance.context.swimlane.SwimlaneContextInstance;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.jbpm.workflow.instance.node.*;
import org.kie.api.definition.process.Process;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.NodeInstanceContainer;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;

/**
 * Default implementation of a process instance marshaller.
 * 
 */
public abstract class AbstractProcessInstanceMarshaller implements
        ProcessInstanceMarshaller {

    // Output methods
    public Object writeProcessInstance(MarshallerWriteContext context,
            ProcessInstance processInstance) throws IOException {        
        WorkflowProcessInstanceImpl workFlow = (WorkflowProcessInstanceImpl) processInstance;
        context.writeLong(workFlow.getId());
        context.writeUTF(workFlow.getProcessId());
        context.writeInt(workFlow.getState());
        context.writeLong(workFlow.getNodeInstanceCounter());

        SwimlaneContextInstance swimlaneContextInstance = (SwimlaneContextInstance) workFlow.getContextInstance(SwimlaneContext.SWIMLANE_SCOPE);
        if (swimlaneContextInstance != null) {
            Map<String, String> swimlaneActors = swimlaneContextInstance.getSwimlaneActors();
            context.writeInt(swimlaneActors.size());
            for (Map.Entry<String, String> entry : swimlaneActors.entrySet()) {
                context.writeUTF(entry.getKey());
                context.writeUTF(entry.getValue());
            }
        } else {
            context.writeInt(0);
        }
        
        List<NodeInstance> nodeInstances = new ArrayList<NodeInstance>(workFlow.getNodeInstances());
        Collections.sort(nodeInstances,
                new Comparator<NodeInstance>() {

                    public int compare(NodeInstance o1,
                            NodeInstance o2) {
                        return (int) (o1.getId() - o2.getId());
                    }
                });
        for (NodeInstance nodeInstance : nodeInstances) {
            context.writeShort(PersisterEnums.NODE_INSTANCE);
            writeNodeInstance(context,
                    nodeInstance);
        }
        context.writeShort(PersisterEnums.END);
        
        List<ContextInstance> exclusiveGroupInstances =
        	workFlow.getContextInstances(ExclusiveGroup.EXCLUSIVE_GROUP);
        if (exclusiveGroupInstances == null) {
            context.writeInt(0);
        } else {
            context.writeInt(exclusiveGroupInstances.size());
        	for (ContextInstance contextInstance: exclusiveGroupInstances) {
        		ExclusiveGroupInstance exclusiveGroupInstance = (ExclusiveGroupInstance) contextInstance;
        		Collection<NodeInstance> groupNodeInstances = exclusiveGroupInstance.getNodeInstances();
                context.writeInt(groupNodeInstances.size());
        		for (NodeInstance nodeInstance: groupNodeInstances) {
                    context.writeLong(nodeInstance.getId());
        		}
        	}
        }
        VariableScopeInstance variableScopeInstance = (VariableScopeInstance) workFlow.getContextInstance(VariableScope.VARIABLE_SCOPE);
        Map<String, Object> variables = variableScopeInstance.getVariables();
        List<String> keys = new ArrayList<String>(variables.keySet());
        Collection<Object> values = variables.values();
        
        Collections.sort(keys,
                new Comparator<String>() {

                    public int compare(String o1,
                            String o2) {
                        return o1.compareTo(o2);
                    }
                });
        // Process Variables
                // - Number of non null Variables = nonnullvariables.size()
                // For Each Variable
                    // - Variable Key
                    // - Marshalling Strategy Index
                    // - Marshalled Object
        
        Collection<Object> notNullValues = new ArrayList<Object>();
        for(Object value: values){
            if(value != null){
                notNullValues.add(value);
            }
        }

        context.writeInt(notNullValues.size());
        for (String key : keys) {
            Object object = variables.get(key); 
            if(object != null){
                context.writeUTF(key);
                // New marshalling algorithm when using strategies
                int useNewMarshallingStrategyAlgorithm = -2;
                context.writeInt(useNewMarshallingStrategyAlgorithm);
                // Choose first strategy that accepts the object (what was always done)
                ObjectMarshallingStrategy strategy = context.getObjectMarshallingStrategyStore().getStrategyObject(object);
                context.writeUTF(strategy.getClass().getName());
                strategy.write((ObjectOutputStream) context, object);
            }
            
        }    
        return null;

    }

    public Object writeNodeInstance(MarshallerWriteContext context,
            NodeInstance nodeInstance) throws IOException {
        context.writeLong(nodeInstance.getId());
        context.writeLong(nodeInstance.getNodeId());
        writeNodeInstanceContent(context, nodeInstance, context);
        return null;
    }

    protected void writeNodeInstanceContent(MarshallerWriteContext stream,
            NodeInstance nodeInstance, MarshallerWriteContext context)
            throws IOException {
        if (nodeInstance instanceof RuleSetNodeInstance) {
            stream.writeShort(PersisterEnums.RULE_SET_NODE_INSTANCE);
            List<Long> timerInstances =
                ((RuleSetNodeInstance) nodeInstance).getTimerInstances();
	        if (timerInstances != null) {
	            stream.writeInt(timerInstances.size());
	            for (Long id : timerInstances) {
	                stream.writeLong(id);
	            }
	        } else {
	            stream.writeInt(0);
	        }
        } else if (nodeInstance instanceof HumanTaskNodeInstance) {
            stream.writeShort(PersisterEnums.HUMAN_TASK_NODE_INSTANCE);
            stream.writeLong(((HumanTaskNodeInstance) nodeInstance).getWorkItemId());
            List<Long> timerInstances =
                ((HumanTaskNodeInstance) nodeInstance).getTimerInstances();
	        if (timerInstances != null) {
	            stream.writeInt(timerInstances.size());
	            for (Long id : timerInstances) {
	                stream.writeLong(id);
	            }
	        } else {
	            stream.writeInt(0);
	        }
        } else if (nodeInstance instanceof WorkItemNodeInstance) {
            stream.writeShort(PersisterEnums.WORK_ITEM_NODE_INSTANCE);
            stream.writeLong(((WorkItemNodeInstance) nodeInstance).getWorkItemId());
            List<Long> timerInstances =
                ((WorkItemNodeInstance) nodeInstance).getTimerInstances();
	        if (timerInstances != null) {
	            stream.writeInt(timerInstances.size());
	            for (Long id : timerInstances) {
	                stream.writeLong(id);
	            }
	        } else {
	            stream.writeInt(0);
	        }
        } else if (nodeInstance instanceof SubProcessNodeInstance) {
            stream.writeShort(PersisterEnums.SUB_PROCESS_NODE_INSTANCE);
            stream.writeLong(((SubProcessNodeInstance) nodeInstance).getProcessInstanceId());
            List<Long> timerInstances =
                ((SubProcessNodeInstance) nodeInstance).getTimerInstances();
	        if (timerInstances != null) {
	            stream.writeInt(timerInstances.size());
	            for (Long id : timerInstances) {
	                stream.writeLong(id);
	            }
	        } else {
	            stream.writeInt(0);
	        }
        } else if (nodeInstance instanceof MilestoneNodeInstance) {
            stream.writeShort(PersisterEnums.MILESTONE_NODE_INSTANCE);
            List<Long> timerInstances =
                    ((MilestoneNodeInstance) nodeInstance).getTimerInstances();
            if (timerInstances != null) {
                stream.writeInt(timerInstances.size());
                for (Long id : timerInstances) {
                    stream.writeLong(id);
                }
            } else {
                stream.writeInt(0);
            }
        } else if (nodeInstance instanceof EventNodeInstance) {
        	stream.writeShort(PersisterEnums.EVENT_NODE_INSTANCE);
    	} else if (nodeInstance instanceof TimerNodeInstance) {
            stream.writeShort(PersisterEnums.TIMER_NODE_INSTANCE);
            stream.writeLong(((TimerNodeInstance) nodeInstance).getTimerId());
        } else if (nodeInstance instanceof JoinInstance) {
            stream.writeShort(PersisterEnums.JOIN_NODE_INSTANCE);
            Map<Long, Integer> triggers = ((JoinInstance) nodeInstance).getTriggers();
            stream.writeInt(triggers.size());
            List<Long> keys = new ArrayList<Long>(triggers.keySet());
            Collections.sort(keys,
                    new Comparator<Long>() {

                        public int compare(Long o1,
                                Long o2) {
                            return o1.compareTo(o2);
                        }
                    });
            for (Long key : keys) {
                stream.writeLong(key);
                stream.writeInt(triggers.get(key));
            }
        } else if (nodeInstance instanceof StateNodeInstance) {
            stream.writeShort(PersisterEnums.STATE_NODE_INSTANCE);
            List<Long> timerInstances =
                    ((StateNodeInstance) nodeInstance).getTimerInstances();
            if (timerInstances != null) {
                stream.writeInt(timerInstances.size());
                for (Long id : timerInstances) {
                    stream.writeLong(id);
                }
            } else {
                stream.writeInt(0);
            }
        } else if (nodeInstance instanceof CompositeContextNodeInstance) {
        	if (nodeInstance instanceof DynamicNodeInstance) {
                stream.writeShort(PersisterEnums.DYNAMIC_NODE_INSTANCE);
        	} else {
        		stream.writeShort(PersisterEnums.COMPOSITE_NODE_INSTANCE);
        	}
            CompositeContextNodeInstance compositeNodeInstance = (CompositeContextNodeInstance) nodeInstance;
            List<Long> timerInstances =
                ((CompositeContextNodeInstance) nodeInstance).getTimerInstances();
            if (timerInstances != null) {
                stream.writeInt(timerInstances.size());
                for (Long id : timerInstances) {
                    stream.writeLong(id);
                }
            } else {
                stream.writeInt(0);
            }
            VariableScopeInstance variableScopeInstance = (VariableScopeInstance) compositeNodeInstance.getContextInstance(VariableScope.VARIABLE_SCOPE);
            if (variableScopeInstance == null) {
            	stream.writeInt(0);
            } else {
	            Map<String, Object> variables = variableScopeInstance.getVariables();
	            List<String> keys = new ArrayList<String>(variables.keySet());
	            Collections.sort(keys,
	                    new Comparator<String>() {
	                        public int compare(String o1,
	                                String o2) {
	                            return o1.compareTo(o2);
	                        }
	                    });
	            stream.writeInt(keys.size());
	            for (String key : keys) {
	                stream.writeUTF(key);
	                stream.writeObject(variables.get(key));
	            }
            }
            List<NodeInstance> nodeInstances = new ArrayList<NodeInstance>(compositeNodeInstance.getNodeInstances());
            Collections.sort(nodeInstances,
                    new Comparator<NodeInstance>() {

                        public int compare(NodeInstance o1,
                                NodeInstance o2) {
                            return (int) (o1.getId() - o2.getId());
                        }
                    });
            for (NodeInstance subNodeInstance : nodeInstances) {
                stream.writeShort(PersisterEnums.NODE_INSTANCE);
                writeNodeInstance(context,
                        subNodeInstance);
            }
            stream.writeShort(PersisterEnums.END);
            List<ContextInstance> exclusiveGroupInstances =
            	compositeNodeInstance.getContextInstances(ExclusiveGroup.EXCLUSIVE_GROUP);
            if (exclusiveGroupInstances == null) {
            	stream.writeInt(0);
            } else {
            	stream.writeInt(exclusiveGroupInstances.size());
            	for (ContextInstance contextInstance: exclusiveGroupInstances) {
            		ExclusiveGroupInstance exclusiveGroupInstance = (ExclusiveGroupInstance) contextInstance;
            		Collection<NodeInstance> groupNodeInstances = exclusiveGroupInstance.getNodeInstances();
            		stream.writeInt(groupNodeInstances.size());
            		for (NodeInstance groupNodeInstance: groupNodeInstances) {
            			stream.writeLong(groupNodeInstance.getId());
            		}
            	}
            }
        } else if (nodeInstance instanceof ForEachNodeInstance) {
            stream.writeShort(PersisterEnums.FOR_EACH_NODE_INSTANCE);
            ForEachNodeInstance forEachNodeInstance = (ForEachNodeInstance) nodeInstance;
            List<NodeInstance> nodeInstances = new ArrayList<NodeInstance>(forEachNodeInstance.getNodeInstances());
            Collections.sort(nodeInstances,
                    new Comparator<NodeInstance>() {
                        public int compare(NodeInstance o1,
                                NodeInstance o2) {
                            return (int) (o1.getId() - o2.getId());
                        }
                    });
            for (NodeInstance subNodeInstance : nodeInstances) {
                if (subNodeInstance instanceof CompositeContextNodeInstance) {
                    stream.writeShort(PersisterEnums.NODE_INSTANCE);
                    writeNodeInstance(context,
                            subNodeInstance);
                }
            }
            stream.writeShort(PersisterEnums.END);
        } else {
            throw new IllegalArgumentException("Unknown node instance type: " + nodeInstance);
        }
    }

    // Input methods
    public ProcessInstance readProcessInstance(MarshallerReaderContext context) throws IOException {
        InternalKnowledgeBase kBase = context.getKnowledgeBase();
        InternalWorkingMemory wm = context.getWorkingMemory();

        WorkflowProcessInstanceImpl processInstance = createProcessInstance();
        processInstance.setId(context.readLong());
        String processId = context.readUTF();
        processInstance.setProcessId(processId);
        Process process = kBase.getProcess(processId);
        if (kBase != null) {
            processInstance.setProcess(process);
        }
        processInstance.setState(context.readInt());
        long nodeInstanceCounter = context.readLong();
        processInstance.setKnowledgeRuntime(wm.getKnowledgeRuntime());

        int nbSwimlanes = context.readInt();
        if (nbSwimlanes > 0) {
            Context swimlaneContext = ((org.jbpm.process.core.Process) process).getDefaultContext(SwimlaneContext.SWIMLANE_SCOPE);
            SwimlaneContextInstance swimlaneContextInstance = (SwimlaneContextInstance) processInstance.getContextInstance(swimlaneContext);
            for (int i = 0; i < nbSwimlanes; i++) {
                String name = context.readUTF();
                String value = context.readUTF();
                swimlaneContextInstance.setActorId(name, value);
            }
        }

        while (context.readShort() == PersisterEnums.NODE_INSTANCE) {
            readNodeInstance(context, processInstance, processInstance);
        }

        int exclusiveGroupInstances = context.readInt();
    	for (int i = 0; i < exclusiveGroupInstances; i++) {
            ExclusiveGroupInstance exclusiveGroupInstance = new ExclusiveGroupInstance();
            processInstance.addContextInstance(ExclusiveGroup.EXCLUSIVE_GROUP, exclusiveGroupInstance);
            int nodeInstances = context.readInt();
            for (int j = 0; j < nodeInstances; j++) {
                long nodeInstanceId = context.readLong();
                NodeInstance nodeInstance = processInstance.getNodeInstance(nodeInstanceId);
                if (nodeInstance == null) {
                	throw new IllegalArgumentException("Could not find node instance when deserializing exclusive group instance: " + nodeInstanceId);
                }
                exclusiveGroupInstance.addNodeInstance(nodeInstance);
            }
    	}

        // Process Variables
        // - Number of Variables = keys.size()
        // For Each Variable
            // - Variable Key
            // - Marshalling Strategy Index
            // - Marshalled Object
		int nbVariables = context.readInt();
		if (nbVariables > 0) {
			Context variableScope = ((org.jbpm.process.core.Process) process)
					.getDefaultContext(VariableScope.VARIABLE_SCOPE);
			VariableScopeInstance variableScopeInstance = (VariableScopeInstance) processInstance
					.getContextInstance(variableScope);
			for (int i = 0; i < nbVariables; i++) {
				String name = context.readUTF();
				try {
			        ObjectMarshallingStrategy strategy = null;
					int index = context.readInt();
			        // This is the old way of de/serializing strategy objects
			        if ( index >= 0 ) {
			            strategy = context.getResolverStrategyFactory().getStrategy( index );
			        }
			        // This is the new way 
			        else if( index == -2 ) { 
			            String strategyClassName = context.readUTF();
			            if ( ! StringUtils.isEmpty(strategyClassName) ) { 
			                strategy = context.getResolverStrategyFactory().getStrategyObject(strategyClassName);
			                if( strategy == null ) { 
			                    throw new IllegalStateException( "No strategy of type " + strategyClassName + " available." );
			                }
			            }
			        }
			        // If either way retrieves a strategy, use it
			        Object value = null;
			        if( strategy != null ) {
			            value = strategy.read( (ObjectInputStream) context );
			        }
					variableScopeInstance.internalSetVariable(name, value);
				} catch (ClassNotFoundException e) {
					throw new IllegalArgumentException(
							"Could not reload variable " + name);
				}
			}
		}
        processInstance.internalSetNodeInstanceCounter(nodeInstanceCounter);
        if (wm != null) {
            processInstance.reconnect();
        }
        return processInstance;
    }

    protected abstract WorkflowProcessInstanceImpl createProcessInstance();

    public NodeInstance readNodeInstance(MarshallerReaderContext context,
            NodeInstanceContainer nodeInstanceContainer,
            WorkflowProcessInstance processInstance) throws IOException {
        long id = context.readLong();
        long nodeId = context.readLong();
        int nodeType = context.readShort();
        NodeInstanceImpl nodeInstance = readNodeInstanceContent(nodeType, context, processInstance);

        nodeInstance.setNodeId(nodeId);
        nodeInstance.setNodeInstanceContainer(nodeInstanceContainer);
        nodeInstance.setProcessInstance((org.jbpm.workflow.instance.WorkflowProcessInstance) processInstance);
        nodeInstance.setId(id);

        switch (nodeType) {
            case PersisterEnums.COMPOSITE_NODE_INSTANCE:
            case PersisterEnums.DYNAMIC_NODE_INSTANCE:
                int nbVariables = context.readInt();
                if (nbVariables > 0) {
                    Context variableScope = ((org.jbpm.process.core.Process) ((org.jbpm.process.instance.ProcessInstance)
                		processInstance).getProcess()).getDefaultContext(VariableScope.VARIABLE_SCOPE);
                    VariableScopeInstance variableScopeInstance = (VariableScopeInstance) ((CompositeContextNodeInstance) nodeInstance).getContextInstance(variableScope);
                    for (int i = 0; i < nbVariables; i++) {
                        String name = context.readUTF();
                        try {
                            Object value = context.readObject();
                            variableScopeInstance.internalSetVariable(name, value);
                        } catch (ClassNotFoundException e) {
                            throw new IllegalArgumentException("Could not reload variable " + name);
                        }
                    }
                }
                while (context.readShort() == PersisterEnums.NODE_INSTANCE) {
                    readNodeInstance(context,
                            (CompositeContextNodeInstance) nodeInstance,
                            processInstance);
                }
                
                int exclusiveGroupInstances = context.readInt();
            	for (int i = 0; i < exclusiveGroupInstances; i++) {
                    ExclusiveGroupInstance exclusiveGroupInstance = new ExclusiveGroupInstance();
                    ((org.jbpm.process.instance.ProcessInstance) processInstance).addContextInstance(ExclusiveGroup.EXCLUSIVE_GROUP, exclusiveGroupInstance);
                    int nodeInstances = context.readInt();
                    for (int j = 0; j < nodeInstances; j++) {
                        long nodeInstanceId = context.readLong();
                        NodeInstance groupNodeInstance = processInstance.getNodeInstance(nodeInstanceId);
                        if (groupNodeInstance == null) {
                        	throw new IllegalArgumentException("Could not find node instance when deserializing exclusive group instance: " + nodeInstanceId);
                        }
                        exclusiveGroupInstance.addNodeInstance(groupNodeInstance);
                    }
            	}

                break;
            case PersisterEnums.FOR_EACH_NODE_INSTANCE:
                while (context.readShort() == PersisterEnums.NODE_INSTANCE) {
                    readNodeInstance(context,
                            (ForEachNodeInstance) nodeInstance,
                            processInstance);
                }
                break;
            default:
            // do nothing
        }

        return nodeInstance;
    }

    protected NodeInstanceImpl readNodeInstanceContent(int nodeType, MarshallerReaderContext context, WorkflowProcessInstance processInstance) throws IOException {
        NodeInstanceImpl nodeInstance = null;
        switch (nodeType) {
            case PersisterEnums.RULE_SET_NODE_INSTANCE:
                nodeInstance = new RuleSetNodeInstance();
                int nbTimerInstances = context.readInt();
                if (nbTimerInstances > 0) {
                    List<Long> timerInstances = new ArrayList<Long>();
                    for (int i = 0; i < nbTimerInstances; i++) {
                        timerInstances.add(context.readLong());
                    }
                    ((RuleSetNodeInstance) nodeInstance).internalSetTimerInstances(timerInstances);
                }
                break;
            case PersisterEnums.HUMAN_TASK_NODE_INSTANCE:
                nodeInstance = new HumanTaskNodeInstance();
                ((HumanTaskNodeInstance) nodeInstance).internalSetWorkItemId(context.readLong());
                nbTimerInstances = context.readInt();
                if (nbTimerInstances > 0) {
                    List<Long> timerInstances = new ArrayList<Long>();
                    for (int i = 0; i < nbTimerInstances; i++) {
                        timerInstances.add(context.readLong());
                    }
                    ((HumanTaskNodeInstance) nodeInstance).internalSetTimerInstances(timerInstances);
                }
                break;
            case PersisterEnums.WORK_ITEM_NODE_INSTANCE:
                nodeInstance = new WorkItemNodeInstance();
                ((WorkItemNodeInstance) nodeInstance).internalSetWorkItemId(context.readLong());
                nbTimerInstances = context.readInt();
                if (nbTimerInstances > 0) {
                    List<Long> timerInstances = new ArrayList<Long>();
                    for (int i = 0; i < nbTimerInstances; i++) {
                        timerInstances.add(context.readLong());
                    }
                    ((WorkItemNodeInstance) nodeInstance).internalSetTimerInstances(timerInstances);
                }
                break;
            case PersisterEnums.SUB_PROCESS_NODE_INSTANCE:
                nodeInstance = new SubProcessNodeInstance();
                ((SubProcessNodeInstance) nodeInstance).internalSetProcessInstanceId(context.readLong());
                nbTimerInstances = context.readInt();
                if (nbTimerInstances > 0) {
                    List<Long> timerInstances = new ArrayList<Long>();
                    for (int i = 0; i < nbTimerInstances; i++) {
                        timerInstances.add(context.readLong());
                    }
                    ((SubProcessNodeInstance) nodeInstance).internalSetTimerInstances(timerInstances);
                }
                break;
            case PersisterEnums.MILESTONE_NODE_INSTANCE:
                nodeInstance = new MilestoneNodeInstance();
                nbTimerInstances = context.readInt();
                if (nbTimerInstances > 0) {
                    List<Long> timerInstances = new ArrayList<Long>();
                    for (int i = 0; i < nbTimerInstances; i++) {
                        timerInstances.add(context.readLong());
                    }
                    ((MilestoneNodeInstance) nodeInstance).internalSetTimerInstances(timerInstances);
                }
                break;
            case PersisterEnums.TIMER_NODE_INSTANCE:
                nodeInstance = new TimerNodeInstance();
                ((TimerNodeInstance) nodeInstance).internalSetTimerId(context.readLong());
                break;
            case PersisterEnums.EVENT_NODE_INSTANCE:
                nodeInstance = new EventNodeInstance();
                break;
            case PersisterEnums.JOIN_NODE_INSTANCE:
                nodeInstance = new JoinInstance();
                int number = context.readInt();
                if (number > 0) {
                    Map<Long, Integer> triggers = new HashMap<Long, Integer>();
                    for (int i = 0; i < number; i++) {
                        long l = context.readLong();
                        int count = context.readInt();
                        triggers.put(l,
                                count);
                    }
                    ((JoinInstance) nodeInstance).internalSetTriggers(triggers);
                }
                break;
            case PersisterEnums.COMPOSITE_NODE_INSTANCE:
                nodeInstance = new CompositeContextNodeInstance();
                nbTimerInstances = context.readInt();
                if (nbTimerInstances > 0) {
                    List<Long> timerInstances = new ArrayList<Long>();
                    for (int i = 0; i < nbTimerInstances; i++) {
                        timerInstances.add(context.readLong());
                    }
                    ((CompositeContextNodeInstance) nodeInstance).internalSetTimerInstances(timerInstances);
                }
                break;
            case PersisterEnums.FOR_EACH_NODE_INSTANCE:
                nodeInstance = new ForEachNodeInstance();
                nbTimerInstances = context.readInt();
                if (nbTimerInstances > 0) {
                    List<Long> timerInstances = new ArrayList<Long>();
                    for (int i = 0; i < nbTimerInstances; i++) {
                        timerInstances.add(context.readLong());
                    }
                    ((CompositeContextNodeInstance) nodeInstance).internalSetTimerInstances(timerInstances);
                }
                break;
            case PersisterEnums.DYNAMIC_NODE_INSTANCE:
                nodeInstance = new DynamicNodeInstance();
                nbTimerInstances = context.readInt();
                if (nbTimerInstances > 0) {
                    List<Long> timerInstances = new ArrayList<Long>();
                    for (int i = 0; i < nbTimerInstances; i++) {
                        timerInstances.add(context.readLong());
                    }
                    ((CompositeContextNodeInstance) nodeInstance).internalSetTimerInstances(timerInstances);
                }
                break;
            case PersisterEnums.STATE_NODE_INSTANCE:
                nodeInstance = new StateNodeInstance();
                nbTimerInstances = context.readInt();
                if (nbTimerInstances > 0) {
                    List<Long> timerInstances = new ArrayList<Long>();
                    for (int i = 0; i < nbTimerInstances; i++) {
                        timerInstances.add(context.readLong());
                    }
                    ((CompositeContextNodeInstance) nodeInstance).internalSetTimerInstances(timerInstances);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown node type: " + nodeType);
        }
        return nodeInstance;

    }
}
