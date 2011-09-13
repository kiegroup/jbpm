/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.formbuilder.server.render.gwt;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.jbpm.formbuilder.server.render.RendererException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class Renderer implements org.jbpm.formbuilder.server.render.Renderer {

    private final VelocityEngine engine = new VelocityEngine();
    private Template template = null;
    
    @Override
    public Object render(URL url, Map<String, Object> inputData) throws RendererException {
        URL velocityTemplate = getClass().getResource("/langs/gwt/index.vm");
        if (velocityTemplate == null) {
            throw new RendererException("Couldn't find index.vm");
        }
        synchronized (this) {
            if (template == null) {
                template = engine.getTemplate("index.vm");
            }
        }
        try {
            String formContent = IOUtils.toString(url.openStream());
            JsonObject json = new JsonObject();
            json.addProperty("form", formContent);
            json.add("formData", toJsonObject(inputData));
            VelocityContext context = new VelocityContext();
            context.put("formContent", json.toString());
            StringWriter writer = new StringWriter();
            template.merge(context, writer);
            return writer.toString();
        } catch (IOException e) {
            throw new RendererException("Problem reading index.vm", e);
        }
    }
    
    protected JsonObject toJsonObject(Map<String, Object> inputData) {
        JsonObject retval = new JsonObject();
        if (inputData != null) {
            for (String key : inputData.keySet()) {
                Object value = inputData.get(key);
                retval.add(key, asJsonValue(value));
            }
        }
        return retval;
    }
    
    private JsonElement asJsonValue(Object value) {
        if (value == null) {
            return new JsonNull();
        } else if (value.getClass().isArray()) {
            Object[] arr = (Object[]) value;
            JsonArray retval = new JsonArray();
            for (Object sub : arr) {
                retval.add(asJsonValue(sub));
            }
            return retval;
        } else if (value instanceof Boolean) {
            return new JsonPrimitive((Boolean) value);
        } else if (value instanceof Number) {
            return new JsonPrimitive((Number) value);
        } else if (value instanceof String) {
            return new JsonPrimitive((String) value);
        } else {
            Map<String, Object> retval = new HashMap<String, Object>();
            Field[] fields = value.getClass().getFields();
            for (Field field : fields) {
                try {
                    Object subVal = PropertyUtils.getProperty(value, field.getName());
                    retval.put(field.getName(), subVal);
                } catch (Exception e) {
                    retval.put(field.getName(), null);
                }
            }
            return toJsonObject(retval);
        }
    }

}
