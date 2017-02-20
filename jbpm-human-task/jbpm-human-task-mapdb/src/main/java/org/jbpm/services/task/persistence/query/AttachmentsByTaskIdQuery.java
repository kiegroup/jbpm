package org.jbpm.services.task.persistence.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbpm.services.task.persistence.index.TaskTableService;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.Attachment;
import org.kie.api.task.model.Task;

public class AttachmentsByTaskIdQuery implements MapDBQuery<List<Attachment>> {

	@Override
	public List<Attachment> execute(UserGroupCallback callback,
			Map<String, Object> params, TaskTableService tts,
			boolean singleResult) {
		Long taskId = (Long) params.get("taskId");
		if (tts.getById().containsKey(taskId)) {
			Task task = tts.getById().get(taskId);
			return MapDBQueryUtil.paging(params, task.getTaskData().getAttachments());
		}
		return new ArrayList<>();
	}
}
