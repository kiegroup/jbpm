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
import org.kie.api.definition.process.Node;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.api.runtime.process.NodeInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestBpmn2ProcessEventListener implements ProcessEventListener {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static enum Mode {
       NODE_ID, NODE_NAME, NODE_UNIQUE_ID;
    }

    private Mode mode = Mode.NODE_ID;

    public void useNodeInstanceUniqueId() {
        this.mode = Mode.NODE_UNIQUE_ID;
    }

    public void useNodeName() {
        this.mode = Mode.NODE_NAME;
    }

    public void useNodeId() {
        this.mode = Mode.NODE_ID;
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
        logAndAdd("bvc-" + event.getVariableId() + ": [" + event.getOldValue() + "]->[" + event.getNewValue() + "]");
    }

    @Override
    public void afterVariableChanged(ProcessVariableChangedEvent event) {
        logAndAdd("avc-" + event.getVariableId());
    }

    @Override
    public void beforeNodeRemoved(ProcessNodeTriggeredEvent event) {
        logAndAdd("rem-", event.getNodeInstance());
    }

    public List<String> getEventHistory() {
        return eventHistory;
    }

    private void logAndAdd(String event, NodeInstance...instances ) {
        if( instances.length > 0 ) {
            Node node = instances[0].getNode();
            switch(mode) {
                case NODE_ID:
                    String id = ( node == null ? "?" : String.valueOf(node.getId()) );
                    event += id;
                    break;
                case NODE_UNIQUE_ID:
                    event += ((NodeInstanceImpl) instances[0]).getUniqueId();
                    break;
                case NODE_NAME:
                    String name = ( node == null ? "?" : node.getName() );
                    event += name;
                    break;
                default:
                        throw new IllegalArgumentException("Unknown mode: " + mode.toString());
            }
        }
        System.out.println( "> " + event );
        eventHistory.add(event);
    }
}
