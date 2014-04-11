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

package org.jbpm.services.task.audit.service;

import org.jbpm.services.task.audit.impl.model.api.AuditTask;
import org.jbpm.services.task.audit.query.QueryComparator;
import org.kie.api.task.model.Status;

/**
 * @author Hans Lund
 */
public class StatusComparator<T> extends QueryComparator<T> {

    public StatusComparator(Direction direction) {
        super(direction, "taskId", Status.class);
    }


    @Override
    public int compare(T o1, T o2) {
        int nullCompare = objectNullCompare(o1,o2);
        if (nullCompare != 2) return nullCompare;
        int c;
        if (o1 instanceof AuditTask) {
            if (!(o2 instanceof AuditTask)) {
                c = 1;
            } else {
                c = Status.valueOf(((AuditTask) o1).getStatus()).ordinal()
                    - Status.valueOf(((AuditTask) o2).getStatus()).ordinal();
            }
        } else if (o2 instanceof AuditTask) {
            c = -1;
        } else {
            c = 0;
        }
        return direction == Direction.ASCENDING ? c : -c;
    }
}
