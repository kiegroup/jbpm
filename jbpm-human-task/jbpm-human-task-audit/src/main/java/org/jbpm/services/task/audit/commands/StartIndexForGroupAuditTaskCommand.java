package org.jbpm.services.task.audit.commands;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.jbpm.services.task.audit.impl.model.api.GroupAuditTask;
import org.jbpm.services.task.audit.index.IndexService;
import org.jbpm.services.task.utils.ClassUtil;
import org.kie.internal.task.api.TaskPersistenceContext;

@XmlRootElement(name="start-index-for-group-audit-task-command")
@XmlAccessorType(XmlAccessType.NONE)
public class StartIndexForGroupAuditTaskCommand extends StartIndexCommand<GroupAuditTask> {

	private static final long serialVersionUID = -7929370526623674312L;

	public StartIndexForGroupAuditTaskCommand() {
	}
	
	public StartIndexForGroupAuditTaskCommand(IndexService indexService) {
		super(indexService);
	}
	
	@Override
	protected Collection<GroupAuditTask> iterate(TaskPersistenceContext context, int offset, int count) {
		String query = "SELECT gati FROM GroupAuditTaskImpl gati ORDER BY gati.lastModificationDate DESC";
		return context.queryStringWithParametersInTransaction(query,
				context.addParametersToMap("firstResult", offset, "maxResults", count),
				ClassUtil.<List<GroupAuditTask>>castClass(List.class));
	}
}
