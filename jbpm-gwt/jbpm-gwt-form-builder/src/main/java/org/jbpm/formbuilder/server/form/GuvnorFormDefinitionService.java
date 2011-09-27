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
package org.jbpm.formbuilder.server.form;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;
import org.jbpm.formbuilder.server.GuvnorHelper;
import org.jbpm.formbuilder.server.task.AssetDTO;
import org.jbpm.formbuilder.server.task.MetaDataDTO;
import org.jbpm.formbuilder.server.task.PackageDTO;
import org.jbpm.formbuilder.server.task.PackageListDTO;
import org.jbpm.formbuilder.shared.api.FormItemRepresentation;
import org.jbpm.formbuilder.shared.api.FormRepresentation;
import org.jbpm.formbuilder.shared.form.AbstractBaseFormDefinitionService;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormEncodingFactory;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.form.FormRepresentationEncoder;
import org.jbpm.formbuilder.shared.form.FormServiceException;
import org.jbpm.formbuilder.shared.task.TaskRef;

public class GuvnorFormDefinitionService extends AbstractBaseFormDefinitionService {

    private GuvnorHelper helper;
    
    public GuvnorFormDefinitionService(String baseUrl, String user, String password) {
        super();
        this.helper = new GuvnorHelper(baseUrl, user, password);
    }

    public void setHelper(GuvnorHelper helper) {
        this.helper = helper;
    }
    
    public GuvnorHelper getHelper() {
        return helper;
    }
    
    @Override
    public String saveForm(String pkgName, FormRepresentation form) throws FormServiceException {
        HttpClient client = helper.getHttpClient();
        EntityEnclosingMethod method = null;
        String url = helper.getApiSearchUrl(pkgName);
        boolean isUpdate = getForm(pkgName, form.getName()) != null;
        String finalUrl = url + form.getName() + ".formdef";
        method = isUpdate ? helper.createPutMethod(finalUrl) : helper.createPostMethod(finalUrl); 
        FormRepresentationEncoder encoder = FormEncodingFactory.getEncoder();
        try {
            method.setRequestEntity(new StringRequestEntity(encoder.encode(form), null, null));
            method.setRequestHeader("Checkin-Comment", form.getDocumentation());
            method.setRequestHeader("Authorization", helper.getAuth());
            client.executeMethod(method);
            if (!"OK".equalsIgnoreCase(method.getResponseBodyAsString())) {
                throw new FormServiceException("Remote guvnor error: " + method.getResponseBodyAsString());
            }
            return form.getName();
        } catch (IOException e) {
            throw new FormServiceException(e);
        } catch (FormEncodingException e) {
            throw new FormServiceException(e);
        } catch (Exception e) {
            if (e instanceof FormServiceException) {
                throw (FormServiceException) e;
            }
            throw new FormServiceException("Unexpected error", e);
        } finally {
            method.releaseConnection();
        }
    }

    @Override
    public String saveFormItem(String pkgName, String formItemName, FormItemRepresentation formItem) throws FormServiceException {
        HttpClient client = helper.getHttpClient();
        String url = helper.getApiSearchUrl(pkgName);
        StringBuilder builder = new StringBuilder();
        boolean isUpdate = updateItemName(formItemName, builder);
        String finalUrl = url + builder.toString() + ".json";
        EntityEnclosingMethod method = isUpdate ? helper.createPutMethod(finalUrl) : helper.createPostMethod(finalUrl);
        FormRepresentationEncoder encoder = FormEncodingFactory.getEncoder();
        try {
            method.setRequestEntity(new StringRequestEntity(encoder.encode(formItem), null, null));
            method.setRequestHeader("Checkin-Comment", "Committing " + formItemName);
            method.setRequestHeader("Authorization", helper.getAuth());
            client.executeMethod(method);
            return formItemName;
        } catch (IOException e) {
            throw new FormServiceException(e);
        } catch (FormEncodingException e) {
            throw new FormServiceException(e);
        } catch (Exception e) {
            throw new FormServiceException("Unexpected error", e);
        } finally {
            method.releaseConnection();
        }
    }

