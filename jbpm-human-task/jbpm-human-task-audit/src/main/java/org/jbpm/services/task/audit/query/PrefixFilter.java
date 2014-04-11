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
public class PrefixFilter<K> extends WildCardFilter<K> {

    public PrefixFilter(Occurs occurs, String field, String... matches) {
        super(occurs, field);
        for (String term : matches) {
            super.add(escape(term) + WildCardFilter.DEFAULT_GENERIC_WILDCARD);
        }
    }
}
