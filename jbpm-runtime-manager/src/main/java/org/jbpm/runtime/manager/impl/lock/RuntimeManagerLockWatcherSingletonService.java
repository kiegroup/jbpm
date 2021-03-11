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

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jbpm.runtime.manager.spi.RuntimeManagerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuntimeManagerLockWatcherSingletonService {

    private class LockWatchInfo implements Comparable<LockWatchInfo> {

        RuntimeManagerLock lock;
        long processInstanceId;
        long startTime;

        public LockWatchInfo(long processInstanceId, RuntimeManagerLock lock) {
            this.processInstanceId = processInstanceId;
            this.lock = lock;
            this.startTime = System.currentTimeMillis();
        }

        public long getProcessInstanceId() {
            return processInstanceId;
        }

        public long getCurrentProcessingTime() {
            return System.currentTimeMillis() - startTime;
        }

        @Override
        public int compareTo(LockWatchInfo o) {
            return ((Long) startTime).compareTo(o.startTime);
        }

        @Override
        public String toString() {
            return "[Lock Watch for " + processInstanceId + " locked since " + startTime + "]";
        }
    }

    protected static final Logger logger = LoggerFactory.getLogger(RuntimeManagerLockWatcherSingletonService.class);

    private static final RuntimeManagerLockWatcherSingletonService SERVICE = new RuntimeManagerLockWatcherSingletonService();

    private Long watchLockPolling;
    private Long maxLockProcessingTime;

    private int count;
    private ExecutorService executorService;

    private Queue<LockWatchInfo> locksWatched;

    private RuntimeManagerLockWatcherSingletonService() {
        this.locksWatched = new PriorityQueue<>();
        this.count = 0;
    }
    private class RuntimeManagerLockReaper implements Runnable {

        @Override
        public void run() {
            try {
                while(!Thread.currentThread().isInterrupted()) {
                    long maxTimeToWait = tryForcedUnlock();
                    Thread.sleep(maxTimeToWait);
                }
            } catch(InterruptedException e) {
                // do nothing. thread has been killed because a shutdown.
            }
        }

    }

    public long tryForcedUnlock() {

        synchronized (locksWatched) {
            while(!locksWatched.isEmpty()) {
                LockWatchInfo info = locksWatched.peek();
                long idleTime = info.getCurrentProcessingTime();
                if(maxLockProcessingTime > idleTime) {
                    return maxLockProcessingTime - idleTime;
                }
                try {
                    long timeExceeded = idleTime - maxLockProcessingTime;
                    logger.info("Max process time lock exceeded for process instance id {} by {} ms. Trying forceful release lock {}", info.processInstanceId, timeExceeded, info.lock);
                    info.lock.forceUnlock();
                } catch(UnsupportedOperationException e) {
                    logger.warn("Runtime manager lock implementation {} does not allow self release for process instance id {}", info.lock.getClass().getName(), info.processInstanceId);
                }
                // stop watching this lock. We gave already the information
                locksWatched.poll();
            }
        }

        return watchLockPolling;
    }





    public void watch(long processInstanceId, RuntimeManagerLock lock) {
        synchronized (locksWatched) {
            locksWatched.add(new LockWatchInfo(processInstanceId, lock));
        }
    }

    public void unwatch(long processInstanceId) {
        synchronized (locksWatched) {
            locksWatched.removeIf(e -> e.getProcessInstanceId() == processInstanceId);
        }
    }

    public boolean isWatched(int processInstanceId) {
        synchronized (locksWatched) {
            return locksWatched.stream().anyMatch(e -> e.getProcessInstanceId() == processInstanceId);
        }
    }

    private void setWatchLockPolling(Long watchLockPolling) {
        this.watchLockPolling = watchLockPolling;
    }

    private void setMaxLockProcessingTime(Long maxLockProcessingTime) {
        this.maxLockProcessingTime = maxLockProcessingTime;
    }

    private void start() {
        count++;
        if(count > 0 && watchLockPolling > 0 && executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
            executorService.submit(new RuntimeManagerLockReaper());
            logger.info("Started watching locks");
        }
    }

    public void unreference () {
        synchronized (this) {
            if( executorService != null && --count == 0) {
                logger.info("Stopped watching locks");
                executorService.shutdownNow();
                executorService = null;
            }
        }
    }

    public static RuntimeManagerLockWatcherSingletonService reference() {
        return reference(Long.getLong("org.kie.jbpm.lock.polling", 0L), Long.getLong("org.kie.jbpm.lock.maxProcessingTime", 60000L));
    }

    public static RuntimeManagerLockWatcherSingletonService reference(Long watchLockPolling, Long maxLockProcessingTime) {
        synchronized (SERVICE) {
            SERVICE.setMaxLockProcessingTime(maxLockProcessingTime);
            SERVICE.setWatchLockPolling(watchLockPolling);
            SERVICE.start();
            return SERVICE;
        }
    }

    public static boolean isActive() {
        synchronized (SERVICE) {
            return SERVICE.executorService != null;
        }
    }

}
