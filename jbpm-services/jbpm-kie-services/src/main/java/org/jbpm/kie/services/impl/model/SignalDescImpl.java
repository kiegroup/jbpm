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
import org.jbpm.services.api.model.SignalType;

public class SignalDescImpl implements SignalDesc {

    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String structureRef;
    private SignalType signalType;

    public static SignalDescImpl from(Signal signal) {
        return new SignalDescImpl(signal.getId(), signal.getName(), signal.getStructureRef(), SignalType.SIGNAL);
    }

    public static SignalDescImpl from(Message message) {
        return new SignalDescImpl(message.getId(), message.getName(), message.getType(), SignalType.MESSAGE);
    }

    private SignalDescImpl(String id, String name, String structureRef, SignalType signalType) {
        this.id = id;
        this.name = name;
        this.structureRef = structureRef;
        this.signalType = signalType;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getStructureRef() {
        return structureRef;
    }

    @Override
    public SignalType getSignalType() {
        return signalType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((signalType == null) ? 0 : signalType.hashCode());
        result = prime * result + ((structureRef == null) ? 0 : structureRef.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof SignalDescImpl))
            return false;
        SignalDescImpl other = (SignalDescImpl) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (signalType != other.signalType)
            return false;
        if (structureRef == null) {
            if (other.structureRef != null)
                return false;
        } else if (!structureRef.equals(other.structureRef))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DefaultSignalDesc [id=" + id + ", name=" + name + ", structureRef=" + structureRef + ", signalType=" +
               signalType + "]";
    }
}
