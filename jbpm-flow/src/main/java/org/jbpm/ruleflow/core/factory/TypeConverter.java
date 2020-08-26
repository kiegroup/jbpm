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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jbpm.process.core.datatype.DataType;
import org.jbpm.process.core.datatype.impl.type.BooleanDataType;
import org.jbpm.process.core.datatype.impl.type.EnumDataType;
import org.jbpm.process.core.datatype.impl.type.FloatDataType;
import org.jbpm.process.core.datatype.impl.type.IntegerDataType;
import org.jbpm.process.core.datatype.impl.type.ListDataType;
import org.jbpm.process.core.datatype.impl.type.ObjectDataType;
import org.jbpm.process.core.datatype.impl.type.StringDataType;

public class TypeConverter {

    private TypeConverter() {}

    private static final Map<Class<?>, DataType> classTypeMap = new ConcurrentHashMap<>();

    static {
        classTypeMap.put(Boolean.class, new BooleanDataType());
        classTypeMap.put(Integer.class, new IntegerDataType());
        classTypeMap.put(Float.class, new FloatDataType());
        classTypeMap.put(String.class, new StringDataType());
        classTypeMap.put(List.class, new ListDataType());
    }

    public static DataType fromType(Class<?> type) {
        return classTypeMap.computeIfAbsent(type, TypeConverter::missingDataType);
    }

    private static DataType missingDataType(Class<?> type) {
        if (Enum.class.isAssignableFrom(type)) {
            return new EnumDataType(type.getName());
        } else {
            return new ObjectDataType(type.getName());
        }
    }

}
