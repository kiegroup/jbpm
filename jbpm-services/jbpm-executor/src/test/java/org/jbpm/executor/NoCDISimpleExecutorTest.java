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
package org.jbpm.executor;

import java.util.List;
import java.util.UUID;

import javax.persistence.Persistence;

import org.jbpm.executor.impl.ExecutorImpl;
import org.jbpm.executor.impl.ExecutorServiceImpl;
import org.jbpm.executor.test.CountDownAsyncJobStartedListener;
import org.jbpm.test.util.ExecutorTestUtil;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.RequestInfo;
import org.kie.api.executor.STATUS;
import org.kie.api.runtime.query.QueryContext;
import org.kie.test.util.db.PoolingDataSourceWrapper;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class NoCDISimpleExecutorTest extends BasicExecutorBaseTest{
    
	private PoolingDataSourceWrapper pds;
	
    
    @Before
    public void setUp() {
        pds = ExecutorTestUtil.setupPoolingDataSource();
        emf = Persistence.createEntityManagerFactory("org.jbpm.executor");

        executorService = ExecutorServiceFactory.newExecutorService(emf);
        
        
        
        executorService.init();
        super.setUp();
    }

    @Test(timeout=10000)
    public void testJobsQueryWithStatusAfterInterrupt() throws InterruptedException {
        CountDownAsyncJobStartedListener countDownListener = new CountDownAsyncJobStartedListener(1);
        ((ExecutorServiceImpl) executorService).addAsyncJobListener(countDownListener);
        
        String uuid = UUID.randomUUID().toString();
        CommandContext ctxCMD = new CommandContext();
        ctxCMD.setData("businessKey", uuid);

        executorService.scheduleRequest("org.jbpm.executor.ReoccurringPrintOutCommand", ctxCMD);
        countDownListener.waitTillStarted();
        List<RequestInfo> uuidRequests = executorService.getRequestsByBusinessKey(uuid, new QueryContext());
        assertEquals(1, uuidRequests.size());
        assertEquals(STATUS.RUNNING, uuidRequests.get(0).getStatus());

        executorService.destroy();

        List<RequestInfo> info = executorService.getRequestsByBusinessKey(uuid, new QueryContext());
        assertEquals(STATUS.QUEUED, info.get(0).getStatus());

    }

    @After
    public void tearDown() {
        super.tearDown();
        executorService.destroy();
        if (emf != null) {
        	emf.close();
        }
        pds.close();
    }
   
    
}