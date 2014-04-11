/*
 * Copyright 2014 JBoss by Red Hat.
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

package org.jbpm.services.task.audit.marshalling;

import java.util.Date;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.InvalidProtocolBufferException;
import org.jbpm.services.task.audit.impl.model.GroupAuditTaskImpl;
import org.jbpm.services.task.audit.impl.model.HistoryAuditTaskImpl;
import org.jbpm.services.task.audit.impl.model.UserAuditTaskImpl;
import org.jbpm.services.task.audit.impl.model.api.AuditTask;
import org.jbpm.services.task.audit.impl.model.api.GroupAuditTask;
import org.jbpm.services.task.audit.impl.model.api.HistoryAuditTask;
import org.jbpm.services.task.audit.impl.model.api.UserAuditTask;

/**
 * @author Hans Lund
 */
class AuditTaskMarshaller {


    private static final ExtensionRegistry registry =
        ExtensionRegistry.newInstance();

    static {
        registry.add(AuditMessages.UserAuditTask.actualOwner);
        registry.add(AuditMessages.GroupAuditTask.potentialOwners);
    }

    private static AuditMessages.AuditTask.Builder getBuilder(AuditTask au) {
        AuditMessages.AuditTask.Builder auditTask = AuditMessages.AuditTask.newBuilder();
        auditTask.setTaskId(au.getTaskId());
        if (au.getActivationTime() != null) auditTask.setActivationTime(au.getActivationTime().getTime());
        if (au.getName() != null) auditTask.setName(au.getName());
        if (au.getStatus() != null) auditTask.setStatus(au.getStatus());
        if (au.getDescription() != null) auditTask.setDescription(au.getDescription());
        auditTask.setPriority(au.getPriority());
        if (au.getCreatedBy() != null) auditTask.setCreatedBy(au.getCreatedBy());
        if (au.getCreatedOn() != null) auditTask.setCreatedOn(au.getCreatedOn().getTime());
        if (au.getDueDate() != null) auditTask.setDueDate(au.getDueDate().getTime());
        auditTask.setProcessInstanceId(au.getProcessInstanceId());
        if (au.getProcessId() != null) auditTask.setProcessId(au.getProcessId());
        auditTask.setProcessSessionId(au.getProcessSessionId());
        auditTask.setParentId(au.getParentId());
        return auditTask;
    }

    static byte[] toByte(UserAuditTask userAuditTask) {
        AuditMessages.AuditTask.Builder builder = getBuilder(userAuditTask);
        if (userAuditTask.getActualOwner() != null) {
            builder.setExtension(AuditMessages.UserAuditTask.actualOwner,
                userAuditTask.getActualOwner());
        }
        return builder.build().toByteArray();
    }

    static byte[] toByte(HistoryAuditTask historyAuditTask) {
        return toByte((UserAuditTask) historyAuditTask);
    }

    static byte[] toByte(GroupAuditTask groupAuditTask) {
       AuditMessages.AuditTask.Builder builder = getBuilder(groupAuditTask);
        if (groupAuditTask.getPotentialOwners() != null) {
            builder.setExtension(AuditMessages.UserAuditTask.actualOwner,
                groupAuditTask.getPotentialOwners());
        }
        return builder.build().toByteArray();
    }


    static AuditTask fromByte(byte[] bytes, Class type) {
        AuditMessages.AuditTask task;
        try {
            task = AuditMessages.AuditTask.parseFrom(bytes, registry);
            AuditMessages.UserAuditTask.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalArgumentException("Unsupported data type",e);
        }
            if (UserAuditTask.class == type) {
                return new UserAuditTaskImpl(
                    task.hasExtension(AuditMessages.UserAuditTask.actualOwner) ? task.getExtension(AuditMessages.UserAuditTask.actualOwner) : null,
                    task.getTaskId(),
                    task.hasStatus() ? task.getStatus() : null,
                    task.hasActivationTime() ? new Date(task.getActivationTime()) : null, task.getName(),
                    task.hasDescription() ? task.getDescription() : null,
                    task.getPriority(),
                    task.hasCreatedBy() ? task.getCreatedBy(): null,
                    task.hasCreatedOn() ? new Date(task.getCreatedOn()): null,
                    task.hasDueDate() ? new Date(task.getDueDate()): null,
                    task.getProcessInstanceId(),
                    task.hasProcessId() ? task.getProcessId(): null
                    , task.getProcessSessionId(),
                    task.getParentId());
            } else if (HistoryAuditTask.class == type) {
                return new HistoryAuditTaskImpl(
                    task.hasExtension(AuditMessages.UserAuditTask.actualOwner) ? task.getExtension(AuditMessages.UserAuditTask.actualOwner) : null,
                    task.getTaskId(),
                    task.hasStatus() ? task.getStatus() : null,
                    task.hasActivationTime() ? new Date(task.getActivationTime()) : null, task.getName(),
                    task.hasDescription() ? task.getDescription() : null,
                    task.getPriority(),
                    task.hasCreatedBy() ? task.getCreatedBy(): null,
                    task.hasCreatedOn() ? new Date(task.getCreatedOn()): null,
                    task.hasDueDate() ? new Date(task.getDueDate()): null,
                    task.getProcessInstanceId(),
                    task.hasProcessId() ? task.getProcessId(): null
                    , task.getProcessSessionId(),
                    task.getParentId());
            }

            else if (GroupAuditTask.class == type) {
                return new GroupAuditTaskImpl(
                    task.hasExtension(AuditMessages.GroupAuditTask.potentialOwners) ? task.getExtension(AuditMessages.GroupAuditTask.potentialOwners) : null,
                    task.getTaskId(),
                    task.hasStatus() ? task.getStatus() : null,
                    task.hasActivationTime() ? new Date(task.getActivationTime()) : null,
                    task.hasName() ? task.getName() : null,
                    task.hasDescription() ? task.getDescription() : null,
                    task.getPriority(),
                    task.hasCreatedBy() ? task.getCreatedBy(): null ,
                    task.hasCreatedOn() ? new Date(task.getCreatedOn()) : null,
                    task.hasDueDate() ? new Date(task.getDueDate()) : null,
                    task.getProcessInstanceId(),
                    task.hasProcessId() ? task.getProcessId() : null,
                    task.getProcessSessionId(),
                    task.getParentId());
            }
            throw new IllegalArgumentException("Unsupported data type");
    }
}
