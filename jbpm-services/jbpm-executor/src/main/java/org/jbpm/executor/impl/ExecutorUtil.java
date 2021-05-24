/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.executor.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.io.input.ClassLoaderObjectInputStream;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ExecutorUtil {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorUtil.class);

    private ExecutorUtil () {
        // do nothing
    }

    public static CommandContext toCommandContext(byte[] data, ClassLoader cl) {
        if (data == null) {
            return new CommandContext();
        }
        try (ObjectInputStream in = new ClassLoaderObjectInputStream(cl, new ByteArrayInputStream(data))) {
            return (CommandContext) in.readObject();
        } catch (IOException e) {
            logger.warn("Exception while deserializing context data", e);
            return new CommandContext();
        } catch (Exception e) {
            logger.error("Unexpected error when reading request data", e);
            throw new RuntimeException(e);
        }
    }

    public static ExecutionResults toExecutionResult(byte[] data, ClassLoader cl) {
        if (data == null) {
            return new ExecutionResults();
        }
        try (ObjectInputStream in = new ClassLoaderObjectInputStream(cl, new ByteArrayInputStream(data))) {
            return (ExecutionResults) in.readObject();
        } catch (IOException e) {
            logger.warn("Exception while deserializing context data", e);
            return new ExecutionResults();
        } catch (Exception e) {
            logger.error("Unexpected error when reading request data", e);
            throw new RuntimeException(e);
        }
    }

    public static byte [] toByteArray(Object ctx) {
        if (ctx == null) {
            return null;
        }

        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(ctx);
            return bout.toByteArray();
        } catch (IOException e) {
            logger.warn("Error serializing context data", e);
            return null;
        }
    }

    public static ClassLoader getClassLoader(String deploymentId) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (deploymentId == null) {
            return cl;
        }

        InternalRuntimeManager manager = ((InternalRuntimeManager) RuntimeManagerRegistry.get().getManager(deploymentId));
        if (manager != null && manager.getEnvironment().getClassLoader() != null) {
            cl = manager.getEnvironment().getClassLoader();
        }

        return cl;
    }
}
