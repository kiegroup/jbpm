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
import org.jbpm.bpmn2.core.Signal;
import org.jbpm.services.api.model.SignalDesc;
import org.jbpm.services.api.model.SignalDescBase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class SignalDescImplTest {

    @Test
    public void testEqualsHashCode() {
        SignalDesc signal1 = SignalDescImpl.from(new Signal("id", "name", "type"));
        SignalDesc signal2 = SignalDescImpl.from(new Signal("id", "name", "type"));
        assertEquals(signal1, signal2);
        assertEquals(signal1.hashCode(), signal2.hashCode());
    }

    @Test
    public void testNoEqualsHashCode() {
        SignalDesc signalDesc1 = SignalDescImpl.from(new Signal("id", "name", "type"));
        SignalDesc signalDesc2 = SignalDescImpl.from(new Signal("id", "type"));
        assertNotEquals(signalDesc1, signalDesc2);
        assertNotEquals(signalDesc1.hashCode(), signalDesc2.hashCode());
    }

    @Test
    public void testSignalIsNotMessage() {
        SignalDescBase signalDesc1 = SignalDescImpl.from(new Signal("id", "id"));
        SignalDescBase signalDesc2 = MessageDescImpl.from(new Message("id"));
        assertEquals(signalDesc1.getId(), signalDesc2.getId());
        assertEquals(signalDesc1.getName(), signalDesc2.getName());
        assertNotEquals(signalDesc1, signalDesc2);
    }
}
