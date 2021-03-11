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
package org.jbpm.kie.services.impl.model;

import org.jbpm.bpmn2.core.Message;
import org.jbpm.services.api.model.MessageDesc;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class MessageDescImplTest {

    @Test
    public void testEqualsHashCode() {
        MessageDesc message1 = MessageDescImpl.from(new Message("id"));
        MessageDesc message2 = MessageDescImpl.from(new Message("id"));
        assertEquals(message1.hashCode(), message2.hashCode());
        assertEquals(message1, message2);
    }

    @Test
    public void testNotEqualsHashCode() {
        Message message1 = new Message("id");
        Message message2 = new Message("id");
        message2.setName("name");
        message2.setName("type");
        MessageDesc messageDesc1 = MessageDescImpl.from(message1);
        MessageDesc messageDesc2 = MessageDescImpl.from(message2);
        assertNotEquals(messageDesc1.hashCode(), messageDesc2.hashCode());
        assertNotEquals(messageDesc1, messageDesc2);
    }
}
