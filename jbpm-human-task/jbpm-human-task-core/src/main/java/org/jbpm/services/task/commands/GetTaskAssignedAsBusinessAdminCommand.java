package org.jbpm.services.task.commands;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.kie.api.task.model.TaskSummary;
import org.kie.internal.command.Context;
import org.kie.api.task.model.Status;
@XmlRootElement(name = "get-tasks-assigned-bus-admin-command")
@XmlAccessorType(XmlAccessType.NONE)
public class GetTaskAssignedAsBusinessAdminCommand extends UserGroupCallbackTaskCommand<List<TaskSummary>> {
    @XmlElement
    private List<Status> status;
    private static final long serialVersionUID = -128903115964900028L;

    public GetTaskAssignedAsBusinessAdminCommand() {
    }
    
    public GetTaskAssignedAsBusinessAdminCommand(String userId) {
        this.userId = userId;

    }
    public GetTaskAssignedAsBusinessAdminCommand(String userId, List<Status> status) {
        this.userId = userId;
        this.status=status;
    }

    public List<TaskSummary> execute(Context cntxt) {
        TaskContext context = (TaskContext) cntxt;
        doCallbackUserOperation(userId, context);
        doUserGroupCallbackOperation(userId, null, context);
        if (status == null && status.size()==0){
            return context.getTaskQueryService().getTasksAssignedAsBusinessAdministrator(userId);
        }
        return context.getTaskQueryService().getTasksAssignedAsBusinessAdministratorByStatus(userId,status);
    }

}
