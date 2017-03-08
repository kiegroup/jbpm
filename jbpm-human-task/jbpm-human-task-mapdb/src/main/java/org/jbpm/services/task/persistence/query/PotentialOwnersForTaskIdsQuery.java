package org.jbpm.services.task.persistence.query;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jbpm.services.task.persistence.index.TaskTableService;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Task;

public class PotentialOwnersForTaskIdsQuery implements MapDBQuery<List<Object[]>> {

	@Override
	public List<Object[]> execute(UserGroupCallback callback,
			Map<String, Object> params, TaskTableService tts,
			boolean singleResult) {
		@SuppressWarnings("unchecked")
		List<Long> taskIds = (List<Long>) params.get("taskIds");
		List<Object[]> retval = new LinkedList<>();
		for (Long id : taskIds) {
			if (tts.getById().containsKey(id)) {
				Task task = tts.getById().get(id);
				if (task != null) {
					for (OrganizationalEntity entity : task.getPeopleAssignments().getPotentialOwners()) {
						retval.add(new Object[] {id, entity});
					}
				}
			}
		}
		return MapDBQueryUtil.paging(params, retval);
	}

}
