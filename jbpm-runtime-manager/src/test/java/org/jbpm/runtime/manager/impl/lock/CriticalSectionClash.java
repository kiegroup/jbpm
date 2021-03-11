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

package org.jbpm.runtime.manager.impl.lock;

public class CriticalSectionClash {

    private volatile int id = -1;

    public void set(int newId) {
        if (id != -1 && id != newId) {
            throw new RuntimeException("some threads are accessing at the same time to the critical section");
        }
        id = newId;

    }

    public void unset() {
        id = -1;
    }
}
