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

package org.jbpm.test.container.test.ejbservices.tx;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.jbpm.services.api.model.NodeInstanceDesc;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.test.container.AbstractRuntimeEJBServicesTest;
import org.jbpm.test.container.groups.EAP;
import org.jbpm.test.container.groups.WAS;
import org.jbpm.test.container.groups.WLS;
import org.jbpm.test.container.listeners.TrackingAgendaEventListener;
import org.jbpm.test.listener.process.DefaultCountDownProcessEventListener;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.query.QueryContext;
import org.kie.internal.query.QueryFilter;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category({EAP.class, WLS.class, WAS.class})
public class ETransactionTest extends AbstractRuntimeEJBServicesTest {

    private static final String USER_TRANSACTION_NAME = "java:comp/UserTransaction";
    private static final Logger LOGGER = LoggerFactory.getLogger(ETransactionTest.class);
    private static final String PROCESS_ID = "transactions";

    @Before
    @Override
    public void deployKieJar() {
        if (kieJar == null) {
            kieJar = archive.deployTransactionKieJar().getIdentifier();
        }
    }

    @Test
    public void testStartProcessCommit() throws Exception {
        UserTransaction ut = InitialContext.doLookup(USER_TRANSACTION_NAME);

        ut.begin();

        Long processInstanceId = null;
        ProcessInstanceDesc processDesc = null;
        List<NodeInstanceDesc> processInstanceHistory = null;

        try {
            processInstanceId = startProcessInstance(PROCESS_ID);

            checkProcessInstanceIsActive(processInstanceId);
        } catch (Exception e) {
            ut.rollback();
            throw e;
        }
        ut.commit();

        checkProcessInstanceIsActive(processInstanceId);
    }

    @Test
    public void testStartProcessRollback() throws Exception {
        UserTransaction ut = InitialContext.doLookup(USER_TRANSACTION_NAME);
        ut.begin();

        Long processInstanceId = null;
        ProcessInstanceDesc processDesc = null;
        List<NodeInstanceDesc> processInstanceHistory = null;

        try {
            processInstanceId = startProcessInstance(PROCESS_ID);

            checkProcessInstanceIsActive(processInstanceId);

        } catch (Exception e) {
            ut.rollback();
            throw e;
        }
        ut.rollback();

        try {
            processDesc = runtimeDataService.getProcessInstanceById(processInstanceId);
            assertThat(processDesc).isNull();
            processInstanceHistory = getProcessInstanceHistory(processInstanceId);
            assertThat(processInstanceHistory).isNullOrEmpty();
        } catch (NullPointerException npe) {
            LOGGER.error("Non-XA database thrown NPE on process started before rollback", npe);
        }
    }

    @Test
    public void testStartProcessWithExceptionRollback() throws Throwable {
 
        Long processInstanceId = startProcessInstance(SCRIPT_THROW_EXCEPTION_TASK_PROCESS_ID);

        checkProcessInstanceIsActive(processInstanceId);
        // the root cause is not instance of
        assertThat(catchRootCause(() -> { startAndCompleteHumanTask(processInstanceId); })).isNotInstanceOf(NullPointerException.class);
    }



    @Test
    public void testAbortProcessCommit() throws Exception {
        Long processInstanceId = startProcessInstance(PROCESS_ID);

        checkProcessInstanceIsActive(processInstanceId);

        UserTransaction ut = InitialContext.doLookup(USER_TRANSACTION_NAME);
        ut.begin();

        try {
            processService.abortProcessInstance(processInstanceId);
        } catch (Exception e) {
            ut.rollback();
            throw e;
        }

        ut.commit();

        List<Integer> states = new ArrayList<Integer>();
        states.add(ProcessInstance.STATE_ABORTED);
        Collection<ProcessInstanceDesc> processInstances = runtimeDataService.getProcessInstances(states, null,
                new QueryContext());

        assertThat(processInstances).isNotNull().hasSize(1);
        assertThat(processInstances.iterator().next().getId()).isEqualTo(processInstanceId);
    }

