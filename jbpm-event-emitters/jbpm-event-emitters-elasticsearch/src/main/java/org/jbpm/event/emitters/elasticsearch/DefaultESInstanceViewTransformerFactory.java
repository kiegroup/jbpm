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
package org.jbpm.event.emitters.elasticsearch;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.jbpm.persistence.api.integration.InstanceView;
import org.jbpm.persistence.api.integration.model.CaseInstanceView;
import org.jbpm.persistence.api.integration.model.ProcessInstanceView;
import org.jbpm.persistence.api.integration.model.TaskInstanceView;
import org.jbpm.persistence.api.integration.model.TaskOperationView;


public class DefaultESInstanceViewTransformerFactory implements ESInstanceViewTransformerFactory{
    
    private final String dateFormatStr = System.getProperty("org.jbpm.event.emitters.elasticsearch.date_format", System
            .getProperty("org.kie.server.json.date_format", "yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
    private final boolean ignoreNull = Boolean.getBoolean("org.jbpm.event.emitters.elasticsearch.ignoreNull");
    
    protected Map<Class<?>, ESInstanceViewTransformer> viewMapping = new ConcurrentHashMap<>();
     
    public DefaultESInstanceViewTransformerFactory() {
        DefaultESInstanceViewTransformer taskView = new DefaultESInstanceViewTransformer("tasks", "task");
        viewMapping.put(TaskOperationView.class, taskView);
        viewMapping.put(TaskInstanceView.class, taskView);
        viewMapping.put(ProcessInstanceView.class, new DefaultESInstanceViewTransformer("processes", "process"));
        viewMapping.put(CaseInstanceView.class, new DefaultESInstanceViewTransformer("cases", "case"));
    }
    
    @Override
    public void configureObjectMapper(ObjectMapper mapper) {
        mapper.setDateFormat(new SimpleDateFormat(dateFormatStr));
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);
        if (ignoreNull) {
            mapper.setSerializationInclusion(Include.NON_NULL);
        }
    }

    @Override
    public ESInstanceViewTransformer getInstanceViewTransformer(InstanceView<?> view) {
        return viewMapping.get(view.getClass());
    }

}
