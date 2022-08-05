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

package org.jbpm.runtime.manager.impl.tx;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.time.JobContext;
import org.drools.core.time.SelfRemovalJobContext;
import org.drools.core.time.impl.TimerJobInstance;
import org.drools.persistence.api.OrderedTransactionSynchronization;
import org.drools.persistence.api.TransactionManager;
import org.drools.persistence.api.TransactionManagerFactory;
import org.drools.persistence.api.TransactionManagerHelper;
import org.jbpm.process.core.timer.GlobalSchedulerService;
import org.jbpm.process.core.timer.NamedJobContext;
import org.jbpm.process.core.timer.impl.DelegateSchedulerServiceInterceptor;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.timer.TimerManager;
import org.jbpm.process.instance.timer.TimerManager.ProcessJobContext;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

/**
 * This is for transactional global service so it could remove non-transactional resources
 * when the tx is rolled back
 */
public class NoTransactionalTimerResourcesCleanupAwareSchedulerServiceInterceptor extends
                                                                                  DelegateSchedulerServiceInterceptor {

    private RuntimeEnvironment environment;
    private RuntimeManager manager;

    public NoTransactionalTimerResourcesCleanupAwareSchedulerServiceInterceptor(RuntimeEnvironment environment,
                                                                                RuntimeManager manager,
                                                                                GlobalSchedulerService schedulerService) {
        super(schedulerService);
        this.environment = environment;
        this.manager = manager;
    }

    @Override
    public final void internalSchedule(final TimerJobInstance timerJobInstance) {
        if (hasEnvironmentEntry("IS_JTA_TRANSACTION", false)) {
            super.internalSchedule(timerJobInstance);
            return;
        }

        TransactionManager tm = getTransactionManager(timerJobInstance.getJobContext());
        List<Integer> invalidTxStatus = Arrays.asList(TransactionManager.STATUS_NO_TRANSACTION,
                                                      TransactionManager.STATUS_ROLLEDBACK,
                                                      TransactionManager.STATUS_COMMITTED);
        if (!invalidTxStatus.contains(tm.getStatus())) {
            TransactionManagerHelper.registerTransactionSyncInContainer(tm, new ScheduleTimerTransactionSynchronization(
                                                                                                                        timerJobInstance));
        }
        super.internalSchedule(timerJobInstance);
    }

    private class ScheduleTimerTransactionSynchronization extends OrderedTransactionSynchronization {

        private TimerJobInstance timerJobInstance;

        public ScheduleTimerTransactionSynchronization(TimerJobInstance timerJobInstance) {
            super(5, "NoTransactionalTimerResourcesCleanupAwareSchedulerServiceInterceptor");
            this.timerJobInstance = timerJobInstance;
        }

        @Override
        public void beforeCompletion() {}

        @Override
        public void afterCompletion(int status) {
            if (status == TransactionManager.STATUS_ROLLEDBACK) {
                // we remove from the timer from the map in timer manager
                JobContext ctxorig = timerJobInstance.getJobContext();
                ctxorig = (ctxorig instanceof SelfRemovalJobContext) ? ((SelfRemovalJobContext) ctxorig).getJobContext()
                        : ctxorig; // unwrap
                if (!(ctxorig instanceof ProcessJobContext)) {
                    return;
                }
                Optional<InternalKnowledgeRuntime> runtime = ctxorig.getInternalKnowledgeRuntime();
                if (runtime.isPresent()) {
                    TimerManager tm = ((InternalProcessRuntime) runtime.get().getProcessRuntime()).getTimerManager();
                    ProcessJobContext processJobContext = (ProcessJobContext) ctxorig;
                    tm.cancelTimer(processJobContext.getProcessInstanceId(), processJobContext.getTimer().getId());
                }
            }

        }

        @Override
        public int compareTo(OrderedTransactionSynchronization o) {
            if (o instanceof ScheduleTimerTransactionSynchronization) {
                if (this.timerJobInstance.equals(((ScheduleTimerTransactionSynchronization) o).timerJobInstance)) {
                    return 0;
                }
                return -1;
            }
            return super.compareTo(o);
        }

    }

    protected boolean hasEnvironmentEntry(String name, Object value) {
        Object envEntry = environment.getEnvironment().get(name);
        if (value == null) {
            return envEntry == null;
        }
        return value.equals(envEntry);
    }

    protected TransactionManager getTransactionManager(JobContext jobContext) {

        Object txm = getEnvironment(jobContext).get(EnvironmentName.TRANSACTION_MANAGER);
        if (txm != null && txm instanceof TransactionManager) {
            return (TransactionManager) txm;
        }

        return TransactionManagerFactory.get().newTransactionManager();
    }

    protected Environment getEnvironment(JobContext jobContext) {
        JobContext ctxorig = jobContext;
        if (ctxorig instanceof SelfRemovalJobContext) {
            ctxorig = ((SelfRemovalJobContext) ctxorig).getJobContext();
        }
        // first attempt to get knowledge runtime's environment if job context is a process one
        if (ctxorig instanceof ProcessJobContext) {
            return ((ProcessJobContext) ctxorig).getKnowledgeRuntime().getEnvironment();
        } else {
            // next if we have manager set use it to get ksession's environment of active RuntimeEngine
            // while running this there must be an active RuntimeEngine present
            if (manager != null) {
                RuntimeEngine engine = manager.getRuntimeEngine(ProcessInstanceIdContext.get(getProcessInstancId(
                                                                                                                 ctxorig)));
                return engine.getKieSession().getEnvironment();
            } else {
                // last resort use the runtime environment's environment template
                return environment.getEnvironment();
            }
        }
    }

    protected Long getProcessInstancId(JobContext jobContext) {

        if (jobContext instanceof ProcessJobContext) {
            return ((ProcessJobContext) jobContext).getProcessInstanceId();
        } else if (jobContext instanceof NamedJobContext) {
            return ((NamedJobContext) jobContext).getProcessInstanceId();
        } else {
            return null;
        }
    }
}
