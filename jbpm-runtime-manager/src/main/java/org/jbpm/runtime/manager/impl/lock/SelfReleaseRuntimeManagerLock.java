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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import org.jbpm.runtime.manager.spi.RuntimeManagerLock;


public class SelfReleaseRuntimeManagerLock implements RuntimeManagerLock {
    
    static class SelfRelaseCapableSync extends AbstractQueuedSynchronizer {

        private static final long serialVersionUID = 1L;

        final boolean nonfairTryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0) // overflow
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }

        protected final boolean tryRelease(int releases) {
            int c = getState() - releases;
            boolean free = false;
            if (c == 0) {
                free = true;
                setExclusiveOwnerThread(null);
            }
            setState(c);
            return free;
        }

        final int getHoldCount() {
            return isHeldExclusively() ? getState() : 0;
        }

        final boolean isLocked() {
            return getState() != 0;
        }

        final void lock() {
            if (compareAndSetState(0, 1))
                setExclusiveOwnerThread(Thread.currentThread());
            else
                acquire(1);
        }

        protected final boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
        }

        protected final boolean isHeldExclusively() {
            return getExclusiveOwnerThread() == Thread.currentThread();
        }
    }

    private SelfRelaseCapableSync sync;
    private List<StackTraceElement[]> traces;
    private long currentThreadId;

    public SelfReleaseRuntimeManagerLock() {
        sync = new SelfRelaseCapableSync();
        traces = new ArrayList<>();
        currentThreadId = -1;
    }
    
    @Override
    public void lock() {
        sync.lock();
        addTraces();
    }

    @Override
    public boolean tryLock(long units, TimeUnit timeUnit) throws InterruptedException {
        boolean outcome = sync.tryAcquireNanos(1, timeUnit.toMillis(units));
        if(outcome) {
            addTraces();
        }
        return outcome;
    }

    @Override
    public void lockInterruptible() throws InterruptedException {
        sync.acquireInterruptibly(1);
        addTraces();
    }

    private void addTraces() {
        long newThreadId = Thread.currentThread().getId();
        if(currentThreadId < 0 || currentThreadId != newThreadId) {
            currentThreadId = newThreadId;
            traces.clear();
        }
        // we add all stack traces during reentrant lock
        traces.add(Thread.currentThread().getStackTrace());
    }
    @Override
    public void unlock() {
        // only we clear up when count is 0
        if(currentThreadId == Thread.currentThread().getId() && sync.getHoldCount() == 1) {
            traces.clear(); // clean up
        }
        sync.release(1);
    }

    @Override
    public void forceUnlock() {
        sync.release(1);
        traces.clear(); // clean up
        currentThreadId = -1;
    }
    @Override
    public boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

    @Override
    public boolean isHeldByCurrentThread() {
        return sync.isHeldExclusively();
    }

    @Override
    public int getQueueLength() {
        return sync.getQueueLength();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SelfReleaseRuntimeManagerLock [");
        builder.append(sync);
        builder.append("]\n");

        builder.append("--------CURRENT REENTRANT STACKTRACE-----------------------\n");

        for (int i = 0; i < traces.size(); i++) {
            builder.append("\tSTACKTRACE " + i + "\n");
            StackTraceElement[] trace = traces.get(i);
            for(int j = 0; j < trace.length; j++) {
                builder.append("\t\t\t" + trace[j] + "\n");
            }
        }
        builder.append("--------END CURRENT REENTRANT STACKTRACE-------------------\n");
        return builder.toString();
    }

}
