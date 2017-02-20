package org.jbpm.services.task.persistence.query;

import java.util.ArrayList;
import java.util.Arrays;
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

public class TaskAsPotentialOwnerQuery implements MapDBQuery<List<TaskSummary>> {

	/*
	 * select distinct new TaskSummaryImpl(...)
       from TaskImpl t join t.peopleAssignments.potentialOwners potentialOwners
       left join t.peopleAssignments.excludedOwners as excludedOwners
       where t.archived = 0 and
       (t.taskData.actualOwner.id = :userId or t.taskData.actualOwner is null) and
       t.taskData.status in (:status) and 
       (potentialOwners.id  = :userId or potentialOwners.id in (:groupIds)) and 
       (t.peopleAssignments.excludedOwners is empty or excludedOwners.id != :userId)           
	 */
	
	@Override
	public List<TaskSummary> execute(UserGroupCallback callback, 
			Map<String, Object> params, TaskTableService tts, boolean singleResult) {
		@SuppressWarnings("unchecked")
		List<Status> status = (List<Status>) params.get("status");
		if (status == null || status.isEmpty()) {
			status = Arrays.asList(Status.Created, Status.Ready, Status.Reserved, 
					Status.InProgress, Status.Suspended);
		}
		final String userId = (String) params.get("userId");
		@SuppressWarnings("unchecked")
		List<String> groupIds = (List<String>) params.get("groupIds");
		if (groupIds == null || groupIds.isEmpty()) {
			groupIds = callback.getGroupsForUser(userId);
		}
		tts.validateIsUser(userId);
		Set<Long> values = new HashSet<>();
		MapDBQueryUtil.addAll(values, tts.getByActualOwner(), userId);
		//MapDBQueryUtil.addAll(values, tts.getByBizAdmin(), userId);
		MapDBQueryUtil.addAll(values, tts.getByPotentialOwner(), userId);
		for (String groupId : groupIds) {
			//MapDBQueryUtil.addAll(values, tts.getByBizAdmin(), groupId);
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
}
