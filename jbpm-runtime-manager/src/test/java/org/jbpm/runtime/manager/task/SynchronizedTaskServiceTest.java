/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.runtime.manager.task;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.jbpm.runtime.manager.impl.task.SynchronizedTaskService;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.internal.task.api.InternalTaskService;

/**
 *
 */
public class SynchronizedTaskServiceTest {

    @Test
    public void checkSynchronization() {

        // Sync task service setup
        KieSession kieSession = mock(KieSession.class);
        InternalTaskService delegate = mock(InternalTaskService.class);
        final InternalTaskService syncTaskService = new SynchronizedTaskService(kieSession, delegate);

        // test setup
        final CountDownLatch testOrderLatch = new CountDownLatch(1);
        final AtomicInteger operationCounter = new AtomicInteger(0);

        Runnable useSyncTaskServiceRunner = new Runnable() {

            @Override
            public void run() {
                try {
                    testOrderLatch.await();
                    operationCounter.incrementAndGet();
                } catch (InterruptedException e) {
                    // checked via the operation counter
                }

                syncTaskService.complete(23l, "nemo", null);
                operationCounter.incrementAndGet();
            }
        };

        // do test
        synchronized(kieSession) {
            // start thread
            new Thread(useSyncTaskServiceRunner).start();
            // let the thread run
            testOrderLatch.countDown();
            while( operationCounter.get() == 0 ) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // do nothing..
                    e.printStackTrace();
                }
            }
            assertEquals( "Thread operations failed!", operationCounter.get(), 1 );

            try {
                // Not guaranteed that the thread will run, but still 99% that thread will try to sync (and fail)
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // do nothing
            }
            assertEquals( "Synchroniation failed: " + operationCounter.get(), operationCounter.get(), 1 );
        }
        // end synchronization, let thread complete

        while( operationCounter.get() == 1 ) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // do nothing..
                e.printStackTrace();
            }
        }
    }
}
