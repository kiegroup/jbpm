/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.kie.services.impl;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.jbpm.services.api.query.model.QueryParam.all;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.jbpm.kie.services.impl.model.ProcessInstanceWithVarsDesc;
import org.jbpm.kie.services.impl.model.UserTaskInstanceWithPotOwnerDesc;
import org.jbpm.services.api.query.model.QueryParam;
import org.jbpm.shared.services.impl.QueryManager;
import org.jbpm.shared.services.impl.TransactionalCommandService;
import org.jbpm.shared.services.impl.commands.QueryNameCommand;
import org.kie.api.runtime.query.QueryContext;

public abstract class AbstractAdvanceRuntimeDataServiceImpl {

    private static final String ID_LIST = "idList";
    private EntityManagerFactory emf;
    private TransactionalCommandService commandService;

    public AbstractAdvanceRuntimeDataServiceImpl() {
        QueryManager.get().addNamedQueries("META-INF/Servicesorm.xml");
    }
    public void setCommandService(TransactionalCommandService commandService) {
        this.commandService = commandService;
    }

    public void setEmf(EntityManagerFactory emf) {
        this.emf = emf;
    }

    protected List<org.jbpm.services.api.model.ProcessInstanceWithVarsDesc> queryProcessByVariables(List<QueryParam> attributes,
                                                                                                    List<QueryParam> processVariables,
                                                                                                    int processType,
                                                                                                    String varPrefix,
                                                                                                    QueryContext queryContext) {
        setDefaultSorting (queryContext, "pil.processInstanceId");
        BiFunction<StringBuilder, StringBuilder, String> mainSQLProducer = (derivedTables, globalWhere) -> getSelectFields("pil.processInstanceId", queryContext) +
                                                                                                           " FROM ProcessInstanceLog pil \n " +
                                                                                                           derivedTables +
                                                                                                           " WHERE  pil.processType = :processType " + globalWhere;
        
        return queryProcessUserTasksByVariables(attributes, processVariables, emptyList(), null, processType, varPrefix, queryContext, mainSQLProducer, this::collectProcessData);
    }

    protected List<org.jbpm.services.api.model.ProcessInstanceWithVarsDesc> queryProcessByVariablesAndTask(List<QueryParam> attributes,
                                                                                                                      List<QueryParam> processVariables,
                                                                                                                      List<QueryParam> taskVariables,
                                                                                                                      List<String> owners,
                                                                                                                      int processType,
                                                                                                                      String varPrefix,
                                                                                                                      QueryContext queryContext) {
        return queryProcessByVariablesAndTask(attributes, processVariables, taskVariables, toOwnersQueryParam(owners), processType, varPrefix, queryContext);
    }

    protected List<org.jbpm.services.api.model.ProcessInstanceWithVarsDesc> queryProcessByVariablesAndTask(List<QueryParam> attributes,
                                                                                                                  List<QueryParam> processVariables,
                                                                                                                  List<QueryParam> taskVariables,
                                                                                                                  QueryParam owners,
                                                                                                                  int processType,
                                                                                                                  String varPrefix,
                                                                                                                  QueryContext queryContext) {
        BiFunction<StringBuilder, StringBuilder, String> mainSQLProducer;
        setDefaultSorting (queryContext, "pil.processInstanceId");
        Optional<QueryParam> param = findQueryParamMode(attributes);
        if(param.isPresent() && param.get().getObjectValue().equals("HISTORY")) {
            mainSQLProducer = (derivedTables, globalWhere) -> getSelectFields("pil.processInstanceId", queryContext) +
                                                              " FROM AuditTaskImpl task " +
                                                              " INNER JOIN ProcessInstanceLog pil ON pil.processInstanceId = task.processInstanceId \n " +
                                                              derivedTables +
                                                              " WHERE  pil.processType = :processType " + globalWhere;
        } else {
            mainSQLProducer = (derivedTables, globalWhere) -> getSelectFields("pil.processInstanceId", queryContext) +
                                                              " FROM Task task " +
                                                              " INNER JOIN ProcessInstanceLog pil ON pil.processInstanceId = task.processInstanceId \n " +
                                                              derivedTables +
                                                              " WHERE  pil.processType = :processType " + globalWhere;
        }
        
        return queryProcessUserTasksByVariables(attributes, processVariables, taskVariables, owners, processType, varPrefix, queryContext, mainSQLProducer, this::collectProcessData);

    }

