/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.test.persistence.util;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.UserTransaction;

import org.drools.core.base.MapGlobalResolver;
import org.drools.core.impl.EnvironmentFactory;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;
import org.jbpm.process.instance.event.DefaultSignalManagerFactory;
import org.jbpm.process.instance.impl.DefaultProcessInstanceManagerFactory;
import org.junit.Assert;
import org.kie.api.KieBase;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.internal.runtime.conf.ForceEagerActivationOption;
import org.kie.test.util.db.DataSourceFactory;
import org.kie.test.util.db.PoolingDataSourceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.api.runtime.EnvironmentName.ENTITY_MANAGER_FACTORY;
import static org.kie.api.runtime.EnvironmentName.GLOBALS;
import static org.kie.api.runtime.EnvironmentName.TRANSACTION;
import static org.kie.api.runtime.EnvironmentName.TRANSACTION_MANAGER;

public class PersistenceUtil {

    private static final Logger logger = LoggerFactory.getLogger( PersistenceUtil.class );

    // Persistence and data source constants
    public static final String DROOLS_PERSISTENCE_UNIT_NAME = "org.drools.persistence.jpa";
    public static final String DROOLS_LOCAL_PERSISTENCE_UNIT_NAME = "org.drools.persistence.jpa.local";
    public static final String JBPM_PERSISTENCE_UNIT_NAME = "org.jbpm.persistence.jpa";
    public static final String JBPM_LOCAL_PERSISTENCE_UNIT_NAME = "org.jbpm.persistence.jpa.local";
        
    protected static final String DATASOURCE_PROPERTIES = "/datasource.properties";

    public static final String MAX_POOL_SIZE = "maxPoolSize";
    public static final String ALLOW_LOCAL_TXS = "allowLocalTransactions";
    public static final String SERVER_NAME = "serverName";
    public static final String SERVER_PORT = "portNumber";
    public static final String DATABASE_NAME = "databaseName";
    public static final String DATASOURCE_CLASS_NAME = "className";
    public static final String DRIVER_CLASS_NAME = "driverClassName";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String JDBC_URL = "url";
    
    private static TestH2Server h2Server = new TestH2Server();
    
    private static Properties defaultProperties = null;
   
    // Setup and marshalling setup constants
    public static final String DATASOURCE = "org.droolsjbpm.persistence.datasource";

    /**
     * @see #setupWithPoolingDataSource(String, String)
     * @param persistenceUnitName The name of the persistence unit to be used.
     * @return test context
     */
    public static HashMap<String, Object> setupWithPoolingDataSource(String persistenceUnitName) {
        return setupWithPoolingDataSource(persistenceUnitName, "jdbc/testDS1");
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
    public static HashMap<String, Object> setupWithPoolingDataSource(final String persistenceUnitName, String dataSourceName) {
        HashMap<String, Object> context = new HashMap<String, Object>();

        // Setup the datasource
        PoolingDataSourceWrapper ds1 = setupPoolingDataSource(getDatasourceProperties(), dataSourceName);
        context.put(DATASOURCE, ds1);

        // Setup persistence
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName);
        context.put(ENTITY_MANAGER_FACTORY, emf);

        return context;
    }

    /**
     * This method starts H2 database server (tcp).
     * 
     * @param datasourceProperties
     *            The properties used to setup the data source.
     */
    public static void startH2TcpServer(Properties datasourceProperties) {
        String jdbcUrl = datasourceProperties.getProperty("url");
        if (jdbcUrl != null && jdbcUrl.matches("jdbc:h2:tcp:.*")) {
            h2Server.start(datasourceProperties.getProperty("tcpPort"));
        }
    }

    /**
     * This method stops H2 database server (tcp).
     */
    public static void stopH2TcpServer() {
        h2Server.stop();
    }

    /**
     * This method should be called in the @After method of a test to clean up
     * the persistence unit and datasource.
     * 
     * @param context
     *            A HashMap generated by
     *            {@link PersistenceUtil#setupWithPoolingDataSource(String)}
     * 
     */
    public static void cleanUp(Map<String, Object> context) {
        if (context != null) {
            
            Object emfObject = context.remove(ENTITY_MANAGER_FACTORY);
            if (emfObject != null) {
                try {
                    EntityManagerFactory emf = (EntityManagerFactory) emfObject;
                    emf.close();
                } catch (Throwable t) {
                    logger.error("Unable to close entity manager factory {}", ENTITY_MANAGER_FACTORY, t);
                }
            }

            Object ds1Object = context.remove(DATASOURCE);
            if (ds1Object != null) {
                try {
                    PoolingDataSourceWrapper ds1 = (PoolingDataSourceWrapper) ds1Object;
                    ds1.close();
                } catch (Throwable t) {
                    logger.error("Unable to close pooling datasource wrapper {}", DATASOURCE, t);
                }
            }
            
        }
        
    }
    
    /**
     * This method uses the "jdbc/testDS1" datasource, which is the default.
     * @param dsProps The properties used to setup the data source.
     * @return a PoolingDataSourceWrapper
     */
    public static PoolingDataSourceWrapper setupPoolingDataSource(Properties dsProps) {
       return setupPoolingDataSource(dsProps, "jdbc/testDS1");
    }
    
