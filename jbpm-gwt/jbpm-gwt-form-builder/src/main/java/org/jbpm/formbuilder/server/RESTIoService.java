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

import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    public Response getIoAssociations(@QueryParam("q") String filter, @PathParam("pkgName") String pkgName, @Context ServletContext context) {
        setContext(context);
        String[] filters = filter == null ? new String[0] : filter.split(" ");
        String newFilter = filters.length == 0 ? (filter == null ? "" : filter) : "";
        for (String subFilter : filters) {
            if (subFilter.startsWith("iotype:")) {
                //TODO String type = subFilter.replace("iotype:", ""); decide what to do with this filter
            } else {
                newFilter += subFilter + " ";
            }
        }
        try {
            List<TaskRef> tasks = taskService.query(pkgName, newFilter);
            ListTasksDTO dto = new ListTasksDTO(tasks);
            return Response.ok(dto, MediaType.APPLICATION_XML).build();
        } catch (TaskServiceException e) {
            return error(e);
        }
    }
    
    @GET @Path("/package/{pkgName}/process/{procName}/task/{taskName}")
    public Response getIoAssociation(@PathParam("pkgName") String pkgName, 
            @PathParam("procName") String procName, @PathParam("taskName") String taskName,
            @Context ServletContext context) {
        setContext(context);
        try {
            List<TaskRef> tasks = taskService.getTasksByName(pkgName, procName, taskName);
            ListTasksDTO dto = new ListTasksDTO(tasks);
            return Response.ok(dto, MediaType.APPLICATION_XML).build();
        } catch (TaskServiceException e) {
            return error(e);
        }
    }

    private static final Log log = LogFactory.getLog(RESTIoService.class);

    Response error(Exception e) {
        log.error("Error on REST service: ", e);
        return Response.serverError().build();
    }
}
