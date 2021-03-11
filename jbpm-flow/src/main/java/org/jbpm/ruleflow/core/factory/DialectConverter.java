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
package org.jbpm.ruleflow.core.factory;

import java.util.EnumMap;
import java.util.Map;

import org.drools.mvel.java.JavaDialect;
import org.kie.api.fluent.Dialect;

public class DialectConverter {

    private DialectConverter() {}

    // this is not stored in the enum to make in independent of the implementation
    private static final Map<Dialect, String> dialectMap = new EnumMap<>(Dialect.class);
    static {
        dialectMap.put(Dialect.JAVA, JavaDialect.ID);
        dialectMap.put(Dialect.JAVASCRIPT, "JavaScript");
        dialectMap.put(Dialect.MVEL, "mvel");
        dialectMap.put(Dialect.FEEL, "FEEL");
        dialectMap.put(Dialect.XPATH, "XPath");
    }

    public static String fromDialect(Dialect dialect) {
        return dialectMap.get(dialect);
    }
}
