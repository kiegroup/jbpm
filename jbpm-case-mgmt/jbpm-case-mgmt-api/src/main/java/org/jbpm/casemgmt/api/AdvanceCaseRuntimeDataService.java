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

package org.jbpm.casemgmt.api;

import java.util.List;

import org.jbpm.services.api.model.ProcessInstanceWithVarsDesc;
import org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc;
import org.jbpm.services.api.query.model.QueryParam;
import org.kie.api.runtime.query.QueryContext;

public interface AdvanceCaseRuntimeDataService {

    String TASK_ATTR_NAME = "TASK_NAME";
    String TASK_ATTR_OWNER = "TASK_OWNER";
    String TASK_ATTR_STATUS = "TASK_STATUS";
    String CASE_ATTR_INSTANCE_ID = "PROCESS_INSTANCE_ID";
    String CASE_ATTR_CORRELATION_KEY = "CASE_CORRELATION_KEY";
    String CASE_ATTR_DEFINITION_ID = "CASE_DEFINITION_ID";
    String CASE_ATTR_DEPLOYMENT_ID = "CASE_DEPLOYMENT_ID";

    // to work with query param exclude
    String CASE_COLLECTION_VARIABLES = "ATTR_COLLECTION_VARIABLES";

    List<ProcessInstanceWithVarsDesc> queryCaseByVariables(List<QueryParam> attributes,
                                                           List<QueryParam> caseVariables,
                                                           QueryContext queryContext);

    List<ProcessInstanceWithVarsDesc> queryCaseByVariablesAndTask(List<QueryParam> attributes,
                                                                  List<QueryParam> taskVariables,
                                                                  List<QueryParam> caseVariables,
                                                                  List<String> potentialOwners,
                                                                  QueryContext queryContext);

    List<ProcessInstanceWithVarsDesc> queryCaseByVariablesAndTask(List<QueryParam> attributes,
                                                                  List<QueryParam> taskVariables,
                                                                  List<QueryParam> caseVariables,
                                                                  QueryParam potentialOwners,
                                                                  QueryContext queryContext);

    List<UserTaskInstanceWithPotOwnerDesc> queryUserTasksByVariables(List<QueryParam> attributes,
                                                                     List<QueryParam> taskVariables,
                                                                     List<QueryParam> caseVariables,
                                                                     List<String> potentialOwners,
                                                                     QueryContext queryContext);

    List<UserTaskInstanceWithPotOwnerDesc> queryUserTasksByVariables(List<QueryParam> attributes,
                                                                     List<QueryParam> taskVariables,
                                                                     List<QueryParam> caseVariables,
                                                                     QueryParam potentialOwners,
                                                                     QueryContext queryContext);
}
