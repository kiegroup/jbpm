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

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.process.audit.persistence.BAMTransactionManager.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BAMPersistenceManager {

    private Logger logger = LoggerFactory.getLogger(getClass());
    
    private BAMTransactionManager ttxm;
    private EntityManager em;
    private final EntityManagerFactory emf;

    
    BAMPersistenceManager(EntityManagerFactory entityManagerFactory) { 
        this.emf = entityManagerFactory;
        this.ttxm = BAMTransactionManager.getInstance(emf);
        this.em = emf.createEntityManager();
    }
    
    BAMPersistenceManager(BAMPersistenceManager tpm) { 
        this.emf = tpm.emf;
        this.ttxm = BAMTransactionManager.getInstance(emf);
        this.em = emf.createEntityManager();
    }
    
    //=====
    // dealing with transactions
    //=====
    
    public boolean beginTransaction() { 
        boolean txOwner = this.ttxm.ownsTransaction(em);
        if( txOwner ) {  
            this.ttxm.begin(em);
        }
        this.ttxm.attachPersistenceContext(em);
        return txOwner;
    }

    /**
     * This method attempts to end a transaction -- if it's called
     * by someone/thing claiming to be the transaction owner 
     * (otherwise known as the thing that started the transaction). 
     * </p>
     * If we're the tx owner, we first check if the tx has been 
     * marked for rollback. If it's marked for rollback, it doesn't 
     * make sense to commit since committing will only throw an exception. 
     * Instead, we rollback the tx. 
     * </p>
     * If it's <i>not</i> marked for rollback, we commit the tx. 
     * </p>
     * Unfortunately, rolling back or committing might have caused 
     * an exception. If we haven't yet tried to rollback, then 
     * we do it at this point: committing didn't work and we don't want 
     * to leave the transaction open. 
     * </p>
     * While we could technically add more code to make sure that
     * the transaction is closed (for example, a finally clause that
     * checks if the tx is active, etc), that's overkill because
     * the code below does everything possible to close the transaction. 
     * </p>
     * Anything done in a finally clause would either being confusing
     * (for example, checking if the tx is active <i>after</i> the 
     * tx has been commited successfully) or simply retry what
     * had already been done (rollback when that had already been 
     * tried in the catch clause). 
     * </p>
     * @param em The EntityManager: neccessary for local/entity transactions.
     * @param txOwner Whether or not the caller started this transaction.
     */
    public void endTransaction(boolean txOwner) { 
        if( txOwner ) { 
            boolean rollbackAttempted = false;
            try { 
                if( ttxm.getStatus(em) == TransactionStatus.MARKED_ROLLBACK) { 
                    rollbackAttempted = true;
                    ttxm.rollback(em, txOwner);
                }
                this.ttxm.commit(em);
            } catch(RuntimeException re) { 
                String action = rollbackAttempted ? "rollback" : "commit";
                logger.error("Unable to " + action + ".", re);
                // DBG
                re.printStackTrace();
                if( ! rollbackAttempted ) { 
                    this.ttxm.rollback(em, txOwner);
                }
                else { 
                    throw re;
                }
            }
        }
    }
    
    public void rollBackTransaction(boolean txOwner) { 
        try { 
            if( ttxm.getStatus(em) == TransactionStatus.ACTIVE ) { 
                this.ttxm.rollback(em, txOwner);
            }
        } catch(RuntimeException e) { 
            logger.error("Unable to (mark as or) rollback transaction!", e.getCause());
            //DBG
            e.printStackTrace();
        }
        
    }
    
    public void endPersistenceContext() { 
        if( em == null ) { 
            ttxm = null;
            return;
        }
        
        boolean closeEm = em.isOpen();
        if ( closeEm  ) { 
            try { 
                if( em.getTransaction().isActive() ) {
                    endTransaction(true);
                }
                em.close();
            }
            catch( Exception e ) { 
                // Don't worry about it, we're cleaning up. 
            }
        }
        
        this.em = null;
        this.ttxm = null;
    }
 
    public void addProcessLog(long processInstanceId, String processId) {
        ProcessInstanceLog log = new ProcessInstanceLog(processInstanceId, processId);
        this.em.persist(log);
    }

    @SuppressWarnings("unchecked")
    public void updateProcessLog(long processInstanceId) {
        List<ProcessInstanceLog> result = this.em.createQuery(
            "from ProcessInstanceLog as log where log.processInstanceId = ? and log.end is null")
                .setParameter(1, processInstanceId).getResultList();
        if (result != null && result.size() != 0) {
            ProcessInstanceLog log = result.get(result.size() - 1);
            log.setEnd(new Date());
            this.em.merge(log);
        }
    }

    public void addNodeEnterLog(long processInstanceId, String processId, String nodeInstanceId, String nodeId, String nodeName) {
        NodeInstanceLog log = new NodeInstanceLog(
            NodeInstanceLog.TYPE_ENTER, processInstanceId, processId, nodeInstanceId, nodeId, nodeName);
        this.em.persist(log);
    }

    public void addNodeExitLog(long processInstanceId,
            String processId, String nodeInstanceId, String nodeId, String nodeName) {
        NodeInstanceLog log = new NodeInstanceLog(
            NodeInstanceLog.TYPE_EXIT, processInstanceId, processId, nodeInstanceId, nodeId, nodeName);
        this.em.persist(log);
    }

    public void addVariableLog(long processInstanceId, String processId, String variableInstanceId, String variableId, String objectToString) {
        VariableInstanceLog log = new VariableInstanceLog(
            processInstanceId, processId, variableInstanceId, variableId, objectToString);
        this.em.persist(log);
    }
    
    @SuppressWarnings("unchecked")
    public List<ProcessInstanceLog> findProcessInstances() {
        List<ProcessInstanceLog> result = em.createQuery("FROM ProcessInstanceLog").getResultList();

        return result;
    }
    
    @SuppressWarnings("unchecked")
    public List<ProcessInstanceLog> findProcessInstances(String processId) {

        List<ProcessInstanceLog> result = em
            .createQuery("FROM ProcessInstanceLog p WHERE p.processId = :processId")
                .setParameter("processId", processId).getResultList();

        return result;
    }
    
    @SuppressWarnings("unchecked")
    public List<ProcessInstanceLog> findActiveProcessInstances(String processId) {
        
        List<ProcessInstanceLog> result = em
            .createQuery("FROM ProcessInstanceLog p WHERE p.processId = :processId AND p.end is null")
                .setParameter("processId", processId).getResultList();
        return result;
    }

    public ProcessInstanceLog findProcessInstance(long processInstanceId) {
       
        ProcessInstanceLog result = (ProcessInstanceLog) em
            .createQuery("FROM ProcessInstanceLog p WHERE p.processInstanceId = :processInstanceId")
                .setParameter("processInstanceId", processInstanceId).getSingleResult();
        return result;
    }
    
    @SuppressWarnings("unchecked")
    public List<NodeInstanceLog> findNodeInstances(long processInstanceId) {
        
        List<NodeInstanceLog> result = em
            .createQuery("FROM NodeInstanceLog n WHERE n.processInstanceId = :processInstanceId ORDER BY date")
                .setParameter("processInstanceId", processInstanceId).getResultList();
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<NodeInstanceLog> findNodeInstances(long processInstanceId, String nodeId) {
        
        List<NodeInstanceLog> result = em
            .createQuery("FROM NodeInstanceLog n WHERE n.processInstanceId = :processInstanceId AND n.nodeId = :nodeId ORDER BY date")
                .setParameter("processInstanceId", processInstanceId)
                .setParameter("nodeId", nodeId).getResultList();
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<VariableInstanceLog> findVariableInstances(long processInstanceId) {
        
        List<VariableInstanceLog> result = em
            .createQuery("FROM VariableInstanceLog v WHERE v.processInstanceId = :processInstanceId ORDER BY date")
                .setParameter("processInstanceId", processInstanceId).getResultList();
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<VariableInstanceLog> findVariableInstances(long processInstanceId, String variableId) {
        
        List<VariableInstanceLog> result = em
            .createQuery("FROM VariableInstanceLog v WHERE v.processInstanceId = :processInstanceId AND v.variableId = :variableId ORDER BY date")
                .setParameter("processInstanceId", processInstanceId)
                .setParameter("variableId", variableId).getResultList();
        return result;
    }

    @SuppressWarnings("unchecked")
    public void clear() {
            
        List<ProcessInstanceLog> processInstances = em.createQuery("FROM ProcessInstanceLog").getResultList();
        for (ProcessInstanceLog processInstance: processInstances) {
            em.remove(processInstance);
        }
        List<NodeInstanceLog> nodeInstances = em.createQuery("FROM NodeInstanceLog").getResultList();
        for (NodeInstanceLog nodeInstance: nodeInstances) {
            em.remove(nodeInstance);
        }
        List<VariableInstanceLog> variableInstances = em.createQuery("FROM VariableInstanceLog").getResultList();
        for (VariableInstanceLog variableInstance: variableInstances) {
            em.remove(variableInstance);
        }           

    }
}