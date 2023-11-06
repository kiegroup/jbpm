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
package org.jbpm.test.functional.timer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.drools.core.command.SingleSessionCommandService;
import org.drools.core.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.core.impl.StatefulKnowledgeSessionImpl;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.command.UpdateTimerCommand;
import org.jbpm.process.instance.timer.TimerInstance;
import org.jbpm.process.instance.timer.TimerManager;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.listener.process.DefaultCountDownProcessEventListener;
import org.jbpm.test.listener.process.NodeLeftCountDownProcessEventListener;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for UpdateTimmerCommand
 */
public class TimerUpdateTest extends JbpmTestCase {

    private static final Logger logger = LoggerFactory.getLogger(TimerPersistenceTest.class);

    private static final String TIMER_FILE = "org/jbpm/test/functional/timer/UpdateTimer.bpmn2";
    private static final String PROCESS_NAME = "UpdateTimer";
    private static final String TIMER_NAME = "Timer";

    private static final String TIMER_NO_NAME_FILE = "org/jbpm/test/functional/timer/UpdateTimerNoName.bpmn2";

    private static final String START_TIMER_FILE = "org/jbpm/test/functional/timer/UpdateStartTimer.bpmn2";
    private static final String START_PROCESS_NAME = "UpdateStartTimer";
    private static final String START_TIMER_NAME = "StartTimer";

    private static final String BOUNDARY_TIMER_FILE = "org/jbpm/test/functional/timer/UpdateBoundaryTimer.bpmn2";
    private static final String BOUNDARY_PROCESS_NAME = "UpdateBoundaryTimer";
    private static final String BOUNDARY_TIMER_NAME = "BoundaryTimer";
    private static final String BOUNDARY_TIMER_ATTACHED_TO_NAME = "User";
    
    private static final String TIMER_SUBPROCESS_FILE = "org/jbpm/test/functional/timer/UpdateSubprocessTimer.bpmn2";
    private static final String PROCESS_SUBPROCESS_NAME = "UpdateSubProcessTimer";
    private static final String TIMER_SUBPROCESS_NAME = "Timer";

    private static final String ISO_TIMER_FILE = "org/jbpm/test/regression/ISOTimerTest.bpmn2";
    private static final String ISO_TIMER_NAME = "ISOTimerTest";

    private RuntimeEngine runtimeEngine;
    private KieSession kieSession;

    private static final String TIMER_FIRED_TEXT = "Hello Adele";
    private static final String TIMER_FIRED_TIME_PROP = "Time Adele";

    @Before
    public void setup() {
        runtimeEngine = null;
        kieSession = null;
        System.clearProperty(TIMER_FIRED_TEXT);
        System.clearProperty(TIMER_FIRED_TIME_PROP);
    }

    @Test(timeout = 30000)
    public void updateTimerLongerDelayTest() {
        NodeLeftCountDownProcessEventListener countDownListener = new NodeLeftCountDownProcessEventListener(TIMER_NAME, 1);
        //delay is set for 5s
        setProcessScenario(TIMER_FILE);

        kieSession.addEventListener(countDownListener);
        final List<Long> list = new ArrayList<Long>();
        kieSession.addEventListener(new DefaultProcessEventListener() {
            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                list.add(event.getProcessInstance().getId());
            }
        });

        Assertions.assertThat(list).isEmpty();
        long id = kieSession.startProcess(PROCESS_NAME).getId();
        long startTime = System.currentTimeMillis();
        Assertions.assertThat(list).isNotEmpty();
        //set delay to 8s
        kieSession.execute(new UpdateTimerCommand(id, TIMER_NAME, 8));

        countDownListener.waitTillCompleted();
        Assertions.assertThat(timerHasFired()).isTrue();
        long firedTime = timerFiredTime();
        long timeDifference = Math.abs(firedTime - startTime - 8000);
        logger.info("Start time: " + startTime + ", fired time: " + firedTime + ", difference: " + (firedTime-startTime));
        Assertions.assertThat(timeDifference).isLessThan(500);

