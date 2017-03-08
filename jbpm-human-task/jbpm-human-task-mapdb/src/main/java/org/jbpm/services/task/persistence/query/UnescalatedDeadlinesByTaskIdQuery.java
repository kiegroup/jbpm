package org.jbpm.services.task.persistence.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbpm.services.task.persistence.index.TaskTableService;
import org.jbpm.services.task.query.DeadlineSummaryImpl;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.model.Deadline;
import org.kie.internal.task.api.model.DeadlineSummary;
import org.kie.internal.task.api.model.Deadlines;
import org.kie.internal.task.api.model.InternalTask;

public class UnescalatedDeadlinesByTaskIdQuery implements MapDBQuery<List<DeadlineSummary>> {
	
	private boolean isEndDeadlines;

	public UnescalatedDeadlinesByTaskIdQuery(boolean isEndDeadlines) {
		this.isEndDeadlines = isEndDeadlines;
	}

	@Override
	public List<DeadlineSummary> execute(UserGroupCallback callback,
			Map<String, Object> params, TaskTableService tts,
			boolean singleResult) {
		Long taskId = (Long) params.get("taskId");
		List<DeadlineSummary> retval = new ArrayList<>();
		if (tts.getById().containsKey(taskId)) {
			Task task = tts.getById().get(taskId);
			Deadlines d = ((InternalTask) task).getDeadlines();
			if (d != null) {
				List<Deadline> list = isEndDeadlines ? d.getEndDeadlines() : d.getStartDeadlines();
				if (list != null) {
					for (Deadline dl : list) {
						retval.add(new DeadlineSummaryImpl(taskId, dl.getId(), dl.getDate()));
					}
				}
			}
		}
		return MapDBQueryUtil.paging(params, retval);
	}

}
