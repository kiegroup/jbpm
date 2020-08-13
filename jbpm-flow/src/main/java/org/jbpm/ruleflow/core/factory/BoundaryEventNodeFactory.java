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

package org.jbpm.ruleflow.core.factory;

import java.util.function.UnaryOperator;

import org.jbpm.process.core.event.EventFilter;
import org.jbpm.process.core.event.EventTransformer;
import org.jbpm.process.core.event.EventTypeFilter;
import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.node.BoundaryEventNode;
import org.kie.api.fluent.BoundaryEventNodeBuilder;
import org.kie.api.fluent.Dialect;
import org.kie.api.fluent.NodeContainerBuilder;

public class BoundaryEventNodeFactory<T extends NodeContainerBuilder<T, ?>> extends NodeFactory<BoundaryEventNodeBuilder<T>, T> implements BoundaryEventNodeBuilder<T> {
    private String attachedToUniqueId;

    public BoundaryEventNodeFactory(T nodeContainerFactory, NodeContainer nodeContainer, long id) {
        super(nodeContainerFactory, nodeContainer, new BoundaryEventNode(), id);
    }

    @Override
    public BoundaryEventNodeFactory<T> attachedTo(long attachedToId) {
        attachedToUniqueId = (String) nodeContainer.getNode(attachedToId).getMetaData().get("UniqueId");
        getBoundaryEventNode().setAttachedToNodeId(attachedToUniqueId);
        getBoundaryEventNode().setMetaData("AttachedTo", attachedToUniqueId);
        return this;
    }

    protected BoundaryEventNode getBoundaryEventNode() {
        return(BoundaryEventNode) getNode();
    }


    @Override
    public BoundaryEventNodeFactory<T> variableName(String variableName) {
        getBoundaryEventNode().setVariableName(variableName);
        return this;
    }

    public BoundaryEventNodeFactory<T> eventFilter(EventFilter eventFilter) {
        getBoundaryEventNode().addEventFilter(eventFilter);
        return this;
    }

    @Override
    public BoundaryEventNodeFactory<T> eventType(String eventType) {
        EventTypeFilter filter = new EventTypeFilter();
        filter.setType(eventType);
        return eventFilter(filter);
    }

    @Override
    public BoundaryEventNodeFactory<T> eventType(String eventTypePrefix, String eventTypeSurffix) {
        if (attachedToUniqueId == null) {
            throw new IllegalStateException("attachedTo() must be called before");
        }
        EventTypeFilter filter = new EventTypeFilter();
        filter.setType(eventTypePrefix + "-" + attachedToUniqueId + "-" + eventTypeSurffix);
        return eventFilter(filter);
    }

    @Override
    public BoundaryEventNodeFactory<T> timeCycle(String timeCycle) {
        eventType("Timer", timeCycle);
        setMetadata("TimeCycle", timeCycle);
        return this;
    }

    public BoundaryEventNodeFactory<T> timeCycle(String timeCycle, String language) {
        eventType("Timer", timeCycle);
        setMetadata("TimeCycle", timeCycle);
        setMetadata("Language", language);
        return this;
    }

    @Override
    public BoundaryEventNodeFactory<T> timeDuration(String timeDuration) {
        eventType("Timer", timeDuration);
        setMetadata("TimeDuration", timeDuration);
        return this;
    }

    @Override
    public BoundaryEventNodeFactory<T> cancelActivity(boolean cancelActivity) {
        setMetadata("CancelActivity", cancelActivity);
        return this;
    }

    public BoundaryEventNodeFactory<T> eventTransformer(EventTransformer transformer) {
        getBoundaryEventNode().setEventTransformer(transformer);
        return this;
    }

    @Override
    public BoundaryEventNodeFactory<T> scope(String scope) {
        getBoundaryEventNode().setScope(scope);
        return this;
    }

    @Override
    public BoundaryEventNodeBuilder<T> timeCycle(String timeCycle, Dialect dialect) {
        return timeCycle(timeCycle, DialectConverter.fromDialect(dialect));
    }

    @Override
    public BoundaryEventNodeBuilder<T> eventTransformer(UnaryOperator<Object> function) {
        return eventTransformer((EventTransformer) function::apply);
    }
}
