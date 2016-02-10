package org.jbpm.workflow.instance.impl.queue;

import org.jbpm.workflow.instance.NodeInstance;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;

public class AfterInternalTriggerAction implements ProcessInstanceAction {

    private final NodeInstance nodeInstance;

    public AfterInternalTriggerAction(NodeInstance nodeInstance) {
        this.nodeInstance = nodeInstance;
    }

    @Override
    public void trigger() {
        nodeInstance.afterInternalTrigger();
    }

    @Override
    public String getUniqueInstanceId() {
        // OCRAM getUniqueInstanceId => equals(NodeInstance)
        return ((NodeInstanceImpl) nodeInstance).getUniqueId();
    }

    @Override
    public String toString() {
        return nodeInstance.getClass().getSimpleName() + ".afterInternalTrigger()";
    }

}
