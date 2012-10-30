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

import junit.framework.TestCase;

import org.jbpm.task.Group;
import org.jbpm.task.OrganizationalEntity;
import org.jbpm.task.PeopleAssignments;
import org.jbpm.task.Task;
import org.jbpm.task.User;
import org.junit.Test;

/**
 *
 */
public class PeopleAssignmentHelperTests extends TestCase {
	
	@Test
	public void testProcessPeopleAssignments() {

		PeopleAssignmentHelper peopleAssignmentHelper = new PeopleAssignmentHelper();
		List<OrganizationalEntity> organizationalEntities = new ArrayList<OrganizationalEntity>();
		
		String ids = "espiegelberg,   drbug   ";
		assertTrue(organizationalEntities.size() == 0);		
		peopleAssignmentHelper.processPeopleAssignments(ids, organizationalEntities, true);
		assertTrue(organizationalEntities.size() == 2);
		organizationalEntities.contains("drbug");
		organizationalEntities.contains("espiegelberg");
		assertTrue(organizationalEntities.get(0) instanceof User);
		assertTrue(organizationalEntities.get(1) instanceof User);
		
		ids = null;
		organizationalEntities = new ArrayList<OrganizationalEntity>();
		assertTrue(organizationalEntities.size() == 0);		
		peopleAssignmentHelper.processPeopleAssignments(ids, organizationalEntities, true);
		assertTrue(organizationalEntities.size() == 0);
		
		ids = "     ";
		organizationalEntities = new ArrayList<OrganizationalEntity>();
		assertTrue(organizationalEntities.size() == 0);		
		peopleAssignmentHelper.processPeopleAssignments(ids, organizationalEntities, true);
		assertTrue(organizationalEntities.size() == 0);
		
		ids = "Software Developer";
		organizationalEntities = new ArrayList<OrganizationalEntity>();
		assertTrue(organizationalEntities.size() == 0);		
		peopleAssignmentHelper.processPeopleAssignments(ids, organizationalEntities, false);
		assertTrue(organizationalEntities.size() == 1);
		assertTrue(organizationalEntities.get(0) instanceof Group);
		
	}
	
	
	@Test
	public void testGetNullSafePeopleAssignment() {
		
		PeopleAssignmentHelper peopleAssignmentHelper = new PeopleAssignmentHelper();
		
		Task task = new Task();
		
		PeopleAssignments peopleAssignment = peopleAssignmentHelper.getNullSafePeopleAssignment(task);
		assertNotNull(peopleAssignment);
		
		peopleAssignment = peopleAssignmentHelper.getNullSafePeopleAssignment(task);
		assertNotNull(peopleAssignment);
		
	}
	
}
