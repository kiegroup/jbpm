package org.jbpm.persistence.mapdb;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.jbpm.process.instance.event.SignalManager;
import org.jbpm.process.instance.event.SignalManagerFactory;

public class MapDBSignalManagerFactory implements SignalManagerFactory {

	@Override
	public SignalManager createSignalManager(InternalKnowledgeRuntime kruntime) {
		return new MapDBSignalManager(kruntime);
	}

}
