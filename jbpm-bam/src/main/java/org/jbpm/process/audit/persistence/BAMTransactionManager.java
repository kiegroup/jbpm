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
import javax.persistence.EntityManagerFactory;

abstract class BAMTransactionManager {
    
    enum TransactionStatus { 
        ACTIVE, COMMITTED, 
        MARKED_ROLLBACK, ROLLEDBACK, 
        NO_TRANSACTION, UNKNOWN; 
    }

    abstract void begin(EntityManager em);

    abstract void commit(EntityManager em);

    abstract void rollback(EntityManager em, boolean txOwner);

    abstract void registerTransactionSynchronization(TransactionSynchronization ts);
    
    abstract TransactionStatus getStatus(EntityManager em);
    
    abstract boolean ownsTransaction(EntityManager em);
    
    abstract void attachPersistenceContext(EntityManager em);
    
    interface TransactionSynchronization {
        void beforeCompletion();
        void afterCompletion(int status);
    }
    
    static BAMTransactionManager getInstance(EntityManagerFactory emf) {
        BAMTransactionManager ttxm = null;
        EntityManager em = emf.createEntityManager();
        
        boolean useResourceLocalTxm = false;
        boolean useJTATxm = false;
        try { 
            em.getTransaction();
            useResourceLocalTxm = true;
        } catch(Exception e) { 
            boolean illegalStateExceptionThrown = false;
            Throwable cause = e;
            while( cause != null && ! illegalStateExceptionThrown ) { 
                illegalStateExceptionThrown = (cause instanceof IllegalStateException);
                cause = cause.getCause();
            }
            if( illegalStateExceptionThrown ) { 
                useJTATxm = true;
            }
            else { 
                // this resource is not JTA
                throw new RuntimeException("Unable to determine persistence-unit type (JTA/Local)", e);
            }
        }
            
        if( useJTATxm ) { 
            ttxm = new BAMJTATransactionManager();
        } else if( useResourceLocalTxm ) { 
            ttxm = new BAMLocalTransactionManager();
        } else { 
            throw new RuntimeException("Unknown resource type");
        }
        return ttxm;
    }
}
