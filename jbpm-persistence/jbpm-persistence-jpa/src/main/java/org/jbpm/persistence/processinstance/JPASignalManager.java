/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.persistence.processinstance;

import java.util.ArrayList;
import java.util.List;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.jbpm.persistence.api.ProcessPersistenceContext;
import org.jbpm.persistence.api.ProcessPersistenceContextManager;
import org.jbpm.process.core.async.AsyncSignalEventCommand;
import org.jbpm.process.core.async.BatchAsyncSignalEventCommand;
import org.jbpm.process.instance.event.DefaultSignalManager;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutorService;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JPASignalManager extends DefaultSignalManager {

    private final long BATCH_THREASHOLD = Long.getLong("org.kie.jbpm.signal.batch.threshold", 0L);
    private final long BATCH_SIZE = Long.getLong("org.kie.jbpm.signal.batch.size", 50L);

    private static final String ASYNC_SIGNAL_PREFIX = "ASYNC-";
    private static final Logger logger = LoggerFactory.getLogger(JPASignalManager.class);

    public JPASignalManager(InternalKnowledgeRuntime kruntime) {
        super(kruntime);
    }

    private boolean isAsync(String signalName, Object event) {
        if (!signalName.startsWith(ASYNC_SIGNAL_PREFIX)) {
            return false;
        }

        String actualSignalType = signalName.replaceFirst(ASYNC_SIGNAL_PREFIX, "");

        ProcessPersistenceContextManager contextManager = (ProcessPersistenceContextManager) getKnowledgeRuntime().getEnvironment().get(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER);
        ProcessPersistenceContext context = contextManager.getProcessPersistenceContext();

        List<Long> processInstancesToSignalList = context.getProcessInstancesWaitingForEvent(actualSignalType);
        // handle signal asynchronously
        if (signalName.startsWith(ASYNC_SIGNAL_PREFIX)) {

            RuntimeManager runtimeManager = ((RuntimeManager) getKnowledgeRuntime().getEnvironment().get("RuntimeManager"));
            ExecutorService executorService = (ExecutorService) getKnowledgeRuntime().getEnvironment().get("ExecutorService");
            if (runtimeManager != null && executorService != null) {

                for (Long processInstanceId : processInstancesToSignalList) {
                    logger.info("About to create an async signal {} to {} for deployment ", signalName, processInstanceId, runtimeManager.getIdentifier());
                    CommandContext ctx = new CommandContext();
                    ctx.setData("deploymentId", runtimeManager.getIdentifier());
                    ctx.setData("processInstanceId", processInstanceId);
                    ctx.setData("Signal", actualSignalType);
                    ctx.setData("Event", event);
                    executorService.scheduleRequest(AsyncSignalEventCommand.class.getName(), ctx);
                }

                return true;
            } else {
                logger.warn("Signal should be sent asynchronously but there is no executor service available, continuing sync...");
            }

        } else if (BATCH_THREASHOLD > 0 && processInstancesToSignalList.size() >= BATCH_THREASHOLD) {
            int currentIndex = 0;
            RuntimeManager runtimeManager = ((RuntimeManager) getKnowledgeRuntime().getEnvironment().get("RuntimeManager"));
            ExecutorService executorService = (ExecutorService) getKnowledgeRuntime().getEnvironment().get("ExecutorService");
            if (runtimeManager != null && executorService != null) {
                for (currentIndex = 0; currentIndex < processInstancesToSignalList.size(); currentIndex += BATCH_SIZE) {
                    List<Long> batch = processInstancesToSignalList.subList(currentIndex, Math.min(processInstancesToSignalList.size(), (int) (currentIndex + BATCH_SIZE)));
                    logger.info("About to create an batched signal {} to {} for deployment ", signalName, batch, runtimeManager.getIdentifier());
                    CommandContext ctx = new CommandContext();
                    ctx.setData("deploymentId", runtimeManager.getIdentifier());
                    ctx.setData("processInstanceIds", new ArrayList<>(batch));
                    ctx.setData("Signal", actualSignalType);
                    ctx.setData("Event", event);
                    executorService.scheduleRequest(BatchAsyncSignalEventCommand.class.getName(), ctx);
                }
            }
            return true;
        }
        return false;
    }

    public void signalEvent(String type, Object event) {
        String actualSignalType = type.replaceFirst(ASYNC_SIGNAL_PREFIX, "");
        super.signalEventStart(actualSignalType, event);

        if (isAsync(type, event)) {
            return;
        }


        InternalRuntimeManager runtimeManager = ((InternalRuntimeManager) getKnowledgeRuntime().getEnvironment().get("RuntimeManager"));
        if (runtimeManager != null) {
            sendSignalKieSessionScope(runtimeManager, (KieSession) getKnowledgeRuntime(), actualSignalType, event);
        } else {
            ProcessPersistenceContextManager contextManager = (ProcessPersistenceContextManager) getKnowledgeRuntime().getEnvironment().get(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER);
            ProcessPersistenceContext context = contextManager.getProcessPersistenceContext();

            List<Long> processInstancesToSignalList = context.getProcessInstancesWaitingForEvent(actualSignalType);

            for (long id : processInstancesToSignalList) {
                logger.info("About to signal event {} to process instance id {} for kieSession {}", type, id, getKnowledgeRuntime());
                try {
                    getKnowledgeRuntime().getProcessInstance(id);
                    super.signalEvent(id, actualSignalType, event);
                } catch (IllegalStateException e) {
                    // IllegalStateException can be thrown when using RuntimeManager
                    // and invalid ksession was used for given context
                } catch (RuntimeException e) {
                    logger.warn("Exception when loading process instance for signal '{}', instance with id {} will not be signaled", type, id);
                }
            }
        }

        logger.info("About to signal as a start event {} jpa", type);
        // this will include start events and cannot be avoided probably
        // given that is after execution all process Instance Id only start event will be executed (no duplications)
        super.signalEventStart(actualSignalType, event);
    }

    @Override
    public void sendSignalKieSessionScope(InternalRuntimeManager runtimeManager, KieSession kieSession, String type, Object event) {
        if (isAsync(type, event)) {
            return;
        }
        String actualSignalType = type.replaceFirst(ASYNC_SIGNAL_PREFIX, "");
        super.sendSignalKieSessionScope(runtimeManager, kieSession, actualSignalType, event);
    }

    @Override
    public void sendSignalProcessInstanceScope(InternalRuntimeManager runtimeManager, long processInstanceId, String type, Object event) {
        if (isAsync(type, event)) {
            return;
        }
        String actualSignalType = type.replaceFirst(ASYNC_SIGNAL_PREFIX, "");
        super.sendSignalProcessInstanceScope(runtimeManager, processInstanceId, actualSignalType, event);
    }
    

    @Override
    public void sendSignalRuntimeManagerScope(InternalRuntimeManager runtimeManager, String type, Object event) {
        if (isAsync(type, event)) {
            return;
        }
        String actualSignalType = type.replaceFirst(ASYNC_SIGNAL_PREFIX, "");
        super.sendSignalRuntimeManagerScope(runtimeManager, actualSignalType, event);
    }
}
