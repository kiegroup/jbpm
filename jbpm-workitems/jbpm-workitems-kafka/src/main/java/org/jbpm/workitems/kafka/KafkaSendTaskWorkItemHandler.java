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
package org.jbpm.workitems.kafka;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidAuth;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.runtime.Cacheable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.emptyMap;

@Wid(widfile = "KafkaSendTaskDefinitions.wid", name = "KafkaSendTask",
     displayName = "KafkaSendTask",
     defaultHandler = "mvel: new org.jbpm.workitems.kafka.KafkaSendTaskWorkItemHandler()",
     documentation = "${artifactId}/index.html",
     category = "${artifactId}",
     icon = "KafkaSendTask.png",
     parameters = {
                   @WidParameter(name = "Signal"),
                   @WidParameter(name = "SignalProcessInstanceId"),
                   @WidParameter(name = "SignalWorkItemId"),
                   @WidParameter(name = "SignalDeploymentId"),
                   @WidParameter(name = "Data"),
                   @WidParameter(name = "Topic")
     },
     mavenDepends = {
                     @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
     },
     serviceInfo = @WidService(category = "${name}", description = "${description}",
                               keywords = "kafka,send,task",
                               action = @WidAction(title = "Send Kafka Message"),
                               authinfo = @WidAuth(required = true, params = {"kafkaBootstrapServers"},
                                                   paramsdescription = {"URL link to bootstrap server"})
     ))
public class KafkaSendTaskWorkItemHandler extends AbstractLogOrThrowWorkItemHandler implements Cacheable {

    private static final Logger logger = LoggerFactory.getLogger(KafkaSendTaskWorkItemHandler.class);

    private Producer<String, byte[]> kafkaProducer;

    public KafkaSendTaskWorkItemHandler(String kafkaBootstrapServers, String clientId) {
        this(init(kafkaBootstrapServers, clientId));
        logger.info("Kafka based work item handler successfully activated on destination {}", kafkaBootstrapServers);
    }

    public KafkaSendTaskWorkItemHandler(Producer<String, byte[]> kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    private static Producer<String, byte[]> init(String kafkaBootstrapServers, String clientId) {
        try {
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
            props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);

            setupBatchingAndCompression(props);
            setupRetriesInFlightTimeout(props);

            // Set number of acknowledgments - acks - default is all
            props.put(ProducerConfig.ACKS_CONFIG, "all");

            return new KafkaProducer<>(props);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unable to initialize Kafka send work item handler due to {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void setupRetriesInFlightTimeout(Properties props) {
        // - max.in.flight.requests.per.connection (default 5)
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        // Set the number of retries - retries
        props.put(ProducerConfig.RETRIES_CONFIG, 3);

        // Request timeout - request.timeout.ms
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 15_000);

        // Only retry after one second.
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1_000);

    }

    private static void setupBatchingAndCompression(Properties props) {
        // If 0, it turns the batching off.
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 10_240);
        // turns linger on and allows us to batch for 10 ms if size is not met
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1000);
    }


    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {

        String topic = (String) workItem.getParameter("Topic");
        if (topic == null) {
            logger.warn("No kafka topic defined in workitem {}; skipping sending data.", workItem.getId());
            manager.completeWorkItem(workItem.getId(), emptyMap());
            return;
        }

        byte[] serialized = serialize(workItem.getParameter("Data"));
        if(serialized.length == 0) {
            logger.warn("No data object in workitem {}; skipping sending data.", workItem.getId());
            manager.completeWorkItem(workItem.getId(), emptyMap());
            return;
        }

        ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, serialized);

        // set headers
        addPropertyIfExists("KIE_Signal", workItem.getParameter("Signal"), record);
        addPropertyIfExists("KIE_SignalProcessInstanceId", workItem.getParameter("SignalProcessInstanceId"), record);
        addPropertyIfExists("KIE_SignalWorkItemId", workItem.getParameter("SignalWorkItemId"), record);
        addPropertyIfExists("KIE_SignalDeploymentId", workItem.getParameter("SignalDeploymentId"), record);
        addPropertyIfExists("KIE_ProcessInstanceId", workItem.getProcessInstanceId(), record);
        addPropertyIfExists("KIE_DeploymentId", ((WorkItemImpl) workItem).getDeploymentId(), record);
        addPropertyIfExists("KIE_WorkItemId", workItem.getId(), record);

        Future<RecordMetadata> workItemUnitOfWork = kafkaProducer.send(record);
        try {
            RecordMetadata metadata = workItemUnitOfWork.get();
            Map<String, Object> results = new HashMap<>();
            results.put("kafka_metadata", metadata);
            manager.completeWorkItem(workItem.getId(), results);
        } catch (Exception e) {
            handleException(e);
        }
    }


    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        // do nothing
    }

    @Override
    public void close() {
        if(kafkaProducer != null) {
            kafkaProducer.close();
        }
    }

    protected void addPropertyIfExists(String propertyName, Object propertyValue, ProducerRecord<String, byte[]> record) {
        if (propertyValue != null) {
            record.headers().add(propertyName, serialize(propertyValue));
        }
    }

    private byte[] serialize(Object data) {
        if(data == null) {
            return new byte[0];
        }
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
             ObjectOutputStream oout = new ObjectOutputStream(bout);){
            oout.writeObject(data);
            return bout.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error during serialization for kafka server", e);
        }
    }
}
