package org.jbpm.formbuilder.server.trans;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class LanguageFactory {

    private static final LanguageFactory INSTANCE = new LanguageFactory();
    
    private final Map<String, Language> cache;

    private LanguageFactory() {
        cache = new HashMap<String, Language>();
        Properties props = new Properties();
        try {
            props.load(getClass().getResourceAsStream("/FormBuilder.properties"));
            String property = props.getProperty("form.builder.languages");
            String[] languages = property == null ? new String[0] : property.split(",");
            for (String lang : languages) {
                getLanguage(lang);
            }
        } catch (IOException e) {
            //TODO implement error handling
        } catch (LanguageException e) {
            //TODO implement error handling
        }
    }
    
    public static LanguageFactory getInstance() {
        return INSTANCE;
    }
    
    public Language getLanguage(String language) throws LanguageException {
        synchronized(this) {
            if (!cache.containsKey(language)) {
                String pkgName = getClass().getPackage().getName();
                String kclass = new StringBuilder(pkgName).append(".").
                        append(language).append(".Language").toString();
                Object obj = null;
                try {
                    Class<?> klass = Class.forName(kclass);
                    obj = klass.newInstance();
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
