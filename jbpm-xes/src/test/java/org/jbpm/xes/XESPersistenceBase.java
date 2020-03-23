/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.xes;

import java.util.Properties;

import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.jbpm.test.persistence.util.PersistenceUtil;
import org.kie.test.util.db.PoolingDataSourceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XESPersistenceBase extends JbpmJUnitBaseTestCase {

    private static final Logger logger = LoggerFactory.getLogger(XESPersistenceBase.class);
    
    protected Properties dsProps;
    protected PoolingDataSourceWrapper pds;

    public XESPersistenceBase() {
        super(true, true);
    }

    protected PoolingDataSourceWrapper setupPoolingDataSource() {
        dsProps = PersistenceUtil.getDatasourceProperties();
        logger.info("datasource properties: {}", dsProps);
        PersistenceUtil.startH2TcpServer(dsProps);
        pds = PersistenceUtil.setupPoolingDataSource(dsProps, "jdbc/jbpm-ds");
        return pds;
    }

}
