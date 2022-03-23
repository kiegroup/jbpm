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

package org.jbpm.persistence.processinstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.persistence.api.TransactionManager;
import org.drools.persistence.api.TransactionManagerHelper;
import org.jbpm.persistence.api.ProcessPersistenceContext;
import org.jbpm.persistence.api.ProcessPersistenceContextManager;
import org.jbpm.persistence.api.integration.EventManagerProvider;
import org.jbpm.persistence.api.integration.InstanceView;
import org.jbpm.persistence.api.integration.model.CaseInstanceView;
import org.jbpm.persistence.api.integration.model.ProcessInstanceView;
import org.jbpm.persistence.correlation.CorrelationKeyInfo;
import org.jbpm.persistence.correlation.CorrelationPropertyInfo;
import org.jbpm.process.instance.ProcessInstanceManager;
import org.jbpm.process.instance.impl.ProcessInstanceImpl;
import org.jbpm.workflow.core.WorkflowProcess;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.kie.api.definition.process.Process;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

/**
 * This is an implementation of the {@link ProcessInstanceManager} that uses JPA.
 * </p>
 * What's important to remember here is that we have a jbpm-console which has 1 static (stateful) knowledge session
 * which is used by multiple threads: each request sent to the jbpm-console is picked up in it's own thread. 
 * </p>
 * This means that multiple threads can be using the same instance of this class. 
 */
