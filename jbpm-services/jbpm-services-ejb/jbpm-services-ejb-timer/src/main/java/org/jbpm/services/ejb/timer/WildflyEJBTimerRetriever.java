/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.services.ejb.timer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ejb.TimerService;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.jbpm.process.core.timer.impl.GlobalTimerService;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WildflyEJBTimerRetriever extends EJBTimerRetriever<WildflyAcceptedInfo> {

    private static final Logger logger = LoggerFactory.getLogger(WildflyEJBTimerRetriever.class);
    private final boolean isPersistentWildfly;
    private Object persistence;
    
    
    protected WildflyEJBTimerRetriever(TimerService timerService) {
        super(timerService);
        isPersistentWildfly = isPersistentWildfly();
        if (isPersistentWildfly) {
            logger.info ("EJBTimer is using Wildfly persistence");
        }  
    }

    @SuppressWarnings({"squid:S1872","squid:S3011"})
    private boolean isPersistentWildfly() {
        Class<? extends TimerService> clazz = timerService.getClass();
        if (clazz.getName().equals("org.jboss.as.ejb3.timerservice.TimerServiceImpl")) {
            try {
                Field field = clazz.getDeclaredField("persistence");
                field.setAccessible(true);
                persistence = field.get(timerService);
                return persistence != null && persistence.getClass().getName().equals("org.jboss.as.ejb3.timerservice.persistence.database.DatabaseTimerPersistence");
            } catch (ReflectiveOperationException ex) {
                logger.trace("Exception retrieving timer service persistence field", ex);
            }
        }
        return false;
    }

    
    @Override
    public Optional<WildflyAcceptedInfo> accept(GlobalTimerService globalTimerService) {
        return isPersistentWildfly ? getDB(globalTimerService.getRuntimeManager()) : Optional.empty();
    }

    private Optional<WildflyAcceptedInfo> getDB(InternalRuntimeManager runtime) {
        EntityManagerFactory emf = EntityManagerFactoryManager.get().getOrCreate(runtime.getDeploymentDescriptor().getPersistenceUnit());
        for (Object value : emf.getProperties().values()) {
            if (value instanceof String) {
                String search = value.toString().toLowerCase();
                if (search.contains("oracle")) {
                    return Optional.of(new WildflyAcceptedInfo(EJBTimerDB.ORACLE, emf));
                } else if (search.contains("postgresql")) {
                    return Optional.of(new WildflyAcceptedInfo(EJBTimerDB.POSTGRESQL,emf));
                }
            }
        }
        return Optional.empty();
    }
   
    @Override
    public Collection<Object> getTimers(String jobName, WildflyAcceptedInfo accepted) {
        EntityManager em = accepted.getEmf().createEntityManager();
        List<String> results = em.createNativeQuery(accepted.getDb().getQuery(), String.class).setParameter(1, jobName).getResultList();
        return results.stream().map(this::deserialize).collect(Collectors.toList());
    }
    
    private Object deserialize( String info) {
        try {
            Method method = persistence.getClass().getMethod("deSerialize", String.class);
            return method.invoke(persistence, info);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Error deserializing info", e);
        }
    }

}
