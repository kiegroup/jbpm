package org.jbpm.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.persistence.map.MapBasedPersistenceContext;
import org.jbpm.persistence.processinstance.ProcessInstanceInfo;

public class MapBasedProcessPersistenceContext extends MapBasedPersistenceContext
    implements
    ProcessPersistenceContext,
    NonTransactionalProcessPersistentSession{
    
    private ProcessStorage storage;
    private Map<Long, ProcessInstanceInfo> processes;
    private Map<String, Long> processesIdByBusinessKey;

    public MapBasedProcessPersistenceContext(ProcessStorage storage) {
        super( storage );
        this.storage = storage;
        this.processes = new HashMap<Long, ProcessInstanceInfo>();
        this.processesIdByBusinessKey = new HashMap<String, Long>();
    }

    public void persist(ProcessInstanceInfo processInstanceInfo) {
        if( processInstanceInfo.getId() == null ) {
            processInstanceInfo.setId( storage.getNextProcessInstanceId() );
        }
        processes.put( processInstanceInfo.getId(), processInstanceInfo );
        processesIdByBusinessKey.put(processInstanceInfo.getBusinessKey(), processInstanceInfo.getId());
    }

    public ProcessInstanceInfo findProcessInstanceInfo(Long processId) {
        ProcessInstanceInfo processInstanceInfo = processes.get( processId );
        if( processInstanceInfo == null){
            processInstanceInfo = storage.findProcessInstanceInfo( processId );
        }
        return processInstanceInfo;
    }

    public List<ProcessInstanceInfo> getStoredProcessInstances() {
        return Collections.unmodifiableList( new ArrayList<ProcessInstanceInfo>(processes.values()));
    }

    @Override
    public void close() {
        super.close();
        clearStoredProcessInstances();
    }

    public void remove(ProcessInstanceInfo processInstanceInfo) {
        storage.removeProcessInstanceInfo( processInstanceInfo.getId() );
    }

    public List<Long> getProcessInstancesWaitingForEvent(String type) {
        return storage.getProcessInstancesWaitingForEvent( type );
    }

    public void clearStoredProcessInstances() {
        processes.clear();
    }

    public Long getProcessInstanceByBusinessKey(String businessKey) {        
        return processesIdByBusinessKey.get(businessKey);        
    }
}
