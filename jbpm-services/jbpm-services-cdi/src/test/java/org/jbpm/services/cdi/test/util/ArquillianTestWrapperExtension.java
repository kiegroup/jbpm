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
package org.jbpm.services.cdi.test.util;

import java.util.Properties;

import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jbpm.test.persistence.util.PersistenceUtil;
import org.kie.test.util.db.DataSourceFactory;
import org.kie.test.util.db.PoolingDataSourceWrapper;

/**
 * Custom extension for arquillian to setup data source for all the tests that can be closed properly
 */
public class ArquillianTestWrapperExtension implements LoadableExtension {
    @Override
    public void register(ExtensionBuilder builder) {
        builder.observer(DataSourceHandler.class);
    }

    public static class DataSourceHandler {
        private PoolingDataSourceWrapper ds;
        
        public void init(@Observes BeforeSuite event, ContainerRegistry registry) {
            Properties driverProperties = new Properties();
            driverProperties.put("user", PersistenceUtil.getDatasourceProperties().getProperty("user"));
            driverProperties.put("password", PersistenceUtil.getDatasourceProperties().getProperty("password"));
            driverProperties.put("url", PersistenceUtil.getDatasourceProperties().getProperty("url"));
            driverProperties.put("driverClassName", PersistenceUtil.getDatasourceProperties().getProperty("driverClassName"));
            driverProperties.put("className", PersistenceUtil.getDatasourceProperties().getProperty("className"));
            driverProperties.put("databaseName", PersistenceUtil.getDatasourceProperties().getProperty("databaseName"));
            driverProperties.put("serverName", PersistenceUtil.getDatasourceProperties().getProperty("serverName"));
            driverProperties.put("portNumber", PersistenceUtil.getDatasourceProperties().getProperty("portNumber"));
            
            ds = DataSourceFactory.setupPoolingDataSource("jdbc/testDS1", driverProperties);
        }
        
        public void close(@Observes AfterSuite event, ContainerRegistry registry) {
            ds.close();
        }
    }
}
