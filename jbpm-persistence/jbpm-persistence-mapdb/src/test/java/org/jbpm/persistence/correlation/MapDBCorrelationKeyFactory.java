package org.jbpm.persistence.correlation;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.persistence.mapdb.MapDBCorrelationKey;
import org.jbpm.persistence.mapdb.MapDBCorrelationProperty;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.process.CorrelationKeyFactory;
import org.kie.internal.process.CorrelationProperty;

public class MapDBCorrelationKeyFactory implements CorrelationKeyFactory {

	@Override
    public CorrelationKey newCorrelationKey(String businessKey) {
        if (businessKey.isEmpty()) {
            throw new IllegalArgumentException("businessKey cannot be empty");
        }

        MapDBCorrelationKey correlationKey = new MapDBCorrelationKey();
        correlationKey.setName(businessKey);
        MapDBCorrelationProperty prop = new MapDBCorrelationProperty();
        prop.setValue(businessKey);
        correlationKey.setProperties(new ArrayList<CorrelationProperty<?>>());
        correlationKey.getProperties().add(prop);
        return correlationKey;
    }

    public CorrelationKey newCorrelationKey(List<String> properties) {
        if (properties.isEmpty()) {
            throw new IllegalArgumentException("properties cannot be empty");
        }

        MapDBCorrelationKey correlationKey = new MapDBCorrelationKey();
        List<CorrelationProperty<?>> props = new ArrayList<>();
        for (String businessKey : properties) {
        	MapDBCorrelationProperty prop = new MapDBCorrelationProperty();
            prop.setValue(businessKey);
            props.add(prop);
        }
        correlationKey.setProperties(props);
        return correlationKey;
    }
}
