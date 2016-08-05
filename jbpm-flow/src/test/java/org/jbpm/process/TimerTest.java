/**
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.drools.core.common.InternalWorkingMemory;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.timer.TimerInstance;
import org.jbpm.process.instance.timer.TimerManager;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.test.util.AbstractBaseTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.kie.api.runtime.KieSession;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class TimerTest extends AbstractBaseTest  {

    public void addLogger() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

	private Semaphore counter = new Semaphore(10, true);

    @Parameters(name="{0}")
    public static Collection<Object[]> useStack() {
        Object[][] execModelType = new Object[][] {
                { OLD_RECURSIVE_STACK },
                { QUEUE_BASED_EXECUTION }
                };
        return Arrays.asList(execModelType);
    };

    public TimerTest(String execModel) {
        this.queueBasedExecution = QUEUE_BASED_EXECUTION.equals(execModel);
    }

    @Rule
    public TestName testName = new TestName();

    @Before
    public void printTestName() {
        // DBG
       System.out.println( " " + testName.getMethodName() );
    }

    @Test
	public void testTimer() {
        // set num available permits = 0;
        counter.drainPermits();

        final KieSession workingMemory = KnowledgeBaseFactory.newKnowledgeBase().newKieSession();

        RuleFlowProcessInstance processInstance = new RuleFlowProcessInstance() {
			private static final long serialVersionUID = 510l;
			public void signalEvent(String type, Object event) {
        		if ("timerTriggered".equals(type)) {
        			TimerInstance timer = (TimerInstance) event;
        			logger.info("Timer {} triggered", timer.getId());
            		counter.release();
        		}
        	}
        };
        processInstance.setKnowledgeRuntime(((InternalWorkingMemory) workingMemory).getKnowledgeRuntime());
        processInstance.setId(1234);
        InternalProcessRuntime processRuntime = ((InternalProcessRuntime) ((InternalWorkingMemory) workingMemory).getProcessRuntime());
        processRuntime.getProcessInstanceManager().internalAddProcessInstance(processInstance);

        new Thread(new Runnable() {
			public void run() {
	        	workingMemory.fireUntilHalt();
			}
        }).start();

        TimerManager timerManager = ((InternalProcessRuntime) ((InternalWorkingMemory) workingMemory).getProcessRuntime()).getTimerManager();
        TimerInstance timer = new TimerInstance();
        timerManager.registerTimer(timer, processInstance);
        try {
        	counter.tryAcquire(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Timer did not release permit: " + e.getClass().getSimpleName());
        }
        assertEquals(0, counter.availablePermits());

        counter.drainPermits();
        timer = new TimerInstance();
        timer.setDelay(500);
        timerManager.registerTimer(timer, processInstance);
        assertEquals(0, counter.availablePermits());
        try {
            counter.tryAcquire(1, TimeUnit.SECONDS);;
        } catch (InterruptedException e) {
            fail("Timer did not release permit: " + e.getClass().getSimpleName());
        }
        assertEquals(0, counter.availablePermits());

        counter.drainPermits();
        timer = new TimerInstance();
        timer.setDelay(500);
        timer.setPeriod(300);
        timerManager.registerTimer(timer, processInstance);
        assertEquals(0, counter.availablePermits());
        try {
            counter.tryAcquire(700, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Timer did not release permit: " + e.getClass().getSimpleName());
        	// do nothing
        }
        assertEquals(0, counter.availablePermits());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // do nothing
        }
        // we can't know exactly how many times this will fire as timers are not precise, but should be at least 3 more
        // ((700 + 1000 [time waited])-500 [delay])/300 [period] = 4 - 1 (1rst fire) = 3
        assertTrue( "Num permits/timer runs: " + counter.availablePermits(), counter.availablePermits() >= 3 );

        timerManager.cancelTimer(timer.getId());
        int lastCount = counter.availablePermits();
        try {
        	Thread.sleep(1000);
        } catch (InterruptedException e) {
        	// do nothing
        }
        assertEquals(lastCount, counter.availablePermits());
	}

}
