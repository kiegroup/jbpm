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

package org.jbpm.process.workitem.bpmn2;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JavaType;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.ClassOrInterfaceType;


public class TypeTransformer {

    private ObjectMapper mapper;
    private ClassLoader classLoader;
    
    public TypeTransformer() {
        this(TypeTransformer.class.getClassLoader());
    }
    public TypeTransformer(ClassLoader classLoader) {
        this.mapper = new ObjectMapper();
        this.classLoader = classLoader;
    }

    public Object transform(Object toMarshal, String className) throws ClassNotFoundException, IOException {
        JavaParser parser = new JavaParser();
        ParseResult<Type> unit = parser.parseType(className);
        if(!unit.isSuccessful()) {
            return toMarshal;
        }
        ClassOrInterfaceType type = (ClassOrInterfaceType) unit.getResult().get();
        Class<?> targetClazz = classLoader.loadClass(toString(type));
        if(Collection.class.isAssignableFrom(targetClazz) && type.getTypeArguments().isPresent()) {
            // it is a generic so we try to read it.
            ClassOrInterfaceType argument = (ClassOrInterfaceType) type.getTypeArguments().get().get(0);
            Class<?> genericType = classLoader.loadClass(toString(argument));
            JavaType targetGenericType = mapper.getTypeFactory().constructCollectionType(List.class, genericType);
            return mapper.convertValue(toMarshal, targetGenericType);
        }

        return mapper.convertValue(toMarshal, targetClazz);
        
    }

    private String toString(ClassOrInterfaceType type) {
        StringBuilder str = new StringBuilder();
        type.getScope().ifPresent(s -> str.append(s.asString()).append("."));
        str.append(type.getNameAsString());
        return str.toString();
    }
}
