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

import org.jbpm.process.core.datatype.DataType;
import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.node.ForEachNode;
import org.kie.api.fluent.ForEachNodeBuilder;
import org.kie.api.fluent.NodeContainerBuilder;

/**
 *
 */
public class ForEachNodeFactory<T extends NodeContainerBuilder<T, ?>> extends AbstractCompositeNodeFactory<ForEachNodeBuilder<T>, T> implements ForEachNodeBuilder<T> {

    public ForEachNodeFactory(T nodeContainerFactory, NodeContainer nodeContainer, long id) {
        super(nodeContainerFactory, nodeContainer, new ForEachNode(), id);
    }

    
    protected ForEachNode getForEachNode() {
        return (ForEachNode) node;
    }

    @Override
    public ForEachNodeFactory<T> collectionExpression(String collectionExpression) {
    	getForEachNode().setCollectionExpression(collectionExpression);
        return this;
    }

    @Override
    public ForEachNodeFactory<T> variable(String variableName, DataType dataType) {
    	getForEachNode().setVariable(variableName, dataType);
        return this;
    }

    @Override
    public ForEachNodeFactory<T> waitForCompletion(boolean waitForCompletion) {
    	getForEachNode().setWaitForCompletion(waitForCompletion);
        return this;
    }
}
