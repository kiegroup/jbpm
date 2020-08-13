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

import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.NodeContainer;
import org.kie.api.fluent.NodeBuilder;
import org.kie.api.fluent.NodeContainerBuilder;

@SuppressWarnings("unchecked")
public abstract class NodeFactory<T extends NodeBuilder<T, P>, P extends NodeContainerBuilder<P, ?>> implements NodeBuilder<T, P> {

    protected final Object node;
    protected final NodeContainer nodeContainer;
    protected final P nodeContainerFactory;
    
    protected NodeFactory(P nodeContainerFactory, NodeContainer nodeContainer, Object node, Object id) {
        this.nodeContainerFactory = nodeContainerFactory;
        this.nodeContainer = nodeContainer;
        this.node = node;
        setId(node, id);
    }

    protected void setId(Object node, Object id) {
        ((Node) node).setId((long) id);
    }

    @Override
    public P done() {
        nodeContainer.addNode((Node) node);
        return this.nodeContainerFactory;
    }

    protected Node getNode() {
        return (Node) node;
    }

    @Override
    public T name(String name) {
        getNode().setName(name);
        return (T) this;
    }

    @Override
    public T setMetadata(String key, Object value) {
        getNode().setMetaData(key, value);
        return (T) this;
    }
}

    