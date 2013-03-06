package org.jbpm;

import org.jbpm.test.JbpmTestCase;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.process.ProcessInstance;

/**
 * This is a sample file to test a process.
 */
public class ProcessTest extends JbpmTestCase {

    @BeforeClass
    public static void setup() throws Exception {
        if (PERSISTENCE) {
            setUpDataSource();
        }
    }

    @Test
    public void testProcess() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("hello.bpmn");
        ProcessInstance processInstance = ksession
                .startProcess("com.sample.bpmn.hello");
        // check whether the process instance has completed successfully
        assertProcessInstanceCompleted(processInstance);
        assertNodeTriggered(processInstance.getId(), "StartProcess", "Hello",
                "EndProcess");
    }

}