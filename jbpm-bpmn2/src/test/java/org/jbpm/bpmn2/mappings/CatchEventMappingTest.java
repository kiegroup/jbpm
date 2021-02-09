package org.jbpm.bpmn2.mappings;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jbpm.bpmn2.JbpmBpmn2TestCase;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.runtime.KieSession;


public class CatchEventMappingTest  extends JbpmBpmn2TestCase {

    @BeforeClass
    public static void setup() throws Exception {
        setUpDataSource();
    }

    @Test
    public void testStartNodeMapping() throws Exception {
        KieBase kbase = createKnowledgeBase("mappings/StartNodeMapping.bpmn2");
        KieSession ksession = createKnowledgeSession(kbase);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean ok = new AtomicBoolean(false);
        ksession.addEventListener(new DefaultProcessEventListener() {

            @Override
            public void afterProcessCompleted(ProcessCompletedEvent event) {
                String actualValue = getProcessVarValue(event.getProcessInstance(), "procVar");
                ok.set("HOLA".equals(actualValue));
                assertProcessVarValue(event.getProcessInstance(), "procVar", "HOLA");
                latch.countDown();
            }
        });
        ksession.signalEvent("Message-message", "HOLA");
        latch.await();
        Assert.assertTrue(ok.get());
    }

    @Test
    public void testIntermediateCatchNodeMapping() throws Exception {
        KieBase kbase = createKnowledgeBase("mappings/CatchNodeMapping.bpmn2");
        KieSession ksession = createKnowledgeSession(kbase);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean ok = new AtomicBoolean(false);
        ksession.addEventListener(new DefaultProcessEventListener() {

            @Override
            public void afterProcessCompleted(ProcessCompletedEvent event) {
                String actualValue = getProcessVarValue(event.getProcessInstance(), "procVar");
                ok.set("HOLA".equals(actualValue));
                assertProcessVarValue(event.getProcessInstance(), "procVar", "HOLA");
                latch.countDown();
            }
        });
        ksession.startProcess("CatchNodeMapping");
        ksession.signalEvent("Message-message", "HOLA");
        latch.await();
        Assert.assertTrue(ok.get());
    }
}
