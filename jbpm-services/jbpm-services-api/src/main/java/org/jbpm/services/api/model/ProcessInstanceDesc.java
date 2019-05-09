/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.services.api.model;

import java.util.Date;
import java.util.List;

public interface ProcessInstanceDesc {

    /**
     * Returns the process instance description process id.
     * @return process id
     */
	String getProcessId();

    /**
     * Returns the process instance description id.
     * @return id
     */
    Long getId();

    /**
     * Returns the process instance description process name.
     * @return process name
     */
    String getProcessName();

    /**
     * Returns the process instance description state.
     * @return state
     */
    Integer getState();

    /**
     * Returns the process instance description deployment id.
     * @return deployment ids
     */
    String getDeploymentId();

    /**
     * Returns the process instance description timestamp.
     * @return timestamp
     */
    Date getDataTimeStamp();

    /**
     * Returns the process instance description process version.
     * @return process version
     */
    String getProcessVersion();

    /**
     * Returns the process instance description initiator.
     * @return initiator
     */
    String getInitiator();

    /**
     * Returns the process instance description.
     * @return description
     */
    String getProcessInstanceDescription();

    /**
     * Returns the process instance description correlation key.
     * @return correlation key
     */
    String getCorrelationKey();

    /**
     * Returns the process instance description parent id.
     * @return parent id
     */
    Long getParentId();

    /**
     * Returns the process instance description SLA due date.
     * @return SLA due date
     */
    Date getSlaDueDate();

    /**
     * Returns the process instance description SLA compliance.
     * @return SLA compliance
     */
    Integer getSlaCompliance();

    /**
     * Returns the process instance description active tasks.
     * @return @{@link UserTaskInstanceDesc} active tasks
     */
    List<UserTaskInstanceDesc> getActiveTasks();
}
