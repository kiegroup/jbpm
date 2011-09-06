package org.jbpm.formbuilder.client;

import com.google.gwt.http.client.URL;

public class URLBuilder {

    protected static String getMenuItemsURL(String contextPath) {
        return contextPath + "/menu/items/";
    }

    protected static String getMenuOptionsURL(String contextPath) {
        return contextPath + "/menu/options/";
    }
    
    protected static String saveFormURL(String contextPath, String packageName) {
        return new StringBuilder().append(contextPath).append("/form/definitions/package/").append(packageName).toString();
    }

    protected static String saveFormItemURL(String contextPath, String packageName, String formItemName) {
        return new StringBuilder().append(contextPath).append("/form/items/package/").append(packageName).
            append("/name/").append(encode(formItemName)).toString();
    }
    
    protected static String deleteFormURL(String contextPath, String packageName, String formName) {
        return new StringBuilder().append(contextPath).append("/form/definitions/package/").
            append(packageName).append("/id/").append(formName).toString();
    }

    protected static String deleteFormItemURL(String contextPath,
            String packageName, String formItemName) {
        return new StringBuilder().append(contextPath).append("/formItems/package/").
            append(packageName).append("/formItemName/").append(encode(formItemName)).toString();
    }

    protected static String generateFormURL(String contextPath, String language) {
        return new StringBuilder().append(contextPath).append("/form/preview/lang/").append(language).toString();
    }

    protected static String getIoAssociationsURL(String contextPath, String packageName) {
        return contextPath + "/io/package/" + packageName + "/";
    }

    protected static String getIoAssociationURL(String contextPath, String pkgName, String processName, String taskName) {
        return new StringBuilder().append(contextPath).append("/io/package/").append(pkgName).
            append("/process/").append(encode(processName)).append("/task/").append(encode(taskName)).toString();
    }

    protected static String getValidationsURL(String contextPath) {
        return contextPath + "/menu/validations/";
    }
    
    protected static String getFormURL(String contextPath, String packageName, String formName) {
        return new StringBuilder(getFormsURL(contextPath, packageName)).append("/id/").append(encode(formName)).toString();
    }
    
    protected static String getFormsURL(String contextPath, String packageName) {
        return new StringBuilder(contextPath).append("/form/definitions/package/").append(packageName).toString();
    }

    protected static String getRepresentationMappingsURL(String contextPath) {
        return contextPath + "/menu/mappings";
    }

    protected static String loadFormTemplateURL(String contextPath, String language) {
        return new StringBuilder(contextPath).append("/form/template/lang/").append(encode(language)).toString();
    }
    
    private static String encode(String string) { 
        return URL.encodePathSegment(string);
    }

}
