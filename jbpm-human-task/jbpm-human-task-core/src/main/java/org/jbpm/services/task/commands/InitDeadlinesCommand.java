/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.services.task.commands;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.jbpm.services.task.utils.ClassUtil;
import org.kie.api.runtime.Context;
import org.kie.internal.task.api.TaskDeadlinesService;
import org.kie.internal.task.api.TaskDeadlinesService.DeadlineType;
import org.kie.internal.task.api.TaskPersistenceContext;
import org.kie.internal.task.api.model.DeadlineSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlRootElement(name="init-deadlines-command")
@XmlAccessorType(XmlAccessType.NONE)
public class InitDeadlinesCommand extends TaskCommand<Void> {
	
	private static final long serialVersionUID = -8095766991770311489L;
	private static final Logger logger = LoggerFactory.getLogger(InitDeadlinesCommand.class);
	
	private final String deploymentId;
	private final Class<List<DeadlineSummary>> resultClass;
	
    public InitDeadlinesCommand() {
        this(null);
    }

    public InitDeadlinesCommand(String deploymentId) {
        this.deploymentId = deploymentId;
        resultClass = ClassUtil.<List<DeadlineSummary>> castClass(List.class);
    }

	@Override
	public Void execute(Context context) {
		TaskContext ctx = (TaskContext) context;
		
		TaskPersistenceContext persistenceContext = ctx.getPersistenceContext();
		TaskDeadlinesService deadlineService = ctx.getTaskDeadlinesService();
		
        try {
            processDeadlines("UnescalatedStartDeadlines", DeadlineType.START, persistenceContext, deadlineService);
            processDeadlines("UnescalatedEndDeadlines", DeadlineType.END, persistenceContext, deadlineService);
        } catch (Exception e) {
        	logger.error("Error when executing deadlines", e);
        }
		return null;
	}
	
    private void processDeadlines(String queryName, DeadlineType deadlineType, TaskPersistenceContext persistenceContext, TaskDeadlinesService deadlineService) {
        List<DeadlineSummary> resultList = deploymentId == null ? persistenceContext.queryInTransaction(queryName, resultClass)
                : persistenceContext.queryWithParametersInTransaction(queryName + "ByDeployment", Collections.singletonMap("deploymentId", deploymentId), resultClass);
        long now = System.currentTimeMillis();
        for (DeadlineSummary summary : resultList) {
            deadlineService.schedule(summary.getTaskId(), summary.getDeadlineId(), summary.getDate().getTime() - now, deadlineType);
        }
    }

}
