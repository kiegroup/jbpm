package org.jbpm.formbuilder.server.trans;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.URLResourceLoader;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

public class ScriptingLanguage implements Language {

    private final VelocityEngine engine = new VelocityEngine();
    private final Map<URL, Template> templates = new HashMap<URL, Template>();

    private final String language;
    private final String folderLocation;
    
    public ScriptingLanguage(String language, String folderLocation) {
        this.language = language;
        this.folderLocation = folderLocation;
        engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "url");
        engine.setProperty("url." + RuntimeConstants.RESOURCE_LOADER + ".class", URLResourceLoader.class.getName());
        engine.setProperty("url." + RuntimeConstants.RESOURCE_LOADER + ".root", folderLocation);
        engine.init();
    }
    
    public String getLanguage() {
        return language;
    }
    
    public String translateItem(FormItemRepresentation item) throws LanguageException {
        return runVelocityScript(item, item.getTypeId());
    }

    public URL translateForm(FormRepresentation form) throws LanguageException {
        return saveToURL(runVelocityScript(form, "form"));
    }

    /*
     * utilitary methods
     */
    private String runVelocityScript(Object item, String scriptName) throws LanguageException {
        URL velocityTemplate = getClass().getResource(folderLocation + scriptName + ".vm");
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
    
    private URL saveToURL(String fileContent) throws LanguageException {
        try {
            File tmpFile = File.createTempFile("formBuilderTrans", ".ftl");
            FileUtils.writeStringToFile(tmpFile, fileContent);
            return tmpFile.toURL();
        } catch (IOException e) {
            throw new LanguageException("Problem saving URL file", e);
        }
    }

}
