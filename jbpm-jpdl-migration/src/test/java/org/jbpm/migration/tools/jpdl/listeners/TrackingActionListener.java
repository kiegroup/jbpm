/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.migration.tools.jpdl.listeners;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.exe.ExecutionContext;

/**
 * Tracking Listener. saves events and node connected with action
 */
public class TrackingActionListener {
    private List<Event> acceptedEvents = new ArrayList<Event>();
    private List<Node> firedNodes = new ArrayList<Node>();

    public void addNode(Node n) {
        if (n != null) {
            firedNodes.add(n);
        }
    }

    public void addEvent(Event e) {
        if (e != null) {
            acceptedEvents.add(e);
        }
    }

    /**
     * Adds information about both node and event from actual execution context.
     *
     * @param ctx
     *            execution context (should be passed from the action handler)
     */
    public void addFromContext(ExecutionContext ctx) {
        addEvent(ctx.getEvent());
        addNode(ctx.getNode());
    }

    /**
     * Tells whether was action called on the event.
     *
     * @param eventType
     *            type of the event
     */
    public boolean wasEventAccepted(String eventType) {
        for (Event e : getEvents()) {
            if (e.getEventType().equals(eventType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tells whether was action called on the event.
     *
     * @param event
     *            event instance
     */
    public boolean wasEventAccepted(Event event) {
        return getEvents().contains(event);
    }

    /**
     * Tells whether was action called on the given node.
     *
     * @param nodeName
     *            name of the node
     */
    public boolean wasCalledOnNode(String nodeName) {
        for (Node n : getNodes()) {
            if (n.getName().equals(nodeName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tells whether was action called on the given node.
     *
     * @param node
     *            node instance
     */
    public boolean wasCalledOnNode(Node node) {
        return getNodes().contains(node);
    }

    /**
     * @return all recorded events
     */
    public List<Event> getEvents() {
        return this.acceptedEvents;
    }

    /**
     * @return all recorded nodes
     */
    public List<Node> getNodes() {
        return this.firedNodes;
    }

    /**
     * clears both nodes and event recordings
     */
    public void clear() {
        getEvents().clear();
        getNodes().clear();
    }
}
