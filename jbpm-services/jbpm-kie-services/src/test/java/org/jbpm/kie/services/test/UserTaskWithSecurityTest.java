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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.kie.scanner.KieMavenRepository.getKieMavenRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.kie.test.util.AbstractKieServicesBaseTest;
import org.jbpm.kie.test.util.CountDownListenerFactory;
import org.jbpm.services.api.ProcessInstanceNotFoundException;
import org.jbpm.services.api.model.DeploymentUnit;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.query.QueryContext;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.query.QueryFilter;
import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.kie.internal.runtime.conf.ObjectModel;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.internal.runtime.manager.deploy.DeploymentDescriptorImpl;
import org.kie.scanner.KieMavenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RunWith(Parameterized.class)
public class UserTaskWithSecurityTest extends AbstractKieServicesBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(KModuleDeploymentServiceTest.class); 
    
    @Parameters(name="{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {     
                 { RuntimeStrategy.SINGLETON }, { RuntimeStrategy.PER_PROCESS_INSTANCE }, { RuntimeStrategy.PER_REQUEST }, { RuntimeStrategy.PER_CASE } 
           });
    }
    
    private List<DeploymentUnit> units = new ArrayList<DeploymentUnit>();
    protected String correctUser = "testUser";
    protected String wrongUser = "wrongUser";
    
    private Long processInstanceId = null;
    private KModuleDeploymentUnit deploymentUnit = null;
    
    private RuntimeStrategy strategy;

    public UserTaskWithSecurityTest(RuntimeStrategy strategy) {
        this.strategy = strategy;
    }
       
    @Before
    public void prepare() {
    	configureServices();
    	logger.debug("Preparing kjar");
        KieServices ks = KieServices.Factory.get();
        ReleaseId releaseId = ks.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        List<String> processes = new ArrayList<String>();
        processes.add("repo/processes/general/EmptyHumanTask.bpmn");
        processes.add("repo/processes/general/humanTask.bpmn");
        processes.add("repo/processes/general/BPMN2-UserTask.bpmn2");
        processes.add("repo/processes/general/timer-process.bpmn2");
        
        DeploymentDescriptor customDescriptor = new DeploymentDescriptorImpl("org.jbpm.domain");
		customDescriptor.getBuilder()
		.runtimeStrategy(strategy)
		.addEventListener(new ObjectModel("mvel", "org.jbpm.kie.test.util.CountDownListenerFactory.get(\"securityTest\", \"timer\", 1)"))
		.addRequiredRole("view:managers")
		.addRequiredRole("execute:employees");
		
        Map<String, String> resources = new HashMap<String, String>();
		resources.put("src/main/resources/" + DeploymentDescriptor.META_INF_LOCATION, customDescriptor.toXml());
        
        InternalKieModule kJar1 = createKieJar(ks, releaseId, processes, resources);
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
        
        assertNotNull(deploymentService);
        
        deploymentUnit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);
        
        deploymentService.deploy(deploymentUnit);
        units.add(deploymentUnit);
    	assertNotNull(processService);

    	identityProvider.setRoles(Arrays.asList("employees"));
    }
    
    @After
    public void cleanup() {
    	if (processInstanceId != null) {
    		try {
		    	// let's abort process instance to leave the system in clear state
		    	processService.abortProcessInstance(processInstanceId);
		    	
		    	ProcessInstance pi = processService.getProcessInstance(processInstanceId);    	
		    	assertNull(pi);
    		} catch (ProcessInstanceNotFoundException e) {
    			// ignore it as it was already completed/aborted
    		}
    	}
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
        CountDownListenerFactory.clear();
    }
    
    
 
    @Test
    public void testGetAndWorkOnUserTasks() {
        
        // let's grant managers role so process can be started
        List<String> roles = new ArrayList<String>();
        roles.add("managers");
        roles.add("employees");
        identityProvider.setName("salaboy");
        identityProvider.setRoles(roles);
        
        Collection<ProcessInstanceDesc> instances = runtimeDataService.getProcessInstances(new QueryContext());
        assertNotNull(instances);
        assertEquals(0, instances.size());
        
        processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), "org.jbpm.writedocument");
        assertNotNull(processInstanceId);
        
        roles.clear();
        identityProvider.setRoles(roles);
        List<TaskSummary> tinstances = runtimeDataService.getTasksAssignedAsPotentialOwner(identityProvider.getName(), new QueryFilter());
        assertNotNull(tinstances);
        assertEquals(1, tinstances.size());
        
        Long taskID = tinstances.get(0).getId();
        
        userTaskService.completeAutoProgress(taskID, identityProvider.getName(), null);
        
        roles.add("managers");
        roles.add("employees");        
        identityProvider.setRoles(roles);
        
        processService.abortProcessInstance(processInstanceId);
        processInstanceId = null;
        
        instances = runtimeDataService.getProcessInstances(new QueryContext());
        assertNotNull(instances);
        assertEquals(1, instances.size());
        assertEquals(3, (int)instances.iterator().next().getState());
 
    
    }
    
    @Test(expected=SecurityException.class)
    public void testProcessDoesNotStartForRolesNotAllowed() {
        
        // managers role can not start process
        List<String> roles = new ArrayList<String>();
        roles.add("managers"); 
        identityProvider.setName("salaboy");
        identityProvider.setRoles(roles);
        
        Collection<ProcessInstanceDesc> instances = runtimeDataService.getProcessInstances(new QueryContext());
        assertNotNull(instances);
        assertEquals(0, instances.size());
        
        processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), "org.jbpm.writedocument");
    }
}
