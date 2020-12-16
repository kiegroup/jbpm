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

import java.util.concurrent.TimeUnit;

import org.jbpm.runtime.manager.spi.RuntimeManagerLock;


public class TimeoutRuntimeManagerLockStrategy extends AbstractRuntimeManagerLockStrategy {
    private static long TIMEOUT_LOCK = Long.getLong("org.kie.jbpm.runtime.manager.lock.timeout", 5000L);

    @Override
    protected void lock(RuntimeManagerLock lock) throws InterruptedException {
        if(!lock.tryLock(TIMEOUT_LOCK, TimeUnit.MILLISECONDS)) {
            throw new InterruptedException("This lock was interrupted by timeout when trying to get the lock " + lock);
        }
    }

    @Override
    protected void unlock(RuntimeManagerLock lock) {
        lock.unlock();
    }


}
