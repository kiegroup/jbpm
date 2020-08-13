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

import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.node.DynamicNode;
import org.kie.api.fluent.DynamicNodeBuilder;
import org.kie.api.fluent.NodeContainerBuilder;


public class DynamicNodeFactory<T extends NodeContainerBuilder<T, ?>> extends AbstractCompositeNodeFactory<DynamicNodeBuilder<T>, T> implements DynamicNodeBuilder<T> {

    public DynamicNodeFactory(T nodeContainerFactory, NodeContainer nodeContainer, long id) {
        super(nodeContainerFactory, nodeContainer, new DynamicNode(), id);
    }

    protected DynamicNode getDynamicNode() {
        return (DynamicNode) node;
    }

    
    @Override
    public DynamicNodeFactory<T> autoComplete(boolean autoComplete) {
    	getDynamicNode().setAutoComplete(autoComplete);
    	return this;
    }

}
