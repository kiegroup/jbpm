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

package org.jbpm.process.builder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.Process;
import org.kie.api.runtime.process.DataTransformer;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessContext;
import org.drools.compiler.lang.descr.ProcessDescr;
import org.jbpm.process.builder.dialect.ProcessDialect;
import org.jbpm.process.builder.dialect.ProcessDialectRegistry;
import org.jbpm.process.core.impl.DataTransformerRegistry;
import org.jbpm.process.instance.impl.AssignmentProducer;
import org.jbpm.workflow.core.WorkflowProcess;
import org.jbpm.workflow.core.node.Assignment;
import org.jbpm.workflow.core.node.DataAssociation;
import org.jbpm.workflow.core.node.Transformation;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;

public class WorkItemNodeBuilder extends EventBasedNodeBuilder {

	public void build(Process process, ProcessDescr processDescr,
			ProcessBuildContext context, Node node) {
		super.build(process, processDescr, context, node);
		WorkflowProcess wfProcess = (WorkflowProcess) process;
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("imports", wfProcess.getImports());
		parameters.put("classloader", context.getConfiguration().getClassLoader());
		
		for (DataAssociation dataAssociation: ((WorkItemNode) node).getInAssociations()) {
			Transformation transformation = dataAssociation.getTransformation();
			if (transformation != null) {
				DataTransformer transformer = DataTransformerRegistry.get().find(transformation.getLanguage());
				transformation.setCompiledExpression(transformer.compile(transformation.getExpression(), parameters));		
			}
			
			List<Assignment> assignments = dataAssociation.getAssignments();
			if (assignments != null) {
                
				for (Assignment assignment: assignments) {
				    ProcessDialect dialect = ProcessDialectRegistry.getDialect( assignment.getDialect() );   
                    AssignmentProducer producer = (pc, ni, target) -> {
                        WorkItemNodeInstance wini = (WorkItemNodeInstance) ni;
                        wini.getWorkItem().setParameter(dataAssociation.getTarget(), target);
                    };           
	                
	                BiFunction<ProcessContext, NodeInstance, Object> source = (pc, ni) -> pc.getVariable(dataAssociation.getSources().get(0));
	                BiFunction<ProcessContext, NodeInstance, Object> target = (pc, ni) -> ((WorkItemNodeInstance) ni).getWorkItem().getParameter(dataAssociation.getTarget());

                    dialect.getAssignmentBuilder().build(context,
                                                         assignment,
                                                         dataAssociation.getSources().get(0),
                                                         dataAssociation.getTarget(),
                                                         source,
                                                         target,
                                                         producer);
	                   
				}
			}
		}
		for (DataAssociation dataAssociation: ((WorkItemNode) node).getOutAssociations()) {
			Transformation transformation = dataAssociation.getTransformation();
			if (transformation != null) {
				
				DataTransformer transformer = DataTransformerRegistry.get().find(transformation.getLanguage());
				transformation.setCompiledExpression(transformer.compile(transformation.getExpression(), parameters));
				
			}
			List<Assignment> assignments = dataAssociation.getAssignments();
			if (assignments != null) {             
				for (Assignment assignment: assignments) {
				    AssignmentProducer producer = (pc, ni, target) -> {
	                    pc.setVariable(dataAssociation.getTarget(), target);
	                };	                
	                BiFunction<ProcessContext, NodeInstance, Object> source = (pc, ni) -> ((WorkItemNodeInstance)ni).getWorkItem().getResult(dataAssociation.getSources().get(0));
	                BiFunction<ProcessContext, NodeInstance, Object> target = (pc, ni) -> pc.getVariable(dataAssociation.getTarget());
	                
					ProcessDialect dialect = ProcessDialectRegistry.getDialect( assignment.getDialect() );            
                    dialect.getAssignmentBuilder().build(context, 
                                                         assignment,
                                                         dataAssociation.getSources().get(0),
                                                         dataAssociation.getTarget(),
                                                         source,
                                                         target,
                                                         producer);
				}
			}
		}
	}

}
