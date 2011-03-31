/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.process.audit;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class NodeInstanceLog implements Serializable {
    
	public static final int TYPE_ENTER = 0;
	public static final int TYPE_EXIT = 1;
	
	private static final long serialVersionUID = 510l;
	
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
    private Integer type;
    private Long processInstanceId;
    private String processId;
    private String nodeInstanceId;
    private String nodeId;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "log_date")
    private Date date;
    
    NodeInstanceLog() {
    }
    
	public NodeInstanceLog(Integer type, Long processInstanceId, String processId,
			               String nodeInstanceId, String nodeId) {
		this.type = type;
        this.processInstanceId = processInstanceId;
        this.processId = processId;
		this.nodeInstanceId = nodeInstanceId;
		this.nodeId = nodeId;
        this.date = new Date();
    }
	
	public Integer getType() {
		return type;
	}
	
	void setType(Integer type) {
		this.type = type;
	}
    
    public Long getId() {
    	return id;
    }
    
    void setId(Long id) {
		this.id = id;
	}

    public Long getProcessInstanceId() {
        return processInstanceId;
    }
    
	void setProcessInstanceId(Long processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

    public String getProcessId() {
        return processId;
    }
    
	void setProcessId(String processId) {
		this.processId = processId;
	}

    public String getNodeInstanceId() {
		return nodeInstanceId;
	}

	void setNodeInstanceId(String nodeInstanceId) {
		this.nodeInstanceId = nodeInstanceId;
	}

	public String getNodeId() {
		return nodeId;
	}

	void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public Date getDate() {
        return date;
    }
    
	void setDate(Date date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "NodeInstanceLog [id=" + id + ", type=" + type
				+ ", processInstanceId=" + processInstanceId + ", processId="
				+ processId + ", nodeInstanceId=" + nodeInstanceId
				+ ", nodeId=" + nodeId + ", date=" + date + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
		result = prime * result
				+ ((nodeInstanceId == null) ? 0 : nodeInstanceId.hashCode());
		result = prime * result
				+ ((processId == null) ? 0 : processId.hashCode());
		result = prime
				* result
				+ ((processInstanceId == null) ? 0 : processInstanceId
						.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		NodeInstanceLog other = (NodeInstanceLog) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (nodeId == null) {
			if (other.nodeId != null)
				return false;
		} else if (!nodeId.equals(other.nodeId))
			return false;
		if (nodeInstanceId == null) {
			if (other.nodeInstanceId != null)
				return false;
		} else if (!nodeInstanceId.equals(other.nodeInstanceId))
			return false;
		if (processId == null) {
			if (other.processId != null)
				return false;
		} else if (!processId.equals(other.processId))
			return false;
		if (processInstanceId == null) {
			if (other.processInstanceId != null)
				return false;
		} else if (!processInstanceId.equals(other.processInstanceId))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

}
