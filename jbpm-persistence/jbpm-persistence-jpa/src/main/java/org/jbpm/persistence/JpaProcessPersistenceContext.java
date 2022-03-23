/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.persistence;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.drools.persistence.api.TransactionManager;
import org.drools.persistence.api.TransactionManagerHelper;
import org.drools.persistence.jpa.JpaPersistenceContext;
import org.jbpm.persistence.api.PersistentCorrelationKey;
import org.jbpm.persistence.api.PersistentProcessInstance;
import org.jbpm.persistence.api.ProcessPersistenceContext;
import org.jbpm.persistence.correlation.CorrelationKeyInfo;
import org.jbpm.persistence.processinstance.JPASignalManager;
import org.jbpm.persistence.processinstance.ProcessInstanceInfo;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.process.instance.ProcessInstanceManager;
import org.kie.internal.process.CorrelationKey;

public class JpaProcessPersistenceContext extends JpaPersistenceContext
    implements
    ProcessPersistenceContext {
    
    public JpaProcessPersistenceContext(EntityManager em, TransactionManager txm) {
        super( em, txm );
    }

    public JpaProcessPersistenceContext(EntityManager em, boolean useJTA, boolean locking, String lockingMode, TransactionManager txm) {
        super( em, useJTA, locking, lockingMode, txm);
    }

    public PersistentProcessInstance persist(PersistentProcessInstance processInstanceInfo) {
        EntityManager em = getEntityManager();
        em.persist(processInstanceInfo);
        TransactionManagerHelper.addToUpdatableSet(txm, processInstanceInfo);
        if( this.pessimisticLocking ) { 
        	em.flush();
            return em.find(ProcessInstanceInfo.class, processInstanceInfo.getId(), lockMode );
        }
        return processInstanceInfo;
    }

    public PersistentProcessInstance findProcessInstanceInfo(Long processId) {
    	EntityManager em = getEntityManager();       
    	if( this.pessimisticLocking ) { 
            return em.find( ProcessInstanceInfo.class, processId, lockMode );
        }
        return em.find( ProcessInstanceInfo.class, processId );
    }

    public void remove(PersistentProcessInstance processInstanceInfo) {
        getEntityManager().remove( processInstanceInfo );
        TransactionManagerHelper.removeFromUpdatableSet(txm, processInstanceInfo);
        List<CorrelationKeyInfo> correlations = getEntityManager().createNamedQuery("GetCorrelationKeysByProcessInstanceId")
        .setParameter("pId", processInstanceInfo.getId()).getResultList();
        if (correlations != null) {
            for (CorrelationKeyInfo key : correlations) {
                getEntityManager().remove(key);
            }
        }
    }

    @Override
    public void evict(PersistentProcessInstance processInstanceInfo) {
        getEntityManager().detach(processInstanceInfo);
    }

    /**
     * This method is used by the {@link JPASignalManager} in order to load {@link ProcessInstance} instances
     * into the {@link ProcessInstanceManager} cache so that they can then be signalled. 
     * </p>
     * Unfortunately, with regards to locking, the method is not always called during a transaction, which means 
     * that including logic to lock the query will cause exceptions and is not feasible. 
     * </p>
     * Because the {@link org.drools.core.command.SingleSessionCommandService} design is based around a synchronized execute(...) method,
     * it's not possible for one thread to create a process instance while another thread simultaneously tries to 
     * signal it. That means that a 
     * <a href="http://en.wikipedia.org/wiki/Isolation_%28database_systems%29#Phantom_reads">phantom read</a> 
     * race condition, that might be caused by a lack of pessimistic locking on this query, isn't possible. 
     * </p>
     * Of course, if you're using multiple ksessions to simultaneoulsy interact with the same process instance, 
     * all bets are off. This however is true for almost everything involving process instances, so that it's not 
     * worth discussing. 
     * </p>
     */
    public List<Long> getProcessInstancesWaitingForEvent(String type) {
    	EntityManager entityManager = getEntityManager();
    	if (entityManager != null) {
	        Query processInstancesForEvent = getEntityManager().createNamedQuery( "ProcessInstancesWaitingForEvent" );
	        processInstancesForEvent.setParameter( "type",
	                                               type );
	        return (List<Long>) processInstancesForEvent.getResultList();
    	} else {
    		// entity manager can be null when fireActivationCreated is
    		// called on session unmarshalling
    		return new ArrayList<Long>();
    	}
    }

    public PersistentCorrelationKey persist(PersistentCorrelationKey correlationKeyInfo) {
        try {
            EntityManager em = getEntityManager();
            em.persist(correlationKeyInfo);
            em.flush();
            if (this.pessimisticLocking) {
                return em.find(CorrelationKeyInfo.class, correlationKeyInfo.getId(), this.lockMode);
            }
            return correlationKeyInfo;
        } catch (PersistenceException e) {
            throw new RuntimeException(correlationKeyInfo + " already exists", e);
        }
    }

    /**
     * With regards to locking, the method is not always called during a transaction, which means 
     * that including logic to lock the query will cause exceptions and is not feasible. 
     * </p>
     * However, this is not an issue: see the {@link #getProcessInstancesWaitingForEvent(String)} documentation
     * for more information. The same logic applies to this method. 
     * </p>
     */
    public Long getProcessInstanceByCorrelationKey(CorrelationKey correlationKey) {
        Query processInstancesForEvent = getEntityManager().createNamedQuery( "GetProcessInstanceIdByCorrelation" );
        processInstancesForEvent.setParameter( "ckey", correlationKey.toExternalForm());
        
        try {
            return (Long) processInstancesForEvent.getSingleResult();
        } catch (NonUniqueResultException e) {
            return null;
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Long> findAllProcessInstanceInfo() {
        EntityManager em = getEntityManager();
        return em.createQuery("SELECT o.id FROM ProcessInstanceInfo o", Long.class).getResultList();
    }

}
