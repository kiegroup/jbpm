package org.jbpm.workflow.exception;

import java.util.HashMap;

import org.drools.event.DefaultProcessEventListener;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.workflow.instance.NodeInstance;
import org.jbpm.workflow.instance.NodeInstanceContainer;
import org.kie.event.process.ProcessNodeTriggeredEvent;

/**
 * As an extended {@link DefaultProcessEventListener} instance, this class keeps track of the last {@link NodeInstance} activated, 
 * and the {@link NodeInstance} activated before the last one. This information is later used by the exception-handling framework 
 * in the following situations:<ol>
 * <li>After an exception is thrown (in order to deactivate the {@link NodeInstance} that threw the exception</li>
 * <li>When retrying the process (and thus the {@link NodeInstance} instance from 1.)</li>
 * </ol>
 */
public class ExceptionProcessEventListener extends DefaultProcessEventListener {

    private ThreadLocal<HashMap<Long, NodeInstanceInfo>> nodeInstanceInfoMap = new ThreadLocal<HashMap<Long,NodeInstanceInfo>>();
    
    /**
     * This class is used to track the information needed to resume the process once the user is ready to do so. 
     */
    static class NodeInstanceInfo { 
        NodeInstance lastNodeInstance;
        NodeInstance previousNodeInstance;
        
        void updateNodeInstanceInfo( NodeInstance newLastNodeInstance, NodeInstanceContainer newLastNodeInstanceContainer) { 
            previousNodeInstance = lastNodeInstance;
            lastNodeInstance = newLastNodeInstance;
        }
        
        public NodeInstanceInfo clone() { 
           NodeInstanceInfo clone = new NodeInstanceInfo();
           clone.lastNodeInstance = this.lastNodeInstance;
           clone.previousNodeInstance = this.previousNodeInstance;
           return clone;
        }
    };
    
    /**
     * This method that uses lazy-initialization to retrieve the NodeInstance info (last and previous) 
     * for a ProcessInstance. This method will return a (new) empty {@link NodeInstanceInfo} instance if none has 
     * been created yet. 
     * 
     * @param processInstanceId The id of the {@link ProcessInstance}.
     * @return The required {@link NodeInstanceInfo} instance. 
     */
    NodeInstanceInfo getNodeInstanceInfo(long processInstanceId) { 
        HashMap<Long, NodeInstanceInfo> map = nodeInstanceInfoMap.get();
        if( map == null ) { 
            map = new HashMap<Long, NodeInstanceInfo>();
            nodeInstanceInfoMap.set(map);
        }
        NodeInstanceInfo info = map.get(processInstanceId);
        if( info == null ) { 
           info = new NodeInstanceInfo();
           map.put(processInstanceId, info);
        }
        return info;
    }
    
    /*
     * (non-Javadoc)
     * @see org.kie.event.ProcessEventListener#beforeNodeTriggered(org.kie.event.process.ProcessNodeTriggeredEvent)
     */
    public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
        NodeInstanceInfo info = getNodeInstanceInfo(event.getProcessInstance().getId());
        
        // Update the NodeInstanceInfo with the most recent NodeInstance triggered
        NodeInstance newLastNodeInstance = (NodeInstance) event.getNodeInstance();
        info.updateNodeInstanceInfo(newLastNodeInstance, (NodeInstanceContainer) newLastNodeInstance.getNodeInstanceContainer());
    }
    
    public void cancelLastActiveNodeInstance(long processInstanceId) { 
        getNodeInstanceInfo(processInstanceId).lastNodeInstance.cancel();
    }

}
