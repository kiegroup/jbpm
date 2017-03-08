package org.jbpm.persistence.mapdb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.persistence.TransactionManager;
import org.drools.persistence.TransactionManagerHelper;
import org.jbpm.persistence.PersistentProcessInstance;
import org.jbpm.persistence.ProcessPersistenceContext;
import org.jbpm.persistence.ProcessPersistenceContextManager;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.ProcessInstanceManager;
import org.jbpm.process.instance.impl.ProcessInstanceImpl;
import org.jbpm.process.instance.timer.TimerManager;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.jbpm.workflow.instance.node.StateBasedNodeInstance;
import org.jbpm.workflow.instance.node.TimerNodeInstance;
import org.kie.api.definition.process.Process;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

public class MapDBProcessInstanceManager implements ProcessInstanceManager {

	private InternalKnowledgeRuntime kruntime;
	
	private volatile transient Map<Long, ProcessInstance> processInstances = new ConcurrentHashMap<Long, ProcessInstance>();

	public MapDBProcessInstanceManager(InternalKnowledgeRuntime kruntime) {
		this.kruntime = kruntime;
	}

	@Override
	public ProcessInstance getProcessInstance(long id) {
		return getProcessInstance(id, false);
	}

	@Override
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
                MapDBProcessInstance processInstanceInfo = (MapDBProcessInstance) context.findProcessInstanceInfo( id );
                if ( processInstanceInfo == null ) {
                    return null;
                }
                TransactionManagerHelper.addToUpdatableSet(txm, processInstanceInfo);
                processInstanceInfo.getProcessInstance(kruntime, kruntime.getEnvironment(), readOnly);
                processInstanceInfo.updateLastReadDate();
  
            }
        	return processInstance;
        }

    	// Make sure that the cmd scoped entity manager has started
    	ProcessPersistenceContextManager ppcm 
    	    = (ProcessPersistenceContextManager) this.kruntime.getEnvironment().get( EnvironmentName.PERSISTENCE_CONTEXT_MANAGER );
    	ppcm.beginCommandScopedEntityManager();
    	
        ProcessPersistenceContext context = ppcm.getProcessPersistenceContext();
        MapDBProcessInstance processInstanceInfo = (MapDBProcessInstance) context.findProcessInstanceInfo( id );
        if ( processInstanceInfo == null ) {
            return null;
        }
        processInstance = (org.jbpm.process.instance.ProcessInstance)
        	processInstanceInfo.getProcessInstance(kruntime, this.kruntime.getEnvironment(), false);
        if (!readOnly) {
        	TransactionManagerHelper.addToUpdatableSet(txm, processInstanceInfo);
            processInstanceInfo.updateLastReadDate();
        }
        if (((ProcessInstanceImpl) processInstance).getProcessXml() == null) {
	        Process process = kruntime.getKieBase().getProcess( processInstance.getProcessId() );
	        if ( process == null ) {
	            throw new IllegalArgumentException( "Could not find process " + processInstance.getProcessId() );
	        }
	        processInstance.setProcess( process );
        }
        if ( processInstance.getKnowledgeRuntime() == null ) {
            Long parentProcessInstanceId = (Long) ((ProcessInstanceImpl) processInstance).getMetaData().get("ParentProcessInstanceId");
            if (parentProcessInstanceId != null) {
                kruntime.getProcessInstance(parentProcessInstanceId);
            }
            processInstance.setKnowledgeRuntime( kruntime );
            ((ProcessInstanceImpl) processInstance).reconnect();
        }
        return processInstance;
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

	@Override
	public Collection<ProcessInstance> getProcessInstances() {
		return Collections.unmodifiableCollection(processInstances.values());
	}

	@Override
	public void addProcessInstance(ProcessInstance processInstance,
			CorrelationKey correlationKey) {
		MapDBProcessInstance processInstanceInfo = new MapDBProcessInstance( processInstance, this.kruntime.getEnvironment() );
        ProcessPersistenceContext context 
            = ((ProcessPersistenceContextManager) this.kruntime.getEnvironment()
                    .get( EnvironmentName.PERSISTENCE_CONTEXT_MANAGER ))
                    .getProcessPersistenceContext();
        processInstanceInfo.transform();
        processInstanceInfo = (MapDBProcessInstance) context.persist( processInstanceInfo );
        ((org.jbpm.process.instance.ProcessInstance) processInstance).setId( processInstanceInfo.getId() );
        processInstanceInfo.updateLastReadDate();
        // persist correlation if exists
        if (correlationKey != null) {
            MapDBCorrelationKey correlationKeyInfo = (MapDBCorrelationKey) correlationKey;
            correlationKeyInfo.setProcessInstanceId(processInstanceInfo.getId());
            context.persist(correlationKeyInfo);
        }
        internalAddProcessInstance(processInstance);
	}

	@Override
	public void internalAddProcessInstance(ProcessInstance processInstance) {
        processInstances.put(processInstance.getId(), processInstance); 
	}

	@Override
	public void removeProcessInstance(ProcessInstance processInstance) {
		ProcessPersistenceContext context = ((ProcessPersistenceContextManager) this.kruntime.getEnvironment().get( EnvironmentName.PERSISTENCE_CONTEXT_MANAGER )).getProcessPersistenceContext();
        PersistentProcessInstance processInstanceInfo = context.findProcessInstanceInfo( processInstance.getId() );
        
        if ( processInstanceInfo != null ) {
            context.remove( processInstanceInfo );
        }
        internalRemoveProcessInstance(processInstance);
	}

	@Override
	public void internalRemoveProcessInstance(ProcessInstance processInstance) {
		processInstances.remove( processInstance.getId() );
	}

	@Override
	public void clearProcessInstances() {
		for (ProcessInstance processInstance: new ArrayList<ProcessInstance>(processInstances.values())) {
            ((ProcessInstanceImpl) processInstance).disconnect();
        }
		processInstances.clear();
	}

	@Override
	public void clearProcessInstancesState() {
		try {
            // at this point only timers are considered as state that needs to be cleared
            TimerManager timerManager = ((InternalProcessRuntime)kruntime.getProcessRuntime()).getTimerManager();
            
            for (ProcessInstance processInstance: new ArrayList<ProcessInstance>(processInstances.values())) {
                WorkflowProcessInstance pi = ((WorkflowProcessInstance) processInstance);
    
                
                for (org.kie.api.runtime.process.NodeInstance nodeInstance : pi.getNodeInstances()) {
                    if (nodeInstance instanceof TimerNodeInstance){
                        if (((TimerNodeInstance)nodeInstance).getTimerInstance() != null) {
                            timerManager.cancelTimer(((TimerNodeInstance)nodeInstance).getTimerInstance().getId());
                        }
                    } else if (nodeInstance instanceof StateBasedNodeInstance) {
                        List<Long> timerIds = ((StateBasedNodeInstance) nodeInstance).getTimerInstances();
                        if (timerIds != null) {
                            for (Long id: timerIds) {
                                timerManager.cancelTimer(id);
                            }
                        }
                    }
                }
                
            }
        } catch (Exception e) {
            // catch everything here to make sure it will not break any following 
            // logic to allow complete clean up 
        }
	}

}
