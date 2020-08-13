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

import java.util.ArrayList;
import java.util.List;

import org.jbpm.process.core.timer.Timer;
import org.jbpm.workflow.core.DroolsAction;
import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.impl.DroolsConsequenceAction;
import org.jbpm.workflow.core.node.MilestoneNode;
import org.kie.api.fluent.Dialect;
import org.kie.api.fluent.MilestoneNodeBuilder;
import org.kie.api.fluent.NodeContainerBuilder;

/**
 *
 */
public class MilestoneNodeFactory<T extends NodeContainerBuilder<T, ?>> extends NodeFactory<MilestoneNodeBuilder<T>, T> implements MilestoneNodeBuilder<T> {

    public MilestoneNodeFactory(T nodeContainerFactory, NodeContainer nodeContainer, long id) {
        super(nodeContainerFactory, nodeContainer, new MilestoneNode(), id);
    }


    protected MilestoneNode getMilestoneNode() {
        return (MilestoneNode) getNode();
    }


    public MilestoneNodeFactory<T> onEntryAction(String dialect, String action) {
        if (getMilestoneNode().getActions(dialect) != null) {
            getMilestoneNode().getActions(dialect).add(new DroolsConsequenceAction(dialect, action));
        } else {
            List<DroolsAction> actions = new ArrayList<DroolsAction>();
            actions.add(new DroolsConsequenceAction(dialect, action));
            getMilestoneNode().setActions(MilestoneNode.EVENT_NODE_ENTER, actions);
        }
        return this;
    }

    public MilestoneNodeFactory<T> onExitAction(String dialect, String action) {
        if (getMilestoneNode().getActions(dialect) != null) {
            getMilestoneNode().getActions(dialect).add(new DroolsConsequenceAction(dialect, action));
        } else {
            List<DroolsAction> actions = new ArrayList<DroolsAction>();
            actions.add(new DroolsConsequenceAction(dialect, action));
            getMilestoneNode().setActions(MilestoneNode.EVENT_NODE_EXIT, actions);
        }
        return this;
    }

    @Override
    public MilestoneNodeFactory<T> constraint(String constraint) {
        getMilestoneNode().setConstraint(constraint);
        return this;
    }

    public MilestoneNodeFactory<T> timer(String delay, String period, String dialect, String action) {
        Timer timer = new Timer();
        timer.setDelay(delay);
        timer.setPeriod(period);
        getMilestoneNode().addTimer(timer, new DroolsConsequenceAction(dialect, action));
        return this;
    }

    @Override
    public MilestoneNodeBuilder<T> onEntryAction(Dialect dialect, String action) {
        return onEntryAction(DialectConverter.fromDialect(dialect), action);
    }

    @Override
    public MilestoneNodeBuilder<T> onExitAction(Dialect dialect, String action) {
        return onExitAction(DialectConverter.fromDialect(dialect), action);
    }

    @Override
    public MilestoneNodeBuilder<T> timer(String delay, String period, Dialect dialect, String action) {
        return timer(delay, period, DialectConverter.fromDialect(dialect), action);
    }
    
}

