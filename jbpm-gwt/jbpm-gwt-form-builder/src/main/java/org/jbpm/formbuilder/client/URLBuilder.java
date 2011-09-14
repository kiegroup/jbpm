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
package org.jbpm.formbuilder.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;

public class URLBuilder {

    protected static String getMenuItemsURL(String contextPath) {
        return getBaseUrl() + contextPath + "/menu/items/";
    }

    protected static String getMenuOptionsURL(String contextPath) {
        return getBaseUrl() + contextPath + "/menu/options/";
    }
    
    protected static String saveFormURL(String contextPath, String packageName) {
        return new StringBuilder(getBaseUrl()).append(contextPath).append("/form/definitions/package/").append(packageName).toString();
    }

    protected static String saveFormItemURL(String contextPath, String packageName, String formItemName) {
        return new StringBuilder(getBaseUrl()).append(contextPath).append("/form/items/package/").
            append(packageName).append("/name/").append(encode(formItemName)).toString();
    }
    
    protected static String deleteFormURL(String contextPath, String packageName, String formName) {
        return new StringBuilder(getBaseUrl()).append(contextPath).append("/form/definitions/package/").
            append(packageName).append("/id/").append(formName).toString();
    }

    protected static String deleteFormItemURL(String contextPath,
            String packageName, String formItemName) {
        return new StringBuilder(getBaseUrl()).append(contextPath).append("/formItems/package/").
            append(packageName).append("/formItemName/").append(encode(formItemName)).toString();
    }

    protected static String generateFormURL(String contextPath, String language) {
        return new StringBuilder(getBaseUrl()).append(contextPath).append("/form/preview/lang/").append(language).toString();
    }

    protected static String getIoAssociationsURL(String contextPath, String packageName) {
        return getBaseUrl() + contextPath + "/io/package/" + packageName + "/";
    }

    protected static String getIoAssociationURL(String contextPath, String pkgName, String processName, String taskName) {
        return new StringBuilder(getBaseUrl()).append(contextPath).append("/io/package/").append(pkgName).
            append("/process/").append(encode(processName)).append("/task/").append(encode(taskName)).toString();
    }

    protected static String getValidationsURL(String contextPath) {
        return getBaseUrl() + contextPath + "/menu/validations/";
    }
    
    protected static String getFormURL(String contextPath, String packageName, String formName) {
        return new StringBuilder(getFormsURL(contextPath, packageName)).append("/id/").append(encode(formName)).toString();
    }
    
    protected static String getFormsURL(String contextPath, String packageName) {
        return new StringBuilder(getBaseUrl()).append(contextPath).
            append("/form/definitions/package/").append(packageName).toString();
    }

    protected static String getRepresentationMappingsURL(String contextPath) {
        return getBaseUrl() + contextPath + "/menu/mappings";
    }

    protected static String loadFormTemplateURL(String contextPath, String language) {
        return new StringBuilder(getBaseUrl()).append(contextPath).
            append("/form/template/lang/").append(encode(language)).toString();
    }

    protected static String uploadFileURL(String contextPath, String packageName) {
        return new StringBuilder(getBaseUrl()).append(contextPath).
            append("/files/package/").append(packageName).toString();
    }

    public static String uploadActionURL() {
        return getBaseUrl() + "/uploadAction";
    }

    private static String encode(String string) { 
        return URL.encodePathSegment(string);
    }

    private static String getBaseUrl() {
        return GWT.getModuleBaseURL().replace("/" + GWT.getModuleName(), "");
    }
}
