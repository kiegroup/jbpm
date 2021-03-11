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

package org.jbpm.casemgmt.impl.audit;

import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.query.AbstractAuditDeleteBuilderImpl;
import org.kie.api.runtime.CommandExecutor;
import org.kie.internal.query.QueryParameterIdentifiers;

public class CaseRoleAssignmentLogDeleteBuilderImpl extends AbstractAuditDeleteBuilderImpl<CaseRoleAssignmentLogDeleteBuilder> implements CaseRoleAssignmentLogDeleteBuilder {

    private final static String CASE_ROLE_ASSIGMENT_LOG_DELETE = "CaseRoleAssignmentLog";

    protected String caseDefId;

    public CaseRoleAssignmentLogDeleteBuilderImpl(JPAAuditLogService jpaService) {
        super(jpaService);
        intersect();
    }

    public CaseRoleAssignmentLogDeleteBuilderImpl(CommandExecutor cmdExecutor) {
        super(cmdExecutor);
        intersect();
    }

    @Override
    protected boolean isSubquerySupported() {
        return true;
    }

    @Override
    protected Subquery getSubQuery() {
        return new Subquery("l.caseId", "select spl.correlationKey FROM ProcessInstanceLog spl", 1);
    }

    @Override
    protected Subquery applyParameters(Subquery subquery) {
        Subquery query = super.applyParameters(subquery);
        query.parameter(QueryParameterIdentifiers.SUBQUERY_CASE, caseDefId);
        return query;
    }

    @Override
    public CaseRoleAssignmentLogDeleteBuilder inCaseDefId(String caseDefId) {
        if (checkIfNull(caseDefId)) {
            return this;
        }
        this.caseDefId = caseDefId;
        return this;
    }

    @Override
    protected Class<?> getQueryType() {
        return CaseRoleAssignmentLog.class;
    }

    @Override
    protected String getQueryTable() {
        return CASE_ROLE_ASSIGMENT_LOG_DELETE;
    }

}
