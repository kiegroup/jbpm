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

package org.jbpm.ruleflow.core;

import java.util.Map;

import org.jbpm.process.core.Context;
import org.jbpm.process.core.ContextContainer;
import org.jbpm.process.core.context.exception.ActionExceptionHandler;
import org.jbpm.process.core.context.exception.ExceptionHandler;
import org.jbpm.process.core.context.exception.ExceptionScope;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.ruleflow.core.factory.ActionNodeFactory;
import org.jbpm.ruleflow.core.factory.BoundaryEventNodeFactory;
import org.jbpm.ruleflow.core.factory.CompositeNodeFactory;
import org.jbpm.ruleflow.core.factory.DialectConverter;
import org.jbpm.ruleflow.core.factory.DynamicNodeFactory;
import org.jbpm.ruleflow.core.factory.EndNodeFactory;
import org.jbpm.ruleflow.core.factory.EventNodeFactory;
import org.jbpm.ruleflow.core.factory.FaultNodeFactory;
import org.jbpm.ruleflow.core.factory.ForEachNodeFactory;
import org.jbpm.ruleflow.core.factory.HumanTaskNodeFactory;
import org.jbpm.ruleflow.core.factory.JoinFactory;
import org.jbpm.ruleflow.core.factory.MilestoneNodeFactory;
import org.jbpm.ruleflow.core.factory.NodeFactory;
import org.jbpm.ruleflow.core.factory.RuleSetNodeFactory;
import org.jbpm.ruleflow.core.factory.SplitFactory;
import org.jbpm.ruleflow.core.factory.StartNodeFactory;
import org.jbpm.ruleflow.core.factory.SubProcessNodeFactory;
import org.jbpm.ruleflow.core.factory.TimerNodeFactory;
import org.jbpm.ruleflow.core.factory.TypeConverter;
import org.jbpm.ruleflow.core.factory.WorkItemNodeFactory;
import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.impl.ConnectionImpl;
import org.jbpm.workflow.core.impl.DroolsConsequenceAction;
import org.kie.api.definition.process.Node;
import org.kie.api.fluent.Dialect;
import org.kie.api.fluent.NodeContainerBuilder;
import org.kie.api.fluent.Variable;

@SuppressWarnings("unchecked")
public abstract class RuleFlowNodeContainerFactory<T extends NodeContainerBuilder<T, P>, P extends NodeContainerBuilder<P, ?>> extends NodeFactory<T, P> implements NodeContainerBuilder<T, P> {


    protected RuleFlowNodeContainerFactory(P nodeContainerFactory, NodeContainer nodeContainer, NodeContainer node, Object id) {
        super(nodeContainerFactory, nodeContainer, node, id);
    }

    @Override
    public StartNodeFactory<T> startNode(long id) {
        return new StartNodeFactory<>((T) this, (NodeContainer) node, id);
    }

    @Override
    public EndNodeFactory<T> endNode(long id) {
        return new EndNodeFactory<>((T) this, (NodeContainer) node, id);
    }

    @Override
    public ActionNodeFactory<T> actionNode(long id) {
        return new ActionNodeFactory<>((T) this, (NodeContainer) node, id);
    }

    @Override
    public MilestoneNodeFactory<T> milestoneNode(long id) {
        return new MilestoneNodeFactory<>((T) this, (NodeContainer) node, id);
    }

    @Override
    public TimerNodeFactory<T> timerNode(long id) {
        return new TimerNodeFactory<>((T) this, (NodeContainer) node, id);
    }

    @Override
    public HumanTaskNodeFactory<T> humanTaskNode(long id) {
        return new HumanTaskNodeFactory<>((T) this, (NodeContainer) node, id);
    }

    @Override
    public SubProcessNodeFactory<T> subProcessNode(long id) {
        return new SubProcessNodeFactory<>((T) this, (NodeContainer) node, id);
    }

    @Override
    public SplitFactory<T> splitNode(long id) {
        return new SplitFactory<>((T) this, (NodeContainer) node, id);
    }