    protected List<org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc> queryUserTasksByVariables(List<QueryParam> attributes,
                                                                                                           List<QueryParam> processVariables,
                                                                                                           List<QueryParam> taskVariables,
                                                                                                           List<String> owners,
                                                                                                           int processType,
                                                                                                           String varPrefix,
                                                                                                           QueryContext queryContext) {

        return queryUserTasksByVariables(attributes, processVariables, taskVariables, toOwnersQueryParam(owners), processType, varPrefix, queryContext);
    }

    private QueryParam toOwnersQueryParam(List<String> owners) {
        if(owners == null || owners.isEmpty()) {
            return null;
        }
        return all(owners);
    }

    protected List<org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc> queryUserTasksByVariables(List<QueryParam> attributes,
                                                                                                           List<QueryParam> processVariables,
                                                                                                           List<QueryParam> taskVariables,
                                                                                                           QueryParam owners,
                                                                                                           int processType,
                                                                                                           String varPrefix,
                                                                                                           QueryContext queryContext) {
        BiFunction<StringBuilder, StringBuilder, String> mainSQLProducer;
        Optional<QueryParam> param = findQueryParamMode(attributes);
        DataCollector<org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc> dataCollector;
        if(param.isPresent() && param.get().getObjectValue().equals("HISTORY")) {
            setDefaultSorting (queryContext, "task.taskId");
            mainSQLProducer = (derivedTables, globalWhere) -> getSelectFields("task.taskId", queryContext) +
                                                              " FROM AuditTaskImpl task " +
                                                              " INNER JOIN ProcessInstanceLog pil ON pil.processInstanceId = task.processInstanceId \n " +
                                                              derivedTables +
                                                              " WHERE  pil.processType = :processType " + globalWhere;
            dataCollector = this::collectHistoryUserTaskData;
            

        } else {
            setDefaultSorting (queryContext, "task.id");
            mainSQLProducer = (derivedTables, globalWhere) -> getSelectFields("task.id", queryContext) +
                                                              " FROM Task task " +
                                                              " INNER JOIN ProcessInstanceLog pil ON pil.processInstanceId = task.processInstanceId \n " +
                                                              derivedTables +
                                                              " WHERE  pil.processType = :processType " + globalWhere;
            dataCollector = this::collectRuntimeUserTaskData;
        }

        return queryProcessUserTasksByVariables(attributes, processVariables, taskVariables, owners, processType, varPrefix, queryContext, mainSQLProducer, dataCollector);
    }
    
    
    protected static String getSelectFields(String idField, QueryContext queryContext) {
        StringBuilder sb = new StringBuilder("SELECT DISTINCT ").append(idField);
        String sortField = queryContext.getOrderBy().trim();
        if (!idField.equalsIgnoreCase(sortField)) {
            int indexOf = idField.indexOf(".");
            if (indexOf == -1 || !idField.substring(indexOf + 1).equalsIgnoreCase(sortField)) {
                sb.append(", ").append(sortField);
            }
        }
        return sb.toString();
    }

    private void setDefaultSorting (QueryContext queryContext, String orderBy) {
        if (queryContext.getOrderBy() == null) {
            queryContext.setOrderBy(orderBy);
            queryContext.setAscending(Boolean.TRUE);
        }
    }

    private Optional<QueryParam> findQueryParamMode(List<QueryParam> params) {
        return findQueryParamByOperator(params, "MODE");
    }

    private Optional<QueryParam> findQueryParamByOperator(List<QueryParam> params, String specialOperator) {
        return params.stream().filter(e -> e.getOperator().equals(specialOperator)).findFirst();
    }

