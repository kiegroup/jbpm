package org.jbpm.services.task.persistence.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbpm.services.task.persistence.index.TaskTableService;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.Task;

public class AllTasksQuery implements MapDBQuery<List<Task>> {

	@Override
	public List<Task> execute(UserGroupCallback callback,
			Map<String, Object> params, TaskTableService tts,
			boolean singleResult) {
		List<Task> retval = new ArrayList<>(tts.getById().values());
		return MapDBQueryUtil.paging(params, retval);
	}

}
