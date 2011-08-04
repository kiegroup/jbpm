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
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.formbuilder.client.menu.items.CustomMenuItem;
import org.jbpm.formbuilder.server.form.FormDefDTO;
import org.jbpm.formbuilder.server.form.FormEncodingServerFactory;
import org.jbpm.formbuilder.server.form.FormItemDefDTO;
import org.jbpm.formbuilder.server.form.GuvnorFormDefinitionService;
import org.jbpm.formbuilder.server.form.ListFormsDTO;
import org.jbpm.formbuilder.server.form.ListFormsItemsDTO;
import org.jbpm.formbuilder.server.form.SaveMenuItemDTO;
import org.jbpm.formbuilder.server.menu.GuvnorMenuService;
import org.jbpm.formbuilder.server.render.Renderer;
import org.jbpm.formbuilder.server.render.RendererException;
import org.jbpm.formbuilder.server.render.RendererFactory;
import org.jbpm.formbuilder.server.task.GuvnorTaskDefinitionService;
import org.jbpm.formbuilder.server.trans.Language;
import org.jbpm.formbuilder.server.trans.LanguageException;
import org.jbpm.formbuilder.server.trans.LanguageFactory;
import org.jbpm.formbuilder.server.xml.FormEffectDTO;
import org.jbpm.formbuilder.server.xml.FormPreviewDTO;
import org.jbpm.formbuilder.server.xml.FormPreviewParameterDTO;
import org.jbpm.formbuilder.server.xml.ListMenuItemsDTO;
import org.jbpm.formbuilder.server.xml.ListOptionsDTO;
import org.jbpm.formbuilder.server.xml.ListTasksDTO;
import org.jbpm.formbuilder.server.xml.ListValidationsDTO;
import org.jbpm.formbuilder.server.xml.MenuGroupDTO;
import org.jbpm.formbuilder.server.xml.MenuItemDTO;
import org.jbpm.formbuilder.server.xml.MenuOptionDTO;
import org.jbpm.formbuilder.server.xml.MetaDataDTO;
import org.jbpm.formbuilder.server.xml.PropertiesDTO;
import org.jbpm.formbuilder.server.xml.PropertiesItemDTO;
import org.jbpm.formbuilder.server.xml.PropertyDTO;
import org.jbpm.formbuilder.server.xml.TaskRefDTO;
import org.jbpm.formbuilder.server.xml.ValidationDTO;
import org.jbpm.formbuilder.shared.form.FormDefinitionService;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormEncodingFactory;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.form.FormServiceException;
import org.jbpm.formbuilder.shared.menu.FormEffectDescription;
import org.jbpm.formbuilder.shared.menu.MenuItemDescription;
import org.jbpm.formbuilder.shared.menu.MenuOptionDescription;
import org.jbpm.formbuilder.shared.menu.MenuService;
import org.jbpm.formbuilder.shared.menu.MenuServiceException;
import org.jbpm.formbuilder.shared.menu.ValidationDescription;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.task.TaskDefinitionService;
import org.jbpm.formbuilder.shared.task.TaskRef;
import org.jbpm.formbuilder.shared.task.TaskServiceException;

public class FormBuilderServlet extends HttpServlet {

    private static final long serialVersionUID = -5961620265453738055L;
    private static final Log log = LogFactory.getLog(FormBuilderServlet.class);

    MenuService menuService;
    TaskDefinitionService taskService;
    FormDefinitionService formService;

