package org.jbpm.process.instance.impl;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.process.instance.impl.DefaultWorkItemManager;

/**
 * This class is a jBPM-specific work item manager that should be used whenever
 * 1. jBPM is in the classpath
 * 2. and persistence is *not* being used.
 */
public class ProcessInstanceWorkItemManager extends DefaultWorkItemManager {

    public ProcessInstanceWorkItemManager(InternalKnowledgeRuntime kruntime) {
        super(kruntime);
    }

}
