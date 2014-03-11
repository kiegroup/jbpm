package org.jbpm.services.task.audit.commands;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.jbpm.services.task.audit.impl.model.api.GroupAuditTask;

import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.utils.ClassUtil;
import org.kie.internal.command.Context;
import org.kie.internal.task.api.TaskContext;
import org.kie.internal.task.api.TaskPersistenceContext;

@XmlRootElement(name="get-all-group-audit-tasks-command")
@XmlAccessorType(XmlAccessType.NONE)
public class GetAllGroupAuditTasksCommand extends TaskCommand<List<GroupAuditTask>> {

        private String groupIds; 
	public GetAllGroupAuditTasksCommand() {
		
	}
	
	public GetAllGroupAuditTasksCommand(String groupIds) {
		this.groupIds = groupIds;
	}
	
	@Override
	public List<GroupAuditTask> execute(Context context) {
		TaskPersistenceContext persistenceContext = ((TaskContext) context).getPersistenceContext();
		return persistenceContext.queryWithParametersInTransaction("getAllGroupAuditTasks", 
				persistenceContext.addParametersToMap("groupIds", groupIds),
				ClassUtil.<List<GroupAuditTask>>castClass(List.class));
	}

}
