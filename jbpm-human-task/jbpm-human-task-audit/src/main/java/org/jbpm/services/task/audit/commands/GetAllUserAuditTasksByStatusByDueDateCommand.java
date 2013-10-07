package org.jbpm.services.task.audit.commands;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.audit.impl.model.UserAuditTask;
import org.jbpm.services.task.utils.ClassUtil;
import org.kie.internal.command.Context;
import org.kie.internal.task.api.TaskContext;
import org.kie.internal.task.api.TaskPersistenceContext;

@XmlRootElement(name="get-all-user-audit-tasks-bystatusbyduedate-command")
@XmlAccessorType(XmlAccessType.NONE)
public class GetAllUserAuditTasksByStatusByDueDateCommand extends TaskCommand<List<UserAuditTask>> {

        private List<String> statuses;
        private Date dueDate;
	public GetAllUserAuditTasksByStatusByDueDateCommand() {
		
	}
	
	public GetAllUserAuditTasksByStatusByDueDateCommand(String userId, List<String> statuses, Date dueDate) {
		this.userId = userId;
                this.statuses = statuses;
                this.dueDate = dueDate;
	}
	
	@Override
	public List<UserAuditTask> execute(Context context) {
		TaskPersistenceContext persistenceContext = ((TaskContext) context).getPersistenceContext();
		return persistenceContext.queryWithParametersInTransaction("getAllUserAuditTasksByStatusByDueDate", 
				persistenceContext.addParametersToMap("userId", userId, "statuses", statuses, "dueDate", dueDate),
				ClassUtil.<List<UserAuditTask>>castClass(List.class));
	}

}