    @Override
    public JoinFactory<T> joinNode(long id) {
        return new JoinFactory<>((T) this, (NodeContainer) node, id);
    }

    @Override
    public RuleSetNodeFactory<T> ruleSetNode(long id) {
        return new RuleSetNodeFactory<>((T) this, (NodeContainer) node, id);
    }

    @Override
    public FaultNodeFactory<T> faultNode(long id) {
        return new FaultNodeFactory<>((T) this, (NodeContainer) node, id);
    }

    @Override
    public EventNodeFactory<T> eventNode(long id) {
        return new EventNodeFactory<>((T) this, (NodeContainer) node, id);
    }

    @Override
    public BoundaryEventNodeFactory<T> boundaryEventNode(long id) {
        return new BoundaryEventNodeFactory<>((T) this, (NodeContainer) node, id);
    }

    @Override
    public CompositeNodeFactory<T> compositeNode(long id) {
        return new CompositeNodeFactory<>((T) this, (NodeContainer) node, id);
    }

    @Override
    public ForEachNodeFactory<T> forEachNode(long id) {
        return new ForEachNodeFactory<>((T) this, (NodeContainer) node, id);
    }
    
    @Override
    public DynamicNodeFactory<T> dynamicNode(long id) {
        return new DynamicNodeFactory<>((T) this, (NodeContainer) node, id);
    }
    
    @Override
    public WorkItemNodeFactory<T> workItemNode(long id) {
        return new WorkItemNodeFactory<>((T) this, (NodeContainer) node, id);
    }

    @Override
    public T connection(long fromId, long toId) {
        Node from = ((NodeContainer) node).getNode(fromId);
        Node to = ((NodeContainer) node).getNode(toId);
        new ConnectionImpl(
            from, org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE,
            to, org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE);
        return (T) this;
    }
    
    @Override
    public T exceptionHandler(Class<? extends Throwable> exceptionClass, Dialect dialect, String code) {
        return (T) exceptionHandler(exceptionClass.getName(), DialectConverter.fromDialect(dialect), code);
    }

    public RuleFlowNodeContainerFactory<T, P> exceptionHandler(String exception, ExceptionHandler exceptionHandler) {
        getScope(ExceptionScope.EXCEPTION_SCOPE, ExceptionScope.class).setExceptionHandler(exception, exceptionHandler);
        return this;
    }

    public RuleFlowNodeContainerFactory<T, P> exceptionHandler(String exception, String dialect, String action) {
        ActionExceptionHandler exceptionHandler = new ActionExceptionHandler();
        exceptionHandler.setAction(new DroolsConsequenceAction(dialect, action));
        return exceptionHandler(exception, exceptionHandler);
    }

    private <S extends Context> S getScope(String scopeType, Class<S> scopeClass) {
        ContextContainer contextContainer = (ContextContainer) node;
        Context scope = contextContainer.getDefaultContext(scopeType);
        if (scope == null) {
            try {
                scope = scopeClass.newInstance();
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException(ex);
            }
            contextContainer.addContext(scope);
            contextContainer.setDefaultContext(scope);
        }
        return scopeClass.cast(scope);
    }

    @Override
    public <V> T variable(Variable<V> variable) {
        getScope(VariableScope.VARIABLE_SCOPE, VariableScope.class).getVariables().add(convertVariable(variable));
        return (T) this;
    }

    private <V> org.jbpm.process.core.context.variable.Variable convertVariable(Variable<V> variable) {
        org.jbpm.process.core.context.variable.Variable result = new org.jbpm.process.core.context.variable.Variable();
        result.setName(variable.getName());
        result.setType(TypeConverter.fromType(variable.getClass()));
        if (variable.getValue() != null)
            result.setValue(variable.getValue());
        if (variable.getMetadata() != null) {
            for (Map.Entry<String, Object> entry : variable.getMetadata().entrySet()) {
                result.setMetaData(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}

