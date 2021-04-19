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

package org.jbpm.test.persistence.scripts.util;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;
import java.util.stream.Collectors;

import org.jbpm.test.persistence.scripts.DatabaseType;
import org.jbpm.test.persistence.scripts.PersistenceUnit;
import org.jbpm.test.persistence.scripts.TestPersistenceContextBase;
import org.jbpm.test.persistence.util.PersistenceUtil;
import org.jbpm.test.util.DatabaseScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains util methods that are used for testing SQL scripts.
 */
public final class TestsUtil {

    private static final Logger logger = LoggerFactory.getLogger(TestsUtil.class);

    /**
     * Gets SQL scripts for selected database type.
     * @param folderWithDDLs Root folder containing SQL scripts for all database types.
     * @param databaseType Database type.
     * @param scriptFilter Indicates the filter to apply, including springboot or not scripts and create/drop scripts
     * @return Array of SQL script files. If there are no SQL script files found, returns empty array.
     */
    public static File[] getDDLScriptFilesByDatabaseType(final File folderWithDDLs,
                                                         final DatabaseType databaseType,
                                                         final ScriptFilter scriptFilter) {
        final File folderWithScripts = new File(folderWithDDLs.getPath() + File.separator + databaseType.getScriptsFolderName());
        
        if (!folderWithScripts.exists()) {
            logger.warn("Folder with DDLs doesn't exist {}", folderWithDDLs);
            return new File[0];
        }

        File[] foundFiles = Arrays.asList(folderWithScripts.listFiles()).stream().filter(scriptFilter.build()).toArray(File[]::new);
           
        foundFiles = Arrays.stream(foundFiles).map(DatabaseScript::new).sorted().map(DatabaseScript::getScript).toArray(File[]::new);

        if (databaseType.equals(DatabaseType.POSTGRESQL)) {               
             //Returns first schema sql
             Arrays.sort(foundFiles, Comparator.<File, Boolean>comparing(s -> s.getName().contains("schema")).reversed());
        }
            
        logger.info("Returned DDL files: {}", Arrays.stream(foundFiles).map(File::getName).collect(Collectors.toList()));
        return foundFiles;
    }

    /**
     * Gets database type based on dialect property specified in the datasource.properties file based in default
     * path /datasource.properties.
     * @return Database type based on specified dialect property. If no dialect is specified,
     * returns H2 database type.
     */
    public static DatabaseType getDatabaseType() {
        return getDatabaseType(PersistenceUtil.getDatasourceProperties());
    }

    /**
     * Gets database type based on dialect property specified in data source properties.
     * @param dataSourceProperties Data source properties.
     * @return Database type based on specified dialect property. If no dialect is specified,
     * returns H2 database type.
     */
    public static DatabaseType getDatabaseType(final Properties dataSourceProperties) {
        final String hibernateDialect = dataSourceProperties.getProperty("dialect");
        if (!"".equals(hibernateDialect)) {
            return getDatabaseTypeBySQLDialect(hibernateDialect);
        } else {
            return DatabaseType.H2;
        }
    }

    /**
     * Gets database type based on specified SQL dialect.
     * @param sqlDialect SQL dialect.
     * @return Database type based on specified SQL dialect.
     * If specified SQL dialect is not supported, throws IllegalArgumentException.
     */
    public static DatabaseType getDatabaseTypeBySQLDialect(final String sqlDialect) {
        if (sqlDialect.contains("DB2Dialect")) {
            return DatabaseType.DB2;
        } else if (sqlDialect.contains("DerbyDialect")) {
            return DatabaseType.DERBY;
        } else if (sqlDialect.contains("H2Dialect")) {
            return DatabaseType.H2;
        } else if (sqlDialect.contains("HSQLDialect")) {
            return DatabaseType.HSQLDB;
        } else if (sqlDialect.contains("MySQL5Dialect")) {
            return DatabaseType.MYSQL5;
        } else if (sqlDialect.contains("MySQL5InnoDBDialect")) {
            return DatabaseType.MYSQLINNODB;
        } else if (sqlDialect.contains("Oracle")) {
            return DatabaseType.ORACLE;
        } else if (sqlDialect.contains("Postgre")) {
            return DatabaseType.POSTGRESQL;
        } else if (sqlDialect.contains("SQLServer2008Dialect") || sqlDialect.contains("SQLServer2012Dialect")) {
            return DatabaseType.SQLSERVER2008;
        } else if (sqlDialect.contains("SQLServerDialect") || sqlDialect.contains("SQLServer2005Dialect")) {
            return DatabaseType.SQLSERVER;
        } else if (sqlDialect.contains("SybaseASE157Dialect")){
            return DatabaseType.SYBASE;
        } else {
            throw new IllegalArgumentException("SQL dialect type " + sqlDialect + " is not supported!");
        }
    }

    public static byte[] hexStringToByteArray(final String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Clears database schema.
     */
    public static void clearSchema() {
        final TestPersistenceContextBase clearSchemaContext = new TestPersistenceContextBase();
        clearSchemaContext.init(PersistenceUnit.CLEAR_SCHEMA);
        clearSchemaContext.clean();
    }

    private TestsUtil() {
        // It makes no sense to create instances of util classes.
    }
        
}
