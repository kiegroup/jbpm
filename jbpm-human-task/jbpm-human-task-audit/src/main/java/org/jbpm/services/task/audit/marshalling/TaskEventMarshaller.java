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

import com.google.protobuf.InvalidProtocolBufferException;
import org.jbpm.services.task.audit.impl.model.TaskEventImpl;
import org.kie.internal.task.api.model.TaskEvent;

/**
 * @author Hans Lund
*/
final class TaskEventMarshaller {


    static byte[] toBytes(TaskEvent event) {
        AuditMessages.TaskEvent.Builder eventBuilder =
            AuditMessages.TaskEvent.newBuilder();
        try {
            eventBuilder.setId(event.getId());
        } catch (NullPointerException e) {
            //TODO: no other way to detect as getter performs unboxing to primitive.
        }
        if (event instanceof TaskEventImpl) {
            if (((TaskEventImpl) event).getVersion() != null) {
                eventBuilder.setVersion(((TaskEventImpl) event).getVersion());
            }
        }
        if (event.getLogTime() != null) eventBuilder.setLogTime(event.getLogTime().getTime());
        if (event.getType() != null) eventBuilder.setType(map(event.getType()));
        if (event.getUserId() != null) eventBuilder.setUserId(event.getUserId());
        return eventBuilder.build().toByteArray();
    }

    static TaskEvent fromBytes(byte[] data) {
        try {
            AuditMessages.TaskEvent messageEvent = AuditMessages.TaskEvent.PARSER.parseFrom(data);
            TaskEventImpl impl =  new TaskEventImpl();
            if (messageEvent.hasUserId()) impl.setUserId(messageEvent.getUserId());
            if (messageEvent.hasType()) impl.setType(mapinv(messageEvent.getType()));
            if (messageEvent.hasId()) impl.setId(messageEvent.getId());
            if (messageEvent.hasVersion()) impl.setVersion(messageEvent.getVersion());
            if (messageEvent.hasLogTime()) impl.setLogTime(new Date(messageEvent.getLogTime()));
            return impl;
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalArgumentException("data not a valid taskEvent");
        }
    }

    private static AuditMessages.TaskEvent.TaskEventType map(TaskEvent.TaskEventType type) {
        switch (type) {
            case STARTED:
                return AuditMessages.TaskEvent.TaskEventType.STARTED;
            case ACTIVATED:
                return AuditMessages.TaskEvent.TaskEventType.ACTIVATED;
            case COMPLETED:
                return AuditMessages.TaskEvent.TaskEventType.COMPLETED;
            case STOPPED:
                return AuditMessages.TaskEvent.TaskEventType.STOPPED;
            case EXITED:
                return AuditMessages.TaskEvent.TaskEventType.EXITED;
            case FAILED:
                return AuditMessages.TaskEvent.TaskEventType.FAILED;
            case ADDED:
                return AuditMessages.TaskEvent.TaskEventType.ADDED;
            case CLAIMED:
                return AuditMessages.TaskEvent.TaskEventType.CLAIMED;
            case SKIPPED:
                return AuditMessages.TaskEvent.TaskEventType.SKIPPED;
            case SUSPENDED:
                return AuditMessages.TaskEvent.TaskEventType.SUSPENDED;
            case CREATED:
                return AuditMessages.TaskEvent.TaskEventType.CREATED;
            case FORWARDED:
                return AuditMessages.TaskEvent.TaskEventType.FORWARDED;
            case RELEASED:
                return AuditMessages.TaskEvent.TaskEventType.RELEASED;
            case RESUMED:
                return AuditMessages.TaskEvent.TaskEventType.RESUMED;
            case DELEGATED:
                return AuditMessages.TaskEvent.TaskEventType.DELEGATED;
            default:
                return null;
        }
    }

    private static TaskEvent.TaskEventType mapinv(AuditMessages.TaskEvent.TaskEventType type) {
        switch (type) {
            case STARTED:
                return TaskEvent.TaskEventType.STARTED;
            case ACTIVATED:
                return TaskEvent.TaskEventType.ACTIVATED;
            case COMPLETED:
                return TaskEvent.TaskEventType.COMPLETED;
            case STOPPED:
                return TaskEvent.TaskEventType.STOPPED;
            case EXITED:
                return TaskEvent.TaskEventType.EXITED;
            case FAILED:
                return TaskEvent.TaskEventType.FAILED;
            case ADDED:
                return TaskEvent.TaskEventType.ADDED;
            case CLAIMED:
                return TaskEvent.TaskEventType.CLAIMED;
            case SKIPPED:
                return TaskEvent.TaskEventType.SKIPPED;
            case SUSPENDED:
                return TaskEvent.TaskEventType.SUSPENDED;
            case CREATED:
                return TaskEvent.TaskEventType.CREATED;
            case FORWARDED:
                return TaskEvent.TaskEventType.FORWARDED;
            case RELEASED:
                return TaskEvent.TaskEventType.RELEASED;
            case RESUMED:
                return TaskEvent.TaskEventType.RESUMED;
            case DELEGATED:
                return TaskEvent.TaskEventType.DELEGATED;
            default:
                return null;
        }
    }
}
