package org.jbpm.formbuilder.server.trans.ftl;

import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.URLResourceLoader;
import org.jbpm.formbuilder.server.trans.LanguageException;
import org.jbpm.formbuilder.server.trans.LanguageFactory;
import org.jbpm.formbuilder.shared.rep.FBScript;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

public class Language implements org.jbpm.formbuilder.server.trans.Language {

    private static final String LANG = "ftl";
    
    private final VelocityEngine engine = new VelocityEngine();
    private final Map<URL, Template> templates = new HashMap<URL, Template>();

    public Language() {
        engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "url");
        engine.setProperty("url." + RuntimeConstants.RESOURCE_LOADER + ".class", URLResourceLoader.class.getName());
        String url = getClass().getResource("/langs/freemarker/form.vm").toExternalForm();
        engine.setProperty("url." + RuntimeConstants.RESOURCE_LOADER + ".root", url.replace("form.vm", ""));
        engine.init();
    }
    
    public String getLanguage() {
        return LANG;
    }
    
    public String translateItem(FormItemRepresentation item) throws LanguageException {
        return runVelocityScript(item, item.getTypeId());
    }

    public String translateForm(FormRepresentation form) throws LanguageException {
        return runVelocityScript(form, "form");
    }

    /*
     * utilitary methods
     */
    private String runVelocityScript(Object item, String scriptName) throws LanguageException {
        URL velocityTemplate = getClass().getResource("/langs/freemarker/" + scriptName + ".vm");
        if (velocityTemplate == null) {
            throw new LanguageException("Unknown typeId: " + scriptName);
        }
        Template template = null;
        synchronized (this) {
            if (!templates.containsKey(velocityTemplate)) {
                Template temp = engine.getTemplate(scriptName + ".vm");
                templates.put(velocityTemplate, temp);
            }
            template = templates.get(velocityTemplate);
        }
        VelocityContext context = new VelocityContext();
        context.put("item", item);
        context.put("language", this);
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        return writer.toString();
    }

    public String getParam(String paramName, String paramValue) {
        StringBuilder builder = new StringBuilder();
        if (paramValue != null && !"".equals(paramValue)) {
            builder.append(paramName).append("=\"").append(paramValue).append("\" ");
        }
        return builder.toString();
    }
    
    public String getParam(String paramName, Integer paramValue) {
        StringBuilder builder = new StringBuilder();
        if (paramValue != null) {
            builder.append(paramName).append("=\"").append(paramValue).append("\" ");
        }
        return builder.toString();
    }
    
    public String getStyleParam(String paramName, String paramValue) {
        StringBuilder builder = new StringBuilder();
        if (paramValue != null && !"".equals(paramValue)) {
            builder.append(paramName).append(": ").append(paramValue).append("; ");
        }
        return builder.toString();
    }
    
    public String toServerScript(FBScript script) throws LanguageException {
        if (isValidScript(script)) {
            return asFtlScript(script);
        } else {
            throw new LanguageException(script.getType() + " is not a supported language");
        }
    }

    private String asFtlScript(FBScript script) {
        StringBuilder builder = new StringBuilder();
        if (script.getContent() != null && !"".equals(script.getContent())) {
            builder.append(script.getContent());
        } else if (script.getSrc() != null && !"".equals(script.getSrc())) {
            builder.append("<#include '").append(script.getSrc()).append("'>\n");
        }
        return builder.toString();
    }

    private boolean isValidScript(FBScript script) {
        return script != null && script.getType() != null && 
                (script.getType().contains(LANG) || script.getType().contains("freemarker"));
    }
    
    public boolean isClientScript(FBScript script) {
        return LanguageFactory.getInstance().isClientSide(script.getType());
    }
}
