package org.jbpm.services.task.audit.commands;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.jbpm.services.task.audit.impl.model.api.HistoryAuditTask;
import org.jbpm.services.task.audit.index.IndexService;
import org.jbpm.services.task.utils.ClassUtil;
import org.kie.internal.task.api.TaskPersistenceContext;

@XmlRootElement(name="start-index-for-history-audit-task-command")
@XmlAccessorType(XmlAccessType.NONE)
public class StartIndexForHistoryAuditTaskCommand extends StartIndexCommand<HistoryAuditTask> {

	private static final long serialVersionUID = -7929370526623674312L;

	public StartIndexForHistoryAuditTaskCommand() {
	}
	
	public StartIndexForHistoryAuditTaskCommand(IndexService indexService) {
		super(indexService);
	}
	
	@Override
	protected Collection<HistoryAuditTask> iterate(TaskPersistenceContext context) {
		return context.queryStringInTransaction("select hati from HistoryAuditTaskImpl hati", 
				ClassUtil.<List<HistoryAuditTask>>castClass(List.class));
	}
}
