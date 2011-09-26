package org.jbpm.formbuilder.server.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.tika.io.IOUtils;
import org.drools.builder.ResourceType;
import org.easymock.EasyMock;
import org.jbpm.formbuilder.server.mock.MockAnswer;
import org.jbpm.formbuilder.server.mock.MockDeleteMethod;
import org.jbpm.formbuilder.server.mock.MockGetMethod;
import org.jbpm.formbuilder.server.mock.MockPostMethod;
import org.jbpm.formbuilder.server.mock.MockPutMethod;
import org.jbpm.formbuilder.shared.task.TaskRef;
import org.jbpm.formbuilder.shared.task.TaskServiceException;

public class GuvnorTaskDefinitionServiceTest extends TestCase {

    public void testGetProcessTasks() throws Exception {
        GuvnorTaskDefinitionService service = new GuvnorTaskDefinitionService("", "", "");
        String bpmn2Content = IOUtils.toString(getClass().getResourceAsStream("GuvnorGetProcessTasksTest.bpmn2"));
        List<TaskRef> tasks = service.getProcessTasks(bpmn2Content, "GuvnorGetProcessTasksTest.bpmn2");
        assertNotNull("tasks shouldn't be null", tasks);
        assertTrue("tasks should contain 6 elements", tasks.size() == 6);
    }

    public void testQueryOK() throws Exception {
        /*GuvnorTaskDefinitionService service = createService("http://www.redhat.com", "", "");
        HttpClient client = EasyMock.createMock(HttpClient.class);
        Map<String, String> responses = new HashMap<String, String>();
        StringBuilder props = new StringBuilder();
        props.append("sampleProcess1.bpmn2=AAAAA\n");
        props.append("sampleProcess2.bpmn2=AAAAA\n");
        responses.put("GET http://www.redhat.com/org.drools.guvnor.Guvnor/api/packages/somePackage/", props.toString());
        String process1Content = IOUtils.toString(getClass().getResourceAsStream("sampleProcess1.bpmn2"));
        String process2Content = IOUtils.toString(getClass().getResourceAsStream("sampleProcess2.bpmn2"));
        responses.put("GET http://www.redhat.com/org.drools.guvnor.Guvnor/api/packages/somePackage/sampleProcess1.bpmn2", process1Content);
        responses.put("GET http://www.redhat.com/org.drools.guvnor.Guvnor/api/packages/somePackage/sampleProcess2.bpmn2", process2Content);
        EasyMock.expect(client.executeMethod(EasyMock.isA(MockGetMethod.class))).
            andAnswer(new MockAnswer(responses, new IllegalArgumentException("unexpected call"))).times(3);
        
        EasyMock.replay(client);
        List<TaskRef> tasks = service.query("somePackage", "");
        EasyMock.verify(client);
        assertNotNull("tasks shouldn't be null", tasks);*/
        //TODO finish validating input, and create two bpmn2 files in the test resources folder
    }
    
    public void testQueryIOProblem() throws Exception {
        //TODO cause a IOException
    }
    
    public void testQueryUnknownProblem() throws Exception {
        //TODO cause a NullPointerException
    }
    
    public void testGetTasksByNameOK() throws Exception {
        //TODO happy path
    }
    
    public void testGetTasksByNameJAXBProblem() throws Exception {
        //TODO cause a JAXBException
    }
    
    public void testGetTasksByNameIOProblem() throws Exception {
        //TODO cause a IOException
    }
    
    public void testGetTasksByNameUnknownProblem() throws Exception {
        //TODO cause a NullPointerException
    }
    
    public void testGetTaskByUUIDOK() throws Exception {
        //TODO happy path
    }
    
    public void testGetTaskByUUIDJAXBProblem() throws Exception {
        //TODO cause a JAXBException
    }
    
    public void testGetTaskByUUIDIOProblem() throws Exception {
        //TODO cause a IOException
    }

    public void testGetTaskByUUIDUnknownProblem() throws Exception {
        //TODO cause a NullPointerException
    }
    
    public void testGetBPMN2TaskOK() throws Exception {
        //TODO happy path
    }
    
    public void testGetBPMN2TaskNoTasks() throws Exception {
        //TODO happy path 2
    }
    
    public void testGetBPMN2TaskIOProblem() throws Exception {
        //TODO cause a IOException
    }
    
    public void testGetBPMN2TaskUnknownProblem() throws Exception {
        //TODO cause a NullPointerException
    }
    
    private GuvnorTaskDefinitionService createService(String baseUrl, String user, String password) {
        return new GuvnorTaskDefinitionService(baseUrl, user, password) {
            @Override
            protected DeleteMethod createDeleteMethod(String url) {
                return new MockDeleteMethod(url);
            }
            @Override
            protected GetMethod createGetMethod(String url) {
                return new MockGetMethod(url);
            }
            @Override
            protected PostMethod createPostMethod(String url) {
                return new MockPostMethod(url);
            }
            @Override
            protected PutMethod createPutMethod(String url) {
                return new MockPutMethod(url);
            }
        };
    }
}
