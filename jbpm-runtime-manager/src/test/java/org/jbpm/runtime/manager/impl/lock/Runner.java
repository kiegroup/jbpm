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

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.jbpm.runtime.manager.spi.RuntimeManagerLockStrategy;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Runner implements Callable<Boolean> {

    protected static final Logger logger = LoggerFactory.getLogger(Runner.class);

    private AtomicInteger count;
    private CriticalSectionClash sectionDetection;
    private RuntimeManagerLockStrategy lockStrategy;
    private boolean bad;

    public Runner (AtomicInteger count, CriticalSectionClash csc, RuntimeManagerLockStrategy lockStrategy, boolean bad) {
        this.count = count;
        this.sectionDetection = csc;
        this.lockStrategy = lockStrategy;
        this.bad = bad;
    }
    
    @Override
    public Boolean call() throws Exception {
        try {
            int id = count.incrementAndGet();
            lockStrategy.lock(1L, (RuntimeEngine) null);
            sectionDetection.set(id);
            logger.info("Critical section thread id {} thread id {}", id, Thread.currentThread().getId());
            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                // do nothing
            }
            sectionDetection.unset();
            if(!bad) {
                lockStrategy.unlock(1L, (RuntimeEngine) null);
            }
        } catch (InterruptedException e1) {
            logger.info("interrupted !");
            // do nothing it is because of the lock
        } catch (Exception e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

}