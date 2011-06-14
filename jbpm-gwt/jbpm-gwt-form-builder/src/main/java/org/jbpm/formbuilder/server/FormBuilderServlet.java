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
import javax.servlet.ServletContext;
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
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

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
        Document document = new Document();
        if (uri.contains("menuItems")) {
            listMenuItems(document);
        } else if (uri.contains("menuOptions")) {
            listOptions(document);
        } else if (uri.contains("listTasks")) {
            listTasks(document, extractPackageName(uri, "listTasks"), req.getParameter("q"));
        } else if (uri.contains(""))
        new XMLOutputter().output(document, resp.getOutputStream());
    }

    private void listTasks(Document document, String filter, String pkgName) {
        List<TaskRef> tasks = taskService.query(pkgName, filter);
        Element tasksElement = new Element("tasks");
        document.setRootElement(tasksElement);
        for (TaskRef task : tasks) {
            Element eTask = new Element("task");
            eTask.setAttribute("processId", task.getProcessId());
            eTask.setAttribute("taskName", task.getTaskName());
            eTask.setAttribute("taskId", task.getTaskId());
            for (TaskPropertyRef input : task.getInputs()) {
                Element eInput = new Element("input");
                eInput.setAttribute("name", input.getName());
                eInput.setAttribute("source", input.getSourceExpresion());
                eTask.addContent(eInput);
            }
            for (TaskPropertyRef output : task.getOutputs()) {
                Element eOutput = new Element("output");
                eOutput.setAttribute("name", output.getName());
                eOutput.setAttribute("source", output.getSourceExpresion());
                eTask.addContent(eOutput);
            }
            for (Map.Entry<String, String> metaData : task.getMetaData().entrySet()) {
                Element eMetaData = new Element("metaData");
                eMetaData.setAttribute("key", metaData.getKey());
                eMetaData.setAttribute("value", metaData.getValue());
            }
            tasksElement.addContent(eTask);
        }
    }
    
    private void listMenuItems(Document document) {
        Map<String, List<FBMenuItem>> items = menuService.listItems();
        Element itemsElement = new Element("menuGroups");
        document.setRootElement(itemsElement);
        for (Map.Entry<String, List<FBMenuItem>> item : items.entrySet()) {
            Element group = new Element("menuGroup");
            group.setAttribute("name", item.getKey());
            List<FBMenuItem> groupItems = item.getValue();
            for (FBMenuItem menuItem : groupItems) {
                Element mItem = new Element("menuItem");
                mItem.setAttribute("className", menuItem.getClass().getName());
                List<FBFormEffect> effects = menuItem.getFormEffects();
                for (FBFormEffect effect : effects) {
                    Element eff = new Element("effect");
                    eff.setAttribute("className", effect.getClass().getName());
                    mItem.addContent(eff);
                }
                group.addContent(mItem);
            }
            itemsElement.addContent(group);
        }
    }

    private void listOptions(Document document) {
        List<MainMenuOption> options = menuService.listOptions();
        Element optionsElement = new Element("menuOptions");
        document.setRootElement(optionsElement);
        for (MainMenuOption option : options) {
            optionsElement.addContent(optionToXml(option));
        }
    }

    private Element optionToXml(MainMenuOption option) {
        Element eOption = new Element("menuOption");
        eOption.setAttribute("name", option.getHtml());
        if (option.getSubMenu() != null && !option.getSubMenu().isEmpty()) {
            for (MainMenuOption subOption : option.getSubMenu()) {
                eOption.addContent(optionToXml(subOption));
            }
        } 
        if (option.getCommand() != null) {
            eOption.setAttribute("commandClass", option.getCommand().getClass().getName());
        }
        return eOption;
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
