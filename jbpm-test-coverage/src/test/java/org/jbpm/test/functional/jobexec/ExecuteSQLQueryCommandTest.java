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

package org.jbpm.test.functional.jobexec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.jbpm.executor.commands.ExecuteSQLQueryCommand;
import org.jbpm.executor.impl.ExecutorServiceImpl;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.jbpm.test.JbpmAsyncJobTestCase;
import org.jbpm.test.listener.CountDownAsyncJobListener;
import org.junit.Test;
import org.kie.api.executor.ExecutionResults;
import org.kie.api.executor.RequestInfo;
import org.kie.api.runtime.KieSession;
import org.kie.internal.executor.api.CommandContext;
import org.kie.internal.query.QueryContext;

import static org.assertj.core.api.Assertions.assertThat;

public class ExecuteSQLQueryCommandTest extends JbpmAsyncJobTestCase {

    private static final String HELLO_WORLD = "org/jbpm/test/functional/common/HelloWorldProcess1.bpmn";
    private static final String HELLO_WORLD_ID = "org.jbpm.test.functional.common.HelloWorldProcess1";
    private static final String PU_NAME = "org.jbpm.persistence.complete";

    private static final String SQL = "select * from ProcessInstanceLog plog where plog.processId=:processId";
    private EntityManagerFactory emf = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        emf = EntityManagerFactoryManager.get().getOrCreate(PU_NAME);
    }

    @Override
    public void tearDown() throws Exception {
        try {
            if (emf != null) {
                emf.close();
            }
        } finally {
            super.tearDown();
        }
    }

    @Test(timeout=10000)
    public void testScheduleSQLQueryCommand() {
        CountDownAsyncJobListener countDownListener = new CountDownAsyncJobListener(1);
        ((ExecutorServiceImpl) getExecutorService()).addAsyncJobListener(countDownListener);
        // Generate data
        KieSession kieSession = createKSession(HELLO_WORLD);
        startProcess(kieSession, HELLO_WORLD_ID, 1);

        // Schedule SQL query command job
        Map<String, Object> params = new HashMap<>();
        params.put("processId", HELLO_WORLD_ID);
        CommandContext commandContext = setSQLQueryCommand(SQL, params);
        getExecutorService().scheduleRequest(ExecuteSQLQueryCommand.class.getName(), commandContext);
        countDownListener.waitTillCompleted();

        List<RequestInfo> requests = getExecutorService().getAllRequests(new QueryContext());
        assertThat(requests).isNotNull();
        assertThat(requests.size()).isEqualTo(1);
        assertThat(requests.get(0).getCommandName()).isEqualTo(ExecuteSQLQueryCommand.class.getName());
    }

    @Test(timeout=10000)
    public void testSQLQueryCommandResults() throws Exception {
        // Generate data
        KieSession kieSession = createKSession(HELLO_WORLD);
        startProcess(kieSession, HELLO_WORLD_ID, 1);

        Query query = emf.createEntityManager().createNativeQuery(SQL);
        query.setParameter("processId", HELLO_WORLD_ID);
        assertThat(query.getResultList()).isNotNull();
        assertThat(query.getResultList().size()).isEqualTo(1);

        ExecuteSQLQueryCommand command = new ExecuteSQLQueryCommand();
        Map<String, Object> params = new HashMap<>();
        params.put("processId", HELLO_WORLD_ID);
        ExecutionResults results = command.execute(setSQLQueryCommand(SQL, params));

        assertThat(results.getData("data")).isNotNull();
        assertThat(results.getData("size")).isEqualTo(1);
    }

    private CommandContext setSQLQueryCommand(String sql, Map<String, Object> parameters) {
        CommandContext commandContext = new CommandContext();
        commandContext.setData("EmfName", PU_NAME);
        commandContext.setData("SQL", sql);
        StringBuilder sb = new StringBuilder();

        parameters.forEach((key, value) -> {
            sb.append(key).append(",");
            commandContext.setData(key, value);
        });
        commandContext.setData("ParametersList", sb.toString());
        commandContext.setData("SingleRun", "true");
        return commandContext;
    }
}
