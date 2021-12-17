/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.services.task.wih.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.drools.core.process.instance.WorkItem;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.services.task.impl.util.HumanTaskHandlerHelper;
import org.jbpm.test.util.AbstractBaseTest;
import org.junit.Test;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.internal.task.api.model.Deadline;
import org.kie.internal.task.api.model.Deadlines;
import org.kie.internal.task.api.model.EmailNotification;
import org.kie.internal.task.api.model.EmailNotificationHeader;
import org.kie.internal.task.api.model.Language;
import org.kie.internal.task.api.model.Notification;
import org.kie.internal.task.api.model.Reassignment;

public class HumanTaskHandlerHelperTest extends AbstractBaseTest {

	@Test
	public void testSetDeadlinesNotStartedReassign() {
		
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedReassign", "[users:john]@[4h]");
		
		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(1, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());
		
		// verify reassignment
		Reassignment reassignment = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().get(0);
		assertEquals(1, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());
		
		// check deadline expiration time
		assertNotNull(deadlines.getStartDeadlines().get(0).getDate());
		long expirationTime = deadlines.getStartDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();
		
		assertEquals(4, roundExpirationTime(expirationTime));
	}

	@Test
	public void testSetDeadlinesNotStartedReassignWithISOExpirationTime() {
		OffsetDateTime fourHoursFromNow = OffsetDateTime.now().plusHours(4);
		String fourHoursFromNowFormatted = fourHoursFromNow.format(DateTimeFormatter.ISO_DATE_TIME);
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedReassign", "[users:john]@[" + fourHoursFromNowFormatted + "]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(1, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());

		// verify reassignment
		Reassignment reassignment = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().get(0);
		assertEquals(1, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());

		// check deadline expiration time
		assertNotNull(deadlines.getStartDeadlines().get(0).getDate());
		long expirationTime = deadlines.getStartDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();

		assertEquals(4, roundExpirationTime(expirationTime));
	}

	@Test
	public void testSetDeadlinesNotStartedReassignWithISOExpirationTimePeriodFormat() {
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedReassign", "[users:john]@[PT4H]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(1, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());

		// verify reassignment
		Reassignment reassignment = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().get(0);
		assertEquals(1, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());

		// check deadline expiration time
		assertNotNull(deadlines.getStartDeadlines().get(0).getDate());
		long expirationTime = deadlines.getStartDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();

		assertEquals(4, roundExpirationTime(expirationTime));
	}

	@Test
	public void testSetDeadlinesNotStartedReassignWithISOExpirationTimePeriodFormatWithRepeatCount() {
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedReassign", "[users:john]@[R3/PT4H]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(3, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());

		// verify reassignment
		Reassignment reassignment = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().get(0);
		assertEquals(1, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());

		// check deadline expiration time
		for (int i = 0; i < 3; i++) {
			assertNotNull(deadlines.getStartDeadlines().get(i).getDate());
			long expirationTime = deadlines.getStartDeadlines().get(i).getDate().getTime() - System.currentTimeMillis();
			// Deadlines are separated 4 hours within each other, first starts at T+4
			assertEquals((i + 1) * 4, roundExpirationTime(expirationTime));
		}
	}


    @Test
    public void testToEmailsNotStarted() {
        WorkItem workItem = new WorkItemImpl();
        workItem.setParameter("NotStartedNotify", "[toemails:salaboy@unkown.com,krisv@unknown.com]@[R3/PT4H]");

        @SuppressWarnings("unchecked")
        Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
        assertNotNull(deadlines);
        assertEquals(3, deadlines.getStartDeadlines().size());
        assertEquals(0, deadlines.getEndDeadlines().size());
        assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
        assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());

