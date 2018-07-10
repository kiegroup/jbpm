/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.executor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jbpm.executor.impl.ExecutorServiceImpl;
import org.jbpm.test.util.ExecutorTestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutorQueryService;
import org.kie.api.executor.ExecutorService;
import org.kie.api.executor.RequestInfo;

import bitronix.tm.resource.jdbc.PoolingDataSource;

/*
 * Test case to verify proper locking of the RequestInfo table with
 * org.jbpm.persistence.jpa.hibernate.DisabledFollowOnLockOracle10gDialect
 *
 * Running the test case with default H2 database succeeds, as only one thread succeeds in obtaining the RequestInfo entity
 *
 * To run the test with Oracle, pass in the following system parameters:
 *
 * // it is expected that the test case fails with default oracle dialect:
 * -Dmaven.hibernate.dialect=org.hibernate.dialect.Oracle10gDialect
 * 
 * // the test should be successful using the custom dialect
 * -Dmaven.hibernate.dialect=org.jbpm.persistence.jpa.hibernate.DisabledFollowOnLockOracle10gDialect 
 *
 * -Dmaven.datasource.classname=oracle.jdbc.xa.client.OracleXADataSource 
 * -Dmaven.jdbc.driver.class=oracle.jdbc.xa.client.OracleXADataSource 
 * 
 * // modify the following properties as necessary
 * -Dmaven.jdbc.driver.jar=/data/misc/ojdbc6.jar 
 * -Dmaven.jdbc.username=jboss      
 * -Dmaven.jdbc.password=jboss 
 * -Dmaven.jdbc.db.server=localhost 
 * -Dmaven.jdbc.db.port=1521 
 * -Dmaven.jdbc.db.name=ORCLPDB1 
 * -Dmaven.jdbc.url=jdbc:oracle:thin:@localhost:1521/ORCLPDB1 
 * -Dmaven.jdbc.schema=jboss
 */
public class ExecutorQueryServiceTest {

	protected ExecutorService executorService;
	private static final int THREADS = 2;
	private static volatile AtomicInteger jobFound = new AtomicInteger(0);

	private PoolingDataSource pds;
	private EntityManagerFactory emf = null;

	@Before
	public void setUp() {
		pds = ExecutorTestUtil.setupPoolingDataSource();
		emf = Persistence.createEntityManagerFactory("org.jbpm.executor");

		executorService = ExecutorServiceFactory.newExecutorService(emf);
		executorService.setThreadPoolSize(2);
		executorService.setInterval(3000);
		executorService.setTimeunit(TimeUnit.MILLISECONDS);

		executorService.init();
	}

	@After
	public void tearDown() {
		executorService.clearAllRequests();
		executorService.clearAllErrors();

		System.clearProperty("org.kie.executor.msg.length");
		System.clearProperty("org.kie.executor.stacktrace.length");
		executorService.destroy();
		if (emf != null) {
			emf.close();
		}
		pds.close();
	}


	@Test
	public void singleThreadedExcecutionTest() throws InterruptedException {
		CommandContext ctxCMD = new CommandContext();
		ctxCMD.setData("businessKey", UUID.randomUUID().toString());

		executorService.scheduleRequest("org.jbpm.executor.commands.ReoccurringPrintOutCommand", ctxCMD);


		RequestInfo request = ((ExecutorServiceImpl)executorService).getQueryService().getRequestForProcessing();
		assertNotNull(request);

	}           

	@Test(timeout=10000)
	public void multiThreadedExcecutionTest() throws InterruptedException {
		CommandContext ctxCMD = new CommandContext();
		ctxCMD.setData("businessKey", UUID.randomUUID().toString());

		executorService.scheduleRequest("org.jbpm.executor.commands.ReoccurringPrintOutCommand", ctxCMD);

		CountDownLatch latch = new CountDownLatch(THREADS);
		for (int i = 0; i < THREADS; i++) {
			QueryServiceExecutorThread runner = new QueryServiceExecutorThread(i, ((ExecutorServiceImpl)executorService).getQueryService(), latch);
			Thread t = new Thread(runner, i + "-query-executor-runner");
			t.start();	

		}		
		latch.await();

		// check number of jobs found by concurrent threads 
		assertEquals(1, jobFound.intValue());
	}



	class QueryServiceExecutorThread implements Runnable {

		private int i;
		private ExecutorQueryService queryService;
		private CountDownLatch latch;

		public QueryServiceExecutorThread(int i, ExecutorQueryService queryService, CountDownLatch latch) {
			this.i = i;
			this.queryService = queryService;
			this.latch = latch;
		}

		@Override
		public void run() {
			RequestInfo request = queryService.getRequestForProcessing();
			if(request!=null) 
			{
				// increase the job counter 
				jobFound.incrementAndGet();
			}
			latch.countDown();
		}
	}
}
