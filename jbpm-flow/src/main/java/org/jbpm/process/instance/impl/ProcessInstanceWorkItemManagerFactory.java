package org.jbpm.process.instance.impl;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.process.instance.WorkItemManager;
import org.drools.core.process.instance.WorkItemManagerFactory;

public class ProcessInstanceWorkItemManagerFactory implements WorkItemManagerFactory {

    @Override
    public WorkItemManager createWorkItemManager( InternalKnowledgeRuntime kruntime ) {
        return new ProcessInstanceWorkItemManager(kruntime);
    }

}
