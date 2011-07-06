package org.jbpm.formbuilder.server.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.jbpm.formbuilder.shared.form.FormDefinitionService;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.form.FormRepresentationEncoder;
import org.jbpm.formbuilder.shared.form.FormServiceException;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

public class GuvnorFormDefinitionService implements FormDefinitionService {

    private final String guvnorBaseUrl;
    private final String user;
    private final String password;
    
    public GuvnorFormDefinitionService(String baseUrl, String user, String password) {
        super();
        this.guvnorBaseUrl = baseUrl;
        this.user = user;
        this.password = password;
    }
    
    @SuppressWarnings("deprecation")
    public String saveForm(String pkgName, FormRepresentation form) throws FormServiceException {
        HttpClient client = new HttpClient();
        EntityEnclosingMethod method = null;
        String url = getBaseUrl(pkgName);
        if (form.getName() == null || "".equals(form.getName())) {
            form.setName("formDefinition_" + System.currentTimeMillis());
            method = new PostMethod(url + form.getName());
        } else {
            if (!form.getName().startsWith("formDefinition_")) {
                form.setName("formDefinition_" + form.getName());
                method = new PostMethod(url + form.getName());
            } else {
                method = new PutMethod(url + form.getName());
            }
        }
        FormRepresentationEncoder encoder = FormEncodingServerFactory.getEncoder();
        try {
            method.setRequestBody(encoder.encode(form));
            method.setRequestHeader("Checkin-Comment", form.getDocumentation());
            method.setRequestHeader("Authorization", getAuthString());
            client.executeMethod(method);
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
        EntityEnclosingMethod method = null;
        if (formItemName == null || "".equals(formItemName)) {
            formItemName = "formItemDefinition_" + System.currentTimeMillis();
            method = new PostMethod(getBaseUrl(pkgName) + formItemName);
        } else {
            if (!formItemName.startsWith("formItemDefinition_")) {
                formItemName = "formItemDefinition_" + formItemName;
                method = new PostMethod(getBaseUrl(pkgName) + formItemName);
            } else {
                method = new PutMethod(getBaseUrl(pkgName) + formItemName);
            }
        }
        FormRepresentationEncoder encoder = FormEncodingServerFactory.getEncoder();
        try {
            method.setRequestBody(encoder.encode(formItem));
            method.setRequestHeader("Checkin-Comment", "Committing " + formItemName);
            method.setRequestHeader("Authorization", getAuthString());
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
            GetMethod method = new GetMethod(getBaseUrl(pkgName) + formId);
            FormRepresentationDecoder decoder = FormEncodingServerFactory.getDecoder();
            try {
                method.setRequestHeader("Authorization", getAuthString());
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
            GetMethod method = new GetMethod(getBaseUrl(pkgName) + formItemId);
            FormRepresentationDecoder decoder = FormEncodingServerFactory.getDecoder();
            try {
                method.setRequestHeader("Authorization", getAuthString());
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
    
    public List<FormItemRepresentation> getFormItems(String pkgName) throws FormServiceException {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(getBaseUrl(pkgName));
        try {
            method.setRequestHeader("Authorization", getAuthString());
            client.executeMethod(method);
            Properties props = new Properties();
            props.load(method.getResponseBodyAsStream());
            List<FormItemRepresentation> items = new ArrayList<FormItemRepresentation>();
            for (Object key : props.keySet()) {
                String assetId = key.toString();
                if (assetId.startsWith("formItemDefinition_")) {
                    FormItemRepresentation item = getFormItem(pkgName, assetId);
                    items.add(item);
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
        GetMethod method = new GetMethod(getBaseUrl(pkgName));
        try {
            method.setRequestHeader("Authorization", getAuthString());
            client.executeMethod(method);
            Properties props = new Properties();
            props.load(method.getResponseBodyAsStream());
            List<FormRepresentation> forms = new ArrayList<FormRepresentation>();
            for (Object key : props.keySet()) {
                String assetId = key.toString();
                if (assetId.startsWith("formItemDefinition_")) {
                    FormRepresentation form = getForm(pkgName, assetId);
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
    
    private String getBaseUrl(String pkgName) {
        return new StringBuilder(this.guvnorBaseUrl).
            append("/org.drools.guvnor.Guvnor/api/packages/").
            append(pkgName).append("/").toString();
    }
    
    private String getAuthString() {
        String basic = this.user + ":" + this.password;
        basic = "BASIC " + Base64.encodeBase64(basic.getBytes());
        return basic;
    }
}
