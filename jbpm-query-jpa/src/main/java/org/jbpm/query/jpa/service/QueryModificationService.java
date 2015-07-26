/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.query.jpa.service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.jbpm.query.jpa.data.QueryWhere;

public interface QueryModificationService {

    public <T> void addTablesToQuery(QueryWhere queryData, CriteriaQuery<T> query, Class<T> queryType );
    
    public <R,T> void addCriteriaToQuery(QueryWhere queryData, CriteriaQuery<R> query, CriteriaBuilder criteriaBuilder, Class<T> queryType );
}
