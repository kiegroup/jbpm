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

import org.apache.commons.io.IOUtils;
import org.jbpm.formbuilder.client.menu.items.CustomOptionMenuItem;
import org.jbpm.formbuilder.server.form.FormEncodingServerFactory;
import org.jbpm.formbuilder.server.form.GuvnorFormDefinitionService;
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
import org.jbpm.formbuilder.server.xml.ListMenuItemsDTO;
import org.jbpm.formbuilder.server.xml.ListOptionsDTO;
import org.jbpm.formbuilder.server.xml.ListTasksDTO;
import org.jbpm.formbuilder.server.xml.MenuGroupDTO;
import org.jbpm.formbuilder.server.xml.MenuItemDTO;
import org.jbpm.formbuilder.server.xml.MenuOptionDTO;
import org.jbpm.formbuilder.server.xml.MetaDataDTO;
import org.jbpm.formbuilder.server.xml.PropertyDTO;
import org.jbpm.formbuilder.server.xml.TaskRefDTO;
import org.jbpm.formbuilder.shared.form.FormDefinitionService;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.form.FormServiceException;
import org.jbpm.formbuilder.shared.menu.FormEffectDescription;
import org.jbpm.formbuilder.shared.menu.MenuItemDescription;
import org.jbpm.formbuilder.shared.menu.MenuOptionDescription;
import org.jbpm.formbuilder.shared.menu.MenuService;
import org.jbpm.formbuilder.shared.menu.MenuServiceException;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.task.TaskDefinitionService;
import org.jbpm.formbuilder.shared.task.TaskRef;

public class FormBuilderServlet extends HttpServlet {

    private static final long serialVersionUID = -5961620265453738055L;

    MenuService menuService;
    TaskDefinitionService taskService;
    FormDefinitionService formService;

    private Map<Class<?>[], Marshaller> marshallers = new HashMap<Class<?>[], Marshaller>();
    private Map<Class<?>[], Unmarshaller> unmarshallers = new HashMap<Class<?>[], Unmarshaller>();
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        this.menuService = new GuvnorMenuService();
        this.taskService = new GuvnorTaskDefinitionService();
        this.formService = new GuvnorFormDefinitionService(
                config.getInitParameter("guvnor-base-url"),
                config.getInitParameter("guvnor-user"),
                config.getInitParameter("guvnor-password")
        );
    }
    
    /*
     * all URLs are supposed to be "/<contextPath>/<pkgName>/<operation>"
     */
    private String extractPackageName(String uri, String operation) {
        String[] folders = uri.split("/");
        for (int index = 0; index < folders.length; index++) {
            if (folders[index].equals(operation)) {
                return folders[index - 1];
            }
        }
        return null;
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
            } else if (uri.contains("/listTasks/")) {
                resp.setContentType("text/xml");
                content.append(listTasks(extractPackageName(uri, "listTasks"), req.getParameter("q")));
            } else if (uri.contains("/listValidations/")) {
                resp.setContentType("text/xml");
                //TODO implement listValidations
            } else if (uri.contains("/formPreview/")) {
                resp.setContentType("text/html");
                content.append(getFormPreview(req.getRequestURI()));
            }
            resp.getWriter().println(content.toString());
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
        } 
    }
    
    private String getUriParameter(String requestUri, String paramName) {
        paramName = "/" + paramName + "/";
        int start = requestUri.indexOf(paramName) + paramName.length();
        int end = requestUri.indexOf("/", start + 1);
        if (end == -1) {
            end = requestUri.length();
        }
        return requestUri.substring(start, end);
    }
    
    private String getFormPreview(String requestUri) throws LanguageException, RendererException, FormServiceException {
        StringBuilder builder = new StringBuilder();
        String formId = getUriParameter(requestUri, "formPreview");
        String language = getUriParameter(requestUri, "lang");
        String pkgName = getUriParameter(requestUri, "package");
        LanguageFactory factory = LanguageFactory.getInstance();
        Language translator = factory.getLanguage(language);
        FormRepresentation form = formService.getForm(pkgName, formId);
        URL url = translator.translateForm(form);
        RendererFactory factory2 = RendererFactory.getInstance();
        Renderer renderer = factory2.getRenderer(language);
        builder.append(renderer.render(url, new HashMap<String, Object>()));
        return builder.toString();
        
    }

    private String listTasks(String filter, String pkgName) throws JAXBException {
        List<TaskRef> tasks = taskService.query(pkgName, filter); //TODO check implementation
        ListTasksDTO dto = new ListTasksDTO(tasks);
        return jaxbTransformation(dto, ListTasksDTO.class, TaskRefDTO.class, PropertyDTO.class, MetaDataDTO.class);
    }
    
    private String listMenuItems() throws JAXBException, MenuServiceException {
        Map<String, List<MenuItemDescription>> items = menuService.listMenuItems();
        ListMenuItemsDTO dto = new ListMenuItemsDTO(items);
        return jaxbTransformation(dto, ListMenuItemsDTO.class, MenuGroupDTO.class, MenuItemDTO.class, FormEffectDTO.class);
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
                resp.getWriter().println(formItemId);
                resp.setStatus(HttpServletResponse.SC_CREATED);
            } else if (uri.contains("/forms/")) {
                String formId = saveForm(uri, req.getReader());
                resp.getWriter().println(formId);
                resp.setStatus(HttpServletResponse.SC_CREATED);
            }
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
        } 
    }

    private String saveFormItem(String uri, BufferedReader reader) throws IOException, FormEncodingException, FormServiceException {
        String json = IOUtils.toString(reader);
        FormRepresentationDecoder decoder = FormEncodingServerFactory.getDecoder();
        FormItemRepresentation item = decoder.decodeItem(json);
        String pkgName = getUriParameter(uri, "package");
        String formItemName = getUriParameter(uri, "formItemName");
        String formItemId = formService.saveFormItem(pkgName, formItemName, item);
        return formItemId;
    }
    
    private String saveForm(String uri, BufferedReader reader) throws IOException, FormEncodingException, FormServiceException {
        String json = IOUtils.toString(reader);
        FormRepresentationDecoder decoder = FormEncodingServerFactory.getDecoder();
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
        FormRepresentationDecoder decoder = FormEncodingServerFactory.getDecoder();
        String json = dto.getClone();
        MenuItemDescription menuItem = new MenuItemDescription();
        try {
            FormItemRepresentation item = decoder.decodeItem(json);
            menuItem.setClassName(CustomOptionMenuItem.class.getName());
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
            } else if (uri.contains("/forms/")) {
                deleteForm(uri);
            } else if (uri.contains("/formItems/")) {
                deleteFormItem(uri);
            }
        } catch (Exception e) {
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
