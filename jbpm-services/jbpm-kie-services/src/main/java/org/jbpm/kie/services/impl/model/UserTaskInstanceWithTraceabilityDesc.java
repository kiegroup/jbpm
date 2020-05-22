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

package org.jbpm.kie.services.impl.model;

import java.io.Serializable;
import java.util.Date;

public class UserTaskInstanceWithTraceabilityDesc extends UserTaskInstanceDesc implements org.jbpm.services.api.model.UserTaskInstanceWithTraceabilityDesc, Serializable {

    private static final long serialVersionUID = 3917945169217694952L;

    private String correlationKey;
    private Integer processType;


    public UserTaskInstanceWithTraceabilityDesc(Long taskId, String status,
                                                Date activationTime, String name, String description,
                                                Integer priority, String actualOwner, String createdBy,
                                                String deploymentId, String processId, Long processInstanceId,
                                                Date createdOn, Date dueDate, String correlationKey, Integer processType, Long workItemId, String formName) {
        super(taskId, status, activationTime, name, description, priority, actualOwner,
              createdBy, deploymentId, processId, processInstanceId, createdOn, dueDate, workItemId, formName);
        this.processType = processType;
        this.correlationKey = correlationKey;
    }

    @Override
    public String getCorrelationKey() {
        return correlationKey;
    }

    @Override
    public void setCorrelationKey(String correlationKey) {
        this.correlationKey = correlationKey;
    }

    @Override
    public Integer getProcessType() {
        return processType;
    }

    @Override
    public void setProcessType(Integer processType) {
        this.processType = processType;
    }
}