    private Map<Class<?>[], Marshaller> marshallers = new HashMap<Class<?>[], Marshaller>();
    private Map<Class<?>[], Unmarshaller> unmarshallers = new HashMap<Class<?>[], Unmarshaller>();
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        FormEncodingFactory.register(FormEncodingServerFactory.getEncoder(), FormEncodingServerFactory.getDecoder());
        this.menuService = new GuvnorMenuService();
        String baseUrl = config.getServletContext().getInitParameter("guvnor-base-url");
        String user = config.getServletContext().getInitParameter("guvnor-user");
        String pass = config.getServletContext().getInitParameter("guvnor-password");
        this.taskService = new GuvnorTaskDefinitionService(baseUrl, user, pass);
        this.formService = new GuvnorFormDefinitionService(baseUrl, user, pass);
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String uri = req.getRequestURI();
            StringBuilder content = new StringBuilder();
            if (uri.contains("/menuItems/")) {
                resp.setContentType("text/xml");
                content.append(listMenuItems());
            } else if (uri.contains("/menuOptions/")) {
                resp.setContentType("text/xml");
                content.append(listOptions());
            } else if (uri.contains("/ioAssociations/")) {
                resp.setContentType("text/xml");
                content.append(listIoAssociations(getUriParameter(uri, "package"), req.getParameter("q")));
            } else if (uri.contains("/ioAssociation/")) {
                resp.setContentType("text/xml");
                content.append(getIoAssociation(getUriParameter(uri, "package"), getUriParameter(uri, "process"), getUriParameter(uri, "task")));
            } else if (uri.contains("/validations/")) {
                resp.setContentType("text/xml");
                content.append(listValidations());
            } else if (uri.contains("/formDefinitions/")) {
                String pkgName = getUriParameter(uri, "package");
                String formId = getUriParameter(uri, "formDefinitionId");
                resp.setContentType("text/xml");
                if (formId == null) {
                    content.append(getForms(pkgName));
                } else {
                    content.append(getForm(pkgName, formId));
                }
            } else if (uri.contains("/formItems/")) {
                String pkgName = getUriParameter(uri, "package");
                String formItemId = getUriParameter(uri, "formItemId");
                resp.setContentType("text/xml");
                if (formItemId == null) {
                    content.append(getFormItems(pkgName));
                } else {
                    content.append(getFormItem(pkgName, formItemId));
                }
            } else if (uri.contains("/formTemplate/")) {
                exportTemplateFile(req, resp);
            } else if (uri.contains("/representationMappings/")) {
                resp.setContentType("text/xml");
                content.append(getRepresentationMappings());
            } else { //print help
                req.getRequestDispatcher("/fbapi/help.jsp").forward(req, resp);
            }
            if (content.length() > 0) {
                resp.getWriter().println(content.toString());
            }
        } catch (Exception e) {
            log.error("Problem during FormBuilderServlet:GET", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
        } 
    }

    private String listValidations() throws JAXBException, MenuServiceException {
        List<ValidationDescription> validations = menuService.listValidations();
        ListValidationsDTO dto = new ListValidationsDTO(validations);
        return jaxbTransformation(dto, ListValidationsDTO.class, ValidationDTO.class, PropertiesItemDTO.class);
    }
    
    private void exportTemplateFile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String fileName = req.getParameter("fileName");
        String formName = req.getParameter("formName");
        String language = getUriParameter(req.getRequestURI(), "lang");
        File file = new File(fileName);
        resp.setContentLength((int) file.length());
        String headerValue = new StringBuilder("attachment; filename=\"").
            append(formName).append('.').append(language).
            append("\"").toString();
        resp.setHeader("Content-Disposition", headerValue);
        IOUtils.write(FileUtils.readFileToByteArray(file), resp.getOutputStream());
    }
    
    private String getUriParameter(String requestUri, String paramName) {
        if (!requestUri.contains(paramName)) {
            return null;
        }
        paramName = "/" + paramName + "/";
        try {
            int start = requestUri.indexOf(paramName) + paramName.length();
            int end = requestUri.indexOf("/", start + 1);
            if (end == -1) {
                end = requestUri.length();
            }
            return requestUri.substring(start, end);
        } catch (RuntimeException e) {
            return null;
        }
    }
    
    private String getForms(String pkgName) throws FormServiceException, FormEncodingException, JAXBException {
        List<FormRepresentation> forms = formService.getForms(pkgName);
        ListFormsDTO dto = new ListFormsDTO(forms);
        return jaxbTransformation(dto, ListFormsDTO.class, FormDefDTO.class);
    }
    
    private String getForm(String pkgName, String formId) throws FormServiceException, FormEncodingException, JAXBException {
        FormRepresentation form = formService.getForm(pkgName, formId);
        ListFormsDTO dto = new ListFormsDTO(form);
        return jaxbTransformation(dto, ListFormsDTO.class, FormDefDTO.class);
    }
    
    private String getFormItems(String pkgName) throws FormServiceException, FormEncodingException, JAXBException {
        Map<String, FormItemRepresentation> formItems = formService.getFormItems(pkgName);
        ListFormsItemsDTO dto = new ListFormsItemsDTO(formItems);
        return jaxbTransformation(dto, ListFormsItemsDTO.class, FormItemDefDTO.class);
    }
    
    private String getFormItem(String pkgName, String formItemId) throws FormServiceException, FormEncodingException, JAXBException {
        FormItemRepresentation formItem = formService.getFormItem(pkgName, formItemId);
        ListFormsItemsDTO dto = new ListFormsItemsDTO(formItemId, formItem);
        return jaxbTransformation(dto, ListFormsItemsDTO.class, FormItemDefDTO.class);
    }
    
    private String getFormPreview(String requestUri, BufferedReader reader) throws JAXBException, LanguageException, RendererException, FormEncodingException {
        
        String language = getUriParameter(requestUri, "lang");
        FormPreviewDTO dto = jaxbTransformation(reader, FormPreviewDTO.class, new Class[] {FormPreviewDTO.class, FormPreviewParameterDTO.class});
        
        URL url = createTemplate(language, dto);
        Map<String, Object> inputs = dto.getInputsAsMap();
        StringBuilder builder = new StringBuilder();
        Renderer renderer = RendererFactory.getInstance().getRenderer(language);
        builder.append("<html><head><title>Test Form: ");
        builder.append(dto.getForm().getName()).append("</title></head><body>");
        builder.append(renderer.render(url, inputs));
        builder.append("</body></html>");
        return builder.toString();
    }
    
    private String getFormTemplate(String requestUri, BufferedReader reader) throws JAXBException, 
            LanguageException, FormEncodingException, IOException {
        String language = getUriParameter(requestUri, "lang");
        FormPreviewDTO dto = jaxbTransformation(reader, FormPreviewDTO.class, 
                new Class[] {FormPreviewDTO.class, FormPreviewParameterDTO.class});
        URL url = createTemplate(language, dto);
        return url.getFile();
    }

    private URL createTemplate(String language, FormPreviewDTO dto) throws FormEncodingException, LanguageException {
        FormRepresentationDecoder decoder = FormEncodingFactory.getDecoder();
        String json = dto.getRepresentation();
        FormRepresentation form = decoder.decode(json);
        dto.setForm(form);
        Language translator = LanguageFactory.getInstance().getLanguage(language);
        URL url = translator.translateForm(form);
        return url;
    }

    private String getIoAssociation(String pkgName, String processName, String taskName) throws JAXBException , TaskServiceException {
        List<TaskRef> tasks = taskService.getTasksByName(pkgName, processName, taskName);
        ListTasksDTO dto = new ListTasksDTO(tasks);
        return jaxbTransformation(dto, ListTasksDTO.class, TaskRefDTO.class, PropertyDTO.class, MetaDataDTO.class);
    }
    
    private String listIoAssociations(String pkgName, String filter) throws JAXBException, TaskServiceException {
        String[] filters = filter == null ? new String[0] : filter.split(" ");
        String newFilter = filters.length == 0 ? (filter == null ? "" : filter) : "";
        for (String subFilter : filters) {
            if (subFilter.startsWith("iotype:")) {
                //TODO String type = subFilter.replace("iotype:", ""); decide what to do with this filter
            } else {
                newFilter += subFilter + " ";
            }
        }
        List<TaskRef> tasks = taskService.query(pkgName, newFilter);
        ListTasksDTO dto = new ListTasksDTO(tasks);
        return jaxbTransformation(dto, ListTasksDTO.class, TaskRefDTO.class, PropertyDTO.class, MetaDataDTO.class);
    }
    
    private String listMenuItems() throws JAXBException, MenuServiceException {
        Map<String, List<MenuItemDescription>> items = menuService.listMenuItems();
        ListMenuItemsDTO dto = new ListMenuItemsDTO(items);
        return jaxbTransformation(dto, ListMenuItemsDTO.class, MenuGroupDTO.class, MenuItemDTO.class, FormEffectDTO.class);
    }
    
    private String getRepresentationMappings() throws JAXBException, MenuServiceException {
        Map<String, String> props = menuService.getFormBuilderProperties();
        PropertiesDTO dto = new PropertiesDTO(props);
        return jaxbTransformation(dto, PropertiesDTO.class, PropertiesItemDTO.class);
    }

    private String listOptions() throws JAXBException, MenuServiceException {
        List<MenuOptionDescription> options = menuService.listOptions();
        ListOptionsDTO dto = new ListOptionsDTO(options);
        return jaxbTransformation(dto, ListOptionsDTO.class, MenuOptionDTO.class);
    }

    private String jaxbTransformation(Object dto, Class<?>... boundClasses) throws JAXBException {
        synchronized (this) {
            if (marshallers.get(boundClasses) == null) {
                JAXBContext ctx = JAXBContext.newInstance(boundClasses);
                Marshaller marshaller = ctx.createMarshaller();
                marshallers.put(boundClasses, marshaller);
            }
        }
        Marshaller marshaller = marshallers.get(boundClasses);
        StringWriter writer = new StringWriter();
        marshaller.marshal(dto, writer);
        return writer.toString();
    }
    
    @SuppressWarnings("unchecked")
    private <T> T jaxbTransformation(BufferedReader reader, Class<T> retvalType, Class<?>... allBoundClasses) throws JAXBException {
        synchronized (this) {
            if (unmarshallers.get(allBoundClasses) == null) {
                JAXBContext ctx = JAXBContext.newInstance(allBoundClasses);
                Unmarshaller unmarshaller = ctx.createUnmarshaller();
                unmarshallers.put(allBoundClasses, unmarshaller);
            }
        }
        Unmarshaller unmarshaller = unmarshallers.get(allBoundClasses);
        Object obj = unmarshaller.unmarshal(reader);
        return (T) obj;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String uri = req.getRequestURI();
            if (uri.contains("/menuItems/")) {
                int status = saveMenuItem(req.getReader());
                resp.setStatus(status);
            } else if (uri.contains("/formItems/")) {
                String formItemId = saveFormItem(uri, req.getReader());
                resp.setContentType("text/xml");
                resp.getWriter().println("<formItemId>"+formItemId+"</formItemId>");
                resp.setStatus(HttpServletResponse.SC_CREATED);
            } else if (uri.contains("/formDefinitions/")) {
                String formId = saveForm(uri, req.getReader());
                resp.setContentType("text/xml");
                resp.getWriter().println("<formId>"+formId+"</formId>");
                resp.setStatus(HttpServletResponse.SC_CREATED);
            } else if (uri.contains("/formPreview/")) {
                resp.setContentType("text/html");
                resp.getWriter().println(getFormPreview(uri, req.getReader()));
            } else if (uri.contains("/formTemplate/")) {
                String fileName = getFormTemplate(uri, req.getReader());
                resp.setContentType("text/xml");
                resp.getWriter().println("<fileName>"+fileName+"</fileName>");
            }
        } catch (Exception e) {
            log.error("Problem during FormBuilderServlet:GET", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
        } 
    }

    private String saveFormItem(String uri, BufferedReader reader) throws IOException, FormEncodingException, FormServiceException {
        String json = IOUtils.toString(reader);
        FormRepresentationDecoder decoder = FormEncodingFactory.getDecoder();
        FormItemRepresentation item = decoder.decodeItem(json);
        String pkgName = getUriParameter(uri, "package");
        String formItemName = getUriParameter(uri, "formItemName");
        String formItemId = formService.saveFormItem(pkgName, formItemName, item);
        return formItemId;
    }
    
    private String saveForm(String uri, BufferedReader reader) throws IOException, FormEncodingException, FormServiceException {
        String json = IOUtils.toString(reader);
        FormRepresentationDecoder decoder = FormEncodingFactory.getDecoder();
        FormRepresentation form = decoder.decode(json);
        String formId = formService.saveForm(getUriParameter(uri, "package"), form);
        return formId;
    }
    
    private int saveMenuItem(BufferedReader reader) throws JAXBException {
        try {
            SaveMenuItemDTO dto = jaxbTransformation(reader, SaveMenuItemDTO.class, new Class[] {SaveMenuItemDTO.class});
            MenuItemDescription menuItem = toMenuItemDescription(dto);
            menuService.saveMenuItem(dto.getGroupName(), menuItem);
            return HttpServletResponse.SC_CREATED;
        } catch (MenuServiceException e) {
            return HttpServletResponse.SC_CONFLICT;
        }
    }

    private int deleteMenuItem(BufferedReader reader) throws JAXBException {
        try {
            SaveMenuItemDTO dto = jaxbTransformation(reader, SaveMenuItemDTO.class, new Class<?>[] {SaveMenuItemDTO.class});
            MenuItemDescription menuItem = toMenuItemDescription(dto);
            Map<String, List<MenuItemDescription>> items = menuService.listMenuItems();
            List<MenuItemDescription> group = items.get(dto.getGroupName());
            if (group == null || group.isEmpty()) {
                return HttpServletResponse.SC_NOT_FOUND;
            }
            if (!group.contains(menuItem)) {
                return HttpServletResponse.SC_CONFLICT;
            }
            menuService.deleteMenuItem(dto.getGroupName(), menuItem);
            return HttpServletResponse.SC_ACCEPTED;
        } catch (MenuServiceException e) {
            return HttpServletResponse.SC_CONFLICT;
        }
    }
    
    private MenuItemDescription toMenuItemDescription(SaveMenuItemDTO dto) throws MenuServiceException {
        FormRepresentationDecoder decoder = FormEncodingFactory.getDecoder();
        String json = dto.getClone();
        MenuItemDescription menuItem = new MenuItemDescription();
        try {
            FormItemRepresentation item = decoder.decodeItem(json);
            menuItem.setClassName(CustomMenuItem.class.getName());
            menuItem.setItemRepresentation(item);
            menuItem.setName(dto.getName());
            List<FormEffectDescription> effects = new ArrayList<FormEffectDescription>();
            if (dto.getEffect() != null) {
                for (FormEffectDTO effectDto : dto.getEffect()) {
                    FormEffectDescription effect = new FormEffectDescription();
                    effect.setClassName(effectDto.getClassName());
                    effects.add(effect);
                }
            }
            menuItem.setEffects(effects);
        } catch (FormEncodingException e) {
            throw new MenuServiceException("Couldn't load formRepresentation from dto", e); 
        }
        return menuItem;
    }
    
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String uri = req.getRequestURI();
            if (uri.contains("/menuItems/")) {
                int status = deleteMenuItem(req.getReader());
                resp.setStatus(status);
            } else if (uri.contains("/formDefinitions/")) {
                deleteForm(uri);
            } else if (uri.contains("/formItems/")) {
                deleteFormItem(uri);
            }
        } catch (Exception e) {
            log.error("Problem during FormBuilderServlet:GET", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
        } 
    }
    
    private int deleteFormItem(String uri) throws IOException {
        String pkgName = getUriParameter(uri, "package");
        String formItemName = getUriParameter(uri, "formItemName");
        try {
            formService.deleteFormItem(pkgName, formItemName);
            return HttpServletResponse.SC_OK;
        } catch (FormServiceException e) {
            return HttpServletResponse.SC_NO_CONTENT;
        }
    }
    
    private int deleteForm(String uri) throws IOException {
        String pkgName = getUriParameter(uri, "package");
        String formId =  getUriParameter(uri, "formId");
        try {
            formService.deleteForm(pkgName, formId);
            return HttpServletResponse.SC_OK;
        } catch (FormServiceException e) {
            return HttpServletResponse.SC_NO_CONTENT;
        }
    }
    
    public void setFormService(FormDefinitionService formService) {
        this.formService = formService;
    }
    
    public void setMenuService(MenuService menuService) {
        this.menuService = menuService;
    }
    
    public void setTaskService(TaskDefinitionService taskService) {
        this.taskService = taskService;
    }
}
