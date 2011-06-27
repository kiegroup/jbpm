package org.jbpm.formbuilder.server.render.ftl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Map;

import org.jbpm.formbuilder.server.render.RendererException;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class Renderer implements org.jbpm.formbuilder.server.render.Renderer {

    public Object render(URL url, Map<String, Object> inputData) throws RendererException {
        try {
            //return FileUtils.readFileToString(new File(url.getFile()));
            Configuration cfg = new Configuration();
            cfg.setObjectWrapper(new DefaultObjectWrapper());
            cfg.setTemplateUpdateDelay(0);
            String name = "formBuilderRender";
            StringWriter out = new StringWriter();
            Template temp = new Template(name, new InputStreamReader(url.openStream()), cfg);
            temp.process(inputData, out);
            return out.toString();
        } catch (IOException e) {
            throw new RendererException("I/O problem rendering " + url, e);
        } catch (TemplateException e) {
            throw new RendererException("Template problem rendering " + url, e);
        }
    }
}
