/**
 * Copyright 2011 JBoss Inc 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
