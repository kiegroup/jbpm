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

package org.jbpm.test.persistence.scripts.quartzmockentities;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class QrtzPausedTriggersId implements Serializable{

    private static final long serialVersionUID = 1L;
    private String schedulerName;
    private String triggerGroup; 


    public String getSchedulerName() {
        return schedulerName;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public String getTriggerGroup() {
        return triggerGroup;
    }

    public void setTriggerGroup(String triggerGroup) {
        this.triggerGroup = triggerGroup;
    }

    public QrtzPausedTriggersId schedulerName(final String schedulerName) {
        this.schedulerName = schedulerName;
        return this;
    }

    public QrtzPausedTriggersId triggerGroup(final String triggerGroup) {
        this.triggerGroup = triggerGroup;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof QrtzPausedTriggersId)) {
            return false;
        }
        QrtzPausedTriggersId q = (QrtzPausedTriggersId) o;
        return new EqualsBuilder()
                  .append(schedulerName, q.schedulerName)
                  .append(triggerGroup, q.triggerGroup)
                  .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                   .append(schedulerName)
                   .append(triggerGroup)
                   .toHashCode();
    }
}
