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

import java.io.Serializable;
import java.util.Collection;

import org.jbpm.services.api.model.SignalDescBase;
import org.kie.api.definition.process.Node;


abstract class SignalDescBaseImpl implements SignalDescBase, Serializable {

    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String structureRef;
    private Collection<Node> incomingNodes;
    private Collection<Node> outgoingNodes;

    protected SignalDescBaseImpl(String id, String name, String structureRef, Collection<Node> incomingNodes,
                                 Collection<Node> outgoingNodes) {
        this.id = id;
        this.name = name;
        this.structureRef = structureRef;
        this.incomingNodes = incomingNodes;
        this.outgoingNodes = outgoingNodes;
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
    public Collection<Node> getIncomingNodes() {
        return incomingNodes;
    }

    @Override
    public Collection<Node> getOutgoingNodes() {
        return outgoingNodes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((structureRef == null) ? 0 : structureRef.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || !(this.getClass().equals(obj.getClass())))
            return false;
        SignalDescBaseImpl other = (SignalDescBaseImpl) obj;
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

        if (structureRef == null) {
            if (other.structureRef != null)
                return false;
        } else if (!structureRef.equals(other.structureRef))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [id=" + id + ", name=" + name + ", structureRef=" + structureRef +
               ", incomingNodes=" + incomingNodes + ", outgoingNodes=" + outgoingNodes + "]";
    }

}
