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

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Kafka implementation of EventEmitter that simply pushes out data to several Kafka topics depending on InstanceView type. 
 * 
 * Expects following parameters to configure itself - via system properties
 * <ul>
 *  <li>org.kie.jbpm.event.emitters.kafka.date_format - date and time format to be sent to Kafka - default format is yyyy-MM-dd'T'HH:mm:ss.SSSZ</li>
 *  <li>org.kie.jbpm.event.emitters.kafka.bootstrap.servers - Kafka server ip, default is localhost:9092</li>
 *  <li>org.kie.jbpm.event.emitters.kafka.client.id - Kafka client id</li>
 *  <li>org.kie.jbpm.event.emitters.kafka.acks - Kafka acknowledge policy, check <a href="http://kafka.apache.org/documentation.html#producerconfigs">Kafka documentation</a></li>
 *  <li>org.kie.jbpm.event.emitters.kafka.topic.<processes|tasks|cases>. Topic name for subscribing to these events. Defaults are "jbpm-<processes|tasks|cases>-events"</li>
 * </ul> 
 */
public class KafkaEventEmitter implements EventEmitter {

    private static final Logger logger = LoggerFactory.getLogger(KafkaEventEmitter.class);
    private static final String SOURCE_FORMATTER = "/process/%s/%s";
    protected static final String KAFKA_EMITTER_PREFIX = "org.kie.jbpm.event.emitters.kafka.";

    private ObjectMapper mapper;
    
    private KafkaSender sender;

    private Producer<String, byte[]> producer;

    public KafkaEventEmitter() {
        this(getProducer());
    }

    KafkaEventEmitter(Producer<String, byte[]> producer) {
        this.producer = producer;
        this.sender = Boolean.getBoolean(KAFKA_EMITTER_PREFIX+"sync") ? this::sendSync : this::sendAsync; 
        mapper = new ObjectMapper()
                .setDateFormat(new SimpleDateFormat(System.getProperty(
                        KAFKA_EMITTER_PREFIX+"date_format", System.getProperty(
                                "org.kie.server.json.date_format",
                                "yyyy-MM-dd'T'HH:mm:ss.SSSZ"))))
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);
    }

    @Override
    public void deliver(Collection<InstanceView<?>> data) {
        // nothing to do
    }

    @Override
    public void apply(Collection<InstanceView<?>> data) {
        if (data == null || data.isEmpty()) {
            return;
        }

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
                logger.warn("Unsupported view type {}", view.getClass());
                continue;
            }
            sender.send(topic, type, processId, processInstanceId, view);
        }
    }
    
    private interface KafkaSender {
        void send (String topic, String type, String processId, long processInstanceId, InstanceView<?> view);
    }

    private byte[] viewToPayload(String type, String processId, long processInstanceId, InstanceView<?> view) throws JsonProcessingException {
        return mapper.writeValueAsBytes(new CloudEventSpec1(type, String.format(SOURCE_FORMATTER, processId, processInstanceId), view));
    }

    private void sendAsync(String topic, String type, String processId, long processInstanceId, InstanceView<?> view) {
        try {
            producer.send(new ProducerRecord<>(getTopic(topic), viewToPayload(type, processId, processInstanceId, view)), (m, e) -> {
                if (e != null) {
                    logError(view, e);
                }
            });
        } catch (Exception e) {
            logError(view, e);
        }
    }

    private void sendSync(String topic, String type, String processId, long processInstanceId, InstanceView<?> view) {
        try {
            producer.send(new ProducerRecord<>(getTopic(topic), viewToPayload(type, processId, processInstanceId, view))).get();
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else {
                throw new KafkaException(e.getCause());
            }
        }
    }

    private void logError(InstanceView<?> view, Exception e) {
        logger.error("Error publishing view {}", view, e);
    }


    @Override
    public void drop(Collection<InstanceView<?>> data) {
        // nothing to do
    }

    @Override
    public void close() {
        producer.close();
    }

    @Override
    public EventCollection newCollection() {
        return new BaseEventCollection();
    }

    private static Producer<String, byte[]> getProducer() {
        Map<String, Object> configs = getProducerProperties();
        configs.putIfAbsent(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configs.putIfAbsent(ProducerConfig.MAX_BLOCK_MS_CONFIG, "2000");
        return new KafkaProducer<>(configs, new StringSerializer(), new ByteArraySerializer());
    }

    private static String getTopic(String eventType) {
        return System.getProperty("org.kie.jbpm.event.emitters.kafka.topic." + eventType, "jbpm-" + eventType +
                                                                                          "-events");
    }

    protected static Map<String, Object> getProducerProperties() {
        Map<String, Object> properties = new HashMap<>();
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            String key = entry.getKey().toString();
            if (key.startsWith(KAFKA_EMITTER_PREFIX)) {
                String propName = key.substring(KAFKA_EMITTER_PREFIX.length());
                if (ProducerConfig.configNames().contains(propName)) {
                    properties.put(propName, entry.getValue());
                }
            }
        }
        return properties;
    }
}
