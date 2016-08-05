/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.drools.core.impl.EnvironmentFactory;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.jbpm.process.test.TestProcessEventListener;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.kie.api.KieBase;
import org.kie.api.definition.process.Process;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.test.util.logging.LoggingPrintStream;
import org.slf4j.Logger;

public abstract class AbstractBaseTest {

    protected Logger logger;

    protected static final String OLD_RECURSIVE_STACK = "RECURSIVE";
    protected static final String QUEUE_BASED_EXECUTION =  "queueBased";

    protected boolean queueBasedExecution = false;
    protected static boolean showOutput = true;

    @Rule
    public TestName name = new TestName();

    @Before
    public void before() {
        addLogger();
        logger.debug( "> " + name.getMethodName() );
    }

    public abstract void addLogger();

    protected static AtomicInteger uniqueIdGen = new AtomicInteger(0);

    protected KieSession createKieSession(Process... process) {
        KieBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        for (Process processToAdd : process) {
            ((KnowledgeBaseImpl) kbase).addProcess(processToAdd);
        }
        Environment env = EnvironmentFactory.newEnvironment();
        env.set(EnvironmentName.USE_QUEUE_BASED_EXECUTION, queueBasedExecution);
        return kbase.newKieSession(null, env);
    }

    protected void showEventHistory(KieSession ksession) {
        TestProcessEventListener procEventListener = (TestProcessEventListener) ksession.getProcessEventListeners().iterator().next();
        for (String event : procEventListener.getEventHistory()) {
            System.out.println("\"" + event + "\",");
        }
    }


    protected void verifyEventHistory(String[] expectedRecursiveEventOrder, List<String> eventHistory) {
        verifyEventHistory(expectedRecursiveEventOrder, eventHistory, true);
    }

    protected void verifyEventHistory(String[] expectedRecursiveEventOrder, List<String> eventHistory, boolean checkqueueBased) {
        if( ! queueBasedExecution ) {
//            assertEquals("Mismatch in number of events expected.", expectedRecursiveEventOrder.length, eventHistory.size());
            int max = expectedRecursiveEventOrder.length > eventHistory.size() ? expectedRecursiveEventOrder.length : eventHistory.size();
            logger.debug("{} | {}", "EXPECTED", "TEST" );
            for (int i = 0; i < max; ++i) {
                String expected = "", real = "";
                if (i < expectedRecursiveEventOrder.length) {
                    expected = expectedRecursiveEventOrder[i];
                }
                if (i < eventHistory.size()) {
                    real = eventHistory.get(i);
                }
                logger.debug("{} | {}", expected, real);
                assertEquals("Mismatch in expected event", expected, real);
            }
        } else if( checkqueueBased ) {
            assertEquals("Mismatch in number of events expected.", expectedRecursiveEventOrder.length, eventHistory.size());
            Map<String, Integer> actualHistory = new HashMap<String, Integer>();
            Map<String, Integer> expectedHistory = new HashMap<String, Integer>();
            for( String processEvent : expectedRecursiveEventOrder ) {
                Integer numTimes = expectedHistory.get(processEvent);
                if( numTimes == null ) {
                    numTimes = 0;
                }
                expectedHistory.put(processEvent, numTimes.intValue() + 1);
            }
            String lastEvent = null;
            for( String processEvent : eventHistory ) {
                Integer numTimes = actualHistory.get(processEvent);
                if( numTimes == null ) {
                   numTimes = 0;
                }
                actualHistory.put(processEvent, numTimes.intValue() + 1);
                assertTrue( "Event " + processEvent + " did not happen during recursive execution!", expectedHistory.containsKey(processEvent) );
                if( processEvent.startsWith("anl") ) {
                    String beforeNodeLeft = "bnl-" + processEvent.substring(4);
                    assertEquals( beforeNodeLeft, lastEvent);
                }
                lastEvent = processEvent;
            }
            for( Entry<String, Integer> eventTotal : expectedHistory.entrySet() ) {
                String processEvent = eventTotal.getKey();
                assertEquals( "Actual history for " + processEvent, eventTotal.getValue(), actualHistory.get(processEvent) );
            }
        }
    }

    @BeforeClass
    public static void configure() {
        if( ! showOutput ) {
            LoggingPrintStream.interceptSysOutSysErr();
        }
    }

    @AfterClass
    public static void reset() {
        if( ! showOutput ) {
            LoggingPrintStream.restoreSysOutAndSysErr();
        }
    }
}
