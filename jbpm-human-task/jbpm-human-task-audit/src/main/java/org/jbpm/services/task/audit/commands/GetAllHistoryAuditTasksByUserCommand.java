package org.jbpm.services.task.audit.commands;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.jbpm.services.task.audit.impl.model.api.HistoryAuditTask;
import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.utils.ClassUtil;
import org.kie.internal.command.Context;
import org.kie.internal.task.api.TaskContext;
import org.kie.internal.task.api.TaskPersistenceContext;

@XmlRootElement(name="get-all-history-by-user-audit-tasks-command")
@XmlAccessorType(XmlAccessType.NONE)
public class GetAllHistoryAuditTasksByUserCommand extends TaskCommand<List<HistoryAuditTask>> {

        private String owner;
	public GetAllHistoryAuditTasksByUserCommand() {
	}
        
        public GetAllHistoryAuditTasksByUserCommand(String owner) {
            this.owner = owner;
	}

	@Override
	public List<HistoryAuditTask> execute(Context context) {
		TaskPersistenceContext persistenceContext = ((TaskContext) context).getPersistenceContext();
		return persistenceContext.queryWithParametersInTransaction("getAllHistoryAuditTasksByUser", 
                                persistenceContext.addParametersToMap("owner", owner),
				ClassUtil.<List<HistoryAuditTask>>castClass(List.class));
	}

}
