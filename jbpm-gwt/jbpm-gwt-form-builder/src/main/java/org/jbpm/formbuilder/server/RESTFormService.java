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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
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
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.FileUtils;
import org.jboss.resteasy.annotations.providers.jaxb.DoNotUseJAXBProvider;
import org.jbpm.formbuilder.server.form.FormEncodingServerFactory;
import org.jbpm.formbuilder.server.form.GuvnorFormDefinitionService;
import org.jbpm.formbuilder.server.form.ListFormsDTO;
import org.jbpm.formbuilder.server.form.ListFormsItemsDTO;
import org.jbpm.formbuilder.server.render.Renderer;
import org.jbpm.formbuilder.server.render.RendererException;
import org.jbpm.formbuilder.server.render.RendererFactory;
import org.jbpm.formbuilder.server.trans.LanguageException;
import org.jbpm.formbuilder.server.trans.Translator;
import org.jbpm.formbuilder.server.trans.TranslatorFactory;
import org.jbpm.formbuilder.server.xml.FormPreviewDTO;
import org.jbpm.formbuilder.shared.form.FormDefinitionService;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormEncodingFactory;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.form.FormServiceException;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

@Path("/form")
public class RESTFormService {

    private FormDefinitionService formService;
    
    public void setContext(@Context ServletContext context) {
        String baseUrl = context.getInitParameter("guvnor-base-url");
        String user = context.getInitParameter("guvnor-user");
        String pass = context.getInitParameter("guvnor-password");
        this.formService = new GuvnorFormDefinitionService(baseUrl, user, pass);
    }
    
    public RESTFormService() {
        FormEncodingFactory.register(FormEncodingServerFactory.getEncoder(), FormEncodingServerFactory.getDecoder());
    }
    
    @GET @Path("/definitions/package/{pkgName}")
    public Response getForms(@PathParam("pkgName") String pkgName, @Context ServletContext context) {
        setContext(context);
        ResponseBuilder builder = Response.noContent();
        try {
            List<FormRepresentation> forms = formService.getForms(pkgName);
            ListFormsDTO dto = new ListFormsDTO(forms);
            builder = Response.ok(dto, MediaType.APPLICATION_XML);
        } catch (FormServiceException e) {
            builder = Response.serverError();
        } catch (FormEncodingException e) {
            builder = Response.serverError();
        }
        return builder.build();
    }
    
    @GET @Path("/definitions/package/{pkgName}/id/{formId}")
    public Response getForm(@PathParam("pkgName") String pkgName, @PathParam("formId") String formId, @Context ServletContext context) {
        setContext(context);
        ResponseBuilder builder = Response.noContent();
        try {
            FormRepresentation form = formService.getForm(pkgName, formId);
            ListFormsDTO dto = new ListFormsDTO(form);
            builder = Response.ok(dto, MediaType.APPLICATION_XML);
        } catch (FormServiceException e) {
            builder = Response.serverError();
        } catch (FormEncodingException e) {
            builder = Response.serverError();
        }
        return builder.build();
    }
    
    @POST @Path("/definitions/package/{pkgName}")
    @Consumes("text/plain")
    @DoNotUseJAXBProvider
    public Response saveForm(String jsonBody, @PathParam("pkgName") String pkgName, @Context ServletContext context) {
        setContext(context);
        FormRepresentationDecoder decoder = FormEncodingFactory.getDecoder();
        try {
            FormRepresentation form = decoder.decode(jsonBody);
            String formId = formService.saveForm(pkgName, form);
            return Response.ok("<formId>"+formId+"</formId>", MediaType.APPLICATION_XML).
                status(Status.CREATED).build();
        } catch (FormEncodingException e) {
            return Response.serverError().build();
        } catch (FormServiceException e) {
            return Response.serverError().build();
        }
    }
    
    @DELETE @Path("/definitions/package/{pkgName}/id/{formId}") 
    public Response deleteForm(@PathParam("pkgName") String pkgName, @PathParam("formId") String formId, @Context ServletContext context) {
        setContext(context);
        try {
            formService.deleteForm(pkgName, formId);
            return Response.ok().build();
        } catch (FormServiceException e) {
            return Response.noContent().build();
        }
    }

    @GET @Path("/items/package/{pkgName}")
    public Response getFormItems(@PathParam("pkgName") String pkgName, @Context ServletContext context) {
        setContext(context);
        ResponseBuilder builder = Response.noContent();
        try {
            Map<String, FormItemRepresentation> formItems = formService.getFormItems(pkgName);
            ListFormsItemsDTO dto = new ListFormsItemsDTO(formItems);
            builder = Response.ok(dto, MediaType.APPLICATION_XML);
        } catch (FormServiceException e) {
            builder = Response.serverError();
        } catch (FormEncodingException e) {
            builder = Response.serverError();
        }
        return builder.build();
    }
    
