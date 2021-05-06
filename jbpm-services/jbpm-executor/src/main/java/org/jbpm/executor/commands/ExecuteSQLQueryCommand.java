/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.executor.commands;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.jbpm.process.core.timer.DateTimeUtils;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.jbpm.shared.services.impl.TransactionalCommandService;
import org.jbpm.shared.services.impl.commands.NativeQueryStringCommand;
import org.kie.api.executor.Command;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;
import org.kie.api.executor.Reoccurring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ExecuteSQLQueryCommand implements Command, Reoccurring {

    private static final Logger logger = LoggerFactory.getLogger(ExecuteSQLQueryCommand.class);

    private long nextScheduleTimeAdd = 1 * 60 * 60 * 1000; // one hour in milliseconds

    @Override
    public Date getScheduleTime() {
        if (nextScheduleTimeAdd < 0) {
            return null;
        }

        long current = System.currentTimeMillis();

        Date nextSchedule = new Date(current + nextScheduleTimeAdd);
        logger.debug("Next schedule for job {} is set to {}", this.getClass().getSimpleName(), nextSchedule);

        return nextSchedule;
    }

    @Override
    public ExecutionResults execute(CommandContext ctx) throws Exception {

        computeNextScheduleTime(ctx);
        EntityManagerFactory emf = getEntityManager(ctx);
        TransactionalCommandService commandService = new TransactionalCommandService(emf);

        String sql = (String) ctx.getData("SQL");
        String paramsString = (String) ctx.getData("ParametersList");

        Map<String, Object> parameters = new HashMap<>();
        if(paramsString != null && !paramsString.isEmpty()) {
            String []p = paramsString.split(",");
            Arrays.stream(p).forEach(item -> parameters.put(item, ctx.getData(item))); 
        }

        List<Object> data = commandService.execute(new NativeQueryStringCommand(sql, parameters));

        ExecutionResults executionResults = new ExecutionResults();
        executionResults.setData("size", data.size());

        StringBuilder report = new StringBuilder();
        for(Object item : data) {
            if(item instanceof Object[]) {
                for(Object cell : (Object[]) item) {
                    report.append(cell).append(",");
                }
            } else {
                report.append(item);
            }
            report.append("\n");
        }

        executionResults.setData("data", report.toString());
        return executionResults;
    }

    private EntityManagerFactory getEntityManager(CommandContext ctx) {
        String emfName = (String) ctx.getData("EmfName");
        if (emfName == null) {
            emfName = "org.jbpm.domain";
        }
        return EntityManagerFactoryManager.get().getOrCreate(emfName);
    }

    private void computeNextScheduleTime(CommandContext ctx) {
        String singleRun = (String) ctx.getData("SingleRun");
        if ("true".equalsIgnoreCase(singleRun)) {
            // disable rescheduling
            this.nextScheduleTimeAdd = -1;
        }
        String nextRun = (String) ctx.getData("NextRun");
        if (nextRun != null) {
            nextScheduleTimeAdd = DateTimeUtils.parseDateAsDuration(nextRun);
        }
    }
}
