package org.jbpm.formbuilder.server;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.jbpm.formbuilder.shared.menu.MenuService;
import org.jbpm.formbuilder.shared.menu.MockMenuService;
import org.jbpm.formbuilder.shared.task.MockTaskDefinitionService;
import org.jbpm.formbuilder.shared.task.TaskDefinitionService;

public class FormBuilderServletTest extends TestCase {

    public void testMenuItems() throws Exception {
        FormBuilderServlet servlet = new FormBuilderServlet();
        MenuService menuService = new MockMenuService();
        servlet.setMenuService(menuService);
        HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(req.getRequestURI()).andReturn("/org.jbpm.formbuilder.FormBuilder/fbapi/defaultPackage/menuItems/").once();
        HttpServletResponse resp = EasyMock.createMock(HttpServletResponse.class);
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

    public void testMenuOptions() throws Exception {
        FormBuilderServlet servlet = new FormBuilderServlet();
        MenuService menuService = new MockMenuService();
        servlet.setMenuService(menuService);
        HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(req.getRequestURI()).andReturn("/org.jbpm.formbuilder.FormBuilder/fbapi/defaultPackage/menuOptions/").once();
        HttpServletResponse resp = EasyMock.createMock(HttpServletResponse.class);
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
        FormBuilderServlet servlet = new FormBuilderServlet();
        TaskDefinitionService taskService = new MockTaskDefinitionService();
        servlet.setTaskService(taskService);
        HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(req.getRequestURI()).andReturn("/org.jbpm.formbuilder.FormBuilder/fbapi/defaultPackage/listTasks/?q=task").once();
        EasyMock.expect(req.getParameter(EasyMock.same("q"))).andReturn("task");
        HttpServletResponse resp = EasyMock.createMock(HttpServletResponse.class);
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
}
