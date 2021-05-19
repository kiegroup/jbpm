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

package org.jbpm.services.api.query.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * Basic data carrier to provide filtering capabilities on top of query definition.
 *
 */
public class QueryParam implements Serializable {
    


    private static final long serialVersionUID = -7751811350486978746L;
    
    public static final String MILLISECOND = "MILLISECOND";
    public static final String HUNDRETH = "HUNDRETH";
    public static final String TENTH = "TENTH";
    public static final String SECOND = "SECOND";
    public static final String MINUTE = "MINUTE";
    public static final String HOUR = "HOUR";
    public static final String DAY = "DAY";
    public static final String DAY_OF_WEEK = "DAY_OF_WEEK";
    public static final String WEEK = "WEEK";
    public static final String MONTH = "MONTH";
    public static final String QUARTER = "QUARTER";
    public static final String YEAR = "YEAR";
    public static final String DECADE = "DECADE";
    public static final String CENTURY = "CENTURY";
    public static final String MILLENIUM = "MILLENIUM";
    
    private String column;
    private String operator;
    private List<?> value;
    
    public QueryParam(String column, String operator, List<?> value) {
        this.column = column;
        this.operator = operator;
        this.value = value;
    }

    /**
     * Returns "is null" query parameter for given column.
     * @param column
     * @return @{@link QueryParam} query parameters
     */
    public static QueryParam isNull(String column) {
        return new QueryParam(column, "IS_NULL", null);
    }

    /**
     * Returns "is not null" query parameter for given column.
     * @param column
     * @return @{@link QueryParam} query parameter
     */
    public static QueryParam isNotNull(String column) {
        return new QueryParam(column, "NOT_NULL", null);
    }

    /**
     * Returns "equal" query parameter for given column and set of values.
     * @param column
     * @param values
     * @return @{@link QueryParam} query parameter
     */
    public static QueryParam equalsTo(String column, Comparable<?>...values) {
        return new QueryParam(column, "EQUALS_TO", Arrays.asList(values));
    }

    /**
     * Returns the "not equals to" query parameter for given column and set of values
     * @param column
     * @param values
     * @return @{@link QueryParam} query parameter
     */
    public static QueryParam notEqualsTo(String column, Comparable<?>...values) {
        return new QueryParam(column, "NOT_EQUALS_TO", Arrays.asList(values));
    }

    /**
     * Returns the "like to" query parameter for given column, case sensitivity and set of values.
     * @param column
     * @param caseSensitive
     * @param value
     * @return @{@link QueryParam} query param
     */
    @SuppressWarnings("unchecked")
    public static QueryParam likeTo(String column, boolean caseSensitive, Comparable<?> value) {
        return new QueryParam(column, "LIKE_TO", Arrays.asList(value, caseSensitive));
    }

    /**
     * Returns the "greater than" query parameter for given column and set of values.
     * @param column
     * @param value
     * @return @{@link QueryParam} query param
     */
    @SuppressWarnings("unchecked")
    public static QueryParam greaterThan(String column, Comparable<?> value) {
        return new QueryParam(column, "GREATER_THAN", Arrays.asList(value));
    }

    /**
     * Returns the "greater than or equal" query parameter for given column and set of values.
     * @param column
     * @param value
     * @return @{@link QueryParam} query param
     */
    @SuppressWarnings("unchecked")
    public static QueryParam greaterOrEqualTo(String column, Comparable<?> value) {
        return new QueryParam(column, "GREATER_OR_EQUALS_TO", Arrays.asList(value));
    }

    /**
     * Returns the "lower than" query parameter for given column and set of values.
     * @param column
     * @param value
     * @return @{@link QueryParam} query param
     */
    @SuppressWarnings("unchecked")
    public static QueryParam lowerThan(String column, Comparable<?> value) {
        return new QueryParam(column, "LOWER_THAN", Arrays.asList(value));
    }

    /**
     * Returns the "loqer or equal to" query parameter for given column and set of values.
     * @param column
     * @param value
     * @return @{@link QueryParam} query param
     */
    @SuppressWarnings("unchecked")
    public static QueryParam lowerOrEqualTo(String column, Comparable<?> value) {
        return new QueryParam(column, "LOWER_OR_EQUALS_TO", Arrays.asList(value));
    }

    /**
     * Returns the "between" query parameter for given column, start and end.
     * @param column
     * @param start
     * @param end
     * @return @{@link QueryParam} query param
     */
    @SuppressWarnings("unchecked")
    public static QueryParam between(String column, Comparable<?> start, Comparable<?> end) {
        return new QueryParam(column, "BETWEEN", Arrays.asList(start, end));
    }

    /**
     * Returns the "in" query parameter for given column and set of values.
     * @param column
     * @param values
     * @return @{@link QueryParam} query param
     */
    public static QueryParam in(String column, List<?> values) {
        return new QueryParam(column, "IN", values);
    }

    public static QueryParam in(String column, Object... values) {
        return new QueryParam(column, "IN", Arrays.asList(values));
    }

    /**
     * Returns the "not in" query parameter for given column and set of values.
     * @param column
     * @param values
     * @return @{@link QueryParam} query param
     */
    public static QueryParam notIn(String column, List<?> values) {
        return new QueryParam(column, "NOT_IN", values);
    }

    public static QueryParam notIn(String column, Object... values) {
        return new QueryParam(column, "NOT_IN", Arrays.asList(values));
    }

    /**
     * Returns the "count" query parameter for given column.
     * @param column
     * @return @{@link QueryParam} query param
     */
    public static QueryParam count(String column) {
        return new QueryParam(column, "COUNT", Arrays.asList(column));
    }

