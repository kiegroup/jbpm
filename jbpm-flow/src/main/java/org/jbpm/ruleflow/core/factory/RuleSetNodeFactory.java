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

import org.jbpm.process.core.timer.Timer;
import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.impl.DroolsConsequenceAction;
import org.jbpm.workflow.core.node.RuleSetNode;
import org.kie.api.fluent.Dialect;
import org.kie.api.fluent.NodeContainerBuilder;
import org.kie.api.fluent.RuleSetNodeBuilder;

/**
 *
 */
public class RuleSetNodeFactory<T extends NodeContainerBuilder<T, ?>> extends NodeFactory<RuleSetNodeBuilder<T>, T> implements RuleSetNodeBuilder<T> {

    public RuleSetNodeFactory(T nodeContainerFactory, NodeContainer nodeContainer, long id) {
        super(nodeContainerFactory, nodeContainer, new RuleSetNode(), id);
    }
    
    protected RuleSetNode getRuleSetNode() {
    	return (RuleSetNode) getNode();
    }

    @Override
    public RuleSetNodeFactory<T> name(String name) {
        getNode().setName(name);
        return this;
    }

    @Override
    public RuleSetNodeFactory<T> ruleFlowGroup(String ruleFlowGroup) {
        getRuleSetNode().setRuleFlowGroup(ruleFlowGroup);
        return this;
    }
    
    public RuleSetNodeFactory<T> timer(String delay, String period, String dialect, String action) {
    	Timer timer = new Timer();
    	timer.setDelay(delay);
    	timer.setPeriod(period);
    	getRuleSetNode().addTimer(timer, new DroolsConsequenceAction(dialect, action));
    	return this;
    }

    @Override
    public RuleSetNodeBuilder<T> timer(String delay, String period, Dialect dialect, String action) {
        return timer(delay, period, DialectConverter.fromDialect(dialect), action);
    }

}
