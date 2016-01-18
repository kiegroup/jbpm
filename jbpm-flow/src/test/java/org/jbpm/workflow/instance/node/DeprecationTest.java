package org.jbpm.workflow.instance.node;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.junit.Test;
import org.kie.test.util.StaticMethodTestHelper;

public class DeprecationTest {

    @Test
    public void idStrategyAndNodeInstanceCounterFields() throws Exception {
        boolean branch6x = StaticMethodTestHelper.projectVersionIsLessThan(7.0);

        String [] fieldNames =  {
                "nodeInstanceCounter",
                "deprecatedIdStrategy"
        };
        for( String fieldName : fieldNames ) {
            Class [] clss = {
                    WorkflowProcessInstanceImpl.class,
                    CompositeNodeInstance.class
            };
            for( Class fieldClass : clss ) {
                try {
                    fieldClass.getDeclaredField(fieldName);
                    assertTrue( "The " + fieldClass.getSimpleName() + "." + fieldName + " field should be removed once we move to 7.x!", branch6x );
                } catch( NoSuchFieldException nsfe ) {
                    fail( "Please remove the check for the " + fieldClass.getSimpleName() + "." + fieldName );
                }
            }
        }


    }
}
