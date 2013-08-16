package org.jbpm.persistence.processinstance;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.jbpm.process.instance.ProcessInstanceManager;
import org.jbpm.process.instance.ProcessInstanceManagerFactory;

public class InfinispanProcessInstanceManagerFactory implements ProcessInstanceManagerFactory {

	public ProcessInstanceManager createProcessInstanceManager(InternalKnowledgeRuntime kruntime) {
		InfinispanProcessInstanceManager result = new InfinispanProcessInstanceManager();
		result.setKnowledgeRuntime(kruntime);
		return result;
	}

}
