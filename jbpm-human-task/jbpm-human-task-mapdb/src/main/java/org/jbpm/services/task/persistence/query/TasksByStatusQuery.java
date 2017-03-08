package org.jbpm.services.task.persistence.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpm.services.task.persistence.index.TaskTableService;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;

public class TasksByStatusQuery implements MapDBQuery<List<TaskSummary>> {

	@Override
	public List<TaskSummary> execute(UserGroupCallback callback,
			Map<String, Object> params, TaskTableService tts,
			boolean singleResult) {
		List<Status> status = null;
		Object objStatus = params.get("status");
		if (objStatus instanceof Status) {
			status = Arrays.asList((Status) objStatus);
		} else {
			@SuppressWarnings("unchecked")
			Collection<Status> objStatus2 = (Collection<Status>) objStatus;
			status = new ArrayList<>(objStatus2);
		}
		Date since = (Date) params.get("since");
		Set<Long> idsByStatus = new HashSet<>();
		for (Status s : status) {
			MapDBQueryUtil.addAll(idsByStatus, tts.getByStatus(), s.name());
		}
		List<TaskSummary> retval = new ArrayList<>(idsByStatus.size());
		for (long id : idsByStatus) {
			retval.add(new TaskSummaryImpl(tts.getById().get(id)));
		}
		if (since != null) {
			retval = filter(retval, since);
		}
		return MapDBQueryUtil.paging(params, retval);
	}

	private List<TaskSummary> filter(List<TaskSummary> list, Date since) {
		List<TaskSummary> retval = new ArrayList<>();
		for (TaskSummary s : list) {
			if (s.getActivationTime() != null && s.getActivationTime().after(since)) {
				retval.add(s);
			}
		}
		return retval;
	}
}
