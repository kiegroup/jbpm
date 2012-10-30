package org.jbpm.bpmn2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.event.process.DefaultProcessEventListener;
import org.drools.event.process.ProcessCompletedEvent;
import org.drools.event.process.ProcessNodeLeftEvent;
import org.drools.event.process.ProcessNodeTriggeredEvent;
import org.drools.event.process.ProcessStartedEvent;
import org.drools.event.process.ProcessVariableChangedEvent;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class VersioningTest extends JbpmBpmn2TestCase {
    public VersioningTest() {
        super(false);
    }

    @Before
    public void prepareEnvironment() {
        setUp();
    }

    @Test
    public void testVersioning() {
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(createKnowledgeBuilder("HelloWorld_1.0.bpmn").getKnowledgePackages());
        assertEquals(kbase.getProcess("hello").getVersion(), "1.0");
        kbase.removeProcess("hello");

        kbase.addKnowledgePackages(createKnowledgeBuilder("HelloWorld_2.0.bpmn").getKnowledgePackages());
        assertEquals(kbase.getProcess("hello").getVersion(), "2.0");
        kbase.removeProcess("hello");
    }

    @Test
    public void testAddNewVersion() {
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(createKnowledgeBuilder("HelloWorld_1.0.bpmn").getKnowledgePackages());

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        ProcessInstance firstVersion = ksession.createProcessInstance("hello", null);
        assertEquals(firstVersion.getProcess().getVersion(), "1.0");

        kbase.addKnowledgePackages(createKnowledgeBuilder("HelloWorld_2.0.bpmn").getKnowledgePackages());

        ProcessInstance secondVersion = ksession.createProcessInstance("hello", null);
        assertEquals(firstVersion.getProcess().getVersion(), "1.0");
        assertEquals(secondVersion.getProcess().getVersion(), "2.0");

        TrackingProcessEventListener listener = new TrackingProcessEventListener();
        ksession.addEventListener(listener);
        ksession.startProcessInstance(firstVersion.getId());
        checkProcess1(listener);

        listener.clear();
        ksession.startProcessInstance(secondVersion.getId());
        checkProcess2(listener);
    }

    @Test
    public void testOldVersion() {
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(createKnowledgeBuilder("HelloWorld_2.0.bpmn").getKnowledgePackages());

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        TrackingProcessEventListener listener = new TrackingProcessEventListener();
        ksession.addEventListener(listener);

        ProcessInstance firstVersion = ksession.createProcessInstance("hello", null);
        assertEquals(firstVersion.getProcess().getVersion(), "2.0");

        kbase.addKnowledgePackages(createKnowledgeBuilder("HelloWorld_1.0.bpmn").getKnowledgePackages());

        ProcessInstance secondVersion = ksession.createProcessInstance("hello", null);
        assertEquals(firstVersion.getProcess().getVersion(), "2.0");
        assertEquals(secondVersion.getProcess().getVersion(), "1.0");

        ksession.startProcessInstance(secondVersion.getId());
        checkProcess1(listener);

        listener.clear();

        ksession.startProcessInstance(firstVersion.getId());
        checkProcess2(listener);
    }

    @After
    public void after() {
        tearDown();
    }

    private void checkProcess1(TrackingProcessEventListener listener) {
        String[] nodes = new String[] { "Start", "Hello world", "End" };
        assertTrue(listener.wasProcessStarted("hello"));
        for (String node : nodes) {
            assertTrue(listener.wasNodeTriggered(node));
            assertTrue(listener.wasNodeLeft(node));
        }
        assertTrue(listener.wasProcessCompleted("hello"));
        assertEquals(listener.getProcessesStarted().size(), 1);
        assertEquals(listener.getProcessesCompleted().size(), 1);
        assertEquals(listener.getNodesLeft().toString(), listener.getNodesLeft().size(), nodes.length);
        assertEquals(listener.getNodesTriggered().toString(), listener.getNodesTriggered().size(), nodes.length);
        assertEquals(listener.getVariablesChanged().size(), 0);
    }

    private void checkProcess2(TrackingProcessEventListener listener) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            // LOGGER.warn("Interrupted", ex);
        }
        // LOGGER.info("Triggered: " + listener.getNodesTriggered().toString());
        // LOGGER.info("Left: " + listener.getNodesLeft().toString());
        String[] nodes = new String[] { "Start", "Hello world", "Diverge", "Timer", "Converge", "Converge", "Goodbye world",
                "End" };
        assertTrue(listener.wasProcessStarted("hello"));
        for (String node : nodes) {
            assertTrue(node, listener.wasNodeTriggered(node));
            assertTrue(node, listener.wasNodeLeft(node));
        }
        assertTrue(listener.wasProcessCompleted("hello"));
        assertEquals(listener.getProcessesStarted().size(), 1);
        assertEquals(listener.getProcessesCompleted().size(), 1);
        assertEquals(listener.getNodesLeft().toString(), listener.getNodesLeft().size(), nodes.length);
        assertEquals(listener.getNodesTriggered().toString(), listener.getNodesTriggered().size(), nodes.length);
        assertEquals(listener.getVariablesChanged().size(), 0);
    }

    private KnowledgeBuilder createKnowledgeBuilder(String... processes) {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        for (String process : processes) {
            kbuilder.add(ResourceFactory.newClassPathResource(process), ResourceType.BPMN2);
        }

        assertFalse(kbuilder.getErrors().toString(), kbuilder.hasErrors());

        return kbuilder;
    }

    private static class TrackingProcessEventListener extends DefaultProcessEventListener {
        private final List<String> processesStarted = new ArrayList<String>();
        private final List<String> processesCompleted = new ArrayList<String>();

        private final List<String> nodesTriggered = new ArrayList<String>();
        private final List<String> nodesLeft = new ArrayList<String>();

        private final List<String> variablesChanged = new ArrayList<String>();

        @Override
        public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
            nodesTriggered.add(event.getNodeInstance().getNodeName());
        }

        @Override
        public void beforeNodeLeft(ProcessNodeLeftEvent event) {
            nodesLeft.add(event.getNodeInstance().getNodeName());
        }

        @Override
        public void beforeProcessStarted(ProcessStartedEvent event) {
            processesStarted.add(event.getProcessInstance().getProcessId());
        }

        @Override
        public void beforeProcessCompleted(ProcessCompletedEvent event) {
            processesCompleted.add(event.getProcessInstance().getProcessId());
        }

        @Override
        public void beforeVariableChanged(ProcessVariableChangedEvent event) {
            variablesChanged.add(event.getVariableId());
        }

        public List<String> getNodesTriggered() {
            return Collections.unmodifiableList(nodesTriggered);
        }

        public List<String> getNodesLeft() {
            return Collections.unmodifiableList(nodesLeft);
        }

        public List<String> getProcessesStarted() {
            return Collections.unmodifiableList(processesStarted);
        }

        public List<String> getProcessesCompleted() {
            return Collections.unmodifiableList(processesCompleted);
        }

        public List<String> getVariablesChanged() {
            return Collections.unmodifiableList(variablesChanged);
        }

        public boolean wasNodeTriggered(String nodeName) {
            return nodesTriggered.contains(nodeName);
        }

        public boolean wasNodeLeft(String nodeName) {
            return nodesLeft.contains(nodeName);
        }

        public boolean wasProcessStarted(String processName) {
            return processesStarted.contains(processName);
        }

        public boolean wasProcessCompleted(String processName) {
            return processesCompleted.contains(processName);
        }

        public void clear() {
            nodesTriggered.clear();
            nodesLeft.clear();
            processesStarted.clear();
            processesCompleted.clear();
            variablesChanged.clear();
        }
    }
}
