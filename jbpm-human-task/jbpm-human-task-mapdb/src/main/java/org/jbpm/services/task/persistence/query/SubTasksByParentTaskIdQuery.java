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

public class SubTasksByParentTaskIdQuery implements MapDBQuery<List<TaskSummary>> {

/*
select distinct new org.jbpm.services.task.query.TaskSummaryImpl( ... )
from TaskImpl t, OrganizationalEntityImpl potentialOwners where
 t.taskData.parentId = :parentId and t.taskData.status in ('Created', 'Ready', 'Reserved', 'InProgress', 'Suspended') 
 */

	@Override
	public List<TaskSummary> execute(UserGroupCallback callback,
			Map<String, Object> params, TaskTableService tts,
			boolean singleResult) {
		Long parentId = (Long) params.get("parentId");
		List<Status> status = Arrays.asList(Status.Created, Status.Ready, 
				Status.Reserved, Status.InProgress, Status.Suspended);
		Set<Long> values = new HashSet<>();
		MapDBQueryUtil.addAll(values, tts.getByParentId(), parentId);

		Set<Long> idsByStatus = new HashSet<>();
		for (Status s : status) {
			MapDBQueryUtil.addAll(idsByStatus, tts.getByStatus(), s.name());
		}

		values.retainAll(idsByStatus);
		
		List<TaskSummary> retval = new ArrayList<>(values.size());
		for (Long id : values) {
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
