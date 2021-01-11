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
import java.util.concurrent.locks.ReentrantLock;

import org.jbpm.runtime.manager.spi.RuntimeManagerLock;

public class DefaultRuntimeManagerLock implements RuntimeManagerLock {

    private ReentrantLock lock = new ReentrantLock(true);

    @Override
    public void lock() {
        lock.lock();
    }

    @Override
    public void lockInterruptible() throws InterruptedException {
        lock.lockInterruptibly();
    }

    @Override
    public void unlock() {
        lock.unlock();
    }

    @Override
    public boolean tryLock(long units, TimeUnit timeUnit) throws InterruptedException {
        return lock.tryLock(units, timeUnit);
    }

    @Override
    public boolean hasQueuedThreads() {
        return lock.hasQueuedThreads();
    }

    @Override
    public boolean isHeldByCurrentThread() {
        return lock.isHeldByCurrentThread();
    }

    @Override
    public int getQueueLength() {
        return lock.getQueueLength();
    }

    @Override
    public String toString() {
        return "DefaultRuntimeManagerLock [" + lock + "]";
    }



}
