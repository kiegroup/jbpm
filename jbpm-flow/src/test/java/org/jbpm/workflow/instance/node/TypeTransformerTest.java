/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.workflow.instance.node;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.process.instance.impl.util.TypeTransformer;
import org.jbpm.process.test.Person;
import org.junit.Assert;
import org.junit.Test;

public class TypeTransformerTest {

    @Test
    public void pojoTest() throws Exception {
        TypeTransformer typeTransformer = new TypeTransformer();
        Map<String, Object> data= new LinkedHashMap<>();
        data.put("age", 12);
        Object person = typeTransformer.transform(data, Person.class.getCanonicalName());
        Assert.assertTrue(person instanceof Person);
    }

    @Test
    public void colletionTest() throws Exception {
        TypeTransformer typeTransformer = new TypeTransformer();
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> p1 = new LinkedHashMap<>();
        p1.put("age", 12);
        data.add(p1);
        Object outcome = typeTransformer.transform(data, "java.util.List<org.jbpm.process.test.Person>");
        Assert.assertTrue(outcome instanceof java.util.List);
        List<Person> persons = (List<Person>) outcome;
        Assert.assertTrue(persons.get(0) instanceof Person);
    }
}
