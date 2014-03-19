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

package org.jbpm.services.task.audit.index;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.jbpm.services.task.audit.query.Filter;
import org.jbpm.services.task.audit.query.QueryComparator;
import org.jbpm.services.task.audit.query.QueryResult;

/**
 * @author Hans Lund
 */
public interface IndexService {

	void prepare(Collection updates, Collection inserts, Collection deletes)
        throws IOException;

	void commit() throws IOException;

	void rollback();

	void buildIndex(Iterator stored) throws IOException;

	<T> QueryResult<T> find(int offset, int count, QueryComparator<T> comparator,
        Class<T> clazz, Filter<?, ?>... filters)
        throws IOException;
}
