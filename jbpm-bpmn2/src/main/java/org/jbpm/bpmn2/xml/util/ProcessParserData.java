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

package org.jbpm.bpmn2.xml.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.drools.core.xml.ExtensibleXmlParser;
import org.jbpm.bpmn2.core.Bpmn2Import;
import org.jbpm.bpmn2.core.Error;
import org.jbpm.bpmn2.core.Escalation;
import org.jbpm.bpmn2.core.Interface;
import org.jbpm.bpmn2.core.ItemDefinition;
import org.jbpm.bpmn2.core.Message;
import org.jbpm.bpmn2.core.Signal;
import org.jbpm.compiler.xml.ProcessBuildData;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.workflow.core.Node;
import org.kie.api.definition.process.Process;

public class ProcessParserData {

    public final ProcessMetadataListAtrribute<Bpmn2Import> bpmnImports;
    public final ProcessMetadataDictionaryAtrribute<Signal> signals;
    public final ProcessMetadataDictionaryAtrribute<Message> messages;
    public final ProcessMetadataDictionaryAtrribute<ItemDefinition> itemDefinitions;
    public final ProcessMetadataDictionaryAtrribute<Escalation> escalations;
    public final ProcessMetadataListAtrribute<Interface> interfaces;
    public final ProcessMetadataListAtrribute<Error> errors;
    public final ProcessMetadataSingleAtrribute<Variable> variable;
    public final ProcessMetadataSingleAtrribute<AtomicInteger> idGen;
    
    public final ProcessMapAttribute<Long, Node> nodes;
    public final ProcessListAttribute<Process> processes;

    private ProcessBuildData metadata;
    private ExtensibleXmlParser parser;




    private ProcessParserData(ExtensibleXmlParser parser) {
        this.parser = parser;
        this.metadata = (ProcessBuildData) parser.getData();

        this.bpmnImports = new ProcessMetadataListAtrribute<Bpmn2Import>(this.metadata, "Bpmn2Imports");
        this.errors = new ProcessMetadataListAtrribute<Error>(this.metadata, "Errors");
        this.escalations = new ProcessMetadataDictionaryAtrribute<Escalation>(this.metadata, "Escalations");
        this.signals = new ProcessMetadataDictionaryAtrribute<Signal>(this.metadata, "Signals");
        this.messages = new ProcessMetadataDictionaryAtrribute<Message>(this.metadata, "Messages");
        this.interfaces = new ProcessMetadataListAtrribute<Interface>(this.metadata, "Interfaces");
        this.itemDefinitions = new ProcessMetadataDictionaryAtrribute<ItemDefinition>(this.metadata, "ItemDefinitions");
        this.variable = new ProcessMetadataSingleAtrribute<Variable>(this.metadata, "Variable");
        this.idGen = new ProcessMetadataSingleAtrribute<AtomicInteger>(this.metadata, "idGen");



        this.nodes = new ProcessMapAttribute<Long, Node>() {

            @Override
            public void add(Node node) {
                ProcessParserData.this.metadata.addNode(node);
            }

            @Override
            public Map<Long, Node> get() {
                return ProcessParserData.this.metadata.getNodes();
            }


            
        };

        this.processes = new ProcessListAttribute<Process>() {

            @Override
            public void add(Process process) {
                ProcessParserData.this.metadata.addProcess(process);
                
            }

            @Override
            public List<Process> get() {
                return ProcessParserData.this.metadata.getProcesses();
            }


        };
    }
    
    public <T> T parent() {
        return (T) parser.getParent();
    }

    @SuppressWarnings("unchecked")
    public <T> T current() {
        return (T) parser.getCurrent();
    }

    static public ProcessParserData wrapParserMetadata(ExtensibleXmlParser data) {
        return new ProcessParserData(data);
    }
}
