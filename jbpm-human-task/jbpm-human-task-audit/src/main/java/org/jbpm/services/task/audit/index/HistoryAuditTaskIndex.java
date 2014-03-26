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

import org.apache.lucene.document.Document;
import org.jbpm.services.task.audit.impl.model.HistoryAuditTaskImpl;
import org.jbpm.services.task.audit.impl.model.api.HistoryAuditTask;
import org.jbpm.services.task.audit.marshalling.AuditMarshaller;
import org.jbpm.services.task.audit.query.Filter;
import org.jbpm.services.task.audit.query.TypeFilter;

/**
 * @author Hans Lund
 */
public class HistoryAuditTaskIndex extends AuditTaskIndex<HistoryAuditTask> {

    private static final String type = "HistoryAuditTask";
    private static final TypeFilter<HistoryAuditTask> typeFilter = new TypeFilter<HistoryAuditTask>(type);

    @Override
    public Document prepare(HistoryAuditTask object) {
        Document doc = super.prepare(object);
        addKeyWordField("type", type, doc, false);
        //TODO: actualOwner is not the interface!
        if (object instanceof HistoryAuditTaskImpl) {
            addKeyWordField("actualOwner", ((HistoryAuditTaskImpl)object).getActualOwner(), doc, false);
        }
        return doc;
    }

    @Override
    public HistoryAuditTask fromBytes(byte[] bytes) {
        return AuditMarshaller.unMarshall(bytes, HistoryAuditTask.class);
    }

    @Override
    public Filter getTypeFilter() {
        return typeFilter;
    }

    @Override
    public String getId(HistoryAuditTask object) {
        return type + "_" + object.getTaskId();
    }

    @Override
    public Class getModelInterface() {
        return HistoryAuditTask.class;
    }

    @Override
    public boolean isModelFor(Class clazz) {
        return HistoryAuditTask.class.isAssignableFrom(clazz);
    }

    @Override
    protected String getType() {
        return type;
    }
}
