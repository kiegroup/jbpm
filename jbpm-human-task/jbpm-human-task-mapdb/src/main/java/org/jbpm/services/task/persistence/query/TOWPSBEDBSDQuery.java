package org.jbpm.services.task.persistence.query;

import java.util.ArrayList;
import java.util.Date;
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

public class TOWPSBEDBSDQuery implements MapDBQuery<List<TaskSummary>> {

	@Override
	public List<TaskSummary> execute(UserGroupCallback callback,
			Map<String, Object> params, TaskTableService tts,
			boolean singleResult) {
		Date date = (Date) params.get("date");
		@SuppressWarnings("unchecked")
		List<Status> status = (List<Status>) params.get("status");
		String userId = (String) params.get("userId");
	
		Set<Long> values = new HashSet<>();
		MapDBQueryUtil.addAll(values, tts.getByActualOwner(), userId);
		MapDBQueryUtil.addAll(values, tts.getByPotentialOwner(), userId);

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
				if (matchesCondition(date, task)) {
					retval.add(new TaskSummaryImpl(task));
				}
			}
		}
		return MapDBQueryUtil.paging(params, retval);
	}

	private boolean matchesCondition(Date date, Task task) {
		Date expTime = task.getTaskData().getExpirationTime();
		return date != null && date.after(expTime);
	}
}
