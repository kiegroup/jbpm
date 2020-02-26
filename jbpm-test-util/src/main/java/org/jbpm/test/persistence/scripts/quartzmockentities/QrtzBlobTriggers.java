/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.test.persistence.scripts.quartzmockentities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity(name = "QRTZ_BLOB_TRIGGERS")
public class QrtzBlobTriggers {

    @Id
    @Column(name = "SCHED_NAME")
    private String schedulerName;

    @Column(name = "TRIGGER_NAME")
    private String triggerName;

    @Column(name = "TRIGGER_GROUP")
    private String triggerGroup;

    @Lob
    @Column(name = "BLOB_DATA")
    private byte[] blobData;
}
