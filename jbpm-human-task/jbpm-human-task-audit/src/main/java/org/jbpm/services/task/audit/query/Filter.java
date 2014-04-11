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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Hans Lund
 */
public abstract class Filter<K, T>  {

    public enum Occurs {
        MUST, SHOULD, NOT
    }

    protected List<T> matches;

    protected Occurs occurs;
    protected String field;

    public Filter(Occurs occurs, String field, T... matches) {
        this.occurs = occurs;
        this.field = field;
        if (matches != null && matches.length > 0) {
            this.matches = new ArrayList<T>(Arrays.asList(matches));
        } else {
            this.matches = new ArrayList<T>();
        }
    }

    public boolean isInFilter(K object) {
        return true;
    }

    public boolean matches(Object value) {
        for (T t : getMatches()) {
            if (t.equals(value)) {
                return true;
            }
        }
        return false;
    }

    public Occurs getOccurs() {
        return occurs;
    }

    public String getField() {
        return field;
    }

    public boolean isSingle() {
        return matches.size() == 1;
    }

    public boolean add(T match) {
        return match != null && matches.add(match);
    }

    public abstract T[] getMatches();


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Filter filter = (Filter) o;

        if (field != null ? !field.equals(filter.field)
            : filter.field != null) {
            return false;
        }
        if (matches != null ? matches.size() == filter.matches.size()
            : filter.matches != null) {
            return false;
        }
        return occurs == filter.occurs;

    }

    @Override
    public int hashCode() {
        int result = matches != null ? matches.size() : 0;
        result = 31 * result + (occurs != null ? occurs.hashCode() : 0);
        result = 31 * result + (field != null ? field.hashCode() : 0);
        return result;
    }
}
