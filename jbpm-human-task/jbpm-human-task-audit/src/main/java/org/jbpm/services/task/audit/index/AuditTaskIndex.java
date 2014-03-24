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

package org.jbpm.services.task.audit.index;

import java.util.UUID;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.jbpm.services.task.audit.impl.model.api.AuditTask;

/**
 * @author Hass Lund
 */
public abstract class AuditTaskIndex<T extends AuditTask> extends ModelIndexImpl<T> {

    @Override
    public Document prepare(T object) {
        Document doc = createDocument(object);
        addLongField("taskId", object.getTaskId(), doc, false);
        addKeyWordField("status", object.getStatus(), doc, false);
        addDateField("activationTime", object.getActivationTime(), doc, false);
        addKeyWordField("name", object.getName(), doc, false);
        addKeyWordField("description", object.getDescription(), doc, false);
        addIntField("priority", object.getPriority(), doc, false);
        addKeyWordField("createdBy", object.getCreatedBy(), doc, false);
        addDateField("createdOn", object.getCreatedOn(), doc, false);
        addDateField("dueDate", object.getDueDate(), doc, false);
        addLongField("processInstanceId", object.getProcessInstanceId(), doc,false);
        addKeyWordField("processId", object.getProcessId(), doc, false);
        addIntField("processSessionId", object.getProcessSessionId(), doc,false);
        addLongField("parentId", object.getParentId(), doc, false);
        return doc;
    }
}
