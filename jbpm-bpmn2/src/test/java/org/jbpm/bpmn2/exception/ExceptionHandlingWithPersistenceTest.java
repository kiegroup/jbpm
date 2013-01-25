package org.jbpm.bpmn2.exception;

import static org.jbpm.persistence.util.PersistenceUtil.*;

import java.util.HashMap;

import org.drools.exception.ExceptionHandlingInterceptor;
import org.jbpm.process.audit.JPAProcessInstanceDbLog;
import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
import org.jbpm.workflow.exception.ExceptionConstants;
import org.junit.After;
import org.junit.Before;
import org.kie.KnowledgeBase;
import org.kie.persistence.jpa.JPAKnowledgeService;
import org.kie.runtime.*;

public class ExceptionHandlingWithPersistenceTest extends ExceptionHandlingTest {

    private HashMap<String, Object> context;
    protected final boolean persistence = true;

    @Before
    public void setup() {
        context = setupWithPoolingDataSource(JBPM_PERSISTENCE_UNIT_NAME);
    }

    @After
    public void cleanup() {
        cleanUp(context);
    }

    /**
     * HELPER METHODS
     */

    protected StatefulKnowledgeSession createKnowledgeSession(KnowledgeBase kbase) {
        Environment env = createEnvironment(context);

        StatefulKnowledgeSession result = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);
        new JPAWorkingMemoryDbLogger(result);
        JPAProcessInstanceDbLog.setEnvironment(result.getEnvironment());
        return result;
    }

    protected StatefulKnowledgeSession createExceptionHandlingKnowledgeSession(KnowledgeBase kbase) {
        StatefulKnowledgeSession ksession = null;

        Environment env = createEnvironment(context);
        // configure exception catching
        env.set(ExceptionConstants.EXCEPTION_ERROR_CODE, exceptionErrorCode);
        env.set(EnvironmentName.COMMAND_SERVICE_INTERCEPTOR, new ExceptionHandlingInterceptor());

        ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);

        new JPAWorkingMemoryDbLogger(ksession);
        JPAProcessInstanceDbLog.setEnvironment(ksession.getEnvironment());

        return ksession;
    }

}
