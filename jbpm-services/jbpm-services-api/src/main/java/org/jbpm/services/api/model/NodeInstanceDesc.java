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

public interface NodeInstanceDesc {

    /**
     * Returns the node instance id.
     * @return id
     */
	Long getId();

    /**
     * Returns the node instance name.
     * @return name
     */
    String getName();

    /**
     * Returns the node instance deployment id.
     * @return deployment id
     */
    String getDeploymentId();

    /**
     * Returns the process instance id.
     * @return process instance id
     */
    Long getProcessInstanceId();

    /**
     * Returns the node instance timestamp.
     * @return timestamp
     */
    Date getDataTimeStamp();

    /**
     * Returns the node instance node type.
     * @return node type
     */
    String getNodeType();

    /**
     * Returns the node instance node id.
     * @return node id
     */
    String getNodeId();

    /**
     * Returns the node instance completion.
     * @return completion
     */
    boolean isCompleted();

    /**
     * Returns the node instance connection.
     * @return connection
     */
    String getConnection();

    /**
     * Returns the node instance workitem id.
     * @return workitem id
     */
    Long getWorkItemId();

    /**
     * Returns the node instance reference id.
     * @return reference id
     */
    Long getReferenceId();

    /**
     * Returns the node instance container id.
     * @return container id
     */
    String getNodeContainerId();

    /**
     * Returns the node instance SLA due date.
     * @return SLA due date
     */
    Date getSlaDueDate();

    /**
     * Returns the node instance SLA complience.
     * @return SLA compliance
     */
    Integer getSlaCompliance();
}
