/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.test.util;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.jbpm.workflow.instance.node.EndNodeInstance;
import org.jbpm.workflow.instance.node.EventNodeInstance;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.api.runtime.process.NodeInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestProcessEventListener implements ProcessEventListener {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private boolean useNodeId = true;

    public void useNodeInstanceUniqueId() {
        this.useNodeId = false;
    }

    private List<String> eventHistory = new ArrayList<String>();

    @Override
    public void beforeProcessStarted(ProcessStartedEvent event) {
        logAndAdd("bps");
    }

    @Override
    public void afterProcessStarted(ProcessStartedEvent event) {
        logAndAdd("aps");
    }

    @Override
    public void beforeProcessCompleted(ProcessCompletedEvent event) {
        logAndAdd("bpc");
    }

    @Override
    public void afterProcessCompleted(ProcessCompletedEvent event) {
        logAndAdd("apc");
    }

    @Override
    public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
        logAndAdd("bnt-", event.getNodeInstance());
    }

    @Override
    public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
        logAndAdd("ant-", event.getNodeInstance());
    }

    @Override
    public void beforeNodeLeft(ProcessNodeLeftEvent event) {
        logAndAdd("bnl-", event.getNodeInstance());
    }

    @Override
    public void afterNodeLeft(ProcessNodeLeftEvent event) {
        String prefix = "anl-";
        StackTraceElement [] stackTrace = Thread.currentThread().getStackTrace();
        if( stackTrace[3].getMethodName().equals("cancel") ) {
            prefix = "cnl-";
        }
        logAndAdd(prefix, event.getNodeInstance());
    }

    @Override
    public void beforeVariableChanged(ProcessVariableChangedEvent event) {
        logAndAdd("bvc-" + event.getVariableId());
    }

    @Override
    public void afterVariableChanged(ProcessVariableChangedEvent event) {
        logAndAdd("avc-" + event.getVariableId());
    }

    public List<String> getEventHistory() {
        return eventHistory;
    }

    private void logAndAdd(String event, NodeInstance...instances ) {
        if( instances.length > 0 ) {
            if( useNodeId ) {
                event += instances[0].getNode().getId();
            } else {
                event += ((NodeInstanceImpl) instances[0]).getUniqueId();
            }
        }
        System.out.println( "> " + event );
        eventHistory.add(event);
    }
}
