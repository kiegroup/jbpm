package org.jbpm.services.task.persistence.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpm.services.task.persistence.index.TaskTableService;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;

public class TasksByStatusAndProcessInstanceIdQuery implements MapDBQuery<List<TaskSummary>> {
    
	@Override
	public List<TaskSummary> execute(UserGroupCallback callback,
			Map<String, Object> params, TaskTableService tts,
			boolean singleResult) {
		@SuppressWarnings("unchecked")
		List<Status> status = (List<Status>) params.get("status");
		Long processInstanceId = (Long) params.get("processInstanceId");
		String taskName = (String) params.get("taskName");
		Set<Long> idsByProcess = new HashSet<>();
		MapDBQueryUtil.addAll(idsByProcess, tts.getByProcessInstanceId(), processInstanceId);
		
		Set<Long> idsByStatus = new HashSet<>();
		if (status != null) {
            List<String> strStatus = MapDBQueryUtil.asStringStatus(status);
		    for (Long value : idsByProcess) {
			    String taskStatus = tts.getTaskStatusById().get(value);
			    if (taskStatus != null && strStatus.contains(taskStatus)) {
				    idsByStatus.add(value);
			    }
		    }
		    idsByProcess.retainAll(idsByStatus); //and operation
		}
		
		List<TaskSummary> retval = new ArrayList<>(idsByProcess.size());
		for (Long id : idsByProcess) {
			Task task = tts.getById().get(id);
			if (matchesCondition(task, taskName)) {
				retval.add(new TaskSummaryImpl(task));
			}
		}
		return MapDBQueryUtil.paging(params, retval);
	}

	private boolean matchesCondition(Task task, String taskName) {
		if (taskName != null) {
			return taskName.equalsIgnoreCase(task.getName());
		}
		return true;
	}
}
