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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.kie.scanner.KieMavenRepository.getKieMavenRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.kie.test.util.AbstractKieServicesBaseTest;
import org.jbpm.process.core.context.variable.VariableViolationException;
import org.jbpm.services.api.model.DeploymentUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.runtime.conf.ObjectModel;
import org.kie.scanner.KieMavenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessServiceWithVariableTagsTest extends AbstractKieServicesBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(ProcessServiceWithVariableTagsTest.class);

    private List<DeploymentUnit> units = new ArrayList<DeploymentUnit>();

    @Before
    public void prepare() {
    	configureServices();
    	logger.debug("Preparing kjar");
        KieServices ks = KieServices.Factory.get();
        ReleaseId releaseId = ks.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        List<String> processes = new ArrayList<String>();
        processes.add("repo/processes/general/customtask-with-variable-tags.bpmn");
        
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

        identityProvider.setRoles(new ArrayList<String>());
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
    public void testStartProcessWithRestrictedVariable() {
    	assertNotNull(deploymentService);
    	identityProvider.setRoles(Collections.singletonList("admin"));
        KModuleDeploymentUnit deploymentUnit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);

        deploymentService.deploy(deploymentUnit);
        units.add(deploymentUnit);
    	assertNotNull(processService);
    	
    	Map<String, Object> parameters = new HashMap<>();
    	parameters.put("id", "test");

    	long processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), "customtask-tags", parameters);
    	assertNotNull(processInstanceId);

    	ProcessInstance pi = processService.getProcessInstance(processInstanceId);
    	assertNull(pi);
    }
    
    @Test
    public void testStartProcessWithViolatedRestrictedVariable() {
        assertNotNull(deploymentService);        
        KModuleDeploymentUnit deploymentUnit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);

        deploymentService.deploy(deploymentUnit);
        units.add(deploymentUnit);
        assertNotNull(processService);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", "test");

        assertThatExceptionOfType(VariableViolationException.class).isThrownBy(() -> processService.startProcess(deploymentUnit.getIdentifier(), "customtask-tags", parameters));
            }
    
    /*
     * Helper methods 
     */
    @Override
    protected List<ObjectModel> getProcessListeners() {
        List<ObjectModel> listeners = super.getProcessListeners();
        
        listeners.add(new ObjectModel("mvel", "new org.jbpm.process.instance.event.listeners.VariableGuardProcessEventListener(\"admin\", identityProvider)"));
        
        return listeners;
    }

    protected boolean createDescriptor() {
        return true;
    }
}
