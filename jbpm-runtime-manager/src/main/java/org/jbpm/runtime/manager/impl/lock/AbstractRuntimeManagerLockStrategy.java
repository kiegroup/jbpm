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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import org.jbpm.runtime.manager.spi.RuntimeManagerLock;
import org.jbpm.runtime.manager.spi.RuntimeManagerLockFactory;
import org.jbpm.runtime.manager.spi.RuntimeManagerLockStrategy;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


abstract class AbstractRuntimeManagerLockStrategy implements RuntimeManagerLockStrategy {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractRuntimeManagerLockStrategy.class);

    protected Map<Long, RuntimeManagerLockThreadsInfo> engineLocks; 

    private RuntimeManagerLockFactory runtimeManagerLockFactory;

    
    protected AbstractRuntimeManagerLockStrategy() {
        this.runtimeManagerLockFactory = new DefaultRuntimeManagerLockFactory();
        this.engineLocks = new HashMap<>();
    }

    @Override
    public void init(RuntimeManagerLockFactory factory) {
        this.runtimeManagerLockFactory = factory;
    }

    @Override
    public RuntimeManagerLock lock(Long id, RuntimeEngine runtime) throws InterruptedException {
        RuntimeManagerLockThreadsInfo lockThreadsInfo = null;
        synchronized (engineLocks) {
            lockThreadsInfo = engineLocks.computeIfAbsent(id, (Long key) -> new RuntimeManagerLockThreadsInfo(runtimeManagerLockFactory.newRuntimeManagerLock()));
            lockThreadsInfo.set();
        }
        logger.debug("Trying to get a lock {} for {} by {}", lockThreadsInfo, id, runtime);
        try {
            lock(lockThreadsInfo.getRuntimeManagerLock());
        } catch(InterruptedException e) {
            logger.warn("Interrupted lock {}", lockThreadsInfo.getRuntimeManagerLock());
            throw e;
        }
        logger.debug("Lock {} taken for {} by {} for waiting threads by {}", lockThreadsInfo, id, runtime, lockThreadsInfo.count());
        return lockThreadsInfo.getRuntimeManagerLock();
    }

    protected abstract void lock(RuntimeManagerLock lock) throws InterruptedException;
    
    @Override
    public void unlock(Long id, RuntimeEngine runtime) {
        RuntimeManagerLockThreadsInfo lockThreadsInfo = null;
        synchronized (engineLocks) {
            lockThreadsInfo = engineLocks.get(id);
            if (lockThreadsInfo == null) {
                logger.warn("[LOCK] lock {} is already removed for {} unlocked by {}", id, lockThreadsInfo, runtime);
                return;
            }
            lockThreadsInfo.unset();
            if (lockThreadsInfo.count() == 0) {
                logger.debug("[LOCK] Removing lock for {} for lock  {} from list as non is waiting for it by {}", id, lockThreadsInfo, runtime);
                engineLocks.remove(id);
            }
        }

        if (lockThreadsInfo.isHeldByCurrentThread()) {
            unlock(lockThreadsInfo.getRuntimeManagerLock());
            logger.debug("[LOCK] process instance id {} with thread info {} unlocked by {}", id, lockThreadsInfo, runtime);
        } else {
            logger.warn("[LOCK] trying to unlock for {} for lock {} no lock held by {}", id, lockThreadsInfo, runtime);
        }
    }

    protected abstract void unlock(RuntimeManagerLock lock);
}

class RuntimeManagerLockThreadsInfo {

    private Set<Long> threadIds;
    private RuntimeManagerLock runtimeManagerLock;

    public RuntimeManagerLockThreadsInfo(RuntimeManagerLock runtimeManagerLock) {
        this.threadIds = new HashSet<>();
        this.runtimeManagerLock = runtimeManagerLock;
    }
    public void set() {
        this.threadIds.add(Thread.currentThread().getId());
    }
    public void unset() {
        this.threadIds.remove(Thread.currentThread().getId());
    }
    public int count() {
        return this.threadIds.size();
    }

    public void lock() {
        runtimeManagerLock.lock();
    }
    public void unlock() {
        runtimeManagerLock.unlock();
    }
    public boolean hasQueuedThreads() {
        return runtimeManagerLock.hasQueuedThreads();
    }
    public boolean isHeldByCurrentThread() {
        return runtimeManagerLock.isHeldByCurrentThread();
    }
    
    public RuntimeManagerLock getRuntimeManagerLock() {
        return runtimeManagerLock;
    }

}
