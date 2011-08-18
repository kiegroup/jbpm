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
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.jbpm.formbuilder.server.form.FormEncodingServerFactory;
import org.jbpm.formbuilder.server.form.GuvnorFormDefinitionService;
import org.jbpm.formbuilder.server.task.GuvnorTaskDefinitionService;
import org.jbpm.formbuilder.server.task.ProcessGetInputHandler;
import org.jbpm.formbuilder.server.trans.Translator;
import org.jbpm.formbuilder.server.trans.TranslatorFactory;
import org.jbpm.formbuilder.shared.form.FormDefinitionService;
import org.jbpm.formbuilder.shared.form.FormEncodingFactory;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.task.TaskDefinitionService;

public class ExportTemplateServlet extends HttpServlet {

    private static final long serialVersionUID = -7653438101539099368L;

    private String guvnorBaseUrl = null;
    private String user = null;
    private String pass = null;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        FormEncodingFactory.register(FormEncodingServerFactory.getEncoder(), FormEncodingServerFactory.getDecoder());
        this.guvnorBaseUrl = config.getServletContext().getInitParameter("guvnor-base-url");
        this.user = config.getServletContext().getInitParameter("guvnor-user");
        this.pass = config.getServletContext().getInitParameter("guvnor-password");
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String profile = req.getParameter("profile");
        try {
            if (notEmpty(profile) && "jbpm".equals(profile)) {
                String uuid = req.getParameter("uuid");
                TaskDefinitionService taskService = new GuvnorTaskDefinitionService(this.guvnorBaseUrl, this.user, this.pass);
                FormDefinitionService formService = new GuvnorFormDefinitionService(this.guvnorBaseUrl, this.user, this.pass);
                String packageName = taskService.getContainingPackage(uuid);
                FormRepresentation form = formService.getFormByUUID(packageName, uuid);
                if (notEmpty(form.getProcessName()) || notEmpty(form.getTaskId())) {
                    Translator translator = TranslatorFactory.getInstance().getTranslator("ftl");
                    URL url = translator.translateForm(form);
                    String content = IOUtils.toString(url.openStream());
                    String templateName = "";
                    if (!notEmpty(form.getTaskId()) || ProcessGetInputHandler.PROCESS_INPUT_NAME.equals(form.getTaskId())) {
                        templateName = form.getProcessName();
                    } else {
                        templateName = form.getTaskId();
                    }
                    if (templateName != null) {
                        templateName += "-taskform.ftl";
                        formService.saveTemplate(packageName, templateName, content);
                    }
                }
            } else {
                throw new Exception("Profile not available for " + profile);
            }
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
        }
    }

    private boolean notEmpty(String value) {
        return value != null && !"".equals(value) && !"null".equals(value);
    }
}
