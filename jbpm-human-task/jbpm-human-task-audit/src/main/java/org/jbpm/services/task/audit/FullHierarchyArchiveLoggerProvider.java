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
import java.util.List;

import javax.persistence.EntityManager;

import org.jbpm.process.audit.ArchiveLoggerProvider;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FullHierarchyArchiveLoggerProvider implements ArchiveLoggerProvider {

    private static final Logger logger = LoggerFactory.getLogger(FullHierarchyArchiveLoggerProvider.class);

    @Override
    public void archive(EntityManager em, ProcessInstanceLog log) {
        // if it is a children we set the plog as null (not archived)
        if(log.getParentProcessInstanceId() != null || log.getParentProcessInstanceId() != -1) {
            log.setEnd(null);
            return;
        }

        // if it is parent
        long pid = log.getProcessInstanceId();
        Date endDate = log.getEnd();

        @SuppressWarnings("unchecked")
        // getting full hierarchy of process instance for this parent process
        List<Number> ids = (List<Number>) em.createNativeQuery("WITH RECURSIVE CTE AS ( \n"
                + "    SELECT parent.*, parent.processInstanceId AS group FROM ProcessInstanceLog parent WHERE parent.parentProcessInstanceId IS NULL OR parent.parentProcessInstanceId = -1\n"
                + "    UNION  ALL\n"
                + "    SELECT child.*, CTE.group AS group FROM ProcessInstanceLog child INNER JOIN CTE ON child.parentProcessInstanceId = CTE.processInstanceId\n"
                + ") \n"
                + "SELECT processInstanceId FROM CTE WHERE CTE.group = :parentProcessInstanceId").setParameter("parentProcessInstanceId", pid).getResultList();

        // one by one all tables regarding the data associated to this. All process of the hierarchy will have the same end date as the parent process instance
        int count = em.createQuery("UPDATE ProcessInstanceLog o SET o.end = :endDate WHERE o.processInstanceId IN (:pid)")
                .setParameter("pid", ids)
                .setParameter("endDate", endDate)
                .executeUpdate();
        logger.info("Archived {} process instances log for process instance id {}", count, ids);

        count = em.createQuery("UPDATE NodeInstanceLog o SET o.end = :endDate WHERE o.processInstanceId IN (:pid)")
                .setParameter("pid", ids)
                .setParameter("endDate", endDate)
                .executeUpdate();

        logger.info("Archived {} node instances log for process instance id {}", count, ids);

        count = em.createQuery("UPDATE VariableInstanceLog o SET o.end = :endDate WHERE o.processInstanceId IN (:pid)")
                .setParameter("pid", pid)
                .setParameter("endDate", endDate)
                .executeUpdate();
        logger.info("Archived {} variable instances log for process instance id {}", count, ids);

        count = em.createQuery("UPDATE AuditTaskImpl o SET o.end = :endDate WHERE o.processInstanceId IN (:pid)")
                .setParameter("pid", pid)
                .setParameter("endDate", endDate)
                .executeUpdate();
        logger.info("Archived {} AuditTaskImpl for process instance id {}", count, ids);

        count = em.createQuery("UPDATE TaskEventImpl o SET o.end = :endDate WHERE o.processInstanceId IN (:pid)")
                .setParameter("pid", pid)
                .setParameter("endDate", endDate)
                .executeUpdate();
        logger.debug("Archived {} TaskEventImpl for process instance id {}", count, ids);

        count = em.createQuery("UPDATE BAMTaskSummaryImpl o SET o.end = :endDate WHERE o.processInstanceId IN (:pid)")
                .setParameter("pid", pid)
                .setParameter("endDate", endDate)
                .executeUpdate();
        logger.info("Archived {} BAMTaskSummaryImpl for process instance id {}", count, ids);
    }

}