    /**
     * Returns the "distinct" query parameter for given column.
     * @param column
     * @return @{@link QueryParam} query param
     */
    public static QueryParam distinct(String column) {
        return new QueryParam(column, "DISTINCT", Arrays.asList(column));
    }

    /**
     * Returns the "average" query parameter for given column.
     * @param column
     * @return @{@link QueryParam} query param
     */
    public static QueryParam average(String column) {
        return new QueryParam(column, "AVERAGE", Arrays.asList(column));
    }

    /**
     * Returns the "sum" query parameter for given column.
     * @param column
     * @return @{@link QueryParam} query param
     */
    public static QueryParam sum(String column) {
        return new QueryParam(column, "SUM", Arrays.asList(column));
    }

    /**
     * Returns the "min" query parameter for given column.
     * @param column
     * @return @{@link QueryParam} query param
     */
    public static QueryParam min(String column) {
        return new QueryParam(column, "MIN", Arrays.asList(column));
    }

    /**
     * Returns the "max" query parameter for given column.
     * @param column
     * @return @{@link QueryParam} query param
     */
    public static QueryParam max(String column) {
        return new QueryParam(column, "MAX", Arrays.asList(column));
    }

    /**
     * Returns the "group by" query parameters for given column.
     * @param column
     * @return @{@link QueryParam} query params
     */
    public static QueryParam[] groupBy(String column) {
        return new QueryParam[] {new QueryParam(column, "group", Arrays.asList(column)), new QueryParam(column, null, Arrays.asList(column))};
    }

    /**
     * Returns the "group by" query parameters for given column, interval size and max interval.
     * @param column
     * @param intervalSize
     * @param maxInterval
     * @return @{@link QueryParam} query params
     */
    public static QueryParam[] groupBy(String column, String intervalSize, int maxInterval) {
        return new QueryParam[] {new QueryParam(column, "group", Arrays.asList(column, intervalSize, maxInterval)), new QueryParam(column, null, Arrays.asList(column))};
    }

    public static QueryParam type(String column, Comparable<?> type) {
        return new QueryParam(column, "TYPE", Arrays.asList(type));
    }

    public static QueryParam history() {
        return new QueryParam("TABLE", "MODE", singletonList("HISTORY"));
    }

    public static QueryParam exclude(String collection) {
        return new QueryParam(collection, "EXCLUDE", emptyList());
    }

    public static QueryParam all(List<?> values) {
        return new QueryParam(null, "ALL", values == null ? emptyList() : values);
    }

    public static QueryParam any(List<?> values) {
        return new QueryParam(null, "ANY", values == null ? emptyList() : values);
    }

    /**
     * Returns the column.
     * @return column
     */
    public String getColumn() {
        return column;
    }

    /**
     * Sets the column.
     * @param column
     */
    public void setColumn(String column) {
        this.column = column;
    }

    /**
     * Returns the operator.
     * @return operator
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Sets the operator.
     * @param operator
     */
    public void setOperator(String operator) {
        this.operator = operator;
    }

    /**
     * Returns list of values.
     * @return values
     */
    public List<?> getValue() {
        return value;
    }

    /**
     * Sets the value.
     * @param value
     */
    public void setValue(List<?> value) {
        this.value = value;
    }

    public Object getObjectValue() {
        if (value == null || value.isEmpty()) {
            return null;
        }
        switch (type()) {
            case BINARY_OPERAND:
                return value.get(0);
            case RANGE_OPERAND:
                return value.subList(0, 2);
            case UNARY_OPERAND:
            case AGGREGATE:
                return null;
            case LIST_OPERAND:
            default:
                return value;
        }

    }

    private enum Type {
        DEFAULT,
        AGGREGATE,
        UNARY_OPERAND,
        BINARY_OPERAND,
        RANGE_OPERAND,
        LIST_OPERAND
    }

    private Type type() {
        switch (operator) {
            case "IS_NULL":
            case "NOT_NULL":
            case "DISTINCT":
                return Type.UNARY_OPERAND;
            case "MIN":
            case "MAX":
            case "SUM":
            case "AVERAGE":
            case "COUNT":
                return Type.AGGREGATE;
            case "EQUALS_TO":
            case "NOT_EQUALS_TO":
            case "LIKE_TO":
            case "GREATER_THAN":
            case "LESS_THAN":
            case "GREATER_OR_EQUALS_TO":
            case "LESS_OR_EQUALS_TO":
            case "TYPE":
            case "MODE":
                return Type.BINARY_OPERAND;
            case "BETWEEN":
                return Type.RANGE_OPERAND;
            case "IN":
            case "NOT_IN":
                return Type.LIST_OPERAND;
            default:
                return Type.DEFAULT;
        }
    }

    public static List<QueryParam> list(QueryParam... params) {
        return Arrays.asList(new Builder().append(params).get());
    }
    /**
     * Returns the builder.
     * @return @{@link Builder} builder
     */
    public static Builder getBuilder() {
        return new Builder();
    }

    /**
     * Query Parameter Builder.
     */
    public static class Builder {
        private List<QueryParam> parameters = new ArrayList<QueryParam>();

        /**
         * Appends new query parameters to existing ones.
         * @param params
         * @return
         */
        public Builder append(QueryParam...params) {
            this.parameters.addAll(Arrays.asList(params));
            
            return this;
        }

        /**
         * Returns query parameters.
         * @return query parameters
         */
        public QueryParam[] get() {
            return this.parameters.toArray(new QueryParam[this.parameters.size()]);
        }
    }

}
