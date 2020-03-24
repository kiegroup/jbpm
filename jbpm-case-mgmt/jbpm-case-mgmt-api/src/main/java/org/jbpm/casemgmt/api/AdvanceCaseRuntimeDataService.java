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
    String CASE_ATTR_INSTANCE_ID = "PROCESS_INSTANCE_ID";
    String CASE_ATTR_CORRELATION_KEY = "PROCESS_CORRELATION_KEY";
    String CASE_ATTR_DEFINITION_ID = "PROCESS_DEFINITION_ID";
    String CASE_ATTR_DEPLOYMENT_ID = "PROCESS_DEPLOYMENT_ID";

    List<ProcessInstanceWithVarsDesc> queryCaseByVariables(List<QueryParam> attributes,
                                                           List<QueryParam> variables,
                                                           QueryContext queryContext);

    List<UserTaskInstanceWithPotOwnerDesc> queryUserTasksByVariables(List<QueryParam> attributes,
                                                                     List<QueryParam> variables,
                                                                     List<QueryParam> processVariables,
                                                                     List<String> owners,
                                                                     QueryContext queryContext);

}
