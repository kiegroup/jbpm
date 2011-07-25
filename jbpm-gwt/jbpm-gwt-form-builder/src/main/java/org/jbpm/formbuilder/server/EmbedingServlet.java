package org.jbpm.formbuilder.server;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jbpm.formbuilder.server.form.FormEncodingServerFactory;
import org.jbpm.formbuilder.server.task.GuvnorTaskDefinitionService;
import org.jbpm.formbuilder.shared.form.FormEncodingFactory;
import org.jbpm.formbuilder.shared.task.TaskDefinitionService;
import org.jbpm.formbuilder.shared.task.TaskRef;
import org.jbpm.formbuilder.shared.task.TaskServiceException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class EmbedingServlet extends HttpServlet {

    private static final long serialVersionUID = -5943196576708424978L;

    private String guvnorBaseUrl = null;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        FormEncodingFactory.register(FormEncodingServerFactory.getEncoder(), FormEncodingServerFactory.getDecoder());
        this.guvnorBaseUrl = config.getInitParameter("guvnor-base-url");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String uuid = request.getParameter("uuid");
        String userTask = request.getParameter("userTask");
        String usr = request.getParameter("usr");
        String pwd = request.getParameter("pwd");
        try {
            TaskDefinitionService taskService = new GuvnorTaskDefinitionService(this.guvnorBaseUrl, usr, pwd);
            TaskRef task = taskService.getByUUID(userTask, uuid);
            JsonObject json = new JsonObject();
            json.addProperty("uuid", uuid);
            json.addProperty("userTask", userTask);
            //TODO implement transformation to JSON here
            request.setAttribute("uuid", new Gson().toJson(json));
            request.getRequestDispatcher("/FormBuilder.jsp").forward(request, response);
        } catch (TaskServiceException e) {
            //TODO implement error handling here
        }
    }
}
