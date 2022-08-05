/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

import java.util.HashMap;
import java.util.Map;

import org.jbpm.process.workitem.rest.RESTWorkItemHandler;
import org.jbpm.test.JbpmTestCase;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.ProcessWorkItemHandlerException.HandlingStrategy;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

public class WorkItemExceptionHandlingMultipleWIHRetryTest extends JbpmTestCase {

    private static final String FAILING_PROCESS_ID = "ParentProcessMultipleRestWIH";
    private static final String EXCEPTION_HANDLING_PROCESS_ID = "ExceptionHandlingProcess";

    public WorkItemExceptionHandlingMultipleWIHRetryTest() {
        super( true, true );
    }

    @Test
    public void exceptionHandlingAbortProcessWithOneWIH() {
        KieSession kieSession = createSession();

        kieSession.getWorkItemManager().registerWorkItemHandler( "Rest", new RESTWorkItemHandler(getClass().getClassLoader(), EXCEPTION_HANDLING_PROCESS_ID, HandlingStrategy.RETRY.name()) );
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("path", "one");

        ProcessInstance pi = kieSession.startProcess( FAILING_PROCESS_ID, params );
        assertProcessInstanceActive( pi.getId(), kieSession );

        try {
            kieSession.signalEvent("moveon", null); 
        } catch(Exception ex) {
        }

        kieSession.abortProcessInstance(pi.getId());
        assertProcessInstanceAborted( pi.getId() );
    }

    @Test
    public void exceptionHandlingAbortProcessWithTwoWIH() {
        KieSession kieSession = createSession();

        kieSession.getWorkItemManager().registerWorkItemHandler( "Rest", new RESTWorkItemHandler(getClass().getClassLoader(), EXCEPTION_HANDLING_PROCESS_ID, HandlingStrategy.RETRY.name()) );
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("path", "two");

        ProcessInstance pi = kieSession.startProcess( FAILING_PROCESS_ID, params );
        assertProcessInstanceActive( pi.getId(), kieSession );

        try {
            kieSession.signalEvent("moveon", null); 
        } catch(Exception ex) {
        }

        kieSession.abortProcessInstance(pi.getId());
        assertProcessInstanceAborted( pi.getId() );
    }
    

    @Test
    public void exceptionHandlingAbortProcessWithTwoMockRestWIH() {
        KieSession kieSession = createSession();

        kieSession.getWorkItemManager().registerWorkItemHandler( "Rest", new MockRestWorkItemHandler(EXCEPTION_HANDLING_PROCESS_ID, HandlingStrategy.RETRY.name()) );
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("path", "two");

        ProcessInstance pi = kieSession.startProcess( FAILING_PROCESS_ID, params );
        assertProcessInstanceActive( pi.getId(), kieSession );

        try {
            kieSession.signalEvent("moveon", null); 
        } catch(Exception ex) {
        }

        kieSession.abortProcessInstance(pi.getId());
        assertProcessInstanceAborted( pi.getId() );
    }

    private KieSession createSession() {
        manager = createRuntimeManager(Strategy.PROCESS_INSTANCE, (String) null, 
                                       "org/jbpm/test/functional/workitem/ParentProcessMultipleRestWIH.bpmn", 
                                       "org/jbpm/test/functional/workitem/ExceptionHandlingProcess.bpmn" 
        );
        RuntimeEngine runtimeEngine = getRuntimeEngine( ProcessInstanceIdContext.get() );
        KieSession kieSession = runtimeEngine.getKieSession();
        return kieSession;
    }

}
