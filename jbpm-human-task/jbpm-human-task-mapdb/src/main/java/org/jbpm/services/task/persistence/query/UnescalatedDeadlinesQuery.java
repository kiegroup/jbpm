package org.jbpm.services.task.persistence.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpm.services.task.persistence.index.TaskTableService;
import org.jbpm.services.task.query.DeadlineSummaryImpl;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.model.Deadline;
import org.kie.internal.task.api.model.DeadlineSummary;
import org.kie.internal.task.api.model.Deadlines;
import org.kie.internal.task.api.model.InternalTask;

public class UnescalatedDeadlinesQuery implements MapDBQuery<List<DeadlineSummary>> {

	private boolean isEndDeadlines;

	public UnescalatedDeadlinesQuery(boolean isEndDeadlines) {
		this.isEndDeadlines = isEndDeadlines;
	}

	@Override
	public List<DeadlineSummary> execute(UserGroupCallback callback,
			Map<String, Object> params, TaskTableService tts,
			boolean singleResult) {
		Set<Long> ids = new HashSet<>();
		for (long[] taskIds : tts.getByDeadlineId().values()) {
			addAll(ids, taskIds);
		}
		List<DeadlineSummary> retval = new ArrayList<>();
		for (Long taskId : ids) {
			if (tts.getById().containsKey(taskId)) {
				Task task = tts.getById().get(taskId);
				Deadlines d = ((InternalTask) task).getDeadlines();
				List<Deadline> list = isEndDeadlines ? d.getEndDeadlines() : d.getStartDeadlines();
				for(Deadline dl : list) {
					if (!dl.isEscalated()) {
						retval.add(new DeadlineSummaryImpl(taskId, dl.getId(), dl.getDate()));
					}
				}
			}
		}
		return MapDBQueryUtil.paging(params, retval);
	}

	private void addAll(Set<Long> values, long[] v) {
		if (v != null) {
			for (long value : v) {
				values.add(value);
			}
		}
	}

}
