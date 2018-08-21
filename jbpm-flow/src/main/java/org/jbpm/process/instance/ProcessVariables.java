/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.process.instance;

import java.beans.BeanInfo;
import java.beans.FeatureDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jbpm.process.core.context.variable.LambdaValueReference;
import org.jbpm.process.core.context.variable.SimpleValueReference;
import org.jbpm.process.core.context.variable.ValueReference;
import org.kie.api.runtime.rule.RuleUnit;

public abstract class ProcessVariables implements Serializable {

    public static <T extends RuleUnit> ProcessVariables.Typed<T> typed(T value) {
        return new Typed<>(value);
    }

    public static ProcessVariables.Untyped untyped(Map<String, Object> parameters) {
        return new Untyped(parameters == null ? Collections.emptyMap() : parameters);
    }

    public abstract Map<String, ValueReference<?>> variables();

    static class Typed<T> extends ProcessVariables {

        private final T value;
        private final HashMap<String, PropertyDescriptor> propertyDescriptors;

        private Typed(T value) {
            this.value = value;
            try {
                BeanInfo beanInfo = Introspector.getBeanInfo(value.getClass());
                this.propertyDescriptors = new HashMap<>();
                for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
                    propertyDescriptors.put(propertyDescriptor.getName(), propertyDescriptor);
                }
            } catch (IntrospectionException e) {
                throw new IllegalArgumentException(e);
            }
        }

        public T value() {
            return value;
        }

        public Map<String, ValueReference<?>> variables() {
            return propertyDescriptors.values()
                    .stream()
                    .collect(Collectors.toMap(
                            FeatureDescriptor::getName,
                            pd -> new LambdaValueReference<>(
                                    () -> getPropertyValue(pd),
                                    v -> setPropertyValue(pd, v))));
        }

        private Object getPropertyValue(PropertyDescriptor propertyDescriptor) {
            try {
                return propertyDescriptor.getReadMethod().invoke(value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException(e);
            }
        }

        private void setPropertyValue(PropertyDescriptor propertyDescriptor, Object v) {
            try {
                propertyDescriptor.getWriteMethod().invoke(value, v);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    static class Untyped extends ProcessVariables {

        private final Map<String, Object> parameters;

        private Untyped(Map<String, Object> parameters) {
            this.parameters = parameters;
        }

        public Map<String, Object> parameters() {
            return parameters;
        }

        public Map<String, ValueReference<?>> variables() {
            Set<Map.Entry<String, Object>> entries = parameters.entrySet();
            HashMap<String, ValueReference<?>> result = new HashMap<>();
            for (Map.Entry<String, Object> entry : entries) {
                result.put(entry.getKey(), new SimpleValueReference<>(entry.getValue()));
            }
            return result;

        }
    }
}
