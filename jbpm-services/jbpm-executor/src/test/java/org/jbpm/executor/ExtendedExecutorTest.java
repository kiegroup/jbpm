/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jbpm.executor.impl.ExecutorImpl;
import org.jbpm.executor.impl.ExecutorServiceImpl;
import org.jbpm.executor.test.CountDownAsyncJobListener;
import org.jbpm.test.persistence.util.PersistenceUtil;
import org.jbpm.test.util.ExecutorTestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutorService;
import org.kie.api.executor.RequestInfo;
import org.kie.api.executor.STATUS;
import org.kie.api.executor.ExecutorService.IdProvider;
import org.kie.api.runtime.query.QueryContext;
import org.kie.test.util.db.PoolingDataSourceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExtendedExecutorTest {
    private static final Logger logger = LoggerFactory.getLogger(ExtendedExecutorTest.class);

    private ExecutorService executorService;

    private static Properties dsProps;

    private EntityManagerFactory emf = null;

    private PoolingDataSourceWrapper pds;

    @BeforeClass
    public static void createDBServer() {
        System.setProperty("org.kie.executor.id", "owner");
        System.setProperty("org.kie.executor.setDefaultOwner", "true");
        System.setProperty("org.kie.executor.olderThan", "2");
        dsProps = ExecutorTestUtil.getDatasourceProperties();
        dsProps.setProperty("url", "jdbc:h2:tcp://localhost:9123/target/./jbpm-exec-test;MVCC=TRUE");
        dsProps.setProperty("tcpPort", "9123");
        PersistenceUtil.startH2TcpServer(dsProps);
        IdProvider.reset();
    }

    @AfterClass
    public static void stopDBServer() {
        System.clearProperty("org.kie.executor.id");
        System.clearProperty("org.kie.executor.setDefaultOwner");
        System.clearProperty("org.kie.executor.olderThan");
        PersistenceUtil.stopH2TcpServer();
        IdProvider.reset();
    }

    @Before
    public void setUp() {

        pds = PersistenceUtil.setupPoolingDataSource(dsProps, "jdbc/jbpm-ds");
        emf = Persistence.createEntityManagerFactory("org.jbpm.executor");

        executorService = ExecutorServiceFactory.newExecutorService(emf);

        executorService.setThreadPoolSize(1);
        executorService.setInterval(3);

        executorService.init();
    }

    @After
    public void tearDown() {
        executorService.clearAllRequests();
        executorService.clearAllErrors();
        executorService.destroy();
        if (emf != null) {
            emf.close();
        }
        pds.close();
    }

    protected CountDownAsyncJobListener configureListener(int threads) {
        CountDownAsyncJobListener countDownListener = new CountDownAsyncJobListener(threads);
        ((ExecutorServiceImpl) executorService).addAsyncJobListener(countDownListener);

        return countDownListener;
    }

    @Test(timeout = 60000)
    public void testSchedulerWithOwner() throws Exception {
        CommandContext ctxCMD = new CommandContext();
        ctxCMD.setData("businessKey", UUID.randomUUID().toString());
        Date schedule = new Date(Instant.now().plus(Duration.ofSeconds(5L)).toEpochMilli());
        logger.info("schedule command for {}", schedule);
        executorService.scheduleRequest("org.jbpm.executor.commands.PrintOutCommand", schedule, ctxCMD);
        List<RequestInfo> rescheduled = executorService.getRequestsByBusinessKey((String) ctxCMD.getData("businessKey"), Arrays.asList(STATUS.QUEUED), new QueryContext());
        assertEquals("owner", ((org.jbpm.executor.entities.RequestInfo) rescheduled.get(0)).getOwner());

    }

    // this test that only some jobs overdue by olderThan are scheduled
    @Test(timeout = 60000)
    public void testOlderThan() throws Exception {
        CommandContext ctxCMD = new CommandContext();
        ctxCMD.setData("businessKey", UUID.randomUUID().toString());

        CountDownAsyncJobListener countDownListener = configureListener(1);

        ExecutorImpl impl = ((ExecutorImpl) ((ExecutorServiceImpl) executorService).getExecutor());

        long start = System.currentTimeMillis();

        Date schedule = new Date(Instant.now().plus(Duration.ofSeconds(5L)).toEpochMilli());
        logger.info("schedule command for {}", schedule);
        impl.getExecutorStoreService().persistRequest(impl.buildRequestInfo("org.jbpm.executor.commands.PrintOutCommand", schedule, ctxCMD), (tmp) -> {});

        countDownListener.waitTillCompleted();
        long end = System.currentTimeMillis();
        assertTrue(end - start >= 7000);
    }
}
