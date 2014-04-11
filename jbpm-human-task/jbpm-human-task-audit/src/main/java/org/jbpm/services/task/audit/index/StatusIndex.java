package org.jbpm.services.task.audit.index;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.kie.api.task.model.Status;

/**
 * Created by halu on 4/4/14.
 */
public class StatusIndex {

    private Map<Long, Status> states = Collections.synchronizedMap(new HashMap<Long, Status>());

    public void setStatus(long taskId, Status status) {
        states.put(taskId,status);
    }

    public void setStatus (long taskId, String status) {
        Status s = Status.valueOf(status);
        if (s == null) throw new IllegalArgumentException("Not a valid status: " + status);
            states.put(taskId,s);
    }

    public String getStatus(long taskId) {
        Status s = states.get(taskId);
        return s == null ? null : s.toString();
    }

    public Status get(long taskId) {
        return states.get(taskId);
    }

}
