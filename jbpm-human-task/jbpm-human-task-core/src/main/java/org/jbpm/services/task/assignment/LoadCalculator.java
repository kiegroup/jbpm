package org.jbpm.services.task.assignment;

import java.util.Collection;
import java.util.List;

import org.kie.api.task.TaskContext;
import org.kie.api.task.model.User;

public interface LoadCalculator {
	public String getIdentifier();
	public UserTaskLoad getUserTaskLoad(User user, TaskContext context);
	public Collection<UserTaskLoad> getUserTaskLoads(List<User> users, TaskContext context);
}
