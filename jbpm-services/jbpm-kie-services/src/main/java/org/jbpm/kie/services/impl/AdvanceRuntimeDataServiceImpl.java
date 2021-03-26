/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.kie.services.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.services.api.AdvanceRuntimeDataService;
import org.jbpm.services.api.model.ProcessInstanceWithVarsDesc;
import org.jbpm.services.api.query.model.QueryParam;
import org.kie.api.runtime.query.QueryContext;

import static org.jbpm.workflow.core.WorkflowProcess.PROCESS_TYPE;

public class AdvanceRuntimeDataServiceImpl extends AbstractAdvanceRuntimeDataServiceImpl implements AdvanceRuntimeDataService {

    private final Map<String, String> translateTable = new HashMap<>();

    public AdvanceRuntimeDataServiceImpl() {
        translateTable.put(PROCESS_ATTR_CORRELATION_KEY, "pil.correlationKey");
        translateTable.put(PROCESS_ATTR_DEFINITION_ID, "pil.processId");
        translateTable.put(PROCESS_ATTR_INSTANCE_ID, "pil.processInstanceId");
        translateTable.put(TASK_ATTR_NAME, "task.name");
        translateTable.put(TASK_ATTR_OWNER, "task.actualOwner_id");
        translateTable.put(TASK_ATTR_STATUS, "task.status");
        translateTable.put(PROCESS_ATTR_DEPLOYMENT_ID, "pil.externalId");
    }

    @Override
    public List<org.jbpm.services.api.model.ProcessInstanceWithVarsDesc> queryProcessByVariables(List<QueryParam> attributes,
                                                                                                 List<QueryParam> processVariables,
                                                                                                 QueryContext queryContext) {
        return queryProcessByVariables(translate(translateTable, attributes), processVariables, PROCESS_TYPE, "", queryContext);

    }

    @Override
    public List<ProcessInstanceWithVarsDesc> queryProcessByVariablesAndTask(List<QueryParam> attributes,
                                                                            List<QueryParam> processVariables,
                                                                            List<QueryParam> taskVariables,
                                                                            List<String> owners,
                                                                            QueryContext queryContext) {
        return queryProcessByVariablesAndTask(translate(translateTable, attributes), processVariables, taskVariables, owners, PROCESS_TYPE, "", queryContext);
    }

    @Override
    public List<ProcessInstanceWithVarsDesc> queryProcessByVariablesAndTask(List<QueryParam> attributes,
                                                                            List<QueryParam> processVariables,
                                                                            List<QueryParam> taskVariables,
                                                                            QueryParam owners,
                                                                            QueryContext queryContext) {
        return queryProcessByVariablesAndTask(translate(translateTable, attributes), processVariables, taskVariables, owners, PROCESS_TYPE, "", queryContext);
    }

    @Override
    public List<org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc> queryUserTasksByVariables(List<QueryParam> attributes,
                                                                                                        List<QueryParam> taskVariables,
                                                                                                        List<QueryParam> processVariables,
                                                                                                        List<String> owners,
                                                                                                        QueryContext queryContext) {

        return queryUserTasksByVariables(translate(translateTable, attributes), processVariables, taskVariables, owners, PROCESS_TYPE, "", queryContext);
    }

    @Override
    public List<org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc> queryUserTasksByVariables(List<QueryParam> attributes,
                                                                                                        List<QueryParam> taskVariables,
                                                                                                        List<QueryParam> processVariables,
                                                                                                        QueryParam owners,
                                                                                                        QueryContext queryContext) {

        return queryUserTasksByVariables(translate(translateTable, attributes), processVariables, taskVariables, owners, PROCESS_TYPE, "", queryContext);
    }


}
