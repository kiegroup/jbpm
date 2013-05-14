/*
 * Copyright 2011 Red Hat Inc.
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
package org.jbpm.persistence.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.kie.api.runtime.EnvironmentName.ENTITY_MANAGER_FACTORY;
import static org.kie.api.runtime.EnvironmentName.GLOBALS;
import static org.kie.api.runtime.EnvironmentName.TRANSACTION;
import static org.kie.api.runtime.EnvironmentName.TRANSACTION_MANAGER;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Properties;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.drools.core.base.MapGlobalResolver;
import org.drools.core.impl.EnvironmentFactory;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.junit.Assert;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.persistence.infinispan.InfinispanKnowledgeService;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;

public class PersistenceUtil {

    private static Logger logger = LoggerFactory.getLogger( PersistenceUtil.class );

    private static boolean TEST_MARSHALLING = true;
    
    // Persistence and data source constants
    public static final String DROOLS_PERSISTENCE_UNIT_NAME = "org.drools.persistence.jpa";
    public static final String DROOLS_LOCAL_PERSISTENCE_UNIT_NAME = "org.drools.persistence.jpa.local";
    public static final String JBPM_PERSISTENCE_UNIT_NAME = "org.jbpm.persistence.jpa";
    public static final String JBPM_LOCAL_PERSISTENCE_UNIT_NAME = "org.jbpm.persistence.jpa.local";
        
    protected static final String DATASOURCE_PROPERTIES = "/datasource.properties";
    
    private static Properties defaultProperties = null;
   
    // Setup and marshalling setup constants
    public static String DATASOURCE = "org.droolsjbpm.persistence.datasource";

    /**
     * @see #setupWithPoolingDataSource(String, String, boolean)
     * @param persistenceUnitName The name of the persistence unit to be used.
     * @return test context
     */
    public static HashMap<String, Object> setupWithPoolingDataSource(String persistenceUnitName) {
        return setupWithPoolingDataSource(persistenceUnitName, true);
    }
    
    /**
     * @see #setupWithPoolingDataSource(String, String, boolean)
     * @param persistenceUnitName The name of the persistence unit to be used.
     * @return test context
     */
    public static HashMap<String, Object> setupWithPoolingDataSource(String persistenceUnitName, boolean testMarshalling) {
        return setupWithPoolingDataSource(persistenceUnitName, "jdbc/testDS1", testMarshalling);
    }
    
    /**
     * This method does all of the setup for the test and returns a HashMap
     * containing the persistence objects that the test might need.
     * 
     * @param persistenceUnitName
     *            The name of the persistence unit used by the test.
     * @return HashMap<String Object> with persistence objects, such as the
     *         EntityManagerFactory and DataSource
     */
    public static HashMap<String, Object> setupWithPoolingDataSource(final String persistenceUnitName, String dataSourceName, final boolean testMarshalling) {
    	try {
    		TransactionManagerServices.getTransactionManager().setTransactionTimeout(300);
    	} catch (SystemException e) {
    		//TODO
    		e.printStackTrace();
    	}
    	HashMap<String, Object> context = new HashMap<String, Object>();

        // set the right jdbc url
        Properties dsProps = getDatasourceProperties();

        determineTestMarshalling(dsProps, testMarshalling);
        
        // Setup persistence
        DefaultCacheManager cm = null;
        try {
	        if (TEST_MARSHALLING) {
	            cm = new DefaultCacheManager("infinispan.xml");
	           
	            UserTransaction ut = (UserTransaction) cm.getCache("jbpm-configured-cache").getAdvancedCache().getTransactionManager();
	            context.put(TRANSACTION, ut);
	        } else {
	        	cm = new DefaultCacheManager("infinispan.xml");
	        }
        } catch (Exception e) {
        	//TODO
        	e.printStackTrace();
        }
        
        context.put(ENTITY_MANAGER_FACTORY, cm);

        return context;
    }

    private static void determineTestMarshalling(Properties dsProps, boolean useTestMarshallingInTestMethod ) { 
        Object testMarshallingProperty = dsProps.get("testMarshalling"); 
        if( "true".equals(testMarshallingProperty) ) { 
            TEST_MARSHALLING = true;
           if( !useTestMarshallingInTestMethod ) { 
               TEST_MARSHALLING = false;
           }
        } 
        else { 
            TEST_MARSHALLING = false;
        }
    }
    
    /**
     * This method should be called in the @After method of a test to clean up
     * the persistence unit and datasource.
     * 
     * @param context
     *            A HashMap generated by
     *            {@link org.drools.persistence.util.PersistenceUtil setupWithPoolingDataSource(String)}
     * 
     */
    public static void cleanUp(HashMap<String, Object> context) {
        if (context != null) {
            Object emfObject = context.remove(ENTITY_MANAGER_FACTORY);
            if (emfObject != null) {
                try {
                    DefaultCacheManager cm = (DefaultCacheManager) emfObject;
                    Cache<String,Object> cache = cm.getCache("jbpm-configured-cache");
                    TransactionManager tm = cache.getAdvancedCache().getTransactionManager();
                    boolean txOwner = false;
                    if (tm.getStatus() == Status.STATUS_NO_TRANSACTION ) {
                        tm.begin();
                        txOwner = true;
                    }
                    if (tm.getStatus() != Status.STATUS_ACTIVE) {
                    	tm.rollback();
                    	tm.begin();
                    	txOwner = true;
                    }
                    cache.clear();
                    if (txOwner) {
                    	tm.commit();
                    }
                    //cm.stop();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            BitronixTransactionManager txm = TransactionManagerServices.getTransactionManager();
            if( txm != null ) { 
                txm.shutdown();
            }

        }
        
    }
    
    /**
     * Return the default database/datasource properties - These properties use
     * an in-memory H2 database
     * 
     * This is used when the developer is somehow running the tests but
     * bypassing the maven filtering that's been turned on in the pom.
     * 
     * @return Properties containing the default properties
     */
    private static Properties getDefaultProperties() {
        if (defaultProperties == null) {
            String[] keyArr = { "serverName", "portNumber", "databaseName", "url", "user", "password", "driverClassName",
                    "className", "maxPoolSize", "allowLocalTransactions" };
            String[] defaultPropArr = { "", "", "", "jdbc:h2:tcp://localhost/JPADroolsFlow", "sa", "", "org.h2.Driver",
                    "bitronix.tm.resource.jdbc.lrc.LrcXADataSource", "16", "true" };
            Assert.assertTrue("Unequal number of keys for default properties", keyArr.length == defaultPropArr.length);
            defaultProperties = new Properties();
            for (int i = 0; i < keyArr.length; ++i) {
                defaultProperties.put(keyArr[i], defaultPropArr[i]);
            }
        }

        return defaultProperties;
    }

    /**
     * This reads in the (maven filtered) datasource properties from the test
     * resource directory.
     * 
     * @return Properties containing the datasource properties.
     */
    public static Properties getDatasourceProperties() { 
        String propertiesNotFoundMessage = "Unable to load datasource properties [" + DATASOURCE_PROPERTIES + "]";
        boolean propertiesNotFound = false;

        // Central place to set additional H2 properties
        System.setProperty("h2.lobInDatabase", "true");
        
        InputStream propsInputStream = PersistenceUtil.class.getResourceAsStream(DATASOURCE_PROPERTIES);
        assertNotNull(propertiesNotFoundMessage, propsInputStream);
        Properties props = new Properties();
        if (propsInputStream != null) {
            try {
                props.load(propsInputStream);
            } catch (IOException ioe) {
                propertiesNotFound = true;
                logger.warn("Unable to find properties, using default H2 properties: " + ioe.getMessage());
                ioe.printStackTrace();
            }
        } else {
            propertiesNotFound = true;
        }

        String password = props.getProperty("password");
        if ("${maven.jdbc.password}".equals(password) || propertiesNotFound) {
            props = getDefaultProperties();
        }

        return props;
    }

    /**
     * Reflection method when doing ugly hacks in tests.
     * 
     * @param fieldname
     *            The name of the field to be retrieved.
     * @param source
     *            The object containing the field to be retrieved.
     * @return The value (object instance) stored in the field requested from
     *         the given source object.
     */
    public static Object getValueOfField(String fieldname, Object source) {
        String sourceClassName = source.getClass().getSimpleName();
    
        Field field = null;
        try {
            field = source.getClass().getDeclaredField(fieldname);
            field.setAccessible(true);
        } catch (SecurityException e) {
            fail("Unable to retrieve " + fieldname + " field from " + sourceClassName + ": " + e.getCause());
        } catch (NoSuchFieldException e) {
            fail("Unable to retrieve " + fieldname + " field from " + sourceClassName + ": " + e.getCause());
        }
    
        assertNotNull("." + fieldname + " field is null!?!", field);
        Object fieldValue = null;
        try {
            fieldValue = field.get(source);
        } catch (IllegalArgumentException e) {
            fail("Unable to retrieve value of " + fieldname + " from " + sourceClassName + ": " + e.getCause());
        } catch (IllegalAccessException e) {
            fail("Unable to retrieve value of " + fieldname + " from " + sourceClassName + ": " + e.getCause());
        }
        return fieldValue;
    }

    public static Environment createEnvironment(HashMap<String, Object> context) {
        Environment env = EnvironmentFactory.newEnvironment();
        
        UserTransaction ut = (UserTransaction) context.get(TRANSACTION);
        if( ut != null ) { 
            env.set( TRANSACTION, ut);
        }
        
        env.set( ENTITY_MANAGER_FACTORY, context.get(ENTITY_MANAGER_FACTORY) );
        env.set( TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager() );
        env.set( GLOBALS, new MapGlobalResolver() );
        
        return env;
    }
    
   public static StatefulKnowledgeSession createKnowledgeSessionFromKBase(KnowledgeBase kbase, HashMap<String, Object> context) {
       KieSessionConfiguration ksconf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
       StatefulKnowledgeSession knowledgeSession = InfinispanKnowledgeService.newStatefulKnowledgeSession(kbase, ksconf, createEnvironment(context));
       return knowledgeSession;
   }
   
   public static boolean testMarshalling() { 
       return TEST_MARSHALLING;
   }
   
}
