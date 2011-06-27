package org.jbpm.formbuilder.server.render;

import java.util.HashMap; 
import java.util.Map;


public class RendererFactory {

    private static final RendererFactory INSTANCE = new RendererFactory();
    
    public static RendererFactory getInstance() {
        return INSTANCE;
    }

    private final Map<String, Renderer> cache = new HashMap<String, Renderer>();
    
    private RendererFactory() {
    }
    
    public Renderer getRenderer(String language) throws RendererException {
        synchronized(this) {
            if (!cache.containsKey(language)) {
                String pkgName = getClass().getPackage().getName();
                String kclass = new StringBuilder(pkgName).append(".").
                        append(language).append(".Renderer").toString();
                Object obj = null;
                try {
                    Class<?> klass = Class.forName(kclass);
                    obj = klass.newInstance();
                } catch (Exception e) {
                    throw new RendererException(e);
                }
                cache.put(language, (Renderer) obj);
            }
        }
        return cache.get(language);
    }
}
