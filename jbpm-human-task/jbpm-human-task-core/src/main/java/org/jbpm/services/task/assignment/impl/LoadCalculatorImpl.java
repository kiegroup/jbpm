package org.jbpm.services.task.assignment.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.services.task.assignment.LoadCalculator;
import org.jbpm.services.task.assignment.UserTaskLoad;
import org.kie.api.task.model.User;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public abstract class LoadCalculatorImpl implements LoadCalculator {
	private static final Cache<User, UserTaskLoad> cachedTaskLoads = CacheBuilder.newBuilder().build();
	

	protected void addUserTaskLoad(UserTaskLoad taskLoad) {
		if (taskLoad != null)
			cachedTaskLoads.put(taskLoad.getUser(), taskLoad);
	}
	
	protected void addUserTaskLoads(Collection<UserTaskLoad> taskLoads) {
		taskLoads.forEach(utl -> {cachedTaskLoads.put(utl.getUser(), utl);});
	}
	
	protected UserTaskLoad getTaskLoadFromCache(User user) {
		UserTaskLoad taskLoad = cachedTaskLoads.getIfPresent(user);
		if (taskLoad == null) {
			taskLoad = this.calculateUserTaskLoad(user);
			addUserTaskLoad(taskLoad);
		}
		return taskLoad;
	}
	
	protected Map<User,UserTaskLoad> getTaskLoadsFromCache(List<User> users) {
		Map<User,UserTaskLoad> taskLoads = new HashMap();
		users.forEach(u -> {
			UserTaskLoad taskLoad = getTaskLoadFromCache(u);
			if (taskLoad != null) {
				taskLoads.put(u,taskLoad);
			}
		});
		return taskLoads;
	}
	
	protected UserTaskLoad getLightestLoad() {
		UserTaskLoad taskLoad = null;
		
		return taskLoad;
	}
}
