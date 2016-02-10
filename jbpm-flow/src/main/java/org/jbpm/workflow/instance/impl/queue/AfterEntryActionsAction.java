package org.jbpm.workflow.instance.impl.queue;

import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.jbpm.workflow.instance.node.EntryActionExceptionHandlingNodeInstance;
import org.kie.api.runtime.process.NodeInstance;

public class AfterEntryActionsAction implements ProcessInstanceAction {

    private final EntryActionExceptionHandlingNodeInstance nodeInstance;
    private final NodeInstance from;
    private final String type;

    public AfterEntryActionsAction(
            EntryActionExceptionHandlingNodeInstance nodeInstance,
            NodeInstance from,
            String type) {
        this.nodeInstance = nodeInstance;
        this.from = from;
        this.type = type;
    }

    @Override
    public void trigger() {
        nodeInstance.afterEntryActions(from,type);
    }

    @Override
    public String getUniqueInstanceId() {
        // OCRAM getUniqueInstanceId => equals(NodeInstance)
        return null;
    }

    @Override
    public String toString() {
        String fromStr = nodeInstance == null ? "null" : nodeInstance.getClass().getSimpleName();
        String uniqueId = (String) ((NodeInstanceImpl) nodeInstance).getMetaData().get("UniqueId");
        return nodeInstance.getClass().getSimpleName() + "[" + uniqueId + "].afterEntryActions(" + fromStr + ", " + type + ")";
    }

}
