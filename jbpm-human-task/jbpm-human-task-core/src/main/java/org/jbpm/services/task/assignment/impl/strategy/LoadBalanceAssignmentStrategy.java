package org.jbpm.services.task.assignment.impl.strategy;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jbpm.services.task.assignment.LoadCalculator;
import org.jbpm.services.task.assignment.UserTaskLoad;
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

public class LoadBalanceAssignmentStrategy implements AssignmentStrategy {
	private static final Logger logger = LoggerFactory.getLogger(LoadBalanceAssignmentStrategy.class);
	private static final String IDENTIFIER = "LoadBalance";
	private LoadCalculator calculator;
	
	public LoadBalanceAssignmentStrategy() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		String calculatorClass = System.getProperty("org.jbpm.task.assignment.loadbalance.calculator","org.jbpm.services.task.assignment.impl.TaskCountLoadCalculator");
		this.calculator = (LoadCalculator)Class.forName(calculatorClass).newInstance();
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
        logger.info("Asking the load calculator [{}] for task loads for the users {}",calculator.getIdentifier(),potentialOwners);
        List<User> users = potentialOwners.stream().map(entityToUser).collect(Collectors.toList());
        Collection<UserTaskLoad> loads = calculator.getUserTaskLoads(users, taskContext);
        UserTaskLoad lightestLoad = loads.stream().min(UserTaskLoad::compareTo).orElse(null);
		return lightestLoad != null ? new Assignment(lightestLoad.getUser().getId()):null;
	}

}
