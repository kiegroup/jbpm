package org.jbpm.formbuilder.server.form;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class FormItemRepresentationTest extends TestCase {

    public void testGetData() throws Exception {
        //get class names
        List<String> classNames = new ArrayList<String>();
        URL dir = getClass().getResource("/org/jbpm/formbuilder/shared/rep/items/");
        File file = new File(dir.getFile());
        String[] classFiles = file.list();
        for (String classFile : classFiles) {
            if (!classFile.contains("$")) {
                classNames.add("org.jbpm.formbuilder.shared.rep.items." + classFile.replace(".class", ""));
            }
        }
        //get classes
        List<Class<?>> classes = new ArrayList<Class<?>>();
        for (String className : classNames) {
            Class<?> klass = Class.forName(className);
            if (FormItemRepresentation.class.isAssignableFrom(klass)) {
                classes.add(klass);
            }
        }
        //create instances
        Map<Class<?>, FormItemRepresentation> instances = new HashMap<Class<?>, FormItemRepresentation>();
        for (Class<?> clazz : classes) {
            Object obj = clazz.newInstance();
            instances.put(clazz, (FormItemRepresentation) obj);
        }
        //create auxiliar map to keep uncovered fields
        Map<String, List<String>> uncovered = new HashMap<String, List<String>>();
        
        //for each object, check field coverage
        for (Class<?> clazz : instances.keySet()) {
            Field[] fields = clazz.getFields();
            List<String> uncoveredFields = new ArrayList<String>();
            FormItemRepresentation item = instances.get(clazz);
            Map<String, Object> data = item.getDataMap();
            for (Field field : fields) {
                String fieldName = field.getName();
                if (!data.containsKey(fieldName)) {
                    uncoveredFields.add(fieldName);
                }
            }
            if (!uncoveredFields.isEmpty()) {
                uncovered.put(clazz.getName(), uncoveredFields);
            }
        }
        //print messages and fail if uncovered isn't empty
        if (!uncovered.isEmpty()) {
            StringBuilder builder = new StringBuilder("FormItemRepresentation subclasses need total coverage of ");
            builder.append("fields on the getData method. However, the following exceptions were found:\n");
            for (String className : uncovered.keySet()) {
                builder.append("Class: ").append(className).append(", uncovered fields: ").append(uncovered.get(className)).append("\n");
            }
            builder.append("\nCorrect these fields and try compiling again");
            fail(builder.toString());
        }
    }
    
}
