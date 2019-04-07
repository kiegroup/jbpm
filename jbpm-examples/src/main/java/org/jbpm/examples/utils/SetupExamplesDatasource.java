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

package org.jbpm.examples.utils;

import java.util.Properties;

import org.kie.test.util.db.DataSourceFactory;
import org.kie.test.util.db.PoolingDataSourceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetupExamplesDatasource {
    private static final Logger logger = LoggerFactory.getLogger(SetupExamplesDatasource.class);

	public static PoolingDataSourceWrapper setupPoolingDataSource() {
        Properties driverProperties = new Properties();
        driverProperties.put("user", "sa");
        driverProperties.put("password", "");
        driverProperties.put("url", "jdbc:h2:mem:jbpm-db;MVCC=true");
        driverProperties.put("driverClassName", "org.h2.Driver");
        driverProperties.put("className", "org.h2.jdbcx.JdbcDataSource");
        
        PoolingDataSourceWrapper pds = null;
        try {
            pds = DataSourceFactory.setupPoolingDataSource("jdbc/jbpm-ds", driverProperties);
        } catch (Exception e) {
            logger.warn("DBPOOL_MGR:Looks like there is an issue with creating db pool because of " + e.getMessage() + " cleaing up...");
            logger.debug("DBPOOL_MGR: attempting to create db pool again...");
            pds = DataSourceFactory.setupPoolingDataSource("jdbc/jbpm-ds", driverProperties);
            logger.debug("DBPOOL_MGR:Pool created after cleanup of leftover resources");
        }
        return pds;
    }

}