    @FunctionalInterface
    private interface DataCollector<T> {
         List<T> apply(List<Number> idList, String varPrefix, List<QueryParam> params);
    }

    protected <R> List<R> queryProcessUserTasksByVariables(List<QueryParam> attributesArg,
                                                           List<QueryParam> processVariablesArg,
                                                           List<QueryParam> taskVariablesArg,
                                                           QueryParam ownersArg,
                                                           int processType,
                                                           String varPrefix,
                                                           QueryContext queryContext,
                                                           BiFunction<StringBuilder, StringBuilder, String> mainSQLproducer,
                                                           DataCollector<R> dataCollector) {

        List<QueryParam> attributes = new ArrayList<>(attributesArg != null ? attributesArg : emptyList());
        List<QueryParam> processVariables = processVariablesArg != null ? processVariablesArg : emptyList();
        List<QueryParam> taskVariables = taskVariablesArg != null ? taskVariablesArg : emptyList();

        StringBuilder globalWhere = new StringBuilder();
        StringBuilder derivedTables = new StringBuilder();
        if (!taskVariables.isEmpty()) {
            String where = computeVariableExpression(taskVariables, "V", "name", "value");
            derivedTables.append("INNER JOIN (\n" +
                                 "    SELECT taskId \n" +
                                 "    FROM TaskVariableImpl \n" +
                                 "    WHERE " + where + " \n" +
                                 "    GROUP BY taskId \n" +
                                 "    HAVING COUNT(*) >= :NUMBER_OF_TASKVARS \n" +
                                 ") TABLE_TASK_VAR ON TABLE_TASK_VAR.taskId = task.id  \n");
        }

        if (!processVariables.isEmpty()) {
            String where = computeVariableExpression(processVariables, "P", "A1.variableId", "A1.value");
            derivedTables.append("INNER JOIN (" +
                                 "SELECT A1.processInstanceId \n" +
                                 "FROM VariableInstanceLog A1 \n" +
                                 "LEFT JOIN VariableInstanceLog A2 ON A1.processId = A2.processId AND A1.processInstanceId = A2.processInstanceId AND A1.variableInstanceId = A2.variableInstanceId AND A2.id > A1.id  \n" +
                                 "WHERE A2.id IS NULL AND (" + where + ") " +
                                 "GROUP BY A1.processInstanceId " +
                                 "HAVING COUNT(*) = :NUMBER_OF_PROCVARS " +
                                 ") TABLE_PROC_VAR ON TABLE_PROC_VAR.processInstanceId = pil.processInstanceId \n");
        }

        if (ownersArg != null) {
            if (isExclusiveQuery(ownersArg)) {
                derivedTables.append("INNER JOIN ( \n" +
                        "           SELECT DISTINCT po.task_id \n" +
                        "           FROM PeopleAssignments_PotOwners po \n" +
                        "           WHERE po.entity_id IN (:owners) \n" +
                        "           GROUP BY po.task_id \n" +
                        "           HAVING COUNT(po.entity_id) = :num_owners \n" +
                        ") pot ON pot.task_id = task.id ");
            } else {
                derivedTables.append("INNER JOIN ( \n" +
                        "           SELECT DISTINCT po.task_id \n" +
                        "           FROM PeopleAssignments_PotOwners po \n" +
                        "           WHERE po.entity_id IN (:owners) \n" +
                        ") pot ON pot.task_id = task.id ");
            }
        }

        attributes.removeIf(param -> param.getOperator().equals("MODE"));
        attributes.removeIf(param -> param.getOperator().equals("EXCLUDE"));
        attributes.stream().forEach((expr) -> globalWhere.append(" AND " + computeExpression(expr, expr.getColumn(), ":ATTR_" + expr.getColumn())));
        globalWhere.append(" ORDER BY " + queryContext.getOrderBy() + (queryContext.isAscending().booleanValue() ? " ASC" : " DESC"));

        String procSQLString = mainSQLproducer.apply(derivedTables, globalWhere);
        List<Number> ids = emptyList();
        EntityManager entityManager = emf.createEntityManager();
        try {
            Query query = entityManager.createNativeQuery(procSQLString);

            taskVariables.stream().forEach(var -> {
                String nameParam = computeVarNameParameter("V", var.getColumn());
                query.setParameter(nameParam, var.getColumn());
            });

            taskVariables.stream().filter(e -> e.getObjectValue() != null).forEach(var -> {
                String valueParam = computeVarValueParameter(var, "V");
                query.setParameter(valueParam, var.getObjectValue());
            });

            if (!taskVariables.isEmpty()) {
                query.setParameter("NUMBER_OF_TASKVARS", taskVariables.stream().map(QueryParam::getColumn).distinct().count());
            }

            processVariables.stream().forEach(var -> {
                String nameParam = computeVarNameParameter("P", var.getColumn());
                query.setParameter(nameParam, varPrefix + var.getColumn());
            });
            processVariables.stream().filter(e -> e.getObjectValue() != null).forEach(var -> {
                String valueParam = computeVarValueParameter(var, "P");
                query.setParameter(valueParam, var.getObjectValue());
            });

            if (!processVariables.isEmpty()) {
                query.setParameter("NUMBER_OF_PROCVARS", processVariables.stream().map(QueryParam::getColumn).distinct().count());
            }

            if (ownersArg != null) {
                List<String> distinctOwners = ((List<String>) ownersArg.getValue()).stream().distinct().collect(Collectors.toList());
                query.setParameter("owners", distinctOwners);
                if (isExclusiveQuery(ownersArg)) {
                    query.setParameter("num_owners", distinctOwners.size());
                }
            }

            attributes.stream().filter(e -> e.getObjectValue() != null).forEach(entry -> {
                if ("BETWEEN".equals(entry.getOperator())) {
                    for (int i = 0; i < entry.getValue().size(); i++) {
                        query.setParameter("ATTR_" + entry.getColumn() + "_" + i, entry.getValue().get(i));
                    }
                } else {
                    query.setParameter("ATTR_" + entry.getColumn(), entry.getObjectValue());
                }
            });
            query.setParameter("processType", processType);

            addPagination(query, queryContext);

            ids = (List<Number>) query.getResultList().stream().map(this::getId).collect(Collectors.toList());
            if (ids.isEmpty()) {
                return emptyList();
            }
        } finally {
            if (entityManager.isOpen()) {
                entityManager.close();
            }
        }
        return dataCollector.apply(ids, varPrefix, attributesArg != null ? attributesArg : emptyList());
    }

