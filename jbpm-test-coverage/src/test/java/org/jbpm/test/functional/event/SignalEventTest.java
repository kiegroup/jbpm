/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.test.functional.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.jbpm.test.JbpmTestCase;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.audit.ProcessInstanceLog;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

public class SignalEventTest extends JbpmTestCase {

    private static final String END_THROW_DEFAULT = "org/jbpm/test/functional/event/Signal-endThrow-default.bpmn2";
    private static final String END_THROW_DEFAULT_ID = "org.jbpm.test.functional.event.Signal-endThrow-default";

    private static final String END_THROW_INSTANCE = "org/jbpm/test/functional/event/Signal-endThrow-instance.bpmn2";
    private static final String END_THROW_INSTANCE_ID = "org.jbpm.test.functional.event.Signal-endThrow-instance";

    private static final String END_THROW_PROJECT = "org/jbpm/test/functional/event/Signal-endThrow-project.bpmn2";
    private static final String END_THROW_PROJECT_ID = "org.jbpm.test.functional.event.Signal-endThrow-project";

    private static final String INTERMEDIATE_CATCH = "org/jbpm/test/functional/event/Signal-intermediateCatch.bpmn2";
    private static final String INTERMEDIATE_CATCH_ID = "org.jbpm.test.functional.event.Signal-intermediateCatch";

    private static final String INTERMEDIATE_THROW_DEFAULT =
            "org/jbpm/test/functional/event/Signal-intermediateThrow-default.bpmn2";
    private static final String INTERMEDIATE_THROW_DEFAULT_ID =
            "org.jbpm.test.functional.event.Signal-intermediateThrow-default";

    private static final String INTERMEDIATE_THROW_INSTANCE =
            "org/jbpm/test/functional/event/Signal-intermediateThrow-instance.bpmn2";
    private static final String INTERMEDIATE_THROW_INSTANCE_ID =
            "org.jbpm.test.functional.event.Signal-intermediateThrow-instance";

    private static final String INTERMEDIATE_THROW_PROJECT =
            "org/jbpm/test/functional/event/Signal-intermediateThrow-project.bpmn2";
    private static final String INTERMEDIATE_THROW_PROJECT_ID =
            "org.jbpm.test.functional.event.Signal-intermediateThrow-project";

    private static final String START_CATCH = "org/jbpm/test/functional/event/Signal-startCatch.bpmn2";
    private static final String START_CATCH_ID = "org.jbpm.test.functional.event.Signal-startCatch";

    private static final String SUBPROCESS_CATCH = "org/jbpm/test/functional/event/Signal-subprocessCatch.bpmn2";
    private static final String SUBPROCESS_CATCH_ID = "org.jbpm.test.functional.event.Signal-subprocessCatch";

    private enum Scope {
        DEFAULT, PROCESS_INSTANCE, PROJECT
    }

    private KieSession getKieSession(Strategy strategy) {
        Context<?> context = strategy == Strategy.PROCESS_INSTANCE ? ProcessInstanceIdContext.get() : EmptyContext.get();
        return getRuntimeEngine(context).getKieSession();
    }

    private void testSignal(Strategy strategy, Scope scope) {
        createRuntimeManager(strategy, (String) null, INTERMEDIATE_CATCH);

        KieSession ksession = getKieSession(strategy);
        ProcessInstance pi1 = ksession.startProcess(INTERMEDIATE_CATCH_ID);
        getKieSession(strategy).startProcess(INTERMEDIATE_CATCH_ID);

        switch (scope) {
            case DEFAULT:
                ksession.signalEvent("commonSignal", null);
                break;
            case PROCESS_INSTANCE:
                ksession.signalEvent("commonSignal", null, pi1.getId());
                break;
            case PROJECT:
                manager.signalEvent("commonSignal", null);
                break;
            default:
                throw new IllegalArgumentException("unknown scope");
        }

        List<? extends ProcessInstanceLog> instances = getRuntimeEngine().getAuditService().findProcessInstances();
        Assertions.assertThat(instances).hasSize(2);

        Assertions.assertThat(instances.get(0).getProcessId()).isEqualTo(INTERMEDIATE_CATCH_ID);
        Assertions.assertThat(instances.get(0).getStatus()).isEqualTo(ProcessInstance.STATE_COMPLETED);

        Assertions.assertThat(instances.get(1).getProcessId()).isEqualTo(INTERMEDIATE_CATCH_ID);
        if (strategy == Strategy.SINGLETON) {
            Assertions.assertThat(instances.get(1).getStatus()).isEqualTo(scope == Scope.PROCESS_INSTANCE ?
                    ProcessInstance.STATE_ACTIVE : ProcessInstance.STATE_COMPLETED);
        } else if (strategy == Strategy.PROCESS_INSTANCE) {
            Assertions.assertThat(instances.get(1).getStatus()).isEqualTo(scope == Scope.PROJECT ?
                    ProcessInstance.STATE_COMPLETED : ProcessInstance.STATE_ACTIVE);
        }
    }

