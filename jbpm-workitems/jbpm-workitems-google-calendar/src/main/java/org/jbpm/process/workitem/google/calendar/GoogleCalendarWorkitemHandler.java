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

import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Event.Creator;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Wid(widfile = "GoogleCalendarDefinitions.wid", name = "GoogleCalendar",
        displayName = "GoogleCalendar",
        defaultHandler = "mvel: new org.jbpm.process.workitem.google.calendar.GoogleCalendarWorkitemHandler()",
        parameters = {
                @WidParameter(name = "AppName"),
                @WidParameter(name = "ClientSecret"),
                @WidParameter(name = "Action"),
                @WidParameter(name = "CalendarSummary"),
                @WidParameter(name = "EventSummary"),
                @WidParameter(name = "EventStart"),
                @WidParameter(name = "EventEnd"),
                @WidParameter(name = "EventAttendees"),
                @WidParameter(name = "EventCreator")
        },
        results = {
                @WidResult(name = "Calendar"),
                @WidResult(name = "AllCalendars"),
                @WidResult(name = "AllEvents")
        },
        mavenDepends = {
                @WidMavenDepends(group = "com.google.apis", artifact = "google-api-services-calendar", version = "v3-rev87-1.19.0"),
                @WidMavenDepends(group = "com.google.oauth-client", artifact = "google-oauth-client-jetty", version = "1.19.0"),
                @WidMavenDepends(group = "com.google.http-client", artifact = "google-http-client-jackson2", version = "1.19.0")
        })
public class GoogleCalendarWorkitemHandler extends AbstractLogOrThrowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(GoogleCalendarWorkitemHandler.class);
    private static final String RESULTS_CALENDAR = "Calendar";
    private static final String RESULTS_ALL_CALENDARS = "AllCalendars";
    private static final String RESULTS_ALL_EVENTS = "AllEvents";

    private GoogleCalendarAuth auth = new GoogleCalendarAuth();

    public void executeWorkItem(WorkItem workItem,
                                WorkItemManager workItemManager) {

        String paramAppName = (String) workItem.getParameter("AppName");
        String paramClientSecretJSON = (String) workItem.getParameter("ClientSecret");
        String paramAction = (String) workItem.getParameter("Action");
        String paramCalendarSummary = (String) workItem.getParameter("CalendarSummary");
        String paramEventSummary = (String) workItem.getParameter("EventSummary");
        String paramEventStart = (String) workItem.getParameter("EventStart");
        String paramEventEnd = (String) workItem.getParameter("EventEnd");
        String paramEventAttendees = (String) workItem.getParameter("EventAttendees");
        String paramEventCreator = (String) workItem.getParameter("EventCreator");

        Map<String, Object> results = new HashMap<String, Object>();

        CalendarActions calendarAction = CalendarActions.fromInput(paramAction);
        if (calendarAction == null) {
            logger.info(MessageFormat.format("Invalid calendar action {0}.",
                                             paramAction));
        } else {
            try {
                com.google.api.services.calendar.Calendar client = auth.getAuthorizedCalendar(paramAppName,
                                                                                              paramClientSecretJSON);

                if (calendarAction.equals(CalendarActions.ADD_CALENDAR)) {
                    results.put(RESULTS_CALENDAR,
                                addCalendar(client,
                                            paramCalendarSummary));
                } else if (calendarAction.equals(CalendarActions.RETURN_CALENDARS)) {
                    results.put(RESULTS_ALL_CALENDARS,
                                getAllCalendars(client));
                } else if (calendarAction.equals(CalendarActions.RETURN_EVENTS)) {
                    results.put(RESULTS_ALL_EVENTS,
                                getAllEvents(client,
                                             getCalendarIdBySummary(client,
                                                                    paramCalendarSummary)));
                } else if (calendarAction.equals(CalendarActions.ADD_EVENT)) {
                    results.put(RESULTS_ALL_EVENTS,
                                addEvent(client,
                                         getCalendarIdBySummary(client,
                                                                paramCalendarSummary),
                                         paramEventSummary,
                                         paramEventStart,
                                         paramEventEnd,
                                         paramEventAttendees,
                                         paramEventCreator
                                ));
                }
            } catch (Exception e) {
                handleException(e);
            }
        }

        workItemManager.completeWorkItem(workItem.getId(),
                                         results);
    }

    public String getCalendarIdBySummary(com.google.api.services.calendar.Calendar client,
                                         String summary) {
        String resultId = null;
        try {
            CalendarList calendarList = getAllCalendars(client);
            List<CalendarListEntry> entryList = calendarList.getItems();
            for (CalendarListEntry entry : entryList) {
                if (entry.getSummary().equalsIgnoreCase(summary)) {
                    resultId = entry.getId();
                }
            }
        } catch (Exception e) {
            logger.error(MessageFormat.format("Error retrieveing calendars: {0}",
                                              e.getMessage()));
        }
        return resultId;
    }

    public Calendar addCalendar(com.google.api.services.calendar.Calendar client,
                                String calendarSummary) throws IOException {
        Calendar entry = new Calendar();
        entry.setSummary(calendarSummary);
        return client.calendars().insert(entry).execute();
    }

    public CalendarList getAllCalendars(com.google.api.services.calendar.Calendar client) {
        try {
            return client.calendarList().list().execute();
        } catch (Exception e) {
            logger.error(MessageFormat.format("Error trying to get calendars: {0}.",
                                              e.getMessage()));
            return null;
        }
    }

    public Events getAllEvents(com.google.api.services.calendar.Calendar client,
                               String calendarId) throws IOException {
        return client.events().list(calendarId).execute();
    }

    public Event addEvent(com.google.api.services.calendar.Calendar client,
                          String calendarId,
                          String paramEventSummary,
                          String paramEventStart,
                          String paramEventEnd,
                          String paramEventAttendees,
                          String paramEventCreator) throws Exception {
        Event event = createNewEvent(paramEventSummary,
                                     paramEventStart,
                                     paramEventEnd,
                                     paramEventAttendees,
                                     paramEventCreator);
        return client.events().insert(calendarId,
                                      event).execute();
    }

    private static Event createNewEvent(String paramEventSummary,
                                        String paramEventStart,
                                        String paramEventEnd,
                                        String paramEventAttendees,
                                        String paramEventCreator) throws Exception {
        Event event = new Event();
        event.setSummary(paramEventSummary);

        DateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z",
                                                 Locale.ENGLISH);

        if (paramEventStart != null) {
            DateTime startDateTime = new DateTime(format.parse(paramEventStart));
            event.setStart(new EventDateTime().setDateTime(startDateTime));
        }

        if (paramEventEnd != null) {
            DateTime endDateTime = new DateTime(format.parse(paramEventEnd));
            event.setEnd(new EventDateTime().setDateTime(endDateTime));
        }

        if (paramEventAttendees != null) {
            List<String> attendees = Arrays.asList(paramEventAttendees.split(","));
            List<EventAttendee> attendiesList = new ArrayList<>();
            for (String attendee : attendees) {
                EventAttendee newAttendee = new EventAttendee();
                newAttendee.setEmail(attendee);
                attendiesList.add(newAttendee);
            }
            event.setAttendees(attendiesList);
        }

        if (paramEventCreator != null) {
            Creator creator = new Creator();
            creator.setEmail(paramEventCreator);
            event.setCreator(creator);
        }

        return event;
    }

    public void abortWorkItem(WorkItem wi,
                              WorkItemManager wim) {
    }

    // for testing
    public void setAuth(GoogleCalendarAuth auth) {
        this.auth = auth;
    }
}
