package org.jbpm.persistence.processinstance;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.persistence.jpa.processinstance.JPAWorkItemManager;

/**
 * This class is a jBPM-specific work item manager that should be used whenever
 * 1. jBPM is in the classpath
 * 2. and persistence is being used.
 */
public class JPAProcessInstanceWorkItemManager extends JPAWorkItemManager {

    public JPAProcessInstanceWorkItemManager(InternalKnowledgeRuntime kruntime) {
        super(kruntime);
    }

}
