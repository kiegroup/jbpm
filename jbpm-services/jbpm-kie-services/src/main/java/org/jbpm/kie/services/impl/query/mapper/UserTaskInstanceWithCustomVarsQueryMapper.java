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

package org.jbpm.kie.services.impl.query.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dashbuilder.dataset.DataSet;
import org.jbpm.services.api.model.UserTaskInstanceWithVarsDesc;
import org.jbpm.services.api.query.QueryResultMapper;

/**
 * Dedicated mapper that transforms data set into UserTaskInstanceWithVarsDesc based on given mapping of custom variables
 *
 */
public class UserTaskInstanceWithCustomVarsQueryMapper extends AbstractQueryMapper<UserTaskInstanceWithVarsDesc> implements QueryResultMapper<List<UserTaskInstanceWithVarsDesc>> {

    private static final long serialVersionUID = 5935133069234696711L;
    
    private Map<String, String> variablesMap = new HashMap<String, String>();
    
    public UserTaskInstanceWithCustomVarsQueryMapper() {
        
    }
    
    public UserTaskInstanceWithCustomVarsQueryMapper(Map<String, String> variablesMap) {
        super();
        this.variablesMap = variablesMap;
    }
    
    public static UserTaskInstanceWithCustomVarsQueryMapper get(Map<String, String> variablesMap) {
        return new UserTaskInstanceWithCustomVarsQueryMapper(variablesMap);
    }
    
    @Override
    public List<UserTaskInstanceWithVarsDesc> map(Object result) {
        if (result instanceof DataSet) {
            DataSet dataSetResult = (DataSet) result;
            List<UserTaskInstanceWithVarsDesc> mappedResult = new ArrayList<UserTaskInstanceWithVarsDesc>();
            
            if (dataSetResult != null) {
                Map<Long, UserTaskInstanceWithVarsDesc> tmp = new HashMap<Long, UserTaskInstanceWithVarsDesc>();
                
                for (int i = 0; i < dataSetResult.getRowCount(); i++) {
                    
                    Long taskId = getColumnLongValue(dataSetResult, COLUMN_TASKID, i);
                    UserTaskInstanceWithVarsDesc ut = tmp.get(taskId);
                    if (ut == null) {

                        ut = buildInstance(dataSetResult, i);
                        mappedResult.add(ut);    
                        
                        tmp.put(taskId, ut);
                    }
                    
                    Map<String, Object> variables = readVariables(variablesMap, dataSetResult, i);
                    ((org.jbpm.kie.services.impl.model.UserTaskInstanceWithVarsDesc) ut).setVariables(variables);
                                    
                }
            }
            
            return mappedResult;
        }
        
        throw new IllegalArgumentException("Unsupported result for mapping " + result);
    }

    @Override
    protected UserTaskInstanceWithVarsDesc buildInstance(DataSet dataSetResult, int index) {
        UserTaskInstanceWithVarsDesc userTask = new org.jbpm.kie.services.impl.model.UserTaskInstanceWithVarsDesc(
                getColumnLongValue(dataSetResult, COLUMN_TASKID, index),//taskId,
                getColumnStringValue(dataSetResult, COLUMN_TASK_STATUS, index),//status,
                getColumnDateValue(dataSetResult, COLUMN_ACTIVATIONTIME, index),//activationTime,
                getColumnStringValue(dataSetResult, COLUMN_NAME, index),//name,
                getColumnStringValue(dataSetResult, COLUMN_DESCRIPTION, index),//description,
                getColumnIntValue(dataSetResult, COLUMN_PRIORITY, index),//priority,
                getColumnStringValue(dataSetResult, COLUMN_ACTUALOWNER, index),//actualOwner,
                getColumnStringValue(dataSetResult, COLUMN_CREATEDBY, index),//createdBy,
                getColumnStringValue(dataSetResult, COLUMN_DEPLOYMENTID, index),//deploymentId,
                getColumnStringValue(dataSetResult, COLUMN_TASK_PROCESSID, index),//processId,
                getColumnLongValue(dataSetResult, COLUMN_TASK_PROCESSINSTANCEID, index),//processInstanceId,
                getColumnDateValue(dataSetResult, COLUMN_CREATEDON, index),//createdOn,
                getColumnDateValue(dataSetResult, COLUMN_DUEDATE, index),//dueDate
                getColumnLongValue(dataSetResult, COLUMN_WORKITEMID, index), //workItemId
                getColumnStringValue(dataSetResult, COLUMN_FORM_NAME, index),//formName
                getColumnStringValue(dataSetResult, COLUMN_SUBJECT, index),//subject
                getColumnStringValue(dataSetResult, COLUMN_CORRELATIONKEY, index),//correlationKey
                getColumnIntValue(dataSetResult, COLUMN_PROCESSTYPE, index),//processType
                getColumnDateValue(dataSetResult, COLUMN_TASK_SLA_DUE_DATE, index),//slaDueDate
                getColumnIntValue(dataSetResult, COLUMN_TASK_SLA_COMPLIANCE, index)//slaCompliance
                );
        return userTask;
    }

    @Override
    public String getName() {
        return "UserTasksWithCustomVariables";
    }

    @Override
    public Class<?> getType() {
        return UserTaskInstanceWithVarsDesc.class;
    }

    @Override
    public QueryResultMapper<List<UserTaskInstanceWithVarsDesc>> forColumnMapping(Map<String, String> columnMapping) {
        return new UserTaskInstanceWithCustomVarsQueryMapper(columnMapping);
    }
}
