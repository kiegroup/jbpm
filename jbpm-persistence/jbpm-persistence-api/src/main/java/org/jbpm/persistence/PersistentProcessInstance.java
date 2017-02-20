package org.jbpm.persistence;

import java.util.Set;

import org.drools.persistence.Transformable;

public interface PersistentProcessInstance extends Transformable {

	Long getId();
	
	void setId(Long id);

	Set<String> getEventTypes();

	byte[] getProcessInstanceByteArray();

}
