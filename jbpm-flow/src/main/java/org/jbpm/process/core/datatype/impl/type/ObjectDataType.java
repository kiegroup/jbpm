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

package org.jbpm.process.core.datatype.impl.type;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.format.DateTimeParseException;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;
import org.drools.reflective.classloader.ProjectClassLoader;
import org.jbpm.process.core.datatype.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.soup.xstream.XStreamUtils.createTrustingXStream;

/**
 * Representation of an object datatype.
 */
public class ObjectDataType implements DataType {

    protected static final transient Logger logger = LoggerFactory.getLogger(ObjectDataType.class);

    private static final long serialVersionUID = 510l;

    private String className;

    private ClassLoader classLoader;
    
    public ObjectDataType() {
    }

    public ObjectDataType(String className) {
        setClassName(className);
    }

    public ObjectDataType(String className, ClassLoader classLoader) {
        setClassName(className);
        setClassLoader(classLoader);
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        className = (String) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(className);
    }

    public boolean verifyDataType(final Object value) {
        if (value == null || className == null) {
            return true;
        }
        try {
            Class<?> clazz = Class.forName(className, true, value.getClass().getClassLoader());
            if ("java.time.LocalDateTime".equals(className)) {
                if (clazz.isInstance(parseDateObject(value))) {
                    return true;
                }
            }
            if (clazz.isInstance(value)) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            return false;
        }
        return false;
    }

    private Object parseDateObject(Object value) {
        if (value instanceof java.time.LocalDateTime) {
            return value;
        }
        try {
            value = java.time.LocalDateTime.parse(value.toString());
        } catch (DateTimeParseException exception) {
            logger.warn(exception.getMessage());
        }
        return value;
    }

    public Object readValue(String value) {
        return getXStream().fromXML(value);
    }

    public String writeValue(Object value) {
        return getXStream().toXML(value);
    }

    private XStream getXStream() {
        XStream xstream = createTrustingXStream();
        if (classLoader != null) {
            xstream.setClassLoader(classLoader);
            if (classLoader instanceof ProjectClassLoader ) {
                Map<String, byte[]> store = ((ProjectClassLoader) classLoader).getStore();
                if (store != null) {
                    String[] classes = store.keySet().stream()
                                            .map( s -> s.replace( '/', '.' ) )
                                            .map( s -> s.endsWith( ".class" ) ? s.substring( 0, s.length() - ".class".length() ) : s )
                                            .toArray( String[]::new );
                    xstream.addPermission( new ExplicitTypePermission( classes ) );
                }
            }
        }
        return xstream;
    }

    public String getStringType() {
        return className == null ? "java.lang.Object" : className;
    }
}
