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

package org.jbpm.casemgmt.impl.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.jbpm.casemgmt.api.generator.CaseIdGenerator;
import org.jbpm.casemgmt.api.generator.CasePrefixNotFoundException;
import org.mvel2.templates.TemplateRuntime;

import static org.jbpm.casemgmt.impl.generator.CaseIdExpressionFunctions.CASE_ID_FUNCTIONS;

/**
 * Simple in memory (usually for test or demo purpose) case id generator.
 * It does not provide any actual storage of the generated values.
 *
 */
public class InMemoryCaseIdGenerator implements CaseIdGenerator {

    private static ConcurrentMap<String, AtomicLong> sequences = new ConcurrentHashMap<>();
    private static final String IDENTIFIER = "InMemory";
    
    @Override
    public void register(String prefix) {
        sequences.putIfAbsent(prefix, new AtomicLong());
    }

    @Override
    public void unregister(String prefix) {
        sequences.remove(prefix);
    }

    @Override
    public String generate(String expression, Map<String, Object> optionalParameters) throws CasePrefixNotFoundException {
        String prefix = (String) optionalParameters.get("PREFIX");
        if (!sequences.containsKey(prefix)) {
            throw new CasePrefixNotFoundException("No case identifier prefix '" + prefix + "' was registered");
        }

        long nextVal = sequences.get(prefix).incrementAndGet();
        Map<String, Object> variables = new HashMap<>(optionalParameters);
        variables.put("SEQUENCE", nextVal);
        return (String) TemplateRuntime.eval(expression, CASE_ID_FUNCTIONS, variables);
    }
    
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

}
