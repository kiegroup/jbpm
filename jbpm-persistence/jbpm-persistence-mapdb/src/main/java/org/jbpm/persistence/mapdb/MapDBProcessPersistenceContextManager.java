package org.jbpm.persistence.mapdb;

import org.drools.persistence.mapdb.MapDBPersistenceContextManager;
import org.jbpm.persistence.ProcessPersistenceContext;
import org.jbpm.persistence.ProcessPersistenceContextManager;
import org.kie.api.runtime.Environment;

public class MapDBProcessPersistenceContextManager extends MapDBPersistenceContextManager implements ProcessPersistenceContextManager {

	public MapDBProcessPersistenceContextManager(Environment env) {
		super(env);
	}

	@Override
	public ProcessPersistenceContext getProcessPersistenceContext() {
		return new MapDBProcessPersistenceContext(getDB(), getTXM(), getStrategies());
	}

}
