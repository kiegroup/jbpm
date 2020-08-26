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
import org.jbpm.workflow.core.impl.ConnectionRef;
import org.jbpm.workflow.core.impl.ConstraintImpl;
import org.jbpm.workflow.core.node.Split;
import org.kie.api.fluent.Dialect;
import org.kie.api.fluent.NodeContainerBuilder;
import org.kie.api.fluent.SplitNodeBuilder;

/**
 *
 */
public class SplitFactory<T extends NodeContainerBuilder<T, ?>> extends NodeFactory<SplitNodeBuilder<T>, T> implements SplitNodeBuilder<T> {

    public SplitFactory(T nodeContainerFactory, NodeContainer nodeContainer, long id) {
        super(nodeContainerFactory, nodeContainer, new Split(), id);
    }

    
    protected Split getSplit() {
    	return (Split) getNode();
    }

    @Override
    public SplitFactory<T> type(int type) {
    	getSplit().setType(type);
        return this;
    }
    
    public SplitFactory<T> constraint(long toNodeId, String name, String type, String dialect, String constraint) {
    	return constraint(toNodeId, name, type, dialect, constraint, 0);
    }
    

    public SplitFactory<T> constraint(long toNodeId, String name, String type, String dialect, String constraint, int priority) {
        ConstraintImpl constraintImpl = new ConstraintImpl();
        constraintImpl.setName(name);
        constraintImpl.setType(type); 
        constraintImpl.setDialect(dialect);
        constraintImpl.setConstraint(constraint);
        constraintImpl.setPriority(priority);
        getSplit().addConstraint(
    		new ConnectionRef(toNodeId, Node.CONNECTION_DEFAULT_TYPE), constraintImpl);
        return this;
    }

    @Override
    public SplitNodeBuilder<T> constraint(long toNodeId, String name, String type, Dialect dialect, String constraint, int priority) {
        return constraint(toNodeId, name, type, DialectConverter.fromDialect(dialect), constraint, priority);
    }

}
