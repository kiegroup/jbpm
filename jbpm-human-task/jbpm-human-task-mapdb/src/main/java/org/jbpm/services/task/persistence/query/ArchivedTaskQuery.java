package org.jbpm.services.task.persistence.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jbpm.services.task.persistence.index.TaskTableService;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;

public class ArchivedTaskQuery implements MapDBQuery<List<TaskSummary>> {

	@Override
	public List<TaskSummary> execute(UserGroupCallback callback,
			Map<String, Object> params, TaskTableService tts,
			boolean singleResult) {
		Collection<Task> colOfTasks = tts.getByArchived().values();
		List<TaskSummary> retval = new ArrayList<>(colOfTasks.size());
		for (Task task : colOfTasks) {
			retval.add(new TaskSummaryImpl(task));
		}
		return MapDBQueryUtil.paging(params, retval);
	}

}
