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

import java.util.Collection;

import org.jbpm.bpmn2.core.Signal;
import org.jbpm.services.api.model.SignalDesc;
import org.kie.api.definition.process.Node;

public class SignalDescImpl  extends SignalDescBaseImpl implements SignalDesc {

    private static final long serialVersionUID = 1L;

    public static SignalDescImpl from(Signal signal) {
        return new SignalDescImpl(signal.getId(), signal.getName(), signal.getStructureRef(), signal.getIncomingNodes(),
                signal.getOutgoingNodes());
    }

    private SignalDescImpl(String id, String name, String structureRef, Collection<Node> incomingNodes,
                           Collection<Node> outgoingNodes) {
        super(id, name, structureRef, incomingNodes, outgoingNodes);
    }
}
