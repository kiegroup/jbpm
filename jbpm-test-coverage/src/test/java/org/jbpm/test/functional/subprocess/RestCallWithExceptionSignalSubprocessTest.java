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

package org.jbpm.test.functional.subprocess;

import org.assertj.core.api.Assertions;
import org.jbpm.bpmn2.handler.SignallingTaskHandlerDecorator;
import org.jbpm.process.workitem.rest.RESTWorkItemHandler;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.jbpm.workflow.instance.WorkflowRuntimeException;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;

public class RestCallWithExceptionSignalSubprocessTest extends JbpmTestCase {

    private static final String REST_CALL_SUBPROCESS =
            "org/jbpm/test/functional/subprocess/RestCallWithExceptionSignalSubprocess.bpmn";
    private static final String REST_CALL_SUBPROCESS_ID =
            "org.jbpm.test.functional.subprocess.RestCallWithExceptionSignal";


    @Test
    public void testNoErrorUsingSignallingTaskHandlerDecorator() {
    	
    	SignallingTaskHandlerDecorator signalDec = new SignallingTaskHandlerDecorator(new org.jbpm.process.workitem.rest.RESTWorkItemHandler(), "Error-serviceErrorSignal");
    	
        KieSession ksession = createKSession(REST_CALL_SUBPROCESS);
        ksession.getWorkItemManager().registerWorkItemHandler("Rest", signalDec);
        
        Map<String, Object> input = new HashMap<>();
        
        input.put("serviceUrl", "http://localhost:8081");
        
        ProcessInstance processInstance = ksession.startProcess(REST_CALL_SUBPROCESS_ID,input);
        assertProcessInstanceActive(processInstance.getId());
        String [] nodesTriggered = new String [4];
        nodesTriggered[0] = "Before Rest Call";
        nodesTriggered[1] = "Rest Service";
        nodesTriggered[2] = "SignalError";
        nodesTriggered[3] = "Error Logged";
        assertNodeTriggered(processInstance.getId(),nodesTriggered);
    }
	
    @Test
    public void testStackOverFlowErrorUsingRESTWorkItemHandler() {
	
    	RESTWorkItemHandler rest = new RESTWorkItemHandler();
        
	KieSession ksession = createKSession(REST_CALL_SUBPROCESS);
        ksession.getWorkItemManager().registerWorkItemHandler("Rest", rest);
        
        Map<String, Object> input = new HashMap<>();
        
        input.put("serviceUrl", "http://localhost:8081");
        
        try {
             ProcessInstance processInstance = ksession.startProcess(REST_CALL_SUBPROCESS_ID,input);
	     assertProcessInstanceActive(processInstance.getId());
	     String [] nodesTriggered = new String [4];
	     nodesTriggered[0] = "Before Rest Call";
	     nodesTriggered[1] = "Rest Service";
	     nodesTriggered[2] = "SignalError";
	     nodesTriggered[2] = "Error Logged";
	     assertNodeTriggered(processInstance.getId(),nodesTriggered);    
        } catch (StackOverflowError e) {
            fail("Process call fails with stackoverflow error");
        }
           

    }


}
