/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.services.task.commands;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.api.task.model.TaskSummary;
import org.kie.internal.command.Context;

@XmlRootElement(name = "get-tasks-by-group-command")
@XmlAccessorType(XmlAccessType.NONE)
public class GetTasksByGroupCommand extends UserGroupCallbackTaskCommand<List<TaskSummary>> {

    private static final long serialVersionUID = 4920650870258057313L;

    public GetTasksByGroupCommand() {
        // for marshalling
    }

    public GetTasksByGroupCommand(List<String> groupIds) {
        this.groupIds = groupIds;
    }

    public List<TaskSummary> execute(Context cntxt) {
        TaskContext context = (TaskContext) cntxt;

        for( String groupId: groupIds ) {
            doCallbackGroupOperation(groupId, context);
        }
        return context.getTaskQueryService().getTasksByGroup(groupIds);
    }

}
