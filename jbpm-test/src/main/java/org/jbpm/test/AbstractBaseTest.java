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

package org.jbpm.test;

import java.util.Properties;

import org.jbpm.process.instance.impl.util.LoggingPrintStream;
import org.jbpm.test.persistence.util.PersistenceUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.kie.test.util.db.PoolingDataSourceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractBaseTest.class);

    protected static final String DATASOURCE_NAME = "jdbc/jbpm-ds";

    @BeforeClass
    public static void configure() {
        LoggingPrintStream.interceptSysOutSysErr();
    }
    
    @AfterClass
    public static void reset() {
        LoggingPrintStream.resetInterceptSysOutSysErr();
    }

    protected String getJndiDatasourceName(){
        return DATASOURCE_NAME;
    }

    protected Properties getDataSourceProperties(){
        return PersistenceUtil.getDatasourceProperties();
    }

    protected static PoolingDataSourceWrapper setupPoolingDataSource(String datasourceName) {
        return setupDataSource(PersistenceUtil.getDatasourceProperties(), datasourceName);
    }

    protected PoolingDataSourceWrapper setupPoolingDataSource() {
        return setupDataSource(getDataSourceProperties(), getJndiDatasourceName());
    }

    private static PoolingDataSourceWrapper setupDataSource(Properties dsProps, String datasourceName) {
        PoolingDataSourceWrapper pds;
        try {
            pds = PersistenceUtil.setupPoolingDataSource(dsProps, datasourceName);
        } catch (Exception e) {
            logger.warn("DBPOOL_MGR:Looks like there is an issue with creating db pool because of {} cleaning up...", e.getMessage());
            logger.info("DBPOOL_MGR: attempting to create db pool again...");
            pds = PersistenceUtil.setupPoolingDataSource(dsProps, datasourceName);

            logger.info("DBPOOL_MGR:Pool created after cleanup of leftover resources");
        }
        return pds;
    }
}
