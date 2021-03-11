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

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.QueryHelper;
import org.jbpm.process.audit.command.AuditCommand;
import org.jbpm.query.jpa.builder.impl.AbstractDeleteBuilderImpl;
import org.jbpm.query.jpa.data.QueryWhere;
import org.jbpm.query.jpa.impl.QueryAndParameterAppender;
import org.kie.api.runtime.CommandExecutor;
import org.kie.api.runtime.Context;
import org.kie.internal.query.ParametrizedUpdate;
import org.kie.internal.query.QueryParameterIdentifiers;
import org.kie.internal.runtime.manager.audit.query.AuditDeleteBuilder;

import static org.kie.internal.query.QueryParameterIdentifiers.DATE_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.PROCESS_ID_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.PROCESS_INSTANCE_ID_LIST;

public abstract class AbstractAuditDeleteBuilderImpl<T> extends AbstractDeleteBuilderImpl<T> implements AuditDeleteBuilder<T> {

    protected Integer[] statuses = new Integer[]{2, 3};
    protected String deploymentId;
    protected int recordsPerTransaction;


    protected static class Subquery {

        private String field;
        private String queryBase;
        private int queryParamId;
        private QueryWhere where;

        private QueryAndParameterAppender queryAndParameterAppender;

        public Subquery(String field, String queryBase, int queryParamId) {
            this.field = field;
            this.queryBase = queryBase;
            this.queryParamId = queryParamId;
            this.where = new QueryWhere();
        }

        public Subquery parameter(String listId, Object... values) {
            if (values == null || values.length == 0 || values[0] == null) {
                return this;
            }
            where.setToIntersection();
            where.addParameter(listId, values);
            return this;
        }

        Map<String, Object> getQueryParams() {
            if (queryAndParameterAppender != null) {
                return queryAndParameterAppender.getQueryParams();
            }
            return null;
        }
        public String build() {
            if (queryAndParameterAppender == null) {
                queryAndParameterAppender = QueryHelper.createQuery(queryBase, where, new HashMap<>(), queryParamId);
            }
            return field + " in (" + queryAndParameterAppender.toSQL() + ")";
        }

    }

    protected final CommandExecutor executor; 
    protected final JPAAuditLogService jpaAuditService; 
    
    protected AbstractAuditDeleteBuilderImpl(JPAAuditLogService jpaService) { 
        this.executor = null;
        this.jpaAuditService = jpaService;
    }
    
    protected AbstractAuditDeleteBuilderImpl(CommandExecutor cmdExecutor) { 
        this.executor = cmdExecutor;
        this.jpaAuditService = null;
    }
   
    // service methods
    
    protected JPAAuditLogService getJpaAuditLogService() { 
        JPAAuditLogService jpaAuditLogService = this.jpaAuditService;
        if( jpaAuditLogService == null ) { 
           jpaAuditLogService = this.executor.execute(getJpaAuditLogServiceCommand);
        }
        return jpaAuditLogService;
    }
    
    private AuditCommand<JPAAuditLogService> getJpaAuditLogServiceCommand = new AuditCommand<JPAAuditLogService>() {
        private static final long serialVersionUID = 101L;
        @Override
        public JPAAuditLogService execute( Context context ) {
            setLogEnvironment(context);
            return (JPAAuditLogService) this.auditLogService;
        }
    };

    // query builder methods
    
    @SuppressWarnings("unchecked")
    public T date( Date... date ) {
        if (checkIfNull(date)) {
            return (T) this;
        }
        date = ensureDateNotTimestamp(date);
        addObjectParameter(DATE_LIST, "date", date);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T dateRangeStart( Date rangeStart ) {
        if (checkIfNull(rangeStart)) {
            return (T) this;
        }
        rangeStart = ensureDateNotTimestamp(rangeStart)[0];
        addRangeParameter(DATE_LIST, "date range start", rangeStart, true);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T dateRangeEnd(Date rangeEnd) {
        if (checkIfNull(rangeEnd)) {
            return (T) this;
        }
        rangeEnd = ensureDateNotTimestamp(rangeEnd)[0];
        addRangeParameter(DATE_LIST, "date range end", rangeEnd, false);
        return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T recordsPerTransaction(int numRecords) {
        this.recordsPerTransaction = numRecords;
        return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T processInstanceId(long... processInstanceId) {
        if (checkIfNull(processInstanceId)) {
            return (T) this;
        }
        addLongParameter(PROCESS_INSTANCE_ID_LIST, "process instance id", processInstanceId);
        return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T processId(String... processId) {
        if (checkIfNull(processId)) {
            return (T) this;
        }
        addObjectParameter(PROCESS_ID_LIST, "process id", processId);
        return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T logBelongsToProcessInStatus(Integer... statuses) {
        if (checkIfNull(statuses)) {
            return (T) this;
        }
        this.statuses = statuses;
        return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T logBelongsToProcessInDeployment(String deploymentId) {
        if (checkIfNull(deploymentId)) {
            return (T) this;
        }
        this.deploymentId = deploymentId;
        return (T) this;
    }


    protected <P> boolean checkIfNull(P... parameter) {
    	if( parameter == null ) { 
            return true;
        }
        for( int i = 0; i < parameter.length; ++i ) { 
           if( parameter[i] == null ) { 
        	   return true;
           }
        }
        
        return false;
    }
    
    protected Date[] ensureDateNotTimestamp(Date...date) {
		Date[] validated = new Date[date.length];
		for (int i = 0; i < date.length; ++i) {
			if (date[i] instanceof Timestamp) {
				validated[i] = new Date(date[i].getTime());
			} else {
				validated[i] = date[i];
			}
		}
		
		return validated;
    }
 
    abstract protected Class<?> getQueryType();
    
    protected abstract String getQueryTable();
    
    protected boolean isSubquerySupported() {
        return false;
    }

    protected Subquery getSubQuery() {
        return new Subquery("l.processInstanceId", "SELECT spl.processInstanceId FROM ProcessInstanceLog spl", 1);
    }

    protected Subquery applyParameters(Subquery subquery) {
        return subquery.parameter(QueryParameterIdentifiers.SUBQUERY_STATUS, statuses)
                       .parameter(QueryParameterIdentifiers.SUBQUERY_DEPLOYMENT, deploymentId);
    }

    public ParametrizedUpdate build() {
        return new ParametrizedUpdate() {
            private QueryWhere queryWhere = new QueryWhere(getQueryWhere());
            @Override
            public int execute() {
                Map<String, Object> params = new HashMap<>();
                String subquerySQL = null;
                if (isSubquerySupported()) {
                    Subquery subquery = applyParameters(getSubQuery());
                    subquerySQL = subquery.build();
                    params.putAll(subquery.getQueryParams());
                }
                return recordsPerTransaction <= 0 ? getJpaAuditLogService().doDelete(getQueryTable(), queryWhere,
                        subquerySQL, params) : getJpaAuditLogService().doPartialDelete(getQueryTable(), queryWhere,
                                subquerySQL, params, recordsPerTransaction);

            }
        };
    }
}
