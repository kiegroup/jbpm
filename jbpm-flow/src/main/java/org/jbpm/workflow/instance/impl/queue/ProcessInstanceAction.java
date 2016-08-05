package org.jbpm.workflow.instance.impl.queue;

import org.jbpm.process.instance.ProcessImplementationPart;
import org.jbpm.workflow.instance.ProcessInstanceActionQueueExecutor;

public interface ProcessInstanceAction {

    /**
     * This method is called by the {@link ProcessInstanceActionQueueExecutor} to trigger this action
     */
    void trigger();

    /**
     * This method is used to determine if the action should be removed from the queue (in the {@link ProcessInstanceActionQueueExecutor})
     * because the associated {@link ProcessImplementationPart} has been cancelled.
     *
     * @param instance A {@link ProcessImplementationPart} instance
     * @return Whether or not the given {@link ProcessImplementationPart} is related to this instance
     */
    boolean actsOn(ProcessImplementationPart instance);

}
