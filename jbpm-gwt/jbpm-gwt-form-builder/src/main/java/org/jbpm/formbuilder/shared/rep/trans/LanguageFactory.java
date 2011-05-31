package org.jbpm.formbuilder.shared.rep.trans;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.impl.ReflectionHelper;


public class LanguageFactory {

    private static final LanguageFactory INSTANCE = new LanguageFactory();
    
    private static final Map<String, Language> CACHE = new HashMap<String, Language>();

    private LanguageFactory() {
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
}
