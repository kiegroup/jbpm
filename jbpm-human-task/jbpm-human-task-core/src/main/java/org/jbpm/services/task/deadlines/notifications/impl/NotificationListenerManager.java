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
package org.jbpm.services.task.deadlines.notifications.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Predicate;

import org.jbpm.services.task.deadlines.NotificationListener;
import org.kie.api.runtime.EnvironmentName;
import org.kie.internal.identity.IdentityProvider;
import org.kie.internal.task.api.TaskContext;
import org.kie.internal.task.api.UserInfo;
import org.kie.internal.task.api.model.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.emptyList;

/**
 * Manages broadcasting of notification events to all found listeners 
 *
 */
public class NotificationListenerManager {

    public static final String KIE_LISTENER_EXCLUDE = "org.kie.jbpm.notification_listeners.exclude";

    public static final String KIE_LISTENER_INCLUDE = "org.kie.jbpm.notification_listeners.include";

    private static final Logger logger = LoggerFactory.getLogger(NotificationListenerManager.class);

    private static ServiceLoader<NotificationListener> listenersLoaded = ServiceLoader.load(NotificationListener.class);

    private static NotificationListenerManager INSTANCE = new NotificationListenerManager();

    private List<NotificationListener> listeners = new ArrayList<NotificationListener>();

    private Optional<List<String>> excludeLists;

    private Optional<List<String>> includeLists;

    public static Optional<List<String>> propertyToList(String propertyName) {
        String name = System.getProperty(propertyName);
        if (name == null) {
            return Optional.empty();
        } else if (name.isEmpty()) {
            return Optional.of(emptyList());
        }
        return Optional.of(Arrays.asList(name.trim().split("\\s*,\\s*")));
    }

    private NotificationListenerManager() {
        reset();
    }

    public void reset() {
        excludeLists = propertyToList(KIE_LISTENER_EXCLUDE);
        includeLists = propertyToList(KIE_LISTENER_INCLUDE);

        listeners.clear();
        Predicate<String> predicate = createPredicate();
        for (NotificationListener listener : listenersLoaded) {
            if (predicate.test(listener.getClass().getName())) {
                listeners.add(listener);
            }
        }
    }

    private Predicate<String> createPredicate() {
        if (includeLists.isPresent()) {
            return e -> includeLists.get().contains(e);
        } else if (excludeLists.isPresent()) {
            return e -> !excludeLists.get().contains(e);
        } else {
            return e -> true; 
        }
    }

    public void registerAdditionalNotificationListener(List<NotificationListener> additionalNotificationListener) {
        for (NotificationListener listener : additionalNotificationListener) {
            listeners.add(listener);
        }
    }

    public List<NotificationListener> getNotificationListeners() {
        return listeners;
    }

    public void broadcast(TaskContext taskContext, NotificationEvent event, UserInfo userInfo) {
        IdentityProvider identityProvider = (IdentityProvider) taskContext.get(EnvironmentName.IDENTITY_PROVIDER);
        if(identityProvider != null) {
            identityProvider.setContextIdentity(System.getProperty("org.jbpm.ht.admin.user", "Administrator"));
        }

        try {
            broadcast(event, userInfo);
        } finally {
            if(identityProvider != null) {
                identityProvider.removeContextIdentity();
            }
        }
    }
    /**
     * Broadcast given event to all listeners independently meaning catches possible exceptions to 
     * avoid breaking notification by listeners
     * @param event notification event to be sent
     * @param params additional parameters see NotificationListener.onNotification for details.
     * 
     * @see NotificationListener#onNotification(NotificationEvent, Object...)
     */
    public void broadcast(NotificationEvent event, UserInfo userInfo) {

        for (NotificationListener listener : listeners) {
            try {
                logger.debug("Sending notification {} to {} with params {}", event, listener, userInfo);
                listener.onNotification(event, userInfo);
            } catch (Exception e) {
                logger.warn("Exception encountered while notifying listener {} with event {} - error {}",
                            listener, event, e.getMessage());
            }
        }
    }

    public static NotificationListenerManager get() {
        return INSTANCE;
    }
}
