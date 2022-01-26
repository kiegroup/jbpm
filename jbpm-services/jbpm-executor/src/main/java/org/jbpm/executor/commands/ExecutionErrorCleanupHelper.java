/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.jbpm.process.core.timer.DateTimeUtils;
import org.jbpm.shared.services.impl.TransactionalCommandService;
import org.jbpm.shared.services.impl.commands.UpdateStringCommand;
import org.kie.api.executor.CommandContext;
import org.kie.api.runtime.process.ProcessInstance;

final class ExecutionErrorCleanupHelper {

    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    private ExecutionErrorCleanupHelper() {
    }

    static int cleanup(CommandContext ctx, EntityManagerFactory emf) throws ParseException {
        SimpleDateFormat formatToUse;

        String dataFormat = (String) ctx.getData("DateFormat");
        if (dataFormat != null) {
            formatToUse = new SimpleDateFormat(dataFormat);
        } else {
            formatToUse = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        }

        // collect parameters
        String olderThan = (String) ctx.getData("OlderThan");
        String olderThanPeriod = (String) ctx.getData("OlderThanPeriod");
        String forProcess = (String) ctx.getData("ForProcess");
        String forProcessInstance = (String) ctx.getData("ForProcessInstance");
        String forDeployment = (String) ctx.getData("ForDeployment");

        if (olderThanPeriod != null) {
            long olderThanDuration = DateTimeUtils.parseDateAsDuration(olderThanPeriod);
            Date olderThanDate = new Date(System.currentTimeMillis() - olderThanDuration);

            olderThan = formatToUse.format(olderThanDate);
        }
        Map<String, Object> parameters = new HashMap<>();
        StringBuilder cleanUpErrorsQuery = new StringBuilder();

        cleanUpErrorsQuery.append("delete from ExecutionErrorInfo where (processInstanceId in ")
                          .append("(select processInstanceId from ProcessInstanceLog where status in (")
                          .append(ProcessInstance.STATE_COMPLETED)
                          .append(",")
                          .append(ProcessInstance.STATE_ABORTED)
                          .append("))")
                          .append(" or processInstanceId not in (select processInstanceId from ProcessInstanceLog))");

        if (olderThan != null && !olderThan.isEmpty()) {
            cleanUpErrorsQuery.append(" and errorDate < :olderThan");
            parameters.put("olderThan", formatToUse.parse(olderThan));
        }
        if (forProcess != null && !forProcess.isEmpty()) {
            cleanUpErrorsQuery.append(" and processId = :forProcess");
            parameters.put("forProcess", forProcess);
        }
        if (forProcessInstance != null && !forProcessInstance.isEmpty()) {
            cleanUpErrorsQuery.append(" and processInstanceId = :forProcessInstance");
            parameters.put("forProcessInstance", Long.parseLong(forProcessInstance));
        }
        if (forDeployment != null && !forDeployment.isEmpty()) {
            cleanUpErrorsQuery.append(" and deploymentId = :forDeployment");
            parameters.put("forDeployment", forDeployment);
        }

        TransactionalCommandService commandService = new TransactionalCommandService(emf);

        return commandService.execute(new UpdateStringCommand(cleanUpErrorsQuery.toString(), parameters));
    }
}
