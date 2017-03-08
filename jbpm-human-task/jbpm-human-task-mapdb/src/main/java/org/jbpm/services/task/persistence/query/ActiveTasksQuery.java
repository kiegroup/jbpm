package org.jbpm.services.task.persistence.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpm.services.task.persistence.index.TaskTableService;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;

public class ActiveTasksQuery implements MapDBQuery<List<Task>> {

	@Override
	public List<Task> execute(UserGroupCallback callback,
			Map<String, Object> params, TaskTableService tts,
			boolean singleResult) {
		String[] activeStatuses = new String[] {
				Status.Suspended.name(), Status.Created.name(), 
				Status.Ready.name(), Status.Reserved.name(),
				Status.InProgress.name() };
		Set<Long> ids = new HashSet<>();
		for (String status : activeStatuses) {
			MapDBQueryUtil.addAll(ids, tts.getByStatus(), status);
		}
		List<Task> retval = new ArrayList<>(ids.size());
		for (Long id : ids) {
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
