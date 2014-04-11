package org.jbpm.services.task.audit.service;

import org.jbpm.services.task.audit.commands.StartIndexForGroupAuditTaskCommand;
import org.jbpm.services.task.audit.commands.StartIndexForHistoryAuditTaskCommand;
import org.jbpm.services.task.audit.commands.StartIndexForTaskEventCommand;
import org.jbpm.services.task.audit.commands.StartIndexForUserAuditTaskCommand;
import org.jbpm.services.task.audit.index.IndexService;
import org.kie.internal.task.api.InternalTaskService;

public class TaskIndexThread extends Thread {

	private final InternalTaskService taskService;
	private final IndexService indexService;
	private boolean done = false;

	public TaskIndexThread(InternalTaskService taskService, IndexService indexService) {
		this.taskService = taskService;
		this.indexService = indexService;
	}

	@Override
	public void run() {
    	taskService.execute(new StartIndexForTaskEventCommand(indexService));
    	taskService.execute(new StartIndexForGroupAuditTaskCommand(indexService));
    	taskService.execute(new StartIndexForHistoryAuditTaskCommand(indexService));
    	taskService.execute(new StartIndexForUserAuditTaskCommand(indexService));
    	this.done = true;
	}
	
	public boolean done() {
		return this.done;
	}
}
