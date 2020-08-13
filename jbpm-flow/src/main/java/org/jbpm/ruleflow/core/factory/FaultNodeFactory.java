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
import org.jbpm.workflow.core.node.FaultNode;
import org.kie.api.fluent.FaultNodeBuilder;
import org.kie.api.fluent.NodeContainerBuilder;

public class FaultNodeFactory<T extends NodeContainerBuilder<T, ?>> extends NodeFactory<FaultNodeBuilder<T>, T> implements FaultNodeBuilder<T> {

    public FaultNodeFactory(T nodeContainerFactory, NodeContainer nodeContainer, long id) {
        super(nodeContainerFactory, nodeContainer, new FaultNode(), id);
    }

    @Override
    public FaultNodeFactory<T> setFaultVariable(String faultVariable) {
        ((FaultNode) getNode()).setFaultVariable(faultVariable);
        return this;
    }

    @Override
    public FaultNodeFactory<T> setFaultName(String faultName) {
        ((FaultNode) getNode()).setFaultName(faultName);
        return this;
    }
}
