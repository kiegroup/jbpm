package org.jbpm.services.task.persistence.query;

import java.util.ArrayList;
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

public class TaskAsPotentialOwnerByGroupQuery implements MapDBQuery<List<TaskSummary>> {

	/* select distinct new TaskSummaryImpl(...)
       from TaskImpl t join t.peopleAssignments.potentialOwners potentialOwners 
       where t.archived = 0 and t.taskData.actualOwner = null 
       and t.taskData.status in ('Created', 'Ready', 'Reserved', 'InProgress', 'Suspended') and 
       potentialOwners.id = :groupId order by t.id DESC
    */
	@Override
	public List<TaskSummary> execute(UserGroupCallback callback, 
			Map<String, Object> params, TaskTableService tts, boolean singleResult) {
		List<Status> status = Arrays.asList(Status.Created, Status.Ready, 
				Status.Reserved, Status.InProgress, Status.Suspended);
		String groupId = (String) params.get("groupId");
		
		Set<Long> values = new HashSet<>();
		MapDBQueryUtil.addAll(values, tts.getByPotentialOwner(), groupId);
		cleanTasksWithActualOwners(values, tts.getById());

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
		
		final List<TaskSummary> retval = new ArrayList<TaskSummary>();
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

	private void cleanTasksWithActualOwners(Set<Long> ids, Map<Long, Task> tasks) {
		List<Long> toRemove = new LinkedList<>();
		for (Long id : ids) {
			Task t = tasks.get(id);
			if (t.getTaskData().getActualOwner() != null) {
				toRemove.add(id);
			}
		}
		ids.removeAll(toRemove);
	}
}
