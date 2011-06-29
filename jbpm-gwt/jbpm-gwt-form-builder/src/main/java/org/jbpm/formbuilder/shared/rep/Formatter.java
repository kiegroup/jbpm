package org.jbpm.formbuilder.shared.rep;

import java.util.Map;

public interface Formatter {

    Object format(Object object);

	Map<String, Object> getDataMap();
}
