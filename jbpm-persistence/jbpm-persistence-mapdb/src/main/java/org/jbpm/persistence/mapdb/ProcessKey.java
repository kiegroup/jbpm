package org.jbpm.persistence.mapdb;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.kie.internal.process.CorrelationKey;


public final class ProcessKey implements Serializable, Comparable<ProcessKey> {

	private static final long serialVersionUID = 1L;

	private final Long processInstanceId;
	private final String[] types;
	private final CorrelationKey corrKey;
	
	public ProcessKey(Long id, Set<String> types, CorrelationKey corrKey) {
		this(id, types == null ? null : types == null ? (String[]) null : types.toArray(new String[0]), corrKey);
	}
	
	public ProcessKey(Long processInstanced, String[] types, CorrelationKey corrKey) {
		super();
		this.processInstanceId = processInstanced;
		this.types = types;
		this.corrKey = corrKey;
	}

	public Long getProcessInstanceId() {
		return processInstanceId;
	}

	public String[] getTypes() {
		return types;
	}

	public CorrelationKey getCorrKey() {
		return corrKey;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((corrKey == null) ? 0 : corrKey.hashCode());
		result = prime * result + ((processInstanceId == null) ? 0 : processInstanceId.hashCode());
		result = prime * result + Arrays.hashCode(types);
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
		ProcessKey other = (ProcessKey) obj;
		if (corrKey == null) {
			if (other.corrKey != null)
				return false;
		} else if (!corrKey.equals(other.corrKey))
			return false;
		if (processInstanceId == null) {
			if (other.processInstanceId != null)
				return false;
		} else if (!processInstanceId.equals(other.processInstanceId))
			return false;
		if (!Arrays.equals(types, other.types))
			return false;
		return true;
	}

	@Override
	public int compareTo(ProcessKey other) {
		if (other == null) {
			return 1;
		}
		int sum = 0;
		if (this.processInstanceId != null && other.processInstanceId == null) {
			sum += 100;
		} else if (this.processInstanceId == null && other.processInstanceId != null) {
			sum -= 100;
		} else if (this.processInstanceId != null && other.processInstanceId != null) {
			sum += (this.processInstanceId.compareTo(other.processInstanceId) * 100);
		}
		if (this.types != null && other.types == null) {
			sum += 10;
		} else if (this.types == null && other.types != null) {
			sum -= 10;
		} else if (this.types != null && other.types != null) {
			HashSet<String> intersection = new HashSet<>(Arrays.asList(this.types));
			intersection.retainAll(Arrays.asList(other.types));
			sum += intersection.isEmpty() ? 0 : 10;
		}
		if (this.corrKey != null && other.corrKey == null) {
			sum ++;
		} else if (this.corrKey == null && other.corrKey != null) {
			sum --;
		} else if (this.corrKey != null && other.corrKey != null) {
			if (this.corrKey.getName() != null && other.corrKey.getName() == null) {
				sum ++;
			} else if (this.corrKey.getName() == null && other.corrKey.getName() != null) {
				sum --;
			} else if (this.corrKey.getName() != null && other.corrKey.getName() != null) {
				sum += this.corrKey.getName().compareTo(other.corrKey.getName());
			}
		}
		return sum;
	}
}
