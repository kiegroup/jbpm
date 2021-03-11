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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class RuntimeManagerLockStrategyTest {

    protected static final Logger logger = LoggerFactory.getLogger(RuntimeManagerLockStrategyTest.class);

    private static final int NUMBER_OF_WORKING_THREADS = 10;

    @Parameters(name = "Strategy : {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                                            {"org.jbpm.runtime.manager.impl.lock.SerializableRuntimeManagerLockStrategy"},
                                            {"org.jbpm.runtime.manager.impl.lock.TimeoutRuntimeManagerLockStrategy"},
                                            {"org.jbpm.runtime.manager.impl.lock.InterruptibleRuntimeManagerLockStrategy"},
                                            {"org.jbpm.runtime.manager.impl.lock.LegacyRuntimeManagerLockStrategy"}
        });
    }

    private String strategy;
    private ExecutorService executorService;

    public RuntimeManagerLockStrategyTest(String strategy) {
        this.strategy = strategy;
    }

    @Before
    public void init() {
        executorService = Executors.newFixedThreadPool(NUMBER_OF_WORKING_THREADS);
    }

    @After
    public void destroy() {
        executorService.shutdown();
        executorService = null;
    }

    @Test
    public void testLockStrategies() throws InterruptedException {
        logger.info("entering test {}", this.strategy);
        final CriticalSectionClash sectionDetection = new CriticalSectionClash();
        String factory = DebugRuntimeManagerLockFactory.class.getName();
        final RuntimeManagerLockStrategy lockStrategy = new RuntimeManagerLockStrategyFactory(strategy, factory).createLockStrategy(strategy);
        AtomicInteger count = new AtomicInteger(0);
        List<Future<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            futures.add(executorService.submit(new Runner(count, sectionDetection, lockStrategy,false)));
        }
        futures.forEach(e -> {
            try {
                // assert every process didn't not have a critical section violation
                Assert.assertTrue(e.get());
            } catch (Exception e1) {
                // do nothing
            }
        });
        logger.info("finished jobs test {}", this.strategy);
        executorService.shutdown();
        executorService.awaitTermination(20, TimeUnit.SECONDS);

        // assert hangs
        Assert.assertTrue(executorService.isTerminated());
        logger.info("exiting test {}", this.strategy);
    }

    @Test
    public void testSelfRuntimeLockStrategies() throws InterruptedException {
        logger.info("entering test {}", this.strategy);
        final CriticalSectionClash sectionDetection = new CriticalSectionClash();
        String factory = SelfReleaseRuntimeManagerLockFactory.class.getName();
        final RuntimeManagerLockStrategy lockStrategy = new RuntimeManagerLockStrategyFactory(strategy, factory).createLockStrategy(strategy);
        AtomicInteger count = new AtomicInteger(0);
        List<Future<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            futures.add(executorService.submit(new Runner(count, sectionDetection, lockStrategy,false)));
        }
        futures.forEach(e -> {
            try {
                // assert every process didn't not have a critical section violation
                Assert.assertTrue(e.get());
            } catch (Exception e1) {
                // do nothing
            }
        });
        logger.info("finished jobs test {}", this.strategy);
        executorService.shutdown();
        executorService.awaitTermination(20, TimeUnit.SECONDS);

        // assert hangs
        Assert.assertTrue(executorService.isTerminated());
        logger.info("exiting test {}", this.strategy);
    }
}
