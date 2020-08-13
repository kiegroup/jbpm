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

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.drools.core.process.instance.impl.DefaultWorkItemManager;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KafkaSendTaskWorkItemHandlerTest {

    @Mock
    private Producer<String, byte[]> producer;

    @Mock
    private Future<RecordMetadata> metadata;

    public static class Data implements Serializable {

        private static final long serialVersionUID = 32519560293229247L;

    }

    @Test
    public void testProducer() throws InterruptedException, ExecutionException {
        when(producer.send(any())).thenReturn(metadata);
        KafkaSendTaskWorkItemHandler handler = new KafkaSendTaskWorkItemHandler(producer);
        DefaultWorkItemManager witm = new DefaultWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.getParameters().put("Topic", "testTopic");
        workItem.getParameters().put("Data", new Data());
        handler.executeWorkItem(workItem, witm);
        verify(producer, times(1)).send(any());
        verify(metadata, times(1)).get();
    }

    @Test
    public void testProducerNoTopic() throws InterruptedException, ExecutionException {
        when(producer.send(any())).thenReturn(metadata);
        KafkaSendTaskWorkItemHandler handler = new KafkaSendTaskWorkItemHandler(producer);
        DefaultWorkItemManager witm = new DefaultWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        handler.executeWorkItem(workItem, witm);
        verify(metadata, times(0)).get();
    }

    @Test
    public void testProducerNoData() throws InterruptedException, ExecutionException {
        when(producer.send(any())).thenReturn(metadata);
        KafkaSendTaskWorkItemHandler handler = new KafkaSendTaskWorkItemHandler(producer);
        DefaultWorkItemManager witm = new DefaultWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.getParameters().put("Data", new Data());
        handler.executeWorkItem(workItem, witm);
        verify(metadata, times(0)).get();
    }
}
