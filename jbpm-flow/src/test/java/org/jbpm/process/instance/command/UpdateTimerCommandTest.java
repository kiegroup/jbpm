/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.process.instance.command;

import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.drools.core.runtime.process.ProcessRuntimeFactory;
import org.kie.api.event.process.*;

import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.ProcessRuntimeFactoryServiceImpl;
import org.jbpm.process.instance.timer.TimerInstance;
import org.jbpm.process.instance.timer.TimerManager;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.test.util.AbstractBaseTest;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;

import org.kie.internal.command.RegistryContext;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class UpdateTimerCommandTest extends AbstractBaseTest {

    public void addLogger() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    static {
        ProcessRuntimeFactory.setProcessRuntimeFactoryService(new ProcessRuntimeFactoryServiceImpl());
    }

    @Test
    public void testUpdateTimerCommand() throws Exception {
        long processInstanceId = 1234;
        KieBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        final KieSession workingMemory = kbase.newKieSession();
        InternalProcessRuntime processRuntime = ((InternalProcessRuntime) ((InternalWorkingMemory) workingMemory).getProcessRuntime());
        final List<ProcessEvent> processEventList = new ArrayList<ProcessEvent>();

        final ProcessEventListener processEventListener = new ProcessEventListener() {
            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                processEventList.add(event);
            }

            @Override
            public void afterProcessStarted(ProcessStartedEvent event) {
                processEventList.add(event);
            }

            @Override
            public void beforeProcessCompleted(ProcessCompletedEvent event) {
                processEventList.add(event);
            }

            @Override
            public void afterProcessCompleted(ProcessCompletedEvent event) {
                processEventList.add(event);
            }

            @Override
            public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
                processEventList.add(event);
            }

            @Override
            public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
                processEventList.add(event);
            }

            @Override
            public void beforeNodeLeft(ProcessNodeLeftEvent event) {
                processEventList.add(event);
            }

            @Override
            public void afterNodeLeft(ProcessNodeLeftEvent event) {
                processEventList.add(event);
            }

            @Override
            public void beforeVariableChanged(ProcessVariableChangedEvent event) {
                processEventList.add(event);
            }

            @Override
            public void afterVariableChanged(ProcessVariableChangedEvent event) {
                processEventList.add(event);
            }

            public void onProcessDataChangedEvent(ProcessDataChangedEvent event) {
                processEventList.add(event);
            }

        };
        workingMemory.addEventListener(processEventListener);

        RuleFlowProcessInstance processInstance = new RuleFlowProcessInstance() ;
        processInstance.setKnowledgeRuntime(((InternalWorkingMemory) workingMemory).getKnowledgeRuntime());
        processInstance.setId(processInstanceId);

        TimerManager timerManager = ((InternalProcessRuntime) ((InternalWorkingMemory) workingMemory).getProcessRuntime()).getTimerManager();
        TimerInstance timer = new TimerInstance();
        timer.setDelay(100000);
        timerManager.registerTimer(timer, processInstance);

        processInstance.internalSetSlaTimerId(timer.getId());
        processRuntime.getProcessInstanceManager().internalAddProcessInstance(processInstance);
        RegistryContext mockContext = Mockito.mock(RegistryContext.class);
        when(mockContext.lookup(KieSession.class)).thenReturn(workingMemory);

        UpdateTimerCommand command = new UpdateTimerCommand(processInstance.getId(), timer.getId(),
                timer.getDelay(), timer.getPeriod(), timer.getRepeatLimit());
        command.execute(mockContext);

        assertEquals(1, processEventList.size());
        assertTrue(processEventList.get(0) instanceof ProcessDataChangedEvent);
        assertEquals(processInstanceId, ((ProcessDataChangedEvent) processEventList.get(0)).getProcessInstance().getId());

        timerManager.cancelTimer(processInstanceId, timer.getId());
    }

}
