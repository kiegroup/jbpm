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

package org.jbpm.process.audit.query;

import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.VariableInstanceLog;
import org.kie.api.runtime.CommandExecutor;
import org.kie.internal.runtime.manager.audit.query.VariableInstanceLogDeleteBuilder;

import static org.kie.internal.query.QueryParameterIdentifiers.EXTERNAL_ID_LIST;

public class VarInstanceLogDeleteBuilderImpl extends AbstractAuditDeleteBuilderImpl<VariableInstanceLogDeleteBuilder> implements VariableInstanceLogDeleteBuilder {

    private static final String VARIABLE_INSTANCE_LOG_DELETE = "VariableInstanceLog";
    
    
    public VarInstanceLogDeleteBuilderImpl(CommandExecutor cmdExecutor ) {
        super(cmdExecutor);
        intersect();
    }
  
    public VarInstanceLogDeleteBuilderImpl(JPAAuditLogService jpaAuditService) { 
       super(jpaAuditService);
       intersect();
    }

	@Override
	public VariableInstanceLogDeleteBuilder externalId(String... externalId) {
		if (checkIfNull(externalId) ) { 
			return this;
		}
		addObjectParameter(EXTERNAL_ID_LIST, "external id", externalId);
        return this;
	}

    @Override
    protected Class getQueryType() {
        return VariableInstanceLog.class;
    }

    @Override
    protected String getQueryTable() {
        return VARIABLE_INSTANCE_LOG_DELETE;
    }

    @Override
    protected boolean isSubquerySupported() {
        return true;
    }

}
