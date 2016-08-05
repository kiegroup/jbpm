package org.jbpm.workflow.instance.impl.queue;

import org.jbpm.process.instance.ProcessImplementationPart;
import org.jbpm.workflow.instance.NodeInstance;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.jbpm.workflow.instance.node.StateNodeInstance;

public class AfterEventActionsAction implements ProcessInstanceAction {

    private final StateNodeInstance stateNodeInstance;
    private final NodeInstance nodeInstance;
    private final String type;

    public AfterEventActionsAction(
            StateNodeInstance exitActionNodeInstance,
            NodeInstance nodeInstance,
            String type) {
        this.stateNodeInstance = exitActionNodeInstance;
        this.nodeInstance = nodeInstance;
        this.type = type;
    }

    @Override
    public void trigger() {
        stateNodeInstance.afterEventActions(nodeInstance, type);
    }

    @Override
    public boolean actsOn(ProcessImplementationPart instance) {
        if( instance instanceof NodeInstance ) {
            return this.stateNodeInstance.getUniqueId().equals(((NodeInstance) instance).getUniqueId());
        }
        return false;
    }

    @Override
    public String toString() {
        String nodeInstanceStr = nodeInstance == null ? "null" : nodeInstance.getClass().getSimpleName();
        String uniqueId = (String) ((NodeInstanceImpl) stateNodeInstance).getMetaData().get("UniqueId");
        return stateNodeInstance.getClass().getSimpleName() + "[" + uniqueId + "].afterEventActions(" + nodeInstanceStr + ", " + type + ")";
    }
}
