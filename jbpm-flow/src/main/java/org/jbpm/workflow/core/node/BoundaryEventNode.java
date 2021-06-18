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
package org.jbpm.workflow.core.node;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.jbpm.process.core.event.EventFilter;
import org.kie.api.definition.process.NodeType;

public class BoundaryEventNode extends EventNode {

    private static final long serialVersionUID = 3448981074702415561L;
    
    private String attachedToNodeId;

    private List<DataAssociation> outMapping = new LinkedList<DataAssociation>();

    public BoundaryEventNode() {
        super(NodeType.BOUNDARY_EVENT);
    }

    public String getAttachedToNodeId() {
        return attachedToNodeId;
    }

    public void setAttachedToNodeId(String attachedToNodeId) {
        this.attachedToNodeId = attachedToNodeId;
    }  
   
    
    public void addOutAssociation(DataAssociation dataAssociation) {
        outMapping.add(dataAssociation);
    }

    public List<DataAssociation> getOutAssociations() {
        return Collections.unmodifiableList(outMapping);
    }

    @Override
    public boolean acceptsEvent(String type, Object event, Function<String, Object> resolver) {
        if (resolver == null) {
            return acceptsEvent(type, event);
        }

        boolean isCorrelated = false;
        for( EventFilter filter : getEventFilters() ) {
            isCorrelated |= filter.isCorrelated();
            if( filter.acceptsEvent(type, event, resolver) ) {
                return true;
            }
        }
        return !isCorrelated && super.acceptsEvent(type, event);
    }
}
