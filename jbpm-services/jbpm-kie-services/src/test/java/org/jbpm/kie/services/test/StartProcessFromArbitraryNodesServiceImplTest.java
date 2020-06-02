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

package org.jbpm.kie.services.test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.kie.test.util.AbstractKieServicesBaseTest;
import org.jbpm.services.api.RuntimeDataService.EntryType;
import org.jbpm.services.api.model.DeploymentUnit;
import org.jbpm.services.api.model.NodeInstanceDesc;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.query.QueryContext;
import org.kie.api.task.model.Task;
import org.kie.scanner.KieMavenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.kie.scanner.KieMavenRepository.getKieMavenRepository;

public class StartProcessFromArbitraryNodesServiceImplTest extends AbstractKieServicesBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(StartProcessFromArbitraryNodesServiceImplTest.class);

    private List<DeploymentUnit> units = new ArrayList<DeploymentUnit>();

    @Before
    public void prepare() {
        configureServices();
        logger.debug("Preparing kjar");
        KieServices ks = KieServices.Factory.get();
        ReleaseId releaseId = ks.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        List<String> processes = new ArrayList<String>();
        processes.add("repo/processes/general/BPMN2-SimpleRestartTest.bpmn2");
        processes.add("repo/processes/general/BPMN2-SimpleRestartWithTimerTest.bpmn2");
        processes.add("repo/processes/general/BPMN2-SimpleRestartWithObsoleteTest.bpmn2");
        processes.add("repo/processes/general/BPMN2-SimpleRestartWithErrorOnEntryScriptHT.bpmn2");
        processes.add("repo/processes/general/BPMN2-SimpleRestartWithErrorOnExitScriptHT.bpmn2");

        InternalKieModule kJar1 = createKieJar(ks, releaseId, processes);
        File pom = new File("target/kmodule", "pom.xml");
        pom.getParentFile().mkdir();
        try {
            FileOutputStream fs = new FileOutputStream(pom);
            fs.write(getPom(releaseId).getBytes());
            fs.close();
        } catch (Exception e) {

        }
        KieMavenRepository repository = getKieMavenRepository();
        repository.deployArtifact(releaseId, kJar1, pom);

        ReleaseId releaseId2 = ks.newReleaseId(GROUP_ID, ARTIFACT_ID, "1.1.0");

        InternalKieModule kJar2 = createKieJar(ks, releaseId2, processes);
        File pom2 = new File("target/kmodule2", "pom.xml");
        pom2.getParentFile().mkdirs();
        try {
            FileOutputStream fs = new FileOutputStream(pom2);
            fs.write(getPom(releaseId2).getBytes());
            fs.close();
        } catch (Exception e) {

        }
        repository = getKieMavenRepository();
        repository.deployArtifact(releaseId2, kJar2, pom2);
    }

    @After
    public void cleanup() {
        cleanupSingletonSessionId();
        if (units != null && !units.isEmpty()) {
            for (DeploymentUnit unit : units) {
                try {
                    deploymentService.undeploy(unit);
                } catch (Exception e) {
                    // do nothing in case of some failed tests to avoid next test to fail as well
                }
            }
            units.clear();
        }
        close();
    }

    @Test
    public void testStartProcessFromNodeIds() {
        assertNotNull(deploymentService);

        KModuleDeploymentUnit deploymentUnit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);

        deploymentService.deploy(deploymentUnit);
        units.add(deploymentUnit);
        assertNotNull(processService);

        Long processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), "restart.simple", singletonMap("var_a", 3));
        assertNotNull(processInstanceId);

        processService.abortProcessInstance(processInstanceId);

        Collection<NodeInstanceDesc> logs = runtimeDataService.getProcessInstanceFullHistoryByType(processInstanceId, EntryType.ABORTED, new QueryContext(0, 0));
        assertThat(logs.size(), is(1));
        String[] nodeIds = logs.stream().map(NodeInstanceDesc::getNodeId).toArray(String[]::new);

        processInstanceId = processService.startProcessFromNodeIds(deploymentUnit.getIdentifier(), "restart.simple", singletonMap("var_a", 3), nodeIds);
        runtimeDataService.getTasksByProcessInstanceId(processInstanceId).forEach(e -> {
            this.userTaskService.start(e, "john");
            this.userTaskService.complete(e, "john", emptyMap());
        });

        processService.abortProcessInstance(processInstanceId);

        logs = runtimeDataService.getProcessInstanceFullHistoryByType(processInstanceId, EntryType.ABORTED, new QueryContext(0, 0));
        nodeIds = logs.stream().map(NodeInstanceDesc::getNodeId).toArray(String[]::new);
        processInstanceId = processService.startProcessFromNodeIds(deploymentUnit.getIdentifier(), "restart.simple", singletonMap("var_a", 3), nodeIds);
        runtimeDataService.getTasksByProcessInstanceId(processInstanceId).forEach(e -> {
            this.userTaskService.start(e, "john");
            this.userTaskService.complete(e, "john", emptyMap());
        });

        ProcessInstance pi = processService.getProcessInstance(processInstanceId);
        assertNull(pi);
    }

    @Test
    public void testStartProcessFromNodeIdsWithTimer() throws InterruptedException {
        final String processId = "simple.restartWithTimer";
        assertNotNull(deploymentService);

        KModuleDeploymentUnit deploymentUnit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);

        deploymentService.deploy(deploymentUnit);
        units.add(deploymentUnit);
        assertNotNull(processService);

        Long processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), processId, singletonMap("var_a", 3));
        assertNotNull(processInstanceId);

        TimeUnit.SECONDS.sleep(3);

        processService.abortProcessInstance(processInstanceId);

        Collection<NodeInstanceDesc> skipped = runtimeDataService.getProcessInstanceFullHistoryByType(processInstanceId, EntryType.SKIPPED, new QueryContext(0, 0));
        assertThat(skipped.size(), is(1));
        Collection<NodeInstanceDesc> aborted = runtimeDataService.getProcessInstanceFullHistoryByType(processInstanceId, EntryType.ABORTED, new QueryContext(0, 0));
        assertThat(aborted.size(), is(1));
        String[] nodeIds = aborted.stream().map(NodeInstanceDesc::getNodeId).toArray(String[]::new);

        processInstanceId = processService.startProcessFromNodeIds(deploymentUnit.getIdentifier(), processId, singletonMap("var_a", 3), nodeIds);
        runtimeDataService.getTasksByProcessInstanceId(processInstanceId).forEach(e -> {
            Task task = this.userTaskService.getTask(e);
            assertThat(task.getName(), is("Third Task"));
            this.userTaskService.start(e, "katy");
            this.userTaskService.complete(e, "katy", emptyMap());
        });

        ProcessInstance pi = processService.getProcessInstance(processInstanceId);
        assertNull(pi);
    }

    @Test
    public void testObsoleteNodes() throws Exception {
        final String processId = "restart.simpleObsolete";
        assertNotNull(deploymentService);

        KModuleDeploymentUnit deploymentUnit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);

        deploymentService.deploy(deploymentUnit);
        units.add(deploymentUnit);
        assertNotNull(processService);

        Long processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), processId, singletonMap("var_a", 3));
        assertNotNull(processInstanceId);

        processService.abortProcessInstance(processInstanceId);

        Collection<NodeInstanceDesc> aborted = runtimeDataService.getProcessInstanceFullHistoryByType(processInstanceId, EntryType.ABORTED, new QueryContext(0, 0));
        assertThat(aborted.size(), is(2));
        String[] nodeIds = aborted.stream().map(e -> e.getNodeId()).toArray(String[]::new);

        processInstanceId = processService.startProcessFromNodeIds(deploymentUnit.getIdentifier(), processId, singletonMap("var_a", 3), nodeIds);
        runtimeDataService.getTasksByProcessInstanceId(processInstanceId).forEach(e -> {
            Task task = this.userTaskService.getTask(e);
            if (task.getName().equals("First Task")) {
                this.userTaskService.start(e, "katy");
                this.userTaskService.complete(e, "katy", emptyMap());
            }
        });

        Collection<NodeInstanceDesc> obsolete = runtimeDataService.getProcessInstanceFullHistoryByType(processInstanceId, EntryType.OBSOLETE, new QueryContext(0, 0));
        assertThat(obsolete.size(), is(1));

        ProcessInstance pi = processService.getProcessInstance(processInstanceId);
        assertNull(pi);
    }
    
    @Test
    public void testErrorNodesOnExitScript() throws Exception {
        final String processId = "restart.simpleErrorOnExitScript";
        assertNotNull(deploymentService);

        KModuleDeploymentUnit deploymentUnit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);

        deploymentService.deploy(deploymentUnit);
        units.add(deploymentUnit);
        assertNotNull(processService);
        
        Long processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), processId, singletonMap("var_exception", "yes"));

        assertNotNull(processInstanceId);
        
        try {
            startAndCompleteTask(processInstanceId, "User Task 1");
        } catch (Exception e) {
          // expected as this is broken script process
        }

        processService.abortProcessInstance(processInstanceId);

        Collection<NodeInstanceDesc> errorNodes = runtimeDataService.getProcessInstanceFullHistoryByType(processInstanceId, EntryType.ERROR, new QueryContext(0, 0));
        assertThat(errorNodes.size(), is(1));
        String[] nodeIds = errorNodes.stream().map(e -> e.getNodeId()).toArray(String[]::new);
        
        
        processInstanceId = processService.startProcessFromNodeIds(deploymentUnit.getIdentifier(), processId, singletonMap("var_exception", "no"), nodeIds);
        
        startAndCompleteTask(processInstanceId, "User Task 1");
        
        startAndCompleteTask(processInstanceId, "User Task 2");
        
        ProcessInstance pi = processService.getProcessInstance(processInstanceId);
        assertNull(pi);
    }
    
    @Test
    public void testErrorNodesOnEntryScript() throws Exception {
        final String processId = "restart.simpleErrorOnEntryScript";
        assertNotNull(deploymentService);

        KModuleDeploymentUnit deploymentUnit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);

        deploymentService.deploy(deploymentUnit);
        units.add(deploymentUnit);
        assertNotNull(processService);
        
        Long processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), processId, singletonMap("var_exception", "yes"));

        assertNotNull(processInstanceId);
        
        try {
            runtimeDataService.getTasksByProcessInstanceId(processInstanceId).forEach(e -> {
                this.userTaskService.start(e, "katy");
            });
        } catch (Exception e) {
          // expected as this is broken script process
        }

        processService.abortProcessInstance(processInstanceId);

        Collection<NodeInstanceDesc> errorNodes = runtimeDataService.getProcessInstanceFullHistoryByType(processInstanceId, EntryType.ERROR, new QueryContext(0, 0));
        assertThat(errorNodes.size(), is(1));
        String[] nodeIds = errorNodes.stream().map(e -> e.getNodeId()).toArray(String[]::new);
        
        
        processInstanceId = processService.startProcessFromNodeIds(deploymentUnit.getIdentifier(), processId, singletonMap("var_exception", "no"), nodeIds);
        
        startAndCompleteTask(processInstanceId, "User Task 1");
        
        startAndCompleteTask(processInstanceId, "User Task 2");
        
        ProcessInstance pi = processService.getProcessInstance(processInstanceId);
        assertNull(pi);
    }

    private void startAndCompleteTask(Long processInstanceId, String taskName) {
        runtimeDataService.getTasksByProcessInstanceId(processInstanceId).forEach(e -> {
            Task task = this.userTaskService.getTask(e);
            if (taskName.equals(task.getName())){
              this.userTaskService.start(e, "katy");
              this.userTaskService.complete(e, "katy", emptyMap());
            }
        });
    }

}
