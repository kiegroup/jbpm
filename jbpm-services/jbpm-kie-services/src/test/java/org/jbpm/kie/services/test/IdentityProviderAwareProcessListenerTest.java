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

package org.jbpm.kie.services.test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.kie.test.util.AbstractKieServicesBaseTest;
import org.jbpm.persistence.api.integration.EventManagerProvider;
import org.jbpm.persistence.api.integration.InstanceView;
import org.jbpm.persistence.api.integration.model.ProcessInstanceView;
import org.jbpm.services.api.model.DeployedUnit;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.api.model.VariableDesc;
import org.jbpm.test.persistence.processinstance.objects.TestEventEmitter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.query.QueryContext;
import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.kie.internal.runtime.conf.NamedObjectModel;
import org.kie.internal.runtime.manager.deploy.DeploymentDescriptorImpl;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.kie.scanner.KieMavenRepository.getKieMavenRepository;
import static org.jbpm.kie.services.impl.IdentityProviderAwareProcessListener.PROCESS_INITIATOR_KEY;
import static org.jbpm.kie.services.impl.IdentityProviderAwareProcessListener.PROCESS_TERMINATOR_KEY;

public class IdentityProviderAwareProcessListenerTest extends AbstractKieServicesBaseTest {

    private KModuleDeploymentUnit deploymentUnit;

    private static final String PROCESS_ID = "org.jbpm.writedocument";

    @Before
    public void init() {
        configureServices();
        final KieServices ks = KieServices.Factory.get();
        ReleaseId releaseId = ks.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        final List<String> processes = Collections.singletonList("repo/processes/general/humanTask.bpmn");

        final InternalKieModule kJar1 = createKieJar(ks, releaseId, processes);
        final File pom = new File("target/kmodule", "pom.xml");
        pom.getParentFile().mkdir();
        try (FileOutputStream fs = new FileOutputStream(pom)) {
            fs.write(getPom(releaseId).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        getKieMavenRepository().deployArtifact(releaseId, kJar1, pom);

        deploymentUnit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);

        final DeploymentDescriptor descriptor = new DeploymentDescriptorImpl();
        descriptor.getBuilder().addEventListener(new NamedObjectModel(
                "mvel",
                "processIdentity",
                "new org.jbpm.kie.services.impl.IdentityProviderAwareProcessListener(ksession)"
        ));
        deploymentUnit.setDeploymentDescriptor(descriptor);

        deploymentService.deploy(deploymentUnit);

        final DeployedUnit deployed = deploymentService.getDeployedUnit(deploymentUnit.getIdentifier());
        assertNotNull(deployed);
        assertNotNull(deployed.getDeploymentUnit());

        assertNotNull(runtimeDataService);
        Collection<ProcessDefinition> processDefinitions = runtimeDataService.getProcesses(new QueryContext());
        assertNotNull(processDefinitions);
        assertEquals(1, processDefinitions.size());
    }

    @After
    public void cleanup() {
        cleanupSingletonSessionId();
        if (deploymentUnit != null) {
            deploymentService.undeploy(deploymentUnit);
        }
        close();
    }


    @Test
    public void testStartProcessWithIdentityListener() {
        assertNotNull(processService);

        final String userId = "userId";
        identityProvider.setName(userId);

        long processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), PROCESS_ID);
        assertNotNull(processInstanceId);
        try {
            final String initiator = (String) processService.getProcessInstanceVariable(processInstanceId, "initiator");
            assertEquals(userId, initiator);
        } finally {
            processService.abortProcessInstance(processInstanceId);
        }
    }

    @Test
    public void testAbortProcessWithIdentityListener() {

        EventManagerProvider.getInstance().get().setEventEmitter(new TestEventEmitter());
        assertNotNull(processService);

        final String startUserId = "startUserId";
        identityProvider.setName(startUserId);
        long processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), PROCESS_ID);

        final String initiator = (String) processService.getProcessInstanceVariable(processInstanceId, PROCESS_INITIATOR_KEY);
        assertEquals(startUserId, initiator);

        final String abortUserId = "abortUserId";
        identityProvider.setName(abortUserId);
        processService.abortProcessInstance(processInstanceId);

        Collection<VariableDesc> variableHistory = runtimeDataService.getVariableHistory(processInstanceId, PROCESS_TERMINATOR_KEY, null);
        assertThat(variableHistory).hasSize(1);
        VariableDesc var = variableHistory.iterator().next();
        assertThat(var.getVariableId()).isEqualTo(PROCESS_TERMINATOR_KEY);
        assertThat(var.getNewValue()).isEqualTo(abortUserId);

        List<InstanceView<?>> events = TestEventEmitter.getListEvents();
        List<ProcessInstanceView> views = events.stream().filter(ProcessInstanceView.class::isInstance).map(ProcessInstanceView.class::cast).collect(Collectors.toList());
        assertThat(views).hasSize(2);
        assertThat(views).extracting(ProcessInstanceView::getTerminator).anyMatch(abortUserId::equals);
        TestEventEmitter.clear();

    }

}