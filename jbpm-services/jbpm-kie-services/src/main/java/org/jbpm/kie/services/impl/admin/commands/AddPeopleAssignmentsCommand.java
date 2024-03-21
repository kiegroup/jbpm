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

package org.jbpm.kie.services.impl.admin.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jbpm.services.task.commands.TaskContext;
import org.jbpm.services.task.commands.UserGroupCallbackTaskCommand;
import org.jbpm.services.task.events.TaskEventSupport;
import org.jbpm.services.task.exception.PermissionDeniedException;
import org.kie.api.runtime.Context;
import org.kie.api.task.TaskLifeCycleEventListener.AssignmentType;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.model.InternalPeopleAssignments;

import static org.jbpm.kie.services.impl.admin.UserTaskAdminServiceImpl.ADMIN;
import static org.jbpm.kie.services.impl.admin.UserTaskAdminServiceImpl.EXCL_OWNER;
import static org.jbpm.kie.services.impl.admin.UserTaskAdminServiceImpl.POT_OWNER;


public class AddPeopleAssignmentsCommand extends UserGroupCallbackTaskCommand<Void> {

    private static final long serialVersionUID = -1856489382099976731L;
    
    private int type;
    private OrganizationalEntity[] entities;
    private boolean removeExisting;


    public AddPeopleAssignmentsCommand(String userId, long taskId, int type, OrganizationalEntity[] entities, boolean removeExisting) {
        super();
        setUserId(userId);
        setTaskId(taskId);
        this.type = type;
        this.entities = entities;
        this.removeExisting = removeExisting;
    }

    @Override
    public Void execute(Context cntxt) {
        TaskContext context = (TaskContext) cntxt;
        TaskEventSupport taskEventSupport = context.getTaskEventSupport();
        
        Task task = context.getTaskQueryService().getTaskInstanceById(taskId);       
        // security check
        if (!isBusinessAdmin(userId, task.getPeopleAssignments().getBusinessAdministrators(), context)) {
            throw new PermissionDeniedException("User " + userId + " is not business admin of task " + taskId);
        }
        
        List<OrganizationalEntity> beforeChangeEntityList = new ArrayList<>();
        List<OrganizationalEntity> entityList = Arrays.asList(entities);
        AssignmentType assignmentType = null;
        switch (type) {
            case POT_OWNER:
                beforeChangeEntityList.addAll(task.getPeopleAssignments().getPotentialOwners());
                if (removeExisting) {
                    task.getPeopleAssignments().getPotentialOwners().clear();
                }
                task.getPeopleAssignments().getPotentialOwners().addAll(entityList);
                assignmentType = AssignmentType.POT_OWNER;
                break;
            case EXCL_OWNER:
                beforeChangeEntityList.addAll(((InternalPeopleAssignments)task.getPeopleAssignments()).getExcludedOwners());
                if (removeExisting) {
                    ((InternalPeopleAssignments)task.getPeopleAssignments()).getExcludedOwners().clear();
                }
                ((InternalPeopleAssignments)task.getPeopleAssignments()).getExcludedOwners().addAll(entityList);
                assignmentType = AssignmentType.EXCL_OWNER;
                break;
            case ADMIN:
                beforeChangeEntityList.addAll(task.getPeopleAssignments().getBusinessAdministrators());
                if (removeExisting) {
                    task.getPeopleAssignments().getBusinessAdministrators().clear();
                }
                task.getPeopleAssignments().getBusinessAdministrators().addAll(entityList);
                assignmentType = AssignmentType.ADMIN;
                break;

            default:
                break;
        }

        taskEventSupport.fireBeforeTaskAssignmentsAddedEvent(task, context, assignmentType, entityList, beforeChangeEntityList);
        doCallbackOperationForPeopleAssignments(((InternalPeopleAssignments) task.getPeopleAssignments()), context);
        context.getPersistenceContext().updateTask(task);

        List<OrganizationalEntity> afterChangeEntityList = new ArrayList<>(entityList);
        if (!removeExisting) {
            afterChangeEntityList.addAll(beforeChangeEntityList);
        }
        taskEventSupport.fireAfterTaskAssignmentsAddedEvent(task, context, assignmentType, entityList, afterChangeEntityList);
        return null;
    }

}
