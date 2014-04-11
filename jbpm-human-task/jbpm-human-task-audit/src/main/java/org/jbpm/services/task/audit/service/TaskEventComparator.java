package org.jbpm.services.task.audit.service;

import org.jbpm.services.task.audit.query.QueryComparator;
import org.kie.internal.task.api.model.TaskEvent;

/**
 * @author Hans Lund
 */
public class TaskEventComparator extends QueryComparator<TaskEvent> {

    public TaskEventComparator(Direction direction) {
        super(direction, "id");
    }
}
