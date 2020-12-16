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

import java.util.Iterator;
import java.util.ServiceLoader;

import org.jbpm.runtime.manager.spi.RuntimeManagerLockFactory;
import org.jbpm.runtime.manager.spi.RuntimeManagerLockStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RuntimeManagerLockStrategyFactory {

    protected static final Logger logger = LoggerFactory.getLogger(RuntimeManagerLockStrategyFactory.class);

    public String lockStrategyClassName;
    public String lockFactoryClassName;

    public RuntimeManagerLockStrategyFactory() {
        this(System.getProperty("org.kie.jbpm.runtime.manager.lock.strategy", LegacyRuntimeManagerLockStrategy.class.getName()),
             System.getProperty("org.kie.jbpm.runtime.manager.lock.factory", DefaultRuntimeManagerLockFactory.class.getName()));
    }

    public RuntimeManagerLockStrategyFactory(String strategy, String factory) {
        this.lockFactoryClassName = factory;
        this.lockStrategyClassName = strategy;
    }

    public RuntimeManagerLockStrategy createFreeLockStrategy() {
        return new FreeRuntimeManagerLockStrategy();
    }

    public RuntimeManagerLockStrategy createLockStrategy(String identifier) {
        RuntimeManagerLockStrategy runtimeManagerLockStrategy = null;

        // choose the right locking system
        ServiceLoader<RuntimeManagerLockStrategy> loader = ServiceLoader.load(RuntimeManagerLockStrategy.class);
        Iterator<RuntimeManagerLockStrategy> iterator = loader.iterator();
        while (iterator.hasNext()) {
            RuntimeManagerLockStrategy lockStrategy = iterator.next();
            if (lockStrategy.getClass().getName().equals(lockStrategyClassName)) {
                runtimeManagerLockStrategy = lockStrategy;
                break;
            }
        }


        if (runtimeManagerLockStrategy == null) {
            throw new RuntimeException("Could not find a proper RuntimeManagerLockStrategy for value " + lockStrategyClassName);
        }

        RuntimeManagerLockFactory runtimeManagerLockFactory = createRuntimeManagerLockFactory();
        runtimeManagerLockStrategy.init(runtimeManagerLockFactory);
        logger.info("RuntimeManagerLockStrategy {} with lock factory {} is created for {}", 
                    runtimeManagerLockStrategy.getClass().getName(),
                    runtimeManagerLockFactory.getClass().getName(), identifier);
        return runtimeManagerLockStrategy;
    }

    private RuntimeManagerLockFactory createRuntimeManagerLockFactory () {
        // chose the right factory lock
        RuntimeManagerLockFactory runtimeManagerLockFactory = null;

        ServiceLoader<RuntimeManagerLockFactory> loader = ServiceLoader.load(RuntimeManagerLockFactory.class);
        Iterator<RuntimeManagerLockFactory> iterator = loader.iterator();
        while (iterator.hasNext()) {
            RuntimeManagerLockFactory currentRuntimeManagerLockFactory = iterator.next();
            if (currentRuntimeManagerLockFactory.getClass().getName().equals(lockFactoryClassName)) {
                runtimeManagerLockFactory = currentRuntimeManagerLockFactory;
                break;
            }
        }

        // fallback to default
        if (runtimeManagerLockFactory == null) {
            throw new RuntimeException("Could not find a proper RuntimeManagerLockFactory for value " + lockFactoryClassName);
        }
        return runtimeManagerLockFactory;
    }
}
