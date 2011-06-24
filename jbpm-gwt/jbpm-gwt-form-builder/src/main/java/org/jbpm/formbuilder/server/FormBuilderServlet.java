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

import java.io.IOException; 
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.drools.repository.RulesRepository;
import org.jboss.seam.Component;
import org.jboss.seam.contexts.Contexts;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.options.MainMenuOption;
import org.jbpm.formbuilder.server.form.GuvnorFormDefinitionService;
import org.jbpm.formbuilder.server.menu.GuvnorMenuService;
import org.jbpm.formbuilder.server.task.GuvnorTaskDefinitionService;
import org.jbpm.formbuilder.shared.form.FormDefinitionService;
import org.jbpm.formbuilder.shared.menu.MenuService;
import org.jbpm.formbuilder.shared.task.TaskDefinitionService;
import org.jbpm.formbuilder.shared.task.TaskPropertyRef;
import org.jbpm.formbuilder.shared.task.TaskRef;

public class FormBuilderServlet extends HttpServlet {

    private static final long serialVersionUID = -5961620265453738055L;

    private RulesRepository repo;
    private MenuService menuService;
    private TaskDefinitionService taskService;
    private FormDefinitionService formService;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        this.menuService = new GuvnorMenuService();
        this.taskService = new GuvnorTaskDefinitionService();
        if ( Contexts.isApplicationContextActive() ) {
            this.repo = (RulesRepository) Component.getInstance( "repository" );
        }
        this.formService = new GuvnorFormDefinitionService(this.repo);
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
        String uri = req.getRequestURI();
        StringBuilder xml = new StringBuilder();
        if (uri.contains("menuItems")) {
            xml.append(listMenuItems());
        } else if (uri.contains("menuOptions")) {
            xml.append(listOptions());
        } else if (uri.contains("listTasks")) {
            xml.append(listTasks(extractPackageName(uri, "listTasks"), req.getParameter("q")));
        } else if (uri.contains("listValidations")) {
            //TODO implement
        }
        resp.setContentType("text/xml");
        resp.getWriter().println(xml.toString());
    }

    private String listTasks(String filter, String pkgName) {
        List<TaskRef> tasks = taskService.query(pkgName, filter);
        StringBuilder builder = new StringBuilder();
        builder.append("<tasks>\n");
        for (TaskRef task : tasks) {
            builder.append("<task ");
            builder.append("processId=\"").append(task.getProcessId()).append("\" ");
            builder.append("taskName=\"").append(task.getTaskName()).append("\" ");
            builder.append("taskId=\"").append(task.getTaskId()).append("\">\n");
            for (TaskPropertyRef input : task.getInputs()) {
                builder.append("<input ");
                builder.append("name=\"").append(input.getName()).append("\" ");
                builder.append("source=\"").append(input.getSourceExpresion()).append("\"/>\n");
            }
            for (TaskPropertyRef output : task.getOutputs()) {
                builder.append("<output ");
                builder.append("name=\"").append(output.getName()).append("\" ");
                builder.append("source=\"").append(output.getSourceExpresion()).append("\"/>\n");
            }
            for (Map.Entry<String, String> metaData : task.getMetaData().entrySet()) {
                builder.append("<metaData ");
                builder.append("key=\"").append(metaData.getKey()).append("\" ");
                builder.append("value=\"").append(metaData.getValue()).append("\"/>\n");
            }
            builder.append("</task>\n");
        }
        builder.append("</tasks>\n");
        return builder.toString();
    }
    
    private String listMenuItems() {
        StringBuilder builder = new StringBuilder();
        Map<String, List<FBMenuItem>> items = menuService.listItems();
        builder.append("<menuGroups>\n");
        for (Map.Entry<String, List<FBMenuItem>> item : items.entrySet()) {
            builder.append("<menuGroup ");
            builder.append("name=\"").append(item.getKey()).append("\">\n");
            List<FBMenuItem> groupItems = item.getValue();
            for (FBMenuItem menuItem : groupItems) {
                builder.append("<menuItem ");
                builder.append("className=\"").append(menuItem.getClass().getName()).append("\">\n");
                List<FBFormEffect> effects = menuItem.getFormEffects();
                for (FBFormEffect effect : effects) {
                    builder.append("<effect ");
                    builder.append("className=\"").append(effect.getClass().getName()).append("\"/>\n");
                }
                builder.append("</menuItem>\n");
            }
            builder.append("</menuGroup>\n");
        }
        builder.append("</menuGroups>\n");
        return builder.toString();
    }

    private String listOptions() {
        List<MainMenuOption> options = menuService.listOptions();
        StringBuilder builder = new StringBuilder();
        builder.append("<menuOptions>\n");
        for (MainMenuOption option : options) {
            builder.append(optionToXml(option));
        }
        builder.append("</menuOptions>\n");
        return builder.toString();
    }

    private String optionToXml(MainMenuOption option) {
        StringBuilder builder = new StringBuilder();
        builder.append("<menuOption ");
        builder.append("name=\"").append(option.getHtml()).append("\" ");
        if (option.getCommand() != null) {
            builder.append("commandClass=\"").append(option.getCommand().getClass().getName()).append("\" ");
        }
        if (option.getSubMenu() != null && !option.getSubMenu().isEmpty()) {
            builder.append(">\n");
            for (MainMenuOption subOption : option.getSubMenu()) {
                builder.append(optionToXml(subOption));
            }
            builder.append("</menuOption>\n");
        } else {
            builder.append("/>\n");
        }
        return builder.toString();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        // TODO Auto-generated method stub
    }
    
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        // TODO Auto-generated method stub
    }
    
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        // TODO Auto-generated method stub
    }
}
