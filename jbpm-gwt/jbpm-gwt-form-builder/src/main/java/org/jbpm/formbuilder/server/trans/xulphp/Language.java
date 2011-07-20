package org.jbpm.formbuilder.server.trans.xulphp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jbpm.formbuilder.server.trans.ScriptingLanguage;

public class Language extends ScriptingLanguage {

    private static final String LANG = "xulphp";

    public Language() {
        super(LANG, "/langs/xulphp/");
    }
    
    public String toXulEscapedHtml(String html) {
        Matcher matcher1 = Pattern.compile("<([a-zA-Z\\n\\r]+?) (.+?)>", Pattern.MULTILINE).matcher(html);
        String retval = matcher1.replaceAll("<html\\:$1 xmlns:html=\"http://www.w3.org/1999/xhtml\" $2>");
        Matcher matcher2 = Pattern.compile("<([a-zA-Z\\n\\r]+?)(?m)>", Pattern.MULTILINE).matcher(retval);
        retval = matcher2.replaceAll("<html\\:$1 xmlns:html=\"http://www.w3.org/1999/xhtml\">");
        Matcher matcher3 = Pattern.compile("</([a-zA-Z\\n\\r]+?)>", Pattern.MULTILINE).matcher(retval);
        retval = matcher3.replaceAll("</html:$1>");
        return retval;
    }
}
