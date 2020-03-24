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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.dashbuilder.dataset.filter.CoreFunctionType;
import org.jbpm.kie.services.impl.model.ProcessInstanceWithVarsDesc;
import org.jbpm.kie.services.impl.model.UserTaskInstanceWithPotOwnerDesc;
import org.jbpm.services.api.query.model.QueryParam;
import org.jbpm.shared.services.impl.QueryManager;
import org.jbpm.shared.services.impl.TransactionalCommandService;
import org.jbpm.shared.services.impl.commands.QueryNameCommand;
import org.kie.api.runtime.query.QueryContext;

import static java.util.Collections.singletonMap;

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


    public List<org.jbpm.services.api.model.ProcessInstanceWithVarsDesc> queryProcessByVariables(List<QueryParam> attributes,
                                                                                                 List<QueryParam> variables,
                                                                                                 int processType,
                                                                                                 String varPrefix,
                                                                                                 QueryContext queryContext) {
        EntityManager entityManager = emf.createEntityManager();

        // first step is to filter the data creating a derived tables and pivoting var - rows to columns (only the variables we are interested to filter)
        StringBuilder derivedTables = new StringBuilder();
        if (!variables.isEmpty()) {
            List<String> conditions = new ArrayList<>();
            variables.stream().forEach(expr -> conditions.add("(A1.variableId = :NAME_" + expr.getColumn() + " AND " + computeExpression(expr, "A1.value", ":VALUE_" + expr.getColumn()) + ")\n"));
            String where = String.join(" OR ", conditions);
            derivedTables.append("INNER JOIN (" +
                                 "SELECT A1.processInstanceId \n" +
                                 "FROM VariableInstanceLog A1 \n" +
                                 "LEFT JOIN VariableInstanceLog A2 ON A1.processId = A2.processId AND A1.processInstanceId = A2.processInstanceId AND A1.variableInstanceId = A2.variableInstanceId AND A2.id > A1.id  \n" +
                                 "WHERE A2.id IS NULL AND (" + where + ") " +
                                 "GROUP BY A1.processInstanceId " +
                                 "HAVING COUNT(*) = :NUMBER_OF_VARS " +
                                 ") TABLE_VAR ON TABLE_VAR.processInstanceId = pil.processInstanceId \n");
        }

        StringBuilder globalWhere = new StringBuilder();
        attributes.stream().forEach(expr -> globalWhere.append(" AND " + computeExpression(expr, expr.getColumn(), ":ATTR_" + expr.getColumn())));

        String procSQLString =
                " SELECT DISTINCT pil.processInstanceId " +
                               " FROM ProcessInstanceLog pil \n " +
                               derivedTables +
                               " WHERE pil.processType = :processType " + globalWhere +
                               " ORDER BY pil.processInstanceId ASC ";

        Query query = entityManager.createNativeQuery(procSQLString);
        variables.stream().forEach(var -> query.setParameter("NAME_" + var.getColumn(), varPrefix + var.getColumn()));
        variables.stream().filter(e -> e.getObjectValue() != null).forEach(var -> query.setParameter("VALUE_" + var.getColumn(), var.getObjectValue()));
        attributes.stream().filter(e -> e.getObjectValue() != null).forEach((entry) -> query.setParameter("ATTR_" + entry.getColumn(), entry.getObjectValue()));

        if (!variables.isEmpty()) {
            query.setParameter("NUMBER_OF_VARS", variables.size());
        }
        query.setParameter("processType", processType);

        addPagination(query, queryContext);
        List<Number> ids = query.getResultList();
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        // now we get the information

        List<Object[]> procRows = commandService.execute(new QueryNameCommand<List<Object[]>>("GetProcessInstanceByIdList", singletonMap(ID_LIST, ids)));
        List<Object[]> varRows = commandService.execute(new QueryNameCommand<List<Object[]>>("GetVariablesByProcessInstanceIdList", singletonMap(ID_LIST, ids)));

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

        entityManager.close();
        return data;
    }

    private String computeExpression(QueryParam expr, String leftOperand, String rightOperand) {
        CoreFunctionType type = CoreFunctionType.getByName(expr.getOperator());
        switch (type) {
            case IS_NULL:
                return leftOperand + " IS NULL ";
            case NOT_NULL:
                return leftOperand + " IS NOT NULL ";
            case IN:
                return leftOperand + " IN (" + rightOperand + ") ";
            case NOT_IN:
                return leftOperand + " NOT IN (" + rightOperand + ") ";
            case EQUALS_TO:
                return leftOperand + " = " + rightOperand + " ";
            case NOT_EQUALS_TO:
                return leftOperand + " <> " + rightOperand + " ";
            case LIKE_TO:
                return leftOperand + " LIKE " + rightOperand + " ";
            default:
                throw new UnsupportedOperationException("Queryparam: " + expr + " not supported");
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

    public List<org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc> queryUserTasksByVariables(List<QueryParam> attributes,
                                                                                                        List<QueryParam> variables,
                                                                                                        List<QueryParam> processVariables,
                                                                                                        List<String> owners,
                                                                                                        int processType,
                                                                                                        String varPrefix,
                                                                                                        QueryContext queryContext) {
        EntityManager entityManager = emf.createEntityManager();
        StringBuilder globalWhere = new StringBuilder();
        StringBuilder derivedTables = new StringBuilder();
        if (!variables.isEmpty()) {
            List<String> conditions = new ArrayList<>();
            variables.stream().forEach((expr) -> conditions.add("(name = :V_NAME_" + expr.getColumn() + " AND " + computeExpression(expr, "value", ":V_VALUE_" + expr.getColumn()) + ")\n"));
            String where = String.join(" OR ", conditions);
            derivedTables.append("INNER JOIN (\n" +
                                 "SELECT taskId \n" +
                                 "FROM TaskVariableImpl \n" +
                                 "WHERE type = 0 AND (" + where + ")\n" +
                                 "GROUP BY taskId \n" +
                                 "HAVING COUNT(*) = :NUMBER_OF_TASKVARS \n" +
                                 ") TABLE_TASK_VAR ON TABLE_TASK_VAR.taskId = task.id  \n");
        }

        if (!processVariables.isEmpty()) {
            List<String> conditions = new ArrayList<>();
            processVariables.stream().forEach((expr) -> conditions.add("(A1.variableId = :P_NAME_" + expr.getColumn() + " AND " + computeExpression(expr, "A1.value", ":P_VALUE_" + expr.getColumn()) + ")\n"));
            String where = String.join(" OR ", conditions);
            derivedTables.append("INNER JOIN (" +
                                 "SELECT A1.processInstanceId \n" +
                                 "FROM VariableInstanceLog A1 \n" +
                                 "LEFT JOIN VariableInstanceLog A2 ON A1.processId = A2.processId AND A1.processInstanceId = A2.processInstanceId AND A1.variableInstanceId = A2.variableInstanceId AND A2.id > A1.id  \n" +
                                 "WHERE A2.id IS NULL AND (" + where + ") " +
                                 "GROUP BY A1.processInstanceId " +
                                 "HAVING COUNT(*) = :NUMBER_OF_PROCVARS " +
                                 ") TABLE_PROC_VAR ON TABLE_PROC_VAR.processInstanceId = pil.processInstanceId \n");
        }

        if (!owners.isEmpty()) {
            derivedTables.append("INNER JOIN ( \n" +
                             "           SELECT DISTINCT po.task_id \n" +
                             "           FROM PeopleAssignments_PotOwners po \n" +
                             "           WHERE po.entity_id IN (:owners) \n" +
                             "           GROUP BY po.task_id \n" +
                             "           HAVING COUNT(po.entity_id) = :num_owners \n" +
                                 ") pot ON pot.task_id = task.id ");
        }

        attributes.stream().forEach((expr) -> globalWhere.append(" AND " + computeExpression(expr, expr.getColumn(), ":ATTR_" + expr.getColumn())));

        String procSQLString = "SELECT DISTINCT task.id " +
                               " FROM Task task " +
                               " INNER JOIN ProcessInstanceLog pil ON pil.processInstanceId = task.processInstanceId \n " +
                               derivedTables +
                               " WHERE  pil.processType = :processType " + globalWhere +
                               " ORDER BY task.id ASC ";

        Query query = entityManager.createNativeQuery(procSQLString);
        variables.stream().forEach(var -> query.setParameter("V_NAME_" + var.getColumn(), var.getColumn()));
        variables.stream().filter(e -> e.getObjectValue() != null).forEach((var) -> query.setParameter("V_VALUE_" + var.getColumn(), var.getObjectValue()));

        if (!variables.isEmpty()) {
            query.setParameter("NUMBER_OF_TASKVARS", variables.size());
        }

        processVariables.stream().forEach(var -> query.setParameter("P_NAME_" + var.getColumn(), varPrefix + var.getColumn()));
        processVariables.stream().filter(e -> e.getObjectValue() != null).forEach(var -> query.setParameter("P_VALUE_" + var.getColumn(), var.getObjectValue()));

        if (!processVariables.isEmpty()) {
            query.setParameter("NUMBER_OF_PROCVARS", processVariables.size());
        }

        if (!owners.isEmpty()) {
            query.setParameter("num_owners", owners.size());
            query.setParameter("owners", owners);
        }

        attributes.stream().filter(e -> e.getObjectValue() != null).forEach(entry -> query.setParameter("ATTR_" + entry.getColumn(), entry.getObjectValue()));
        query.setParameter("processType", processType);

        addPagination(query, queryContext);

        List<Number> ids = query.getResultList();
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        entityManager.close();
        return collectData(ids, varPrefix);

    }

    private List<org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc> collectData(List<Number> ids, String varPrefix) {
        // query data
        List<Object[]> taskRows = commandService.execute(new QueryNameCommand<List<Object[]>>("GetTasksByIdList", singletonMap(ID_LIST, ids)));
        List<Object[]> varRows = commandService.execute(new QueryNameCommand<List<Object[]>>("GetTaskVariablesByTaskIdList", singletonMap(ID_LIST, ids)));
        List<Object[]> potRows = commandService.execute(new QueryNameCommand<List<Object[]>>("GetPotentialOwnersByTaskIdList", singletonMap(ID_LIST, ids)));
        List<Object[]> varProcSQLRows = commandService.execute(new QueryNameCommand<List<Object[]>>("GetProcessVariablesByTaskIdList", singletonMap(ID_LIST, ids)));

        int currentVarIdx = 0;
        int currentPotIdx = 0;
        int currentVarProcIdx = 0;
        List<org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc> data = new ArrayList<>();
        for (Object[] row : taskRows) {
            UserTaskInstanceWithPotOwnerDesc pwv = toUserTaskInstanceWithPotOwnerDesc(row);

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
        return data;
    }

    private UserTaskInstanceWithPotOwnerDesc toUserTaskInstanceWithPotOwnerDesc(Object[] row) {
        return new UserTaskInstanceWithPotOwnerDesc(
                                                    ((Number) row[0]).longValue(), // id
                                                    (String) row[1], // task name
                                                    (String) row[2], // formName
                                                    (String) row[3], // subject
                                                    (String) row[4], // actualOwner_id
                                                    (String) null, // potOwner
                                                    (String) row[5], // correlationKey
                                                    (Date) row[6], // createdOn
                                                    (String) row[7], // createdBy
                                                    (Date) row[8], // expiration time
                                                    (Date) null, // lastModificationDate
                                                    (String) null, // lastModificationUser
                                                    ((Number) row[9]).intValue(), //priority
                                                    ((String) row[10]), // Status
                                                    ((Number) row[11]).longValue(), // processInstanceId
                                                    (String) row[12], // processId
                                                    (String) row[13], // deployment Id
                                                    (String) row[14] // instance description
        );
    }

    protected List<QueryParam> translate(Map<String, String> translationTable, List<QueryParam> attributes) {
        List<QueryParam> translated = new ArrayList<>();
        for (QueryParam entry : attributes) {
            translated.add(new QueryParam(translationTable.get(entry.getColumn()), entry.getOperator(), entry.getValue()));
        }
        return translated;
    }

}
