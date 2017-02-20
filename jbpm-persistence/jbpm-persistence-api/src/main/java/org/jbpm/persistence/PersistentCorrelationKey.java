package org.jbpm.persistence;

import org.kie.internal.process.CorrelationKey;

public interface PersistentCorrelationKey extends CorrelationKey {

	long getProcessInstanceId();

	long getId();

}
