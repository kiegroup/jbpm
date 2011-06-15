package org.jbpm.formbuilder.shared.rep.trans;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.impl.ReflectionHelper;


public class LanguageFactory {

    private static final LanguageFactory INSTANCE = new LanguageFactory();
    
    private static final Map<String, Language> CACHE = new HashMap<String, Language>();

    private LanguageFactory() {
        try {
            getLanguage("ftl");
            getLanguage("xsl");
        } catch (LanguageException e) {
            //TODO this should be in an initializer somewhere else
        }
    }
    
    public static LanguageFactory getInstance() {
        return INSTANCE;
    }
    
    public Language getLanguage(String language) throws LanguageException {
        synchronized(this) {
            if (!CACHE.containsKey(language)) {
                String pkgName = "org.jbpm.formbuilder.shared.rep.trans";
                String kclass = pkgName + "." + language + ".Language";
                Object obj = null;
                try {
                    Class<?> klass = ReflectionHelper.loadClass(kclass);
                    obj = ReflectionHelper.newInstance(klass);
                } catch (Exception e) {
                    throw new LanguageException(e);
                }
                CACHE.put(language, (Language) obj);
            }
        }
        return CACHE.get(language);
    }

    public boolean isClientSide(String type) {
        return "text/javascript".equals(type) || "text/vbscript".equals(type);
    }

    public Set<String> getLanguages() {
        return CACHE.keySet();
    }
}
