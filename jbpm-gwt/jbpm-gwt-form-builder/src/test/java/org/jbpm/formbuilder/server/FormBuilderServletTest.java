package org.jbpm.formbuilder.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.jbpm.formbuilder.shared.form.MockFormDefinitionService;
import org.jbpm.formbuilder.shared.menu.MenuItemDescription;
import org.jbpm.formbuilder.shared.menu.MenuService;
import org.jbpm.formbuilder.shared.menu.MockMenuService;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.rep.items.LabelRepresentation;
import org.jbpm.formbuilder.shared.task.MockTaskDefinitionService;
import org.jbpm.formbuilder.shared.task.TaskDefinitionService;

public class FormBuilderServletTest extends TestCase {

    private FormBuilderServlet servlet; 
    private HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
    private HttpServletResponse resp = EasyMock.createMock(HttpServletResponse.class);
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.servlet = new FormBuilderServlet();
        MenuService menuService = new MockMenuService();
        servlet.setMenuService(menuService);
        TaskDefinitionService taskService = new MockTaskDefinitionService();
        servlet.setTaskService(taskService);
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        this.servlet = null;
    }

    public void testListMenuItems() throws Exception {
        EasyMock.expect(req.getRequestURI()).andReturn("/org.jbpm.formbuilder.FormBuilder/fbapi/menuItems/package/defaultPackage/").once();
        StringWriter writer = new StringWriter();
        EasyMock.expect(resp.getWriter()).andReturn(new PrintWriter(writer)).once();
        resp.setContentType(EasyMock.same("text/xml"));
        EasyMock.expectLastCall().once();
        
        EasyMock.replay(req, resp);
        servlet.doGet(req, resp);
        EasyMock.verify(req, resp);
        String xmlResponse = writer.toString();
        assertNotNull("xml response should not be null", xmlResponse);
        assertTrue("xml response should contain ComboBoxMenuItem", xmlResponse.contains("ComboBoxMenuItem"));
    }

    public void testListMenuOptions() throws Exception {
        EasyMock.expect(req.getRequestURI()).andReturn("/org.jbpm.formbuilder.FormBuilder/fbapi/menuOptions/package/defaultPackage/").once();
        StringWriter writer = new StringWriter();
        EasyMock.expect(resp.getWriter()).andReturn(new PrintWriter(writer)).once();
        resp.setContentType(EasyMock.same("text/xml"));
        EasyMock.expectLastCall().once();
        
        EasyMock.replay(req, resp);
        servlet.doGet(req, resp);
        EasyMock.verify(req, resp);
        String xmlResponse = writer.toString();
        assertNotNull("xml response should not be null", xmlResponse);
        assertTrue("xml response should contain PreviewFormAsFtlCommand", xmlResponse.contains("PreviewFormAsFtlCommand"));
    }
    
    public void testListForms() throws Exception {
        EasyMock.expect(req.getRequestURI()).
            andReturn("/org.jbpm.formbuilder.FormBuilder/fbapi/formDefinitions/package/defaultPackage/").
            once();
        StringWriter writer = new StringWriter();
        EasyMock.expect(resp.getWriter()).andReturn(new PrintWriter(writer)).once();
        resp.setContentType("text/xml");
        EasyMock.expectLastCall().once();
        
        MockFormDefinitionService formService = new MockFormDefinitionService();
        FormRepresentation myForm1 = new FormRepresentation();
        myForm1.setName("myForm");
        myForm1.setTaskId("myTask");
        formService.saveForm("defaultPackage", myForm1);
        FormRepresentation myForm2 = new FormRepresentation();
        myForm2.setName("otherForm");
        myForm2.setTaskId("otherTask");
        formService.saveForm("defaultPackage", myForm2);
        servlet.setFormService(formService);
        
        EasyMock.replay(req, resp);
        servlet.doGet(req, resp);
        EasyMock.verify(req, resp);
        
        String xml = writer.toString();
        assertNotNull("xml shouldn't be null", xml);
        assertFalse("xml shouldn't be empty", xml.equals(""));
        assertTrue("xml should contain myTask", xml.contains("myTask"));
        assertTrue("xml should contain otherTask", xml.contains("otherTask"));
        assertTrue("xml should contain otherForm", xml.contains("otherForm"));
        assertTrue("xml should contain myForm", xml.contains("myForm"));
    }
    
    public void testGetForm() throws Exception {
        EasyMock.expect(req.getRequestURI()).
            andReturn("/org.jbpm.formbuilder.FormBuilder/fbapi/formDefinitions/package/defaultPackage/formDefinitionId/formDefinition_myForm").
            once();
        StringWriter writer = new StringWriter();
        EasyMock.expect(resp.getWriter()).andReturn(new PrintWriter(writer)).once();
        resp.setContentType("text/xml");
        EasyMock.expectLastCall().once();
        
        MockFormDefinitionService formService = new MockFormDefinitionService();
        FormRepresentation myForm1 = new FormRepresentation();
        myForm1.setName("formDefinition_myForm");
        myForm1.setTaskId("myTask");
        formService.saveForm("defaultPackage", myForm1);
        FormRepresentation myForm2 = new FormRepresentation();
        myForm2.setName("formDefinition_otherForm");
        myForm2.setTaskId("otherTask");
        formService.saveForm("defaultPackage", myForm2);
        servlet.setFormService(formService);
        
        EasyMock.replay(req, resp);
        servlet.doGet(req, resp);
        EasyMock.verify(req, resp);
        
        String xml = writer.toString();
        assertNotNull("xml shouldn't be null", xml);
        assertFalse("xml shouldn't be empty", xml.equals(""));
        assertTrue("xml should contain myTask", xml.contains("myTask"));
        assertTrue("xml should contain myForm", xml.contains("myForm"));
        assertFalse("xml shouldn't contain otherTask", xml.contains("otherTask"));
        assertFalse("xml shouldn't contain otherForm", xml.contains("otherForm"));
    }
    
    public void testListFormItems() throws Exception {
        /* TODO Review this test
        EasyMock.expect(req.getRequestURI()).
            andReturn("/org.jbpm.formbuilder.FormBuilder/fbapi/formItems/package/defaultPackage/").
            once();
        StringWriter writer = new StringWriter();
        EasyMock.expect(resp.getWriter()).andReturn(new PrintWriter(writer)).once();
        resp.setContentType("text/xml");
        EasyMock.expectLastCall().once();
        
        MockFormDefinitionService formService = new MockFormDefinitionService();
        LabelRepresentation myFormItem1 = new LabelRepresentation();
        myFormItem1.setValue("some value");
        String formItemId1 = formService.saveFormItem("defaultPackage", null, myFormItem1);
        LabelRepresentation myFormItem2 = new LabelRepresentation();
        myFormItem2.setValue("some other value");
        Thread.sleep(10);
        String formItemId2 = formService.saveFormItem("defaultPackage", null, myFormItem2);
        servlet.setFormService(formService);
        
        EasyMock.replay(req, resp);
        servlet.doGet(req, resp);
        EasyMock.verify(req, resp);
        
        String xml = writer.toString();
        assertNotNull("xml shouldn't be null", xml);
        assertFalse("xml shouldn't be empty", xml.equals(""));
        assertTrue("xml should contain " + formItemId1, xml.contains(formItemId1));
        assertTrue("xml should contain " + formItemId2, xml.contains(formItemId2));
        assertTrue("xml should contain 'some value'", xml.contains("some value"));
        assertTrue("xml should contain 'some other value'", xml.contains("some other value"));*/
    }

    public void testGetFormItem() throws Exception {
        /* TODO Review this test
        EasyMock.expect(req.getRequestURI()).
            andReturn("/org.jbpm.formbuilder.FormBuilder/fbapi/formItems/package/defaultPackage/formItemId/formItemDefinition_bla").
            once();
        StringWriter writer = new StringWriter();
        EasyMock.expect(resp.getWriter()).andReturn(new PrintWriter(writer)).once();
        resp.setContentType("text/xml");
        EasyMock.expectLastCall().once();
        
        MockFormDefinitionService formService = new MockFormDefinitionService();
        LabelRepresentation myFormItem1 = new LabelRepresentation();
        myFormItem1.setValue("some value");
        String formItemId1 = formService.saveFormItem("defaultPackage", "formItemDefinition_bla", myFormItem1);
        Thread.sleep(10);
        LabelRepresentation myFormItem2 = new LabelRepresentation();
        myFormItem2.setValue("some other value");
        String formItemId2 = formService.saveFormItem("defaultPackage", "formItemDefinition_other", myFormItem2);
        servlet.setFormService(formService);
        
        EasyMock.replay(req, resp);
        servlet.doGet(req, resp);
        EasyMock.verify(req, resp);
        
        String xml = writer.toString();
        assertNotNull("xml shouldn't be null", xml);
        assertFalse("xml shouldn't be empty", xml.equals(""));
        assertTrue("xml should contain " + formItemId1, xml.contains(formItemId1));
        assertFalse("xml shouldn't contain " + formItemId2, xml.contains(formItemId2));
        assertTrue("xml should contain 'some value'", xml.contains("some value"));
        assertFalse("xml shouldn't contain 'some other value'", xml.contains("some other value"));*/
    }
    
    public void testListTasks() throws Exception {
        EasyMock.expect(req.getRequestURI()).andReturn("/org.jbpm.formbuilder.FormBuilder/fbapi/tasks/package/defaultPackage/?q=task").once();
        EasyMock.expect(req.getParameter(EasyMock.same("q"))).andReturn("task");
        StringWriter writer = new StringWriter();
        EasyMock.expect(resp.getWriter()).andReturn(new PrintWriter(writer)).once();
        resp.setContentType(EasyMock.same("text/xml"));
        EasyMock.expectLastCall().once();
        
        EasyMock.replay(req, resp);
        servlet.doGet(req, resp);
        EasyMock.verify(req, resp);
        String xmlResponse = writer.toString();
        assertNotNull("xml response should not be null", xmlResponse);
        assertTrue("xml response should contain task1", xmlResponse.contains("task1"));
    }
    
    public void testSaveMenuItem() throws Exception {
        EasyMock.expect(req.getRequestURI()).andReturn("/org.jbpm.formbuilder.FormBuilder/fbapi/menuItems/package/defaultPackage/").once();
        StringBuilder builder = new StringBuilder();
        builder.append("<menuItem>\n");
        builder.append("  <groupName>My Test Group</groupName>\n");
        builder.append("  <name>test component</name>\n");
        URL url = getClass().getResource("/org/jbpm/formbuilder/server/menu/testSaveMenuItem.json");
        String json = FileUtils.readFileToString(new File(url.getFile()));
        String jsonTag = new StringBuilder("  <clone><![CDATA[").append(json).append("]]></clone>\n").toString();
        builder.append(jsonTag);
        builder.append("  <effect className=\"org.jbpm.formbuilder.client.effect.RemoveEffect\" />\n");
        builder.append("  <effect className=\"org.jbpm.formbuilder.client.effect.DoneEffect\" />\n");
        builder.append("  <effect className=\"org.jbpm.formbuilder.client.effect.SaveAsMenuOptionFormEffect\" />\n");
        builder.append("</menuItem>\n");
        String xml = builder.toString();
        EasyMock.expect(req.getReader()).andReturn(new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(xml.getBytes())))).once();
        resp.setStatus(HttpServletResponse.SC_CREATED);
        EasyMock.expectLastCall().once();
        
        EasyMock.replay(req, resp);
        servlet.doPost(req, resp);
        EasyMock.verify(req, resp);
    }
    
    public void testDeleteMenuItem() throws Exception {
        EasyMock.expect(req.getRequestURI()).andReturn("/org.jbpm.formbuilder.FormBuilder/fbapi/menuItems/package/defaultPackage").times(2);
        StringBuilder builder = new StringBuilder();
        builder.append("<menuItem>\n");
        builder.append("  <groupName>My Test Group</groupName>\n");
        builder.append("  <name>test component</name>\n");
        URL url = getClass().getResource("/org/jbpm/formbuilder/server/menu/testSaveMenuItem.json");
        String json = FileUtils.readFileToString(new File(url.getFile()));
        String jsonTag = new StringBuilder("  <clone><![CDATA[").append(json).append("]]></clone>\n").toString();
        builder.append(jsonTag);
        builder.append("  <effect className=\"org.jbpm.formbuilder.client.effect.RemoveEffect\" />\n");
        builder.append("  <effect className=\"org.jbpm.formbuilder.client.effect.DoneEffect\" />\n");
        builder.append("  <effect className=\"org.jbpm.formbuilder.client.effect.SaveAsMenuOptionFormEffect\" />\n");
        builder.append("</menuItem>\n");
        final String xml = builder.toString();
        EasyMock.expect(req.getReader()).andAnswer(new IAnswer<BufferedReader>() {
                    public BufferedReader answer() throws Throwable {
                        byte[] byteArray = xml.getBytes();
                        ByteArrayInputStream bin = new ByteArrayInputStream(byteArray);
                        InputStreamReader inReader = new InputStreamReader(bin);
                        return new BufferedReader(inReader);
                    }
                }).times(2);
        resp.setStatus(HttpServletResponse.SC_CREATED);
        EasyMock.expectLastCall().once();
        resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        EasyMock.expectLastCall().once();
        
        EasyMock.replay(req, resp);
        
        Map<String, List<MenuItemDescription>> itemsInitial = servlet.menuService.listMenuItems();
        servlet.doPost(req, resp);
        Map<String, List<MenuItemDescription>> itemsAfterSave = servlet.menuService.listMenuItems();
        servlet.doDelete(req, resp);
        Map<String, List<MenuItemDescription>> itemsAfterDelete = servlet.menuService.listMenuItems();
        EasyMock.verify(req, resp);
        
        assertNotNull("itemsInitial shouldn't be null", itemsInitial);
        assertNotNull("itemsAfterSave shouldn't be null", itemsAfterSave);
        assertNotNull("itemsAfterDelete shouldn't be null", itemsInitial);
        assertEquals("itemsInitial and itemsAfterDelete should be the same", itemsInitial, itemsAfterDelete);
        assertFalse("itemsAfterSave should be different of itemsInitial", itemsInitial.equals(itemsAfterSave));
    }
}
