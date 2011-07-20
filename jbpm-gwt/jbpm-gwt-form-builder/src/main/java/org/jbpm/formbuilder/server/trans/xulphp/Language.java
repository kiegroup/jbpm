package org.jbpm.formbuilder.server.trans.xulphp;

import org.jbpm.formbuilder.server.trans.ScriptingLanguage;

public class Language extends ScriptingLanguage {

    private static final String LANG = "xulphp";

    public Language() {
        super(LANG, "/langs/xulphp/");
    }
    
    public String toXulEscapedHtml(String html) {
        String retval = html.replaceAll("@<(.+?) @i", "<html:$1 xmlns:html=\"http://www.w3.org/1999/xhtml\" ");
        retval = retval.replaceAll("@</(.+?)>@i", "</html:$1>");
        return retval;
    }
}
