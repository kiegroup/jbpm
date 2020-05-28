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

package org.jbpm.test.persistence.scripts.quartzdialects;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;

public class QuartzDialectResolver implements DialectResolver {
 
    private static final long serialVersionUID = 1L;
    private static Map<String, Dialect> DIALECT_BY_NAME = new HashMap<>();

    {
        registerDialect(new MySQLCustomDialect(), "MySQL", "MariaDB");
        registerDialect(new DB2CustomDialect(), "DB2/LINUXX8664");
        registerDialect(new OracleCustomDialect(), "Oracle");
        registerDialect(new PostgreSQLCustomDialect(), "PostgreSQL", "EnterpriseDB");
        registerDialect(new SQLServerCustomDialect(), "Microsoft SQL Server");
        registerDialect(new SybaseCustomDialect(), "Adaptive Server Enterprise");
    }
 
    private static void registerDialect(Dialect dialect, String... databaseNames) {
        Stream.of(databaseNames).forEach(x->DIALECT_BY_NAME.put(x, dialect));
    }

    @Override
    public Dialect resolveDialect(DialectResolutionInfo info){
        return DIALECT_BY_NAME.get(info.getDatabaseName());
    }
}