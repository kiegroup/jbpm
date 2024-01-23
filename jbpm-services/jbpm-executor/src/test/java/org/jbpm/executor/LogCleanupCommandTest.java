/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitConfig;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.jbpm.executor.impl.ExecutorServiceImpl;
import org.jbpm.executor.test.CountDownAsyncJobListener;
import org.jbpm.test.util.ExecutorTestUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutorService;
import org.kie.api.executor.RequestInfo;
import org.kie.api.executor.STATUS;
import org.kie.api.runtime.query.QueryContext;
import org.kie.test.util.db.PoolingDataSourceWrapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(BMUnitRunner.class)
@BMUnitConfig(loadDirectory = "target/test-classes")
public class LogCleanupCommandTest {

	private PoolingDataSourceWrapper pds;

    protected ExecutorService executorService;
    protected EntityManagerFactory emf = null;

    @Before
    public void setUp() {
        pds = ExecutorTestUtil.setupPoolingDataSource();
        emf = Persistence.createEntityManagerFactory("org.jbpm.executor");

        executorService = ExecutorServiceFactory.newExecutorService(emf);

        executorService.init();
        executorService.setThreadPoolSize(1);
        executorService.setInterval(3);
    }

    @After
    public void tearDown() {
        executorService.clearAllErrors();
        executorService.clearAllRequests();

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


    @Test(timeout=10000)
    @BMScript(value = "byteman-scripts/simulateSlowLogCleanupCommand.btm")
    public void logCleanupSingleRunTest() throws InterruptedException {
        CountDownAsyncJobListener countDownListener = configureListener(1);

        CommandContext ctxCMD = new CommandContext();
        ctxCMD.setData("businessKey", UUID.randomUUID().toString());
        ctxCMD.setData("SingleRun", "true");
        ctxCMD.setData("EmfName", "org.jbpm.executor");
        ctxCMD.setData("SkipProcessLog", "true");
        ctxCMD.setData("SkipTaskLog", "true");
        executorService.scheduleRequest("org.jbpm.executor.commands.LogCleanupCommand", ctxCMD);

        countDownListener.waitTillCompleted();

        List<RequestInfo> rescheduled = executorService.getRequestsByBusinessKey((String)ctxCMD.getData("businessKey"), Arrays.asList(STATUS.QUEUED), new QueryContext());
        assertEquals(0, rescheduled.size());

        List<RequestInfo> inErrorRequests = executorService.getInErrorRequests(new QueryContext());
        assertEquals(0, inErrorRequests.size());
        List<RequestInfo> queuedRequests = executorService.getQueuedRequests(new QueryContext());
        assertEquals(0, queuedRequests.size());
        List<RequestInfo> executedRequests = executorService.getCompletedRequests(new QueryContext());
        assertEquals(1, executedRequests.size());
    }

    @Test(timeout=10000)
    @BMScript(value = "byteman-scripts/simulateSlowLogCleanupCommand.btm")
    public void logCleanupNextRunIntervalTest() throws InterruptedException {
        CountDownAsyncJobListener countDownListener = configureListener(1);

        CommandContext ctxCMD = new CommandContext();
        ctxCMD.setData("businessKey", UUID.randomUUID().toString());
        ctxCMD.setData("NextRun", "10s");
        ctxCMD.setData("EmfName", "org.jbpm.executor");
        ctxCMD.setData("SkipProcessLog", "true");
        ctxCMD.setData("SkipTaskLog", "true");
        executorService.scheduleRequest("org.jbpm.executor.commands.LogCleanupCommand", ctxCMD);

        countDownListener.waitTillCompleted();

        List<RequestInfo> rescheduled = executorService.getRequestsByBusinessKey((String)ctxCMD.getData("businessKey"), Arrays.asList(STATUS.QUEUED), new QueryContext());
        assertEquals(1, rescheduled.size());

        List<RequestInfo> inErrorRequests = executorService.getInErrorRequests(new QueryContext());
        assertEquals(0, inErrorRequests.size());
        List<RequestInfo> queuedRequests = executorService.getQueuedRequests(new QueryContext());
        assertEquals(1, queuedRequests.size());
        List<RequestInfo> executedRequests = executorService.getCompletedRequests(new QueryContext());
        assertEquals(1, executedRequests.size());

        executorService.cancelRequest(queuedRequests.get(0).getId());

        long firstExecution = executedRequests.get(0).getTime().getTime();
        long nextExecution = queuedRequests.get(0).getTime().getTime();

        // time difference between first and second should be at least 11 seconds (10s next interval + artificial slowdown from byteman)
        long diff = nextExecution - firstExecution;
        assertTrue(diff > 11000);
    }

    @Test(timeout=10000)
    @BMScript(value = "byteman-scripts/simulateSlowLogCleanupCommand.btm")
    public void logCleanupNextRunFixedTest() throws InterruptedException {
        CountDownAsyncJobListener countDownListener = configureListener(1);

        CommandContext ctxCMD = new CommandContext();
        ctxCMD.setData("businessKey", UUID.randomUUID().toString());
        ctxCMD.setData("NextRun", "10s");
        ctxCMD.setData("EmfName", "org.jbpm.executor");
        ctxCMD.setData("SkipProcessLog", "true");
        ctxCMD.setData("SkipTaskLog", "true");
        ctxCMD.setData("RepeatMode", "fixed");
        executorService.scheduleRequest("org.jbpm.executor.commands.LogCleanupCommand", ctxCMD);

        countDownListener.waitTillCompleted();

        List<RequestInfo> rescheduled = executorService.getRequestsByBusinessKey((String)ctxCMD.getData("businessKey"), Arrays.asList(STATUS.QUEUED), new QueryContext());
        assertEquals(1, rescheduled.size());

        List<RequestInfo> inErrorRequests = executorService.getInErrorRequests(new QueryContext());
        assertEquals(0, inErrorRequests.size());
        List<RequestInfo> queuedRequests = executorService.getQueuedRequests(new QueryContext());
        assertEquals(1, queuedRequests.size());
        List<RequestInfo> executedRequests = executorService.getCompletedRequests(new QueryContext());
        assertEquals(1, executedRequests.size());

        executorService.cancelRequest(queuedRequests.get(0).getId());

        long firstExecution = executedRequests.get(0).getTime().getTime();
        long nextExecution = queuedRequests.get(0).getTime().getTime();

        // time difference between first and second should be less than 11 seconds (10s next interval, regardless of artifial slowdown by byteman)
        long diff = nextExecution - firstExecution;
        assertTrue(diff < 11000);
    }

    @Test(timeout=10000)
    @BMScript(value = "byteman-scripts/simulateSlowLogCleanupCommand.btm")
    public void logCleanupNextRunFixedIntervalTooSmallTest() throws InterruptedException {
        CountDownAsyncJobListener countDownListener = configureListener(1);
        // this delay will be invoked 3 times during the test, resulting in an overall delay of 2s
        System.setProperty("byteman.jpaaudit.sleep", "666");

        CommandContext ctxCMD = new CommandContext();
        ctxCMD.setData("businessKey", UUID.randomUUID().toString());
        ctxCMD.setData("NextRun", "1s");
        ctxCMD.setData("EmfName", "org.jbpm.executor");
        ctxCMD.setData("SkipProcessLog", "true");
        ctxCMD.setData("SkipTaskLog", "true");
        ctxCMD.setData("RepeatMode", "fixed");
        executorService.scheduleRequest("org.jbpm.executor.commands.LogCleanupCommand", ctxCMD);

        countDownListener.waitTillCompleted();

        List<RequestInfo> rescheduled = executorService.getRequestsByBusinessKey((String)ctxCMD.getData("businessKey"), Arrays.asList(STATUS.QUEUED), new QueryContext());
        // check if the job has been rescheduled
        assertEquals(1, rescheduled.size());

        List<RequestInfo> inErrorRequests = executorService.getInErrorRequests(new QueryContext());
        assertEquals(0, inErrorRequests.size());
        List<RequestInfo> queuedRequests = executorService.getQueuedRequests(new QueryContext());
        assertEquals(1, queuedRequests.size());
        List<RequestInfo> executedRequests = executorService.getCompletedRequests(new QueryContext());
        assertEquals(1, executedRequests.size());

        executorService.cancelRequest(queuedRequests.get(0).getId());

        long firstExecution = executedRequests.get(0).getTime().getTime();
        long nextExecution = queuedRequests.get(0).getTime().getTime();

        // time difference between first and second should be around 2 seconds (2nd job rescheduled immediately)
        long diff = nextExecution - firstExecution;
        assertTrue(diff >= 2000 && diff < 3000);
        System.clearProperty("byteman.jpaaudit.sleep");
    }

    @Test(timeout=10000)
    @BMScript(value = "byteman-scripts/simulateSlowLogCleanupCommand.btm")
    public void logCleanupNextRunFixedPaginationTest() throws InterruptedException {
        CountDownAsyncJobListener countDownListener = configureListener(1);
        // this delay will be invoked 3 times during the test, resulting in an overall delay of 2s
        System.setProperty("byteman.jpaaudit.sleep", "666");
        // number of records to be returned by JPAAuditLogService.doDelete
        System.setProperty("byteman.jpaaudit.delete", "1000");

        CommandContext ctxCMD = new CommandContext();
        ctxCMD.setData("businessKey", UUID.randomUUID().toString());
        ctxCMD.setData("NextRun", "10s");
        ctxCMD.setData("EmfName", "org.jbpm.executor");
        ctxCMD.setData("SkipProcessLog", "true");
        ctxCMD.setData("SkipTaskLog", "true");
        ctxCMD.setData("RepeatMode", "fixed");
        ctxCMD.setData("RecordsPerTransaction", 500);
        executorService.scheduleRequest("org.jbpm.executor.commands.LogCleanupCommand", ctxCMD);

        // waiting until the first LogCleanupCommand execution is done
        countDownListener.waitTillCompleted();
        System.clearProperty("byteman.jpaaudit.delete");
        // as the number of records to be deleted > RecordsPerTransaction, a second LogCleanupCommand will be scheduled for immediate execution
        countDownListener.reset(1);
        countDownListener.waitTillCompleted();

        List<RequestInfo> rescheduled = executorService.getRequestsByBusinessKey((String)ctxCMD.getData("businessKey"), Arrays.asList(STATUS.QUEUED), new QueryContext());
        // check if the job has been rescheduled
        assertEquals(1, rescheduled.size());

        List<RequestInfo> inErrorRequests = executorService.getInErrorRequests(new QueryContext());
        assertEquals(0, inErrorRequests.size());
        List<RequestInfo> queuedRequests = executorService.getQueuedRequests(new QueryContext());
        assertEquals(1, queuedRequests.size());
        List<RequestInfo> executedRequests = executorService.getCompletedRequests(new QueryContext());
        assertEquals(2, executedRequests.size());

        executorService.cancelRequest(queuedRequests.get(0).getId());

        long firstExecution = executedRequests.get(0).getTime().getTime();
        long lastExecution = queuedRequests.get(queuedRequests.size()-1).getTime().getTime();

        // time difference between first and last should be around 10 seconds, even if the original command got split into two executions due to RecordsPerTransaction
        long diff = lastExecution - firstExecution;
        assertTrue(diff < 11000);
        System.clearProperty("byteman.jpaaudit.sleep");
    }
}
