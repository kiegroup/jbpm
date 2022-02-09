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
package org.jbpm.executor.impl.jpa;

import java.util.Date;

import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.query.AbstractAuditDeleteBuilderImpl;
import org.jbpm.runtime.manager.impl.jpa.ExecutionErrorInfo;
import org.kie.internal.runtime.manager.audit.query.ExecutionErrorInfoDeleteBuilder;

import static org.kie.internal.query.QueryParameterIdentifiers.ERROR_DATE_LIST;

public class ExecutionErrorInfoDeleteBuilderImpl extends AbstractAuditDeleteBuilderImpl<ExecutionErrorInfoDeleteBuilder> implements ExecutionErrorInfoDeleteBuilder {

    private static final String EXECUTION_ERROR_INFO_LOG_DELETE = "ExecutionErrorInfo";

    public ExecutionErrorInfoDeleteBuilderImpl(JPAAuditLogService jpaService) {
        super(jpaService);
        intersect();
    }

    @Override
    protected Class<?> getQueryType() {
        return ExecutionErrorInfo.class;
    }

    @Override
    protected String getQueryTable() {
        return EXECUTION_ERROR_INFO_LOG_DELETE;
    }

    @Override
    public ExecutionErrorInfoDeleteBuilder dateRangeEnd(Date rangeEnd) {
        if (checkIfNull(rangeEnd)) {
            return this;
        }
        rangeEnd = ensureDateNotTimestamp(rangeEnd)[0];
        addRangeParameter(ERROR_DATE_LIST, "date range end", rangeEnd, false);
        return this;
    }
}
