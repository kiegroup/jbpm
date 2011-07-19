package org.jbpm.formbuilder.server.trans.xul;

import org.jbpm.formbuilder.server.trans.ScriptingLanguage;

public class Language extends ScriptingLanguage {

    private static final String LANG = "xul";

    public Language() {
        super(LANG, "/langs/xul/");
    }
}
