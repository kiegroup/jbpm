/*
 * Copyright 2014 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.services.task.audit.query;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Comparator;

/**
 * @author Hans Lund
 */
public class QueryResult<T> extends AbstractList<T> {

    private Comparator<T> comparator;
    private long offset;
    private Object[] objects;
    private long total;

    public QueryResult(long offset, long total,
        Collection<T> result) {
        this(offset, total, result, null);
    }

    public QueryResult(long offset, long total,
        Collection<T> result, Comparator<T> comparator) {
        super();
        this.offset = offset;
        this.objects = result.toArray(new Object[result.size()]);
        this.total = total;
        this.comparator = comparator;
    }

    public Comparator<T> getComparator() {
        return comparator;
    }

    public long getOffset() {
        return offset;
    }

    public long getTotal() {
        return total;
    }

    @Override
    public int size() {
        return objects.length;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int index) {
        return (T) objects[index];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("QueryResult [comparator=").append(comparator)
            .append(", count=").append(size()).append(", offset=")
            .append(offset).append(", total=").append(total)
            .append(", result=");
        if (objects == null || objects.length == 0) {
            sb.append("null");
        } else {
            for (Object record : objects) {
                sb.append("\n").append(record);
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
