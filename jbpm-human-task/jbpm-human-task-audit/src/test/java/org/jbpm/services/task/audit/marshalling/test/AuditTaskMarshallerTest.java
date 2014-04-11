package org.jbpm.services.task.audit.marshalling.test;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.UUID;

import org.jbpm.services.task.audit.impl.model.api.AuditTask;
import org.jbpm.services.task.audit.impl.model.api.GroupAuditTask;
import org.jbpm.services.task.audit.impl.model.api.UserAuditTask;
import org.jbpm.services.task.audit.marshalling.AuditMarshaller;
import org.junit.Test;
import org.jbpm.services.task.audit.impl.model.UserAuditTaskImpl;

/**
 * Created by halu on 3/26/14.
 */
public class AuditTaskMarshallerTest {


    @Test
    public void testAuditTaskProtobufMarshaller() {
        //public UserAuditTaskImpl(String actualOwner, long taskId, String status, Date activationTime, String name, String description, int priority, String createdBy, Date createdOn, Date expirationTime,
        // long processInstanceId, String processId, int processSessionId, long parentId) {

            UserAuditTaskImpl userAuditTask = new UserAuditTaskImpl(
                "testOwner",
                1,
                "created",
                new Date(),
                "taskName",
                "taskDescription",
                5,
                "testCreator",
                new Date(),
                new Date(System.currentTimeMillis() + 100000),
                2, "processId", 1, -1);

        byte[] data = AuditMarshaller.marshall(userAuditTask);

        UserAuditTaskImpl parsed = (UserAuditTaskImpl) AuditMarshaller.unMarshall(data, UserAuditTask.class);
        assertTrue(testEquals(userAuditTask,parsed));

        //test null values
        userAuditTask = new UserAuditTaskImpl(
            "testOwner",
            1,
            "created",
            null,
            "taskName",
            "taskDescription",
            5,
            "testCreator",
            new Date(),
            new Date(System.currentTimeMillis() + 100000),
            2, "processId", 1, -1);

        data = AuditMarshaller.marshall(userAuditTask);
        parsed = (UserAuditTaskImpl) AuditMarshaller.unMarshall(data, UserAuditTask.class);
        assertTrue(testEquals(userAuditTask,parsed));

        //test null values
        userAuditTask = new UserAuditTaskImpl(
            "testOwner",
            1,
            "created",
            null,
            "taskName",
            "taskDescription",
            5,
            null,
            new Date(),
            new Date(System.currentTimeMillis() + 100000),
            2, "processId", 1, -1);

        data = AuditMarshaller.marshall(userAuditTask);

        parsed = (UserAuditTaskImpl) AuditMarshaller.unMarshall(data, UserAuditTask.class);
        assertTrue(testEquals(userAuditTask,parsed));
    }

    @Test
    public void testPerformance() throws Exception  {

        int sampleSize = 5000;
        long limit = 500000; //performance limit in ns (½ ms)

        UserAuditTask[] tasks = new UserAuditTask[sampleSize];

        for (int i = 0; i < sampleSize; i ++) {
            tasks[i] =
                new UserAuditTaskImpl(UUID.randomUUID().toString(), 1,
                    UUID.randomUUID().toString(), new Date(),
                    UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                    5, UUID.randomUUID().toString(), new Date(), new Date(System.currentTimeMillis() + 100000),
                    2, UUID.randomUUID().toString(), 1, -1);
        }

        byte[][] data = new byte[sampleSize][];
        long start = System.nanoTime();
        for (int i = 0 ; i < sampleSize ; i++) {
            data[i] = AuditMarshaller.marshall(tasks[i]);
        }
        assertTrue("Serialization took more than " + limit + "ns."
            ,(limit*sampleSize >= (System.nanoTime() - start)));
        start = System.nanoTime();
        for (int i = 0 ; i < sampleSize ; i++) {
            UserAuditTask ut = AuditMarshaller.unMarshall(data[i], UserAuditTask.class);
            testEquals(ut,tasks[i]);
        }
        assertTrue("unSerialization took more than 3ms for very small object"
            ,(limit*sampleSize >= (System.nanoTime() - start)));

    }

    private boolean testEquals(AuditTask task1, AuditTask task2) {
        if (task1 == task2) return true;
        if (UserAuditTask.class.isAssignableFrom(task1.getClass())) {
            if (!UserAuditTask.class.isAssignableFrom(task2.getClass())) return false;
            if (!assertComp(((UserAuditTask) task1).getActualOwner(),
                ((UserAuditTask) task2).getActualOwner())) {
                return false;
            }
        }
        if (GroupAuditTask.class.isAssignableFrom(task1.getClass())) {
            if (!GroupAuditTask.class.isAssignableFrom(task2.getClass())) return false;
            if (!assertComp(((GroupAuditTask) task1).getPotentialOwners(),
                ((GroupAuditTask) task2).getPotentialOwners())) {
                return false;
            }
        }
        if (task1.getTaskId() != task2.getTaskId()) return false;
        if (!assertComp(task1.getStatus(), task2.getStatus())) return false;
        if (!assertComp(task1.getActivationTime(), task2.getActivationTime())) return false;
        if (!assertComp(task1.getName(), task2.getName())) return false;
        if (!assertComp(task1.getDescription(), task2.getDescription())) return false;
        if (task1.getPriority() != task2.getPriority()) return false;
        if (!assertComp(task1.getCreatedBy(), task2.getCreatedBy())) return false;
        if (!assertComp(task1.getCreatedOn(), task2.getCreatedOn())) return false;
        if (!assertComp(task1.getDueDate(), task2.getDueDate())) return false;
        if (task1.getProcessInstanceId() != task2.getProcessInstanceId()) return false;
        if (!assertComp(task1.getProcessId(), task2.getProcessId())) return false;
        if (task1.getProcessSessionId() != task2.getProcessSessionId()) return false;
        return task1.getParentId() == task2.getParentId();
    }

    private boolean assertComp(Comparable s1, Comparable s2) {
        if (s1 != null) return s1.compareTo(s2) == 0;
        return s2 == null || s2.compareTo(s1) == 0;
    }
}
