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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.jbpm.runtime.manager.spi.RuntimeManagerLockStrategy;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomRuntimeManagerLockStrategyTest {

    protected static final Logger logger = LoggerFactory.getLogger(CustomRuntimeManagerLockStrategyTest.class);

    private static final int NUMBER_OF_WORKING_THREADS = 10;

    private ExecutorService executorService;

    @Before
    public void init() {
        executorService = Executors.newFixedThreadPool(NUMBER_OF_WORKING_THREADS);
    }

    @After
    public void destroy() {
        executorService.shutdown();
        executorService = null;
    }

    @Test(timeout = 10000)
    public void testTimeout() throws Exception {
        final CriticalSectionClash sectionDetection = new CriticalSectionClash();
        String factory = DebugRuntimeManagerLockFactory.class.getName();
        final RuntimeManagerLockStrategy lockStrategy = new RuntimeManagerLockStrategyFactory("org.jbpm.runtime.manager.impl.lock.TimeoutRuntimeManagerLockStrategy", factory).createLockStrategy("timeout");
        AtomicInteger count = new AtomicInteger(0);

        executorService.submit(new Runner(count, sectionDetection, lockStrategy, true)).get();
        executorService.submit(new Runner(count, sectionDetection, lockStrategy, false));

        logger.info("finished jobs test TimeoutRuntimeManagerLockStrategy");
        executorService.shutdown();
        executorService.awaitTermination(20, TimeUnit.SECONDS);

        // assert hangs
        Assert.assertTrue(executorService.isTerminated());
        logger.info("exiting test TimeoutRuntimeManagerLockStrategy");
    }


    @Test(timeout = 10000)
    public void testInterruptible() throws Exception {
        final CriticalSectionClash sectionDetection = new CriticalSectionClash();
        String factory = DebugRuntimeManagerLockFactory.class.getName();
        final RuntimeManagerLockStrategy lockStrategy = new RuntimeManagerLockStrategyFactory("org.jbpm.runtime.manager.impl.lock.InterruptibleRuntimeManagerLockStrategy", factory).createLockStrategy("interruptible");
        AtomicInteger count = new AtomicInteger(0);

        executorService.submit(new Runner(count, sectionDetection, lockStrategy, true)).get();
        Future<Boolean> job = executorService.submit(new Runner(count, sectionDetection, lockStrategy, false));
        Thread.sleep(1000L);
        job.cancel(true);

        logger.info("finished jobs test TimeoutRuntimeManagerLockStrategy");
        executorService.shutdown();
        executorService.awaitTermination(20, TimeUnit.SECONDS);

        // assert hangs
        Assert.assertTrue(executorService.isTerminated());
        logger.info("exiting test TimeoutRuntimeManagerLockStrategy");
    }
}
