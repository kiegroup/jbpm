package org.jbpm.services.task.audit.index;

import org.apache.lucene.document.Document;
import org.jbpm.services.task.audit.impl.model.api.UserAuditTask;
import org.jbpm.services.task.audit.query.Filter;
import org.jbpm.services.task.audit.query.TypeFilter;

/**
 * @author Hans Lund
 */
public class UserAuditTaskIndex extends AuditTaskIndex<UserAuditTask> {

    private final String type = "UserAuditTask";
    private final TypeFilter<UserAuditTask> typeFilter = new TypeFilter<UserAuditTask>(type);

    @Override
    public Document prepare(UserAuditTask object) {
        Document doc = super.prepare(object);
        addKeyWordField("type", type, doc, false);
        addKeyWordField("actualOwner", object.getActualOwner(), doc, false);
        return doc;
    }

    @Override
    public Filter getTypeFilter() {
        return typeFilter;
    }

    @Override
    public Class<UserAuditTask> getClazz() {
        return UserAuditTask.class;
    }
}
