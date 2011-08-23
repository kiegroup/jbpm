package org.jbpm.formbuilder.server;

import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.jbpm.formbuilder.server.form.FormEncodingServerFactory;
import org.jbpm.formbuilder.server.task.GuvnorTaskDefinitionService;
import org.jbpm.formbuilder.server.xml.ListTasksDTO;
import org.jbpm.formbuilder.shared.form.FormEncodingFactory;
import org.jbpm.formbuilder.shared.task.TaskDefinitionService;
import org.jbpm.formbuilder.shared.task.TaskRef;
import org.jbpm.formbuilder.shared.task.TaskServiceException;

@Path("/io")
public class RESTIoService {

    private TaskDefinitionService taskService;
    
    public void setContext(@Context ServletContext context) {
        String url = context.getInitParameter("guvnor-base-url");
        String user = context.getInitParameter("guvnor-user");
        String pass = context.getInitParameter("guvnor-password");
        this.taskService = new GuvnorTaskDefinitionService(url, user, pass);
    }
    
    public RESTIoService() {
        FormEncodingFactory.register(FormEncodingServerFactory.getEncoder(), FormEncodingServerFactory.getDecoder());
    }
    
    @GET @Path("/package/{pkgName}")
    public Response getIoAssociations(@QueryParam("q") String filter, @PathParam("pkgName") String pkgName) {
        String[] filters = filter == null ? new String[0] : filter.split(" ");
        String newFilter = filters.length == 0 ? (filter == null ? "" : filter) : "";
        for (String subFilter : filters) {
            if (subFilter.startsWith("iotype:")) {
                //TODO String type = subFilter.replace("iotype:", ""); decide what to do with this filter
            } else {
                newFilter += subFilter + " ";
            }
        }
        ResponseBuilder builder = Response.noContent();
        try {
            List<TaskRef> tasks = taskService.query(pkgName, newFilter);
            ListTasksDTO dto = new ListTasksDTO(tasks);
            builder = Response.ok(dto);
        } catch (TaskServiceException e) {
            builder = Response.serverError();
        }
        return builder.build();
    }
    
    @GET @Path("/package/{pkgName}/process/{procName}/task/{taskName}")
    public Response getIoAssociation(@PathParam("pkgName") String pkgName, 
            @PathParam("procName") String procName, @PathParam("taskName") String taskName) {
        ResponseBuilder builder = Response.noContent();
        try {
            List<TaskRef> tasks = taskService.getTasksByName(pkgName, procName, taskName);
            ListTasksDTO dto = new ListTasksDTO(tasks);
            builder = Response.ok(dto);
        } catch (TaskServiceException e) {
            builder = Response.serverError();
        }
        return builder.build();
    }
}
