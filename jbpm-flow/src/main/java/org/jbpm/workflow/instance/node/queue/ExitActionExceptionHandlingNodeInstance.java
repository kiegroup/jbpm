package org.jbpm.workflow.instance.node.queue;

public interface ExitActionExceptionHandlingNodeInstance {

    void afterExitActions(String type, boolean remove);

}
