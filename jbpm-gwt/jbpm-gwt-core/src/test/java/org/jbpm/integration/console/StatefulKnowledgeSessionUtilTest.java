package org.jbpm.integration.console;

import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.integration.JbpmGwtCoreTestCase;
import org.junit.Test;

public class StatefulKnowledgeSessionUtilTest extends JbpmGwtCoreTestCase {

    @Test
    public void generalTest() { 
        StatefulKnowledgeSession origKsession = StatefulKnowledgeSessionUtil.getStatefulKnowledgeSession();  
        int origKsessionId = origKsession.getId();
        assertTrue(origKsessionId > 0);
        
        StatefulKnowledgeSession newKsession = StatefulKnowledgeSessionUtil.getStatefulKnowledgeSession();
        assertTrue(newKsession == origKsession);
        assertTrue(origKsessionId == newKsession.getId());

        // test that origKsession has been disposed?
    }

}