    private Number getId(Object o) {
        return o instanceof Number ? (Number) o : (Number) ((Object[]) o)[0];
    }

    private List<org.jbpm.services.api.model.ProcessInstanceWithVarsDesc> collectProcessData(List<Number> ids, String varPrefix, List<QueryParam> params) {
        Optional<QueryParam> exclude = findQueryParamByOperator(params, "EXCLUDE"); // PROCESS_VARIABLES EXCLUDE 
        boolean excludeProcessVariables = exclude.isPresent() && exclude.get().getColumn().equals("ATTR_COLLECTION_VARIABLES");

        List<Object[]> procRows = commandService.execute(new QueryNameCommand<List<Object[]>>("GetProcessInstanceByIdList", singletonMap(ID_LIST, ids)));
        List<Object[]> varRows = (!excludeProcessVariables) ? commandService.execute(new QueryNameCommand<List<Object[]>>("GetVariablesByProcessInstanceIdList", singletonMap(ID_LIST, ids))) : emptyList();

        int currentVarIdx = 0;
        List<org.jbpm.services.api.model.ProcessInstanceWithVarsDesc> data = new ArrayList<>();
        for (Object[] row : procRows) {
            ProcessInstanceWithVarsDesc pwv = toProcessInstanceWithVarsDesc(row);

            Map<String, Object> vars = new HashMap<>();
            pwv.setVariables(vars);

            Map<String, Object> extra = new HashMap<>();
            pwv.setExtraData(extra);
            while (currentVarIdx < varRows.size() && row[0].equals(varRows.get(currentVarIdx)[0])) {
                String name = (String) varRows.get(currentVarIdx)[1];
                if (!varPrefix.isEmpty() && name.startsWith(varPrefix)) {
                    extra.put(name.substring(varPrefix.length()), varRows.get(currentVarIdx)[2]);
                } else {
                    vars.put(name, varRows.get(currentVarIdx)[2]);
                }
                currentVarIdx++;
            }
            data.add(pwv);
        }
        Collections.sort(data, new ProcessComparator(ids));
        return data;
    }