    @GET @Path("/items/package/{pkgName}/id/{fItemId}") 
    public Response getFormItem(@PathParam("pkgName") String pkgName, @PathParam("fItemId") String formItemId, @Context ServletContext context) {
        setContext(context);
        ResponseBuilder builder = Response.noContent();
        try {
            FormItemRepresentation formItem = formService.getFormItem(pkgName, formItemId);
            ListFormsItemsDTO dto = new ListFormsItemsDTO(formItemId, formItem);
            builder = Response.ok(dto, MediaType.APPLICATION_XML);
        } catch (FormServiceException e) {
            builder = Response.serverError();
        } catch (FormEncodingException e) {
            builder = Response.serverError();
        }
        return builder.build();
    }
    
    @POST @Path("/items/package/{pkgName}/name/{fItemName}")
    @Consumes("text/plain")
    @DoNotUseJAXBProvider
    public Response saveFormItem(String jsonBody,
            @PathParam("pkgName") String pkgName, 
            @PathParam("fItemName") String formItemName, @Context ServletContext context) {
        setContext(context);
        FormRepresentationDecoder decoder = FormEncodingFactory.getDecoder();
        try {
            FormItemRepresentation item = decoder.decodeItem(jsonBody);
            String formItemId = formService.saveFormItem(pkgName, formItemName, item);
            return Response.ok("<formItemId>"+formItemId+"</formItemId>", 
                    MediaType.APPLICATION_XML).status(Status.CREATED).build();
        } catch (FormEncodingException e) {
            return Response.serverError().build();
        } catch (FormServiceException e) {
            return Response.serverError().build();
        }
    }

    @DELETE @Path("/items/package/{pkgName}/name/{fItemName}")
    public Response deleteFormItem(@PathParam("pkgName")String pkgName, @PathParam("fItemName") String formItemName, @Context ServletContext context) {
        setContext(context);
        try {
            formService.deleteFormItem(pkgName, formItemName);
            return Response.ok().build();
        } catch (FormServiceException e) {
            return Response.noContent().build();
        }
    }
    
    @POST @Path("/preview/lang/{language}")
    public Response getFormPreview(FormPreviewDTO dto, @PathParam("language") String language, @Context ServletContext context) {
        setContext(context);
        try {
            URL url = createTemplate(language, dto);
            Map<String, Object> inputs = dto.getInputsAsMap();
            Renderer renderer = RendererFactory.getInstance().getRenderer(language);
            Object html = renderer.render(url, inputs);
            return Response.ok(html, MediaType.TEXT_HTML).build();
        } catch (FormEncodingException e) {
            return Response.serverError().build();
        } catch (LanguageException e) {
            return Response.serverError().build();
        } catch (RendererException e) {
            return Response.serverError().build();
        }
    }
    
    @POST @Path("/template/lang/{language}")
    public Response getFormTemplate(FormPreviewDTO dto, @PathParam("language") String language, @Context ServletContext context) {
        setContext(context);
        try {
            URL url = createTemplate(language, dto);
            String fileName = url.getFile();
            return Response.ok("<fileName>"+fileName+"</fileName>", MediaType.APPLICATION_XML).build();
        } catch (FormEncodingException e) {
            return Response.serverError().build();
        } catch (LanguageException e) {
            return Response.serverError().build();
        }
    }

    private URL createTemplate(String language, FormPreviewDTO dto) throws FormEncodingException, LanguageException {
        FormRepresentationDecoder decoder = FormEncodingFactory.getDecoder();
        String json = dto.getRepresentation();
        FormRepresentation form = decoder.decode(json);
        dto.setForm(form);
        Translator translator = TranslatorFactory.getInstance().getTranslator(language);
        URL url = translator.translateForm(form);
        return url;
    }
    
    @GET @Path("/template/lang/{language}")
    public Response getExportTemplate(@QueryParam("fileName") String fileName,
            @QueryParam("formName") String formName,
            @PathParam("language") String language, @Context ServletContext context) {
        setContext(context);
        File file = new File(fileName);
        String headerValue = new StringBuilder("attachment; filename=\"").
            append(formName).append('.').append(language).
            append("\"").toString();
        try {
            return Response.ok(FileUtils.readFileToByteArray(file), 
                MediaType.APPLICATION_OCTET_STREAM).
                header("Content-Disposition", headerValue).build();
        } catch (IOException e) {
            return Response.serverError().build();
        }
    }
}
