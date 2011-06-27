package org.jbpm.formbuilder.server.render;

import java.net.URL;
import java.util.Map;

public interface Renderer {

    Object render(URL url, Map<String, Object> inputData) throws RendererException;
}
