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

package org.jbpm.workflow.instance.node;

import org.drools.core.process.instance.WorkItem;
import org.jbpm.process.core.context.swimlane.SwimlaneContext;
import org.jbpm.process.instance.context.swimlane.SwimlaneContextInstance;
import org.jbpm.process.instance.timer.TimerInstance;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.model.Task;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HumanTaskNodeInstance extends WorkItemNodeInstance {
    public static final String ADMINISTRATOR_USER = System.getProperty("org.jbpm.ht.admin.user", "Administrator");
    public static final String SUSPEND_UNTIL_PARAMETER = "suspendUntil";
    public static final String SUSPEND_SIGNAL = "humanTaskNodeInstance:suspended";
    public static final String ACTIVATE_SIGNAL = "humanTaskNodeInstance:activated";

    private static final Logger logger = LoggerFactory.getLogger(HumanTaskNodeInstance.class);

    private static final long serialVersionUID = 510l;
    private String separator = System.getProperty("org.jbpm.ht.user.separator", ",");
    
    private transient SwimlaneContextInstance swimlaneContextInstance;

    private long suspendUntilTimerId = -1;
    
    public HumanTaskNode getHumanTaskNode() {
        return (HumanTaskNode) getNode();
    }
    
    public long getSuspendUntilTimerId() {
        return suspendUntilTimerId;
    }

    public void setSuspendUntilTimerId(long suspendUntilTimerId) {
        this.suspendUntilTimerId = suspendUntilTimerId;
    }

    protected WorkItem createWorkItem(WorkItemNode workItemNode) {
        WorkItem workItem = super.createWorkItem(workItemNode);
        String actorId = assignWorkItem(workItem);
        if (actorId != null) {
            ((org.drools.core.process.instance.WorkItem) workItem).setParameter("ActorId", actorId);
        }
        return workItem;
    }
    
    protected String assignWorkItem(WorkItem workItem) {
        String actorId = null;
        // if this human task node is part of a swimlane, check whether an actor
        // has already been assigned to this swimlane
        String swimlaneName = getHumanTaskNode().getSwimlane();
        SwimlaneContextInstance swimlaneContextInstance = getSwimlaneContextInstance(swimlaneName);
        if (swimlaneContextInstance != null) {
            actorId = swimlaneContextInstance.getActorId(swimlaneName);
            workItem.setParameter("SwimlaneActorId", actorId);
        }
        // if no actor can be assigned based on the swimlane, check whether an
        // actor is specified for this human task
        if (actorId == null) {
        	actorId = (String) workItem.getParameter("ActorId");
        	if (actorId != null && swimlaneContextInstance != null && actorId.split(separator).length == 1) {
        		swimlaneContextInstance.setActorId(swimlaneName, actorId);
        		workItem.setParameter("SwimlaneActorId", actorId);
        	}
        }
        // always return ActorId from workitem as SwimlaneActorId is kept as separate parameter
        return (String) workItem.getParameter("ActorId");
    }
    
    private SwimlaneContextInstance getSwimlaneContextInstance(String swimlaneName) {
        if (this.swimlaneContextInstance == null) {
            if (swimlaneName == null) {
                return null;
            }
            SwimlaneContextInstance swimlaneContextInstance =
                (SwimlaneContextInstance) resolveContextInstance(
                    SwimlaneContext.SWIMLANE_SCOPE, swimlaneName);
            if (swimlaneContextInstance == null) {
                throw new IllegalArgumentException(
                    "Could not find swimlane context instance");
            }
            this.swimlaneContextInstance = swimlaneContextInstance;
        }
        return this.swimlaneContextInstance;
    }
    
    public void triggerCompleted(WorkItem workItem) {
        String swimlaneName = getHumanTaskNode().getSwimlane();
        SwimlaneContextInstance swimlaneContextInstance = getSwimlaneContextInstance(swimlaneName);
        if (swimlaneContextInstance != null) {
            String newActorId = (String) workItem.getResult("ActorId");
            if (newActorId != null) {
                swimlaneContextInstance.setActorId(swimlaneName, newActorId);
            }
        }
        super.triggerCompleted(workItem);
    }
    
    @Override
    public void signalEvent(String type, Object event) {

        if("timerTriggered".equals(type)) {
            TimerInstance timerInstance = (TimerInstance) event;
            if(timerInstance.getId() == suspendUntilTimerId) {
                // if there is suspended until we need to activate the task with the same user id was the owner.
                RuntimeManager runtimeManager = RuntimeManagerRegistry.get().getManager(getProcessInstance().getDeploymentId());
                RuntimeEngine engine = runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get(timerInstance.getProcessInstanceId()));
                try {
                    Task task = engine.getTaskService().getTaskByWorkItemId(getWorkItemId());
                    // setToPreviousStatus = true of resume therefore it will recover the old status
                    // the operation is auto therefore use is administrator
                    engine.getTaskService().resume(task.getId(), ADMINISTRATOR_USER);
                } finally {
                    runtimeManager.disposeRuntimeEngine(engine);
                }
                suspendUntilTimerId = -1;
                return;
            }
        }

        super.signalEvent(type, event);

        if (!ownsWorkItem(event)) {
            return;
        }
        if (SUSPEND_SIGNAL.equals(type)) {
            createSuspendTimer((WorkItem) event);
        } else if (ACTIVATE_SIGNAL.equals(type)) {
            removeSuspendTimer();
        }
    }

    @Override
    public void cancel(CancelType cancelType) {
        if(suspendUntilTimerId != -1) {
            removeSuspendTimer();
        }
        super.cancel(cancelType);
    }

    private boolean ownsWorkItem(Object event) {
        if(!(event instanceof WorkItem)) {
            return false;
        }
        WorkItem workItem = (WorkItem) event;
        return (getWorkItemId() == workItem.getId() || (getWorkItemId() == -1 && getWorkItem().getId() == workItem.getId())) ;
    }

    private void removeSuspendTimer() {
        if(this.suspendUntilTimerId == -1) {
            return;
        }
        ((WorkflowProcessInstanceImpl) getProcessInstance()).cancelTimer(suspendUntilTimerId);
        suspendUntilTimerId = -1;
    }

    private void createSuspendTimer(WorkItem workItem) {
        // parameters takes priority over metadata
        String suspendUntil = (String) workItem.getParameter(SUSPEND_UNTIL_PARAMETER);

        if (suspendUntil == null) {
            suspendUntil = (String) getNode().getMetaData().get(SUSPEND_UNTIL_PARAMETER);
        } 

        if (suspendUntil == null) {
            return;
        }

        addTimerListener();
        TimerInstance timer = ((WorkflowProcessInstanceImpl) getProcessInstance()).configureTimer(suspendUntil, getNodeName() + "SuspendUntil", true);
        if (timer == null) {
            return;
        }

        this.suspendUntilTimerId = timer.getId();
        logger.debug("suspendUntilTimerId for node instance {} with expression {}", this.getId(), suspendUntil);

    }

    public void addEventListeners() {
        super.addEventListeners();
        addHumanTaskListeners();
        if(this.suspendUntilTimerId >= 0) {
            this.addTimerListener();
        }
    }

    private void addHumanTaskListeners() {
        getProcessInstance().addEventListener(ACTIVATE_SIGNAL, this, false);
        getProcessInstance().addEventListener(SUSPEND_SIGNAL, this, false);
    }

    @Override
    public void removeEventListeners() {
        super.removeEventListeners();
        removeHumanTaskListeners();
    }

    private void removeHumanTaskListeners() {
        getProcessInstance().removeEventListener(ACTIVATE_SIGNAL, this, false);
        getProcessInstance().removeEventListener(SUSPEND_SIGNAL, this, false);
    }
}