    /**
     * This sets up a PoolingDataSourceWrapper.
     * 
     * @return PoolingDataSourceWrapper that has been set up but _not_ initialized.
     */
    public static PoolingDataSourceWrapper setupPoolingDataSource(Properties dsProps, String datasourceName) {
        startH2TcpServer(dsProps);
        return DataSourceFactory.setupPoolingDataSource(datasourceName, dsProps);
    }

    /**
     * Sets up a PoolingDataSourceWrapper with the specified datasource name based on
     * the datasource.properties file existing in the classpath. If not found, default H2 datasource
     * properties will be used instead.
     * @param datasourceName Datasource name to setup
     * @return PoolingDataSourceWrapper that has been set up but _not_ initialized.
     */
    public static PoolingDataSourceWrapper setupPoolingDataSource(String datasourceName){
        return setupPoolingDataSource(getDatasourceProperties(), datasourceName);
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
            String[] keyArr = {
                    SERVER_NAME, SERVER_PORT, DATABASE_NAME, JDBC_URL,
                    USER, PASSWORD,
                    DRIVER_CLASS_NAME, DATASOURCE_CLASS_NAME,
                    MAX_POOL_SIZE, ALLOW_LOCAL_TXS };
            String[] defaultPropArr = {
                    "", "", "", "jdbc:h2:mem:jbpm-db;MVCC=true",
                    "sa", "",
                    "org.h2.Driver", "org.h2.jdbcx.JdbcDataSource",
                    "16", "true" };
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
        // Central place to set additional H2 properties
        System.setProperty("h2.lobInDatabase", "true");
        
        Properties props = new Properties();
        try (InputStream propsInputStream = PersistenceUtil.class.getResourceAsStream(DATASOURCE_PROPERTIES)){
            props.load(propsInputStream);
        } catch (Exception e) {
            logger.warn("Unable to load datasource properties file {}, using default H2 properties: {}",
                        DATASOURCE_PROPERTIES, e.getMessage());
            logger.debug("Stacktrace:", e);
            props = getDefaultProperties();
        }
        return props;
    }

    /**
     * This method returns whether or not transactions should be used when
     * dealing with the SessionInfo object (or any other persisted entity that
     * contains @Lob's )
     * 
     * @return boolean Whether or not to use transactions
     */
    public static boolean useTransactions() {
        boolean useTransactions = false;
        String databaseDriverClassName = getDatasourceProperties().getProperty(DRIVER_CLASS_NAME);

        // Postgresql has a "Large Object" api which REQUIRES the use of transactions
        //  since @Lob/byte array is actually stored in multiple tables.
        if (databaseDriverClassName.startsWith("org.postgresql") || databaseDriverClassName.startsWith("com.edb")) {
            useTransactions = true;
        }
        return useTransactions;
    }

    public static Environment createEnvironment(Map<String, Object> context) {
        Environment env = EnvironmentFactory.newEnvironment();
        
        UserTransaction ut = (UserTransaction) context.get(TRANSACTION);
        if( ut != null ) { 
            env.set( TRANSACTION, ut);
        }
        
        env.set( ENTITY_MANAGER_FACTORY, context.get(ENTITY_MANAGER_FACTORY) );
        env.set( TRANSACTION_MANAGER, com.arjuna.ats.jta.TransactionManager.transactionManager() );
        env.set( GLOBALS, new MapGlobalResolver() );
        
        return env;
    }
    
   /**
    * An class responsible for starting the H2 database (tcp)
    * server
    */
   private static class TestH2Server {
       private Server realH2Server;

       public synchronized void start(String port) {
           if (realH2Server == null || !realH2Server.isRunning(false)) {
               try {
                   DeleteDbFiles.execute("", null, true);
                   realH2Server = Server.createTcpServer((port != null && !port.isEmpty()) ? new String[]{"-tcpPort", port} : new String[0]);
                   realH2Server.start();
               } catch (SQLException e) {
                   throw new RuntimeException("can't start h2 server db", e);
               }
           }
       }

       @Override
       protected void finalize() throws Throwable {
           stop();
           super.finalize();
       }

       /**
        * An class responsible for stopping the H2 database (tcp)
        * server
        */
       public synchronized void stop() {
           if (realH2Server != null) {
               realH2Server.stop();
               realH2Server.shutdown();
               DeleteDbFiles.execute("", null, true);
               realH2Server = null;
           }
       }

   }

   public static KieSession createKieSessionFromKBase(KieBase kbase, Map<String, Object> context) {
       Properties defaultProps = new Properties();
       defaultProps.setProperty("drools.processSignalManagerFactory",
               DefaultSignalManagerFactory.class.getName());
       defaultProps.setProperty("drools.processInstanceManagerFactory",
               DefaultProcessInstanceManagerFactory.class.getName());
       KieSessionConfiguration ksconf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration(defaultProps);
       ksconf.setOption(ForceEagerActivationOption.YES);
               
       return kbase.newKieSession(ksconf, createEnvironment(context));
   }
   
}
