package org.jbpm.services.task.audit.marshalling;

import org.jbpm.services.task.audit.impl.model.api.GroupAuditTask;
import org.jbpm.services.task.audit.impl.model.api.HistoryAuditTask;
import org.jbpm.services.task.audit.impl.model.api.UserAuditTask;
import org.kie.internal.task.api.model.TaskEvent;

public class AuditMarshaller {

    public static byte[] marshall(TaskEvent taskEvent) {
        return TaskEventMarshaller.toBytes(taskEvent);
    }

    public static <T> T unMarshall(byte[] data, Class<T> tClass) {
        if (TaskEvent.class == tClass) {
            return (T) TaskEventMarshaller.fromBytes(data);
        }
        return (T) AuditTaskMarshaller.fromByte(data, tClass);
    }

    public static <T> byte[] marshall(T object) {
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
