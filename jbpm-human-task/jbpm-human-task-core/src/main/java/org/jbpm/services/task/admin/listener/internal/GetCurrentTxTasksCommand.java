package org.jbpm.services.task.admin.listener.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.jbpm.services.task.commands.TaskCommand;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.command.Context;
import org.kie.internal.command.ProcessInstanceIdCommand;

@XmlRootElement(name="get-current-tx-tasks-command")
@XmlAccessorType(XmlAccessType.NONE)
public class GetCurrentTxTasksCommand extends TaskCommand<List<TaskSummary>> implements ProcessInstanceIdCommand {

    /** Generated serial version UID */
	private static final long serialVersionUID = 6474368266134150938L;

	@XmlElement(required=true)
	@XmlSchemaType(name="long")
	private Long processInstanceId;
	
	public GetCurrentTxTasksCommand() {
	   // default constructor 
	}
	
	public GetCurrentTxTasksCommand(Long processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	@Override
	public Long getProcessInstanceId() {
        return processInstanceId;
    }

	@Override
    public void setProcessInstanceId( Long processInstanceId ) {
        this.processInstanceId = processInstanceId;
    }

    @SuppressWarnings("unchecked")
	@Override
	public List<TaskSummary> execute(Context context) {
		List<TaskSummary> tasks = new ArrayList<TaskSummary>();
		Set<TaskSummary> tasksToRemove = (Set<TaskSummary>) context.get("local:current-tasks");
        if (tasksToRemove != null) {
        	for (TaskSummary task : tasksToRemove) {
        		if (task.getProcessInstanceId() == processInstanceId) {
        			tasks.add(task);
        		}
        	}
        }
        return tasks;
	}
	
}
