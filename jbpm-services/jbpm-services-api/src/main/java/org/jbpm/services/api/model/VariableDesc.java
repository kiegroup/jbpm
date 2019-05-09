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

public interface VariableDesc {

    /**
     * Returns the variable id.
     * @return id
     */
    String getVariableId();

    /**
     * Returns the variable instance id.
     * @return instance id
     */
    String getVariableInstanceId();

    /**
     * Returns the variable old value.
     * @return old value
     */
    String getOldValue();

    /**
     * Returns the variable new value.
     * @return new value
     */
    String getNewValue();

    /**
     * Returns the deployment id.
     * @return deployment id
     */
    String getDeploymentId();

    /**
     * Returns the process instance id.
     * @return process instance id
     */
    Long getProcessInstanceId();

    /**
     * Returns the variable data timestamp.
     * @return data time stamp
     */
    Date getDataTimeStamp();
}