    private String computeVariableExpression(List<QueryParam> params, String prefix, String varField, String valueField) {
        // we get the variable names
        List<String> vars = params.stream().map(QueryParam::getColumn).distinct().collect(toList());
        List<String> conditions = new ArrayList<>();
        for (String var : vars) {
            StringBuilder condition = new StringBuilder();
            String nameParam = computeVarNameParameter(prefix, var);
            condition.append("(" + varField + " = :" + nameParam);
            // get the conditions for this variables

            List<QueryParam> varParams = params.stream().filter(e -> e.getColumn().equals(var)).collect(toList());
            varParams.stream().forEach(expr -> {
                String valueParam = computeVarValueParameter(expr, prefix);
                condition.append(" AND " + computeExpression(expr, valueField, ":" + valueParam));
            });

            condition.append(")");
            conditions.add(condition.toString());
        }
        return String.join(" OR ", conditions);
    }

    private String computeVarNameParameter(String prefix, String name) {
        return prefix + "_NAME_" + sanitize(name);
    }

    private String computeVarValueParameter(QueryParam expr, String prefix) {
        return prefix + "_VALUE_" + expr.getOperator() + "_" + sanitize(expr.getColumn());
    }

    private String sanitize(String name) {
        return name.replace("-", "_");
    }

    private String computeExpression(QueryParam expr, String leftOperand, String rightOperand) {

        switch (expr.getOperator()) {
            case "IS_NULL":
                return leftOperand + " IS NULL ";
            case "NOT_NULL":
                return leftOperand + " IS NOT NULL ";
            case "IN":
                return leftOperand + " IN (" + rightOperand + ") ";
            case "NOT_IN":
                return leftOperand + " NOT IN (" + rightOperand + ") ";
            case "TYPE":
                return " type = " + rightOperand + " ";
            case "EQUALS_TO":
                return leftOperand + " = " + rightOperand + " ";
            case "NOT_EQUALS_TO":
                return leftOperand + " <> " + rightOperand + " ";
            case "LIKE_TO":
                return leftOperand + " LIKE " + rightOperand + " ";
            case "GREATER_THAN":
                return leftOperand + " > " + rightOperand;
            case "GREATER_OR_EQUALS_TO":
                return leftOperand + " >= " + rightOperand;
            case "LOWER_THAN":
                return leftOperand + " < " + rightOperand;
            case "LOWER_OR_EQUALS_TO":
                return leftOperand + " <= " + rightOperand;
            case "BETWEEN":
                 if(expr.getValue().size() != 2) {
                    throw new IllegalArgumentException("BETWEEN operator requires 2 values. Received: " + expr.getValue().size());
                }
                return leftOperand + " >= " + rightOperand + "_0 AND " + leftOperand + " <= " + rightOperand + "_1";
            default:
                throw new UnsupportedOperationException("Queryparam: " + expr.getOperator() + " not supported");
        }
    }

    private Boolean isExclusiveQuery(QueryParam ownersArg) {
        switch (ownersArg.getOperator()) {
            case "ALL":
                return true;
            case "ANY":
                return false;
            default:
                throw new UnsupportedOperationException("QueryParam Operator for pot-owners: " + ownersArg.getOperator() + " not supported");
        }
    }

