package org.jbpm.services.task.audit.commands;

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

@XmlRootElement(name="get-all-user-audit-tasks-command")
@XmlAccessorType(XmlAccessType.NONE)
public class GetAllUserAuditTasksCommand extends TaskCommand<List<UserAuditTask>> {

	public GetAllUserAuditTasksCommand() {
		
	}
	
	public GetAllUserAuditTasksCommand(String userId) {
		this.userId = userId;
	}
	
	@Override
	public List<UserAuditTask> execute(Context context) {
		TaskPersistenceContext persistenceContext = ((TaskContext) context).getPersistenceContext();
		return persistenceContext.queryWithParametersInTransaction("getAllUserAuditTasks", 
				persistenceContext.addParametersToMap("userId", userId),
				ClassUtil.<List<UserAuditTask>>castClass(List.class));
	}

}
