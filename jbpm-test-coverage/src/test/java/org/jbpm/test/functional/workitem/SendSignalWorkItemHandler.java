package org.jbpm.test.functional.workitem;

import java.util.Collections;

import org.jbpm.process.core.async.AsyncSignalEventCommand;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutorService;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;


public class SendSignalWorkItemHandler implements WorkItemHandler {


    private RuntimeManager runtimeManager;


    // this should be replace by constructor
    public void setRuntimeManager(RuntimeManager runtimeManager) {
        this.runtimeManager = runtimeManager;
    }

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {

        RuntimeEngine engine = runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get(workItem.getProcessInstanceId()));
        ExecutorService executorService = (ExecutorService) engine.getKieSession().getEnvironment().get("ExecutorService");

        CommandContext ctx = new CommandContext();
        ctx.setData("deploymentId", runtimeManager.getIdentifier());
        ctx.setData("processInstanceId", Long.parseLong((String) workItem.getParameter("ProcessInstanceId")));
        ctx.setData("Signal", "commonSignal");
        ctx.setData("Event", null);
        
        executorService.scheduleRequest(AsyncSignalEventCommand.class.getName(), ctx);
        manager.completeWorkItem(workItem.getId(), Collections.emptyMap());
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        // TODO Auto-generated method stub

    }





}
