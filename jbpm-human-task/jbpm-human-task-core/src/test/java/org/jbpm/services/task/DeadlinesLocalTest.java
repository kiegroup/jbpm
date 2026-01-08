/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

import org.jbpm.services.task.deadlines.notifications.impl.MockNotificationListener;
import org.jbpm.services.task.impl.TaskDeadlinesServiceImpl;
import org.kie.test.util.db.PoolingDataSourceWrapper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.kie.internal.task.api.InternalTaskService;

public class DeadlinesLocalTest extends DeadlinesBaseTest {

    private static boolean setupDataSource = false;
    private static PoolingDataSourceWrapper pds;
    private static EntityManagerFactory emf;

    @BeforeClass
    public static void beforeClass() {
        setupDataSource = true;
        pds = setupPoolingDataSource();
        emf = Persistence.createEntityManagerFactory( "org.jbpm.services.task");
    }
    
    @Before
    public void setup() {
        this.notificationListener = new MockNotificationListener();
        
        this.taskService = (InternalTaskService) HumanTaskServiceFactory.newTaskServiceConfigurator()
                                                .entityManagerFactory(emf)
                                                .getTaskService();
    }
    
    @After
    public void clean() {
        TaskDeadlinesServiceImpl.reset();
        super.tearDown();
    }
    
    @AfterClass
    public static void afterClass() {
        if (!setupDataSource) {
            return;
        }

        if (emf != null) {
            emf.close();
        }
        if (pds != null) {
            pds.close();
        }
    }

}
