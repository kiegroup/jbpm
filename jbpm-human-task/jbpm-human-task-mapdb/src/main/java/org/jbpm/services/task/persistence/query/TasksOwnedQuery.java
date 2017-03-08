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
import org.kie.api.task.model.TaskSummary;

public class TasksOwnedQuery implements MapDBQuery<List<TaskSummary>> {

	@Override
	public List<TaskSummary> execute(UserGroupCallback callback,
			Map<String, Object> params, TaskTableService tts,
			boolean singleResult) {
		@SuppressWarnings("unchecked")
		List<Status> status = (List<Status>) params.get("status");
		String userId = (String) params.get("userId");
		
		Set<Long> idsByOwner = new HashSet<>();
		MapDBQueryUtil.addAll(idsByOwner, tts.getByActualOwner(), userId);
		
		Set<Long> idsByStatus = new HashSet<>();
		if (status != null) {
            List<String> strStatus = MapDBQueryUtil.asStringStatus(status);
		    for (Long value : idsByOwner) {
			    String taskStatus = tts.getTaskStatusById().get(value);
			    if (taskStatus != null && strStatus.contains(taskStatus)) {
				    idsByStatus.add(value);
			    }
		    }
		    idsByOwner.retainAll(idsByStatus); //and operation
		}
		
		List<TaskSummary> retval = new ArrayList<>(idsByOwner.size());
		for (Long id : idsByOwner) {
			retval.add(new TaskSummaryImpl(tts.getById().get(id)));
		}
		
		return MapDBQueryUtil.paging(params, retval);
	}

}
