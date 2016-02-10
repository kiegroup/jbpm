package org.jbpm.workflow.instance.impl.queue;

import org.jbpm.workflow.instance.impl.ExtendedNodeInstanceImpl;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;

public class AfterExitActionsAction implements ProcessInstanceAction {

    private final ExtendedNodeInstanceImpl nodeInstance;
    private final String type;
    private final boolean remove;

    public AfterExitActionsAction(
            ExtendedNodeInstanceImpl nodeInstance,
            String type,
            boolean remove) {
        this.nodeInstance = nodeInstance;
        this.type = type;
        this.remove = remove;
    }

    @Override
    public void trigger() {
        this.nodeInstance.afterExitActions(type, remove);
    }

    @Override
    public String getUniqueInstanceId() {
        // OCRAM getUniqueInstanceId => equals(NodeInstance)
        return null;
    }


    @Override
    public String toString() {
        String uniqueId = (String) ((NodeInstanceImpl) nodeInstance).getMetaData().get("UniqueId");
        return nodeInstance.getClass().getSimpleName() + "[" + uniqueId + "].afterExitActions(" + type + ", " + remove + ")";
    }
}
