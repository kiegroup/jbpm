/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.process.workitem.google.calendar;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.Calendar.Calendars;
import com.google.api.services.calendar.model.CalendarList;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.jbpm.test.AbstractBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GoogleCalendarWorkitemHandlerTest extends AbstractBaseTest {

    @Mock
    GoogleCalendarAuth auth;

    @Mock
    Calendar client;

    @Mock
    Calendars calendars;

    @Mock
    Calendar.CalendarList calendarsList;

    @Mock
    Calendar.CalendarList.List calendarsListList;

    @Before
    public void setUp() {
        try {
            CalendarList calendarListModel = new com.google.api.services.calendar.model.CalendarList();
            when(client.calendars()).thenReturn(calendars);
            when(client.calendarList()).thenReturn(calendarsList);
            when(calendarsList.list()).thenReturn(calendarsListList);
            when(calendarsListList.execute()).thenReturn(calendarListModel);
            when(auth.getAuthorizedCalendar(anyString(),
                                            anyString())).thenReturn(client);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testHandler() throws Exception {

        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Action",
                              "returnCalendars");

        GoogleCalendarWorkitemHandler handler = new GoogleCalendarWorkitemHandler();
        handler.setAuth(auth);
        handler.executeWorkItem(workItem,
                                manager);

        handler.executeWorkItem(workItem,
                                manager);
        assertNotNull(manager.getResults());
        assertEquals(1,
                     manager.getResults().size());
        assertTrue(manager.getResults().containsKey(workItem.getId()));

        assertTrue((manager.getResults().get(workItem.getId())).get("AllCalendars") instanceof CalendarList);
    }
}
