package org.jbpm.services.task.persistence.query;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpm.services.task.persistence.index.TaskTableService;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;

public class TasksAsPotentialOwnerByGroupsWithExclusionQuery implements MapDBQuery<List<TaskSummary>> {

	@Override
	public List<TaskSummary> execute(UserGroupCallback callback,
			Map<String, Object> params, TaskTableService tts,
			boolean singleResult) {
		@SuppressWarnings("unchecked")
		List<String> groupIds = (List<String>) params.get("groupIds");
		@SuppressWarnings("unchecked")
		List<Status> status = (List<Status>) params.get("status");
		String userId = (String) params.get("userId");
		if (status == null) {
			status = Arrays.asList(
				Status.Created, Status.Ready, Status.Reserved, 
				Status.InProgress, Status.Suspended);
		}
		Set<Long> values = new HashSet<>();
		MapDBQueryUtil.addAll(values, tts.getByActualOwner(), userId);
		MapDBQueryUtil.addAll(values, tts.getByPotentialOwner(), userId);
		for (String groupId : groupIds) {
			MapDBQueryUtil.addAll(values, tts.getByPotentialOwner(), groupId);
		}
		MapDBQueryUtil.removeAll(values, tts.getByExclOwner(), userId);
		
		Set<Long> valuesByStatus = new HashSet<>();
		if (status != null) {
            List<String> strStatus = MapDBQueryUtil.asStringStatus(status);
		    for (Long value : values) {
			    String taskStatus = tts.getTaskStatusById().get(value);
			    if (taskStatus != null && strStatus.contains(taskStatus)) {
				    valuesByStatus.add(value);
			    }
		    }
		    values.retainAll(valuesByStatus); //and operation
		}

		
		for (String owner : tts.getByActualOwner().keySet()) {
			MapDBQueryUtil.addAll(values, tts.getByActualOwner(), owner);
		}
		
		List<TaskSummary> retval = new LinkedList<TaskSummary>();
		
		for (Long taskId : values) {
			if (tts.getById().containsKey(taskId)) {
				Task task = tts.getById().get(taskId);
				if (task != null) {
					retval.add(new TaskSummaryImpl(task));
				}
			}
		}
		return MapDBQueryUtil.paging(params, retval);
	}
}
