/*
 * Copyright 2012 JBoss Inc
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
package org.jbpm.process.audit.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BAMLocalTransactionManager extends BAMTransactionManager {

    private Logger logger = LoggerFactory.getLogger(getClass());
    
    BAMLocalTransactionManager() { 
        // empty constructor
    }
    
    boolean ownsTransaction(EntityManager em) { 
        TransactionStatus status = getStatus(em);
       return (status != TransactionStatus.ACTIVE
               && status != TransactionStatus.MARKED_ROLLBACK);     
    }
    
    void attachPersistenceContext(EntityManager em) { 
        // no-op for entity transactions
    }
    
    synchronized void begin(EntityManager em) {
        try {
            em.getTransaction().begin();
        } catch (Exception e) {
            logger.warn("Unable to begin transaction", e);
            throw new RuntimeException("Unable to begin transaction", e);
        }
    }

    void commit(EntityManager em) {
        try { 
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.warn("Unable to begin transaction", e);
            throw new RuntimeException("Unable to commit transaction", e);
        }
    }

    void rollback(EntityManager em, boolean txOwner) {
        if( ! em.getTransaction().isActive() ) { 
            return;
        }
        
        try {
            if( txOwner ) { 
                em.getTransaction().rollback();
            }
            else { 
                em.getTransaction().setRollbackOnly();
            }
        } catch (Exception e) {
            logger.warn("Unable to rollback transaction", e);
            throw new RuntimeException("Unable to rollback transaction", e);
        }
    }

    TransactionStatus getStatus(EntityManager em) { 
        EntityTransaction tx = em.getTransaction();
        if( tx.isActive() ) { 
            if( tx.getRollbackOnly() ) { 
                return TransactionStatus.MARKED_ROLLBACK;
            }
            return TransactionStatus.ACTIVE;
        }
        else { 
            return TransactionStatus.COMMITTED;
        }
    }
    
    void registerTransactionSynchronization(TransactionSynchronization ts) {
        // DBG Auto-generated method stub

    }

}
