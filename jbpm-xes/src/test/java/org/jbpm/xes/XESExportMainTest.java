/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.xes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.jbpm.xes.model.AttributeStringType;
import org.jbpm.xes.model.LogType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class XESExportMainTest extends XESPersistenceBase {

    private static final String XES_IEEE_SCHEMA = "xes-ieee-1849-2016.xsd";
    private static final String PROCESS = "com.sample.bpmn.hello";
    private static final String ABORTED_XES_FILE = "aborted.xes";
    private static final String ACTIVE_COMPLETED_XES_FILE = "activeCompleted.xes";
    private static final String COMPLETED_XES_FILE = "completed.xes";
    private static final String COMPLETED_EE_XES_FILE = "completedEnterExit.xes";
    private static final String COMPLETED_REL_XES_FILE = "completedRelevant.xes";
    private static final String COMPLETED_1_2_XES_FILE = "completed12.xes";
    private static final String ALL_STATUS_XES_FILE = "allStatus.xes";
    private static final List<String> xesFiles = Arrays.asList(ABORTED_XES_FILE, 
                                                               ACTIVE_COMPLETED_XES_FILE,
                                                               COMPLETED_XES_FILE,
                                                               COMPLETED_EE_XES_FILE,
                                                               COMPLETED_REL_XES_FILE,
                                                               ALL_STATUS_XES_FILE);
    
    private static final Logger logger = LoggerFactory.getLogger(XESExportMainTest.class);
    
    private String driver;
    private String url;
    private String password;
    private String user;

    private KieSession ksession;
    private TaskService taskService;
    private RuntimeEngine runtimeEngine;
    
    @Before
    public void setup() {
        getMainInputParams();

        // create runtime manager with single process - hello.bpmn
        createRuntimeManager("humantask.bpmn");

        // take RuntimeManager to work with process engine
        runtimeEngine = getRuntimeEngine();

        // get access to KieSession instance
        ksession = runtimeEngine.getKieSession();

        taskService = runtimeEngine.getTaskService();

        activeProcess();      
        completeProcess();
        abortedProcess();
    }

    private void getMainInputParams() {
        user = dsProps.getProperty("user");
        password = dsProps.getProperty("password");
        url = dsProps.getProperty("url");
        driver = dsProps.getProperty("driverClassName");
    }

    private void activeProcess() {
        ksession.startProcess(PROCESS);     
        startAndCompleteTask("john");
    }

    private void startAndCompleteTask(String resource) {
        List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner(resource, "en-UK");

        TaskSummary taskSummary = tasks.get(0);

        taskService.start(taskSummary.getId(), resource);
        taskService.complete(taskSummary.getId(), resource, null);
    }

    private void completeProcess() {
        activeProcess();
        startAndCompleteTask("mary");
    }

    private void abortedProcess() {
        ProcessInstance instance = ksession.startProcess(PROCESS); 
        ksession.abortProcessInstance(instance.getId());	
    }


    @After
    public void cleanup() throws Exception {
        xesFiles.forEach(f -> {
            try {
                Files.deleteIfExists(Paths.get(f));
            } catch (IOException e) {
                logger.debug("Exception during deleting file {} - {}", f, e.getMessage());
            }
        });

    }


    @Test
    public void testHelloProcessWithoutFilteringStatus() throws Exception {      
        XESExportMain.main(new String[] {"-user", user,
                                         "-password", password,
                                         "-url", url, 
                                         "-driver", driver,
                                         "-process", PROCESS,
                                         "-file" , ALL_STATUS_XES_FILE});

        assertTrue(validateXML(ALL_STATUS_XES_FILE));
        LogType log = assertTraceNodeInstances(ALL_STATUS_XES_FILE, 3);
        assertStatus(log, "active", "completed", "aborted");
    }

    @Test
    public void testHelloProcessFilteringStatusCompleteAndActive() throws Exception {
        XESExportMain.main(new String[] {"-user", user,
                                         "-password", password,
                                         "-url", url, 
                                         "-driver", driver,
                                         "-process", PROCESS,
                                         "-status", "1,2",  //1: active, 2: completed
                                         "-file" , ACTIVE_COMPLETED_XES_FILE});

        assertTrue(validateXML(ACTIVE_COMPLETED_XES_FILE));
        LogType log = assertTraceNodeInstances(ACTIVE_COMPLETED_XES_FILE, 2);               
        assertStatus(log, "active", "completed");
    }

    @Test
    public void testHelloProcessFilteringStatusAborted() throws Exception {
        XESExportMain.main(new String[] {"-user", user,
                                         "-password", password,
                                         "-url", url, 
                                         "-driver", driver,
                                         "-process", PROCESS,
                                         "-status", "3",  //aborted
                                         "-file" , ABORTED_XES_FILE});
        
        assertTrue(validateXML(ABORTED_XES_FILE));
        LogType log = assertTraceNodeInstances(ABORTED_XES_FILE, 1);
        assertEquals(2, log.getTrace().get(0).getEvent().size());
        assertStatus(log, "aborted");
    }
    
    @Test
    public void testHelloProcessFilteringStatusCompletedEnterAndExitEvents() throws Exception {
        XESExportMain.main(new String[] {"-user", user,
                                         "-password", password,
                                         "-url", url, 
                                         "-driver", driver,
                                         "-process", PROCESS,
                                         "-status", "2",  //2: completed
                                         "-file" , COMPLETED_EE_XES_FILE});

        assertTrue(validateXML(COMPLETED_EE_XES_FILE));
        LogType log = assertTraceNodeInstances(COMPLETED_EE_XES_FILE, 1);
        assertEquals(4, log.getTrace().get(0).getEvent().size());
        assertStatus(log, "completed");
    }
    
    @Test
    public void testHelloProcessFilteringStatusCompletedOnlyExitEvents() throws Exception {
        XESExportMain.main(new String[] {"-user", user,
                                         "-password", password,
                                         "-url", url, 
                                         "-driver", driver,
                                         "-process", PROCESS,
                                         "-status", "2",  //2: completed
                                         "-file" , COMPLETED_XES_FILE,
                                         "-logtype", "1"});

        assertTrue(validateXML(COMPLETED_XES_FILE));
        LogType log = assertTraceNodeInstances(COMPLETED_XES_FILE, 1);
        assertEquals(2, log.getTrace().get(0).getEvent().size());
        assertStatus(log, "completed");
    }
    
    @Test
    public void testHelloProcessFilteringStatusCompletedRelevantNodes() throws Exception {
        XESExportMain.main(new String[] {"-user", user,
                                         "-password", password,
                                         "-url", url, 
                                         "-driver", driver,
                                         "-process", PROCESS,
                                         "-status", "2",  //2: completed
                                         "-file" , COMPLETED_REL_XES_FILE,
                                         "-nodetypes"});

        assertTrue(validateXML(COMPLETED_REL_XES_FILE));
        LogType log = assertTraceNodeInstances(COMPLETED_REL_XES_FILE, 1);
        assertEquals(8, log.getTrace().get(0).getEvent().size());
        assertStatus(log, "completed");
    }
    
    @Test
    public void testHelloProcessFilteringStatusCompletedVersion() throws Exception {
        XESExportMain.main(new String[] {"-user", user,
                                         "-password", password,
                                         "-url", url, 
                                         "-driver", driver,
                                         "-process", PROCESS,
                                         "-status", "2",  //2: completed
                                         "-file" , COMPLETED_1_2_XES_FILE,
                                         "-version", "1.2"});

        assertTrue(validateXML(COMPLETED_1_2_XES_FILE));
        LogType log = assertTraceNodeInstances(COMPLETED_1_2_XES_FILE, 1);
        assertEquals(4, log.getTrace().get(0).getEvent().size());
        assertStatus(log, "completed");
    }

    private boolean validateXML(String fileName) { 
        File schemaFile = Paths.get("src", "test", "resources", XES_IEEE_SCHEMA).toFile();
        Source xmlFile = new StreamSource(new File(fileName));
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            Schema schema = schemaFactory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            validator.validate(xmlFile);
            return true;
        } catch (SAXException e) {
            logger.error("Exception during validating xml file {} - {} - {}", fileName, xmlFile.getSystemId(), e.getMessage());
        } catch (IOException e) {}
        return false;
    }

    private LogType assertTraceNodeInstances(String file, int expected) throws Exception, IOException {
        XESLogMarshaller marshaller = new XESLogMarshaller();

        LogType log = marshaller.unmarshall(new String ( Files.readAllBytes( Paths.get(file) )));
        assertEquals(expected, log.getTrace().size());
        return log;
    }

    private void assertStatus(LogType log, String ... expectedStatus) {
        List<String> status = log.getTrace().stream()
                .map(t->t.getStringOrDateOrInt())
                .flatMap(List::stream)
                .filter(AttributeStringType.class::isInstance)
                .map(AttributeStringType.class::cast)
                .filter(sic -> sic.getKey().equals("jbpm:status"))
                .map(st -> new String(st.getValue()))
                .collect(Collectors.toList());

        assertTrue(status.containsAll(Arrays.asList(expectedStatus)));
    }


}
