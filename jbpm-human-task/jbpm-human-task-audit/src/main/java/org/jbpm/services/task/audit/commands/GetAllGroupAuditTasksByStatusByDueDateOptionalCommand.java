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

@XmlRootElement(name="get-all-group-audit-tasks-bystatusbyduedateoptional-command")
@XmlAccessorType(XmlAccessType.NONE)
public class GetAllGroupAuditTasksByStatusByDueDateOptionalCommand extends TaskCommand<List<GroupAuditTask>> {
        private String groupIds;
        private String status;
        private Date dueDate;
	public GetAllGroupAuditTasksByStatusByDueDateOptionalCommand() {
		
	}
	
	public GetAllGroupAuditTasksByStatusByDueDateOptionalCommand(String groupIds, String status, Date dueDate) {
		this.groupIds = groupIds;
                this.status = status;
                this.dueDate = dueDate;
	}
	
	@Override
	public List<GroupAuditTask> execute(Context context) {
		TaskPersistenceContext persistenceContext = ((TaskContext) context).getPersistenceContext();
		return persistenceContext.queryWithParametersInTransaction("getAllGroupAuditTasksByStatusByDueDateOptional", 
				persistenceContext.addParametersToMap("groupIds", groupIds, "status", status, "dueDate", dueDate),
				ClassUtil.<List<GroupAuditTask>>castClass(List.class));
	}

}
