/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jbpm.process.workitem.wsht;

import java.util.ArrayList;
import java.util.List;

import org.drools.process.instance.WorkItem;
import org.jbpm.task.Group;
import org.jbpm.task.OrganizationalEntity;
import org.jbpm.task.PeopleAssignments;
import org.jbpm.task.Task;
import org.jbpm.task.TaskData;
import org.jbpm.task.User;

/**
 *
 */
public class PeopleAssignmentHelper {

	public void handlePeopleAssignments(WorkItem workItem, Task task, TaskData taskData) {
		
		PeopleAssignments peopleAssignments = getNullSafePeopleAssignment(task);
		
		assignActor(workItem, peopleAssignments);
		assignGroup(workItem, peopleAssignments);
		
		assignBusinessAdministrator(workItem, peopleAssignments);
		assignTaskStakeholder(workItem, peopleAssignments);
		
		task.setPeopleAssignments(peopleAssignments);
		
		List<OrganizationalEntity> potentialOwners = peopleAssignments.getPotentialOwners();
        // Set the first user as creator ID??? hmmm might be wrong
        if (potentialOwners.size() > 0) {
            taskData.setCreatedBy((User) potentialOwners.get(0));
        }
        
	}
	
	public void assignActor(WorkItem workItem, PeopleAssignments peopleAssignments) {
		
        String actorId = (String) workItem.getParameter("ActorId");        
        
        if (actorId != null && actorId.trim().length() > 0) {

            String[] actorIds = actorId.split(",");

        	List<OrganizationalEntity> potentialOwners = peopleAssignments.getPotentialOwners();
            
            for (String id : actorIds) {
                potentialOwners.add(new User(id.trim()));
            }
            
        }
        
	}
	
	public void assignGroup(WorkItem workItem, PeopleAssignments peopleAssignments) {
	
        String groupId = (String) workItem.getParameter("GroupId");
        
        if (groupId != null && groupId.trim().length() > 0) {
        	
            String[] groupIds = groupId.split(",");
            
            List<OrganizationalEntity> potentialOwners = peopleAssignments.getPotentialOwners();
            
            for (String id : groupIds) {
                potentialOwners.add(new Group(id.trim()));
            }
            
        }
        
	}
	
	public void assignBusinessAdministrator(WorkItem workItem, PeopleAssignments peopleAssignments) {
		
        List<OrganizationalEntity> businessAdministrators = new ArrayList<OrganizationalEntity>();
        businessAdministrators.add(new User("Administrator"));
        peopleAssignments.setBusinessAdministrators(businessAdministrators);
        
	}
	
	public void assignTaskStakeholder(WorkItem workItem, PeopleAssignments peopleAssignments) {
		
	}

	protected PeopleAssignments getNullSafePeopleAssignment(Task task) {
		
		PeopleAssignments peopleAssignments = task.getPeopleAssignments();
        
        if (peopleAssignments == null) {
        	
        	peopleAssignments = new PeopleAssignments();
        	
        }
        
		return peopleAssignments;
		
	}
	
	/*
	public void existingCode(WorkItem workItem, Task task, TaskData taskData) {
		
        PeopleAssignments assignments = new PeopleAssignments();
        List<OrganizationalEntity> potentialOwners = new ArrayList<OrganizationalEntity>();

        String actorId = (String) workItem.getParameter("ActorId");
        if (actorId != null && actorId.trim().length() > 0) {
            String[] actorIds = actorId.split(",");
            for (String id : actorIds) {
                potentialOwners.add(new User(id.trim()));
            }
            //Set the first user as creator ID??? hmmm might be wrong
            if (potentialOwners.size() > 0) {
                taskData.setCreatedBy((User) potentialOwners.get(0));
            }
        }

        String groupId = (String) workItem.getParameter("GroupId");
        if (groupId != null && groupId.trim().length() > 0) {
            String[] groupIds = groupId.split(",");
            for (String id : groupIds) {
                potentialOwners.add(new Group(id.trim()));
            }
        }

        assignments.setPotentialOwners(potentialOwners);
        
        List<OrganizationalEntity> businessAdministrators = new ArrayList<OrganizationalEntity>();
        businessAdministrators.add(new User("Administrator"));
        assignments.setBusinessAdministrators(businessAdministrators);
        task.setPeopleAssignments(assignments);
        
	}
	*/
	
}