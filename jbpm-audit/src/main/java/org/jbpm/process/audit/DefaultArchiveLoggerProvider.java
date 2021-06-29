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

package org.jbpm.process.audit;

import java.util.Date;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultArchiveLoggerProvider implements ArchiveLoggerProvider {

    private static final Logger logger = LoggerFactory.getLogger(DefaultArchiveLoggerProvider.class);

    @Override
    public void archive(EntityManager em, ProcessInstanceLog log) {
        long pid = log.getProcessInstanceId();
        Date endDate = log.getEnd();

        int count = em.createQuery("UPDATE NodeInstanceLog o SET o.end = :endDate WHERE o.processInstanceId = :pid")
                .setParameter("pid", pid)
                .setParameter("endDate", endDate)
                .executeUpdate();

        logger.debug("Archived {} node instances log for process instance id {}", count, pid);

        count = em.createQuery("UPDATE VariableInstanceLog o SET o.end = :endDate WHERE o.processInstanceId = :pid")
                .setParameter("pid", pid)
                .setParameter("endDate", endDate)
                .executeUpdate();
        logger.debug("Archived {} variable instances log for process instance id {}", count, pid);
    }

}
