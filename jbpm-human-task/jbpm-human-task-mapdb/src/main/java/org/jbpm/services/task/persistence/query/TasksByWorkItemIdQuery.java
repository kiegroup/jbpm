package org.jbpm.services.task.persistence.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpm.services.task.persistence.index.TaskTableService;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.Task;

public class TasksByWorkItemIdQuery implements MapDBQuery<List<Task>> {

	@Override
	public List<Task> execute(UserGroupCallback callback,
			Map<String, Object> params, TaskTableService tts,
			boolean singleResult) {
		Long workItemId = (Long) params.get("workItemId");
		Set<Long> values = new HashSet<>();
		MapDBQueryUtil.addAll(values, tts.getByWorkItemId(), workItemId);
		List<Task> retval = new ArrayList<>();
		for (long id : values) {
			if (tts.getById().containsKey(id)) {
				Task task = tts.getById().get(id);
				if (task != null) {
					retval.add(task);
				}
			}
		}
		return MapDBQueryUtil.paging(params, retval);
	}
}
