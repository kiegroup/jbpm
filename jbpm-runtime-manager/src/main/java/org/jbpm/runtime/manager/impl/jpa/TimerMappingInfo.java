/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.runtime.manager.impl.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


@Entity
@Table(name = "TimerMappingInfo", indexes = {
        @Index(name = "IDX_TMI_KSessionUUID", unique = true, columnList = "kieSessionId,uuid")
})
@SequenceGenerator(name="timerMappingInfoIdSeq", sequenceName="TIMER_MAPPING_INFO_ID_SEQ")
public class TimerMappingInfo implements Serializable {

    private static final long serialVersionUID = 533985957655465840L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator="timerMappingInfoIdSeq")
    private Long id;

    private long timerId;

    private String externalTimerId;

    private long kieSessionId;

    @Column(nullable = false)
    private String uuid;

    public TimerMappingInfo() {

    }

    public TimerMappingInfo(long timerId, String externalTimerId, long kieSessionId, String uuid) {
        this.timerId = timerId;
        this.externalTimerId = externalTimerId;
        this.kieSessionId = kieSessionId;
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalTimerId() {
        return externalTimerId;
    }

    public void setExternalTimerId(String externalTimerId) {
        this.externalTimerId = externalTimerId;
    }

    public long getKieSessionId() {
        return kieSessionId;
    }

    public void setKieSessionId(long kieSessionId) {
        this.kieSessionId = kieSessionId;
    }

    public long getTimerId() {
        return timerId;
    }

    public void setTimerId(long timerId) {
        this.timerId = timerId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((externalTimerId == null) ? 0 : externalTimerId.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (int) (kieSessionId ^ (kieSessionId >>> 32));
        result = prime * result + (int) (timerId ^ (timerId >>> 32));
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TimerMappingInfo other = (TimerMappingInfo) obj;
        if (externalTimerId == null) {
            if (other.externalTimerId != null)
                return false;
        } else if (!externalTimerId.equals(other.externalTimerId))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (kieSessionId != other.kieSessionId)
            return false;
        if (timerId != other.timerId)
            return false;
        if (uuid == null) {
            if (other.uuid != null)
                return false;
        } else if (!uuid.equals(other.uuid))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "TimerMappingInfo [id=" + id + ", timerId=" + timerId + ", externalTimerId=" + externalTimerId + ", kieSessionId=" + kieSessionId + ", uuid=" + uuid + "]";
    }

}
