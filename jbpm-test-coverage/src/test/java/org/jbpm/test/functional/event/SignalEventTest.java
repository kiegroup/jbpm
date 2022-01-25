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

package org.jbpm.test.functional.event;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jbpm.test.JbpmTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.audit.ProcessInstanceLog;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
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

    private final Strategy strategy;
    private final Scope scope;

    public SignalEventTest(Strategy strategy, Scope scope) {
        this.strategy = strategy;
        this.scope = scope;
    }

    @Parameterized.Parameters(name = "{0} strategy, {1} scope")
    public static Collection<Object[]> parameters() {
        Object[][] combinations = new Object[][]{
                {Strategy.SINGLETON, Scope.DEFAULT},
                {Strategy.SINGLETON, Scope.PROJECT},
                {Strategy.SINGLETON, Scope.PROCESS_INSTANCE},
                {Strategy.PROCESS_INSTANCE, Scope.DEFAULT},
                {Strategy.PROCESS_INSTANCE, Scope.PROJECT},
                {Strategy.PROCESS_INSTANCE, Scope.PROCESS_INSTANCE}
        };
        return Arrays.asList(combinations);
    }

    private KieSession getKieSession() {
        Context<?> context = strategy == Strategy.PROCESS_INSTANCE ? ProcessInstanceIdContext.get() : EmptyContext.get();
        return getRuntimeEngine(context).getKieSession();
    }

    @Test
    public void testSignalManually() {
        createRuntimeManager(strategy, (String) null, INTERMEDIATE_CATCH);

        KieSession ksession = getKieSession();
        ProcessInstance pi1 = ksession.startProcess(INTERMEDIATE_CATCH_ID);
        ProcessInstance pi2 = getKieSession().startProcess(INTERMEDIATE_CATCH_ID);

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

        Map<Long, ProcessInstanceLog> instances = getRuntimeEngine().getAuditService().findProcessInstances()
                .stream().collect(toMap(ProcessInstanceLog::getProcessInstanceId, instance -> instance));
        assertThat(instances).hasSize(2);

        assertThat(instances.get(pi1.getId()).getProcessId()).isEqualTo(INTERMEDIATE_CATCH_ID);
        assertThat(instances.get(pi1.getId()).getStatus()).isEqualTo(ProcessInstance.STATE_COMPLETED);

        assertThat(instances.get(pi2.getId()).getProcessId()).isEqualTo(INTERMEDIATE_CATCH_ID);
        if (strategy == Strategy.SINGLETON) {
            assertThat(instances.get(pi2.getId()).getStatus()).isEqualTo(scope == Scope.PROCESS_INSTANCE ?
                    ProcessInstance.STATE_ACTIVE : ProcessInstance.STATE_COMPLETED);
        } else if (strategy == Strategy.PROCESS_INSTANCE) {
            assertThat(instances.get(pi2.getId()).getStatus()).isEqualTo(scope == Scope.PROJECT ?
                    ProcessInstance.STATE_COMPLETED : ProcessInstance.STATE_ACTIVE);
        }
    }

    @Test
    public void testSignalEndThrowIntermediateCatch() {
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

        getKieSession().startProcess(INTERMEDIATE_CATCH_ID);
        getKieSession().startProcess(throwingProcessId);

        Map<String, ProcessInstanceLog> instances = getRuntimeEngine().getAuditService().findProcessInstances()
                .stream().collect(toMap(ProcessInstanceLog::getProcessId, instance -> instance));
        assertThat(instances).hasSize(2);

        assertThat(instances.get(INTERMEDIATE_CATCH_ID)).isNotNull();
        assertThat(instances.get(throwingProcessId)).isNotNull();

        if (strategy == Strategy.SINGLETON) {
            assertThat(instances.get(INTERMEDIATE_CATCH_ID).getStatus()).isEqualTo(scope == Scope.PROCESS_INSTANCE ?
                    ProcessInstance.STATE_ACTIVE : ProcessInstance.STATE_COMPLETED);
            assertThat(instances.get(throwingProcessId).getStatus()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        } else if (strategy == Strategy.PROCESS_INSTANCE) {
            assertThat(instances.get(INTERMEDIATE_CATCH_ID).getStatus()).isEqualTo(scope == Scope.PROJECT ?
                    ProcessInstance.STATE_COMPLETED : ProcessInstance.STATE_ACTIVE);
            assertThat(instances.get(throwingProcessId).getStatus()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        }
    }

    @Test
    public void testSignalIntermediateThrowStartCatch() {
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

        getKieSession().startProcess(throwingProcessId);

        boolean run = (strategy == Strategy.SINGLETON && scope != Scope.PROCESS_INSTANCE) ||
                (strategy == Strategy.PROCESS_INSTANCE && scope == Scope.PROJECT) || 
                (strategy == Strategy.PROCESS_INSTANCE && scope == Scope.DEFAULT);

        Map<String, ProcessInstanceLog> instances = getRuntimeEngine().getAuditService().findProcessInstances()
                .stream().collect(toMap(ProcessInstanceLog::getProcessId, instance -> instance));
        assertThat(instances).hasSize(run ? 2 : 1);

        assertThat(instances.get(throwingProcessId)).isNotNull();
        assertThat(instances.get(throwingProcessId).getStatus()).isEqualTo(ProcessInstance.STATE_COMPLETED);

        if (run) {
            assertThat(instances.get(START_CATCH_ID)).isNotNull();
            assertThat(instances.get(START_CATCH_ID).getStatus()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        }
    }

    @Test
    public void testSignalSubProcess() {
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

        getKieSession().startProcess(INTERMEDIATE_CATCH_ID);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("subprocess", throwingProcessId);
        getKieSession().startProcess(SUBPROCESS_CATCH_ID, parameters);

        Map<String, ProcessInstanceLog> instances = getRuntimeEngine().getAuditService().findProcessInstances()
                .stream().collect(toMap(ProcessInstanceLog::getProcessId, instance -> instance));
        assertThat(instances).hasSize(3);

        assertThat(instances.get(INTERMEDIATE_CATCH_ID)).isNotNull();
        assertThat(instances.get(SUBPROCESS_CATCH_ID)).isNotNull();
        assertThat(instances.get(throwingProcessId)).isNotNull();

        if (strategy == Strategy.SINGLETON) {
            assertThat(instances.get(INTERMEDIATE_CATCH_ID).getStatus()).isEqualTo(scope == Scope.PROCESS_INSTANCE ?
                    ProcessInstance.STATE_ACTIVE : ProcessInstance.STATE_COMPLETED);
            assertThat(instances.get(SUBPROCESS_CATCH_ID).getStatus()).isEqualTo(scope == Scope.PROCESS_INSTANCE ?
                    ProcessInstance.STATE_ACTIVE : ProcessInstance.STATE_COMPLETED);
        } else if (strategy == Strategy.PROCESS_INSTANCE) {
            assertThat(instances.get(INTERMEDIATE_CATCH_ID).getStatus()).isEqualTo(scope == Scope.PROJECT ?
                    ProcessInstance.STATE_COMPLETED : ProcessInstance.STATE_ACTIVE);
            assertThat(instances.get(SUBPROCESS_CATCH_ID).getStatus()).isEqualTo(scope == Scope.PROJECT ?
                    ProcessInstance.STATE_COMPLETED : ProcessInstance.STATE_ACTIVE);
        }
        assertThat(instances.get(throwingProcessId).getStatus()).isEqualTo(ProcessInstance.STATE_COMPLETED);
    }

}
