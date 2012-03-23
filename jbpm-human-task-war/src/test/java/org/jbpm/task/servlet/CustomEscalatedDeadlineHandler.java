package org.jbpm.task.servlet;

import javax.persistence.EntityManager;

import org.jbpm.task.Content;
import org.jbpm.task.Deadline;
import org.jbpm.task.Task;
import org.jbpm.task.service.EscalatedDeadlineHandler;
import org.jbpm.task.service.TaskService;

public class CustomEscalatedDeadlineHandler implements EscalatedDeadlineHandler {

	public void executeEscalatedDeadline(Task task, Deadline deadline,
			EntityManager em, TaskService service) {
		// TODO Auto-generated method stub

	}

}
