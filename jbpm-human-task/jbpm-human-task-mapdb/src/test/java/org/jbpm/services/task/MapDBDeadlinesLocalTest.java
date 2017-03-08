/*

 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.services.task;

import java.util.Map;

import org.jbpm.persistence.mapdb.util.MapDBProcessPersistenceUtil;
import org.jbpm.services.task.deadlines.notifications.impl.MockNotificationListener;
import org.jbpm.services.task.impl.TaskDeadlinesServiceImpl;
import org.jbpm.services.task.util.MapDBTaskPersistenceUtil;
import org.junit.After;
import org.junit.Before;
import org.kie.api.runtime.Environment;

/**
 *
 *
 */
//@org.junit.Ignore("For now, we're keeping it commented out")//TODO NEXT remove this
public class MapDBDeadlinesLocalTest extends DeadlinesLocalTest {

	protected Map<String, Object> context;
	
	@Before
	@Override
	public void setup() {
		this.notificationListener = new MockNotificationListener();
		this.context = MapDBProcessPersistenceUtil.setupMapDB();
		Environment env = MapDBProcessPersistenceUtil.createEnvironment(context);
		this.taskService = MapDBTaskPersistenceUtil.createTaskService(env);
	}
	
	@After
	@Override
	public void clean() {
		TaskDeadlinesServiceImpl.reset();
		MapDBProcessPersistenceUtil.cleanUp(context);
	}

}
