/*
 * Copyright 2012 JBoss Inc
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
package org.jbpm.workflow.core.node;

import java.util.HashMap;
import java.util.Map;

import org.kie.definition.process.Node;

public class EventSubProcessNode extends CompositeContextNode {

    private static final long serialVersionUID = 2200928773922042238L;

    private Map<String, String> events = new HashMap<String, String>();
    private boolean keepActive = true;
    
    public void addEvent(String event, String type) {
        this.events.put(event, type);
    }
    
    public Map<String, String> getEvents() {
        return events;
    }

    public void setEvents(Map<String, String> events) {
        this.events = events;
    }

    public boolean isKeepActive() {
        return keepActive;
    }

    public void setKeepActive(boolean triggerOnActivation) {
        this.keepActive = triggerOnActivation;
    }
    
    public StartNode findStartNode() {
        for (Node node: getNodes()) {
            if (node instanceof StartNode) {
                StartNode startNode = (StartNode) node;                                    
                return startNode;                           
            }
        }
        return null;
    }
}
