/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.services.task.persistence;

import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.internal.task.api.TaskPersistenceContext;
import org.kie.internal.task.api.TaskPersistenceContextManager;

public class MapDBTaskPersistenceContextManager implements
		TaskPersistenceContextManager {
	
	private Environment env;
	private MapDBTaskPersistenceContext cmdPersistenceContext;

	public MapDBTaskPersistenceContextManager(Environment environment) {
		this.env = environment;
	}

	@Override
	public TaskPersistenceContext getPersistenceContext() {
		if (cmdPersistenceContext == null) {
			Object ctx = env.get(EnvironmentName.CMD_SCOPED_ENTITY_MANAGER);
			if (ctx == null || !(ctx instanceof MapDBTaskPersistenceContext)) {
				beginCommandScopedEntityManager();
				cmdPersistenceContext = (MapDBTaskPersistenceContext) 
						this.env.get( EnvironmentName.CMD_SCOPED_ENTITY_MANAGER );
			} else {
				cmdPersistenceContext = (MapDBTaskPersistenceContext) ctx;
			}
		}
		return cmdPersistenceContext;
	}

	@Override
	public void beginCommandScopedEntityManager() {
		this.cmdPersistenceContext = new MapDBTaskPersistenceContext(env);
		this.env.set( EnvironmentName.CMD_SCOPED_ENTITY_MANAGER, this.cmdPersistenceContext );
	}


	@Override
	public void endCommandScopedEntityManager() {
		if (this.cmdPersistenceContext != null) {
			this.cmdPersistenceContext.close();
			this.cmdPersistenceContext = null;
			this.env.set(EnvironmentName.CMD_SCOPED_ENTITY_MANAGER, null);
		}
	}

	public void dispose() {
	}

}
