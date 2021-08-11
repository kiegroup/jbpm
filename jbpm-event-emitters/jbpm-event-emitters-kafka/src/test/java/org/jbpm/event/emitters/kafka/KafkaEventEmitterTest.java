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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jbpm.persistence.api.integration.EventEmitter;
import org.jbpm.persistence.api.integration.InstanceView;
import org.jbpm.persistence.api.integration.model.CaseInstanceView;
import org.jbpm.persistence.api.integration.model.ProcessInstanceView;
import org.jbpm.persistence.api.integration.model.TaskInstanceView;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.process.ProcessInstance;
import org.slf4j.LoggerFactory;

import static org.jbpm.event.emitters.kafka.KafkaEventEmitter.KAFKA_EMITTER_PREFIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KafkaEventEmitterTest {


    private MockProducer<String, byte[]> producer;

    @Before
    public void setup() {
        producer = new MockProducer<>(false, new StringSerializer(), new ByteArraySerializer());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProducer() throws IOException, ParseException {

        SimpleDateFormat dateFormat = new SimpleDateFormat(System.getProperty(
                "org.kie.jbpm.event.emitters.kafka.date_format", System.getProperty(
                        "org.kie.server.json.date_format",
                        "yyyy-MM-dd'T'HH:mm:ss.SSSZ")));
        ObjectMapper mapper = new ObjectMapper()
                .setDateFormat(dateFormat)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);
        Date date = new Date();
        ProcessInstanceView piView = new ProcessInstanceView();
        piView.setCompositeId("server-1");
        piView.setContainerId("container-1");
        piView.setCorrelationKey("process-1");
        piView.setDate(date);
        piView.setId(1L);
        piView.setInitiator("pepe");
        piView.setParentId(0L);
        piView.setProcessId("Test");
        piView.setProcessInstanceDescription("a process");
        piView.setProcessName("Test");
        piView.setProcessVersion("1_0");
        piView.setState(1);
        piView.setVariables(Collections.emptyMap());

        TaskInstanceView taskView = new TaskInstanceView();
        taskView.setProcessId("Test");
        taskView.setProcessInstanceId(1L);
        taskView.setActualOwner("pepe");

        CaseInstanceView caseView = new CaseInstanceView();
        caseView.setProcessId("Test");
        caseView.setId(1L);
        caseView.setInitiator("pepe");

        System.setProperty("org.kie.jbpm.event.emitters.kafka.topic.cases", "customer-cases");

        try (KafkaEventEmitter emitter = new KafkaEventEmitter(producer)) {
            emit(emitter, Arrays.asList(piView, taskView, caseView));

            List<ProducerRecord<String, byte[]>> producedEvents = producer.history();
            assertEquals(3, producedEvents.size());

            ProducerRecord<String, byte[]> record = producedEvents.get(0);
            assertEquals("jbpm-processes-events", record.topic());
            Map<String, Object> piEvent = mapper.readValue(record.value(), Map.class);
            assertEquals("process", piEvent.get("type"));
            assertEquals("/process/Test/1", piEvent.get("source"));
            assertTrue(dateFormat.parse(piEvent.get("time").toString()).compareTo(date) >= 0);
            assertTrue(piEvent.get("data") instanceof Map);
            Map<String, Object> pi = (Map<String, Object>) piEvent.get("data");
            assertEquals("server-1", pi.get("compositeId"));
            assertEquals("container-1", pi.get("containerId"));
            assertEquals("process-1", pi.get("correlationKey"));
            assertEquals(1, pi.get("id"));
            assertEquals(0, pi.get("parentId"));
            assertEquals("Test", pi.get("processId"));
            assertEquals("pepe", pi.get("initiator"));
            assertEquals(1, pi.get("state"));
            assertEquals("a process", pi.get("processInstanceDescription"));
            assertEquals("Test", pi.get("processName"));
            assertEquals("1_0", pi.get("processVersion"));
            assertTrue(pi.get("variables") instanceof Map);
            assertTrue(((Map<?, ?>) pi.get("variables")).isEmpty());

            record = producedEvents.get(1);
            assertEquals("jbpm-tasks-events", record.topic());
            Map<String, Object> taskEvent = mapper.readValue(record.value(), Map.class);
            assertEquals("task", taskEvent.get("type"));
            assertEquals("/process/Test/1", taskEvent.get("source"));
            assertTrue(taskEvent.get("data") instanceof Map);
            Map<String, Object> task = (Map<String, Object>) taskEvent.get("data");
            assertEquals("Test", task.get("processId"));
            assertEquals(1, task.get("processInstanceId"));
            assertEquals("pepe", task.get("actualOwner"));

            record = producedEvents.get(2);
            assertEquals("customer-cases", record.topic());
            Map<String, Object> caseEvent = mapper.readValue(record.value(), Map.class);
            assertEquals("case", caseEvent.get("type"));
            assertEquals("/process/Test/1", caseEvent.get("source"));
            assertTrue(caseEvent.get("data") instanceof Map);
            Map<String, Object> case1 = (Map<String, Object>) caseEvent.get("data");
            assertEquals("Test", case1.get("caseDefinitionId"));
            assertEquals(1, case1.get("id"));
            assertEquals("pepe", case1.get("owner"));
        } finally {
            System.clearProperty("org.kie.jbpm.event.emitters.kafka.topic.cases");
        }
    }

    @Test
    public void testProducerWrongView() throws IOException, ParseException {

        InstanceView<ProcessInstance> piView = new InstanceView<ProcessInstance>() {
            private static final long serialVersionUID = 1L;

            @Override
            public ProcessInstance getSource() {
                return null;
            }

            @Override
            public void copyFromSource() {

            }

            @Override
            public String getCompositeId() {
                return null;
            }
        };
        try (KafkaEventEmitter emitter = new KafkaEventEmitter(producer)) {
            emit(emitter, Collections.singletonList(piView));
            List<ProducerRecord<String, byte[]>> producedEvents = producer.history();
            assertEquals(0, producedEvents.size());
        }
    }

    private static class NotSerializable {

        @SuppressWarnings("unused")
        private int tryMe;

        @SuppressWarnings("unused")
        public int getTryMe() {
            throw new IllegalStateException("NOOOO!!!!");
        }
    }

    @Test
    public void testProducerWrongData() throws IOException, ParseException {

        TaskInstanceView taskView = new TaskInstanceView();
        taskView.setProcessId("Test");
        taskView.setProcessInstanceId(1L);
        taskView.setActualOwner("pepe");
        
        ProcessInstanceView processView = new ProcessInstanceView();
        processView.setProcessId("Test");
        processView.setId(1L);
        processView.setVariables(Collections.singletonMap("tryIt", new NotSerializable()));
        processView.setInitiator("pepe");

        try (KafkaEventEmitter emitter = new KafkaEventEmitter(producer)) {
            emit(emitter, Arrays.asList(taskView, processView));
            List<ProducerRecord<String, byte[]>> producedEvents = producer.history();
            assertEquals(1, producedEvents.size());
        }
    }

    @Test
    public void testProducerExceptionLoggerAtCallback() throws IOException, ParseException {
        Logger logger = (Logger) LoggerFactory.getLogger(KafkaEventEmitter.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        ProcessInstanceView processView = new ProcessInstanceView();
        processView.setProcessId("Test");
        processView.setId(1L);
        RuntimeException ex = new IllegalStateException("Something bad has happened!!");
        try (KafkaEventEmitter emitter = new KafkaEventEmitter(producer)) {
            emit(emitter, Collections.singletonList(processView));
            producer.errorNext(ex);
            List<ProducerRecord<String, byte[]>> producedEvents = producer.history();
            assertEquals(1, producedEvents.size());
        }
        Optional<ILoggingEvent> logEvent = listAppender.list.stream().filter(log -> log.getLevel() == Level.ERROR)
                .findAny();
        assertTrue("no trace printed when failed", logEvent.isPresent());
        assertTrue(Arrays.asList(logEvent.get().getArgumentArray()).contains(processView));
        assertEquals(ex.getClass().getCanonicalName(), logEvent.get().getThrowableProxy().getClassName());
        assertEquals(ex.getMessage(), logEvent.get().getThrowableProxy().getMessage());

    }
    
    @Test
    public void testProperties() {
        System.setProperty(KAFKA_EMITTER_PREFIX + ProducerConfig.BATCH_SIZE_CONFIG, "1000");
        System.setProperty(KAFKA_EMITTER_PREFIX + "randomProperty", "JOJOJO");
        try {
            Map<String, Object> producerProperties = KafkaEventEmitter.getProducerProperties();
            assertEquals(1, producerProperties.size());
            assertEquals("1000", producerProperties.get(ProducerConfig.BATCH_SIZE_CONFIG));
        } finally {
            System.clearProperty(KAFKA_EMITTER_PREFIX + ProducerConfig.BATCH_SIZE_CONFIG);
            System.clearProperty(KAFKA_EMITTER_PREFIX + "randomProperty");

        }
    }

    private void emit(EventEmitter emitter, Collection<InstanceView<?>> views) {
        try {
            emitter.deliver(views);
            emitter.apply(views);
        } catch (Exception ex) {
            emitter.drop(views);
        }
    }
    
    

}
