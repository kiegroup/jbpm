package org.jbpm.services.task.assignment;

import java.util.Collection;
import java.util.List;

import org.kie.api.task.model.User;

public interface LoadCalculator {
	public String getIdentifier();
	public UserTaskLoad calculateUserTaskLoad(User user);
	public Collection<UserTaskLoad> calculateUserTaskLoads(List<User> users);
}
