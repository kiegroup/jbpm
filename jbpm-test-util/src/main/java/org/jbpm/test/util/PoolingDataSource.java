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

package org.jbpm.test.util;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.xa.XAResource;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import org.apache.tomcat.dbcp.dbcp2.PoolableConnection;
import org.apache.tomcat.dbcp.dbcp2.PoolableConnectionFactory;
import org.apache.tomcat.dbcp.dbcp2.managed.DataSourceXAConnectionFactory;
import org.apache.tomcat.dbcp.dbcp2.managed.ManagedDataSource;
import org.apache.tomcat.dbcp.pool2.impl.AbandonedConfig;
import org.apache.tomcat.dbcp.pool2.impl.GenericObjectPool;
import org.apache.tomcat.dbcp.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for actual Pooling Data Source provided by tomcat DBCP library. This class offers data source with
 * XA transactions and connection pooling capabilities.
 * */
public class PoolingDataSource implements DataSource {

    private static final String PROP_USERNAME = "username";
    private static final String PROP_PASSWORD = "password";

    private static final Logger logger = LoggerFactory.getLogger(PoolingDataSource.class); 

    private Properties driverProperties = new Properties();
    private String uniqueName;
    private String className;
    private ManagedDataSource<?> managedDataSource;

    /**
     * @param uniqueName Data Source unique name. Serves for registration to JNDI.
     * @param dsClassName Name of a class implementing {@link XADataSource} available in a JDBC driver on a classpath.
     * */
    public PoolingDataSource(final String uniqueName, final String dsClassName) {
        this.uniqueName = uniqueName;
        this.className = dsClassName;
    }

    /**
     * For backward compatibility
     * */
    public PoolingDataSource() {}

    public Properties getDriverProperties() {
        return driverProperties;
    }

    public void init() {
        init(new HashMap<>());
    }

    public void init(final Map<String, Object> environment)  {
        final XADataSource xaDataSource = createXaDataSource();

        final TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
        final TransactionSynchronizationRegistry tsr =
                jtaPropertyManager.getJTAEnvironmentBean().getTransactionSynchronizationRegistry();


        final DataSourceXAConnectionFactory xaConnectionFactory = resolveDataSourceXAConnectionFactory(tm, xaDataSource);
        managedDataSource = createManagedDataSource(xaConnectionFactory, xaDataSource, environment);

        try {
            InitialContext initContext = new InitialContext();

            initContext.rebind(uniqueName, managedDataSource);
            initContext.rebind("java:comp/UserTransaction", com.arjuna.ats.jta.UserTransaction.userTransaction());
            initContext.rebind("java:comp/TransactionManager", tm);
            initContext.rebind("java:comp/TransactionSynchronizationRegistry", tsr);
        } catch (NamingException e) {
            logger.warn("No InitialContext available, resource won't be accessible via lookup");
        }
    }

    private DataSourceXAConnectionFactory resolveDataSourceXAConnectionFactory(final TransactionManager tm,
                                                                               final XADataSource xaDataSource) {
        final DataSourceXAConnectionFactory xaConnectionFactory;
        if (isH2()) {
            xaConnectionFactory = new DataSourceXAConnectionFactory(tm, xaDataSource);
        } else {
            final String username = driverProperties.getProperty("user");
            final String password = driverProperties.getProperty("password");
            xaConnectionFactory = new DataSourceXAConnectionFactory(tm, xaDataSource, username, password);
        }

        return xaConnectionFactory;
    }

    private boolean isH2() {
        return className.startsWith("org.h2");
    }

