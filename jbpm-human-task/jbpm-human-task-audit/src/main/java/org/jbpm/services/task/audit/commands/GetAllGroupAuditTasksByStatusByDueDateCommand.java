package org.jbpm.services.task.audit.commands;

import java.util.Date;
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

@XmlRootElement(name="get-all-group-audit-tasks-bystatusbyduedate-command")
@XmlAccessorType(XmlAccessType.NONE)
public class GetAllGroupAuditTasksByStatusByDueDateCommand extends TaskCommand<List<GroupAuditTask>> {
        private String groupIds;
        private List<String> statuses;
        private Date dueDate;
	public GetAllGroupAuditTasksByStatusByDueDateCommand() {
		
	}
	
	public GetAllGroupAuditTasksByStatusByDueDateCommand(String groupIds, List<String> statuses, Date dueDate) {
		this.groupIds = groupIds;
                this.statuses = statuses;
                this.dueDate = dueDate;
	}
	
	@Override
	public List<GroupAuditTask> execute(Context context) {
		TaskPersistenceContext persistenceContext = ((TaskContext) context).getPersistenceContext();
		return persistenceContext.queryWithParametersInTransaction("getAllGroupAuditTasksByStatusByDueDate", 
				persistenceContext.addParametersToMap("groupIds", groupIds, "statuses", statuses, "dueDate", dueDate),
				ClassUtil.<List<GroupAuditTask>>castClass(List.class));
	}

}
