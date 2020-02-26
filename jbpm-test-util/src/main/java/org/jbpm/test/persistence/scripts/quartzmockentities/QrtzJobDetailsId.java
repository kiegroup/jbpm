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

public class QrtzJobDetailsId implements Serializable {

    private static final long serialVersionUID = 1L;
    private String schedulerName;
    private String jobName;
    private String jobGroup;

    public String getSchedulerName() {
        return schedulerName;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobGroup() {
        return jobGroup;
    }

    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }

    public QrtzJobDetailsId schedulerName(final String schedulerName) {
        this.schedulerName = schedulerName;
        return this;
    }

    public QrtzJobDetailsId jobName(final String jobName) {
        this.jobName = jobName;
        return this;
    }

    public QrtzJobDetailsId jobGroup(final String jobGroup) {
        this.jobGroup = jobGroup;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof QrtzJobDetailsId)) {
            return false;
        }
        QrtzJobDetailsId q = (QrtzJobDetailsId) o;
        return new EqualsBuilder()
                  .append(schedulerName, q.schedulerName)
                  .append(jobName, q.jobName)
                  .append(jobGroup, q.jobGroup)
                  .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                   .append(schedulerName)
                   .append(jobName)
                   .append(jobGroup)
                   .toHashCode();
    }
}
