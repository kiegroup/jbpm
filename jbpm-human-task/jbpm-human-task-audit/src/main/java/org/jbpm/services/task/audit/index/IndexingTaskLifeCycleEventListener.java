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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.jbpm.services.task.audit.JPATaskLifeCycleEventListener;
import org.kie.internal.task.api.TaskPersistenceContext;

/**
 * @author Hans Lund
 */
public class IndexingTaskLifeCycleEventListener extends JPATaskLifeCycleEventListener {

    private IndexService service;
    private ExecutorService esc = Executors.newCachedThreadPool();


    public IndexingTaskLifeCycleEventListener(IndexService service) {
        super();
        this.service = service;
    }


    @Override
    public <T> T persist(final TaskPersistenceContext context, final T object) {
        try {
            Future index = esc.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        service.prepare(Arrays.asList(object),null,null);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            Future<T> futureObj = esc.submit(new Callable<T>() {
                @Override
                public T call() throws Exception {
                    return doPersist(context, object);
                }
            });

            index.get();
            T obj = futureObj.get();
            service.commit();
            return obj;
        } catch (Exception e) {
            service.rollback();
            throw new RuntimeException(e);
        }
    }

    public <T> T remove(final TaskPersistenceContext context, final T object) {
        try {
            Future index = esc.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        service.prepare(null,null,Arrays.asList(object));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            Future<T> futureObj = esc.submit(new Callable<T>() {
                @Override
                public T call() throws Exception {
                    return doRemove(context, object);
                }
            });

            index.get();
            T obj = futureObj.get();
            service.commit();
            return obj;
        } catch (Exception e) {
            service.rollback();
            throw new RuntimeException(e);
        }
    }


    private <T> T doPersist(TaskPersistenceContext context, T object) {
        return super.persist(context,object);
    }

    private <T> T doRemove(TaskPersistenceContext context, T object) {
       return super.remove(context,object);
    }


}
