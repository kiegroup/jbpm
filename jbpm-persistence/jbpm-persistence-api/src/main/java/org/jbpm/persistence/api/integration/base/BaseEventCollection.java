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

package org.jbpm.persistence.api.integration.base;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.jbpm.persistence.api.integration.EventCollection;
import org.jbpm.persistence.api.integration.InstanceView;

/**
 * Base event collection that collects all events avoiding duplicates.
 * No extra filtering is performed.
 *
 */
public class BaseEventCollection implements EventCollection {

    private static final long serialVersionUID = -5241582057875657702L;
    private Map<InstanceView<?>, InstanceView<?>> eventsMap = new LinkedHashMap<>();

    @Override
    public void update(InstanceView<?> item) {
        this.eventsMap.put(item, item);
    }

    @Override
    public void remove(InstanceView<?> item) {
        this.eventsMap.put(item, item);
    }

    @Override
    public Collection<InstanceView<?>> getEvents() {
        return this.eventsMap.values();
    }

    @Override
    public void add(InstanceView<?> event) {
        this.eventsMap.put(event, event);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream input) throws ClassNotFoundException, IOException {
        Object collection = input.readObject();
        if (collection instanceof Set) {
            eventsMap = new LinkedHashMap<>();
            for (InstanceView<?> event : (Set<InstanceView<?>>)collection) {
                eventsMap.put(event, event);
            }
        } else {
            eventsMap = (Map<InstanceView<?>, InstanceView<?>>) collection;
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(eventsMap);
    }

}
