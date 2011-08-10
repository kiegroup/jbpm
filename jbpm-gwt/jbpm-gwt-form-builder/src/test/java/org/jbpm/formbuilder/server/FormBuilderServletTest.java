/**
 * Copyright 2011 JBoss Inc 
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
package org.jbpm.formbuilder.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.jbpm.formbuilder.server.form.FormEncodingServerFactory;
import org.jbpm.formbuilder.shared.form.FormEncodingFactory;
import org.jbpm.formbuilder.shared.form.FormRepresentationEncoder;
import org.jbpm.formbuilder.shared.form.MockFormDefinitionService;
import org.jbpm.formbuilder.shared.menu.MenuItemDescription;
import org.jbpm.formbuilder.shared.menu.MenuService;
import org.jbpm.formbuilder.shared.menu.MockMenuService;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.rep.items.CompleteButtonRepresentation;
import org.jbpm.formbuilder.shared.rep.items.HiddenRepresentation;
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
        FormEncodingFactory.register(FormEncodingServerFactory.getEncoder(), FormEncodingServerFactory.getDecoder());
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
    
    public void testGetFormBuilderProperties() throws Exception {
        EasyMock.expect(req.getRequestURI()).andReturn("/org.jbpm.formbuilder.FormBuilder/fbapi/representationMappings/").once();
        StringWriter writer = new StringWriter();
        EasyMock.expect(resp.getWriter()).andReturn(new PrintWriter(writer)).once();
        resp.setContentType(EasyMock.same("text/xml"));
        EasyMock.expectLastCall().once();
        
        EasyMock.replay(req, resp);
        servlet.doGet(req, resp);
        EasyMock.verify(req, resp);
        String xmlResponse = writer.toString();
        assertNotNull("xml response should not be null", xmlResponse);
        assertTrue("xml response should contain ComboBoxFormItem", xmlResponse.contains("ComboBoxFormItem"));
        assertTrue("xml response should contain <property ", xmlResponse.contains("<property "));
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
    
    public void testListValidations() throws Exception {
        EasyMock.expect(req.getRequestURI()).andReturn("/org.jbpm.formbuilder.FormBuilder/fbapi/validations/").once();
        StringWriter writer = new StringWriter();
        EasyMock.expect(resp.getWriter()).andReturn(new PrintWriter(writer)).once();
        resp.setContentType("text/xml");
        EasyMock.expectLastCall().once();
        
        EasyMock.replay(req, resp);
        servlet.doGet(req, resp);
        EasyMock.verify(req, resp);
        String xmlResponse = writer.toString();
        assertNotNull("xml response should not be null", xmlResponse);
        assertTrue("xml response should contain NotEmptyValidationItem", xmlResponse.contains("NotEmptyValidationItem"));
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
    
    public void testGetFormTemplate() throws Exception {
        //Test post that generates the form template via POST
        EasyMock.expect(req.getRequestURI()).
            andReturn("/org.jbpm.formbuilder.FormBuilder/fbapi/formTemplate/lang/ftl/").once();

        FormRepresentation myForm = new FormRepresentation();
        myForm.setName("myForm");
        myForm.setTaskId("myTask");
        LabelRepresentation label = new LabelRepresentation();
        label.setValue("My label");
        myForm.addFormItem(label);
        HiddenRepresentation hidden = new HiddenRepresentation();
        hidden.setName("hiddenField");
        hidden.setValue("my hidden value");
        myForm.addFormItem(hidden);
        CompleteButtonRepresentation submit = new CompleteButtonRepresentation();
        submit.setName("Complete");
        myForm.addFormItem(submit);
        
        FormRepresentationEncoder encoder = FormEncodingFactory.getEncoder();
        StringBuilder builder = new StringBuilder();
        builder.append("<formPreview>");
        String json = encoder.encode(myForm);
        builder.append("<representation>").append(json).append("</representation>");
        builder.append("</formPreview>");
        String xml = builder.toString();
        EasyMock.expect(req.getReader()).andReturn(new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(xml.getBytes())))).once();
        StringWriter writer = new StringWriter();
        EasyMock.expect(resp.getWriter()).andReturn(new PrintWriter(writer)).once();
        resp.setContentType(EasyMock.same("text/xml"));
        EasyMock.expectLastCall().once();
        
        EasyMock.replay(req, resp);
        servlet.doPost(req, resp);
        EasyMock.verify(req, resp);
        
        String fileNameXml = writer.toString();
        
        assertNotNull("fileNameXml shouldn't be null", fileNameXml);
        assertFalse("fileNameXml shouldn't be empty", "".equals(fileNameXml));
        assertTrue("fileNameXml should start with <fileName>", fileNameXml.startsWith("<fileName>"));
        assertTrue("fileNameXml should end with </fileName>", fileNameXml.contains("</fileName>"));
        
        String fileName = fileNameXml.replace("<fileName>", "").replace("</fileName>", "").replace("\n", "").replace("\r", "");
        
        //With the response (fileName) test the retrieval of the template via GET
        
        HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
        HttpServletResponse resp = EasyMock.createMock(HttpServletResponse.class);
        
        EasyMock.expect(req.getRequestURI()).
            andReturn("/org.jbpm.formbuilder.FormBuilder/fbapi/formTemplate/lang/ftl/").times(2);
        EasyMock.expect(req.getParameter(EasyMock.same("fileName"))).
            andReturn(fileName).once();
        EasyMock.expect(req.getParameter(EasyMock.same("formName"))).
            andReturn(myForm.getName()).once();
        resp.setHeader(EasyMock.same("Content-Disposition"), EasyMock.anyObject(String.class));
        EasyMock.expectLastCall().once();
        resp.setContentLength(EasyMock.anyInt());
        EasyMock.expectLastCall().once();
        
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        EasyMock.expect(resp.getOutputStream()).andReturn(new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                bout.write(b);
            }
        });
        
        EasyMock.replay(req, resp);
        servlet.doGet(req, resp);
        EasyMock.verify(req, resp);
        
        String template = bout.toString();
        
        assertNotNull("template shouldn't be null", template);
        assertFalse("template shouldn't be empty", "".equals(template));
        assertTrue("template should contain myForm's name", template.contains(myForm.getName()));
        assertTrue("template should contain myForm's task ID", template.contains(myForm.getTaskId()));
    }
    
    public void testListFormItems() throws Exception {
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
        String formItemId1 = formService.saveFormItem("defaultPackage", "formItemDefinition_one", myFormItem1);
        LabelRepresentation myFormItem2 = new LabelRepresentation();
        myFormItem2.setValue("some other value");
        Thread.sleep(10);
        String formItemId2 = formService.saveFormItem("defaultPackage", "formItemDefinition_two", myFormItem2);
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
        assertTrue("xml should contain 'some other value'", xml.contains("some other value"));
    }

    public void testGetFormItem() throws Exception {
        EasyMock.expect(req.getRequestURI()).
            andReturn("/org.jbpm.formbuilder.FormBuilder/fbapi/formItems/package/defaultPackage/formItemId/formItemDefinition_some").
            once();
        StringWriter writer = new StringWriter();
        EasyMock.expect(resp.getWriter()).andReturn(new PrintWriter(writer)).once();
        resp.setContentType("text/xml");
        EasyMock.expectLastCall().once();
        
        MockFormDefinitionService formService = new MockFormDefinitionService();
        LabelRepresentation myFormItem1 = new LabelRepresentation();
        myFormItem1.setValue("some value");
        String formItemId1 = formService.saveFormItem("defaultPackage", "formItemDefinition_some", myFormItem1);
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
        assertFalse("xml shouldn't contain 'some other value'", xml.contains("some other value"));
    }
    
    public void testListTasks() throws Exception {
        EasyMock.expect(req.getRequestURI()).andReturn("/org.jbpm.formbuilder.FormBuilder/fbapi/ioAssociations/package/defaultPackage/?q=task").once();
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
                    @Override
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