    private XADataSource createXaDataSource() {
        try {
            XADataSource xaDataSource = (XADataSource) Class.forName(className).newInstance();
            String url = driverProperties.getProperty("url", driverProperties.getProperty("URL"));

            if (isH2()) {
                final String username = driverProperties.getProperty("user");
                final String password = driverProperties.getProperty("password");
                xaDataSource.getClass().getMethod("setPassword", new Class[]{String.class}).invoke(xaDataSource, password);
                xaDataSource.getClass().getMethod("setUser", new Class[]{String.class}).invoke(xaDataSource, username);
            }

            if (!(className.startsWith("com.ibm.db2") || className.startsWith("com.sybase"))) {
                try {
                    xaDataSource.getClass().getMethod("setUrl", new Class[]{String.class}).invoke(xaDataSource, url);
                } catch (NoSuchMethodException ex) {
                    logger.info("Unable to find \"setUrl\" method in db driver JAR. Trying \"setURL\" " );
                    xaDataSource.getClass().getMethod("setURL", new Class[]{String.class}).invoke(xaDataSource, url);
                } catch (InvocationTargetException ex) {
                    logger.info("Driver does not support setURL and setUrl method.");
                    throw new RuntimeException(ex);
                }
            } else {
                setupAdditionalDriverProperties(xaDataSource);
            }

            return xaDataSource;
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException
                | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupAdditionalDriverProperties(final XADataSource xaDataSource) {
        try {
            xaDataSource.getClass().getMethod("setServerName", new Class[]{String.class}).invoke(xaDataSource, driverProperties.getProperty("serverName"));
            xaDataSource.getClass().getMethod("setDatabaseName", new Class[]{String.class}).invoke(xaDataSource, driverProperties.getProperty("databaseName"));
            if (className.startsWith("com.ibm.db2")) {
                xaDataSource.getClass().getMethod("setDriverType", new Class[]{int.class}).invoke(xaDataSource, 4);
                xaDataSource.getClass().getMethod("setPortNumber", new Class[]{int.class}).invoke(xaDataSource, Integer.valueOf(driverProperties.getProperty("portNumber")));
                xaDataSource.getClass().getMethod("setResultSetHoldability", new Class[]{int.class}).invoke(xaDataSource, 1);
                xaDataSource.getClass().getMethod("setDowngradeHoldCursorsUnderXa", new Class[]{boolean.class}).invoke(xaDataSource, true);
            } else if (className.startsWith("com.sybase")) {
                xaDataSource.getClass().getMethod("setPortNumber", new Class[]{int.class}).invoke(xaDataSource, Integer.valueOf(driverProperties.getProperty("portNumber")));
                xaDataSource.getClass().getMethod("setPassword", new Class[]{String.class}).invoke(xaDataSource, driverProperties.getProperty("password"));
                xaDataSource.getClass().getMethod("setUser", new Class[]{String.class}).invoke(xaDataSource, driverProperties.getProperty("user"));
            }
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
            logger.error("Exception thrown while setting properties for {} driver", className);
            throw new RuntimeException(ex);
        }
    }

    private ManagedDataSource createManagedDataSource(final DataSourceXAConnectionFactory xaConnectionFactory,
                                                      final XADataSource xaDataSource,
                                                      final Map<String, Object> environment) {
        final PoolableConnectionFactory poolableConnectionFactory = getPoolableConnectionFactory(xaConnectionFactory, environment);
        final GenericObjectPoolConfig objectPoolConfig = getObjectPoolConfig(environment);
        final AbandonedConfig abandonedConfig = getAbandonedConfig(environment);

        final GenericObjectPool<PoolableConnection> objectPool =
                new GenericObjectPool<>(poolableConnectionFactory, objectPoolConfig, abandonedConfig);
        poolableConnectionFactory.setPool(objectPool);

        // Register for recovery
        registerXARecoveryModule(xaDataSource, environment);

        return new ManagedDataSource<>(objectPool, xaConnectionFactory.getTransactionRegistry());
    }

    private GenericObjectPoolConfig getObjectPoolConfig(final Map<String, Object> environment) {
        final GenericObjectPoolConfig objectPoolConfig = new GenericObjectPoolConfig();
        setFromEnvironment("maxTotal", environment, (value) -> objectPoolConfig.setMaxTotal((int) value));
        setFromEnvironment("minIdle", environment, (value) -> objectPoolConfig.setMinIdle((int) value));
        setFromEnvironment("maxIdle", environment, (value) -> objectPoolConfig.setMaxIdle((int) value));
        setFromEnvironment("lifo", environment, (value) -> objectPoolConfig.setLifo((boolean) value));
        setFromEnvironment("fairness", environment, (value) -> objectPoolConfig.setFairness((boolean) value));
        setFromEnvironment("maxWaitMillis", environment, (value) -> objectPoolConfig.setMaxWaitMillis((long) value));
        setFromEnvironment("minEvictableIdleTimeMillis", environment, (value) -> objectPoolConfig.setMinEvictableIdleTimeMillis((long) value));
        setFromEnvironment("evictorShutdownTimeoutMillis", environment, (value) -> objectPoolConfig.setEvictorShutdownTimeoutMillis((long) value));
        setFromEnvironment("softMinEvictableIdleTimeMillis", environment, (value) -> objectPoolConfig.setSoftMinEvictableIdleTimeMillis((long) value));
        setFromEnvironment("numTestsPerEvictionRun", environment, (value) -> objectPoolConfig.setNumTestsPerEvictionRun((int) value));
        setFromEnvironment("evictionPolicyClassName", environment, (value) -> objectPoolConfig.setEvictionPolicyClassName((String) value));
        setFromEnvironment("testOnCreate", environment, (value) -> objectPoolConfig.setTestOnCreate((boolean) value));
        setFromEnvironment("testOnBorrow", environment, (value) -> objectPoolConfig.setTestOnBorrow((boolean) value));
        setFromEnvironment("testOnReturn", environment, (value) -> objectPoolConfig.setTestOnReturn((boolean) value));
        setFromEnvironment("testWhileIdle", environment, (value) -> objectPoolConfig.setTestWhileIdle((boolean) value));
        setFromEnvironment("timeBetweenEvictionRunsMillis", environment, (value) -> objectPoolConfig.setTimeBetweenEvictionRunsMillis((long) value));
        setFromEnvironment("blockWhenExhausted", environment, (value) -> objectPoolConfig.setBlockWhenExhausted((boolean) value));
        setFromEnvironment("jmxEnabled", environment, (value) -> objectPoolConfig.setJmxEnabled((boolean) value));
        setFromEnvironment("jmxNamePrefix", environment, (value) -> objectPoolConfig.setJmxNamePrefix((String) value));
        setFromEnvironment("jmxNameBase", environment, (value) -> objectPoolConfig.setJmxNameBase((String) value));
        return objectPoolConfig;
    }

    private AbandonedConfig getAbandonedConfig(final Map<String, Object> environment) {
        final AbandonedConfig abandonedConfig = new AbandonedConfig();
        setFromEnvironment("removeAbandonedOnBorrow", environment, (value) -> abandonedConfig.setRemoveAbandonedOnBorrow((boolean) value));
        setFromEnvironment("removeAbandonedOnMaintenance", environment, (value) -> abandonedConfig.setRemoveAbandonedOnMaintenance((boolean) value));
        setFromEnvironment("removeAbandonedTimeout", environment, (value) -> abandonedConfig.setRemoveAbandonedTimeout((int) value));
        setFromEnvironment("logAbandoned", environment, (value) -> abandonedConfig.setLogAbandoned((boolean) value));
        setFromEnvironment("requireFullStackTrace", environment, (value) -> abandonedConfig.setRequireFullStackTrace((boolean) value));
        setFromEnvironment("useUsageTracking", environment, (value) -> abandonedConfig.setUseUsageTracking((boolean) value));
        return abandonedConfig;
    }

    private PoolableConnectionFactory getPoolableConnectionFactory(final DataSourceXAConnectionFactory xaDsConnectionFactory,
                                                                   final Map<String, Object> environment) {
        final PoolableConnectionFactory poolableConnectionFactory =
                new PoolableConnectionFactory(xaDsConnectionFactory, null);
        setFromEnvironment("validationQuery", environment, (value) -> poolableConnectionFactory.setValidationQuery((String) value));
        setFromEnvironment("validationQueryTimeout", environment, (value) -> poolableConnectionFactory.setValidationQueryTimeout((int) value));
        setFromEnvironment("connectionInitSqls", environment, (value) -> poolableConnectionFactory.setConnectionInitSql((Collection<String>) value));
        setFromEnvironment("disconnectionSqlCodes", environment, (value) -> poolableConnectionFactory.setDisconnectionSqlCodes((Collection<String>) value));
        setFromEnvironment("fastFailValidation", environment, (value) -> poolableConnectionFactory.setFastFailValidation((boolean) value));
        setFromEnvironment("defaultTransactionIsolation", environment, (value) -> poolableConnectionFactory.setDefaultTransactionIsolation((int) value));
        setFromEnvironment("defaultCatalog", environment, (value) -> poolableConnectionFactory.setDefaultCatalog((String)value));
        setFromEnvironment("cacheState", environment, (value) -> poolableConnectionFactory.setCacheState((boolean) value));
        return poolableConnectionFactory;
    }

    private void setFromEnvironment(String key, Map<String, Object> environment, Consumer<Object> setter) {
        Object value = environment.get(key);
        if (value != null) {
            setter.accept(value);
        }
    }

    private void registerXARecoveryModule(final XADataSource xaDataSource, final Map<String, Object> environment) {
        final XARecoveryModule xaRecoveryModule = XARecoveryModule.getRegisteredXARecoveryModule();
        if (xaRecoveryModule == null) {
            throw new IllegalStateException("XARecoveryModule is not registered with recovery manager");
        }

        final Properties recoveryModuleProperties = new Properties();
        String username = (String) environment.get(PROP_USERNAME);
        String password = (String) environment.get(PROP_PASSWORD);

        if (username != null) {
            recoveryModuleProperties.setProperty(PROP_USERNAME, username);
        }

        if (password != null) {
            recoveryModuleProperties.setProperty(PROP_PASSWORD, password);
        }

        xaRecoveryModule.addXAResourceRecoveryHelper(new XAResourceRecoveryHelper() {
            private final Object lock = new Object();
            private XAConnection connection;

            @Override
            public boolean initialise(String p) throws Exception {
                return true;
            }

            @Override
            public synchronized XAResource[] getXAResources() throws Exception {
                synchronized (lock) {
                    initialiseConnection();
                    try {
                        return new XAResource[]{connection.getXAResource()};
                    } catch (SQLException ex) {
                        return new XAResource[0];
                    }
                }
            }

            private void initialiseConnection() throws SQLException {
                // This will allow us to ensure that each recovery cycle gets a fresh connection
                // It might be better to close at the end of the recovery pass to free up the connection but
                // we don't have a hook
                if (connection == null) {
                    final String user = recoveryModuleProperties.getProperty(PROP_USERNAME);
                    final String password = recoveryModuleProperties.getProperty(PROP_PASSWORD);

                    if (user != null && password != null) {
                        connection = xaDataSource.getXAConnection(user, password);
                    } else {
                        connection = xaDataSource.getXAConnection();
                    }
                    connection.addConnectionEventListener(new ConnectionEventListener() {
                        @Override
                        public void connectionClosed(ConnectionEvent event) {
                            logger.warn("The connection was closed: " + connection);
                            synchronized (lock) {
                                connection = null;
                            }
                        }

                        @Override
                        public void connectionErrorOccurred(ConnectionEvent event) {
                            logger.warn("A connection error occurred: " + connection);
                            synchronized (lock) {
                                try {
                                    connection.close();
                                } catch (SQLException e) {
                                    // Ignore
                                    logger.warn("Could not close failing connection: " + connection);
                                }
                                connection = null;
                            }
                        }
                    });
                }
            }
        });
    }

    public void close() {
        try {
            managedDataSource.close();
            new InitialContext().unbind(uniqueName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Connection getConnection() throws SQLException {
        return managedDataSource.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return managedDataSource.getConnection(username, password);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return managedDataSource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return managedDataSource.isWrapperFor(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return managedDataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        managedDataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        managedDataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return managedDataSource.getLoginTimeout();
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return managedDataSource.getParentLogger();
    }
}