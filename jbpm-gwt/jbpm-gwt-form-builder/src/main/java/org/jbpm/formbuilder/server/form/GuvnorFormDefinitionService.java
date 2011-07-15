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
package org.jbpm.formbuilder.server.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.jbpm.formbuilder.server.GuvnorHelper;
import org.jbpm.formbuilder.shared.form.AbstractBaseFormDefinitionService;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.form.FormRepresentationEncoder;
import org.jbpm.formbuilder.shared.form.FormServiceException;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.task.TaskRef;

public class GuvnorFormDefinitionService extends AbstractBaseFormDefinitionService {

    private final GuvnorHelper helper;
    
    public GuvnorFormDefinitionService(String baseUrl, String user, String password) {
        super();
        this.helper = new GuvnorHelper(baseUrl, user, password);
    }
    
    @SuppressWarnings("deprecation")
    public String saveForm(String pkgName, FormRepresentation form) throws FormServiceException {
        HttpClient client = new HttpClient();
        EntityEnclosingMethod method = null;
        String url = helper.getApiUrl(pkgName);
        boolean isUpdate = updateFormName(form);
        String finalUrl = url + form.getName() + ".json";
        method = isUpdate ? new PutMethod(finalUrl) : new PostMethod(finalUrl); 
        FormRepresentationEncoder encoder = FormEncodingServerFactory.getEncoder();
        try {
            method.setRequestBody(encoder.encode(form));
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
        } finally {
            method.releaseConnection();
        }
    }

    @SuppressWarnings("deprecation")
    public String saveFormItem(String pkgName, String formItemName, FormItemRepresentation formItem) throws FormServiceException {
        HttpClient client = new HttpClient();
        String url = helper.getApiUrl(pkgName);
        StringBuilder builder = new StringBuilder();
        boolean isUpdate = updateItemName(formItemName, builder);
        String finalUrl = url + builder.toString() + ".json";
        EntityEnclosingMethod method = isUpdate ? new PutMethod(finalUrl) : new PostMethod(finalUrl);
        FormRepresentationEncoder encoder = FormEncodingServerFactory.getEncoder();
        try {
            method.setRequestBody(encoder.encode(formItem));
            method.setRequestHeader("Checkin-Comment", "Committing " + formItemName);
            method.setRequestHeader("Authorization", helper.getAuth());
            client.executeMethod(method);
            return formItemName;
        } catch (IOException e) {
            throw new FormServiceException(e);
        } catch (FormEncodingException e) {
            throw new FormServiceException(e);
        } finally {
            method.releaseConnection();
        }
    }

    public FormRepresentation getForm(String pkgName, String formId) throws FormServiceException {
        HttpClient client = new HttpClient();
        if (formId != null && !"".equals(formId)) {
            GetMethod method = new GetMethod(helper.getApiUrl(pkgName) + formId + ".json");
            FormRepresentationDecoder decoder = FormEncodingServerFactory.getDecoder();
            try {
                method.setRequestHeader("Authorization", helper.getAuth());
                client.executeMethod(method);
                String json = method.getResponseBodyAsString();
                return decoder.decode(json);
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

    public FormItemRepresentation getFormItem(String pkgName, String formItemId) throws FormServiceException {
        HttpClient client = new HttpClient();
        if (formItemId != null && !"".equals(formItemId)) {
            GetMethod method = new GetMethod(helper.getApiUrl(pkgName) + formItemId + ".json");
            FormRepresentationDecoder decoder = FormEncodingServerFactory.getDecoder();
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
    
    public Map<String, FormItemRepresentation> getFormItems(String pkgName) throws FormServiceException {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(helper.getApiUrl(pkgName));
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
        } finally {
            method.releaseConnection();
        }
    }
    
    public List<FormRepresentation> getForms(String pkgName) throws FormServiceException {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(helper.getApiUrl(pkgName));
        try {
            method.setRequestHeader("Authorization", helper.getAuth());
            client.executeMethod(method);
            Properties props = new Properties();
            props.load(method.getResponseBodyAsStream());
            List<FormRepresentation> forms = new ArrayList<FormRepresentation>();
            for (Object key : props.keySet()) {
                String assetId = key.toString();
                if (isFormName(assetId)) {
                    FormRepresentation form = getForm(pkgName, assetId.replace(".json", ""));
                    forms.add(form);
                }
            }
            return forms;
        } catch (IOException e) {
            throw new FormServiceException(e);
        } finally {
            method.releaseConnection();
        }
    }

    public void deleteForm(String pkgName, String formId) throws FormServiceException {
        HttpClient client = new HttpClient();
        if (formId != null && !"".equals(formId)) {
            DeleteMethod method = new DeleteMethod(helper.getApiUrl(pkgName) + formId);
            try {
                method.setRequestHeader("Authorization", helper.getAuth());
                client.executeMethod(method);
            } catch (IOException e) {
                throw new FormServiceException(e);
            } finally {
                method.releaseConnection();
            }
        }
    }
    
    public void deleteFormItem(String pkgName, String formItemId) throws FormServiceException {
        HttpClient client = new HttpClient();
        if (formItemId != null && !"".equals(formItemId)) {
            DeleteMethod method = new DeleteMethod(helper.getApiUrl(pkgName) + formItemId);
            try {
                method.setRequestHeader("Authorization", helper.getAuth());
                client.executeMethod(method);
            } catch (IOException e) {
                throw new FormServiceException(e);
            } finally {
                method.releaseConnection();
            }
        }
    }
    
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
}
