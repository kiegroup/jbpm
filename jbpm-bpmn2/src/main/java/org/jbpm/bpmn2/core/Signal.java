/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.bpmn2.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.kie.api.definition.process.Node;

public class Signal implements Serializable {

	private static final long serialVersionUID = 510l;

    private String id;
    private String name;
    private String structureRef;
    private Collection<Node> incomingNodes = new ArrayList<>();
    private Collection<Node> outgoingNodes = new ArrayList<>();

    public Signal(String id, String structureRef) {
        this.id = id;
        this.structureRef = structureRef;
    }

    public Signal(String id, String name, String structureRef) {
        this(id, structureRef);
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getStructureRef() {
        return structureRef;
    }

	public String getName() {
		return name;
	}

    public void addIncomingNode(Node node) {
        incomingNodes.add(node);
    }

    public void addOutgoingNode(Node node) {
        outgoingNodes.add(node);
    }

    public Collection<Node> getIncomingNodes() {
        return Collections.unmodifiableCollection(incomingNodes);
    }

    public Collection<Node> getOutgoingNodes() {
        return Collections.unmodifiableCollection(outgoingNodes);
    }
}
