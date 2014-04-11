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

import java.util.StringTokenizer;

import org.apache.lucene.document.Document;
import org.jbpm.services.task.audit.impl.model.api.GroupAuditTask;
import org.jbpm.services.task.audit.marshalling.AuditMarshaller;
import org.jbpm.services.task.audit.query.Filter;
import org.jbpm.services.task.audit.query.TypeFilter;

/**
 * @author Hans Lund
 */
public class GroupAuditTaskIndex extends AuditTaskIndex<GroupAuditTask> {

    private static final String type = "GroupAuditTask";
    private static final TypeFilter<GroupAuditTask> typeFilter = new TypeFilter<GroupAuditTask>(type);

    @Override
    public Document prepare(GroupAuditTask object) {
        Document doc = super.prepare(object);
        addKeyWordField("type", type, doc, false);
        if (object.getPotentialOwners() != null) {
            StringTokenizer tok = new StringTokenizer(object.getPotentialOwners(), "|" , false);
            while (tok.hasMoreElements()) {
                addKeyWordField("potentialOwners", tok.nextToken(), doc, false);
            }
        }

        return doc;
    }

    @Override
    public GroupAuditTask fromBytes(byte[] bytes) {
        return AuditMarshaller.unMarshall(bytes, GroupAuditTask.class);
    }

    @Override
    public Filter getTypeFilter() {
        return typeFilter;
    }

    @Override
    public String getId(GroupAuditTask object) {
        return type + "_" + object.getTaskId();
    }

    @Override
    public Class<GroupAuditTask> getModelInterface() {
        return GroupAuditTask.class;
    }

    @Override
    public boolean isModelFor(Class clazz) {
        return GroupAuditTask.class.isAssignableFrom(clazz);
    }

    @Override
    protected String getType() {
        return type;
    }


}
