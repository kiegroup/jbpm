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

import java.util.Collections;

import org.jbpm.bpmn2.core.Message;
import org.jbpm.bpmn2.core.Signal;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProcessAssetDescTest {

    @Test
    public void testClone() {
        ProcessAssetDesc processDefinition = new ProcessAssetDesc("pepe", "manolo", "1_0", "package", "one", "???",
                "namespace", "common_names");
        processDefinition.setSignalsDesc(Collections.singletonList(SignalDescImpl.from(new Signal("id", "id"))));
        processDefinition.setMessagesDesc(Collections.singletonList(MessageDescImpl.from(new Message("id"))));
        assertEquals(processDefinition.copy(), processDefinition);
    }
}