    private ProcessInstanceWithVarsDesc toProcessInstanceWithVarsDesc(Object[] row) {
        return new ProcessInstanceWithVarsDesc(((Number) row[0]).longValue(),
                                               (String) row[1],
                                               (String) row[2],
                                               (String) row[3],
                                               ((Number) row[4]).intValue(),
                                               (String) row[5],
                                               (Date) row[6],
                                               (String) row[7],
                                               (String) row[8]);
    }

    private void addPagination(Query query, QueryContext context) {
        if (context.getCount() > 0) {
            query.setFirstResult(context.getOffset());
            query.setMaxResults(context.getCount());
        }
    }

    private List<org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc> collectRuntimeUserTaskData(List<Number> ids, String varPrefix, List<QueryParam> params) {
        return collectUserTaskData("GetTasksByIdList", this::toUserTaskInstanceWithPotOwnerDesc, ids, varPrefix, params);
    }
    
    private List<org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc> collectHistoryUserTaskData(List<Number> ids, String varPrefix,  List<QueryParam> params) {
        return collectUserTaskData("GetHistoryTasksByIdList", this::toHistoryUserTaskInstanceWithPotOwnerDesc, ids, varPrefix, params);
    }

    private List<org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc> collectUserTaskData(String taskRetriever, Function<Object[], UserTaskInstanceWithPotOwnerDesc> mapper, List<Number> ids, String varPrefix, List<QueryParam> params) {
        Optional<QueryParam> exclude = findQueryParamByOperator(params, "EXCLUDE"); // PROCESS_VARIABLES EXCLUDE 
        boolean excludeProcessVariables = exclude.isPresent() && exclude.get().getColumn().equals("ATTR_COLLECTION_VARIABLES");

        // query data
        List<Object[]> taskRows = commandService.execute(new QueryNameCommand<List<Object[]>>(taskRetriever, singletonMap(ID_LIST, ids)));
        List<Object[]> varRows = commandService.execute(new QueryNameCommand<List<Object[]>>("GetTaskVariablesByTaskIdList", singletonMap(ID_LIST, ids)));
        List<Object[]> potRows = commandService.execute(new QueryNameCommand<List<Object[]>>("GetPotentialOwnersByTaskIdList", singletonMap(ID_LIST, ids)));
        List<Object[]> varProcSQLRows = !excludeProcessVariables ? commandService.execute(new QueryNameCommand<List<Object[]>>("GetProcessVariablesByTaskIdList", singletonMap(ID_LIST, ids))) : emptyList();

        int currentVarIdx = 0;
        int currentPotIdx = 0;
        int currentVarProcIdx = 0;
        List<org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc> data = new ArrayList<>();
        for (Object[] row : taskRows) {
            UserTaskInstanceWithPotOwnerDesc pwv = mapper.apply(row);

            while (currentVarIdx < varRows.size() && row[0].equals(varRows.get(currentVarIdx)[0])) {
                if (((Number) varRows.get(currentVarIdx)[1]).intValue() == 0) {
                    pwv.addInputdata((String) varRows.get(currentVarIdx)[2], varRows.get(currentVarIdx)[3]);
                } else {
                    pwv.addOutputdata((String) varRows.get(currentVarIdx)[2], varRows.get(currentVarIdx)[3]);
                }
                currentVarIdx++;
            }

            pwv.getPotentialOwners().clear();
            while (currentPotIdx < potRows.size() && row[0].equals(potRows.get(currentPotIdx)[0])) {
                pwv.addPotOwner((String) potRows.get(currentPotIdx)[1]);
                currentPotIdx++;
            }

            while (currentVarProcIdx < varProcSQLRows.size() && row[0].equals(varProcSQLRows.get(currentVarProcIdx)[0])) {
                String name = (String) varProcSQLRows.get(currentVarProcIdx)[1];
                Object value = varProcSQLRows.get(currentVarProcIdx)[2];
                if (!varPrefix.isEmpty() && name.startsWith(varPrefix)) {
                    pwv.addExtraData(name.substring(varPrefix.length()), value);
                } else {
                    pwv.addProcessVariable(name, value);
                }
                currentVarProcIdx++;
            }

            data.add(pwv);
        }
        Collections.sort(data, new TaskComparator(ids));
        return data;
    }
    
