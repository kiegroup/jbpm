package org.jbpm.services.task.persistence.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpm.services.task.persistence.index.TaskTableService;
import org.kie.api.task.UserGroupCallback;

public class TaskIdByProcessIdQuery implements MapDBQuery<List<Long>> {

	@Override
	public List<Long> execute(UserGroupCallback callback,
			Map<String, Object> params, TaskTableService tts,
			boolean singleResult) {
		Long processInstanceId = (Long) params.get("processInstanceId");
		Set<Long> values = new HashSet<>();
		MapDBQueryUtil.addAll(values, tts.getByProcessInstanceId(), processInstanceId);
		return MapDBQueryUtil.paging(params, new ArrayList<>(values));
	}

}
