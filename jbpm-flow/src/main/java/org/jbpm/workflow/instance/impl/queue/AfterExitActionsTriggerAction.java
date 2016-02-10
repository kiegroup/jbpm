package org.jbpm.workflow.instance.impl.queue;

import org.jbpm.workflow.instance.NodeInstance;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.jbpm.workflow.instance.node.StateNodeInstance;

public class AfterExitActionsTriggerAction implements ProcessInstanceAction {

    private final StateNodeInstance stateNodeInstance;
    private final NodeInstance nodeInstance;
    private final String type;

    public AfterExitActionsTriggerAction(
            StateNodeInstance exitActionNodeInstance,
            NodeInstance nodeInstance,
            String type) {
        this.stateNodeInstance = exitActionNodeInstance;
        this.nodeInstance = nodeInstance;
        this.type = type;
    }

    @Override
    public void trigger() {
        stateNodeInstance.afterExitActions(nodeInstance, type);
    }

    @Override
    public String getUniqueInstanceId() {
        // OCRAM getUniqueInstanceId => equals(NodeInstance)
        return null;
    }

    @Override
    public String toString() {
        String nodeInstanceStr = nodeInstance == null ? "null" : nodeInstance.getClass().getSimpleName();
        String uniqueId = (String) ((NodeInstanceImpl) stateNodeInstance).getMetaData().get("UniqueId");
        return stateNodeInstance.getClass().getSimpleName() + "[" + uniqueId + "].afterExitActions(" + nodeInstanceStr + ", " + type + ")";
    }
}
