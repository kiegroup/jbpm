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

import org.jbpm.runtime.manager.spi.RuntimeManagerLock;
import org.jbpm.runtime.manager.spi.RuntimeManagerLockFactory;
import org.jbpm.runtime.manager.spi.RuntimeManagerLockStrategy;
import org.kie.api.runtime.manager.RuntimeEngine;


public class FreeRuntimeManagerLockStrategy implements RuntimeManagerLockStrategy {

    @Override
    public void init(RuntimeManagerLockFactory factory) {
        // do nothing
    }

    @Override
    public RuntimeManagerLock lock(Long id, RuntimeEngine runtime) {
        return null;
    }

    @Override
    public void unlock(Long id, RuntimeEngine runtime) {
        // do nothing
    }

}
