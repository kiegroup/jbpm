/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.services.task.assignment.impl.strategy;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class RoundRobinAssignmentStrategy implements AssignmentStrategy {
    private static final Logger logger = LoggerFactory.getLogger(RoundRobinAssignmentStrategy.class);
    private static final String IDENTIFIER = "RoundRobin";

    private boolean active = IDENTIFIER.equals(System.getProperty("org.jbpm.task.assignment.strategy"));

    private Map<String,CircularQueue> circularQueueMap = new ConcurrentHashMap();

    private class CircularQueue<T> extends LinkedBlockingQueue<T> {
        @Override
        public T take() {
            T headValue = null;
            try {
                headValue = super.take();
                super.offer(headValue);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return headValue;
        }
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public Assignment apply(Task task, TaskContext taskContext, String s) {
        List<OrganizationalEntity> excluded = ((InternalPeopleAssignments)task.getPeopleAssignments()).getExcludedOwners();
        UserInfo userInfo = (UserInfo) ((org.jbpm.services.task.commands.TaskContext)taskContext).get(EnvironmentName.TASK_USER_INFO);

        // Get the the users from the task's the potential owners
        List<OrganizationalEntity> potentialOwners = task.getPeopleAssignments().getPotentialOwners().parallelStream()
                .filter(oe -> oe instanceof User && !excluded.contains(oe))
                .collect(Collectors.toList());

        // Get the users belonging to groups that are potential owners
        potentialOwners.stream().filter(oe -> oe instanceof Group)
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
        String queueName = getQueueName(task);
        CircularQueue<OrganizationalEntity> mappedQueue = null;
        // If a queue already exists for this task then its contents should be synchronized with the
        // current list of potential owners
        if (circularQueueMap.containsKey(queueName)) {
            mappedQueue = circularQueueMap.get(queueName);
        } else {
            CircularQueue<OrganizationalEntity> queue = new CircularQueue();
            potentialOwners.forEach(po -> {queue.add(po);});
            circularQueueMap.put(queueName,queue);
            mappedQueue = queue;
        }
        OrganizationalEntity owner = mappedQueue.take();
        return new Assignment(owner.getId());
    }

    protected String getQueueName(Task task) {
        return task.getTaskData().getDeploymentId()+"_"+task.getName();
    }
}
