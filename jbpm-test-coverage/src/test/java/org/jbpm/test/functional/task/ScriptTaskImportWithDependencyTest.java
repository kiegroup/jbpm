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

package org.jbpm.test.functional.task;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.jbpm.kie.test.util.AbstractKieServicesBaseTest;
import org.jbpm.process.instance.event.DefaultSignalManagerFactory;
import org.jbpm.process.instance.impl.DefaultProcessInstanceManagerFactory;
import org.jbpm.test.JbpmTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.scanner.KieMavenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptTaskImportWithDependencyTest extends AbstractKieServicesBaseTest {

    private static final String SCRIPT_TASK_MVEL_IMPORT = "/org/jbpm/test/functional/task/ScriptTaskMvelImportWithDep.bpmn";
//    private static final String SCRIPT_TASK_MVEL_IMPORT = "/org/jbpm/test/functional/task/ScriptTaskMvelImportWithDep2.bpmn";
//    private static final String SCRIPT_TASK_MVEL_IMPORT = "/org/jbpm/test/functional/task/ScriptTaskMvelImportWithDep3.bpmn";

    private static final String SCRIPT_TASK_MVEL_IMPORT_ID = "org.jbpm.test.functional.task.ScriptTaskMvelImportWithDep";

    private static final String GROUP_ID = "org.example";
    private static final String ARTIFACT_ID = "script-dep";
    private static final String VERSION = "1.0.0";

    protected static final Logger logger = LoggerFactory.getLogger(ScriptTaskImportWithDependencyTest.class);
    private static RuntimeManager runtimeManager;
    private static RuntimeEngine runtimeEngine;
    private static KieSession kieSession;

    @Before
    public void prepare() throws Exception {
        KieServices ks = KieServices.Factory.get();
        // create dependency kjar
        ReleaseId releaseIdDep = ks.newReleaseId(GROUP_ID, "dependency-data", VERSION);

        String classStr = "package defaultPackage;\n"
                + "public class MyUtil {\n"
                + "    public static void myStaticMethod() {\n"
                + "        System.out.println(\"Hey I'm MyUtil\");\n"
                + "    }\n"
                + "}";

        Map<String, String> resourcesDep = new HashMap<String, String>();
        resourcesDep.put("src/main/java/defaultPackage/MyUtil.java", classStr);

        InternalKieModule kJarDep = createKieJar(ks, releaseIdDep, new ArrayList<String>(), resourcesDep);
        installKjar(releaseIdDep, kJarDep);

        // create first kjar that will have dependency to another
        ReleaseId releaseId = ks.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);

        Map<String, String> resources = new HashMap<String, String>();
        String process3String = IOUtils.toString(ScriptTaskImportWithDependencyTest.class.getResourceAsStream(SCRIPT_TASK_MVEL_IMPORT), "UTF-8");
        resources.put("src/main/resources" + SCRIPT_TASK_MVEL_IMPORT, process3String);

        InternalKieModule kJar1 = createKieJar(ks, releaseId, new ArrayList<String>(), resources, releaseIdDep);
        installKjar(releaseId, kJar1);

        KieContainer kieContainer = ks.newKieContainer(releaseId);
        RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get()
                .newEmptyBuilder()
                .knowledgeBase(kieContainer.getKieBase())
                .addConfiguration("drools.processSignalManagerFactory", DefaultSignalManagerFactory.class.getName())
                .addConfiguration("drools.processInstanceManagerFactory", DefaultProcessInstanceManagerFactory.class.getName())
                .get();

        runtimeManager = RuntimeManagerFactory.Factory.get().newSingletonRuntimeManager(environment);
        runtimeEngine = runtimeManager.getRuntimeEngine(EmptyContext.get());
        kieSession = runtimeEngine.getKieSession();
    }

    @After
    public void cleanup() {
        runtimeManager.disposeRuntimeEngine(runtimeEngine);
        runtimeManager.close();
        cleanupSingletonSessionId();
    }

    @Test(timeout = 30000)
    public void testScriptTaskMvelImport() throws Exception {
        // JBPM-
        testScriptTask(SCRIPT_TASK_MVEL_IMPORT_ID);
    }

    private void testScriptTask(String processId) throws Exception {
        ProcessInstance pi = kieSession.startProcess(processId);

        assertEquals(ProcessInstance.STATE_COMPLETED, pi.getState());
    }

    protected void installKjar(ReleaseId releaseId, InternalKieModule kJar1) {
        File pom = new File("target/kmodule", "pom.xml");
        pom.getParentFile().mkdir();
        try {
            FileOutputStream fs = new FileOutputStream(pom);
            fs.write(getPom(releaseId).getBytes());
            fs.close();
        } catch (Exception e) {

        }
        KieMavenRepository repository = KieMavenRepository.getKieMavenRepository();
        repository.installArtifact(releaseId, kJar1, pom);
    }
}
