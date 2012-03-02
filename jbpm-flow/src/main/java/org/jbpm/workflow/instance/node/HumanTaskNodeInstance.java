/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.workflow.instance.node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.drools.RuntimeDroolsException;
import org.drools.process.instance.WorkItem;
import org.drools.time.TimeUtils;
import org.jbpm.process.core.context.swimlane.SwimlaneContext;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.context.swimlane.SwimlaneContextInstance;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.jbpm.workflow.core.node.TaskDeadline;
import org.jbpm.workflow.core.node.TaskNotification;
import org.jbpm.workflow.core.node.TaskReassignment;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.jbpm.workflow.instance.impl.NodeInstanceResolverFactory;
import org.mvel2.MVEL;

public class HumanTaskNodeInstance extends WorkItemNodeInstance {

    private static final long serialVersionUID = 510l;
    private static final Pattern PARAMETER_MATCHER = Pattern.compile("#\\{(\\S+)\\}", Pattern.DOTALL);
    
    private transient SwimlaneContextInstance swimlaneContextInstance;
    
    public HumanTaskNode getHumanTaskNode() {
        return (HumanTaskNode) getNode();
    }
    
    protected WorkItem createWorkItem(WorkItemNode workItemNode) {
        WorkItem workItem = super.createWorkItem(workItemNode);
        String actorId = assignWorkItem(workItem);
        if (actorId != null) {
            ((org.drools.process.instance.WorkItem) workItem).setParameter("ActorId", actorId);
        }
        // resolve time values for deadlines
        @SuppressWarnings("unchecked")
        List<TaskDeadline> deadlines = (List<TaskDeadline>) workItem.getParameter("Deadlines");
        if (deadlines != null) {
            for (TaskDeadline deadline : deadlines) {
                long resolvedExpiresInMillis = resolveTimeValue(deadline.getExpires());
                deadline.setExpires(Long.toString(resolvedExpiresInMillis));
                
                // process reassignments
                List<TaskReassignment> reassignments = deadline.getReassignments();
                if (reassignments != null) {
                    for (TaskReassignment reassignment : reassignments) {
                        String resolvedUsers = resolveValue(reassignment.getReassignUsers());
                        reassignment.setReassignUsers(resolvedUsers);
                        
                        String resolvedGroups = resolveValue(reassignment.getReassignGroups());
                        reassignment.setReassignGroups(resolvedGroups);
                    }
                }
                
                // process notifications
                List<TaskNotification> notifications = deadline.getNotifications();
                if (notifications != null) {
                    for (TaskNotification notification : notifications) {
                        String resolvedReceiver = resolveValue(notification.getReceiver());
                        notification.setReceiver(resolvedReceiver);
                        
                        String resolvedRecipients = resolveValue(notification.getRecipients());
                        notification.setRecipients(resolvedRecipients);
                        
                        String resolvedGroupRecipients = resolveValue(notification.getGroupRecipients());
                        notification.setGroupRecipients(resolvedGroupRecipients);
                        
                        String resolvedSender = resolveValue(notification.getSender());
                        notification.setSender(resolvedSender);
                    }
                }
            }
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
        }
        // if no actor can be assigned based on the swimlane, check whether an
        // actor is specified for this human task
        if (actorId == null) {
        	actorId = (String) workItem.getParameter("ActorId");
        	if (actorId != null && swimlaneContextInstance != null) {
        		swimlaneContextInstance.setActorId(swimlaneName, actorId);
        	}
        }
        return actorId;
    }
    
    private long resolveTimeValue(String s) {
        try {
            return TimeUtils.parseTimeString(s);
        } catch (RuntimeDroolsException e) {
            // cannot parse delay, trying to interpret it
            String resolved = resolveValue(s);
            return TimeUtils.parseTimeString(resolved);
        }
    }
    
    private String resolveValue(String s) {
        if (s == null) {
            return null;
        }
        
        Map<String, String> replacements = new HashMap<String, String>();
        Matcher matcher = PARAMETER_MATCHER.matcher(s);
        while (matcher.find()) {
            String paramName = matcher.group(1);
            if (replacements.get(paramName) == null) {
                VariableScopeInstance variableScopeInstance = (VariableScopeInstance)
                    resolveContextInstance(VariableScope.VARIABLE_SCOPE, paramName);
                if (variableScopeInstance != null) {
                    Object variableValue = variableScopeInstance.getVariable(paramName);
                    String variableValueString = variableValue == null ? "" : variableValue.toString(); 
                    replacements.put(paramName, variableValueString);
                } else {
                    try {
                        Object variableValue = MVEL.eval(paramName, new NodeInstanceResolverFactory(this));
                        String variableValueString = variableValue == null ? "" : variableValue.toString();
                        replacements.put(paramName, variableValueString);
                    } catch (Throwable t) {
                        System.err.println("Could not find variable scope for variable " + paramName);
                        System.err.println("when trying to replace variable in processId for sub process " + getNodeName());
                        System.err.println("Continuing without setting process id.");
                    }
                }
            }
        }
        for (Map.Entry<String, String> replacement: replacements.entrySet()) {
            s = s.replace("#{" + replacement.getKey() + "}", replacement.getValue());
        }
        return s;
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
}
