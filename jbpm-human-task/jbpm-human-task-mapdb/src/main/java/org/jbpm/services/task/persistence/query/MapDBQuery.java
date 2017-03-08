package org.jbpm.services.task.persistence.query;

import java.util.Map;

import org.jbpm.services.task.persistence.index.TaskTableService;
import org.kie.api.task.UserGroupCallback;

public interface MapDBQuery<R> {

	R execute(UserGroupCallback callback, Map<String, Object> params, TaskTableService tts, boolean singleResult);
}
