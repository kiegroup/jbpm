package org.jbpm.workflow.instance.node;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.junit.Test;
import org.kie.test.util.StaticMethodTestHelper;

public class DeprecationTest {

    @Test
    public void stacklessUsedByDefault() throws Exception {
        boolean branch8x = StaticMethodTestHelper.projectVersionIsLessThan(8.0);

        RuleFlowProcessInstance procInst = new RuleFlowProcessInstance();
        assertTrue( "Stackless should be used by default as of 8.x",
                ! branch8x || ! procInst.isStackless() );

    }
}