        Assertions.assertThat(kieSession.getProcessInstance(id)).isNull();
    }

    @Test(timeout = 30000)
    public void updateTimerShortherDelayTest() {
        NodeLeftCountDownProcessEventListener countDownListener = new NodeLeftCountDownProcessEventListener(TIMER_NAME, 1);
        //delay is set for 5s
        setProcessScenario(TIMER_FILE);

        kieSession.addEventListener(countDownListener);
        final List<Long> list = new ArrayList<Long>();
        kieSession.addEventListener(new DefaultProcessEventListener() {
            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                list.add(event.getProcessInstance().getId());
            }
        });

        Assertions.assertThat(list).isEmpty();
        long id = kieSession.startProcess(PROCESS_NAME).getId();
        long startTime = System.currentTimeMillis();
        Assertions.assertThat(list).isNotEmpty();
        //set delay to 3s
        kieSession.execute(new UpdateTimerCommand(id, TIMER_NAME, 3));

        countDownListener.waitTillCompleted();
        Assertions.assertThat(timerHasFired()).isTrue();
        long firedTime = timerFiredTime();
        long timeDifference = Math.abs(firedTime - startTime - 3000);
        logger.info("Start time: " + startTime + ", fired time: " + firedTime + ", difference: " + (firedTime-startTime));
        Assertions.assertThat(timeDifference).isLessThan(500);

        Assertions.assertThat(kieSession.getProcessInstance(id)).isNull();
    }

    @Test(timeout = 30000)
    public void updateTimerWithoutNameTest() {
        DefaultCountDownProcessEventListener listener = new DefaultCountDownProcessEventListener(1) {
            @Override
            public void afterNodeLeft(ProcessNodeLeftEvent event) {
                if (event.getNodeInstance().getNodeName() == null) {
                    countDown();
                }
            }
        };
        //delay is set for 5s
        setProcessScenario(TIMER_NO_NAME_FILE);

        kieSession.addEventListener(listener);
        final List<Long> list = new ArrayList<Long>();
        kieSession.addEventListener(new DefaultProcessEventListener() {
            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                list.add(event.getProcessInstance().getId());
            }
        });

        Assertions.assertThat(list).isEmpty();
        long id = kieSession.startProcess(PROCESS_NAME).getId();
        long startTime = System.currentTimeMillis();
        Assertions.assertThat(list).isNotEmpty();
        
        //Get the timerId (there is only one)
        Collection<TimerInstance> timers = getTimerManager(kieSession).getTimers();
        Assertions.assertThat(timers).hasSize(1);
        long timerId = timers.iterator().next().getId();
        
        //set delay to 3s
        kieSession.execute(new UpdateTimerCommand(id, timerId, 3));

        listener.waitTillCompleted();
        Assertions.assertThat(timerHasFired()).isTrue();
        long firedTime = timerFiredTime();
        long timeDifference = Math.abs(firedTime - startTime - 3000);
        logger.info("Start time: " + startTime + ", fired time: " + firedTime + ", difference: " + (firedTime-startTime));
        Assertions.assertThat(timeDifference).isLessThan(500);

        Assertions.assertThat(kieSession.getProcessInstance(id)).isNull();
    }

    @Test(timeout = 30000)
    public void updateTimerBeforeDelayTest() {
        NodeLeftCountDownProcessEventListener countDownListener = new NodeLeftCountDownProcessEventListener(TIMER_NAME, 1);
        //delay is set for 5s
        setProcessScenario(TIMER_FILE);

        kieSession.addEventListener(countDownListener);
        final List<Long> list = new ArrayList<Long>();
        kieSession.addEventListener(new DefaultProcessEventListener() {
            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                list.add(event.getProcessInstance().getId());
            }
        });

        Assertions.assertThat(list).isEmpty();
        long id = kieSession.startProcess(PROCESS_NAME).getId();
        long startTime = System.currentTimeMillis();
        Assertions.assertThat(list).isNotEmpty();
        //set delay on time that passed -> expected that timer fired immediately
        kieSession.execute(new UpdateTimerCommand(id, TIMER_NAME, -5));

        countDownListener.waitTillCompleted();
        Assertions.assertThat(timerHasFired()).isTrue();
        long firedTime = timerFiredTime();
        long timeDifference = Math.abs(firedTime - startTime);
        logger.info("Start time: " + startTime + ", fired time: " + firedTime + ", difference: " + (firedTime-startTime));
        Assertions.assertThat(timeDifference).isLessThan(500);

        Assertions.assertThat(kieSession.getProcessInstance(id)).isNull();
    }
    
    @Test(timeout = 30000)
    public void updateBoundaryTimerTest() {
        NodeLeftCountDownProcessEventListener countDownListener = new NodeLeftCountDownProcessEventListener(BOUNDARY_TIMER_NAME, 1);
        //timer is set for long duration (100s)
        setProcessScenario(BOUNDARY_TIMER_FILE);

        final List<Long> list = new ArrayList<Long>();
        kieSession.addEventListener(new DefaultProcessEventListener() {
            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                list.add(event.getProcessInstance().getId());
            }
        });
        kieSession.addEventListener(countDownListener);

        Assertions.assertThat(list).isEmpty();
        long id = kieSession.startProcess(BOUNDARY_PROCESS_NAME).getId();
        long startTime = System.currentTimeMillis();
        Assertions.assertThat(list).isNotEmpty();
        //set timer delay to 3s
        kieSession.execute(new UpdateTimerCommand(id, BOUNDARY_TIMER_ATTACHED_TO_NAME, 3));

        countDownListener.waitTillCompleted();
        Assertions.assertThat(timerHasFired()).isTrue();
        long firedTime = timerFiredTime();
        long timeDifference = Math.abs(firedTime - startTime - 3000);
        logger.info("Start time: " + startTime + ", fired time: " + firedTime + ", difference: " + (firedTime-startTime));
        Assertions.assertThat(timeDifference).isLessThan(1000);

        Assertions.assertThat(kieSession.getProcessInstance(id)).isNull();
    }
    
    @Test(timeout = 30000)
    public void updateTimerSubprocessLongerDelayTest() {
        NodeLeftCountDownProcessEventListener countDownListener = new NodeLeftCountDownProcessEventListener(TIMER_SUBPROCESS_NAME, 1);
        //delay is set for 5s
        setProcessScenario(TIMER_SUBPROCESS_FILE);

        kieSession.addEventListener(countDownListener);
        final List<Long> list = new ArrayList<Long>();
        kieSession.addEventListener(new DefaultProcessEventListener() {
            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                list.add(event.getProcessInstance().getId());
            }
            @Override
            public void afterNodeLeft(ProcessNodeLeftEvent event) {
                if (TIMER_SUBPROCESS_NAME.equals(event.getNodeInstance().getNodeName())) {
                    System.setProperty(TIMER_FIRED_TEXT, "");
                    System.setProperty(TIMER_FIRED_TIME_PROP, String.valueOf(System.currentTimeMillis()));
                }
            }
        });

        Assertions.assertThat(list).isEmpty();
        long id = kieSession.startProcess(PROCESS_SUBPROCESS_NAME).getId();
        long startTime = System.currentTimeMillis();
        Assertions.assertThat(list).isNotEmpty();
        //set delay to 8s
        kieSession.execute(new UpdateTimerCommand(id, TIMER_SUBPROCESS_NAME, 8));

        countDownListener.waitTillCompleted();
        Assertions.assertThat(timerHasFired()).isTrue();
        long firedTime = timerFiredTime();
        long timeDifference = Math.abs(firedTime - startTime - 8000);
        logger.info("Start time: " + startTime + ", fired time: " + firedTime + ", difference: " + (firedTime-startTime));
        Assertions.assertThat(timeDifference).isLessThan(500);

        Assertions.assertThat(kieSession.getProcessInstance(id)).isNull();
    }

    private void setProcessScenario(String file) {
        createRuntimeManager(file);
        runtimeEngine = getRuntimeEngine();
        kieSession = runtimeEngine.getKieSession();
        Assertions.assertThat(kieSession).isNotNull();
    }

    private boolean timerHasFired() {
        String hasFired = System.getProperty(TIMER_FIRED_TEXT);
        return hasFired != null;
    }

    private int timerFiredCount() {
        String timerFiredCount = System.getProperty(TIMER_FIRED_TEXT);
        if( timerFiredCount == null ) {
           return 0;
        }
        return Integer.parseInt(timerFiredCount);
    }

    private long timerFiredTime() {
        String timerFiredTime = System.getProperty(TIMER_FIRED_TIME_PROP);
        if( timerFiredTime == null ) {
           return 0;
        }
        return Long.parseLong(timerFiredTime);
    }

    @Test(timeout = 10000)
    public void emptyISOTimerTest() throws Exception {

        setProcessScenario(ISO_TIMER_FILE);

        Map<String, Object> parameters = new HashMap<>();
        Assertions.assertThatThrownBy(() -> kieSession.startProcess(ISO_TIMER_NAME, parameters)).hasCauseInstanceOf(IllegalArgumentException.class);

    }
    
    private TimerManager getTimerManager(KieSession ksession) {
        KieSession internal = ksession;
        if (ksession instanceof CommandBasedStatefulKnowledgeSession) {
            internal = ((SingleSessionCommandService)((CommandBasedStatefulKnowledgeSession)ksession ).getRunner()).getKieSession();
        }

        return ((InternalProcessRuntime)((StatefulKnowledgeSessionImpl)internal).getProcessRuntime()).getTimerManager();
    }
}
