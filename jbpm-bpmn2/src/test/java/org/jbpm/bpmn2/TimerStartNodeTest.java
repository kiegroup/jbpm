package org.jbpm.bpmn2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.event.DefaultProcessEventListener;
import org.drools.event.process.ProcessStartedEvent;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.Test;

public class TimerStartNodeTest {

    @Test
    public void testTimerCycle() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("start-timer.bpmn"), ResourceType.BPMN2);
        assertFalse(kbuilder.getErrors().toString(), kbuilder.hasErrors());
        
        KnowledgeBase kbase = kbuilder.newKnowledgeBase();
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        StartCountingListener listener = new StartCountingListener();
        ksession.addEventListener(listener);
        
        ksession.fireAllRules();
        for (int i = 1; i < 10; i++) {
            Thread.sleep(1100);
            assertEquals(i, listener.getCount("start.cycle"));
        }
    }
    
    /**
     * This is how I would expect the start event to work (same as the recurring event)
     */
    @Test
    public void testTimerDelay() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("start-timer2.bpmn"), ResourceType.BPMN2);
        assertFalse(kbuilder.getErrors().toString(), kbuilder.hasErrors());
        
        KnowledgeBase kbase = kbuilder.newKnowledgeBase();
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        StartCountingListener listener = new StartCountingListener();
        ksession.addEventListener(listener);
        
        ksession.fireAllRules();
        Thread.sleep(1100);
        
        assertEquals(1, listener.getCount("start.delaying"));
    }
    
    /**
     * just in case it would work this way
     */
    @Test
    public void testTimerDelay2() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("start-timer2.bpmn"), ResourceType.BPMN2);
        assertFalse(kbuilder.getErrors().toString(), kbuilder.hasErrors());
        
        KnowledgeBase kbase = kbuilder.newKnowledgeBase();
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        StartCountingListener listener = new StartCountingListener();
        ksession.addEventListener(listener);
        
        ksession.startProcess("start.delaying");
        assertEquals(0, listener.getCount("start.delaying"));
        Thread.sleep(1100);
        assertEquals(1, listener.getCount("start.delaying"));
    }

    private static class StartCountingListener extends DefaultProcessEventListener {
        private Map<String, Integer> map = new HashMap<String, Integer>();
        
        public void beforeProcessStarted(ProcessStartedEvent event) {
            String processId = event.getProcessInstance().getProcessId();
            Integer count = map.get(processId);
            
            if (count == null) {
                map.put(processId, 1);
            } else {
                map.put(processId, count + 1);
            }
        }
        
        public int getCount(String processId) {
            Integer count = map.get(processId);
            return (count == null) ? 0 : count;
        }
    }
}