    @Override
    public FormRepresentation getForm(String pkgName, String formId) throws FormServiceException {
        HttpClient client = helper.getHttpClient();
        if (formId != null && !"".equals(formId)) {
            GetMethod method = helper.createGetMethod(helper.getApiSearchUrl(pkgName) + formId + ".formdef");
            FormRepresentationDecoder decoder = FormEncodingFactory.getDecoder();
            try {
                method.setRequestHeader("Authorization", helper.getAuth());
                client.executeMethod(method);
                String json = method.getResponseBodyAsString();
                return decoder.decode(json);
            } catch (IOException e) {
                throw new FormServiceException(e);
            } catch (FormEncodingException e) {
                throw new FormServiceException(e);
            } catch (Exception e) {
                throw new FormServiceException("Unexpected error", e);
            } finally {
                method.releaseConnection();
            }
        }
        return null;
    }

    @Override
    public FormRepresentation getFormByUUID(String packageName, String uuid) throws FormServiceException {
        HttpClient client = helper.getHttpClient();
        if (packageName != null && !"".equals(packageName)) {
            GetMethod call = helper.createGetMethod(helper.getRestBaseUrl());
            try {
                String auth = helper.getAuth();
                call.addRequestHeader("Accept", "application/xml");
                call.addRequestHeader("Authorization", auth);
                client.executeMethod(call);
                PackageListDTO dto = helper.jaxbTransformation(PackageListDTO.class, call.getResponseBodyAsStream(), PackageListDTO.class, PackageDTO.class);
                String formDefUrl = null;
                String format = null;
                PackageDTO pkg = dto.getSelectedPackage(packageName);
                for (String url : pkg.getAssets()) {
                    GetMethod subCall = helper.createGetMethod(url);
                    try {
                        subCall.setRequestHeader("Authorization", auth);
                        subCall.addRequestHeader("Accept", "application/xml");
                        client.executeMethod(subCall);
                        AssetDTO subDto = helper.jaxbTransformation(AssetDTO.class, subCall.getResponseBodyAsStream(), AssetDTO.class, MetaDataDTO.class);
                        if (subDto.getMetadata().getUuid().equals(uuid)) {
                            formDefUrl = subDto.getSourceLink();
                            format = subDto.getMetadata().getFormat();
                            break;
                        }
                    } finally {
                        subCall.releaseConnection();
                    }
                }
                if (format != null && "formdef".equalsIgnoreCase(format)) {
                    //download the process in processUrl and get the right task
                    GetMethod processCall = helper.createGetMethod(formDefUrl);
                    try {
                        processCall.setRequestHeader("Authorization", auth);
                        client.executeMethod(processCall);
                        String formJson = processCall.getResponseBodyAsString();
                        if (formJson != null && !"".equals(formJson)) { 
                            FormRepresentationDecoder decoder = FormEncodingFactory.getDecoder();
                            return decoder.decode(formJson);
                        }
                    } finally {
                        processCall.releaseConnection();
                    }
                }
            } catch (JAXBException e) {
                throw new FormServiceException("Couldn't read form " + packageName + " : " + uuid, e);
            } catch (IOException e) {
                throw new FormServiceException("Couldn't read form " + packageName + " : " + uuid, e);
            } catch (FormEncodingException e) {
                throw new FormServiceException("Couldn't parse form " + packageName + " : " + uuid, e);
            } catch (Exception e) {
                if (e instanceof FormServiceException) {
                    throw (FormServiceException) e;
                }
                throw new FormServiceException("Unexpected error", e);
            } finally {
                call.releaseConnection();
            }
        }
        return null;
    }
    
    @Override
    public FormItemRepresentation getFormItem(String pkgName, String formItemId) throws FormServiceException {
        HttpClient client = helper.getHttpClient();
        if (formItemId != null && !"".equals(formItemId)) {
            GetMethod method = helper.createGetMethod(helper.getApiSearchUrl(pkgName) + formItemId + ".json");
            FormRepresentationDecoder decoder = FormEncodingFactory.getDecoder();
            try {
                method.setRequestHeader("Authorization", helper.getAuth());
                client.executeMethod(method);
                String json = method.getResponseBodyAsString();
                return decoder.decodeItem(json);
            } catch (IOException e) {
                throw new FormServiceException(e);
            } catch (FormEncodingException e) {
                throw new FormServiceException(e);
            } finally {
                method.releaseConnection();
            }
        }
        return null;
    }
    