    @Test
    public void testAbortProcessRollback() throws Exception {
        Long processInstanceId = startProcessInstance(PROCESS_ID);

        checkProcessInstanceIsActive(processInstanceId);

        UserTransaction ut = InitialContext.doLookup(USER_TRANSACTION_NAME);
        ut.begin();

        try {
            processService.abortProcessInstance(processInstanceId);
        } catch (Exception e) {
            ut.rollback();
            throw e;
        }
        ut.rollback();

        ProcessInstance processInstance = processService.getProcessInstance(processInstanceId);
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getId()).isEqualTo(processInstanceId);
        assertThat(processInstance.getState()).isEqualTo(ProcessInstance.STATE_ACTIVE);
    }

    @Test
    public void testScript() throws Exception {
        Long processInstanceId = startProcessInstance(PROCESS_ID);

        UserTransaction ut = InitialContext.doLookup(USER_TRANSACTION_NAME);
        ut.begin();

        try {
            processService.signalProcessInstance(processInstanceId, "start", "script");
            assertThat(hasNodeLeft(processInstanceId, "script")).isTrue();

        } catch (Exception e) {
            ut.rollback();
            throw e;
        }
        ut.rollback();

        ut = InitialContext.doLookup(USER_TRANSACTION_NAME);
        ut.begin();

        try {
            processService.signalProcessInstance(processInstanceId, "start", "script");
            assertThat(ut.getStatus()).isEqualTo(Status.STATUS_ACTIVE);

        } catch (Exception e) {
            ut.rollback();
            throw e;
        }
        ut.commit();

        assertThat(hasNodeLeft(processInstanceId, "script")).isTrue();
        processService.signalProcessInstance(processInstanceId, "finish", null);
        assertThat(hasProcessInstanceCompleted(processInstanceId)).isTrue();
    }

    @Test
    public void testRuleFlowGroup() throws Exception {
        TrackingAgendaEventListener agenda = new TrackingAgendaEventListener();
        RuntimeManager manager = deploymentService.getRuntimeManager(kieJar);
        RuntimeEngine engine = manager.getRuntimeEngine(ProcessInstanceIdContext.get());
        engine.getKieSession().addEventListener(agenda);

        Long processInstanceId = startProcessInstance(PROCESS_ID);

        UserTransaction ut = InitialContext.doLookup(USER_TRANSACTION_NAME);
        ut.begin();

        processService.signalProcessInstance(processInstanceId, "start", "rfg");
        assertThat(hasNodeLeft(processInstanceId, "rfg")).isTrue();

        ut.rollback();
        agenda.clear();

        processService.execute(kieJar, new FireAllRulesCommand());
        assertThat(agenda.isRuleFired("dummyRule")).isFalse();
        agenda.clear();

        ut = InitialContext.doLookup(USER_TRANSACTION_NAME);
        ut.begin();

        processService.signalProcessInstance(processInstanceId, "start", "rfg");

        ut.commit();

        assertThat(hasNodeLeft(processInstanceId, "rfg")).isTrue();
        processService.execute(kieJar, new FireAllRulesCommand());
        processService.signalProcessInstance(processInstanceId, "finish", null);

        assertThat(agenda.isRuleFired("dummyRule")).isTrue();
        assertThat(hasProcessInstanceCompleted(processInstanceId)).isTrue();
    }

    @Test
    public void testTimer() throws Exception {
        DefaultCountDownProcessEventListener listener = new DefaultCountDownProcessEventListener(0) {
            @Override
            public void afterNodeLeft(ProcessNodeLeftEvent event) {
                if ("Timer".equals(event.getNodeInstance().getNodeName())) {
                    countDown();
                }
            }
        };
        RuntimeManager manager = deploymentService.getRuntimeManager(kieJar);
        RuntimeEngine engine = manager.getRuntimeEngine(ProcessInstanceIdContext.get());
        engine.getKieSession().addEventListener(listener);

        Long processInstanceId = startProcessInstance(PROCESS_ID);

        UserTransaction ut = InitialContext.doLookup(USER_TRANSACTION_NAME);
        ut.begin();

        try {
            processService.signalProcessInstance(processInstanceId, "start", "timer");
            assertThat(hasNodeLeft(processInstanceId, "timer")).isTrue();
            assertThat(hasNodeLeft(processInstanceId, "Timer")).isFalse();
        } catch (Exception e) {
            ut.rollback();
            throw e;
        }

        ut.rollback();

        assertThat(hasNodeLeft(processInstanceId, "timer")).isFalse();
        Thread.sleep(2000);
        assertThat(hasNodeLeft(processInstanceId, "Timer")).isFalse();

        ut = InitialContext.doLookup(USER_TRANSACTION_NAME);
        ut.begin();
        listener.reset(1);
        try {
            processService.signalProcessInstance(processInstanceId, "start", "timer");
        } catch (Exception e) {
            ut.rollback();
            throw e;
        }

        ut.commit();

        listener.waitTillCompleted();

        assertThat(hasNodeLeft(processInstanceId, "timer")).isTrue();
        assertThat(hasNodeLeft(processInstanceId, "Timer")).isTrue();
        processService.signalProcessInstance(processInstanceId, "finish", null);
        assertThat(hasProcessInstanceCompleted(processInstanceId)).isTrue();
    }

    @Test
    public void testHumanTask() throws Exception {
        Long processInstanceId = startProcessInstance(PROCESS_ID);

        UserTransaction ut = InitialContext.doLookup(USER_TRANSACTION_NAME);

        List<TaskSummary> taskSummaries = null;
        ut.begin();
        try {
            processService.signalProcessInstance(processInstanceId, "start", "usertask");
        } catch (Exception e) {
            ut.rollback();
            throw e;
        }
        ut.rollback();

        taskSummaries = runtimeDataService.getTasksAssignedAsPotentialOwner("john", new QueryFilter());
        assertThat(taskSummaries).isNotNull().hasSize(0);
        assertThat(hasNodeLeft(processInstanceId, "User Task")).isFalse();

        ut.begin();
        try {
            processService.signalProcessInstance(processInstanceId, "start", "usertask");
        } catch (Exception e) {
            ut.rollback();
            throw e;
        }
        ut.commit();

        ut.begin();
        try {
            taskSummaries = startAndCompleteHumanTask(processInstanceId);
        } catch (Exception e) {
            ut.rollback();
            throw e;
        }
        ut.rollback();

        assertThat(runtimeDataService.getTaskById(taskSummaries.get(0).getId())).isNotNull();
        taskSummaries = runtimeDataService.getTasksAssignedAsPotentialOwner("john", new QueryFilter());
        assertThat(taskSummaries).isNotNull().hasSize(1);
        assertThat(taskSummaries.get(0).getStatus()).isEqualTo(org.kie.api.task.model.Status.Reserved);
        assertThat(hasNodeLeft(processInstanceId, "User Task")).isFalse();

        ut.begin();
        try {
            taskSummaries = startAndCompleteHumanTask(processInstanceId);
        } catch (Exception e) {
            ut.rollback();
            throw e;
        }
        ut.commit();

        assertThat(hasNodeLeft(processInstanceId, "User Task")).isTrue();
        assertThat(hasTaskCompleted(taskSummaries.get(0).getId()));
        assertThat(hasProcessInstanceCompleted(processInstanceId)).isFalse();

    }

    @Test
    public void testForLoop() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("collection", Arrays.asList("hello world", "25", "false", "1234567891011121314151617181920", ""));
        Long processInstanceId = startProcessInstance(PROCESS_ID, params);

        UserTransaction ut = InitialContext.doLookup(USER_TRANSACTION_NAME);
        ut.begin();

        try {
            processService.signalProcessInstance(processInstanceId, "start", "forloop");
            assertThat(hasNodeLeft(processInstanceId, "forloop")).isTrue();
        } catch (Exception e) {
            ut.rollback();
            throw e;
        }
        ut.rollback();
        ut = InitialContext.doLookup(USER_TRANSACTION_NAME);
        ut.begin();

        try {
            processService.signalProcessInstance(processInstanceId, "start", "forloop");
        } catch (Exception e) {
            ut.rollback();
            throw e;
        }
        ut.commit();

        assertThat(hasNodeLeft(processInstanceId, "forloop")).isTrue();
        assertThat(hasNodeLeft(processInstanceId, "Multiple Instances")).isTrue();
        assertThat(hasProcessInstanceCompleted(processInstanceId)).isFalse();
    }

    @Test
    public void testEmbedded() throws Exception {
        Long processInstanceId = startProcessInstance(PROCESS_ID);

        UserTransaction ut = InitialContext.doLookup(USER_TRANSACTION_NAME);
        ut.begin();

        try {
            processService.signalProcessInstance(processInstanceId, "start", "embedded");
            assertThat(hasNodeLeft(processInstanceId, "embedded")).isTrue();

        } catch (Exception e) {
            ut.rollback();
            throw e;
        }
        ut.rollback();
        ut = InitialContext.doLookup(USER_TRANSACTION_NAME);
        ut.begin();

        try {
            processService.signalProcessInstance(processInstanceId, "start", "embedded");

        } catch (Exception e) {
            ut.rollback();
            throw e;
        }
        ut.commit();

        assertThat(hasNodeLeft(processInstanceId, "embedded")).isTrue();
        assertThat(hasProcessInstanceCompleted(processInstanceId)).isFalse();
    }

    private void checkProcessInstanceIsActive(Long processInstanceId) {
        ProcessInstanceDesc processDesc = runtimeDataService.getProcessInstanceById(processInstanceId);
        assertThat(processDesc).isNotNull();
        assertThat(processDesc.getState()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        List<NodeInstanceDesc> processInstanceHistory = getProcessInstanceHistory(processInstanceId);
        assertThat(processInstanceHistory).isNotNull().isNotEmpty();
    }

    private List<TaskSummary> startAndCompleteHumanTask(Long processInstanceId) {
        List<TaskSummary> taskSummaries = runtimeDataService.getTasksAssignedAsPotentialOwner("john", new QueryFilter());
        assertThat(taskSummaries).isNotNull().hasSize(1);
        long taskId = taskSummaries.get(0).getId();
        userTaskService.start(taskId, "john");
        userTaskService.complete(taskId, "john", new HashMap<String, Object>());
        assertThat(hasNodeLeft(processInstanceId, "User Task")).isTrue();
        assertThat(hasTaskCompleted(taskId));

        return taskSummaries;
    }

}
