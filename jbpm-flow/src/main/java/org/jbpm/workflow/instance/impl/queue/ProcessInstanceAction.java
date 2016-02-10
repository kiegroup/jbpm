package org.jbpm.workflow.instance.impl.queue;

public interface ProcessInstanceAction {

    void trigger();

    String getUniqueInstanceId();


}
