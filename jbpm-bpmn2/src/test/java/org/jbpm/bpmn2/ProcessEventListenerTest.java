package org.jbpm.bpmn2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.drools.KnowledgeBase;
import org.drools.event.process.ProcessCompletedEvent;
import org.drools.event.process.ProcessEvent;
import org.drools.event.process.ProcessEventListener;
import org.drools.event.process.ProcessNodeLeftEvent;
import org.drools.event.process.ProcessNodeTriggeredEvent;
import org.drools.event.process.ProcessStartedEvent;
import org.drools.event.process.ProcessVariableChangedEvent;
import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.Test;

public class ProcessEventListenerTest extends JbpmJUnitTestCase {

    @Test
    public void testOrder() throws Exception {
        KnowledgeBase kbase = createKnowledgeBase("BPMN2-MinimalProcess.bpmn2");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        IterableProcessEventListener listener = new IterableProcessEventListener();
        ksession.addEventListener(listener);

        ksession.startProcess("Minimal");
        Thread.sleep(1000);

        Iterator<ProcessEvent> it = listener.iterator();
        Assert.assertTrue(it.next() instanceof ProcessStartedEvent); // beforeProcessStarted
        Assert.assertTrue(it.next() instanceof ProcessStartedEvent); // afterProcessStarted

        Assert.assertTrue(it.next() instanceof ProcessNodeTriggeredEvent); // beforeNodeTriggered - "StartProcess"
        Assert.assertTrue(it.next() instanceof ProcessNodeTriggeredEvent); // afterNodeTriggered - "StartProcess"
        Assert.assertTrue(it.next() instanceof ProcessNodeLeftEvent); // beforeNodeLeft - "StartProcess"
        Assert.assertTrue(it.next() instanceof ProcessNodeLeftEvent); // afterNodeLeft - "StartProcess"

        Assert.assertTrue(it.next() instanceof ProcessNodeTriggeredEvent); // beforeNodeTriggered - "Hello"
        Assert.assertTrue(it.next() instanceof ProcessNodeTriggeredEvent); // afterNodeTriggered - "Hello"
        Assert.assertTrue(it.next() instanceof ProcessNodeLeftEvent); // beforeNodeLeft - "Hello"
        Assert.assertTrue(it.next() instanceof ProcessNodeLeftEvent); // afterNodeLeft - "Hello"

        Assert.assertTrue(it.next() instanceof ProcessNodeTriggeredEvent); // beforeNodeTriggered - "EndProcess"
        Assert.assertTrue(it.next() instanceof ProcessNodeTriggeredEvent); // afterNodeTriggered - "EndProcess"
        Assert.assertTrue(it.next() instanceof ProcessNodeLeftEvent); // beforeNodeLeft - "EndProcess"
        Assert.assertTrue(it.next() instanceof ProcessNodeLeftEvent); // afterNodeLeft - "EndProcess"

        Assert.assertTrue(it.next() instanceof ProcessCompletedEvent); // beforeProcessCompleted
        Assert.assertTrue(it.next() instanceof ProcessCompletedEvent); // afterProcessCompleted
        Assert.assertFalse(it.hasNext()); // no more events now
    }

    private static class IterableProcessEventListener implements ProcessEventListener, Iterable<ProcessEvent> {
        private final List<ProcessEvent> events = new ArrayList<ProcessEvent>();

        public void beforeProcessStarted(ProcessStartedEvent event) {
            events.add(event);
        }

        public void afterProcessStarted(ProcessStartedEvent event) {
            events.add(event);
        }

        public void beforeProcessCompleted(ProcessCompletedEvent event) {
            events.add(event);
        }

        public void afterProcessCompleted(ProcessCompletedEvent event) {
            events.add(event);
        }

        public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
            events.add(event);
        }

        public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
            events.add(event);
        }

        public void beforeNodeLeft(ProcessNodeLeftEvent event) {
            events.add(event);
        }

        public void afterNodeLeft(ProcessNodeLeftEvent event) {
            events.add(event);
        }

        public void beforeVariableChanged(ProcessVariableChangedEvent event) {
            events.add(event);
        }

        public void afterVariableChanged(ProcessVariableChangedEvent event) {
            events.add(event);
        }

        public Iterator<ProcessEvent> iterator() {
            return events.iterator();
        }

    }
}
