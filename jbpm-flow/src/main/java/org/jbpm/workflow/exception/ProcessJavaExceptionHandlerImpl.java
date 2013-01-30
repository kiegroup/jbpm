package org.jbpm.workflow.exception;

import org.drools.command.CommandService;
import org.drools.command.runtime.process.*;
import org.drools.exception.JavaExceptionHandler;
import org.jbpm.workflow.exception.ExceptionProcessEventListener.NodeInstanceInfo;
import org.jbpm.workflow.instance.NodeInstance;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.kie.command.Command;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.process.ProcessInstance;
import org.kie.runtime.process.WorkItem;

/**
 * This is the jBPM specific implementation of the {@link JavaExceptionHandler} interface. 
 * </p>
 * This handler does the following in it's {@link JavaExceptionHandler#handleException(CommandService, Throwable)} method:<ol>
 * <li>Trigger the exception-handling process using the {@link StatefulKnowledgeSession#signalEvent(String, Object)} method.</li>
 * <li>Add a new instance of the {@link RetryProcessEventListener} class so that the user can retry the process (starting from the 
 * {@link NodeInstance} which threw the exception).</li>
 * </ol>
 */
public class ProcessJavaExceptionHandlerImpl implements JavaExceptionHandler {

    private WorkflowProcessInstanceImpl processInstance;
    final private long processInstanceId;
    final private String errorEventName; 
    
    final private NodeInstanceInfo info;
    
    public ProcessJavaExceptionHandlerImpl(WorkflowProcessInstanceImpl processInstance, String configuredErrorName,
            ExceptionProcessEventListener exceptionListener) { 
        this.processInstance = processInstance;
        this.processInstanceId = processInstance.getId();
        this.errorEventName = "Error-" + configuredErrorName;
        
        this.info = exceptionListener.getNodeInstanceInfo(processInstanceId).clone();
    }

    /*
     * (non-Javadoc)
     * @see org.drools.exception.JavaExceptionHandler#shouldBeHandled(org.kie.command.Command)
     */
    @Override
    public boolean shouldBeHandled(Command<?> command) {
        return command instanceof AbortWorkItemCommand
                || command instanceof CompleteWorkItemCommand
                || command instanceof SignalEventCommand
                || command instanceof StartProcessCommand
                || command instanceof StartProcessInstanceCommand;
    }

    /*
     * (non-Javadoc)
     * @see org.drools.exception.JavaExceptionHandler#handleException(org.drools.command.CommandService, java.lang.Throwable)
     */
    @Override
    public ProcessInstance handleException(CommandService commandService, Throwable exception) {
        // Signal the exception-handling process
        commandService.execute(new SignalEventCommand( processInstanceId, errorEventName, null ));
        /**
         *   This must be done AFTER the exception-handling event subprocess completes, 
         * otherwise, the WorkflowProcessInstanceImpl.canComplete() will return true 
         * because WorkflowProcessInstanceImpl.nodeInstances.isEmpty() will be true.
         */
        if( info.lastNodeInstance instanceof WorkItemNodeInstance ) { 
            // This ensures that NodeInstance.cancel() deletes the workItem
            ((WorkItemNodeInstance) info.lastNodeInstance).getWorkItem().setState(WorkItem.ACTIVE);
        }
        info.lastNodeInstance.cancel();
          
        // Add a listener so that the user can retry later (if it's not a work item node instance) 
        RetryProcessEventListener retryListener = new RetryProcessEventListener(info.lastNodeInstance,
                                                                                info.previousNodeInstance, 
                                                                                this.processInstance);
        processInstance.addEventListener(ExceptionConstants.RETRY_EVENT_TYPE, retryListener, false);
        
        ProcessInstance result = this.processInstance;
        this.processInstance = null;
        
        return result;
    }

}
