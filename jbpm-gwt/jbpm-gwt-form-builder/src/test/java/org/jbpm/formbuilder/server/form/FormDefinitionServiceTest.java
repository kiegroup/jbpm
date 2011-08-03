package org.jbpm.formbuilder.server.form;

import junit.framework.TestCase;

import org.jbpm.formbuilder.shared.form.FormDefinitionService;
import org.jbpm.formbuilder.shared.form.MockFormDefinitionService;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.task.TaskRef;

public class FormDefinitionServiceTest extends TestCase {

    public void testTemplateFormFromTask() throws Exception {
        TaskRef task = new TaskRef();
        task.setTaskId("MyTask");
        FormDefinitionService formService = new MockFormDefinitionService();
        FormRepresentation form = formService.createFormFromTask(task);
        assertNotNull("form shouldn't be null", form);
        assertTrue("form should contain two items", form.getFormItems().size() == 2);
    }
}
