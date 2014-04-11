package org.jbpm.services.task.audit.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.jbpm.services.task.audit.index.IndexService;
import org.jbpm.services.task.commands.TaskCommand;
import org.kie.internal.command.Context;
import org.kie.internal.task.api.TaskContext;
import org.kie.internal.task.api.TaskPersistenceContext;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class StartIndexCommand<T> extends TaskCommand<Void> {

	private static final long serialVersionUID = -7929370526623674312L;

	private IndexService indexService;
	
	public StartIndexCommand() {
	}
	
	public StartIndexCommand(IndexService indexService) {
		this.indexService = indexService;
	}
	
	public IndexService getIndexService() {
		return indexService;
	}
	
	public void setIndexService(IndexService indexService) {
		this.indexService = indexService;
	}
	
	@Override
	public Void execute(Context context) {
		TaskPersistenceContext persistenceContext = ((TaskContext) context).getPersistenceContext();
		try {
			int offset = 0;
			int count = getCount();
			Collection<T> iter = new ArrayList<T>();
			do  {
				iter = iterate(persistenceContext, offset, count);
				indexService.prepare(null, iter, null);
				offset += iter.size();
			} while (iter.size() == count);
			indexService.commit();
		} catch (IOException e) {
			throw new RuntimeException("Problem at index startup for type " + getClass().getName(), e);
		}
		return null;
	}

	protected abstract Collection<T> iterate(TaskPersistenceContext context, int offset, int count);
	
	protected int getCount() {
		return 500;
	}
}
