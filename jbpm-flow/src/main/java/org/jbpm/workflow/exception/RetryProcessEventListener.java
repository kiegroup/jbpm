package org.jbpm.workflow.exception;

import java.util.List;
import java.util.Map;

import org.jbpm.workflow.core.impl.NodeImpl;
import org.jbpm.workflow.instance.NodeInstance;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.kie.definition.process.Connection;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.process.EventListener;
import org.kie.runtime.process.ProcessInstance;

/**
 * This {@link EventListener} implementation is used when the user wants to retry the process after an exception 
 * was thrown and caught. Users can retry processes that have thrown exceptions by using the 
 * {@link StatefulKnowledgeSession#signalEvent(String, Object, long)} method. When using that method, the parameters should
 * be the following, in the following order:<ol>
 * <li>{@ ExceptionConstants#RESTART_EVENT_TYPE}</li>
 * <li>The {@link ProcessInstance#getId()}</li>
 * <li>The {@link ProcessInstance#getId()}</li>
 * </ol>
 * </p>
 * An instance of this class is created and added as an (internal) event listener to the {@link WorkflowProcessInstance} instance
 * when the {@link ProcessJavaExceptionHandlerImpl#handleException(org.drools.command.CommandService, Throwable)} method is run. 
 * </p>
 * This listener is meant to be used once and only once: when the {@link EventListener#signalEvent(String, Object)} method fires,
 * it triggers the {@link NodeInstance} that threw the exception, after first removing itself as an {@link EventListener} from the
 * {@link WorkflowProcessInstance} instance. 
 */
public class RetryProcessEventListener implements EventListener {

    private NodeInstance lastNodeInstance;
    private NodeInstance previousNodeInstance;
    private WorkflowProcessInstance processInstance;
    
    public RetryProcessEventListener(NodeInstance lastNodeInstance, NodeInstance previousNodeInstance, WorkflowProcessInstance processInstance) {
        this.lastNodeInstance = lastNodeInstance;
        this.previousNodeInstance = previousNodeInstance;
        this.processInstance = processInstance;
    }
    
    /*
     * (non-Javadoc)
     * @see org.kie.runtime.process.EventListener#signalEvent(java.lang.String, java.lang.Object)
     */
    @Override
    public void signalEvent(String type, Object event) {
        
        // Clean up listener, it's only meant to be used once. 
        NodeInstance previousNodeInstance = this.previousNodeInstance;
        this.previousNodeInstance = null;
        NodeInstance lastNodeInstance = this.lastNodeInstance;
        this.lastNodeInstance = null;
        
        if( previousNodeInstance == null ) { 
            ((WorkflowProcessInstanceImpl) lastNodeInstance.getProcessInstance()).start();
        } else { 
            Map<String, List<Connection>> incomingConnections 
                = ((NodeImpl) lastNodeInstance.getNode()).getIncomingConnections();

            String toType = null;
            INCOMING_CONNECTIONS_LOOP:
            for( String connectionType : incomingConnections.keySet() ) { 
                for( Connection connection : incomingConnections.get(connectionType) ) { 
                   if( connection.getFrom() == previousNodeInstance.getNode()
                       && connection.getTo() == lastNodeInstance.getNode() ) { 
                       toType = connection.getToType();
                       break INCOMING_CONNECTIONS_LOOP;
                   }
                }
            }
            
            // This event listener is for one time use!
            this.processInstance.removeEventListener(ExceptionConstants.RETRY_EVENT_TYPE, this, false);
            this.processInstance = null;
            
            // Trigger the node that failed
            lastNodeInstance.trigger(previousNodeInstance, toType);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.kie.runtime.process.EventListener#getEventTypes()
     */
    @Override
    public String[] getEventTypes() {
        String [] eventTypes = { ExceptionConstants.RETRY_EVENT_TYPE };
        return eventTypes;
    }

}
