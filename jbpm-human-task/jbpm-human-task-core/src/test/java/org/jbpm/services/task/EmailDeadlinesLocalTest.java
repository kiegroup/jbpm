/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.services.task;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.drools.core.time.TimerService;
import org.drools.core.time.impl.JDKTimerService;
import org.jbpm.process.core.timer.TimerServiceRegistry;
import org.junit.After;
import org.junit.Before;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.test.util.db.PoolingDataSourceWrapper;


public class EmailDeadlinesLocalTest extends EmailDeadlinesBaseTest {

	private PoolingDataSourceWrapper pds;
	private EntityManagerFactory emf;
	
    private String timerServiceId;

	@Before
	public void setup() {
		pds = setupPoolingDataSource();
		emf = Persistence.createEntityManagerFactory( "org.jbpm.services.task" );
		super.setup();
		this.taskService = (InternalTaskService) HumanTaskServiceFactory.newTaskServiceConfigurator()
												.entityManagerFactory(emf)
												.getTaskService();
        TimerService globalTs = new JDKTimerService(3);
        timerServiceId = "null" + TimerServiceRegistry.TIMER_SERVICE_SUFFIX;
        // and register it in the registry under 'default' key
        TimerServiceRegistry.getInstance().registerTimerService(timerServiceId, globalTs);
        TaskServiceRegistry.instance().registerTaskService(null, taskService);
	}
	
	@After
	public void clean() {

        TaskServiceRegistry.instance().remove(null);
        TimerServiceRegistry.getInstance().remove(timerServiceId).shutdown();

 		super.tearDown();
		if (emf != null) {
			emf.close();
		}
		if (pds != null) {
			pds.close();
		}
	}
}
