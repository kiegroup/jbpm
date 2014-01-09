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

@XmlRootElement(name="get-all-user-audit-tasks-bystatus-command")
@XmlAccessorType(XmlAccessType.NONE)
public class GetAllUserAuditTasksByStatusCommand extends TaskCommand<List<UserAuditTask>> {

        private String status;
	public GetAllUserAuditTasksByStatusCommand() {
		
	}
	
	public GetAllUserAuditTasksByStatusCommand(String userId, String status) {
		this.userId = userId;
                this.status = status;
	}
	
	@Override
	public List<UserAuditTask> execute(Context context) {
		TaskPersistenceContext persistenceContext = ((TaskContext) context).getPersistenceContext();
		return persistenceContext.queryWithParametersInTransaction("getAllUserAuditTasksByStatus", 
				persistenceContext.addParametersToMap("userId", userId, "status", status),
				ClassUtil.<List<UserAuditTask>>castClass(List.class));
	}

}
