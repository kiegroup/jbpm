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
import org.jbpm.formbuilder.shared.menu.MenuItemDescription;
import org.jbpm.formbuilder.shared.menu.MenuService;
import org.jbpm.formbuilder.shared.menu.MockMenuService;
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
        EasyMock.expect(req.getRequestURI()).andReturn("/org.jbpm.formbuilder.FormBuilder/fbapi/defaultPackage/menuItems/").once();
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
        EasyMock.expect(req.getRequestURI()).andReturn("/org.jbpm.formbuilder.FormBuilder/fbapi/defaultPackage/menuOptions/").once();
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
    
    public void testListTasks() throws Exception {
        EasyMock.expect(req.getRequestURI()).andReturn("/org.jbpm.formbuilder.FormBuilder/fbapi/defaultPackage/listTasks/?q=task").once();
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
        EasyMock.expect(req.getRequestURI()).andReturn("/org.jbpm.formbuilder.FormBuilder/fbapi/defaultPackage/menuItems/").once();
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
        EasyMock.expect(req.getRequestURI()).andReturn("/org.jbpm.formbuilder.FormBuilder/fbapi/defaultPackage/menuItems/").times(2);
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
