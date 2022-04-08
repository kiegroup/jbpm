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

package org.jbpm.test.persistence.scripts.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

import org.jbpm.test.persistence.scripts.DatabaseType;
import org.jbpm.test.persistence.scripts.DistributionType;

public class ScriptFilter {

    public enum Option {
        DISALLOW_EMPTY_RESULTS, // if the filter allow no results
        THROW_ON_SCRIPT_ERROR, // if the filter allows script errors
    }

    public enum Filter {
        IN,
        OUT
    }

    private Set<DatabaseType> dbTypes;
    private Set<Option> options;
    private List<Predicate<File>> predicates;
    private Map<String, Object> env;

    @SafeVarargs
    public ScriptFilter(Predicate<File>... filters) {
        this.predicates = new ArrayList<>();
        this.options = new TreeSet<>();
        this.dbTypes = new TreeSet<>();
        Collections.addAll(this.dbTypes, DatabaseType.values());
        Collections.addAll(this.predicates, filters);
        env = new HashMap<>();
    }

    @SafeVarargs
    public static ScriptFilter create(Predicate<File>... filters) {
        return new ScriptFilter(filters);
    }

    public static ScriptFilter filter(Filter inOut, String... scripts) {
        if (inOut != null) {
            switch (inOut){
                case IN:
                    return new ScriptFilter(scriptFilterIn(scripts));
                case OUT:
                    return new ScriptFilter(scriptFilterOut(scripts));
            }
        }
        return new ScriptFilter(scriptFilterIn(scripts));
    }

    public static ScriptFilter filter(String... scripts) {
        return filter(Filter.IN, scripts);
    }

    public ScriptFilter include(String... scripts) {
        Predicate<File> predicate = scriptFilterIn(scripts);
        predicates.add(predicate);
        return this;
    }

    public ScriptFilter exclude(String... scripts) {
        Predicate<File> predicate = scriptFilterOut(scripts);
        predicates.add(predicate);
        return this;
    }

    private static Predicate<File> scriptFilterIn(String... scripts) {
        return Arrays.stream(scripts).map(s -> (Predicate<File>) file -> file.getName().contains(s)).reduce(x -> false, Predicate::or);
    }

    private static Predicate<File> scriptFilterOut(String... scripts) {
        return Arrays.stream(scripts).map(s -> (Predicate<File>) file -> !file.getName().contains(s)).reduce(x -> true, Predicate::and);
    }

    public ScriptFilter env(String key, Object value) {
        env.put(key, value);
        return this;
    }

    public static ScriptFilter init(boolean springboot, boolean create) {
        Predicate<File> filterExtension = file -> file.getName().toLowerCase().endsWith(".sql");

        Predicate<File> filterSpringboot = file -> file.getName().toLowerCase().contains("springboot");
        filterSpringboot = springboot ? filterSpringboot : filterSpringboot.negate();

        Predicate<File> filterBytea = file -> !file.getName().toLowerCase().contains("bytea");

        Predicate<File> filterCluster = file -> !file.getName().toLowerCase().contains("cluster");

        Predicate<File> filterName = file -> file.getName().contains("drop");
        filterName = !create ? filterName : filterName.negate();

        Predicate<File> filterTaskAssigningTables = file -> !file.getName().toLowerCase().contains("task_assigning_tables");

        ScriptFilter filter = new ScriptFilter(filterExtension, filterName, filterSpringboot, filterBytea, filterCluster, filterTaskAssigningTables);
        if (create) {
            filter.setOptions(Option.DISALLOW_EMPTY_RESULTS, Option.THROW_ON_SCRIPT_ERROR);
        }
        return filter;
    }

    public ScriptFilter setSupportedDatabase(DatabaseType... types) {
        this.dbTypes.clear();
        Collections.addAll(this.dbTypes, types);
        return this;
    }

    public ScriptFilter setDistribution(DistributionType type) {
        this.predicates.add(type.predicate);
        return this;
    }

    public boolean isSupportedDatabase(DatabaseType type) {
        return dbTypes.contains(type);
    }

    public ScriptFilter setOptions(Option... elements) {
        Collections.addAll(this.options, elements);
        return this;
    }

    public boolean hasOption(Option option) {
        return options.contains(option);
    }

    public Predicate<File> build() {
        return predicates.stream().reduce(x -> true, Predicate::and);
    }

    public Set<DatabaseType> getSupportedDatabase() {
        return this.dbTypes;
    }

    public Map<String, Object> getEnvironment() {
        return env;
    }

}