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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jbpm.runtime.manager.spi.RuntimeManagerLock;
import org.jbpm.runtime.manager.spi.RuntimeManagerLockFactory;
import org.jbpm.runtime.manager.spi.RuntimeManagerLockStrategy;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LegacyRuntimeManagerLockStrategy implements RuntimeManagerLockStrategy {

    private static final Logger logger = LoggerFactory.getLogger(LegacyRuntimeManagerLockStrategy.class);

    protected ConcurrentMap<Long, RuntimeManagerLock> engineLocks; 

    private RuntimeManagerLockFactory runtimeManagerLockFactory;

    
    
    public LegacyRuntimeManagerLockStrategy() {
        this.engineLocks = new ConcurrentHashMap<>();
        this.runtimeManagerLockFactory = new DefaultRuntimeManagerLockFactory();
    }
    @Override
    public void init(RuntimeManagerLockFactory factory) {
        this.runtimeManagerLockFactory = factory;
    }

    @Override
    public RuntimeManagerLock lock(Long id, RuntimeEngine runtime) {
        RuntimeManagerLock newLock = runtimeManagerLockFactory.newRuntimeManagerLock();
        RuntimeManagerLock lock = engineLocks.putIfAbsent(id, newLock);
        if (lock == null) {
            lock = newLock;
            logger.debug("New lock created as it did not exist before");
        } else {
            logger.debug("Lock exists with {} waiting threads", lock.getQueueLength());
        }
        logger.debug("Trying to get a lock {} for {} by {}", lock, id, runtime);
        lock.lock();
        logger.debug("Lock {} taken for {} by {} for waiting threads by {}", lock, id, runtime, lock.hasQueuedThreads());
        return lock;
    }

    @Override
    public void unlock(Long id, RuntimeEngine runtime) {
        RuntimeManagerLock lock = engineLocks.get(id);
        if (lock != null) {
            if (!lock.hasQueuedThreads()) {
                logger.debug("Removing lock {} from list as non is waiting for it by {}", lock, runtime);
                engineLocks.remove(id);
            }
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                logger.debug("{} unlocked by {}", lock, runtime);
            }
        }
    }

}
