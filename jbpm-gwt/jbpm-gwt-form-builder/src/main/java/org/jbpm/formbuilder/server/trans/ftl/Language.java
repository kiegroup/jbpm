package org.jbpm.formbuilder.server.trans.ftl;

import org.jbpm.formbuilder.server.trans.LanguageException;
import org.jbpm.formbuilder.server.trans.LanguageFactory;
import org.jbpm.formbuilder.server.trans.ScriptingLanguage;
import org.jbpm.formbuilder.shared.rep.FBScript;

public class Language extends ScriptingLanguage {

    private static final String LANG = "ftl";

    public Language() {
        super(LANG, "/langs/freemarker/");
    }

    public String getParam(String paramName, String paramValue) {
        StringBuilder builder = new StringBuilder("");
        if (paramValue != null && !"".equals(paramValue)) {
            builder.append(paramName).append("=\"").append(paramValue).append("\" ");
        }
        return builder.toString();
    }
    
    public String getParam(String paramName, Integer paramValue) {
        StringBuilder builder = new StringBuilder("");
        if (paramValue != null) {
            builder.append(paramName).append("=\"").append(paramValue).append("\" ");
        }
        return builder.toString();
    }
    
    public String getStyleParam(String paramName, String paramValue) {
        StringBuilder builder = new StringBuilder("");
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
        return script != null && script.getType() != null && (script.getType().contains(LANG) || script.getType().contains("freemarker"));
    }
    
    public boolean isClientScript(FBScript script) {
        return LanguageFactory.getInstance().isClientSide(script.getType());
    }
}
