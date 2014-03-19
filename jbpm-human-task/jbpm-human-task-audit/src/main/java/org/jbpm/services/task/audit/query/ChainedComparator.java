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
import java.util.Comparator;
import java.util.List;

/**
 * @author Hans Lund
 */
public class ChainedComparator<T> extends QueryComparator<T> {


    private List<QueryComparator<T>> chain;

    public ChainedComparator(QueryComparator<T>... initialChain) {
        super(null,null);
        this.chain = new ArrayList<QueryComparator<T>>();
        chain.addAll(Arrays.asList(initialChain));
    }

    public void add(QueryComparator<T> comparator) {
        chain.add(comparator);
    }


    public int compare(T o1, T o2) {
        if (chain.size() == 0) {
            throw new IllegalStateException(
                "ChainedComparator can't compare with empty chain");
        }
        for (Comparator<T> c : chain) {
            int i = c.compare(o1, o2);
            if (i != 0) {
                return i;
            }
        }
        return 0;
    }

    public QueryComparator[] getComparators() {
        return chain.toArray(new QueryComparator[chain.size()]);
    }
}

