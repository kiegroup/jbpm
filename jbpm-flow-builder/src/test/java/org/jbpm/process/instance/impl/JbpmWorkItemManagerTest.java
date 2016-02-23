package org.jbpm.process.instance.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.impl.KnowledgeCommandContext;
import org.jbpm.test.util.AbstractBaseTest;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.command.Context;

public class JbpmWorkItemManagerTest extends AbstractBaseTest {

    @Test
    public void jBPMSpecificWorkItemManager() {
        // load up the knowledge base
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        KieBase kbase = kbuilder.newKnowledgeBase();
        KieSession ksession = kbase.newKieSession();

        WorkItemManager manager = ksession.getWorkItemManager();
        assertNotNull(manager);

        WorkItemManager actualWorkItemManager = ksession.execute(new GenericCommand<WorkItemManager>() {
            @Override
            public WorkItemManager execute( Context context ) {
                KieSession ksession = ((KnowledgeCommandContext) context).getKieSession();
                return ksession.getWorkItemManager();
            }
        });

        assertEquals( "Incorrect work item manager class used!",
                actualWorkItemManager.getClass().getCanonicalName(),
                ProcessInstanceWorkItemManager.class.getCanonicalName() );
    }
}
