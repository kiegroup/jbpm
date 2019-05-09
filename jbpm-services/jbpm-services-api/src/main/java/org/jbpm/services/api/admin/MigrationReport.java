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

package org.jbpm.services.api.admin;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * MigrationReport that provides details
 * about performed steps during process instance migration.
 */
public interface MigrationReport extends Serializable {

    /**
     * Indicates if the migration was successful or not
     * @return migration success
     */
    boolean isSuccessful();

    /**
     * Timestamp representing start time of the migration
     * @return migration start time
     */
    Date getStartDate();

    /**
     * Timestamp representing end time of the migration
     * @return migration end time
     */
    Date getEndDate();
    
    /**
     * Returns process instance id that was migrated
     * @return process instance id
     */
    Long getProcessInstanceId();

    /**
     * Returns complete list of migration entries (steps performed) with details about them
     * @return migration entries
     */
    List<MigrationEntry> getEntries();
}
