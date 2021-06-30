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

package org.jbpm.casemgmt.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.casemgmt.api.AdvanceCaseRuntimeDataService;
import org.jbpm.kie.services.impl.AbstractAdvanceRuntimeDataServiceImpl;
import org.jbpm.services.api.query.model.QueryParam;
import org.kie.api.runtime.query.QueryContext;

import static org.jbpm.process.core.context.variable.VariableScope.CASE_FILE_PREFIX;
import static org.jbpm.workflow.core.WorkflowProcess.CASE_TYPE;

public class AdvanceCaseRuntimeDataServiceImpl extends AbstractAdvanceRuntimeDataServiceImpl implements AdvanceCaseRuntimeDataService {

    private final Map<String, String> translateTable = new HashMap<>();

    public AdvanceCaseRuntimeDataServiceImpl() {
        translateTable.put(CASE_ATTR_CORRELATION_KEY, "pil.correlationKey");
        translateTable.put(CASE_ATTR_DEFINITION_ID, "pil.processId");
        translateTable.put(CASE_ATTR_INSTANCE_ID, "pil.processInstanceId");
        translateTable.put(CASE_ATTR_DEPLOYMENT_ID, "pil.externalId");
        translateTable.put(TASK_ATTR_NAME, "task.name");
        translateTable.put(TASK_ATTR_OWNER, "task.actualOwner_id");
        translateTable.put(TASK_ATTR_STATUS, "task.status");
    }

    @Override
    public List<org.jbpm.services.api.model.ProcessInstanceWithVarsDesc> queryCaseByVariables(List<QueryParam> attributes,
                                                                                              List<QueryParam> caseVariables,
                                                                                              QueryContext queryContext) {
        return queryProcessByVariables(translate(translateTable, attributes), caseVariables, CASE_TYPE, CASE_FILE_PREFIX, queryContext);

    }

    @Override
    public List<org.jbpm.services.api.model.ProcessInstanceWithVarsDesc> queryCaseByVariablesAndTask(List<QueryParam> attributes,
                                                                                                     List<QueryParam> taskVariables,
                                                                                                     List<QueryParam> caseVariables,
                                                                                                     List<String> owners,
                                                                                                     QueryContext queryContext) {
        return queryProcessByVariablesAndTask(translate(translateTable, attributes), caseVariables, taskVariables, owners, CASE_TYPE, CASE_FILE_PREFIX, queryContext);
    }

    @Override
    public List<org.jbpm.services.api.model.ProcessInstanceWithVarsDesc> queryCaseByVariablesAndTask(List<QueryParam> attributes,
                                                                                                     List<QueryParam> taskVariables,
                                                                                                     List<QueryParam> caseVariables,
                                                                                                     QueryParam owners,
                                                                                                     QueryContext queryContext) {
        return queryProcessByVariablesAndTask(translate(translateTable, attributes), caseVariables, taskVariables, owners, CASE_TYPE, CASE_FILE_PREFIX, queryContext);
    }

    @Override
    public List<org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc> queryUserTasksByVariables(List<QueryParam> attributes,
                                                                                                        List<QueryParam> taskVariables,
                                                                                                        List<QueryParam> caseVariables,
                                                                                                        List<String> owners,
                                                                                                        QueryContext queryContext) {
        return queryUserTasksByVariables(translate(translateTable, attributes), caseVariables, taskVariables, owners, CASE_TYPE, CASE_FILE_PREFIX, queryContext);
    }

    @Override
    public List<org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc> queryUserTasksByVariables(List<QueryParam> attributes,
                                                                                                        List<QueryParam> taskVariables,
                                                                                                        List<QueryParam> caseVariables,
                                                                                                        QueryParam owners,
                                                                                                        QueryContext queryContext) {
        return queryUserTasksByVariables(translate(translateTable, attributes), caseVariables, taskVariables, owners, CASE_TYPE, CASE_FILE_PREFIX, queryContext);
    }
}
