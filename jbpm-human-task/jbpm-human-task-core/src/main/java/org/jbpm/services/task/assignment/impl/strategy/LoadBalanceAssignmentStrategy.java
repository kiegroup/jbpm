package org.jbpm.services.task.assignment.impl.strategy;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jbpm.services.task.assignment.LoadCalculator;
import org.jbpm.services.task.assignment.UserTaskLoad;
import org.jbpm.services.task.assignment.impl.LoadCalculatorImpl;
import org.jbpm.services.task.assignment.impl.TaskCountLoadCalculator;
import org.jbpm.services.task.utils.ClassUtil;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.task.TaskContext;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.UserInfo;
import org.kie.internal.task.api.assignment.Assignment;
import org.kie.internal.task.api.assignment.AssignmentStrategy;
import org.kie.internal.task.api.model.InternalPeopleAssignments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class LoadBalanceAssignmentStrategy implements AssignmentStrategy {
	private static final Logger logger = LoggerFactory.getLogger(LoadBalanceAssignmentStrategy.class);
	private static final String IDENTIFIER = "LoadBalance";
	private Cache<User, UserTaskLoad> userTaskLoadsCache;
	private LoadCalculator calculator;
	
	public LoadBalanceAssignmentStrategy(LoadCalculator calculator) {
		this.calculator = new TaskCountLoadCalculator();
		userTaskLoadsCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).build();
	}
	
	private Function<OrganizationalEntity, User> entityToUser = (oe) -> {
		return (User)oe;
	};
	

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public Assignment apply(Task task, TaskContext taskContext, String excludedUser) {
        List<OrganizationalEntity> excluded = ((InternalPeopleAssignments)task.getPeopleAssignments()).getExcludedOwners();
        UserInfo userInfo = (UserInfo) ((org.jbpm.services.task.commands.TaskContext)taskContext).get(EnvironmentName.TASK_USER_INFO);

        // Get the the users from the task's the potential owners
        List<OrganizationalEntity> potentialOwners = task.getPeopleAssignments().getPotentialOwners().stream()
                .filter(oe -> oe instanceof User && !excluded.contains(oe))
                .collect(Collectors.toList());

        // Get the users belonging to groups that are potential owners
        task.getPeopleAssignments().getPotentialOwners().stream().filter(oe -> oe instanceof Group)
                .forEach(oe -> {
                    Iterator<OrganizationalEntity> groupUsers = userInfo.getMembersForGroup((Group)oe);
                    if (groupUsers != null) {
                        groupUsers.forEachRemaining(user -> {
                            if (user != null && !excluded.contains(user) && !potentialOwners.contains(user)) {
                                potentialOwners.add(user);
                            }
                        });
                    }
                });
        List<User> missingUsers = potentialOwners.stream()
        		.filter(po -> !userTaskLoadsCache.asMap().containsKey(po))
        		.map(entityToUser)	// On the fly conversion 
        		.collect(Collectors.toList());
        if (missingUsers != null && !missingUsers.isEmpty()) {
        	if (missingUsers.size() == 1) {
        		UserTaskLoad load = calculator.getUserTaskLoad((User)missingUsers.get(0), taskContext);
        		userTaskLoadsCache.put(load.getUser(), load);
        	} else {
        		Collection<UserTaskLoad> loads = calculator.getUserTaskLoads(missingUsers, taskContext);
        		loads.forEach(l -> {userTaskLoadsCache.put(l.getUser(), l);});
        	}
        }
        UserTaskLoad lightestLoad = getLightestLoad(potentialOwners.stream().map(entityToUser).collect(Collectors.toList()));
		return lightestLoad != null ? new Assignment(lightestLoad.getUser().getId()):null;
	}
	
	private synchronized UserTaskLoad getLightestLoad(List<User> users) {
		UserTaskLoad lightestLoad = null;
		Collection<UserTaskLoad> loads = userTaskLoadsCache.asMap().values();
		Collection<UserTaskLoad> userLoads = loads.stream().filter(taskLoad -> users.contains(taskLoad.getUser())).collect(Collectors.toList());
		lightestLoad = userLoads.stream().min(UserTaskLoad::compareTo).orElse(null);
		return lightestLoad;
	}

}
