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
import java.util.concurrent.locks.ReentrantLock;

import org.jbpm.runtime.manager.spi.RuntimeManagerLock;


public class DebugRuntimeManagerLock implements RuntimeManagerLock {

    private ReentrantLock lock = new ReentrantLock(true);
    private List<StackTraceElement[]> traces;
    private long currentThreadId;

    public DebugRuntimeManagerLock() {
        traces = new ArrayList<>();
        currentThreadId = -1;
    }
    
    @Override
    public void lock() {
        lock.lock();
        addTraces();
    }

    @Override
    public boolean tryLock(long units, TimeUnit timeUnit) throws InterruptedException {
        boolean outcome = lock.tryLock(units, timeUnit);
        if(outcome) {
            addTraces();
        }
        return outcome;
    }

    @Override
    public void lockInterruptible() throws InterruptedException {
        lock.lockInterruptibly();
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
        if(currentThreadId == Thread.currentThread().getId() && lock.getHoldCount() == 1) {
            traces.clear(); // clean up
        }
        lock.unlock();
    }

    @Override
    public boolean isHeldByCurrentThread() {
        return lock.isHeldByCurrentThread();
    }
    
    @Override
    public boolean hasQueuedThreads() {
        return lock.hasQueuedThreads();
    }

    @Override
    public int getQueueLength() {
        return lock.getQueueLength();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DebugRuntimeManagerLock [");
        builder.append(lock);
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