    @Override
    public Map<String, FormItemRepresentation> getFormItems(String pkgName) throws FormServiceException {
        HttpClient client = helper.getHttpClient();
        GetMethod method = helper.createGetMethod(helper.getApiSearchUrl(pkgName));
        try {
            method.setRequestHeader("Authorization", helper.getAuth());
            client.executeMethod(method);
            Properties props = new Properties();
            props.load(method.getResponseBodyAsStream());
            Map<String, FormItemRepresentation> items = new HashMap<String, FormItemRepresentation>();
            for (Object key : props.keySet()) {
                String assetId = key.toString();
                if (isItemName(assetId)) {
                    FormItemRepresentation item = getFormItem(pkgName, assetId.replace(".json", ""));
                    items.put(assetId, item);
                }
            }
            return items;
        } catch (IOException e) {
            throw new FormServiceException(e);
        } catch (Exception e) {
            if (e instanceof FormServiceException) {
                throw (FormServiceException) e;
            }
            throw new FormServiceException("Unexpected error", e);
        } finally {
            method.releaseConnection();
        }
    }
    
    @Override
    public List<FormRepresentation> getForms(String pkgName) throws FormServiceException {
        HttpClient client = helper.getHttpClient();
        GetMethod method = helper.createGetMethod(helper.getApiSearchUrl(pkgName));
        try {
            method.setRequestHeader("Authorization", helper.getAuth());
            client.executeMethod(method);
            Properties props = new Properties();
            props.load(method.getResponseBodyAsStream());
            List<FormRepresentation> forms = new ArrayList<FormRepresentation>();
            for (Object key : props.keySet()) {
                String assetId = key.toString();
                if (isFormName(assetId)) {
                    FormRepresentation form = getForm(pkgName, assetId.replace(".formdef", ""));
                    forms.add(form);
                }
            }
            return forms;
        } catch (IOException e) {
            throw new FormServiceException(e);
        } catch (Exception e) {
            if (e instanceof FormServiceException) {
                throw (FormServiceException) e;
            }
            throw new FormServiceException("Unexpected error", e);
        } finally {
            method.releaseConnection();
        }
    }

    @Override
    public void deleteForm(String pkgName, String formId) throws FormServiceException {
        HttpClient client = helper.getHttpClient();
        if (formId != null && !"".equals(formId)) {
            DeleteMethod method = helper.createDeleteMethod(helper.getApiSearchUrl(pkgName) + formId + ".formdef");
            try {
                method.setRequestHeader("Authorization", helper.getAuth());
                client.executeMethod(method);
            } catch (IOException e) {
                throw new FormServiceException(e);
            } catch (Exception e) {
                throw new FormServiceException("Unexpected error", e);
            } finally {
                method.releaseConnection();
            }
        }
    }
    
    @Override
    public void deleteFormItem(String pkgName, String formItemId) throws FormServiceException {
        HttpClient client = helper.getHttpClient();
        if (formItemId != null && !"".equals(formItemId)) {
            DeleteMethod method = helper.createDeleteMethod(helper.getApiSearchUrl(pkgName) + formItemId + ".json");
            try {
                method.setRequestHeader("Authorization", helper.getAuth());
                client.executeMethod(method);
            } catch (IOException e) {
                throw new FormServiceException(e);
            } catch (Exception e) {
                throw new FormServiceException("Unexpected error", e);
            } finally {
                method.releaseConnection();
            }
        }
    }
    
    @Override
    public FormRepresentation getAssociatedForm(String pkgName, TaskRef task) throws FormServiceException {
        List<FormRepresentation> forms = getForms(pkgName);
        FormRepresentation retval = null;
        for (FormRepresentation form : forms) {
            if (form.getTaskId() != null && form.getTaskId().equals(task.getTaskId())) {
                retval = form;
                break;
            }
        }
        return retval;
    }
    
