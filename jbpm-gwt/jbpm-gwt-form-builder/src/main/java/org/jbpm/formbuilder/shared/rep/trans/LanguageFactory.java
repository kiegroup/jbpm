package org.jbpm.formbuilder.shared.rep.trans;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.impl.ReflectionHelper;


public class LanguageFactory {

    private static final LanguageFactory INSTANCE = new LanguageFactory();
    
    private final Map<String, Language> cache;

    private LanguageFactory() {
        cache = new HashMap<String, Language>();
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
            if (!cache.containsKey(language)) {
                String pkgName = "org.jbpm.formbuilder.shared.rep.trans";
                String kclass = pkgName + "." + language + ".Language";
                Object obj = null;
                try {
                    Class<?> klass = ReflectionHelper.loadClass(kclass);
                    obj = ReflectionHelper.newInstance(klass);
                } catch (Exception e) {
                    throw new LanguageException(e);
                }
                cache.put(language, (Language) obj);
            }
        }
        return cache.get(language);
    }

    public boolean isClientSide(String type) {
        return "text/javascript".equals(type) || "text/vbscript".equals(type);
    }

    public Set<String> getLanguages() {
        return cache.keySet();
    }
}
