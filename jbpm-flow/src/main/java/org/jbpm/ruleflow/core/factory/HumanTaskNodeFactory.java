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

import org.jbpm.process.core.Work;
import org.jbpm.process.core.impl.WorkImpl;
import org.jbpm.process.core.timer.Timer;
import org.jbpm.workflow.core.DroolsAction;
import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.impl.DroolsConsequenceAction;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.jbpm.workflow.core.node.MilestoneNode;
import org.kie.api.fluent.Dialect;
import org.kie.api.fluent.HumanTaskNodeBuilder;
import org.kie.api.fluent.NodeContainerBuilder;

/**
 *
 */
public class HumanTaskNodeFactory<T extends NodeContainerBuilder<T, ?>> extends NodeFactory<HumanTaskNodeBuilder<T>,T> implements HumanTaskNodeBuilder<T> {

    public HumanTaskNodeFactory(T nodeContainerFactory, NodeContainer nodeContainer, long id) {
        super(nodeContainerFactory, nodeContainer, new HumanTaskNode(), id);
    }

    

    protected HumanTaskNode getHumanTaskNode() {
    	return (HumanTaskNode) getNode();
    }

    @Override
    public HumanTaskNodeFactory<T> name(String name) {
        getNode().setName(name);
        return this;
    }
    
    @Override
    public HumanTaskNodeFactory<T> taskName(String taskName) {
    	Work work = getHumanTaskNode().getWork();
    	if (work == null) {
    		work = new WorkImpl();
    		getHumanTaskNode().setWork(work);
    	}
    	work.setParameter("TaskName", taskName);
    	return this;
    }
    
    @Override
    public HumanTaskNodeFactory<T> actorId(String actorId) {
    	Work work = getHumanTaskNode().getWork();
    	if (work == null) {
    		work = new WorkImpl();
    		getHumanTaskNode().setWork(work);
    	}
    	work.setParameter("ActorId", actorId);
    	return this;
    }
    
    @Override
    public HumanTaskNodeFactory<T> priority(String priority) {
    	Work work = getHumanTaskNode().getWork();
    	if (work == null) {
    		work = new WorkImpl();
    		getHumanTaskNode().setWork(work);
    	}
    	work.setParameter("Priority", priority);
    	return this;
    }
    
    @Override
    public HumanTaskNodeFactory<T> comment(String comment) {
    	Work work = getHumanTaskNode().getWork();
    	if (work == null) {
    		work = new WorkImpl();
    		getHumanTaskNode().setWork(work);
    	}
    	work.setParameter("Comment", comment);
    	return this;
    }
    
    @Override
    public HumanTaskNodeFactory<T> skippable(boolean skippable) {
    	Work work = getHumanTaskNode().getWork();
    	if (work == null) {
    		work = new WorkImpl();
    		getHumanTaskNode().setWork(work);
    	}
    	work.setParameter("Skippable", Boolean.toString(skippable));
    	return this;
    }
    
    @Override
    public HumanTaskNodeFactory<T> content(String content) {
    	Work work = getHumanTaskNode().getWork();
    	if (work == null) {
    		work = new WorkImpl();
    		getHumanTaskNode().setWork(work);
    	}
    	work.setParameter("Content", content);
    	return this;
    }
    
    @Override
    public HumanTaskNodeFactory<T> inMapping(String parameterName, String variableName) {
    	getHumanTaskNode().addInMapping(parameterName, variableName);
        return this;
    }

    @Override
    public HumanTaskNodeFactory<T> outMapping(String parameterName, String variableName) {
    	getHumanTaskNode().addOutMapping(parameterName, variableName);
        return this;
    }

    @Override
    public HumanTaskNodeFactory<T> waitForCompletion(boolean waitForCompletion) {
    	getHumanTaskNode().setWaitForCompletion(waitForCompletion);
        return this;
    }

    @Override
    public HumanTaskNodeFactory<T> swimlane(String swimlane) {
    	getHumanTaskNode().setSwimlane(swimlane);
        return this;
    }


    public HumanTaskNodeFactory<T> onEntryAction(String dialect, String action) {
        if (getHumanTaskNode().getActions(dialect) != null) {
        	getHumanTaskNode().getActions(dialect).add(new DroolsConsequenceAction(dialect, action));
        } else {
            List<DroolsAction> actions = new ArrayList<DroolsAction>();
            actions.add(new DroolsConsequenceAction(dialect, action));
            getHumanTaskNode().setActions(MilestoneNode.EVENT_NODE_ENTER, actions);
        }
        return this;
    }


    public HumanTaskNodeFactory<T> onExitAction(String dialect, String action) {
        if (getHumanTaskNode().getActions(dialect) != null) {
        	getHumanTaskNode().getActions(dialect).add(new DroolsConsequenceAction(dialect, action));
        } else {
            List<DroolsAction> actions = new ArrayList<DroolsAction>();
            actions.add(new DroolsConsequenceAction(dialect, action));
            getHumanTaskNode().setActions(MilestoneNode.EVENT_NODE_EXIT, actions);
        }
        return this;
    }

    public HumanTaskNodeFactory<T> timer(String delay, String period, String dialect, String action) {
    	Timer timer = new Timer();
    	timer.setDelay(delay);
    	timer.setPeriod(period);
    	getHumanTaskNode().addTimer(timer, new DroolsConsequenceAction(dialect, action));
    	return this;
    }

	@Override
    public HumanTaskNodeFactory<T> workParameter(String name, Object value) {
		Work work = getHumanTaskNode().getWork();
		if (work == null) {
			work = new WorkImpl();
			getHumanTaskNode().setWork(work);
		}
		work.setParameter(name, value);
		return this;
	}

    @Override
    public HumanTaskNodeBuilder<T> onEntryAction(Dialect dialect, String action) {
        return onEntryAction(DialectConverter.fromDialect(dialect), action);
    }

    @Override
    public HumanTaskNodeBuilder<T> onExitAction(Dialect dialect, String action) {
        return onExitAction(DialectConverter.fromDialect(dialect), action);
    }

    @Override
    public HumanTaskNodeBuilder<T> timer(String delay, String period, Dialect dialect, String action) {
        return timer(delay, period, DialectConverter.fromDialect(dialect), action);
    }
}

