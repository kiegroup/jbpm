/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.event.emitters.kafka;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jbpm.persistence.api.integration.EventCollection;
import org.jbpm.persistence.api.integration.EventEmitter;
import org.jbpm.persistence.api.integration.InstanceView;
import org.jbpm.persistence.api.integration.base.BaseEventCollection;
import org.jbpm.persistence.api.integration.model.CaseInstanceView;
import org.jbpm.persistence.api.integration.model.ProcessInstanceView;
import org.jbpm.persistence.api.integration.model.TaskInstanceView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kafka implementation of EventEmitter that simply pushes out data to several Kafka topics depending on InstanceView type. 
 * 
 * Expects following parameters to configure itself - via system properties
 * <ul>
 *  <li>org.kie.jbpm.event.emitters.kafka.date_format - date and time format to be sent to Kafka - default format is yyyy-MM-dd'T'HH:mm:ss.SSSZ</li>
 *  <li>org.kie.jbpm.event.emitters.kafka.boopstrap.servers - Kafka server ip, default is localhost:9092</li>
 *  <li>org.kie.jbpm.event.emitters.kafka.client.id - Kafka client id</li>
 *  <li>org.kie.jbpm.event.emitters.kafka.topic.<processes|tasks|cases>. Topic name for subscribing to these events. Defaults are "jbpm-<processes|tasks|cases>-events"</li>
 * </ul> 
 */
public class KafkaEventEmitter implements EventEmitter {

    private static final Logger logger = LoggerFactory.getLogger(KafkaEventEmitter.class);
    private static final String SOURCE_FORMATTER = "/process/%s/%s";
    private ObjectMapper mapper;

    private ThreadLocal<Producer<String, byte[]>> localProducer = new ThreadLocal<>();

    public KafkaEventEmitter() {
        mapper = new ObjectMapper()
                .setDateFormat(new SimpleDateFormat(System.getProperty(
                        "org.kie.jbpm.event.emitters.kafka.date_format", System.getProperty(
                                "org.kie.server.json.date_format",
                                "yyyy-MM-dd'T'HH:mm:ss.SSSZ"))))
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);
    }

    public void deliver(Collection<InstanceView<?>> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        Producer<String, byte[]> producer = getProducer();
        localProducer.set(producer);
        producer.initTransactions();
        producer.beginTransaction();

        for (InstanceView<?> view : data) {
            String processId;
            long processInstanceId;
            String type;
            String topic;
            if (view instanceof ProcessInstanceView) {
                ProcessInstanceView processInstanceView = (ProcessInstanceView) view;
                topic = "processes";
                type = "process";
                processInstanceId = processInstanceView.getId();
                processId = processInstanceView.getProcessId();
            } else if (view instanceof TaskInstanceView) {
                TaskInstanceView taskInstanceView = (TaskInstanceView) view;
                topic = "tasks";
                type = "task";
                processInstanceId = taskInstanceView.getProcessInstanceId();
                processId = taskInstanceView.getProcessId();
            } else if (view instanceof CaseInstanceView) {
                CaseInstanceView caseInstanceView = (CaseInstanceView) view;
                topic = "cases";
                type = "case";
                processInstanceId = caseInstanceView.getId();
                processId = caseInstanceView.getCaseDefinitionId();
            } else {
                throw new UnsupportedOperationException("Unsupported view type " + view.getClass());
            }
            try {
                producer.send(new ProducerRecord<>(getTopic(topic), mapper.writeValueAsBytes(
                        new CloudEventSpec1(type, String.format(SOURCE_FORMATTER, processId, processInstanceId),
                                view))));
            } catch (IOException ex) {
                throw new IllegalArgumentException("Error creating cloud event for view " + view, ex);
            }
        }
    }

    @SuppressWarnings({"squid:S2139", "squid:S3457"})
    public void apply(Collection<InstanceView<?>> data) {
        try {
            localProducer.get().commitTransaction();
        } catch (KafkaException ex) {
            logger.error("Error publishing events " + data, ex);
            localProducer.get().abortTransaction();
            throw ex;
        } finally {
            localProducer.remove();
        }
    }

    public void drop(Collection<InstanceView<?>> data) {
        try {
            localProducer.get().abortTransaction();
        } finally {
            localProducer.remove();
        }
    }

    @Override
    public void close() {
        localProducer.remove();
    }

    @Override
    public EventCollection newCollection() {
        return new BaseEventCollection();
    }

    protected Producer<String, byte[]> getProducer() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, System.getProperty(
                "org.kie.jbpm.event.emitters.kafka.boopstrap.servers", "localhost:9092"));
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        configs.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, UUID.randomUUID().toString());
        String clientId = System.getProperty("org.kie.jbpm.event.emitters.kafka.client.id");
        if (clientId != null) {
            configs.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        }
        return new KafkaProducer<>(configs);
    }

    private static String getTopic(String eventType) {
        return System.getProperty("org.kie.jbpm.event.emitters.kafka.topic." + eventType, "jbpm-" + eventType +
                                                                                          "-events");
    }
}
