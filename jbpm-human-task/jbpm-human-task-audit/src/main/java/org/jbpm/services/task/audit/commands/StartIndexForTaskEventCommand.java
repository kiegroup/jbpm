package org.jbpm.services.task.audit.commands;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.jbpm.services.task.audit.index.IndexService;
import org.jbpm.services.task.utils.ClassUtil;
import org.kie.internal.task.api.TaskPersistenceContext;
import org.kie.internal.task.api.model.TaskEvent;

@XmlRootElement(name="start-index-for-task-event-command")
@XmlAccessorType(XmlAccessType.NONE)
public class StartIndexForTaskEventCommand extends StartIndexCommand<TaskEvent> {


	
	
	private static final long serialVersionUID = -7929370526623674312L;

	public StartIndexForTaskEventCommand() {
	}
	
	public StartIndexForTaskEventCommand(IndexService indexService) {
		super(indexService);
	}
	
	@Override
	protected Collection<TaskEvent> iterate(TaskPersistenceContext context, int offset, int count) {
		return context.queryStringWithParametersInTransaction("select tei from TaskEventImpl tei order by tei.id DESC",
				context.addParametersToMap("firstResult", offset, "maxResults", count),
				ClassUtil.<List<TaskEvent>>castClass(List.class));
	}
}