    @Test
    public void testSignalSessionSingletonStrategy() {
        testSignal(Strategy.SINGLETON, Scope.DEFAULT);
    }

    @Test
    public void testSignalProcessInstanceSingletonStrategy() {
        testSignal(Strategy.SINGLETON, Scope.PROCESS_INSTANCE);
    }

    @Test
    public void testSignalRuntimeManagerSingletonStrategy() {
        testSignal(Strategy.SINGLETON, Scope.PROJECT);
    }

    @Test
    public void testSignalSessionPerProcessInstanceStrategy() {
        testSignal(Strategy.PROCESS_INSTANCE, Scope.DEFAULT);
    }

    @Test
    public void testSignalProcessInstancePerProcessInstanceStrategy() {
        testSignal(Strategy.PROCESS_INSTANCE, Scope.PROCESS_INSTANCE);
    }

    @Test
    public void testSignalRuntimeManagerPerProcessInstanceStrategy() {
        testSignal(Strategy.PROCESS_INSTANCE, Scope.PROJECT);
    }

    private void testSignalEndThrowIntermediateCatch(Strategy strategy, Scope scope) {
        String throwingProcessFile;
        String throwingProcessId;
        switch (scope) {
            case DEFAULT:
                throwingProcessFile = END_THROW_DEFAULT;
                throwingProcessId = END_THROW_DEFAULT_ID;
                break;
            case PROJECT:
                throwingProcessFile = END_THROW_PROJECT;
                throwingProcessId = END_THROW_PROJECT_ID;
                break;
            case PROCESS_INSTANCE:
                throwingProcessFile = END_THROW_INSTANCE;
                throwingProcessId = END_THROW_INSTANCE_ID;
                break;
            default:
                throw new IllegalArgumentException("unknown scope");
        }

        createRuntimeManager(strategy, null, throwingProcessFile, INTERMEDIATE_CATCH);

        getKieSession(strategy).startProcess(INTERMEDIATE_CATCH_ID);
        getKieSession(strategy).startProcess(throwingProcessId);

        List<? extends ProcessInstanceLog> instances = getRuntimeEngine().getAuditService().findProcessInstances();
        Assertions.assertThat(instances).hasSize(2);

        Assertions.assertThat(instances.get(0).getProcessId()).isEqualTo(INTERMEDIATE_CATCH_ID);
        Assertions.assertThat(instances.get(1).getProcessId()).isEqualTo(throwingProcessId);

        if (strategy == Strategy.SINGLETON) {
            Assertions.assertThat(instances.get(0).getStatus()).isEqualTo(scope == Scope.PROCESS_INSTANCE ?
                    ProcessInstance.STATE_ACTIVE : ProcessInstance.STATE_COMPLETED);
            Assertions.assertThat(instances.get(1).getStatus()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        } else if (strategy == Strategy.PROCESS_INSTANCE) {
            Assertions.assertThat(instances.get(0).getStatus()).isEqualTo(scope == Scope.PROJECT ?
                    ProcessInstance.STATE_COMPLETED : ProcessInstance.STATE_ACTIVE);
            Assertions.assertThat(instances.get(1).getStatus()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        }
    }

    @Test
    public void testSignalEndThrowIntermediateCatchSingletonStrategyKieSessionScope() {
        testSignalEndThrowIntermediateCatch(Strategy.SINGLETON, Scope.DEFAULT);
    }

    @Test
    public void testSignalEndThrowIntermediateCatchSingletonStrategyRuntimeManagerScope() {
        testSignalEndThrowIntermediateCatch(Strategy.SINGLETON, Scope.PROJECT);
    }

    @Test
    public void testSignalEndThrowIntermediateCatchSingletonStrategyProcessInstanceScope() {
        testSignalEndThrowIntermediateCatch(Strategy.SINGLETON, Scope.PROCESS_INSTANCE);
    }

    @Test
    public void testSignalEndThrowIntermediateCatchPerProcessInstanceStrategyKieSessionScope() {
        testSignalEndThrowIntermediateCatch(Strategy.PROCESS_INSTANCE, Scope.DEFAULT);
    }

    @Test
    public void testSignalEndThrowIntermediateCatchPerProcessInstanceStrategyRuntimeManagerScope() {
        testSignalEndThrowIntermediateCatch(Strategy.PROCESS_INSTANCE, Scope.PROJECT);
    }

    @Test
    public void testSignalEndThrowIntermediateCatchPerProcessInstanceStrategyProcessInstanceScope() {
        testSignalEndThrowIntermediateCatch(Strategy.PROCESS_INSTANCE, Scope.PROCESS_INSTANCE);
    }

    private void testSignalIntermediateThrowStartCatch(Strategy strategy, Scope scope, boolean run) {
        String throwingProcessFile;
        String throwingProcessId;
        switch (scope) {
            case DEFAULT:
                throwingProcessFile = INTERMEDIATE_THROW_DEFAULT;
                throwingProcessId = INTERMEDIATE_THROW_DEFAULT_ID;
                break;
            case PROJECT:
                throwingProcessFile = INTERMEDIATE_THROW_PROJECT;
                throwingProcessId = INTERMEDIATE_THROW_PROJECT_ID;
                break;
            case PROCESS_INSTANCE:
                throwingProcessFile = INTERMEDIATE_THROW_INSTANCE;
                throwingProcessId = INTERMEDIATE_THROW_INSTANCE_ID;
                break;
            default:
                throw new IllegalArgumentException("unknown scope");
        }

        createRuntimeManager(strategy, null, throwingProcessFile, START_CATCH);

        getKieSession(strategy).startProcess(throwingProcessId);

        List<? extends ProcessInstanceLog> instances = getRuntimeEngine().getAuditService().findProcessInstances();
        Assertions.assertThat(instances).hasSize(run ? 2 : 1);

        Assertions.assertThat(instances.get(0).getProcessId()).isEqualTo(throwingProcessId);
        Assertions.assertThat(instances.get(0).getStatus()).isEqualTo(ProcessInstance.STATE_COMPLETED);

        if (run) {
            Assertions.assertThat(instances.get(1).getProcessId()).isEqualTo(START_CATCH_ID);
            Assertions.assertThat(instances.get(1).getStatus()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        }
    }

    @Test
    public void testSignalIntermediateThrowStartCatchSingletonStrategyKieSessionScope() {
        testSignalIntermediateThrowStartCatch(Strategy.SINGLETON, Scope.DEFAULT, true);
    }

    @Test
    public void testSignalIntermediateThrowStartCatchSingletonStrategyRuntimeManagerScope() {
        testSignalIntermediateThrowStartCatch(Strategy.SINGLETON, Scope.PROJECT, true);
    }

    @Test
    public void testSignalIntermediateThrowStartCatchSingletonStrategyProcessInstanceScope() {
        testSignalIntermediateThrowStartCatch(Strategy.SINGLETON, Scope.PROCESS_INSTANCE, false);
    }

    @Test
    public void testSignalIntermediateThrowStartCatchPerProcessInstanceStrategyKieSessionScope() {
        testSignalIntermediateThrowStartCatch(Strategy.PROCESS_INSTANCE, Scope.DEFAULT, false);
    }

    @Test
    public void testSignalIntermediateThrowStartCatchPerProcessInstanceStrategyRuntimeManagerScope() {
        testSignalIntermediateThrowStartCatch(Strategy.PROCESS_INSTANCE, Scope.PROJECT, true);
    }

    @Test
    public void testSignalIntermediateThrowStartCatchPerProcessInstanceStrategyProcessInstanceScope() {
        testSignalIntermediateThrowStartCatch(Strategy.PROCESS_INSTANCE, Scope.PROCESS_INSTANCE, false);
    }

    private void testSignalSubProcess(Strategy strategy, Scope scope) {
        String throwingProcessFile;
        String throwingProcessId;
        switch (scope) {
            case DEFAULT:
                throwingProcessFile = INTERMEDIATE_THROW_DEFAULT;
                throwingProcessId = INTERMEDIATE_THROW_DEFAULT_ID;
                break;
            case PROJECT:
                throwingProcessFile = INTERMEDIATE_THROW_PROJECT;
                throwingProcessId = INTERMEDIATE_THROW_PROJECT_ID;
                break;
            case PROCESS_INSTANCE:
                throwingProcessFile = INTERMEDIATE_THROW_INSTANCE;
                throwingProcessId = INTERMEDIATE_THROW_INSTANCE_ID;
                break;
            default:
                throw new IllegalArgumentException("unknown scope");
        }

        createRuntimeManager(strategy, null, SUBPROCESS_CATCH, throwingProcessFile, INTERMEDIATE_CATCH);

        getKieSession(strategy).startProcess(INTERMEDIATE_CATCH_ID);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("subprocess", throwingProcessId);
        getKieSession(strategy).startProcess(SUBPROCESS_CATCH_ID, parameters);

        List<? extends ProcessInstanceLog> instances = getRuntimeEngine().getAuditService().findProcessInstances();
        Assertions.assertThat(instances).hasSize(3);

        Assertions.assertThat(instances.get(0).getProcessId()).isEqualTo(INTERMEDIATE_CATCH_ID);
        Assertions.assertThat(instances.get(1).getProcessId()).isEqualTo(SUBPROCESS_CATCH_ID);
        Assertions.assertThat(instances.get(2).getProcessId()).isEqualTo(throwingProcessId);

        if (strategy == Strategy.SINGLETON) {
            Assertions.assertThat(instances.get(0).getStatus()).isEqualTo(scope == Scope.PROCESS_INSTANCE ?
                    ProcessInstance.STATE_ACTIVE : ProcessInstance.STATE_COMPLETED);
            Assertions.assertThat(instances.get(1).getStatus()).isEqualTo(scope == Scope.PROCESS_INSTANCE ?
                    ProcessInstance.STATE_ACTIVE : ProcessInstance.STATE_COMPLETED);
        } else if (strategy == Strategy.PROCESS_INSTANCE) {
            Assertions.assertThat(instances.get(0).getStatus()).isEqualTo(scope == Scope.PROJECT ?
                    ProcessInstance.STATE_COMPLETED : ProcessInstance.STATE_ACTIVE);
            Assertions.assertThat(instances.get(1).getStatus()).isEqualTo(scope == Scope.PROJECT ?
                    ProcessInstance.STATE_COMPLETED : ProcessInstance.STATE_ACTIVE);
        }
        Assertions.assertThat(instances.get(2).getStatus()).isEqualTo(ProcessInstance.STATE_COMPLETED);
    }

    @Test
    public void testSignalSubProcessSingletonStrategyKieSessionScope() {
        testSignalSubProcess(Strategy.SINGLETON, Scope.DEFAULT);
    }

    @Test
    public void testSignalSubProcessSingletonStrategyRuntimeManagerScope() {
        testSignalSubProcess(Strategy.SINGLETON, Scope.PROJECT);
    }

    @Test
    public void testSignalSubProcessSingletonStrategyProcessInstanceScope() {
        testSignalSubProcess(Strategy.SINGLETON, Scope.PROCESS_INSTANCE);
    }

    @Test
    public void testSignalSubProcessPerProcessInstanceStrategyKieSessionScope() {
        testSignalSubProcess(Strategy.PROCESS_INSTANCE, Scope.DEFAULT);
    }

    @Test
    public void testSignalSubProcessPerProcessInstanceStrategyRuntimeManagerScope() {
        testSignalSubProcess(Strategy.PROCESS_INSTANCE, Scope.PROJECT);
    }

    @Test
    public void testSignalSubProcessPerProcessInstanceStrategyProcessInstanceScope() {
        testSignalSubProcess(Strategy.PROCESS_INSTANCE, Scope.PROCESS_INSTANCE);
    }

}
