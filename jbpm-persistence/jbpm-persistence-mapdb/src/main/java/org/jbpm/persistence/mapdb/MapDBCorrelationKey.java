package org.jbpm.persistence.mapdb;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.persistence.PersistentCorrelationKey;
import org.kie.internal.jaxb.CorrelationKeyXmlAdapter;
import org.kie.internal.process.CorrelationProperty;

public class MapDBCorrelationKey implements PersistentCorrelationKey {

	private String name;
	private long processInstanceId;
	private long id;
	private List<CorrelationProperty<?>> properties = new ArrayList<>();
	
	@Override
	public List<CorrelationProperty<?>> getProperties() {
		return new ArrayList<CorrelationProperty<?>>(this.properties);
	}

	@Override
	public String toExternalForm() {
		return CorrelationKeyXmlAdapter.marshalCorrelationKey(this);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(long processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ (int) (processInstanceId ^ (processInstanceId >>> 32));
		result = prime * result
				+ ((properties == null) ? 0 : properties.hashCode());
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
		MapDBCorrelationKey other = (MapDBCorrelationKey) obj;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (processInstanceId != other.processInstanceId)
			return false;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MapDBCorrelationKey [name=" + name + ", processInstanceId="
				+ processInstanceId + ", id=" + id + ", properties="
				+ properties + "]";
	}

	public void setProperties(List<CorrelationProperty<?>> properties) {
		this.properties.clear();
		if (properties != null) {
			this.properties.addAll(properties);
		}
	}
}
