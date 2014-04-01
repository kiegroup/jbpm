package org.jbpm.services.task.audit.commands;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.jbpm.services.task.audit.impl.model.api.UserAuditTask;
import org.jbpm.services.task.audit.index.IndexService;
import org.jbpm.services.task.utils.ClassUtil;
import org.kie.internal.task.api.TaskPersistenceContext;

@XmlRootElement(name="start-index-for-user-audit-task-command")
@XmlAccessorType(XmlAccessType.NONE)
public class StartIndexForUserAuditTaskCommand extends StartIndexCommand<UserAuditTask> {

	private static final long serialVersionUID = -7929370526623674312L;

	public StartIndexForUserAuditTaskCommand() {
	}
	
	public StartIndexForUserAuditTaskCommand(IndexService indexService) {
		super(indexService);
	}
	
	@Override
	protected Collection<UserAuditTask> iterate(TaskPersistenceContext context, int offset, int count) {
		String query = "SELECT uati FROM UserAuditTaskImpl uati where TYPE(uati) <> HistoryAuditTaskImpl ORDER BY uati.lastModificationDate DESC";
		return context.queryStringWithParametersInTransaction(query,
				context.addParametersToMap("firstResult", offset, "maxResults", count),
				ClassUtil.<List<UserAuditTask>>castClass(List.class));
	}
}