public class JPAProcessInstanceManager
    implements
    ProcessInstanceManager {

    private InternalKnowledgeRuntime kruntime;
    // In a scenario in which 1000's of processes are running daily,
    //   lazy initialization is more costly than eager initialization
    // Added volatile so that if something happens, we can figure out what
    private volatile transient Map<Long, ProcessInstance> processInstances = new ConcurrentHashMap<Long, ProcessInstance>();

    
    public void setKnowledgeRuntime(InternalKnowledgeRuntime kruntime) {
        this.kruntime = kruntime;
    }

    @Override
    public Collection<ProcessInstance> loadKnowledgeRuntimeProcessInstances() {
        InternalRuntimeManager manager = (InternalRuntimeManager) kruntime.getEnvironment().get(EnvironmentName.RUNTIME_MANAGER);
        List<Long> ids = null;
        if (manager != null && manager.useContextMapping()) {
           ids = toLong(manager.getEnvironment().getMapper().findContextId(((KieSession) kruntime).getIdentifier(), manager.getIdentifier()));
        } else {
            // there is no mapping info in this case so we can only check during getProcessInstance if it belongs or not to the kruntime
            ProcessPersistenceContext context = ((ProcessPersistenceContextManager) this.kruntime.getEnvironment()
                            .get(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER))
                            .getProcessPersistenceContext();
            ids = context.findAllProcessInstanceInfo();

        }
        List<Long> loaded = processInstances.keySet().stream().collect(Collectors.toCollection(ArrayList::new));
        ids.removeAll(loaded);
        ids.forEach(id -> { 
            try {
                getProcessInstance(id);
            } catch (Exception e) {
                // do nothing
            }
        });
        return processInstances.values();
    }

    private List<Long> toLong(Object value) {
        if (value instanceof String ) {
            return toLong(Collections.singletonList((String) value));
        } else if (value instanceof List) {
            return toLong((List<String>) value);
        }
        return Collections.emptyList();
    }

    private List<Long> toLong(List<String> items) {
        List<Long> longList = new ArrayList<>();
        for (String item : items) {
            try {
                longList.add(Long.parseLong(item));
            } catch (NumberFormatException e) {
                // do nothing
            }
        }
        return longList;
    }


    public void addProcessInstance(ProcessInstance processInstance, CorrelationKey correlationKey) {
        ProcessInstanceInfo processInstanceInfo = new ProcessInstanceInfo( processInstance, this.kruntime.getEnvironment() );
        ProcessPersistenceContext context 
            = ((ProcessPersistenceContextManager) this.kruntime.getEnvironment()
                    .get( EnvironmentName.PERSISTENCE_CONTEXT_MANAGER ))
                    .getProcessPersistenceContext();

        processInstanceInfo = (ProcessInstanceInfo) context.persist( processInstanceInfo );
        ((org.jbpm.process.instance.ProcessInstance) processInstance).setId( processInstanceInfo.getId() );
        processInstanceInfo.updateLastReadDate();
        // generate correlation key if not given which is same as process instance id to keep uniqueness 
        if (correlationKey == null) {
            correlationKey = new CorrelationKeyInfo();
            ((CorrelationKeyInfo) correlationKey).addProperty(new CorrelationPropertyInfo(null, processInstanceInfo.getId().toString()));
            ((CorrelationKeyInfo) correlationKey).setName(correlationKey.toExternalForm());
            ((org.jbpm.process.instance.ProcessInstance) processInstance).getMetaData().put("CorrelationKey", correlationKey);
        }
        CorrelationKeyInfo correlationKeyInfo = (CorrelationKeyInfo) correlationKey;
        correlationKeyInfo.setProcessInstanceId(processInstanceInfo.getId());
        context.persist(correlationKeyInfo);
        internalAddProcessInstance(processInstance);
        
        EventManagerProvider.getInstance().get().create(getInstanceViewFor(processInstance));
    }
    
    public void internalAddProcessInstance(ProcessInstance processInstance) {
        if( ((ConcurrentHashMap<Long, ProcessInstance>) processInstances)
                .putIfAbsent(processInstance.getId(), processInstance) 
                != null ) { 
            throw new ConcurrentModificationException(
                    "Duplicate process instance [" + processInstance.getProcessId() + "/" + processInstance.getId() + "]"
                    + " added to process instance manager." );
        }
    }

    public ProcessInstance getProcessInstance(long id) {
        return getProcessInstance(id, false);
    }

    public ProcessInstance getProcessInstance(long id, boolean readOnly) {
        InternalRuntimeManager manager = (InternalRuntimeManager) kruntime.getEnvironment().get(EnvironmentName.RUNTIME_MANAGER);
        if (manager != null) {
            manager.validate((KieSession) kruntime, ProcessInstanceIdContext.get(id));
        }
        TransactionManager txm = (TransactionManager) this.kruntime.getEnvironment().get( EnvironmentName.TRANSACTION_MANAGER );
        org.jbpm.process.instance.ProcessInstance processInstance = null;
        processInstance = (org.jbpm.process.instance.ProcessInstance) this.processInstances.get(id);
        if (processInstance != null) {
            
            if (((WorkflowProcessInstanceImpl) processInstance).isPersisted() && !readOnly) {
            	ProcessPersistenceContextManager ppcm 
        	    = (ProcessPersistenceContextManager) this.kruntime.getEnvironment().get( EnvironmentName.PERSISTENCE_CONTEXT_MANAGER );
            	ppcm.beginCommandScopedEntityManager();
            	ProcessPersistenceContext context = ppcm.getProcessPersistenceContext();
                ProcessInstanceInfo processInstanceInfo = (ProcessInstanceInfo) context.findProcessInstanceInfo( id );
                if ( processInstanceInfo == null ) {
                    return null;
                }  
                TransactionManagerHelper.addToUpdatableSet(txm, processInstanceInfo);
                processInstanceInfo.updateLastReadDate();
                

                EventManagerProvider.getInstance().get().update(getInstanceViewFor(processInstance));
            }
        	return processInstance;
        }
        try {
        	// Make sure that the cmd scoped entity manager has started
        	ProcessPersistenceContextManager ppcm 
        	    = (ProcessPersistenceContextManager) this.kruntime.getEnvironment().get( EnvironmentName.PERSISTENCE_CONTEXT_MANAGER );
        	ppcm.beginCommandScopedEntityManager();
        	
            ProcessPersistenceContext context = ppcm.getProcessPersistenceContext();
            ProcessInstanceInfo processInstanceInfo = (ProcessInstanceInfo) context.findProcessInstanceInfo( id );
            if ( processInstanceInfo == null ) {
                return null;
            }
            processInstance = (org.jbpm.process.instance.ProcessInstance)
            	processInstanceInfo.getProcessInstance(kruntime, this.kruntime.getEnvironment(), readOnly);
            if (!readOnly) {
                processInstanceInfo.updateLastReadDate();
                TransactionManagerHelper.addToUpdatableSet(txm, processInstanceInfo);            
            }
            if (((ProcessInstanceImpl) processInstance).getProcessXml() == null) {
    	        Process process = kruntime.getKieBase().getProcess( processInstance.getProcessId() );
    	        if ( process == null ) {
    	            throw new IllegalArgumentException( "Could not find process " + processInstance.getProcessId() );
    	        }
    	        processInstance.setProcess( process );
            }

            Long parentProcessInstanceId = (Long) ((ProcessInstanceImpl) processInstance).getMetaData().get("ParentProcessInstanceId");
            if (parentProcessInstanceId != null) {
                kruntime.getProcessInstance(parentProcessInstanceId);
            }

            if (processInstance.getKnowledgeRuntime() == null && !readOnly) {
                processInstance.setKnowledgeRuntime(kruntime);
                ((ProcessInstanceImpl) processInstance).reconnect();
            } 

            if (readOnly) {
                internalRemoveProcessInstance(processInstance);
                context.evict(processInstanceInfo);
            }
            return processInstance;
        } finally {
            if (!readOnly && processInstance != null) {
                EventManagerProvider.getInstance().get().update(getInstanceViewFor(processInstance));
            }
        }
    }

    public Collection<ProcessInstance> getProcessInstances() {
        return Collections.unmodifiableCollection(processInstances.values());
    }

    public void removeProcessInstance(ProcessInstance processInstance) {
        ProcessPersistenceContext context = ((ProcessPersistenceContextManager) this.kruntime.getEnvironment().get( EnvironmentName.PERSISTENCE_CONTEXT_MANAGER )).getProcessPersistenceContext();
        ProcessInstanceInfo processInstanceInfo = (ProcessInstanceInfo) context.findProcessInstanceInfo( processInstance.getId() );
        
        if ( processInstanceInfo != null ) {
            context.remove( processInstanceInfo );
        }
        internalRemoveProcessInstance(processInstance);
        
        EventManagerProvider.getInstance().get().delete(getInstanceViewFor(processInstance));
    }

    public void internalRemoveProcessInstance(ProcessInstance processInstance) {
        processInstances.remove( processInstance.getId() );
    }
    
    public void clearProcessInstances() {
        for (ProcessInstance processInstance: new ArrayList<ProcessInstance>(processInstances.values())) {
            ((ProcessInstanceImpl) processInstance).disconnect();
        }
    }

    public void clearProcessInstancesState() {
        // this is controlled in the interceptors with transactions
    }

    @Override
    public ProcessInstance getProcessInstance(CorrelationKey correlationKey) {
        ProcessPersistenceContext context = ((ProcessPersistenceContextManager) this.kruntime.getEnvironment()
                .get( EnvironmentName.PERSISTENCE_CONTEXT_MANAGER ))
                .getProcessPersistenceContext();
        Long processInstanceId = context.getProcessInstanceByCorrelationKey(correlationKey);
        if (processInstanceId == null) {
            return null;
        }
        return getProcessInstance(processInstanceId);
    }
    
    protected InstanceView<ProcessInstance> getInstanceViewFor(ProcessInstance pi) {
        if (((WorkflowProcess)pi.getProcess()).isDynamic()) {
            return new CaseInstanceView(pi);
        }
        
        return new ProcessInstanceView(pi);
    }

}
