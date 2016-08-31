package org.jbpm.persistence.processinstance;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.process.instance.WorkItemManager;
import org.drools.core.process.instance.WorkItemManagerFactory;

public class JPAProcessInstanceWorkItemManagerFactory implements WorkItemManagerFactory {

    @Override
    public WorkItemManager createWorkItemManager( InternalKnowledgeRuntime kruntime ) {
        return new JPAProcessInstanceWorkItemManager(kruntime);
    }

}
