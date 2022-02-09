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
package org.jbpm.services.task.commands;

import org.drools.core.xml.jaxb.util.JaxbMapAdapter;
import org.kie.api.runtime.Context;

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Operation.Suspend : [ new OperationCommand().{ status = [ Status.Ready ],
 * allowed = [ Allowed.PotentialOwner, Allowed.BusinessAdministrator ],
 * newStatus = Status.Suspended }, new OperationCommand().{ status = [
 * Status.Reserved, Status.InProgress ], allowed = [Allowed.Owner,
 * Allowed.BusinessAdministrator ], newStatus = Status.Suspended } ],
 */
@XmlRootElement(name="suspend-task-command")
@XmlAccessorType(XmlAccessType.NONE)
public class SuspendTaskCommand extends UserGroupCallbackTaskCommand<Void> {
	
	private static final long serialVersionUID = 5486559063221608125L;

    @XmlElement(name="parameters")
    @XmlJavaTypeAdapter(JaxbMapAdapter.class)
    private Map<String, Object> parameters;

	public SuspendTaskCommand() {
	}

	public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public SuspendTaskCommand(long taskId, String userId, Map<String, Object> parameters) {
        this.taskId = taskId;
        this.userId = userId;
        this.parameters = parameters;
    }

    public Void execute(Context cntxt) {
        TaskContext context = (TaskContext) cntxt;
        doCallbackUserOperation(userId, context, true);
        groupIds = doUserGroupCallbackOperation(userId, null, context);
        context.set("local:groups", groupIds);
    	context.getTaskInstanceService().suspend(taskId, userId, parameters);
    	return null;        
    }
}
