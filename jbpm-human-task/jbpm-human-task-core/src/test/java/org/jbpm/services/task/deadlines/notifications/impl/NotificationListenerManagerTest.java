/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.services.task.deadlines.notifications.impl;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class NotificationListenerManagerTest {

    private static String EMAIL_LISTENER = "org.jbpm.services.task.deadlines.notifications.impl.email.EmailNotificationListener";
    private static String MOCK_LISTENER = "org.jbpm.services.task.deadlines.notifications.impl.MockNotificationListener";

    @After
    @Before
    public void reset() {
        System.clearProperty(NotificationListenerManager.KIE_LISTENER_EXCLUDE);
        System.clearProperty(NotificationListenerManager.KIE_LISTENER_INCLUDE);
        NotificationListenerManager.get().reset();
    }

    @Test
    public void testNoFlagsLists() {
        assertThat(NotificationListenerManager.get().getNotificationListeners().size(), is(2));
    }

    @Test
    public void testExcludeLists() {
        System.setProperty(NotificationListenerManager.KIE_LISTENER_EXCLUDE, EMAIL_LISTENER);
        NotificationListenerManager.get().reset();
        assertThat(NotificationListenerManager.get().getNotificationListeners().size(), is(1));
        assertThat(NotificationListenerManager.get().getNotificationListeners().get(0).getClass().getName(), is(MOCK_LISTENER));
    }

    @Test
    public void testIncludeLists() {
        System.setProperty(NotificationListenerManager.KIE_LISTENER_INCLUDE, MOCK_LISTENER);
        NotificationListenerManager.get().reset();
        assertThat(NotificationListenerManager.get().getNotificationListeners().size(), is(1));
        assertThat(NotificationListenerManager.get().getNotificationListeners().get(0).getClass().getName(), is(MOCK_LISTENER));
    }

    @Test
    public void testBothLists() {
        System.setProperty(NotificationListenerManager.KIE_LISTENER_EXCLUDE, EMAIL_LISTENER);
        System.setProperty(NotificationListenerManager.KIE_LISTENER_INCLUDE, EMAIL_LISTENER);
        NotificationListenerManager.get().reset();
        assertThat(NotificationListenerManager.get().getNotificationListeners().size(), is(1));
        assertThat(NotificationListenerManager.get().getNotificationListeners().get(0).getClass().getName(), is(EMAIL_LISTENER));
    }

    @Test
    public void testEmptyIncludeList() {
        System.setProperty(NotificationListenerManager.KIE_LISTENER_INCLUDE, "");
        NotificationListenerManager.get().reset();
        assertThat(NotificationListenerManager.get().getNotificationListeners().size(), is(0));
    }

    @Test
    public void testMultipleElementsIncludeList() {
        System.setProperty(NotificationListenerManager.KIE_LISTENER_INCLUDE, EMAIL_LISTENER + "," + MOCK_LISTENER);
        NotificationListenerManager.get().reset();
        assertThat(NotificationListenerManager.get().getNotificationListeners().size(), is(2));
        assertThat(NotificationListenerManager.get().getNotificationListeners().stream().map(e -> e.getClass().getName()).collect(toList()), CoreMatchers.hasItems(EMAIL_LISTENER, MOCK_LISTENER));
    }

}
