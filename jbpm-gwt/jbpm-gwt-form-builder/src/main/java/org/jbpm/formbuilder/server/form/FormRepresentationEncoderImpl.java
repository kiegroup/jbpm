package org.jbpm.formbuilder.server.form;

import java.util.Map;

import org.jbpm.formbuilder.client.form.FormRepresentationEncoderClient;

public class FormRepresentationEncoderImpl extends FormRepresentationEncoderClient {

    @Override
    public Object fromMap(Map<String, Object> map) {
        Object objClassName = map.get("@className");
        if (objClassName == null) {
            return null;
        }
        String className = (String) objClassName;
        try {
            Class<?> klass = Class.forName(className);
            return klass.newInstance();
        } catch (Exception e) {
            return null;
        }
    }
}
