package org.jbpm.persistence.processinstance;

import static org.jbpm.persistence.util.PersistenceUtil.JBPM_PERSISTENCE_UNIT_NAME;
import static org.jbpm.persistence.util.PersistenceUtil.cleanUp;
import static org.jbpm.persistence.util.PersistenceUtil.setupWithPoolingDataSource;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.impl.KnowledgeCommandContext;
import org.jbpm.persistence.util.PersistenceUtil;
import org.jbpm.test.util.AbstractBaseTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.command.Context;
import org.kie.internal.persistence.jpa.JPAKnowledgeService;

public class JPAJbpmWorkItemManagerTest extends AbstractBaseTest {

    private HashMap<String, Object> context;

    @After
    public void cleanup() {
        cleanUp(context);
    }

    @Test
    public void jBPMSpecificWorkItemManager() {
        context = setupWithPoolingDataSource(JBPM_PERSISTENCE_UNIT_NAME);

        // load up the knowledge base
        Environment env = PersistenceUtil.createEnvironment(context);
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        KieBase kbase = kbuilder.newKnowledgeBase();

        // create session
        KieSession ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);
        Assert.assertTrue("Valid KnowledgeSession could not be created.", ksession != null && ksession.getIdentifier() > 0);

        WorkItemManager actualWorkItemManager = ksession.execute(new GenericCommand<WorkItemManager>() {
            @Override
            public WorkItemManager execute( Context context ) {
                KieSession ksession = ((KnowledgeCommandContext) context).getKieSession();
                return ksession.getWorkItemManager();
            }
        });


        assertEquals( "Incorrect work item manager class used!",
                actualWorkItemManager.getClass().getCanonicalName(),
                JPAProcessInstanceWorkItemManager.class.getCanonicalName() );
    }

}
