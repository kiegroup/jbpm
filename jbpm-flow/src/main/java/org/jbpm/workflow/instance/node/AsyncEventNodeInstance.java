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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.jbpm.process.core.async.AsyncSignalEventCommand;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.jbpm.workflow.core.node.AsyncEventNode;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.kie.api.definition.process.Node;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutorService;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.EventListener;
import org.kie.api.runtime.process.NodeInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runtime counterpart of an event node.
 *
 */
public class AsyncEventNodeInstance extends EventNodeInstance {
    
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(AsyncEventNodeInstance.class);
    
    private String eventType = UUID.randomUUID().toString();
    
    private EventListener listener = new AsyncExternalEventListener();

    public void internalTrigger(final NodeInstance from, String type) {

        super.internalTrigger(from, type);
    	
    	ExecutorService executorService = (ExecutorService) getProcessInstance().getKnowledgeRuntime().getEnvironment().get("ExecutorService");
    	if (executorService != null) {
    	    RuntimeManager runtimeManager = ((RuntimeManager)getProcessInstance().getKnowledgeRuntime().getEnvironment().get("RuntimeManager"));
    	    
    	    CommandContext ctx = new CommandContext();
            ctx.setData("deploymentId", runtimeManager.getIdentifier());
            ctx.setData("processInstanceId", getProcessInstance().getId());
            ctx.setData("Signal", getEventType());
            ctx.setData("Event", null);
            
            Long jobId = executorService.scheduleRequest(AsyncSignalEventCommand.class.getName(), ctx);
            
            Node node = getNode();
            if (node != null) {
                String uniqueId = (String) node.getMetaData().get("UniqueId");
                if( uniqueId == null ) { 
                    uniqueId = ((NodeImpl) node).getUniqueId();
                }
                ((WorkflowProcessInstanceImpl) getProcessInstance()).getIterationLevels().remove(getNode().getMetaData().get("UniqueId"));
            }
            Map<String, Object> data = new HashMap<>();
            data.put("jobId", jobId);
            data.put("nodeInstanceId", this.getId());
            InternalKnowledgeRuntime kruntime = getProcessInstance().getKnowledgeRuntime();
            ((InternalProcessRuntime) kruntime.getProcessRuntime()).getProcessEventSupport().fireOnAsyncNodeScheduledEvent(this, kruntime, data);
    	} else {
    	    logger.warn("No async executor service found continuing as sync operation...");
    	    // if there is no executor service available move as sync node
    	    triggerCompleted();
    	}
    }

    @Override
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
 
    @Override
    public Node getNode() {
        return new AsyncEventNode(super.getNode());
    }

    @Override
    protected NodeInstance getFrom() {
        AsyncEventNode node = (AsyncEventNode) getNode();
        int level = getLevel();
        org.jbpm.workflow.instance.NodeInstance instance = ((org.jbpm.workflow.instance.NodeInstanceContainer) getNodeInstanceContainer()).getNodeInstance(node.getPreviousNode());
        ((org.jbpm.workflow.instance.NodeInstanceContainer) getNodeInstanceContainer()).removeNodeInstance(instance);
        ((org.jbpm.workflow.instance.impl.NodeInstanceImpl) instance).setLevel(level);
        return instance;
    }

    @Override
    public void triggerCompleted() {
        getProcessInstance().removeEventListener(getEventType(), getEventListener(), true);
        ((org.jbpm.workflow.instance.NodeInstanceContainer)getNodeInstanceContainer()).setCurrentLevel(getLevel());
        ((org.jbpm.workflow.instance.NodeInstanceContainer) getNodeInstanceContainer()).removeNodeInstance(this);
        
        NodeInstance instance = ((org.jbpm.workflow.instance.NodeInstanceContainer) getNodeInstanceContainer()).getNodeInstance(getNode());
        // correction iteration levels of the composite
        if(getNodeInstanceContainer() instanceof CompositeNodeInstance) {
            CompositeNodeInstance composite = (CompositeNodeInstance) getNodeInstanceContainer();
            composite.getIterationLevels().put(getNode().getNodeUniqueId(), getLevel());
        } else if(getNodeInstanceContainer() instanceof WorkflowProcessInstanceImpl) {
            WorkflowProcessInstanceImpl composite = (WorkflowProcessInstanceImpl) getNodeInstanceContainer();
            composite.getIterationLevels().put(getNode().getNodeUniqueId(), getLevel());
        }
        ((NodeInstanceImpl) instance).setLevel(getLevel()); // we need to set the right level in this case for the instance


        triggerNodeInstance((org.jbpm.workflow.instance.NodeInstance) instance, NodeImpl.CONNECTION_DEFAULT_TYPE);
    }

    @Override
    protected EventListener getEventListener() {
        return this.listener;
    }

    private class AsyncExternalEventListener implements EventListener, Serializable {
        private static final long serialVersionUID = 5L;
        public String[] getEventTypes() {
            return null;
        }
        public void signalEvent(String type,
                Object event) {
            triggerCompleted();
        }       
    }
}
