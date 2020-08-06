/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.persistence.scripts;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Persistence;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static java.util.Arrays.asList;

/**
 * Utility class for generating DDL scripts (create and drop) please ignore it.
 */
@RunWith(Parameterized.class)
@Ignore
public class GenerateDDLScriptsTests {

    private static class ScriptFile {

        private String dialect;
        private String alias;
        private String prefix;
        private boolean newGenerator;

        public ScriptFile(String dialect, String alias, boolean newGenerator) {
            this(dialect, alias, alias, newGenerator);
        }

        public ScriptFile(String dialect, String alias, String prefix, boolean newGenerator) {
            this.dialect = dialect;
            this.alias = alias;
            this.prefix = prefix;
            this.newGenerator = newGenerator;
        }

        public Path buildCreateFile(Path basePath) {
            return basePath.resolve(alias).resolve(prefix + "-" + (this.newGenerator ? "springboot-" : "") + "jbpm-schema.sql");
        }

        public Path buildDropFile(Path basePath) {
            return basePath.resolve(alias).resolve(prefix + "-" + (this.newGenerator ? "springboot-" : "") + "jbpm-drop-schema.sql");
        }

        public String getNewGenerator() {
            return Boolean.toString(this.newGenerator);
        }
        public String getDialect() {
            return this.dialect;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((dialect == null) ? 0 : dialect.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ScriptFile other = (ScriptFile) obj;
            if (dialect == null) {
                if (other.dialect != null)
                    return false;
            } else if (!dialect.equals(other.dialect))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return alias + " New Generator mappings " + this.newGenerator;
        }
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<ScriptFile> dialect() {
        return asList(new ScriptFile("org.hibernate.dialect.Oracle12cDialect", "oracle", true));
    }

    private ScriptFile scriptFile;

    public GenerateDDLScriptsTests(ScriptFile scriptFile) {
        this.scriptFile = scriptFile;
    }

    @Test
    public void generateDDL() throws Exception {
        Path basePath = Paths.get("src", "main", "resources", "db", "ddl-scripts");

        Path createFilePath = scriptFile.buildCreateFile(basePath);
        Path dropFilePath = scriptFile.buildDropFile(basePath);

        Files.deleteIfExists(createFilePath);
        Files.deleteIfExists(dropFilePath);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", this.scriptFile.getDialect());
        properties.put("hibernate.id.new_generator_mappings", this.scriptFile.getNewGenerator());
        properties.put("javax.persistence.schema-generation.scripts.action", "drop-and-create");
        properties.put("javax.persistence.schema-generation.scripts.drop-target", dropFilePath.toString());
        properties.put("javax.persistence.schema-generation.scripts.create-target", createFilePath.toString());
        Persistence.generateSchema("dbGenerateDDL", properties);
    }
}
