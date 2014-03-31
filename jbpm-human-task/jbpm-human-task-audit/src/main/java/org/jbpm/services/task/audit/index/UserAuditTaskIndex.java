package org.jbpm.services.task.audit.index;

import org.apache.lucene.document.Document;
import org.jbpm.services.task.audit.impl.model.HistoryAuditTaskImpl;
import org.jbpm.services.task.audit.impl.model.api.UserAuditTask;
import org.jbpm.services.task.audit.marshalling.AuditMarshaller;
import org.jbpm.services.task.audit.query.Filter;
import org.jbpm.services.task.audit.query.TypeFilter;

/**
 * @author Hans Lund
 */
public class UserAuditTaskIndex extends AuditTaskIndex<UserAuditTask> {

    private  static final String type = "UserAuditTask";
    private final TypeFilter<UserAuditTask> typeFilter = new TypeFilter<UserAuditTask>(type);

    @Override
    public Document prepare(UserAuditTask object) {
        Document doc = super.prepare(object);
        addKeyWordField("type", type, doc, false);
        addKeyWordField("actualOwner", object.getActualOwner(), doc, false);
        return doc;
    }

    @Override
    public UserAuditTask fromBytes(byte[] bytes) {
        return AuditMarshaller.unMarshall(bytes, UserAuditTask.class);
    }

    @Override
    public Filter getTypeFilter() {
        return typeFilter;
    }

    @Override
    public String getId(UserAuditTask object) {
        return type + "_" + object.getTaskId();
    }

    @Override
    public boolean isModelFor(Class clazz) {
        return UserAuditTask.class.isAssignableFrom(clazz) && HistoryAuditTaskImpl.class != clazz;
    }

    @Override
    public Class<UserAuditTask> getModelInterface() {
        return UserAuditTask.class;
    }

    @Override
    protected String getType() {
        return type;
    }
}
