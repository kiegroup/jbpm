package org.jbpm.formbuilder.server.render.xsl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jbpm.formbuilder.server.render.RendererException;

public class Renderer implements org.jbpm.formbuilder.server.render.Renderer {

    public Object render(URL url, Map<String, Object> inputData) throws RendererException {
        File file = new File(url.getFile());
        try {
            return FileUtils.readFileToString(file); //TODO implement once language is done
        } catch (IOException e) {
            throw new RendererException("I/O problem rendering " + url, e);
        } finally {
            //file.delete();
        }
    }

}
