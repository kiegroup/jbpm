package org.jbpm.services.task.audit.commands;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.jbpm.services.task.audit.impl.model.api.HistoryAuditTask;
import org.jbpm.services.task.audit.impl.model.api.UserAuditTask;
import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.utils.ClassUtil;
import org.kie.internal.command.Context;
import org.kie.internal.task.api.TaskContext;
import org.kie.internal.task.api.TaskPersistenceContext;

@XmlRootElement(name="get-all-history-audit-tasks-command")
@XmlAccessorType(XmlAccessType.NONE)
public class GetAllHistoryAuditTasksCommand extends TaskCommand<List<HistoryAuditTask>> {

	public GetAllHistoryAuditTasksCommand() {
	}

	@Override
	public List<HistoryAuditTask> execute(Context context) {
		TaskPersistenceContext persistenceContext = ((TaskContext) context).getPersistenceContext();
		return persistenceContext.queryInTransaction("getAllHistoryAuditTasks", 
				ClassUtil.<List<HistoryAuditTask>>castClass(List.class));
	}

}
