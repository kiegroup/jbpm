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
import org.jbpm.workflow.core.node.EventNode;
import org.kie.api.fluent.EventNodeBuilder;
import org.kie.api.fluent.NodeContainerBuilder;


public class EventNodeFactory<T extends NodeContainerBuilder<T, ?>> extends NodeFactory<EventNodeBuilder<T>, T> implements EventNodeBuilder<T>
{
    public EventNodeFactory(T nodeContainerFactory, NodeContainer nodeContainer, long id) {
        super(nodeContainerFactory, nodeContainer, new EventNode(), id);
    }

    protected EventNode getEventNode() {
    	return(EventNode) getNode();
    }

    @Override
    public EventNodeFactory<T> name(String name) {
        getNode().setName(name);
        return this;
    }

    @Override
    public EventNodeFactory<T> variableName(String variableName) {
    	getEventNode().setVariableName(variableName);
        return this;
    }

    public EventNodeFactory<T> eventFilter(EventFilter eventFilter) {
    	getEventNode().addEventFilter(eventFilter);
        return this;
    }

    @Override
    public EventNodeFactory<T> eventType(String eventType) {
    	EventTypeFilter filter = new EventTypeFilter();
    	filter.setType(eventType);
    	return eventFilter(filter);
    }

    public EventNodeFactory<T> eventTransformer(EventTransformer transformer) {
    	getEventNode().setEventTransformer(transformer);
        return this;
    }

    @Override
    public EventNodeFactory<T> scope(String scope) {
    	getEventNode().setScope(scope);
        return this;
    }

    @Override
    public EventNodeFactory<T> eventTransformer(UnaryOperator<Object> function) {
        return eventTransformer((EventTransformer) function::apply);
    }
}
