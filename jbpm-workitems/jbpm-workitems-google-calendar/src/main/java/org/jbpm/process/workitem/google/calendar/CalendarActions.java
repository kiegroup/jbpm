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

public enum CalendarActions {
    RETURN_CALENDARS,
    ADD_CALENDAR,
    RETURN_EVENTS,
    ADD_EVENT;

    public static CalendarActions fromInput(String input) {
        if (input == null) {
            return null;
        }
        if (input.equalsIgnoreCase("returnCalendars")) {
            return RETURN_CALENDARS;
        } else if (input.equalsIgnoreCase("addCalendar")) {
            return ADD_CALENDAR;
        } else if (input.equalsIgnoreCase("addEvent")) {
            return ADD_EVENT;
        } else if (input.equalsIgnoreCase("returnEvents")) {
            return RETURN_EVENTS;
        } else {
            return null;
        }
    }

}
