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
}
