/**
 * Copyright 2010 JBoss Inc
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

package org.jbpm.process.audit;

import java.util.List;

import org.drools.runtime.Environment;
import org.jbpm.process.audit.persistence.BAMPersistenceManager;
import org.jbpm.process.audit.persistence.BAMPersistenceManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is essentially a very simple implementation of a service
 * that deals with ProcessInstanceLog entities. 
 * </p>
 * Please see the public methods for the interface of this service. 
 * </p>
 * Similar to the "ProcessInstanceDbLog", the idea here is that we 
 * have a entity manager factory (similar to a session factory) that
 * we repeatedly use to generate an entity manager (which is a persistence context)
 * for the specific service command. 
 * </p>
 * While ProcessInstanceLog entities do not contain LOB's (which sometimes
 * necessitate the use of tx's even in <i>read</i> situations, we use
 * transactions here none-the-less, just to be safe. Obviously, if 
 * there is already a running transaction present, we don't do anything
 * to it. 
 * </p>
 * At the end of every command, we make sure to close the entity manager
 * we've been using -- which also means that we detach any entities that
 * might be associated with the entity manager/persistence context. 
 * After all, this is a <i>service</i> which means our philosophy here 
 * is to provide a real interface, and not a leaky one. 
 */
public class JPAProcessInstanceDbLog {

    private static Logger logger = LoggerFactory.getLogger(JPAProcessInstanceDbLog.class);
    
    private static volatile Environment env;
   

    @Deprecated
    public JPAProcessInstanceDbLog() {
    }
    
    @Deprecated
    public JPAProcessInstanceDbLog(Environment env){
        JPAProcessInstanceDbLog.env = env;
    }

    public static void setEnvironment(Environment newEnv) { 
        JPAProcessInstanceDbLog.env = newEnv;
    }
    
    public void og() { 
        
    }
    
    
    public static List<ProcessInstanceLog> findProcessInstances() {
        BAMPersistenceManager manager = BAMPersistenceManagerFactory.getBAMPersistenceManager(env);
        boolean txOwner = manager.beginTransaction();
        
        List<ProcessInstanceLog> result = manager.findProcessInstances();
        
        manager.endTransaction(txOwner);
        return result;
    }

    
    public static List<ProcessInstanceLog> findProcessInstances(String processId) {
        BAMPersistenceManager manager = BAMPersistenceManagerFactory.getBAMPersistenceManager(env);
        boolean txOwner = manager.beginTransaction();
        
        List<ProcessInstanceLog> result = manager.findProcessInstances(processId);
        
        manager.endTransaction(txOwner);
        return result;
    }

    
    public static List<ProcessInstanceLog> findActiveProcessInstances(String processId) {
        BAMPersistenceManager manager = BAMPersistenceManagerFactory.getBAMPersistenceManager(env);
        boolean txOwner = manager.beginTransaction();
        List<ProcessInstanceLog> result = manager.findActiveProcessInstances(processId);
        manager.endTransaction(txOwner);
        return result;
    }

    public static ProcessInstanceLog findProcessInstance(long processInstanceId) {
        BAMPersistenceManager manager = BAMPersistenceManagerFactory.getBAMPersistenceManager(env);
        boolean txOwner = manager.beginTransaction();
        ProcessInstanceLog result = manager.findProcessInstance(processInstanceId);
        manager.endTransaction(txOwner);
        return result;
    }
    
    
    public static List<NodeInstanceLog> findNodeInstances(long processInstanceId) {
        BAMPersistenceManager manager = BAMPersistenceManagerFactory.getBAMPersistenceManager(env);
        boolean txOwner = manager.beginTransaction();
        List<NodeInstanceLog> result = manager.findNodeInstances(processInstanceId);
        manager.endTransaction(txOwner);
        return result;
    }

    
    public static List<NodeInstanceLog> findNodeInstances(long processInstanceId, String nodeId) {
        BAMPersistenceManager manager = BAMPersistenceManagerFactory.getBAMPersistenceManager(env);
        boolean txOwner = manager.beginTransaction();
        List<NodeInstanceLog> result = manager.findNodeInstances(processInstanceId, nodeId);
        manager.endTransaction(txOwner);
        return result;
    }

    
    public static List<VariableInstanceLog> findVariableInstances(long processInstanceId) {
        BAMPersistenceManager manager = BAMPersistenceManagerFactory.getBAMPersistenceManager(env);
        boolean txOwner = manager.beginTransaction();
        List<VariableInstanceLog> result = manager.findVariableInstances(processInstanceId);
        manager.endTransaction(txOwner);
        return result;
    }

    
    public static List<VariableInstanceLog> findVariableInstances(long processInstanceId, String variableId) {
        BAMPersistenceManager manager = BAMPersistenceManagerFactory.getBAMPersistenceManager(env);
        boolean txOwner = manager.beginTransaction();
        List<VariableInstanceLog> result = manager.findVariableInstances(processInstanceId, variableId);
        manager.endTransaction(txOwner);
        return result;
    }

    
    public static void clear() {
        BAMPersistenceManager manager = BAMPersistenceManagerFactory.getBAMPersistenceManager(env);
        boolean txOwner = manager.beginTransaction();
            
        manager.clear();           
        manager.endTransaction(txOwner);
    }

    @Deprecated
    public static void dispose() {
        
    }
    
    @Override
    protected void finalize() throws Throwable {
        
    }

}
