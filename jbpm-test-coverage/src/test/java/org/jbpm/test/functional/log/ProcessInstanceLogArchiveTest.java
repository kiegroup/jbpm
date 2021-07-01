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

package org.jbpm.test.functional.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.services.task.audit.impl.model.TaskEventImpl;
import org.jbpm.test.JbpmTestCase;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.task.api.InternalTaskService;

/**
 * TODO: Add deleteLogsByDate, deleteLogsByDateRange (however it is a bit redundant).
 */
public class ProcessInstanceLogArchiveTest extends JbpmTestCase {

    private static final String HELLO_WORLD_PROCESS = "org/jbpm/test/functional/common/HumanTask.bpmn2";
    private static final String HELLO_WORLD_PROCESS_ID = "org.jbpm.test.functional.common.HumanTask";


    private JPAAuditLogService auditService;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        auditService = new JPAAuditLogService(getEmf());
        auditService.clear();
    }

    @Override
    public void tearDown() throws Exception {
        try {
            auditService.clear();
            auditService.dispose();
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void checkEndDateArchived() {
        KieSession kieSession = createKSession(HELLO_WORLD_PROCESS);

        Map<String, Object> params = new HashMap<>();
        params.put("var", params);
        startProcess(kieSession, HELLO_WORLD_PROCESS_ID, 2, params);

        InternalTaskService taskService = (InternalTaskService) getRuntimeEngine().getTaskService();
        taskService.getTasksAssignedAsPotentialOwnerByStatusByGroup("john", null, null).forEach(e -> {
            taskService.start(e.getId(), "john");
            taskService.complete(e.getId(), "john", Collections.emptyMap());
        });

        EntityManager em = getEmf().createEntityManager();

        em.unwrap(Session.class).enableFilter("jBPMEntityFilter");
        List<ProcessInstanceLog> pLogs = em.createQuery("SELECT o FROM ProcessInstanceLog o", ProcessInstanceLog.class).getResultList();
        Assert.assertTrue(!pLogs.isEmpty());
        for(ProcessInstanceLog log : pLogs) {
            Assert.assertNotNull(log.getEnd());
        }
        List<Long> ids = pLogs.stream().map(ProcessInstanceLog::getId).collect(Collectors.toList());

        List<NodeInstanceLog> nLogs = em.createQuery("SELECT o FROM NodeInstanceLog o", NodeInstanceLog.class).getResultList();
        Assert.assertTrue(!nLogs.isEmpty());
        for(NodeInstanceLog log : nLogs) {
            Assert.assertNotNull(log.getEnd());
        }

        List<VariableInstanceLog> vLogs = em.createQuery("SELECT o FROM VariableInstanceLog o", VariableInstanceLog.class).getResultList();
        Assert.assertTrue(!vLogs.isEmpty());
        for(VariableInstanceLog log : vLogs) {
            Assert.assertNotNull(log.getEnd());
        }

        List<TaskEventImpl> tLogs = em.createQuery("SELECT o FROM TaskEventImpl o WHERE o.processInstanceId IN (:var)", TaskEventImpl.class)
                .setParameter("var", ids)
                .getResultList();
        Assert.assertTrue(!tLogs.isEmpty());
        for(TaskEventImpl log : tLogs) {
            Assert.assertNotNull(log.getEnd());
        }
        em.close();
    }

    private List<ProcessInstance> startProcess(KieSession kieSession, String processId, int count, Map<String, Object> var) {
        List<ProcessInstance> piList = new ArrayList<ProcessInstance>();
        for (int i = 0; i < count; i++) {
            ProcessInstance pi = kieSession.startProcess(processId, var);
            if (pi != null) {
                piList.add(pi);
            }
        }
        return piList;
    }

}
