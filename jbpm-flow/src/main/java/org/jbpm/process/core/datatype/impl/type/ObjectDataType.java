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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;
import org.drools.reflective.classloader.ProjectClassLoader;
import org.jbpm.process.core.datatype.DataType;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import static org.kie.soup.xstream.XStreamUtils.createTrustingXStream;

/**
 * Representation of an object datatype.
 */
public class ObjectDataType implements DataType {

    private static final Logger logger = LoggerFactory.getLogger(ObjectDataType.class);
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

    private static final Collection<String> prefixes = Arrays.asList("java.lang", "java.util", "java.time");

    private Optional<Class<?>> getClass(Object value) {
        try {
            return Optional.of(Class.forName(className, true, value.getClass().getClassLoader()));
        } catch (ClassNotFoundException e) {
            logger.info("Error {} loading class {}", e, className);
        }
        if (!className.contains(".")) {
            for (String prefix : prefixes) {
                String altName = prefix + "." + className;
                try {
                    return Optional.of(Class.forName(altName, true, classLoader));
                } catch (ClassNotFoundException e) {
                    logger.debug("Error {} loading class {}", e, altName);
                }
            }
        }
        return Optional.empty();
    }

    public boolean verifyDataType(final Object value) {
        if (value == null || className == null) {
            return true;
        }
        return getClass(value).map(c -> c.isInstance(value)).orElse(false);
    }

    private Optional<Object> getObjectFromClass(final Object value) {
        Optional<Class<?>> clazz = getClass(value);
        if (clazz.isPresent()) {
            Class<?> objectClass = clazz.get();
            if (objectClass.isInstance(value)) {
                return Optional.of(value);
            }
            if (Date.class.isAssignableFrom(objectClass)) {
                return Optional.of(parseDate(value.toString()));
            } else if (LocalDate.class.isAssignableFrom(objectClass)) {
                return Optional.of(LocalDate.parse((value.toString())));
            } else if (LocalDateTime.class.isAssignableFrom(objectClass)) {
                return Optional.of(LocalDateTime.parse((value.toString())));
            } else if (ZonedDateTime.class.isAssignableFrom(objectClass)) {
                return Optional.of(ZonedDateTime.parse((value.toString())));
            }
        }
        return Optional.empty();
    }

    private static Collection<DateFormat> dateFormats = Arrays.asList(new SimpleDateFormat("yyyy-MM-ddHH:mm:ss"),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"),
            new SimpleDateFormat("yyyy-MM-dd"), new SimpleDateFormat("HH:mm:ss"), DateFormat.getDateInstance(),
            DateFormat.getTimeInstance(),
            DateFormat.getDateTimeInstance());

    private static Date parseDate(String toBeParsed) {
        StringBuilder sb = new StringBuilder();
        for (DateFormat dateFormat : dateFormats) {
            try {
                return dateFormat.parse(toBeParsed);
            } catch (ParseException ex) {
                sb.append(ex.getMessage()).append(System.lineSeparator());
            }
        }
        throw new IllegalArgumentException(sb.toString());
    }

    @Override
    public Object readValue(String value) {
        return value != null ? getObjectFromClass(value).orElseGet(() -> getXStream().fromXML(value)) : null;
    }

    @Override
    public Object valueOf(String value) {
        try {
            return value != null ? getObjectFromClass(value).orElse(value) : null;
        } catch (IllegalArgumentException e) {
            return value;
        }
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
