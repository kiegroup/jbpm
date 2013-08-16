package org.jbpm.persistence;

import org.drools.persistence.infinispan.InfinispanPersistenceContextManager;
import org.kie.api.runtime.Environment;

public class InfinispanProcessPersistenceContextManager extends InfinispanPersistenceContextManager
    implements
    ProcessPersistenceContextManager {

    public InfinispanProcessPersistenceContextManager(Environment env) {
        super( env );
    }

    public ProcessPersistenceContext getProcessPersistenceContext() {
        return new InfinispanProcessPersistenceContext( cmdScopedCache );
    }

}
