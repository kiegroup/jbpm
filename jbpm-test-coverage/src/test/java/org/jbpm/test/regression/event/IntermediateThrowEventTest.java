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

package org.jbpm.test.regression.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.core.process.instance.WorkItemHandler;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.drools.persistence.processinstance.InternalWorkItemManager;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.listener.process.NodeLeftCountDownProcessEventListener;
import org.jbpm.workflow.instance.node.HumanTaskNodeInstance;
import org.junit.Test;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;



public class IntermediateThrowEventTest extends JbpmTestCase {

    // General setup
    private static final Logger logger = LoggerFactory.getLogger(IntermediateThrowEventTest.class);

    
    private final static String PROCESS_TIMER_FILE_NAME = "org/jbpm/test/regression/event/IntermediateThrowMessageToSubprocessEvent.bpmn2";
    private final static String PROCESS_NAME = "IntermediateThrowMessageToSubprocessEvent";
    

    

    public IntermediateThrowEventTest() { 
        super(true, true);
    }
        
    @Test(timeout=10000)
    public void testThrowMessageToEventSubprocess() throws InterruptedException {
        NodeLeftCountDownProcessEventListener nodeLeft = new NodeLeftCountDownProcessEventListener("End Subprocess", 1);
        addProcessEventListener(nodeLeft);
        
        createRuntimeManager(PROCESS_TIMER_FILE_NAME);

        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession ksession = runtimeEngine.getKieSession();
        long ksessionId = ksession.getIdentifier();
        assertTrue("session id not saved.", ksessionId > 0);


        // Register Human Task Handler
        ksession.getWorkItemManager().registerWorkItemHandler("Send Task", new CustomOwnSendTaskHandler());

        // Start the process 
        ProcessInstance process = ksession.startProcess(PROCESS_NAME);
        long processId = process.getId();
        assertTrue("process id not saved", processId > 0);
        nodeLeft.waitTillCompleted();
        // The process completes
        process = ksession.getProcessInstance(process.getId());
        assertNull("Expected process to have been completed and removed", process);

    }

    public class CustomOwnSendTaskHandler implements WorkItemHandler {

        public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
            Long pid = ((WorkItemImpl) workItem).getProcessInstanceId();
            logger.info("Running SendTaskHandler for process instance: {} ", pid);
            logger.info("WorkItem Id: {}", workItem.getId());
            logger.info("WorkItem parameters: {}", workItem.getParameters());

            logger.info("Setting message for process instance: {}", pid);
            RuntimeManager mgr = RuntimeManagerRegistry.get().getManager("default-singleton");
            RuntimeEngine engine = mgr.getRuntimeEngine(ProcessInstanceIdContext.get(pid));
            try {
                Long pidMessage = pid;
                engine.getKieSession().signalEvent("Message-message", "Message", pid);
                logger.info("TEST - sent signal to {}", pidMessage);
            } finally {
                mgr.disposeRuntimeEngine(engine);
            }
            manager.completeWorkItem(workItem.getId(), null);
        }

        public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        }

    }
}
