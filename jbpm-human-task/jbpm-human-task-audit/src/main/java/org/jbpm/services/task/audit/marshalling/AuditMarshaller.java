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

import org.jbpm.services.task.audit.impl.model.api.GroupAuditTask;
import org.jbpm.services.task.audit.impl.model.api.HistoryAuditTask;
import org.jbpm.services.task.audit.impl.model.api.UserAuditTask;
import org.kie.internal.task.api.model.TaskEvent;

/**
 * @author Hans Lund
 */
public class AuditMarshaller {

    public static <T> T unMarshall(byte[] data, Class<T> tClass) {
        if (TaskEvent.class == tClass) {
            return (T) TaskEventMarshaller.fromBytes(data);
        }
        return (T) AuditTaskMarshaller.fromByte(data, tClass);
    }

    public static <T> byte[] marshall(T object) {
        //historyAuditTask must be checked on before its base interface UserAuditTask
        if (HistoryAuditTask.class.isAssignableFrom(object.getClass())) {
            return AuditTaskMarshaller.toByte((HistoryAuditTask)object);
        }
        if (UserAuditTask.class.isAssignableFrom(object.getClass())) {
            return AuditTaskMarshaller.toByte((UserAuditTask)object);
        }
        if (GroupAuditTask.class.isAssignableFrom(object.getClass())) {
            return AuditTaskMarshaller.toByte((GroupAuditTask)object);
        }
        if (TaskEvent.class.isAssignableFrom(object.getClass())) {
            return TaskEventMarshaller.toBytes((TaskEvent)object);
        }
        throw new IllegalArgumentException("Unsupported type: "
            + object.getClass().getCanonicalName());
    }
}
