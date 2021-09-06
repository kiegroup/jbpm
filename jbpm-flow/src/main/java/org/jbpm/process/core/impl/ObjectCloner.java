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
package org.jbpm.process.core.impl;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectCloner {

    private ObjectCloner() {}

    public static class Config {

        private boolean deepCloneCollections = true;

        public Config deepCloneCollections(boolean deepCloneCollections) {
            this.deepCloneCollections = deepCloneCollections;
            return this;
        }

        public boolean isDeepCloneCollections() {
            return deepCloneCollections;
        }
    }

    private static Map<Class<?>, UnaryOperator<Object>> constructorExecutorMap = Collections.synchronizedMap(new WeakHashMap<>());
    private static Collection<Class<?>> unmutableClasses = ConcurrentHashMap.newKeySet();
    private static UnaryOperator<Object> identity = t -> t;
    private static final Logger logger = LoggerFactory.getLogger(ObjectCloner.class);

    public static Object clone(Object object) {
        return clone(object, new Config());
    }

    public static Object clone(Object object, Config config) {
        Object result;
        if (object == null || object instanceof Boolean || object instanceof Number || object instanceof CharSequence || object instanceof Enum)
            result = object;
        else if (object.getClass().isArray()) {
            result = cloneArray(object);
        } else if (object instanceof Collection) {
            result = cloneCollection((Collection) object, config);
        } else if (object instanceof Map) {
            result = cloneMap((Map) object, config);
        } else {
            result = cloneObject(object);
        }
        return result;
    }

    private static Object cloneArray(Object array) {
        int size = Array.getLength(array);
        Object result = Array.newInstance(array.getClass().getComponentType(), size);
        System.arraycopy(array, 0, result, 0, size);
        return result;
    }

    private static Object cloneCollection(Collection<?> collection, Config config) {
        if (collection.isEmpty()) {
            return collection;
        }
        return config.deepCloneCollections ? deepCloneCollection(collection) : lightCloneCollectionMap(collection, Collection.class);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Collection deepCloneCollection(Collection<?> object) {
        Collection result = null;
        Optional<Constructor<?>> constructor = getCollectionConstructor(object.getClass());
        if (constructor.isPresent()) {
            try {
                result = (Collection) constructor.get().newInstance();
            } catch (ReflectiveOperationException e) {
                logger.warn("Unexpected exception invoking default constructor of type {}", object.getClass());
            }
        }
        if (result == null) {
            result = new ArrayList<>();
        }
        for (Object item : object) {
            result.add(clone(item));
        }
        return result;
    }

    private static Object cloneMap(Map<?, ?> map, Config config) {
        if (map.isEmpty()) {
            return map;
        }
        return config.deepCloneCollections ? deepCloneMap(map) : lightCloneCollectionMap(map, Map.class);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Map deepCloneMap(Map<?, ?> object) {
        Map result = null;
        Optional<Constructor<?>> constructor = getCollectionConstructor(object.getClass());

        if (constructor.isPresent()) {
            try {

                result = (Map) constructor.get().newInstance();
            } catch (ReflectiveOperationException e) {
                logger.warn("Unexpected exception invoking default constructor of type {}", object.getClass());
            }
        }

        if (result == null) {
            result = new HashMap<>();
        }
        for (Entry item : object.entrySet()) {
            result.put(item.getKey(), clone(item.getValue()));
        }
        return result;
    }

    private static Object lightCloneCollectionMap(Object object, Class<?> paramType) {
        return getCollectionConstructor(object.getClass(), paramType).map(c -> {
            try {
                return c.newInstance(object);
            } catch (ReflectiveOperationException e) {
                logger.warn("Unexpected exception invoking constructor {} with object {}", c, object);
                return object;
            }
        }).orElse(object);

    }
    
    private static <T> Optional<Constructor<?>> getCollectionConstructor (Class<?> collectionClass, Class<?>... paramTypes) {
        if (unmutableClasses.contains(collectionClass)) {
            return Optional.empty();
        }
        try {
            return Optional.of(collectionClass.getConstructor(paramTypes));
        } catch (NoSuchMethodException ex) {
            logger.debug("Copy constructor not found for type {}", collectionClass, ex);
            unmutableClasses.add(collectionClass);
            return Optional.empty();
        }
    }

    private static Object cloneObject(Object object) {
        Class<?> objectClass = object.getClass();

        if (object instanceof Cloneable) {
            try {
                return objectClass.getMethod("clone").invoke(object);
            } catch (ReflectiveOperationException ex) {
                logger.warn("Failing to call clone in a cloneable object!!", ex);
                return object;
            }
        } else {
            return cloneWithConstructor(object, objectClass);
        }
    }

    private static Object cloneWithConstructor(Object object, Class<?> objectClass) {
        return constructorExecutorMap.computeIfAbsent(objectClass, ObjectCloner::getConstructorExecutor).apply(object);
    }

    private static UnaryOperator<Object> getConstructorExecutor(Class<?> objectClass) {
        ConstructorInfo constructorInfo = new ConstructorInfo(objectClass);
        UnaryOperator<Object> result = constructorInfo.getCopyConstructor() != null ? new CopyConstructorExecutor(constructorInfo.getCopyConstructor()) : getConstructorExecutor(objectClass, constructorInfo);
        if (result == null) {
            logger.info("No suitable constructor found for type {}", objectClass);
            return identity;
        }
        return result;
    }

    private static boolean inmutable(MethodInfo classInfo) {
        return classInfo.getMatchingMethods().isEmpty() && classInfo.getFields().isEmpty();
    }

    private static UnaryOperator<Object> getConstructorExecutor(Class<?> objectClass, ConstructorInfo constructorInfo) {
        MethodInfo methodInfo = new MethodInfo(objectClass);
        if (inmutable(methodInfo)) {
            logger.debug ("Inmutable object type {}", objectClass);
            return identity;
        }
        
        for (Constructor<?> constructor : constructorInfo.getOtherConstructors()) {
            Class<?>[] parameters = constructor.getParameterTypes();
            Collection<ArgResolver> args = new ArrayList<>();
            boolean found = true;
            for (int i = 0; found && i < parameters.length; i++) {
                ArgResolver arg = getArg(parameters[i], methodInfo);
                found = arg != null;
                if (found) {
                    args.add(arg);
                }
            }
            if (found) {
                return new ArgsConstructorExecutor(constructor, args, methodInfo);
            }
        }
        return constructorInfo.getDefaultConstructor() != null ? new DefaultConstructorExecutor(constructorInfo.getDefaultConstructor(), methodInfo) : null;
    }

    private static ArgResolver getArg(Class<?> parameterType, MethodInfo classInfo) {
        for (Method m : classInfo.getMissingGetterMethods()) {
            if (parameterType.isAssignableFrom(m.getReturnType())) {
                return m::invoke;
            }
        }

        for (Method m : classInfo.getMatchingMethods().values()) {
            if (parameterType.isAssignableFrom(m.getReturnType())) {
                return m::invoke;
            }
        }

        for (Field f : classInfo.getFields()) {
            if (parameterType.isAssignableFrom(f.getType())) {
                return f::get;
            }
        }
        return null;
    }
    
    private static class ConstructorInfo {

        private Collection<Constructor<?>> otherConstructors = new ArrayList<>();
        private Constructor<?> defaultConstructor;
        private Constructor<?> copyConstructor;

        public ConstructorInfo(Class<?> objectClass) {
            Constructor<?>[] constructors = objectClass.getConstructors();
            for (Constructor<?> constructor : constructors) {
                if (constructor.getParameterCount() == 0) {
                    defaultConstructor = constructor;
                } else if (constructor.getParameterCount() == 1 && constructor.getParameterTypes()[0].isAssignableFrom(objectClass)) {
                    copyConstructor = constructor;
                    break;
                } else {
                    otherConstructors.add(constructor);
                }
            }
        }

        public Collection<Constructor<?>> getOtherConstructors() {
            return otherConstructors;
        }

        public Constructor<?> getDefaultConstructor() {
            return defaultConstructor;
        }

        public Constructor<?> getCopyConstructor() {
            return copyConstructor;
        }
    }

    private static class MethodInfo {

        private enum MemberType {
            SET,
            GET,
            OTHER
        }

        private static final Set<String> excluded = new HashSet<>(Arrays.asList("equals", "toString", "wait", "getClass", "hashCode"));

        private Map<Method, Method> matchingMethods = new HashMap<>();
        private Collection<Field> fields = new ArrayList<>();
        private Collection<Method> missingGetterMethods = new ArrayList<>();

        public MethodInfo(Class<?> objectClass) {
            Method[] methods = objectClass.getMethods();
            boolean[] alreadyPaired = new boolean[methods.length];
            for (int i = 0; i < methods.length; i++) {
                if (!alreadyPaired[i]) {
                    Method method = methods[i];
                    MemberType memberType = getType(method);
                    if (memberType != MemberType.OTHER) {
                        boolean found = false;
                        for (int j = i + 1; !found && j < methods.length; j++) {
                            if (!alreadyPaired[j]) {
                                found = isMatch(method, memberType, methods[j]);
                                if (found) {
                                    alreadyPaired[j] = true;
                                    addMatchMethod(method, memberType, methods[j]);
                                }
                            }
                        }
                        if (!found) {
                            addMissingMethod(methods[i], memberType);
                        }
                    }
                }
            }
            Field[] classFields = objectClass.getFields();
            for (Field field : classFields) {
                int modifiers = field.getModifiers();
                if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
                    fields.add(field);
                }
            }
        }

        private static MemberType getType(Method method) {
            boolean isVoid = method.getReturnType().equals(void.class);
            int count = method.getParameterCount();
            if (!isVoid && count == 0 && !excluded.contains(method.getName())) {
                return MemberType.GET;
            } else if (isVoid && count == 1 && !excluded.contains(method.getName())) {
                return MemberType.SET;
            } else {
                return MemberType.OTHER;
            }
        }

        private void addMatchMethod(Method method, MemberType memberType, Method match) {
            if (memberType == MemberType.SET) {
                matchingMethods.put(method, match);
            } else {
                matchingMethods.put(match, method);
            }
        }

        private void addMissingMethod(Method method, MemberType type) {
            if (type == MemberType.GET) {
                missingGetterMethods.add(method);
            }
        }

        private boolean isMatch(Method input, MemberType memberType, Method potential) {
            MemberType potentialType = getType(potential);
            return memberType == MemberType.GET && potentialType == MemberType.SET && input.getReturnType().equals(potential.getParameterTypes()[0]) 
                       && matchingNames(input.getName().replace("get", ""), potential.getName().replace("set","")) ||
                   memberType == MemberType.SET && potentialType == MemberType.GET && input.getParameterTypes()[0].equals(potential.getReturnType()) 
                       && matchingNames(input.getName().replace("set", ""), potential.getName().replace("get",""));
        }

        private boolean matchingNames(String name, String candidate) {
            return name.equals(candidate);
        }

        public Map<Method, Method> getMatchingMethods() {
            return matchingMethods;
        }

        public Collection<Field> getFields() {
            return fields;
        }

        public Collection<Method> getMissingGetterMethods() {
            return missingGetterMethods;
        }
    }

    private abstract static class ConstructorExecutor implements UnaryOperator<Object> {

        private Constructor<?> constructor;

        public ConstructorExecutor(Constructor<?> constructor) {
            this.constructor = constructor;
        }

        public Object apply(Object object) {
            Object result;
            try {
                result = constructor.newInstance(args(object));
                postConstruct(object, result);
                return result;
            } catch (ReflectiveOperationException e) {
                logger.error("Introspection error while cloning object {} of type {}", object, object.getClass(), e);
                return object;
            }
        }

        protected abstract Object[] args(Object object) throws ReflectiveOperationException;

        protected void postConstruct(Object src, Object dest) throws ReflectiveOperationException {}
    }

    private static class CopyConstructorExecutor extends ConstructorExecutor {

        public CopyConstructorExecutor(Constructor<?> constructor) {
            super(constructor);
        }

        protected Object[] args(Object object) {
            return new Object[]{object};
        }
    }

    private abstract static class MatcherConstructorExecutor extends ConstructorExecutor {

        private final Map<Method, Method> matchingMethods;
        private final Collection<Field> fields;

        public MatcherConstructorExecutor(Constructor<?> constructor, MethodInfo methodInfo) {
            super(constructor);
            this.matchingMethods = methodInfo.getMatchingMethods();
            this.fields = methodInfo.getFields();
        }

        @Override
        protected void postConstruct(Object src, Object dest) throws ReflectiveOperationException {
            for (Entry<Method, Method> entry : matchingMethods.entrySet()) {
                entry.getKey().invoke(dest, ObjectCloner.clone(entry.getValue().invoke(src)));
            }
            for (Field field : fields) {
                field.set(dest, ObjectCloner.clone(field.get(src)));
            }
        }
    }

    private static class DefaultConstructorExecutor extends MatcherConstructorExecutor {

        public DefaultConstructorExecutor(Constructor<?> constructor, MethodInfo methodInfo) {
            super(constructor, methodInfo);
        }

        @Override
        protected Object[] args(Object object) {
            return new Object[0];
        }
    }

    private static class ArgsConstructorExecutor extends MatcherConstructorExecutor {

        private Collection<ArgResolver> argResolvers;

        public ArgsConstructorExecutor(Constructor<?> constructor, Collection<ArgResolver> argResolvers, MethodInfo methodInfo) {
            super(constructor, methodInfo);
            this.argResolvers = argResolvers;
        }

        @Override
        protected Object[] args(Object object) throws ReflectiveOperationException {
            Object[] args = new Object[argResolvers.size()];
            int i = 0;
            for (ArgResolver argResolver : argResolvers) {
                args[i++] = ObjectCloner.clone(argResolver.apply(object));
            }
            return args;
        }
    }

    @FunctionalInterface
    private interface ArgResolver {

        Object apply(Object object) throws ReflectiveOperationException;
    }
}
