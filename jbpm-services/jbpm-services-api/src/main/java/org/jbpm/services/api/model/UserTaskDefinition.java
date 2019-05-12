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

import java.util.Collection;
import java.util.Map;

public interface UserTaskDefinition {

    /**
     * Returns the user task definition id.
     * @return id
     */
    String getId();

    /**
     * Returns the user task definition name.
     * @return name
     */
    String getName();

    /**
     * Returns the user task definition priority.
     * @return priority
     */
    Integer getPriority();

    /**
     * Returns the user task definition comment.
     * @return comment
     */
    String getComment();

    /**
     * Returns the user task created by.
     * @return created by
     */
    String getCreatedBy();

    /**
     * Returns the user task skippable.
     * @return skippable
     */
    boolean isSkippable();

    /**
     * Returns the user task definition form name.
     * @return form name
     */
    String getFormName();

    /**
     * Returns the user task definition associated entities.
     * @return entities
     */
    Collection<String> getAssociatedEntities();

    /**
     * Returns the user task definition task input mappings.
     * @return task input mappings
     */
    Map<String, String> getTaskInputMappings();

    /**
     * Returns the user task definition task output mappings.
     * @return task output mappings
     */
    Map<String, String> getTaskOutputMappings();
}
