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

package org.jbpm.casemgmt.cmmn.xml.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.drools.core.xml.ExtensibleXmlParser;
import org.jbpm.casemgmt.cmmn.core.Decision;
import org.jbpm.casemgmt.cmmn.core.FileItemDefinition;
import org.jbpm.casemgmt.cmmn.core.PlanItem;
import org.jbpm.casemgmt.cmmn.core.Role;
import org.jbpm.casemgmt.cmmn.core.Sentry;
import org.jbpm.compiler.xml.ProcessBuildData;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.workflow.core.Node;
import org.kie.api.definition.process.Process;

public final class CaseParserData {



    private ProcessBuildData metadata;
    private ExtensibleXmlParser parser;

    public final CaseMetadataDictionaryAtrribute<PlanItem> planItems;
    public final CaseMapAttribute<Long, Node> nodes;
    public final CaseMetadataDictionaryAtrribute<String> fileItems;
    public final CaseListAttribute<Process> processes;
    public final CaseMetadataSingleAtrribute<AtomicInteger> idGen;
    public final CaseMetadataDictionaryAtrribute<Role> roles;
    public final CaseMetadataDictionaryAtrribute<Decision> decisions;
    public final CaseMetadataDictionaryAtrribute<String> processElements;
    public final CaseMetadataDictionaryAtrribute<FileItemDefinition> fileItemDefinitions;
    public final CaseMetadataSingleAtrribute<Variable> variable;
    public final CaseMetadataDictionaryAtrribute<Sentry> sentries;
    
    private CaseParserData(ExtensibleXmlParser parser) {
        this.parser = parser;
        this.metadata = (ProcessBuildData) parser.getData();

        this.planItems = new CaseMetadataDictionaryAtrribute<PlanItem> (this.metadata, "PlanItems");
        this.fileItems = new CaseMetadataDictionaryAtrribute<String> (this.metadata, "FileItems");
        this.idGen = new CaseMetadataSingleAtrribute<AtomicInteger>(this.metadata, "idGen");
        this.roles = new CaseMetadataDictionaryAtrribute<Role>(this.metadata, "Roles");
        this.decisions = new CaseMetadataDictionaryAtrribute<Decision>(this.metadata, "DecisionElements");
        this.processElements = new CaseMetadataDictionaryAtrribute<String>(this.metadata, "ProcessElements");
        this.fileItemDefinitions = new CaseMetadataDictionaryAtrribute<FileItemDefinition>(this.metadata, "FileItemDefinitions"); 
        this.variable = new CaseMetadataSingleAtrribute<Variable>(this.metadata, "Variable");
        this.sentries = new CaseMetadataDictionaryAtrribute<Sentry>(this.metadata, "Sentries");
        
        this.nodes = new CaseMapAttribute<Long, Node>() {

            @Override
            public void add(Node node) {
                CaseParserData.this.metadata.addNode(node);
            }

            @Override
            public Map<Long, Node> get() {
                return CaseParserData.this.metadata.getNodes();
            }


            
        };

        this.processes = new CaseListAttribute<Process>() {

            @Override
            public void add(Process process) {
                CaseParserData.this.metadata.addProcess(process);
                
            }

            @Override
            public List<Process> get() {
                return CaseParserData.this.metadata.getProcesses();
            }


        };

    }

    @SuppressWarnings("unchecked")
    public <T> T current() {
        return (T) parser.getCurrent();
    }

    static public CaseParserData wrapParserMetadata(ExtensibleXmlParser data) {
        return new CaseParserData(data);
    }
}
