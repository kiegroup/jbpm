package org.jbpm.persistence.mapdb;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.jbpm.process.instance.ProcessInstanceManager;
import org.jbpm.process.instance.ProcessInstanceManagerFactory;

public class MapDBProcessInstanceManagerFactory implements
		ProcessInstanceManagerFactory {

	@Override
	public ProcessInstanceManager createProcessInstanceManager(
			InternalKnowledgeRuntime kruntime) {
		return new MapDBProcessInstanceManager(kruntime);
	}

}
