/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jbpm.query.jpa.data.QueryCriteria;
import org.jbpm.query.jpa.data.QueryWhere;
import org.jbpm.query.jpa.data.QueryWhere.QueryCriteriaType;
import org.jbpm.query.jpa.impl.QueryAndParameterAppender;

import static org.kie.internal.query.QueryParameterIdentifiers.LAST_VARIABLE_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.VAR_VALUE_ID_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.VAR_VAL_SEPARATOR;

public class QueryHelper {

    private final static Map<String, String> criteriaFields = new ConcurrentHashMap<>();
    private final static Map<String, Class<?>> criteriaFieldClasses = new ConcurrentHashMap<>();

    private QueryHelper() {}

    public static void addCriteria(String listId, String fieldName, Class<?> type) {
        criteriaFields.put(listId, fieldName);
        criteriaFieldClasses.put(listId, type);
    }

    public static QueryAndParameterAppender createQueryWithSubQuery(String queryBase, QueryWhere queryWhere, Map<String, Object> queryParams, String subQuery) {
        QueryAndParameterAppender queryAppender = createQuery(queryBase, queryWhere, queryParams, 0);
        if (subQuery != null && !subQuery.isEmpty()) {
            queryAppender.addToQueryBuilder(subQuery, false);
        }
        return queryAppender;
    }

    public static QueryAndParameterAppender createQuery(String queryBase, QueryWhere queryWhere, Map<String, Object> queryParams, int queryParamId) {
        // setup
        QueryAndParameterAppender queryAppender = new QueryAndParameterAppender(new StringBuilder(queryBase), queryParams, queryParamId);

        boolean addLastCriteria = false;
        List<Object[]> varValCriteriaList = new ArrayList<Object[]>();
        List<QueryCriteria> queryWhereCriteriaList = queryWhere.getCriteria();

        // 3. apply normal query parameters
        checkVarValCriteria(queryWhere, varValCriteriaList);

        // last criteria
        Iterator<QueryCriteria> iter = queryWhereCriteriaList.iterator();
        while (iter.hasNext()) {
            QueryCriteria criteria = iter.next();
            if (LAST_VARIABLE_LIST.equals(criteria.getListId())) {
                addLastCriteria = true;
                iter.remove();
            }
        }

        for (QueryCriteria criteria : queryWhere.getCriteria()) {
            String listId = criteria.getListId();
            switch (criteria.getType()) {
                case NORMAL:
                    queryAppender.addQueryParameters(
                                                     criteria.getParameters(),
                                                     listId, criteriaFieldClasses.get(listId), criteriaFields.get(listId),
                                                     criteria.isUnion());
                    break;
                case RANGE:
                    queryAppender.addRangeQueryParameters(
                                                          criteria.getParameters(),
                                                          listId, criteriaFieldClasses.get(listId), criteriaFields.get(listId),
                                                          criteria.isUnion());
                    break;
                case REGEXP:
                    List<String> stringParams = castToStringList(criteria.getParameters());
                    queryAppender.addRegexQueryParameters(
                                                          stringParams,
                                                          listId, criteriaFields.get(listId),
                                                          criteria.isUnion());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown criteria type in delete query builder: " + criteria.getType().toString());
            }
        }

        while (queryAppender.getParenthesesNesting() > 0) {
            queryAppender.closeParentheses();
        }

        // 6. Add special criteria 
        boolean addWhereClause = !queryAppender.hasBeenUsed();
        if (!varValCriteriaList.isEmpty()) {
            addVarValCriteria(addWhereClause, queryAppender, "l", varValCriteriaList);
            addWhereClause = false;
        }
        if (addLastCriteria) {
            addLastInstanceCriteria(queryAppender);
        }

        // 8. return query
        return queryAppender;
    }

    private static List<String> castToStringList(List<Object> objectList) {
        List<String> stringList = new ArrayList<String>(objectList.size());
        for (Object obj : objectList) {
            stringList.add(obj.toString());
        }
        return stringList;
    }

    public static void checkVarValCriteria(QueryWhere queryWhere, List<Object[]> varValCriteriaList) {
        List<QueryCriteria> varValCriteria = new LinkedList<QueryCriteria>();
        Iterator<QueryCriteria> iter = queryWhere.getCriteria().iterator();
        while (iter.hasNext()) {
            QueryCriteria criteria = iter.next();
            if (VAR_VALUE_ID_LIST.equals(criteria.getListId())) {
                varValCriteria.add(criteria);
                iter.remove();
            }
        }
        if (varValCriteria.isEmpty()) {
            return;
        }
        for (QueryCriteria criteria : varValCriteria) {
            for (Object varVal : criteria.getParameters()) {
                String[] parts = ((String) varVal).split(VAR_VAL_SEPARATOR, 2);
                String varId = parts[1].substring(0, Integer.parseInt(parts[0]));
                String val = parts[1].substring(Integer.parseInt(parts[0]) + 1);
                int type = (criteria.isUnion() ? 0 : 1) + (criteria.getType().equals(QueryCriteriaType.REGEXP) ? 2 : 0);
                Object[] varValCrit = {type, varId, val};
                varValCriteriaList.add(varValCrit);
            }
        }
    }

    public static void addVarValCriteria(
                                         boolean addWhereClause,
                                         QueryAndParameterAppender queryAppender,
                                         String tableId,
                                         List<Object[]> varValCriteriaList) {

        // for each var/val criteria
        for (Object[] varValCriteria : varValCriteriaList) {

            boolean union = (((Integer) varValCriteria[0]) % 2 == 0);

            // var id: add query parameter
            String varIdQueryParamName = queryAppender.generateParamName();
            queryAppender.addNamedQueryParam(varIdQueryParamName, varValCriteria[1]);
            // var id: append to the query
            StringBuilder queryPhraseBuilder = new StringBuilder(" ( ")
                                                                       .append(tableId).append(".variableId = :").append(varIdQueryParamName).append(" ");

            // val: append to the query
            queryPhraseBuilder.append("AND ").append(tableId).append(".value ");
            String valQueryParamName = queryAppender.generateParamName();
            String val;
            if (((Integer) varValCriteria[0]) >= 2) {
                val = ((String) varValCriteria[2]).replace('*', '%').replace('.', '_');
                queryPhraseBuilder.append("like :").append(valQueryParamName);
            } else {
                val = (String) varValCriteria[2];
                queryPhraseBuilder.append("= :").append(valQueryParamName);
            }
            queryPhraseBuilder.append(" ) ");

            String[] valArr = {val};
            queryAppender.addToQueryBuilder(queryPhraseBuilder.toString(), union, valQueryParamName, Arrays.asList(valArr));
        }
    }

    private static void addLastInstanceCriteria(QueryAndParameterAppender queryAppender) {
        String lastQueryPhrase = new StringBuilder("(l.id IN ")
                                                               .append("(SELECT MAX(ll.id) FROM VariableInstanceLog ll GROUP BY ll.variableId, ll.processInstanceId)")
                                                               .append(") ").toString();
        queryAppender.addToQueryBuilder(lastQueryPhrase, false);
    }
}
