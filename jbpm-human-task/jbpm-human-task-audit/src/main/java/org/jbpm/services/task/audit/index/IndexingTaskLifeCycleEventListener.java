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
import java.util.Arrays;

import org.jbpm.services.task.audit.JPATaskLifeCycleEventListener;
import org.kie.internal.task.api.TaskPersistenceContext;

/**
 *@author Hans Lund
 */
public class IndexingTaskLifeCycleEventListener extends JPATaskLifeCycleEventListener {


    private IndexService service;

    public IndexingTaskLifeCycleEventListener(IndexService service) {
        super();
        this.service = service;
    }


    @Override
    protected <T> T persist(TaskPersistenceContext context, T object) {
        T obj = super.persist(context,object);
        //how to determine if insert or update
        try {
            service.prepare(Arrays.asList(object),null,null);
            service.commit();
        } catch (IOException e) {
            service.rollback();
            e.printStackTrace();
        }
        return obj;
    }
}
