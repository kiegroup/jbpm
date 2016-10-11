/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.migration;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.jbpm.migration.tools.MigrationHelper;

import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.After;
import org.kie.api.io.ResourceType;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Migration test wrapper.
 *
 * Contains methods for test preparation.
 */
public class JbpmMigrationRuntimeTest extends JbpmJUnitBaseTestCase {

    protected static KieBase kbase;

    private static KnowledgeBuilder kbuilder;

    protected static KieSession ksession;

    protected static ProcessDefinition processDef;

    private static JbpmMigrationRuntimeTest instance = new JbpmMigrationRuntimeTest();

    private static File basedir;
    private static Properties properties;
    private static final Logger logger = LoggerFactory.getLogger(JbpmMigrationRuntimeTest.class);

    protected static List<File> migrate(String... processDefinitions) {
        final List<File> migratedFiles = new ArrayList<File>(processDefinitions.length);
        for (String processDefinition : processDefinitions) {
            migratedFiles.add(migrate(processDefinition));
        }
        return migratedFiles;
    }

    protected static File migrate(String processDefinition) {
        File jpdlFile = null;
        try {
            jpdlFile = new File(JbpmMigrationRuntimeTest.class.getResource("/" + processDefinition).toURI());
        } catch (URISyntaxException ex) {
            logger.error(null, ex);
        }
        return migrate(jpdlFile);
    }

    protected static File migrate(File processDefinition) {
        return migrate(processDefinition, processDefinition.getParentFile().getName());
    }

    protected static File migrate(File processDefinition, String bpmnFileName) {
        File bpmnFile = instance.createTempFile(bpmnFileName, "bpmn2");
        MigrationHelper.migration(processDefinition, bpmnFile);
        return bpmnFile;
    }

    protected static void buildNewKbase(File... bpmnFiles) {
        kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        for (File bpmnFile : bpmnFiles) {
            kbuilder.add(ResourceFactory.newFileResource(bpmnFile), ResourceType.BPMN2);
        }

        if (kbuilder.hasErrors()) {
            throw new RuntimeException(kbuilder.getErrors().toString());
        }

        kbase = kbuilder.newKnowledgeBase();
    }

    protected static void createProcessDefinition(String processDefinition) {
        processDef = ProcessDefinition.parseXmlResource(processDefinition);
    }

    /**
     * Creates jpdl process definition, migrates to BPMN2 and builds
     * KnowledgeBase.
     *
     * @param allProcesses
     *            JPDL process definition files, the first one is used for
     *            createProcessDefinition().
     */
    protected static void prepareProcess(String... allProcesses) {
        final List<File> bpmnFiles = migrate(allProcesses);
        createProcessDefinition(allProcesses[0]);
        buildNewKbase(bpmnFiles.toArray(new File[] {}));
    }

    /**
     * Creates jpdl process definition, migrates to BPMN2 and builds
     * KnowledgeBase.
     *
     * @param processDefinition
     *            JPDL process definition file.
     */
    protected static void prepareProcess(String processDefinition) {
        File bpmnFile = migrate(processDefinition);
        createProcessDefinition(processDefinition);
        buildNewKbase(bpmnFile);
    }

    /**
     * Adds new classpath bpmn2 definition to an existing knowledge base.
     *
     * @path path to bpmn2 definition file on classpath.
     */
    protected static void addBpmnProcessFromClassPath(String... paths) {
        for (String path : paths) {
            kbuilder.add(ResourceFactory.newClassPathResource(path, JbpmMigrationRuntimeTest.class), ResourceType.BPMN2);
        }

        if (kbuilder.hasErrors()) {
            throw new RuntimeException(kbuilder.getErrors().toString());
        }

        kbase = kbuilder.newKnowledgeBase();
    }

    /**
     * Creates new knowledge base from classpath bpmn2 definition.
     *
     * @path path to bpmn2 definition file on classpath.
     */
    protected static void createBpmnProcessFromClassPath(String... paths) {
        kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        addBpmnProcessFromClassPath(paths);
    }

    protected final File createTempFile(String name) {
        return createTempFile(name, "");
    }

    public File targetDir(){
        String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File targetDir = new File(relPath+"../../target");
        if(!targetDir.exists()) {
            targetDir.mkdir();
        }
        return targetDir;
    }

    protected final File createTempFile(String name, String extension) {
        File dir = new File(targetDir(), getClass().getSimpleName());
        if (!dir.exists()) {
            dir.mkdir();
        }

        int i = 0;
        File temp;
        while ((temp = new File(dir, String.format("%s_%03d.%s", name, i++, extension))).exists()) {
        }

        return temp;
    }

    @After
    public void dispose() {
        if (ksession != null) {
            ksession.dispose();
        }
    }

}
