package org.jbpm.services.task.audit.commands;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.jbpm.services.task.audit.impl.model.GroupAuditTask;

import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.utils.ClassUtil;
import org.kie.internal.command.Context;
import org.kie.internal.task.api.TaskContext;
import org.kie.internal.task.api.TaskPersistenceContext;

@XmlRootElement(name="get-all-group-audit-tasks-bystatus-command")
@XmlAccessorType(XmlAccessType.NONE)
public class GetAllGroupAuditTasksByStatusCommand extends TaskCommand<List<GroupAuditTask>> {
        private String groupIds;
        private String status;
	public GetAllGroupAuditTasksByStatusCommand() {
		
	}
	
	public GetAllGroupAuditTasksByStatusCommand(String groupIds, String status) {
		this.groupIds = groupIds;
                this.status = status;
	}
	
	@Override
	public List<GroupAuditTask> execute(Context context) {
		TaskPersistenceContext persistenceContext = ((TaskContext) context).getPersistenceContext();
		return persistenceContext.queryWithParametersInTransaction("getAllGroupAuditTasksByStatus", 
				persistenceContext.addParametersToMap("groupIds", groupIds, "status", status),
				ClassUtil.<List<GroupAuditTask>>castClass(List.class));
	}

}
