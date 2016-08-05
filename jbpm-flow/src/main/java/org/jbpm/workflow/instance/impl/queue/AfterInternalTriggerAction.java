package org.jbpm.workflow.instance.impl.queue;

import org.jbpm.process.instance.ProcessImplementationPart;
import org.jbpm.workflow.instance.NodeInstance;
import org.jbpm.workflow.instance.node.queue.ComplexInternalTriggerNodeInstance;

public class AfterInternalTriggerAction implements ProcessInstanceAction {

    private final ComplexInternalTriggerNodeInstance nodeInstance;

    public AfterInternalTriggerAction(ComplexInternalTriggerNodeInstance nodeInstance) {
        this.nodeInstance = nodeInstance;
    }

    @Override
    public void trigger() {
        nodeInstance.afterInternalTrigger();
    }

    @Override
    public boolean actsOn(ProcessImplementationPart instance) {
        if( instance instanceof NodeInstance ) {
            return ((NodeInstance) this.nodeInstance).getUniqueId().equals(((NodeInstance) instance).getUniqueId());
        }
        return false;
    }

    @Override
    public String toString() {
        return nodeInstance.getClass().getSimpleName() + ".afterInternalTrigger()";
    }

}
