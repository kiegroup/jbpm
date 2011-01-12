package org.jbpm.persistence;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.Query;

import org.drools.persistence.jpa.JpaPersistenceContext;
import org.jbpm.persistence.processinstance.ProcessInstanceInfo;

public class JpaProcessPersistenceContext extends JpaPersistenceContext
    implements
    ProcessPersistenceContext {

    
    public JpaProcessPersistenceContext(EntityManager em) {
        super( em );
    }

    public void persist(ProcessInstanceInfo processInstanceInfo) {
        em.persist( processInstanceInfo );
    }

    public ProcessInstanceInfo findProcessInstanceInfo(Long processId) {
        return em.find( ProcessInstanceInfo.class, processId );
    }

    public void remove(ProcessInstanceInfo processInstanceInfo) {
        em.remove( processInstanceInfo );
    }

    @SuppressWarnings("unchecked")
    public List<Long> getProcessInstancesWaitingForEvent(String type) {
        Query processInstancesForEvent = em.createNamedQuery( "ProcessInstancesWaitingForEvent" );
        processInstancesForEvent.setFlushMode(FlushModeType.COMMIT);
        processInstancesForEvent.setParameter( "type",
                                               type );
        return (List<Long>) processInstancesForEvent.getResultList();
    }
    
}
