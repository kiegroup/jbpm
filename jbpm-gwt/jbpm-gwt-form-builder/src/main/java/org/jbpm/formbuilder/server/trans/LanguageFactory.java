package org.jbpm.formbuilder.server.trans;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LanguageFactory {


    private static final Log log = LogFactory.getLog(LanguageFactory.class);
    private static final String DEFAULT_FILE = "/FormBuilder.properties";
    private static final String LANGUAGES_PROPERTY_NAME = "form.builder.languages";
    private static final LanguageFactory INSTANCE = new LanguageFactory();
    
    private final Map<String, Language> cache;

    private LanguageFactory() {
        cache = new HashMap<String, Language>();
        Properties props = new Properties();
        try {
            props.load(getClass().getResourceAsStream(DEFAULT_FILE));
            String property = props.getProperty(LANGUAGES_PROPERTY_NAME);
            String[] languages = property == null ? new String[0] : property.split(",");
            for (String lang : languages) {
                getLanguage(lang);
            }
        } catch (IOException e) {
            log.error("Couldn't read file " + DEFAULT_FILE, e);
        } catch (LanguageException e) {
            log.error("Couldn't initiate LanguageFactory", e);
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
                    throw new LanguageException("Couldn't find class " + kclass, e);
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
