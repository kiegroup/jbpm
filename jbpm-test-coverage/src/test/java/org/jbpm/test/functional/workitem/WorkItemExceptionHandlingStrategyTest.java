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
package org.jbpm.test.functional.workitem;

import org.assertj.core.api.Assertions;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.workflow.instance.WorkflowRuntimeException;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.ProcessWorkItemHandlerException.HandlingStrategy;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

public class WorkItemExceptionHandlingStrategyTest extends JbpmTestCase {

    private static final String FAILING_PROCESS_ID = "com.sample.boolean.waitSignal";
    private static final String EXCEPTION_HANDLING_PROCESS_ID = "WaitSignal";

    public WorkItemExceptionHandlingStrategyTest() {
        super( true, true );
    }

    @Test
    public void exceptionHandlingAbortProcess() {
        KieSession kieSession = createSession();

        kieSession.getWorkItemManager().registerWorkItemHandler( "Human Task", new ExceptionWorkItemHandler(EXCEPTION_HANDLING_PROCESS_ID, HandlingStrategy.ABORT.name()) );
        ProcessInstance pi = kieSession.startProcess( FAILING_PROCESS_ID );
        assertProcessInstanceActive( pi.getId(), kieSession );

        kieSession.abortProcessInstance(pi.getId());
        assertProcessInstanceAborted( pi.getId() );

    }
    
    @Test
    public void exceptionHandlingNotTriggeringSubprocessWhenPersistentException() {
        KieSession kieSession = createSession();

        kieSession.getWorkItemManager().registerWorkItemHandler( "Human Task", new PersistentExceptionWorkItemHandler(EXCEPTION_HANDLING_PROCESS_ID, HandlingStrategy.ABORT.name()) );
        Assertions.assertThatThrownBy(() -> kieSession.startProcess( FAILING_PROCESS_ID )).isInstanceOf(WorkflowRuntimeException.class);
    }

    private KieSession createSession() {
        manager = createRuntimeManager(Strategy.PROCESS_INSTANCE, (String) null, 
                                       "org/jbpm/test/functional/workitem/BPMN2-UserTaskWithBooleanOutputWaitSignal.bpmn2", 
                                       "org/jbpm/test/functional/workitem/BPMN2-WaitSignal.bpmn2" 
        );
        RuntimeEngine runtimeEngine = getRuntimeEngine( ProcessInstanceIdContext.get() );
        KieSession kieSession = runtimeEngine.getKieSession();
        return kieSession;
    }

}
