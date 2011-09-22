package org.jbpm.formbuilder.server.task;

import java.util.List;

import junit.framework.TestCase;

import org.apache.tika.io.IOUtils;
import org.jbpm.formbuilder.shared.task.TaskRef;

public class GuvnorTaskDefinitionServiceTest extends TestCase {

    public void testGetProcessTasks() throws Exception {
        GuvnorTaskDefinitionService service = new GuvnorTaskDefinitionService("", "", "");
        String bpmn2Content = IOUtils.toString(getClass().getResourceAsStream("GuvnorGetProcessTasksTest.bpmn2"));
        List<TaskRef> tasks = service.getProcessTasks(bpmn2Content, "GuvnorGetProcessTasksTest.bpmn2");
        assertNotNull("tasks shouldn't be null", tasks);
        assertTrue("tasks should contain 6 elements", tasks.size() == 6);
    }

    public void testQueryOK() throws Exception {
        //TODO happy path
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
}
