/*
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

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.annotations.providers.jaxb.DoNotUseJAXBProvider;

@Path("/files")
public class RESTFileService extends RESTBaseService {

    private GuvnorFileService fileService = null;
    
    private void setContext(ServletContext context) {
        String url = context.getInitParameter("guvnor-base-url");
        String user = context.getInitParameter("guvnor-user");
        String pass = context.getInitParameter("guvnor-password");
        this.fileService = new GuvnorFileService(url, user, pass);
    }
    
    @POST @Path("/package/{pkgName}")
    @Consumes("*/*")
    @DoNotUseJAXBProvider
    public Response saveFile(@PathParam("pkgName") String packageName, @Context HttpServletRequest request) {
        setContext(request.getSession().getServletContext());
        if (ServletFileUpload.isMultipartContent(request)) {
            //read multipart request and populate request accordingly for display
            int maxMemorySize = 240000;
            File tmpDirectory = new File(System.getProperty("java.io.tmpdir"));
            DiskFileItemFactory factory = new DiskFileItemFactory(maxMemorySize, tmpDirectory);
            ServletFileUpload upload = new ServletFileUpload(factory);
            try {
                List<?> files = upload.parseRequest(request);
                assert (files != null && !files.isEmpty()) : "there should be one file at least";
                FileItem item = (FileItem) files.iterator().next();
                byte[] content = IOUtils.toByteArray(item.getInputStream());
                String fileName = item.getName();
                String expositionUrl = fileService.storeFile(packageName, fileName, content);
                return Response.ok(expositionUrl, MediaType.TEXT_PLAIN).build();
            } catch (GuvnorFileException e) {
                return error("Problem storing file to guvnor", e);
            } catch (IOException e) {
                return error("Problem reading input of file", e);
            } catch (FileUploadException e) {
                return error("Problem reading upload of file", e);
            }
        } else {
            return error("Must be a multipart form data post", null);
        }
    }
    
    @DELETE @Path("/package/{pkgName}/{fileName}")
    public Response deleteFile(@Context HttpServletRequest request, @PathParam("pkgName") String packageName, @PathParam("fileName") String fileName) {
        setContext(request.getSession().getServletContext());
        try {
            fileService.deleteFile(packageName, fileName);
            return Response.noContent().build();
        } catch (GuvnorFileException e) {
            return error("Problem deleting file in guvnor", e);
        }
    }

    @GET @Path("/package/{pkgName}/")
    public Response getFiles(@Context HttpServletRequest request, @PathParam("pkgName") String packageName, @QueryParam("type") String fileType) {
        setContext(request.getSession().getServletContext());
        try {
            List<String> files = fileService.loadFilesByType(packageName, fileType);
            FileListDTO dto = new FileListDTO(files);
            return Response.ok(dto, MediaType.APPLICATION_XML).build();
        } catch (GuvnorFileException e) {
            return error("Problem loading file names", e);
        }
    }
    
    @GET @Path("/package/{pkgName}/{fileName}")
    public Response getFile(@Context HttpServletRequest request, @PathParam("pkgName") String packageName, @PathParam("fileName") String fileName) {
        setContext(request.getSession().getServletContext());
        try {
            byte[] content = fileService.loadFile(packageName, fileName);
            return Response.ok(content, MediaType.APPLICATION_OCTET_STREAM).build();
        } catch (GuvnorFileException e) {
            return error("Problem loading file " + fileName, e);
        }
    }
}