    private abstract static class InMemoryIdComparator<T> implements Comparator<T> {

        private List<Long> ids;

        public InMemoryIdComparator(List<Number> ids) {
            this.ids = ids.stream().map(Number::longValue).collect(Collectors.toList());
        }

        @Override
        public int compare(T o1, T o2) {
            return ids.indexOf(getId(o1)) - ids.indexOf(getId(o2));
        }

        protected abstract Long getId(T object);
    }

    protected static class TaskComparator extends InMemoryIdComparator<org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc> {

        public TaskComparator(List<Number> ids) {
            super(ids);
        }

        protected Long getId(org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc object) {
            return object.getTaskId();
        }
    }

    protected static class ProcessComparator extends InMemoryIdComparator<org.jbpm.services.api.model.ProcessInstanceWithVarsDesc> {

        public ProcessComparator(List<Number> ids) {
            super(ids);
        }

        protected Long getId(org.jbpm.services.api.model.ProcessInstanceWithVarsDesc object) {
            return object.getId();
        }
    }

    private UserTaskInstanceWithPotOwnerDesc toUserTaskInstanceWithPotOwnerDesc(Object[] row) {
        return new UserTaskInstanceWithPotOwnerDesc(
                                                    ((Number) row[0]).longValue(), // id
                                                    (String) row[1], // task name
                                                    (String) row[2], // task description
                                                    (String) row[3], // formName
                                                    (String) row[4], // subject
                                                    (String) row[5], // actualOwner_id
                                                    (String) null, // potOwner
                                                    (String) row[6], // correlationKey
                                                    (Date) row[7], // createdOn
                                                    (String) row[8], // createdBy
                                                    (Date) row[9], // expiration time
                                                    (Date) null, // lastModificationDate
                                                    (String) null, // lastModificationUser
                                                    ((Number) row[10]).intValue(), //priority
                                                    ((String) row[11]), // Status
                                                    ((Number) row[12]).longValue(), // processInstanceId
                                                    (String) row[13], // processId
                                                    (String) row[14], // deployment Id
                                                    (String) row[15] // instance description
        );
    }

    private UserTaskInstanceWithPotOwnerDesc toHistoryUserTaskInstanceWithPotOwnerDesc(Object[] row) {
        return new UserTaskInstanceWithPotOwnerDesc(
                                                    ((Number) row[0]).longValue(), // id
                                                    (String) row[1], // task name
                                                    (String) row[2], // task description
                                                    (String) null, // formName
                                                    (String) null, // subject
                                                    (String) row[3], // actualOwner_id
                                                    (String) null, // potOwner
                                                    (String) row[4], // correlationKey
                                                    (Date) row[5], // createdOn
                                                    (String) row[6], // createdBy
                                                    (Date) null, // expiration time
                                                    (Date) null, // lastModificationDate
                                                    (String) null, // lastModificationUser
                                                    (Integer) null, //priority
                                                    (String) row[7], // Status
                                                    ((Number) row[8]).longValue(), // processInstanceId
                                                    (String) row[9], // processId
                                                    (String) row[10], // deployment Id
                                                    (String) row[11] // instance description
        );
    }

    protected List<QueryParam> translate(Map<String, String> translationTable, List<QueryParam> attributes) {
        if (attributes == null) {
            return emptyList();
        }
        List<QueryParam> translated = new ArrayList<>();
        for (QueryParam entry : attributes) {
            String column = translationTable.get(entry.getColumn());
            
            // small correction for this column as it is not called the same
            if(entry.getColumn() != null && entry.getColumn().equals("TASK_OWNER") && findQueryParamMode(attributes).isPresent()) {
                column = "task.actualOwner";
            }

            // if column is null at this point then we leave the original
            column = column == null ? entry.getColumn() : column;

            translated.add(new QueryParam(column, entry.getOperator(), entry.getValue()));
        }
        return translated;
    }

}
