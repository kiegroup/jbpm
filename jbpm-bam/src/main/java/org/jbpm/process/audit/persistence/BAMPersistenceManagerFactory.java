/*
 * Copyright 2012 JBoss Inc
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
package org.jbpm.process.audit.persistence;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BAMPersistenceManagerFactory {
    private static Logger logger = LoggerFactory.getLogger(BAMPersistenceManagerFactory.class);
    
    private static volatile EntityManagerFactory manager;
    
    private static Properties bamProperties;

    public static BAMPersistenceManager getBAMPersistenceManager() { 
        
        
        return  getBAMPersistenceManager(null);
    }
    
    public static BAMPersistenceManager getBAMPersistenceManager(Environment env) { 
        loadProperties();
        if (manager == null || !manager.isOpen()) {
            manager = getEntityManagerFactory(env);
        }        
        return new BAMPersistenceManager(manager);

    }
    
    protected static void loadProperties() {
        if (bamProperties == null) {
            bamProperties = new Properties();
            try {
                bamProperties.load(BAMPersistenceManagerFactory.class.getResourceAsStream("/jbpm.bam.properties"));
            } catch (Exception e) {
                
            }
        }
    }
    
    protected static EntityManagerFactory getEntityManagerFactory(Environment env) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Configuring EntityManagerFactory for jBPM BAM module...");
            }
            String useEnvEmf = bamProperties.getProperty("use.environment.emf");
            String useJndiEmf = bamProperties.getProperty("use.jndi.emf");
            if ("true".equalsIgnoreCase(useEnvEmf)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Environment EntityManagerFactory was requested to be used");
                }
                if (env != null && env.get(EnvironmentName.ENTITY_MANAGER_FACTORY) != null) {
                    
                    return (EntityManagerFactory) env.get(EnvironmentName.ENTITY_MANAGER_FACTORY);
                } else {
                    throw new RuntimeException("EntityManagerFactory was not found in the environment " + env);
                }
            } else if ("true".equalsIgnoreCase(useJndiEmf)) {
                String jndiName = bamProperties.getProperty("jbpm.bam.emf.jndi");
                if (logger.isDebugEnabled()) {
                    logger.debug("JNDI EntityManagerFactory was requested to be used, JNDI name to look up " + jndiName);
                }
                InitialContext ctx = new InitialContext();
                
                return (EntityManagerFactory) ctx.lookup(jndiName);
            } else {
                String puName = bamProperties.getProperty("jbpm.bam.persistence.unit", "org.jbpm.persistence.jpa");
                if (logger.isDebugEnabled()) {
                    logger.debug("Building new EntityManagerFactory with persistence unit name " + puName);
                }
                return Persistence.createEntityManagerFactory(puName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error when creating EntityManagerFactory for jBPM BAM", e);
        }
    }
    
}
