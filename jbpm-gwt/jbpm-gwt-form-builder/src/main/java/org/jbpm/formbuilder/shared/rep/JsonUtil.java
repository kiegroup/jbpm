package org.jbpm.formbuilder.shared.rep;

import java.util.Date;
import java.util.Map;

import com.google.gwt.user.client.rpc.impl.ReflectionHelper;

public class JsonUtil {

    public static final String getJsonCode(Map<String, Object> data) {
        StringBuilder builder = new StringBuilder();
        if (data == null) {
            builder.append("null");
        } else {
            builder.append("{");
            builder.append(jsonFromMap(data));
            builder.append("}");
        }
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    private static String jsonFromMap(Map<String, Object> data) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            builder.append("'").append(entry.getKey()).append("': ");
            Object obj = entry.getValue();
            if (obj == null) {
                builder.append("null");
            } else if (obj instanceof Map) {
                builder.append(jsonFromMap((Map<String, Object>) obj));
            } else if (obj instanceof String) {
                builder.append("'").append(obj).append("'");
            } else if (obj instanceof Date) {
                builder.append("'").append(formatDate((Date) obj)).append("'");
            } else {
                builder.append(obj);
            }
        }
        return builder.toString();
    }
    
    private static String formatDate(Date date) {
        return "null"; //TODO see how to manage dates later
    }

	public static Object fromMap(Map<String, Object> map) {
		Object objClassName = map.get("className");
		if (objClassName == null) {
			return null;
		}
		String className = (String) objClassName;
		try {
			Class<?> klass = ReflectionHelper.loadClass(className);
			return ReflectionHelper.newInstance(klass);
		} catch (Exception e) {
			return null;
		}
	}
}
