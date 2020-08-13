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
package org.kie.api.fluent;

import org.jbpm.bpmn2.JbpmBpmn2TestCase;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.definition.process.Process;
import org.kie.api.internal.utils.ServiceRegistry;
import org.kie.api.io.KieResources;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.kie.api.fluent.Variable.var;

public class ProcessBuilderTest extends JbpmBpmn2TestCase {

    public ProcessBuilderTest() {
        super(false);
    }

    @Test
    public void testProcessBuilder() throws Exception {
        final String processId = "org.jbpm.process";
        final String processName = "My process";
        final String packageName = "org.jbpm";
        // retrieve process builder factory
        ProcessBuilderFactory factory = ProcessBuilderFactories.get();
        Process process =
                factory
                       // start process definition
                       .processBuilder(processId)
                       // package and name 
                       .packageName(packageName)
                       .name(processName).setMetadata("pepe", true)
                       // start node
                       .startNode(1).name("Start").done()
                       // Add variable of type string
                       .variable(var("pepe", String.class))
                       // Add exception handler
                       .exceptionHandler(IllegalArgumentException.class, Dialect.JAVA, "System.out.println(\"Exception\");")
                       // script node in Java language that prints "action"
                       .actionNode(2).name("Action")
                       .action(Dialect.JAVA,
                               "System.out.println(\"Action\");").done()
                       // end node
                       .endNode(3).name("End").done()
                       // connections
                       .connection(1,
                                   2)
                       .connection(2,
                                   3)
                       .build();
        // Build resource from ProcessBuilder
        assertEquals(processId, process.getId());
        assertEquals(processName, process.getName());
        assertEquals(packageName, process.getPackageName());
        assertEquals(true, process.getMetaData().get("pepe"));
        KieResources resources = ServiceRegistry.getInstance().get(KieResources.class);
        Resource res = resources
                                .newByteArrayResource(factory.toBytes(process))
                                .setSourcePath("/tmp/processFactory.bpmn2"); // source path or target path must be set to be added into kbase
        // Create kie base 
        KieBase kbase = createKnowledgeBaseFromResources(res);
        // Create kie session
        KieSession ksession = createKnowledgeSession(kbase);
        // execute process
        ProcessInstance instance = ksession.startProcess(processId);
        assertNotNull(instance);
        ksession.dispose();
    }
}
