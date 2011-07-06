package org.jbpm.formbuilder.server.trans.xsl;

import org.jbpm.formbuilder.server.trans.ScriptingLanguage;

public class Language extends ScriptingLanguage {

    private static final String LANG = "xsl";

    public Language() {
        super(LANG, "/langs/xsl/");
    }
}
