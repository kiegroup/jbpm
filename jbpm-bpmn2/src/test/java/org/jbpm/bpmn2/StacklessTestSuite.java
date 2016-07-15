package org.jbpm.bpmn2;

import org.jbpm.bpmn2.structureref.StructureRefTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    StandaloneBPMNProcessTest.class,
    StartEventTest.class,
    ResourceTest.class,
    IntermediateEventTest.class,
    FlowTest.class,
    EscalationEventTest.class,
    ErrorEventTest.class,
    EndEventTest.class,
    DataTest.class,
    StructureRefTest.class,
    MultiInstanceTest.class,

    // Broken
    ActivityTest.class, // ad hoc
    CompensationTest.class
})
public class StacklessTestSuite {

}
