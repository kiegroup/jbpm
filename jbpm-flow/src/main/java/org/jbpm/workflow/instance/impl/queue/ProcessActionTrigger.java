package org.jbpm.workflow.instance.impl.queue;

public interface ProcessActionTrigger {

    void trigger();

    String getUniqueInstanceId();


}