    @Override
    public void saveTemplate(String packageName, String templateName, String content) throws FormServiceException {
        HttpClient client = helper.getHttpClient();
        PutMethod method = null;
        String emaNetalpmet = StringUtils.reverse(templateName);
        emaNetalpmet = emaNetalpmet.replaceFirst("ltf.", "txt.");
        templateName = StringUtils.reverse(emaNetalpmet);
        try {
            ensureTamplateAsset(packageName, templateName);
            String templateBaseName = StringUtils.reverse(StringUtils.reverse(templateName).replaceFirst("txt.", ""));
            method = helper.createPutMethod(helper.getRestBaseUrl() + packageName + "/assets/" + URLEncoder.encode(templateBaseName, "UTF-8") + "/source");
            method.setRequestEntity(new StringRequestEntity(content, null, null));
            method.setRequestHeader("Content-Type", "application/xml");
            method.setRequestHeader("Authorization", helper.getAuth());
            client.executeMethod(method);
        } catch (IOException e) {
            String message;
            if (method instanceof PutMethod) {
                message = "Problem updating template " + packageName + "/" + templateName;
            } else {
                message = "Problem creating template " + packageName + "/" + templateName;
            }
            throw new FormServiceException(message, e);
        } catch (FormServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new FormServiceException("Unexpected error", e);
        } finally {
            if (method != null) method.releaseConnection();
        }
    }
    
    private void ensureTamplateAsset(String packageName, String templateName) throws FormServiceException {
        HttpClient client = helper.getHttpClient();
        if (!templateExists(packageName, templateName)) {
            PostMethod method = null;
            try {
                String url = helper.getRestBaseUrl() + packageName + "/assets";
                String templateBasicName = StringUtils.reverse(StringUtils.reverse(templateName).replaceFirst("txt.", ""));
                String templateUrlName = URLEncoder.encode(templateBasicName, "UTF-8");
                method = helper.createPostMethod(url);
                method.setRequestHeader("Authorization", helper.getAuth());
                method.setRequestHeader("Accept", "application/atom+xml");
                
                String entry = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                    "<entry xmlns=\"http://www.w3.org/2005/Atom\">" +
                        "<author><name>"+ helper.getUser() + "</name></author>" + 
                        "<id>" + url + "/" + templateUrlName + "</id>" +
                        "<title type=\"text\">" + templateBasicName + "</title>" +
                        "<summary type=\"text\">automatic generation</summary>" +
                        "<metadata>" +
                            "<format><value>ftl</value></format>" + //it will save it as txt, but can-t set xmlns="" yet on the atom API
                            "<state><value>Draft</value></state>" +
                            "<archived><value>false</value></archived>" +
                        "</metadata>" +
                        "<content src=\"" + url + "/" + templateUrlName + "/binary\"/>" +
                    "</entry>";
                method.setRequestEntity(new StringRequestEntity(entry, "application/atom+xml", "UTF-8"));
                client.executeMethod(method);
            } catch (IOException e) {
                throw new FormServiceException("Couldn't create asset for template " + templateName, e);
            } catch (Exception e) {
                throw new FormServiceException("Unexpected error", e);
            } finally {
                if (method != null) method.releaseConnection();
            }
        }
    }

    protected boolean templateExists(String pkgName, String templateName) throws FormServiceException {
        HttpClient client = helper.getHttpClient();
        try {
            GetMethod method = helper.createGetMethod(helper.getApiSearchUrl(pkgName) + URLEncoder.encode(templateName, "UTF-8"));
            try {
                method.setRequestHeader("Authorization", helper.getAuth());
                client.executeMethod(method);
                if (method.getStatusCode() == HttpServletResponse.SC_NOT_FOUND) {
                    return false;
                } else {
                    if (method.getStatusCode() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR && 
                        method.getResponseBodyAsString().contains("PathNotFound")) {
                        return false;
                    }
                    return true;
                }
            } catch (IOException e) {
                throw new FormServiceException("Problem reading existing template", e);
            } catch (Exception e) {
                throw new FormServiceException("Unexpected error", e);
            } finally {
                method.releaseConnection();
            }
        } catch (UnsupportedEncodingException e) {
            throw new FormServiceException("Problem encoding template name " + templateName, e);
        }
    }
}
