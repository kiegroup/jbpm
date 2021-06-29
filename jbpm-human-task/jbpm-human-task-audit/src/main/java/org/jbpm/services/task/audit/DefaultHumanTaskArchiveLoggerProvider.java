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

package org.jbpm.services.task.audit;

import java.util.Date;

import javax.persistence.EntityManager;

import org.jbpm.process.audit.ArchiveLoggerProvider;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultHumanTaskArchiveLoggerProvider implements ArchiveLoggerProvider {

    private static final Logger logger = LoggerFactory.getLogger(DefaultHumanTaskArchiveLoggerProvider.class);

    @Override
    public void archive(EntityManager em, ProcessInstanceLog log) {
        long pid = log.getProcessInstanceId();
        Date endDate = log.getEnd();

        int count = em.createQuery("UPDATE AuditTaskImpl o SET o.end = :endDate WHERE o.processInstanceId = :pid")
                .setParameter("pid", pid)
                .setParameter("endDate", endDate)
                .executeUpdate();
        logger.debug("Archived {} AuditTaskImpl for process instance id {}", count, pid);

        count = em.createQuery("UPDATE TaskEventImpl o SET o.end = :endDate WHERE o.processInstanceId = :pid")
                .setParameter("pid", pid)
                .setParameter("endDate", endDate)
                .executeUpdate();
        logger.debug("Archived {} TaskEventImpl for process instance id {}", count, pid);

        count = em.createQuery("UPDATE BAMTaskSummaryImpl o SET o.end = :endDate WHERE o.processInstanceId = :pid")
                .setParameter("pid", pid)
                .setParameter("endDate", endDate)
                .executeUpdate();
        logger.debug("Archived {} BAMTaskSummaryImpl for process instance id {}", count, pid);
    }

}
