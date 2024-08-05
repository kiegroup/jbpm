/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.process.audit;

import org.drools.core.WorkingMemory;
import org.jbpm.process.instance.impl.ProcessInstanceImpl;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessDataChangedEvent;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessAsyncNodeScheduledEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.api.event.process.SLAViolatedEvent;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;

public abstract class AbstractAuditLoggerAdapter extends AbstractAuditLogger {

    public static final String METADATA_PROCESSINTANCE_LOG = "ProcessInstanceLog";
    public static final String METADATA_NODEINSTANCE_LOG = "NodeInstanceLog";
    public static final String METADATA_VARIABLEINSTANCE_LOG = "VariableInstanceLog";

    public AbstractAuditLoggerAdapter() {

    }
    public AbstractAuditLoggerAdapter(WorkingMemory workingMemory) {
        super(workingMemory);
    }

    public void setProcessInstanceMetadata(ProcessInstance pi, String key, Object value) {
        ((ProcessInstanceImpl) pi).getMetaData().put(key, value);
    }

    public Object getProcessInstanceMetadata(ProcessInstance pi, String key) {
        return ((ProcessInstanceImpl) pi).getMetaData().get(key);
    }

    public void setNodeInstanceMetadata(NodeInstance pi, String key, Object value) {
        ((NodeInstanceImpl) pi).getMetaData().put(key, value);
    }

    public Object getNodeInstanceMetadata(NodeInstance pi, String key) {
        return ((NodeInstanceImpl) pi).getMetaData().get(key);
    }

    @Override
    public void beforeProcessStarted(ProcessStartedEvent event) {
        processStarted(event);
    }

    protected abstract void processStarted(ProcessStartedEvent event);

    @Override
    public void onAsyncNodeScheduledEvent(ProcessAsyncNodeScheduledEvent event) {
        nodeScheduled(event);
    }

    @Override
    public void afterProcessStarted(ProcessStartedEvent event) {
        // nothing 

    }

    @Override
    public void beforeProcessCompleted(ProcessCompletedEvent event) {
        // nothing 

    }

    @Override
    public void afterProcessCompleted(ProcessCompletedEvent event) {
        processCompleted(event);
    }

    protected abstract void processCompleted(ProcessCompletedEvent event);

    @Override
    public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
        nodeEnter(event);
    }

    protected abstract void nodeScheduled(ProcessAsyncNodeScheduledEvent event);

    protected abstract void nodeEnter(ProcessNodeTriggeredEvent event);

    @Override
    public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
        // trigger this to record some of the data (like work item id) after activity was triggered
        NodeInstanceLog log = (NodeInstanceLog) ((NodeInstanceImpl) event.getNodeInstance()).getMetaData().get(METADATA_NODEINSTANCE_LOG);
        builder.buildEvent(event, log);

    }

    @Override
    public void beforeNodeLeft(ProcessNodeLeftEvent event) {
        nodeLeft(event);
    }

    protected abstract void nodeLeft(ProcessNodeLeftEvent event);

    @Override
    public void afterNodeLeft(ProcessNodeLeftEvent event) {
        // nothing 

    }

    @Override
    public void beforeVariableChanged(ProcessVariableChangedEvent event) {
        // nothing 

    }

    @Override
    public void afterVariableChanged(ProcessVariableChangedEvent event) {
        variableChanged(event);
    }

    protected abstract void variableChanged(ProcessVariableChangedEvent event);

    @Override
    public void afterSLAViolated(SLAViolatedEvent event) {
        if (event.getNodeInstance() != null) {
            slaNodeInstanceViolated(event);
        } else {
            slaProcessInstanceViolated(event);
        }
    }

    protected abstract void slaNodeInstanceViolated(SLAViolatedEvent event);

    protected abstract void slaProcessInstanceViolated(SLAViolatedEvent event);

    @Override
    public void onProcessDataChangedEvent(ProcessDataChangedEvent event){
        processDataChanged(event);
    }

    protected abstract void processDataChanged(ProcessDataChangedEvent event);
}
