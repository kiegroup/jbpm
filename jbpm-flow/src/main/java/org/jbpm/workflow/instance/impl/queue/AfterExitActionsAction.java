package org.jbpm.workflow.instance.impl.queue;

import org.jbpm.process.instance.ProcessImplementationPart;
import org.jbpm.workflow.instance.NodeInstance;
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
    public boolean actsOn(ProcessImplementationPart instance) {
        if( instance instanceof NodeInstance ) {
            return this.nodeInstance.getUniqueId().equals(((NodeInstance) instance).getUniqueId());
        }
        return false;
    }


    @Override
    public String toString() {
        String uniqueId = (String) ((NodeInstanceImpl) nodeInstance).getMetaData().get("UniqueId");
        return nodeInstance.getClass().getSimpleName() + "[" + uniqueId + "].afterExitActions(" + type + ", " + remove + ")";
    }
}
