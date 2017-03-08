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

public class SubTasksByPotentialOwnerQuery implements MapDBQuery<List<TaskSummary>> {

	@Override
	public List<TaskSummary> execute(UserGroupCallback callback,
			Map<String, Object> params, TaskTableService tts,
			boolean singleResult) {
		Long parentId = (Long) params.get("parentId");
		String userId = (String) params.get("userId");
		List<Status> status = Arrays.asList(Status.Created, Status.Ready, 
				Status.Reserved, Status.InProgress, Status.Suspended);
		@SuppressWarnings("unchecked")
		List<String> groupIds = (List<String>) params.get("groupIds");

		Set<Long> ids = new HashSet<>();
		MapDBQueryUtil.addAll(ids, tts.getByActualOwner(), userId);
		MapDBQueryUtil.addAll(ids, tts.getByPotentialOwner(), userId);
		for (String groupId : groupIds) {
			MapDBQueryUtil.addAll(ids, tts.getByActualOwner(), groupId);
			MapDBQueryUtil.addAll(ids, tts.getByPotentialOwner(), groupId);
		}
		MapDBQueryUtil.removeAll(ids, tts.getByExclOwner(), userId);
		
		Set<Long> idsByParent = new HashSet<>();
		MapDBQueryUtil.addAll(idsByParent, tts.getByParentId(), parentId);
		
		ids.retainAll(idsByParent);
		
		Set<Long> idsByStatus = new HashSet<>();
		if (status != null) {
            List<String> strStatus = MapDBQueryUtil.asStringStatus(status);
		    for (Long value : ids) {
			    String taskStatus = tts.getTaskStatusById().get(value);
			    if (taskStatus != null && strStatus.contains(taskStatus)) {
				    idsByStatus.add(value);
			    }
		    }
		    ids.retainAll(idsByStatus); //and operation
		}
		
		List<TaskSummary> retval = new ArrayList<>(ids.size());
		for (Long id : ids) {
			if (tts.getById().containsKey(id)) {
				Task task = tts.getById().get(id);
				if (task != null) {
					retval.add(new TaskSummaryImpl(task));
				}
			}
		}
		return MapDBQueryUtil.paging(params, retval);
	}
}
