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

import org.drools.runtime.process.WorkItem;
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

	public static final String ACTOR_ID = "ActorId";
	public static final String GROUP_ID = "GroupId";
	public static final String BUSINESSADMINISTRATOR_ID = "BusinessAdministratorId";
	public static final String TASKSTAKEHOLDER_ID = "TaskStakeholderId";
	
	public void handlePeopleAssignments(WorkItem workItem, Task task, TaskData taskData) {
		
		PeopleAssignments peopleAssignments = getNullSafePeopleAssignments(task);
        
		assignActors(workItem, peopleAssignments);
		assignGroups(workItem, peopleAssignments);		
		assignBusinessAdministrators(workItem, peopleAssignments);
		assignTaskStakeholders(workItem, peopleAssignments);
		
		task.setPeopleAssignments(peopleAssignments);
		
        // Set the first user as creator ID??? hmmm might be wrong
		List<OrganizationalEntity> potentialOwners = peopleAssignments.getPotentialOwners();
        if (potentialOwners.size() > 0) {
            taskData.setCreatedBy((User) potentialOwners.get(0));
        }
        
	}
	
	public void assignActors(WorkItem workItem, PeopleAssignments peopleAssignments) {
		
        String actorIds = (String) workItem.getParameter(ACTOR_ID);        
        List<OrganizationalEntity> potentialOwners = peopleAssignments.getPotentialOwners();
        
        processPeopleAssignments(actorIds, potentialOwners, true);
        
	}
	
	public void assignGroups(WorkItem workItem, PeopleAssignments peopleAssignments) {
	
        String groupIds = (String) workItem.getParameter(GROUP_ID);
        List<OrganizationalEntity> potentialOwners = peopleAssignments.getPotentialOwners();
        
        processPeopleAssignments(groupIds, potentialOwners, false);
        
	}

	public void assignBusinessAdministrators(WorkItem workItem, PeopleAssignments peopleAssignments) {
		
		String businessAdministratorIds = (String) workItem.getParameter(BUSINESSADMINISTRATOR_ID);
        List<OrganizationalEntity> businessAdministrators = peopleAssignments.getBusinessAdministrators();
        
        User administrator = new User("Administrator");        
        businessAdministrators.add(administrator);
        
        processPeopleAssignments(businessAdministratorIds, businessAdministrators, true);
        
	}
	
	public void assignTaskStakeholders(WorkItem workItem, PeopleAssignments peopleAssignments) {
		
		String taskStakehodlerIds = (String) workItem.getParameter(TASKSTAKEHOLDER_ID);
		List<OrganizationalEntity> taskStakeholders = peopleAssignments.getTaskStakeholders();

		processPeopleAssignments(taskStakehodlerIds, taskStakeholders, true);
		
	}

	protected void processPeopleAssignments(String peopleAssignmentIds, List<OrganizationalEntity> organizationalEntities, boolean user) {
		
        if (peopleAssignmentIds != null && peopleAssignmentIds.trim().length() > 0) {
        	
            String[] ids = peopleAssignmentIds.split(",");
            
            for (String id : ids) {
            	
            	id = id.trim();
            	
            	if (!organizationalEntities.contains(id)) {
            	
            		OrganizationalEntity organizationalEntity = null;
            		
            		if (user) {
            		
            			organizationalEntity = new User(id);
            			
            		} else {
            			
            			organizationalEntity = new Group(id);
            			
            		}
            		
            		organizationalEntities.add(organizationalEntity);
            		
            	}
            	
            }
            
        }
        
	}
	
	protected PeopleAssignments getNullSafePeopleAssignments(Task task) {
		
		PeopleAssignments peopleAssignments = task.getPeopleAssignments();
        
        if (peopleAssignments == null) {
        	
        	peopleAssignments = new PeopleAssignments();
        	peopleAssignments.setPotentialOwners(new ArrayList<OrganizationalEntity>());
        	peopleAssignments.setBusinessAdministrators(new ArrayList<OrganizationalEntity>());
        	peopleAssignments.setExcludedOwners(new ArrayList<OrganizationalEntity>());
        	peopleAssignments.setRecipients(new ArrayList<OrganizationalEntity>());
        	peopleAssignments.setTaskStakeholders(new ArrayList<OrganizationalEntity>());

        }
        
		return peopleAssignments;
		
	}
	
}