        // verify reassignment
        Notification notification = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().get(0);
        assertEquals(2, notification.getRecipients().size());
        assertEquals("salaboy@unkown.com", notification.getRecipients().get(0).getId());
        assertEquals("krisv@unknown.com", notification.getRecipients().get(1).getId());

    }

    @Test
    public void testToEmailsNotCompleted() {
        WorkItem workItem = new WorkItemImpl();
        workItem.setParameter("NotCompletedNotify", "[toemails:salaboy@unkown.com,krisv@unknown.com]@[R3/PT4H]");

        @SuppressWarnings("unchecked")
        Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
        assertNotNull(deadlines);
        assertEquals(0, deadlines.getStartDeadlines().size());
        assertEquals(3, deadlines.getEndDeadlines().size());
        assertEquals(1, deadlines.getEndDeadlines().get(0).getEscalations().size());
        assertEquals(1, deadlines.getEndDeadlines().get(0).getEscalations().get(0).getNotifications().size());

        // verify reassignment
        Notification notification = deadlines.getEndDeadlines().get(0).getEscalations().get(0).getNotifications().get(0);
        assertEquals(2, notification.getRecipients().size());
        assertEquals("salaboy@unkown.com", notification.getRecipients().get(0).getId());
        assertEquals("krisv@unknown.com", notification.getRecipients().get(1).getId());

    }

	@Test
	public void testSetDeadlinesNotStartedReassignWithISOExpirationTimePeriodFormatWithStartEnd() {
		String currentFormatted = OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
		OffsetDateTime fourHoursFromNow = OffsetDateTime.now().plusHours(4);
		String fourHoursFromNowFormatted = fourHoursFromNow.format(DateTimeFormatter.ISO_DATE_TIME);

		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedReassign", "[users:john]@[R3/" + currentFormatted + "/" + fourHoursFromNowFormatted + "]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(3, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());

		// verify reassignment
		Reassignment reassignment = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().get(0);
		assertEquals(1, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());

		// check deadline expiration time
		for (int i = 0; i < 3; i++) {
			assertNotNull(deadlines.getStartDeadlines().get(i).getDate());
			long expirationTime = deadlines.getStartDeadlines().get(i).getDate().getTime() - System.currentTimeMillis();
			// Deadlines are separated 4 hours within each other, first starts at T
			assertEquals(i * 4, roundExpirationTime(expirationTime));
		}
	}

	@Test
	public void testSetDeadlinesNotStartedReassignWithISOExpirationTimeWithPeriodStartEnd() {
		OffsetDateTime fourHoursFromNow = OffsetDateTime.now().plusHours(4);
		String fourHoursFromNowFormatted = fourHoursFromNow.format(DateTimeFormatter.ISO_DATE_TIME);

		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedReassign", "[users:john]@[R3/PT2S/" + fourHoursFromNowFormatted + "]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(3, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());

		// verify reassignment
		Reassignment reassignment = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().get(0);
		assertEquals(1, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());

		// check deadline expiration time
		for (int i = 0; i < 3; i++) {
			assertNotNull(deadlines.getStartDeadlines().get(i).getDate());
			long expirationTime = deadlines.getStartDeadlines().get(i).getDate().getTime() - System.currentTimeMillis();
			// Deadlines are separated 2 seconds within each other (so basically within the same hour), first starts at cca T+4
			assertEquals(4, roundExpirationTime(expirationTime));
		}
	}
	
	@Test
	public void testSetDeadlinesNotStartedReassignWithGroups() {
		
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedReassign", "[users:john|groups:sales]@[4h]");
		
		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(1, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());
		
		// verify reassignment
		Reassignment reassignment = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().get(0);
		assertEquals(2, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());
		assertEquals("sales", reassignment.getPotentialOwners().get(1).getId());
		
		// check deadline expiration time
		assertNotNull(deadlines.getStartDeadlines().get(0).getDate());
		long expirationTime = deadlines.getStartDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();
		
		assertEquals(4, roundExpirationTime(expirationTime));
	}

	@Test
	public void testSetDeadlinesNotStartedReassignWithGroupsWithISOExpirationTime() {
		OffsetDateTime fourHoursFromNow = OffsetDateTime.now().plusHours(4);
		String fourHoursFromNowFormatted = fourHoursFromNow.format(DateTimeFormatter.ISO_DATE_TIME);
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedReassign", "[users:john|groups:sales]@[" + fourHoursFromNowFormatted + "]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(1, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());

		// verify reassignment
		Reassignment reassignment = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().get(0);
		assertEquals(2, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());
		assertEquals("sales", reassignment.getPotentialOwners().get(1).getId());

		// check deadline expiration time
		assertNotNull(deadlines.getStartDeadlines().get(0).getDate());
		long expirationTime = deadlines.getStartDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();

		assertEquals(4, roundExpirationTime(expirationTime));
	}

	@Test
	public void testSetDeadlinesNotStartedReassignWithGroupsWithISOExpirationTimePeriodFormat() {
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedReassign", "[users:john|groups:sales]@[PT4H]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(1, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());

		// verify reassignment
		Reassignment reassignment = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().get(0);
		assertEquals(2, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());
		assertEquals("sales", reassignment.getPotentialOwners().get(1).getId());

		// check deadline expiration time
		assertNotNull(deadlines.getStartDeadlines().get(0).getDate());
		long expirationTime = deadlines.getStartDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();

		assertEquals(4, roundExpirationTime(expirationTime));
	}

	@Test
	public void testSetDeadlinesNotStartedReassignWithGroupsWithISOExpirationTimePeriodFormatWithRepeatCount() {
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedReassign", "[users:john|groups:sales]@[R5/PT4H]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(5, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());

		// verify reassignment
		Reassignment reassignment = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().get(0);
		assertEquals(2, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());
		assertEquals("sales", reassignment.getPotentialOwners().get(1).getId());

		// check deadline expiration time
		for (int i = 0; i < 5; i++) {
			assertNotNull(deadlines.getStartDeadlines().get(i).getDate());
			long expirationTime = deadlines.getStartDeadlines().get(i).getDate().getTime() - System.currentTimeMillis();
			// Deadlines are separated 4 hours within each other, first starts at T+4
			assertEquals((i + 1) * 4, roundExpirationTime(expirationTime));
		}
	}
	
	@Test
	public void testSetDeadlinesNotStartedReassignTwoTimes() {
		
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedReassign", "[users:john]@[4h,6h]");
		
		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(2, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());
		
		assertEquals(1, deadlines.getStartDeadlines().get(1).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(1).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getStartDeadlines().get(1).getEscalations().get(0).getNotifications().size());
		
		// verify reassignment
		Reassignment reassignment = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().get(0);
		assertEquals(1, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());
		
		// check deadline expiration time
		assertNotNull(deadlines.getStartDeadlines().get(0).getDate());
		long expirationTime = deadlines.getStartDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();
		
		assertEquals(4, roundExpirationTime(expirationTime));
		
		// verify reassignment
		reassignment = deadlines.getStartDeadlines().get(1).getEscalations().get(0).getReassignments().get(0);
		assertEquals(1, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());
		
		// check deadline expiration time
		assertNotNull(deadlines.getStartDeadlines().get(1).getDate());
		expirationTime = deadlines.getStartDeadlines().get(1).getDate().getTime() - System.currentTimeMillis();
		
		assertEquals(6, roundExpirationTime(expirationTime));
		
	}

	@Test
	public void testSetDeadlinesNotStartedReassignTwoTimesWithISOExpirationTime() {
		OffsetDateTime fourHoursFromNow = OffsetDateTime.now().plusHours(4);
		String fourHoursFromNowFormatted = fourHoursFromNow.format(DateTimeFormatter.ISO_DATE_TIME);
		OffsetDateTime sixHoursFromNow = OffsetDateTime.now().plusHours(6);
		String sixHoursFromNowFormatted = sixHoursFromNow.format(DateTimeFormatter.ISO_DATE_TIME);

		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedReassign", "[users:john]@[" + fourHoursFromNowFormatted + "," + sixHoursFromNowFormatted + "]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(2, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());

		assertEquals(1, deadlines.getStartDeadlines().get(1).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(1).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getStartDeadlines().get(1).getEscalations().get(0).getNotifications().size());

		// verify reassignment
		Reassignment reassignment = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().get(0);
		assertEquals(1, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());

		// check deadline expiration time
		assertNotNull(deadlines.getStartDeadlines().get(0).getDate());
		long expirationTime = deadlines.getStartDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();

		assertEquals(4, roundExpirationTime(expirationTime));

		// verify reassignment
		reassignment = deadlines.getStartDeadlines().get(1).getEscalations().get(0).getReassignments().get(0);
		assertEquals(1, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());

		// check deadline expiration time
		assertNotNull(deadlines.getStartDeadlines().get(1).getDate());
		expirationTime = deadlines.getStartDeadlines().get(1).getDate().getTime() - System.currentTimeMillis();

		assertEquals(6, roundExpirationTime(expirationTime));

	}

	@Test
	public void testSetDeadlinesNotStartedReassignTwoTimesWithISOExpirationTimePeriodFormat() {
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedReassign", "[users:john]@[PT4H,PT6H]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(2, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());

		assertEquals(1, deadlines.getStartDeadlines().get(1).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(1).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getStartDeadlines().get(1).getEscalations().get(0).getNotifications().size());

		// verify reassignment
		Reassignment reassignment = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().get(0);
		assertEquals(1, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());

		// check deadline expiration time
		assertNotNull(deadlines.getStartDeadlines().get(0).getDate());
		long expirationTime = deadlines.getStartDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();

		assertEquals(4, roundExpirationTime(expirationTime));

		// verify reassignment
		reassignment = deadlines.getStartDeadlines().get(1).getEscalations().get(0).getReassignments().get(0);
		assertEquals(1, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());

		// check deadline expiration time
		assertNotNull(deadlines.getStartDeadlines().get(1).getDate());
		expirationTime = deadlines.getStartDeadlines().get(1).getDate().getTime() - System.currentTimeMillis();

		assertEquals(6, roundExpirationTime(expirationTime));

	}

	@Test
	public void testSetDeadlinesNotStartedReassignTwoTimesWithISOExpirationTimePeriodFormatWithRepeatCount() {
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedReassign", "[users:john]@[R3/PT4H,R3/PT6H]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(6, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());

		assertEquals(1, deadlines.getStartDeadlines().get(1).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(1).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getStartDeadlines().get(1).getEscalations().get(0).getNotifications().size());

		// verify reassignment
		Reassignment reassignment = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().get(0);
		assertEquals(1, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());

		// check deadline expiration time
		for (int i = 0; i < 3; i++) {
			assertNotNull(deadlines.getStartDeadlines().get(i).getDate());
			long expirationTime = deadlines.getStartDeadlines().get(i).getDate().getTime() - System.currentTimeMillis();
			// Deadlines are separated 4 hours within each other, first starts at T+4
			assertEquals((i + 1) * 4, roundExpirationTime(expirationTime));
		}

		// verify reassignment
		reassignment = deadlines.getStartDeadlines().get(1).getEscalations().get(0).getReassignments().get(0);
		assertEquals(1, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());

		// check deadline expiration time
		for (int i = 3; i < 6; i++) {
			assertNotNull(deadlines.getStartDeadlines().get(i).getDate());
			long expirationTime = deadlines.getStartDeadlines().get(i).getDate().getTime() - System.currentTimeMillis();
			// Deadlines are separated 6 hours within each other, first starts at T+6
			assertEquals((i - 3 + 1) * 6, roundExpirationTime(expirationTime));
		}

	}
	
	@Test
	public void testSetDeadlinesNotCompletedReassign() {
		
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotCompletedReassign", "[users:john]@[4h]");
		
		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(1, deadlines.getEndDeadlines().size());
		assertEquals(0, deadlines.getStartDeadlines().size());
		assertEquals(1, deadlines.getEndDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getEndDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getEndDeadlines().get(0).getEscalations().get(0).getNotifications().size());
		
		// verify reassignment
		Reassignment reassignment = deadlines.getEndDeadlines().get(0).getEscalations().get(0).getReassignments().get(0);
		assertEquals(1, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());
		
		// check deadline expiration time
		assertNotNull(deadlines.getEndDeadlines().get(0).getDate());
		long expirationTime = deadlines.getEndDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();
		
		assertEquals(4, roundExpirationTime(expirationTime));
		
	}

	@Test
	public void testSetDeadlinesNotCompletedReassignWithISOExpirationTime() {
		OffsetDateTime fourHoursFromNow = OffsetDateTime.now().plusHours(4);
		String fourHoursFromNowFormatted = fourHoursFromNow.format(DateTimeFormatter.ISO_DATE_TIME);

		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotCompletedReassign", "[users:john]@[" + fourHoursFromNowFormatted + "]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(1, deadlines.getEndDeadlines().size());
		assertEquals(0, deadlines.getStartDeadlines().size());
		assertEquals(1, deadlines.getEndDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getEndDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getEndDeadlines().get(0).getEscalations().get(0).getNotifications().size());

		// verify reassignment
		Reassignment reassignment = deadlines.getEndDeadlines().get(0).getEscalations().get(0).getReassignments().get(0);
		assertEquals(1, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());

		// check deadline expiration time
		assertNotNull(deadlines.getEndDeadlines().get(0).getDate());
		long expirationTime = deadlines.getEndDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();

		assertEquals(4, roundExpirationTime(expirationTime));

	}

	@Test
	public void testSetDeadlinesNotCompletedReassignWithISOExpirationTimePeriodFormat() {
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotCompletedReassign", "[users:john]@[PT4H]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(1, deadlines.getEndDeadlines().size());
		assertEquals(0, deadlines.getStartDeadlines().size());
		assertEquals(1, deadlines.getEndDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getEndDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getEndDeadlines().get(0).getEscalations().get(0).getNotifications().size());

		// verify reassignment
		Reassignment reassignment = deadlines.getEndDeadlines().get(0).getEscalations().get(0).getReassignments().get(0);
		assertEquals(1, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());

		// check deadline expiration time
		assertNotNull(deadlines.getEndDeadlines().get(0).getDate());
		long expirationTime = deadlines.getEndDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();

		assertEquals(4, roundExpirationTime(expirationTime));

	}

	@Test
	public void testSetDeadlinesNotCompletedReassignWithISOExpirationTimePeriodFormatWithRepeatCount() {
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotCompletedReassign", "[users:john]@[R3/PT4H]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(3, deadlines.getEndDeadlines().size());
		assertEquals(0, deadlines.getStartDeadlines().size());
		assertEquals(1, deadlines.getEndDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getEndDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getEndDeadlines().get(0).getEscalations().get(0).getNotifications().size());

		// verify reassignment
		Reassignment reassignment = deadlines.getEndDeadlines().get(0).getEscalations().get(0).getReassignments().get(0);
		assertEquals(1, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());

		// check deadline expiration time
		for (int i = 0; i < 3; i++) {
			assertNotNull(deadlines.getEndDeadlines().get(i).getDate());
			long expirationTime = deadlines.getEndDeadlines().get(i).getDate().getTime() - System.currentTimeMillis();
			// Deadlines are separated 4 hours within each other, first starts at T+4
			assertEquals((i + 1) * 4, roundExpirationTime(expirationTime));
		}
	}
	
	@Test //JBPM-4291
	public void testSetDeadlinesNotCompletedReassignWithGroups() {
		
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotCompletedReassign", "[users:john|groups:sales]@[4h]");
		
		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(1, deadlines.getEndDeadlines().size());
		assertEquals(0, deadlines.getStartDeadlines().size());
		assertEquals(1, deadlines.getEndDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getEndDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getEndDeadlines().get(0).getEscalations().get(0).getNotifications().size());
		
		// verify reassignment
		Reassignment reassignment = deadlines.getEndDeadlines().get(0).getEscalations().get(0).getReassignments().get(0);
		assertEquals(2, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());
		assertEquals("sales", reassignment.getPotentialOwners().get(1).getId());
		
		// check deadline expiration time
		assertNotNull(deadlines.getEndDeadlines().get(0).getDate());
		long expirationTime = deadlines.getEndDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();
		
		assertEquals(4, roundExpirationTime(expirationTime));
		
	}

	@Test //JBPM-4291
	public void testSetDeadlinesNotCompletedReassignWithGroupsWithISOExpirationTime() {
		OffsetDateTime fourHoursFromNow = OffsetDateTime.now().plusHours(4);
		String fourHoursFromNowFormatted = fourHoursFromNow.format(DateTimeFormatter.ISO_DATE_TIME);

		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotCompletedReassign", "[users:john|groups:sales]@[" + fourHoursFromNowFormatted + "]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(1, deadlines.getEndDeadlines().size());
		assertEquals(0, deadlines.getStartDeadlines().size());
		assertEquals(1, deadlines.getEndDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getEndDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getEndDeadlines().get(0).getEscalations().get(0).getNotifications().size());

		// verify reassignment
		Reassignment reassignment = deadlines.getEndDeadlines().get(0).getEscalations().get(0).getReassignments().get(0);
		assertEquals(2, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());
		assertEquals("sales", reassignment.getPotentialOwners().get(1).getId());

		// check deadline expiration time
		assertNotNull(deadlines.getEndDeadlines().get(0).getDate());
		long expirationTime = deadlines.getEndDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();

		assertEquals(4, roundExpirationTime(expirationTime));

	}

	@Test //JBPM-4291
	public void testSetDeadlinesNotCompletedReassignWithGroupsWithISOExpirationTimePeriodFormat() {
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotCompletedReassign", "[users:john|groups:sales]@[PT4H]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(1, deadlines.getEndDeadlines().size());
		assertEquals(0, deadlines.getStartDeadlines().size());
		assertEquals(1, deadlines.getEndDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getEndDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getEndDeadlines().get(0).getEscalations().get(0).getNotifications().size());

		// verify reassignment
		Reassignment reassignment = deadlines.getEndDeadlines().get(0).getEscalations().get(0).getReassignments().get(0);
		assertEquals(2, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());
		assertEquals("sales", reassignment.getPotentialOwners().get(1).getId());

		// check deadline expiration time
		assertNotNull(deadlines.getEndDeadlines().get(0).getDate());
		long expirationTime = deadlines.getEndDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();

		assertEquals(4, roundExpirationTime(expirationTime));

	}

	@Test //JBPM-4291
	public void testSetDeadlinesNotCompletedReassignWithGroupsWithISOExpirationTimePeriodFormatWithRepeatCount() {
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotCompletedReassign", "[users:john|groups:sales]@[R3/PT4H]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(3, deadlines.getEndDeadlines().size());
		assertEquals(0, deadlines.getStartDeadlines().size());
		assertEquals(1, deadlines.getEndDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getEndDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getEndDeadlines().get(0).getEscalations().get(0).getNotifications().size());

		// verify reassignment
		Reassignment reassignment = deadlines.getEndDeadlines().get(0).getEscalations().get(0).getReassignments().get(0);
		assertEquals(2, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());
		assertEquals("sales", reassignment.getPotentialOwners().get(1).getId());

		// check deadline expiration time
		for (int i = 0; i < 3; i++) {
			assertNotNull(deadlines.getEndDeadlines().get(i).getDate());
			long expirationTime = deadlines.getEndDeadlines().get(i).getDate().getTime() - System.currentTimeMillis();
			// Deadlines are separated 4 hours within each other, first starts at T+4
			assertEquals((i + 1) * 4, roundExpirationTime(expirationTime));
		}

	}
	
	@Test
	public void testSetDeadlinesNotCompletedReassignTwoTimes() {
		
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotCompletedReassign", "[users:john]@[4h,6h]");
		
		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(2, deadlines.getEndDeadlines().size());
		assertEquals(0, deadlines.getStartDeadlines().size());
		assertEquals(1, deadlines.getEndDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getEndDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getEndDeadlines().get(0).getEscalations().get(0).getNotifications().size());
		
		assertEquals(1, deadlines.getEndDeadlines().get(1).getEscalations().size());
		assertEquals(1, deadlines.getEndDeadlines().get(1).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getEndDeadlines().get(1).getEscalations().get(0).getNotifications().size());
		
		// verify reassignment
		Reassignment reassignment = deadlines.getEndDeadlines().get(0).getEscalations().get(0).getReassignments().get(0);
		assertEquals(1, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());
		
		// check deadline expiration time
		assertNotNull(deadlines.getEndDeadlines().get(0).getDate());
		long expirationTime = deadlines.getEndDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();
		
		assertEquals(4, roundExpirationTime(expirationTime));
		
		// verify reassignment
		reassignment = deadlines.getEndDeadlines().get(1).getEscalations().get(0).getReassignments().get(0);
		assertEquals(1, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());
		
		// check deadline expiration time
		assertNotNull(deadlines.getEndDeadlines().get(1).getDate());
		expirationTime = deadlines.getEndDeadlines().get(1).getDate().getTime() - System.currentTimeMillis();
		
		assertEquals(6, roundExpirationTime(expirationTime));
		
	}

	@Test
	public void testSetDeadlinesNotCompletedReassignTwoTimesWithISOExpirationTime() {
		OffsetDateTime fourHoursFromNow = OffsetDateTime.now().plusHours(4);
		String fourHoursFromNowFormatted = fourHoursFromNow.format(DateTimeFormatter.ISO_DATE_TIME);
		OffsetDateTime sixHoursFromNow = OffsetDateTime.now().plusHours(6);
		String sixHoursFromNowFormatted = sixHoursFromNow.format(DateTimeFormatter.ISO_DATE_TIME);

		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotCompletedReassign", "[users:john]@[" + fourHoursFromNowFormatted + "," + sixHoursFromNowFormatted + "]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(2, deadlines.getEndDeadlines().size());
		assertEquals(0, deadlines.getStartDeadlines().size());
		assertEquals(1, deadlines.getEndDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getEndDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getEndDeadlines().get(0).getEscalations().get(0).getNotifications().size());

		assertEquals(1, deadlines.getEndDeadlines().get(1).getEscalations().size());
		assertEquals(1, deadlines.getEndDeadlines().get(1).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getEndDeadlines().get(1).getEscalations().get(0).getNotifications().size());

		// verify reassignment
		Reassignment reassignment = deadlines.getEndDeadlines().get(0).getEscalations().get(0).getReassignments().get(0);
		assertEquals(1, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());

		// check deadline expiration time
		assertNotNull(deadlines.getEndDeadlines().get(0).getDate());
		long expirationTime = deadlines.getEndDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();

		assertEquals(4, roundExpirationTime(expirationTime));

		// verify reassignment
		reassignment = deadlines.getEndDeadlines().get(1).getEscalations().get(0).getReassignments().get(0);
		assertEquals(1, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());

		// check deadline expiration time
		assertNotNull(deadlines.getEndDeadlines().get(1).getDate());
		expirationTime = deadlines.getEndDeadlines().get(1).getDate().getTime() - System.currentTimeMillis();

		assertEquals(6, roundExpirationTime(expirationTime));

	}

	@Test
	public void testSetDeadlinesNotCompletedReassignTwoTimesWithISOExpirationTimePeriodFormat() {
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotCompletedReassign", "[users:john]@[PT4H,PT6H]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(2, deadlines.getEndDeadlines().size());
		assertEquals(0, deadlines.getStartDeadlines().size());
		assertEquals(1, deadlines.getEndDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getEndDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getEndDeadlines().get(0).getEscalations().get(0).getNotifications().size());

		assertEquals(1, deadlines.getEndDeadlines().get(1).getEscalations().size());
		assertEquals(1, deadlines.getEndDeadlines().get(1).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getEndDeadlines().get(1).getEscalations().get(0).getNotifications().size());

		// verify reassignment
		Reassignment reassignment = deadlines.getEndDeadlines().get(0).getEscalations().get(0).getReassignments().get(0);
		assertEquals(1, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());

		// check deadline expiration time
		assertNotNull(deadlines.getEndDeadlines().get(0).getDate());
		long expirationTime = deadlines.getEndDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();

		assertEquals(4, roundExpirationTime(expirationTime));

		// verify reassignment
		reassignment = deadlines.getEndDeadlines().get(1).getEscalations().get(0).getReassignments().get(0);
		assertEquals(1, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());

		// check deadline expiration time
		assertNotNull(deadlines.getEndDeadlines().get(1).getDate());
		expirationTime = deadlines.getEndDeadlines().get(1).getDate().getTime() - System.currentTimeMillis();

		assertEquals(6, roundExpirationTime(expirationTime));

	}

	@Test
	public void testSetDeadlinesNotCompletedReassignTwoTimesWithISOExpirationTimePeriodFormatWithRepeatCount() {
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotCompletedReassign", "[users:john]@[R/PT4H,R3/PT6H]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(4, deadlines.getEndDeadlines().size());
		assertEquals(0, deadlines.getStartDeadlines().size());
		assertEquals(1, deadlines.getEndDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getEndDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getEndDeadlines().get(0).getEscalations().get(0).getNotifications().size());

		assertEquals(1, deadlines.getEndDeadlines().get(1).getEscalations().size());
		assertEquals(1, deadlines.getEndDeadlines().get(1).getEscalations().get(0).getReassignments().size());
		assertEquals(0, deadlines.getEndDeadlines().get(1).getEscalations().get(0).getNotifications().size());

		// verify reassignment
		Reassignment reassignment = deadlines.getEndDeadlines().get(0).getEscalations().get(0).getReassignments().get(0);
		assertEquals(1, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());

		// check deadline expiration time
		assertNotNull(deadlines.getEndDeadlines().get(0).getDate());
		long expirationTime = deadlines.getEndDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();

		assertEquals(4, roundExpirationTime(expirationTime));

		// verify reassignment
		reassignment = deadlines.getEndDeadlines().get(1).getEscalations().get(0).getReassignments().get(0);
		assertEquals(1, reassignment.getPotentialOwners().size());
		assertEquals("john", reassignment.getPotentialOwners().get(0).getId());

		// check deadline expiration time
		for (int i = 1; i < 4; i++) {
			assertNotNull(deadlines.getEndDeadlines().get(i).getDate());
			expirationTime = deadlines.getEndDeadlines().get(i).getDate().getTime() - System.currentTimeMillis();
			// Deadlines are separated 6 hours within each other, first starts at cca T+6
			assertEquals((i - 1 + 1) * 6, roundExpirationTime(expirationTime));
		}

	}
	
	@Test
	public void testNotStartedNotifyMinimal() {
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedNotify", "[tousers:john|subject:Test of notification|body:And here is the body]@[4h]");
		
		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(1, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		
		// verify notification
		Notification notification = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().get(0);
		assertNotNull(notification);
		assertEquals(1, notification.getRecipients().size());
		assertEquals("john", notification.getRecipients().get(0).getId());
		
		assertEquals(1, notification.getSubjects().size());
		assertEquals("Test of notification", notification.getSubjects().get(0).getText());
		
		EmailNotification emailNotification = (EmailNotification) notification;
		assertEquals(1, emailNotification.getEmailHeaders().size());
		Language lang = TaskModelProvider.getFactory().newLanguage();
		lang.setMapkey("en-UK");
		EmailNotificationHeader header = emailNotification.getEmailHeaders().get(lang);
		assertNotNull(header);
		assertEquals("Test of notification", header.getSubject());
		assertEquals("And here is the body", header.getBody());
		
		// check deadline expiration time
		assertNotNull(deadlines.getStartDeadlines().get(0).getDate());
		long expirationTime = deadlines.getStartDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();
		
		assertEquals(4, roundExpirationTime(expirationTime));
	}

	@Test
	public void testNotStartedNotifyMinimalWithISOExpirationTime() {
		OffsetDateTime fourHoursFromNow = OffsetDateTime.now().plusHours(4);
		String fourHoursFromNowFormatted = fourHoursFromNow.format(DateTimeFormatter.ISO_DATE_TIME);

		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedNotify", "[tousers:john|subject:Test of notification|body:And here is the body]@[" + fourHoursFromNowFormatted + "]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(1, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());

		// verify notification
		Notification notification = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().get(0);
		assertNotNull(notification);
		assertEquals(1, notification.getRecipients().size());
		assertEquals("john", notification.getRecipients().get(0).getId());

		assertEquals(1, notification.getSubjects().size());
		assertEquals("Test of notification", notification.getSubjects().get(0).getText());

		EmailNotification emailNotification = (EmailNotification) notification;
		assertEquals(1, emailNotification.getEmailHeaders().size());
		Language lang = TaskModelProvider.getFactory().newLanguage();
		lang.setMapkey("en-UK");
		EmailNotificationHeader header = emailNotification.getEmailHeaders().get(lang);
		assertNotNull(header);
		assertEquals("Test of notification", header.getSubject());
		assertEquals("And here is the body", header.getBody());

		// check deadline expiration time
		assertNotNull(deadlines.getStartDeadlines().get(0).getDate());
		long expirationTime = deadlines.getStartDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();

		assertEquals(4, roundExpirationTime(expirationTime));
	}

	@Test
	public void testNotStartedNotifyMinimalWithISOExpirationTimePeriodFormat() {
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedNotify", "[tousers:john|subject:Test of notification|body:And here is the body]@[PT4H]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(1, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());

		// verify notification
		Notification notification = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().get(0);
		assertNotNull(notification);
		assertEquals(1, notification.getRecipients().size());
		assertEquals("john", notification.getRecipients().get(0).getId());

		assertEquals(1, notification.getSubjects().size());
		assertEquals("Test of notification", notification.getSubjects().get(0).getText());

		EmailNotification emailNotification = (EmailNotification) notification;
		assertEquals(1, emailNotification.getEmailHeaders().size());
		Language lang = TaskModelProvider.getFactory().newLanguage();
		lang.setMapkey("en-UK");
		EmailNotificationHeader header = emailNotification.getEmailHeaders().get(lang);
		assertNotNull(header);
		assertEquals("Test of notification", header.getSubject());
		assertEquals("And here is the body", header.getBody());

		// check deadline expiration time
		assertNotNull(deadlines.getStartDeadlines().get(0).getDate());
		long expirationTime = deadlines.getStartDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();

		assertEquals(4, roundExpirationTime(expirationTime));
	}

	@Test
	public void testNotStartedNotifyMinimalWithISOExpirationTimePeriodFormatWithRepeatCount() {
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedNotify", "[tousers:john|subject:Test of notification|body:And here is the body]@[R3/PT4H]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(3, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());

		// verify notification
		Notification notification = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().get(0);
		assertNotNull(notification);
		assertEquals(1, notification.getRecipients().size());
		assertEquals("john", notification.getRecipients().get(0).getId());

		assertEquals(1, notification.getSubjects().size());
		assertEquals("Test of notification", notification.getSubjects().get(0).getText());

		EmailNotification emailNotification = (EmailNotification) notification;
		assertEquals(1, emailNotification.getEmailHeaders().size());
		Language lang = TaskModelProvider.getFactory().newLanguage();
		lang.setMapkey("en-UK");
		EmailNotificationHeader header = emailNotification.getEmailHeaders().get(lang);
		assertNotNull(header);
		assertEquals("Test of notification", header.getSubject());
		assertEquals("And here is the body", header.getBody());

		// check deadline expiration time
		for (int i = 0; i < 3; i++) {
			assertNotNull(deadlines.getStartDeadlines().get(i).getDate());
			long expirationTime = deadlines.getStartDeadlines().get(i).getDate().getTime() - System.currentTimeMillis();
			// Deadlines are separated 4 hours within each other, first starts at cca T+4
			assertEquals((i + 1) * 4, roundExpirationTime(expirationTime));
		}
	}
	
	@Test
	public void testNotStartedNotifyAllElements() {
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedNotify", "[from:mike|tousers:john,mary|togroups:sales,hr|replyto:mike|subject:Test of notification|body:And here is the body]@[4h]");
		
		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(1, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		
		// verify notification
		Notification notification = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().get(0);
		assertNotNull(notification);
		assertEquals(4, notification.getRecipients().size());
		assertEquals("john", notification.getRecipients().get(0).getId());
		assertEquals("mary", notification.getRecipients().get(1).getId());
		assertEquals("sales", notification.getRecipients().get(2).getId());
		assertEquals("hr", notification.getRecipients().get(3).getId());
		
		assertEquals(1, notification.getSubjects().size());
		assertEquals("Test of notification", notification.getSubjects().get(0).getText());
		
		EmailNotification emailNotification = (EmailNotification) notification;
		assertEquals(1, emailNotification.getEmailHeaders().size());
		Language lang = TaskModelProvider.getFactory().newLanguage();
		lang.setMapkey("en-UK");
		EmailNotificationHeader header = emailNotification.getEmailHeaders().get(lang);
		assertNotNull(header);
		assertEquals("Test of notification", header.getSubject());
		assertEquals("And here is the body", header.getBody());
		assertEquals("mike", header.getFrom());
		assertEquals("mike", header.getReplyTo());
		
		// check deadline expiration time
		assertNotNull(deadlines.getStartDeadlines().get(0).getDate());
		long expirationTime = deadlines.getStartDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();
		
		assertEquals(4, roundExpirationTime(expirationTime));
	}

	@Test
	public void testNotStartedNotifyAllElementsWithISOExpirationTime() {
		OffsetDateTime fourHoursFromNow = OffsetDateTime.now().plusHours(4);
		String fourHoursFromNowFormatted = fourHoursFromNow.format(DateTimeFormatter.ISO_DATE_TIME);


		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedNotify", "[from:mike|tousers:john,mary|togroups:sales,hr|replyto:mike|subject:Test of notification|body:And here is the body]@[" + fourHoursFromNowFormatted + "]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(1, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());

		// verify notification
		Notification notification = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().get(0);
		assertNotNull(notification);
		assertEquals(4, notification.getRecipients().size());
		assertEquals("john", notification.getRecipients().get(0).getId());
		assertEquals("mary", notification.getRecipients().get(1).getId());
		assertEquals("sales", notification.getRecipients().get(2).getId());
		assertEquals("hr", notification.getRecipients().get(3).getId());

		assertEquals(1, notification.getSubjects().size());
		assertEquals("Test of notification", notification.getSubjects().get(0).getText());

		EmailNotification emailNotification = (EmailNotification) notification;
		assertEquals(1, emailNotification.getEmailHeaders().size());
		Language lang = TaskModelProvider.getFactory().newLanguage();
		lang.setMapkey("en-UK");
		EmailNotificationHeader header = emailNotification.getEmailHeaders().get(lang);
		assertNotNull(header);
		assertEquals("Test of notification", header.getSubject());
		assertEquals("And here is the body", header.getBody());
		assertEquals("mike", header.getFrom());
		assertEquals("mike", header.getReplyTo());

		// check deadline expiration time
		assertNotNull(deadlines.getStartDeadlines().get(0).getDate());
		long expirationTime = deadlines.getStartDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();

		assertEquals(4, roundExpirationTime(expirationTime));
	}

	@Test
	public void testNotStartedNotifyAllElementsWithISOExpirationTimePeriodFormat() {
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedNotify", "[from:mike|tousers:john,mary|togroups:sales,hr|replyto:mike|subject:Test of notification|body:And here is the body]@[PT4H]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(1, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());

		// verify notification
		Notification notification = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().get(0);
		assertNotNull(notification);
		assertEquals(4, notification.getRecipients().size());
		assertEquals("john", notification.getRecipients().get(0).getId());
		assertEquals("mary", notification.getRecipients().get(1).getId());
		assertEquals("sales", notification.getRecipients().get(2).getId());
		assertEquals("hr", notification.getRecipients().get(3).getId());

		assertEquals(1, notification.getSubjects().size());
		assertEquals("Test of notification", notification.getSubjects().get(0).getText());

		EmailNotification emailNotification = (EmailNotification) notification;
		assertEquals(1, emailNotification.getEmailHeaders().size());
		Language lang = TaskModelProvider.getFactory().newLanguage();
		lang.setMapkey("en-UK");
		EmailNotificationHeader header = emailNotification.getEmailHeaders().get(lang);
		assertNotNull(header);
		assertEquals("Test of notification", header.getSubject());
		assertEquals("And here is the body", header.getBody());
		assertEquals("mike", header.getFrom());
		assertEquals("mike", header.getReplyTo());

		// check deadline expiration time
		assertNotNull(deadlines.getStartDeadlines().get(0).getDate());
		long expirationTime = deadlines.getStartDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();

		assertEquals(4, roundExpirationTime(expirationTime));
	}

	@Test
	public void testNotStartedNotifyAllElementsWithISOExpirationTimePeriodFormatWithRepeatCount() {
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedNotify", "[from:mike|tousers:john,mary|togroups:sales,hr|replyto:mike|subject:Test of notification|body:And here is the body]@[R3/PT4H]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(3, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());

		// verify notification
		Notification notification = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().get(0);
		assertNotNull(notification);
		assertEquals(4, notification.getRecipients().size());
		assertEquals("john", notification.getRecipients().get(0).getId());
		assertEquals("mary", notification.getRecipients().get(1).getId());
		assertEquals("sales", notification.getRecipients().get(2).getId());
		assertEquals("hr", notification.getRecipients().get(3).getId());

		assertEquals(1, notification.getSubjects().size());
		assertEquals("Test of notification", notification.getSubjects().get(0).getText());

		EmailNotification emailNotification = (EmailNotification) notification;
		assertEquals(1, emailNotification.getEmailHeaders().size());
		Language lang = TaskModelProvider.getFactory().newLanguage();
		lang.setMapkey("en-UK");
		EmailNotificationHeader header = emailNotification.getEmailHeaders().get(lang);
		assertNotNull(header);
		assertEquals("Test of notification", header.getSubject());
		assertEquals("And here is the body", header.getBody());
		assertEquals("mike", header.getFrom());
		assertEquals("mike", header.getReplyTo());

		// check deadline expiration time
		for (int i = 0; i < 3; i++) {
			assertNotNull(deadlines.getStartDeadlines().get(i).getDate());
			long expirationTime = deadlines.getStartDeadlines().get(i).getDate().getTime() - System.currentTimeMillis();
			// Deadlines are separated 4 hours within each other, first starts at cca T+4
			assertEquals((i + 1) * 4, roundExpirationTime(expirationTime));
		}
	}
	
	@Test
	public void testNotStartedNotifyMinimalMultipleExpirations() {
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedNotify", "[tousers:john|subject:Test of notification|body:And here is the body]@[4h,6h]");
		
		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(2, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		
		// verify notification
		Notification notification = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().get(0);
		assertNotNull(notification);
		assertEquals(1, notification.getRecipients().size());
		assertEquals("john", notification.getRecipients().get(0).getId());
		
		assertEquals(1, notification.getSubjects().size());
		assertEquals("Test of notification", notification.getSubjects().get(0).getText());
		
		EmailNotification emailNotification = (EmailNotification) notification;
		assertEquals(1, emailNotification.getEmailHeaders().size());
		Language lang = TaskModelProvider.getFactory().newLanguage();
		lang.setMapkey("en-UK");
		EmailNotificationHeader header = emailNotification.getEmailHeaders().get(lang);
		assertNotNull(header);
		assertEquals("Test of notification", header.getSubject());
		assertEquals("And here is the body", header.getBody());
		
		// check deadline expiration time
		assertNotNull(deadlines.getStartDeadlines().get(0).getDate());
		long expirationTime = deadlines.getStartDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();
		
		assertEquals(4, roundExpirationTime(expirationTime));
		
		// verify notification
		notification = deadlines.getStartDeadlines().get(1).getEscalations().get(0).getNotifications().get(0);
		assertNotNull(notification);
		assertEquals(1, notification.getRecipients().size());
		assertEquals("john", notification.getRecipients().get(0).getId());
		
		assertEquals(1, notification.getSubjects().size());
		assertEquals("Test of notification", notification.getSubjects().get(0).getText());
		
		emailNotification = (EmailNotification) notification;
		assertEquals(1, emailNotification.getEmailHeaders().size());
		lang = TaskModelProvider.getFactory().newLanguage();
		lang.setMapkey("en-UK");
		header = emailNotification.getEmailHeaders().get(lang);
		assertNotNull(header);
		assertEquals("Test of notification", header.getSubject());
		assertEquals("And here is the body", header.getBody());
		
		// check deadline expiration time
		assertNotNull(deadlines.getStartDeadlines().get(1).getDate());
		expirationTime = deadlines.getStartDeadlines().get(1).getDate().getTime() - System.currentTimeMillis();
		
		assertEquals(6, roundExpirationTime(expirationTime));
	}

	@Test
	public void testNotStartedNotifyMinimalMultipleExpirationsWithISOExpirationTime() {
		OffsetDateTime fourHoursFromNow = OffsetDateTime.now().plusHours(4);
		String fourHoursFromNowFormatted = fourHoursFromNow.format(DateTimeFormatter.ISO_DATE_TIME);
		OffsetDateTime sixHoursFromNow = OffsetDateTime.now().plusHours(6);
		String sixHoursFromNowFormatted = sixHoursFromNow.format(DateTimeFormatter.ISO_DATE_TIME);

		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedNotify", "[tousers:john|subject:Test of notification|body:And here is the body]@[" + fourHoursFromNowFormatted + "," + sixHoursFromNowFormatted + "]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(2, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());

		// verify notification
		Notification notification = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().get(0);
		assertNotNull(notification);
		assertEquals(1, notification.getRecipients().size());
		assertEquals("john", notification.getRecipients().get(0).getId());

		assertEquals(1, notification.getSubjects().size());
		assertEquals("Test of notification", notification.getSubjects().get(0).getText());

		EmailNotification emailNotification = (EmailNotification) notification;
		assertEquals(1, emailNotification.getEmailHeaders().size());
		Language lang = TaskModelProvider.getFactory().newLanguage();
		lang.setMapkey("en-UK");
		EmailNotificationHeader header = emailNotification.getEmailHeaders().get(lang);
		assertNotNull(header);
		assertEquals("Test of notification", header.getSubject());
		assertEquals("And here is the body", header.getBody());

		// check deadline expiration time
		assertNotNull(deadlines.getStartDeadlines().get(0).getDate());
		long expirationTime = deadlines.getStartDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();

		assertEquals(4, roundExpirationTime(expirationTime));

		// verify notification
		notification = deadlines.getStartDeadlines().get(1).getEscalations().get(0).getNotifications().get(0);
		assertNotNull(notification);
		assertEquals(1, notification.getRecipients().size());
		assertEquals("john", notification.getRecipients().get(0).getId());

		assertEquals(1, notification.getSubjects().size());
		assertEquals("Test of notification", notification.getSubjects().get(0).getText());

		emailNotification = (EmailNotification) notification;
		assertEquals(1, emailNotification.getEmailHeaders().size());
		lang = TaskModelProvider.getFactory().newLanguage();
		lang.setMapkey("en-UK");
		header = emailNotification.getEmailHeaders().get(lang);
		assertNotNull(header);
		assertEquals("Test of notification", header.getSubject());
		assertEquals("And here is the body", header.getBody());

		// check deadline expiration time
		assertNotNull(deadlines.getStartDeadlines().get(1).getDate());
		expirationTime = deadlines.getStartDeadlines().get(1).getDate().getTime() - System.currentTimeMillis();

		assertEquals(6, roundExpirationTime(expirationTime));
	}

	@Test
	public void testNotStartedNotifyMinimalMultipleExpirationsWithISOExpirationTimePeriodFormat() {
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedNotify", "[tousers:john|subject:Test of notification|body:And here is the body]@[PT4H,PT6H]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(2, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());

		// verify notification
		Notification notification = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().get(0);
		assertNotNull(notification);
		assertEquals(1, notification.getRecipients().size());
		assertEquals("john", notification.getRecipients().get(0).getId());

		assertEquals(1, notification.getSubjects().size());
		assertEquals("Test of notification", notification.getSubjects().get(0).getText());

		EmailNotification emailNotification = (EmailNotification) notification;
		assertEquals(1, emailNotification.getEmailHeaders().size());
		Language lang = TaskModelProvider.getFactory().newLanguage();
		lang.setMapkey("en-UK");
		EmailNotificationHeader header = emailNotification.getEmailHeaders().get(lang);
		assertNotNull(header);
		assertEquals("Test of notification", header.getSubject());
		assertEquals("And here is the body", header.getBody());

		// check deadline expiration time
		assertNotNull(deadlines.getStartDeadlines().get(0).getDate());
		long expirationTime = deadlines.getStartDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();

		assertEquals(4, roundExpirationTime(expirationTime));

		// verify notification
		notification = deadlines.getStartDeadlines().get(1).getEscalations().get(0).getNotifications().get(0);
		assertNotNull(notification);
		assertEquals(1, notification.getRecipients().size());
		assertEquals("john", notification.getRecipients().get(0).getId());

		assertEquals(1, notification.getSubjects().size());
		assertEquals("Test of notification", notification.getSubjects().get(0).getText());

		emailNotification = (EmailNotification) notification;
		assertEquals(1, emailNotification.getEmailHeaders().size());
		lang = TaskModelProvider.getFactory().newLanguage();
		lang.setMapkey("en-UK");
		header = emailNotification.getEmailHeaders().get(lang);
		assertNotNull(header);
		assertEquals("Test of notification", header.getSubject());
		assertEquals("And here is the body", header.getBody());

		// check deadline expiration time
		assertNotNull(deadlines.getStartDeadlines().get(1).getDate());
		expirationTime = deadlines.getStartDeadlines().get(1).getDate().getTime() - System.currentTimeMillis();

		assertEquals(6, roundExpirationTime(expirationTime));
	}

	@Test
	public void testNotStartedNotifyMinimalMultipleExpirationsWithISOExpirationTimePeriodFormatWithRepeatCount() {
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedNotify", "[tousers:john|subject:Test of notification|body:And here is the body]@[R4/PT4H,R4/PT6H]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(8, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());

		// verify notification
		Notification notification = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().get(0);
		assertNotNull(notification);
		assertEquals(1, notification.getRecipients().size());
		assertEquals("john", notification.getRecipients().get(0).getId());

		assertEquals(1, notification.getSubjects().size());
		assertEquals("Test of notification", notification.getSubjects().get(0).getText());

		EmailNotification emailNotification = (EmailNotification) notification;
		assertEquals(1, emailNotification.getEmailHeaders().size());
		Language lang = TaskModelProvider.getFactory().newLanguage();
		lang.setMapkey("en-UK");
		EmailNotificationHeader header = emailNotification.getEmailHeaders().get(lang);
		assertNotNull(header);
		assertEquals("Test of notification", header.getSubject());
		assertEquals("And here is the body", header.getBody());

		// check deadline expiration time
		for (int i = 0; i < 4; i++) {
			assertNotNull(deadlines.getStartDeadlines().get(i).getDate());
			long expirationTime = deadlines.getStartDeadlines().get(i).getDate().getTime() - System.currentTimeMillis();
			// Deadlines are separated 4 hours within each other, first starts at cca T+4
			assertEquals((i + 1) * 4, roundExpirationTime(expirationTime));
		}

		// verify notification
		notification = deadlines.getStartDeadlines().get(1).getEscalations().get(0).getNotifications().get(0);
		assertNotNull(notification);
		assertEquals(1, notification.getRecipients().size());
		assertEquals("john", notification.getRecipients().get(0).getId());

		assertEquals(1, notification.getSubjects().size());
		assertEquals("Test of notification", notification.getSubjects().get(0).getText());

		emailNotification = (EmailNotification) notification;
		assertEquals(1, emailNotification.getEmailHeaders().size());
		lang = TaskModelProvider.getFactory().newLanguage();
		lang.setMapkey("en-UK");
		header = emailNotification.getEmailHeaders().get(lang);
		assertNotNull(header);
		assertEquals("Test of notification", header.getSubject());
		assertEquals("And here is the body", header.getBody());

		// check deadline expiration time
		for (int i = 4; i < 8; i++) {
			assertNotNull(deadlines.getStartDeadlines().get(i).getDate());
			long expirationTime = deadlines.getStartDeadlines().get(i).getDate().getTime() - System.currentTimeMillis();
			// Deadlines are separated 6 hours within each other, first starts at cca T+6
			assertEquals((i - 4 + 1) * 6, roundExpirationTime(expirationTime));
		}
	}
	
	
	@Test
	public void testNotStartedNotifyMinimalWithHtml() {
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedNotify", "[tousers:john|subject:Test of notification|body:&lt;html&gt;"+
			    "&lt;body&gt;"+
			    "Reason {s}&lt;br/&gt;"+
			    "body of notification:&lt;br/&gt;"+
			    "work item id - ${workItemId}&lt;br/&gt;"+
			    "process instance id - ${processInstanceId}&lt;br/&gt;"+
			    "task id - ${taskId}&lt;br/&gt;" +
			    "http://localhost:8080/taskserver-url"+
			    "expiration time - ${doc['Deadlines'][0].expires}&lt;br/&gt;"+
			    "&lt;/body&gt;"+
			  "&lt;/html&gt;]@[4h]");
		
		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(1, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());
		
		// verify notification
		Notification notification = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().get(0);
		assertNotNull(notification);
		assertEquals(1, notification.getRecipients().size());
		assertEquals("john", notification.getRecipients().get(0).getId());
		
		assertEquals(1, notification.getSubjects().size());
		assertEquals("Test of notification", notification.getSubjects().get(0).getText());
		
		EmailNotification emailNotification = (EmailNotification) notification;
		assertEquals(1, emailNotification.getEmailHeaders().size());
		Language lang = TaskModelProvider.getFactory().newLanguage();
		lang.setMapkey("en-UK");
		EmailNotificationHeader header = emailNotification.getEmailHeaders().get(lang);
		assertNotNull(header);
		assertEquals("Test of notification", header.getSubject());
		assertTrue((header.getBody().indexOf("http://localhost:8080/taskserver-url") != -1));
		
		// check deadline expiration time
		assertNotNull(deadlines.getStartDeadlines().get(0).getDate());
		long expirationTime = deadlines.getStartDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();
		
		assertEquals(4, roundExpirationTime(expirationTime));
	}

	@Test
	public void testNotStartedNotifyMinimalWithHtmlWithISOExpirationTime() {
		OffsetDateTime fourHoursFromNow = OffsetDateTime.now().plusHours(4);
		String fourHoursFromNowFormatted = fourHoursFromNow.format(DateTimeFormatter.ISO_DATE_TIME);

		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedNotify", "[tousers:john|subject:Test of notification|body:&lt;html&gt;"+
				"&lt;body&gt;"+
				"Reason {s}&lt;br/&gt;"+
				"body of notification:&lt;br/&gt;"+
				"work item id - ${workItemId}&lt;br/&gt;"+
				"process instance id - ${processInstanceId}&lt;br/&gt;"+
				"task id - ${taskId}&lt;br/&gt;" +
				"http://localhost:8080/taskserver-url"+
				"expiration time - ${doc['Deadlines'][0].expires}&lt;br/&gt;"+
				"&lt;/body&gt;"+
				"&lt;/html&gt;]@[" + fourHoursFromNowFormatted + "]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(1, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());

		// verify notification
		Notification notification = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().get(0);
		assertNotNull(notification);
		assertEquals(1, notification.getRecipients().size());
		assertEquals("john", notification.getRecipients().get(0).getId());

		assertEquals(1, notification.getSubjects().size());
		assertEquals("Test of notification", notification.getSubjects().get(0).getText());

		EmailNotification emailNotification = (EmailNotification) notification;
		assertEquals(1, emailNotification.getEmailHeaders().size());
		Language lang = TaskModelProvider.getFactory().newLanguage();
		lang.setMapkey("en-UK");
		EmailNotificationHeader header = emailNotification.getEmailHeaders().get(lang);
		assertNotNull(header);
		assertEquals("Test of notification", header.getSubject());
		assertTrue((header.getBody().indexOf("http://localhost:8080/taskserver-url") != -1));

		// check deadline expiration time
		assertNotNull(deadlines.getStartDeadlines().get(0).getDate());
		long expirationTime = deadlines.getStartDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();

		assertEquals(4, roundExpirationTime(expirationTime));
	}

	@Test
	public void testNotStartedNotifyMinimalWithHtmlWithISOExpirationTimePeriodFormat() {
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedNotify", "[tousers:john|subject:Test of notification|body:&lt;html&gt;"+
				"&lt;body&gt;"+
				"Reason {s}&lt;br/&gt;"+
				"body of notification:&lt;br/&gt;"+
				"work item id - ${workItemId}&lt;br/&gt;"+
				"process instance id - ${processInstanceId}&lt;br/&gt;"+
				"task id - ${taskId}&lt;br/&gt;" +
				"http://localhost:8080/taskserver-url"+
				"expiration time - ${doc['Deadlines'][0].expires}&lt;br/&gt;"+
				"&lt;/body&gt;"+
				"&lt;/html&gt;]@[PT4H]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(1, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());

		// verify notification
		Notification notification = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().get(0);
		assertNotNull(notification);
		assertEquals(1, notification.getRecipients().size());
		assertEquals("john", notification.getRecipients().get(0).getId());

		assertEquals(1, notification.getSubjects().size());
		assertEquals("Test of notification", notification.getSubjects().get(0).getText());

		EmailNotification emailNotification = (EmailNotification) notification;
		assertEquals(1, emailNotification.getEmailHeaders().size());
		Language lang = TaskModelProvider.getFactory().newLanguage();
		lang.setMapkey("en-UK");
		EmailNotificationHeader header = emailNotification.getEmailHeaders().get(lang);
		assertNotNull(header);
		assertEquals("Test of notification", header.getSubject());
		assertTrue((header.getBody().indexOf("http://localhost:8080/taskserver-url") != -1));

		// check deadline expiration time
		assertNotNull(deadlines.getStartDeadlines().get(0).getDate());
		long expirationTime = deadlines.getStartDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();

		assertEquals(4, roundExpirationTime(expirationTime));
	}
	
	
    @Test
    public void testNotStartedNotifyMinimalWithBodyWithNewLineWithISOExpirationTimePeriodFormat() {
        WorkItem workItem = new WorkItemImpl();
        workItem.setParameter("NotStartedNotify", "[tousers:john|subject:Test of notification|body:viva er Beti\nmanque pierda]@[PT4H]");

        @SuppressWarnings("unchecked")
        Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
        assertNotNull(deadlines);
        assertEquals(1, deadlines.getStartDeadlines().size());
        assertEquals(0, deadlines.getEndDeadlines().size());
        assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
        assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());
        assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());

        // verify notification
        Notification notification = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().get(0);
        assertNotNull(notification);
        assertEquals(1, notification.getRecipients().size());
        assertEquals("john", notification.getRecipients().get(0).getId());

        assertEquals(1, notification.getSubjects().size());
        assertEquals("Test of notification", notification.getSubjects().get(0).getText());

        EmailNotification emailNotification = (EmailNotification) notification;
        assertEquals(1, emailNotification.getEmailHeaders().size());
        Language lang = TaskModelProvider.getFactory().newLanguage();
        lang.setMapkey("en-UK");
        EmailNotificationHeader header = emailNotification.getEmailHeaders().get(lang);
        assertNotNull(header);
        assertEquals("Test of notification", header.getSubject());
        assertEquals(header.getBody(), "viva er Beti\nmanque pierda");

        // check deadline expiration time
        assertNotNull(deadlines.getStartDeadlines().get(0).getDate());
        long expirationTime = deadlines.getStartDeadlines().get(0).getDate().getTime() - System.currentTimeMillis();

        assertEquals(4, roundExpirationTime(expirationTime));
    }

	@Test
	public void testNotStartedNotifyMinimalWithHtmlWithISOExpirationTimePeriodFormatWithRepeatCount() {
		WorkItem workItem = new WorkItemImpl();
		workItem.setParameter("NotStartedNotify", "[tousers:john|subject:Test of notification|body:&lt;html&gt;"+
				"&lt;body&gt;"+
				"Reason {s}&lt;br/&gt;"+
				"body of notification:&lt;br/&gt;"+
				"work item id - ${workItemId}&lt;br/&gt;"+
				"process instance id - ${processInstanceId}&lt;br/&gt;"+
				"task id - ${taskId}&lt;br/&gt;" +
				"http://localhost:8080/taskserver-url"+
				"expiration time - ${doc['Deadlines'][0].expires}&lt;br/&gt;"+
				"&lt;/body&gt;"+
				"&lt;/html&gt;]@[R2/PT4H]");

		@SuppressWarnings("unchecked")
		Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(workItem.getParameters(), Collections.EMPTY_LIST, null);
		assertNotNull(deadlines);
		assertEquals(2, deadlines.getStartDeadlines().size());
		assertEquals(0, deadlines.getEndDeadlines().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().size());
		assertEquals(1, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().size());
		assertEquals(0, deadlines.getStartDeadlines().get(0).getEscalations().get(0).getReassignments().size());

		// verify notification
		Notification notification = deadlines.getStartDeadlines().get(0).getEscalations().get(0).getNotifications().get(0);
		assertNotNull(notification);
		assertEquals(1, notification.getRecipients().size());
		assertEquals("john", notification.getRecipients().get(0).getId());

		assertEquals(1, notification.getSubjects().size());
		assertEquals("Test of notification", notification.getSubjects().get(0).getText());

		EmailNotification emailNotification = (EmailNotification) notification;
		assertEquals(1, emailNotification.getEmailHeaders().size());
		Language lang = TaskModelProvider.getFactory().newLanguage();
		lang.setMapkey("en-UK");
		EmailNotificationHeader header = emailNotification.getEmailHeaders().get(lang);
		assertNotNull(header);
		assertEquals("Test of notification", header.getSubject());
		assertTrue((header.getBody().indexOf("http://localhost:8080/taskserver-url") != -1));

		// check deadline expiration time
		for (int i = 0; i < 2; i++) {
			assertNotNull(deadlines.getStartDeadlines().get(i).getDate());
			long expirationTime = deadlines.getStartDeadlines().get(i).getDate().getTime() - System.currentTimeMillis();
			// Deadlines are separated 4 hours within each other, first starts at cca T+4
			assertEquals((i + 1) * 4, roundExpirationTime(expirationTime));
		}
	}

	@Test
	public void testparseDeadlineStringRepeatDuration() {
		long currentTimeInMillis = System.currentTimeMillis();

		// R/duration
		String repeatable1 = "[users:john]@[R2/PT4H]";
		List<Deadline> deadlines = HumanTaskHandlerHelper.parseDeadlineString(repeatable1,
																			  null,
																			  null,
																			  false);
		assertNotNull(deadlines);
		assertEquals(2,
					 deadlines.size());
		long firstDiff = Math.abs(deadlines.get(0).getDate().getTime() - currentTimeInMillis);
		long firstDiffHours = TimeUnit.HOURS.convert(firstDiff,
													 TimeUnit.MILLISECONDS);
		assertEquals(4,
					firstDiffHours);
		long secondDiff = Math.abs(deadlines.get(1).getDate().getTime() - deadlines.get(0).getDate().getTime());
		long secondDiffHours = TimeUnit.HOURS.convert(secondDiff,
													  TimeUnit.MILLISECONDS);
		assertEquals(4,
					secondDiffHours);
	}

	@Test
	public void testparseDeadlineStringRepeatStartDuration() {
		// R/start/duration
		String repeatable1 = "[users:john]@[R2/2019-05-27T13:00:00Z/PT4H]";
		List<Deadline> deadlines = HumanTaskHandlerHelper.parseDeadlineString(repeatable1,
																			  null,
																			  null,
                                                                              false);
		assertNotNull(deadlines);
		assertEquals(2,
					 deadlines.size());
		long diff = Math.abs(deadlines.get(1).getDate().getTime() - deadlines.get(0).getDate().getTime());
		long diffHours = TimeUnit.HOURS.convert(diff,
													  TimeUnit.MILLISECONDS);
		assertEquals(diffHours,
					 4);
	}

	@Test
	public void testparseDeadlineStringDurationEnd() {
		// R/duration/end
		String repeatable1 = "[users:john]@[R2/PT4H/2019-05-27T13:00:00Z]";
		List<Deadline> deadlines = HumanTaskHandlerHelper.parseDeadlineString(repeatable1,
																			  null,
																			  null,
                                                                              false);
		assertNotNull(deadlines);
		assertEquals(2,
					 deadlines.size());
		long diff = Math.abs(deadlines.get(1).getDate().getTime() - deadlines.get(0).getDate().getTime());
		long diffHours = TimeUnit.HOURS.convert(diff,
													  TimeUnit.MILLISECONDS);
		assertEquals(diffHours,
					 4);
	}

	@Test
	public void testparseDeadlineStringStartEnd() {
		// R/start/end
		String repeatable1 = "[users:john]@[R2/2019-05-27T13:00:00Z/2019-05-27T17:00:00Z]";
		List<Deadline> deadlines = HumanTaskHandlerHelper.parseDeadlineString(repeatable1,
																			  null,
																			  null,
                                                                              false);
		assertNotNull(deadlines);
		assertEquals(2,
					 deadlines.size());
		long diff = Math.abs(deadlines.get(1).getDate().getTime() - deadlines.get(0).getDate().getTime());
		long diffHours = TimeUnit.HOURS.convert(diff,
												TimeUnit.MILLISECONDS);
		assertEquals(diffHours,
					 4);
	}

	
	
	private long roundExpirationTime(long expirationTime) {
		BigDecimal a = new BigDecimal(expirationTime);
		a = a.setScale(1, 1);
		BigDecimal b = new BigDecimal(60*60*1000);
		b = b.setScale(1, 1);
		double devided = a.doubleValue()/b.doubleValue();

		long roundedValue = Math.round(devided);
		
		return roundedValue;
	}
}
