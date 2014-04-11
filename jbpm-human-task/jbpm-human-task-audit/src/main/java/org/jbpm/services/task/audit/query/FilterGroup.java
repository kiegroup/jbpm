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


/**
 * @author Hans Lund
 */
public class FilterGroup<K> extends ObjectFilter<K, Filter> {

    /**
     * Creates a new Filter.
     *
     * @param occurs  sets if matches should occur or not.
     * @param matches sets what is matched on.
     */
    public FilterGroup(Occurs occurs, Filter... matches) {
        super(occurs, null, Filter.class, matches);
    }

    @Override
    public int hashCode() {
        int ret = super.hashCode();
        for (Filter f : matches) {
            ret = (29 * ret + f.hashCode()) + 3;
        }
        return ret;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FilterGroup other = (FilterGroup) o;
        if (this.occurs != other.occurs) {
            return false;
        }
        if (matches.size() != other.matches.size()) {
            return false;
        }
        for (int i = 0; i < matches.size(); i++) {
            if (!matches.get(i).equals(other.matches.get(i))) {
                return false;
            }
        }
        return true;
    }


}
