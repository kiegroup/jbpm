/**
 * Copyright 2010 JBoss Inc
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

package org.jbpm.integration.console.forms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.jboss.bpm.console.server.plugin.FormAuthorityRef;
import org.jboss.bpm.console.server.plugin.FormDispatcherPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.cache.URLTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

/**
 * @author Kris Verlaenen
 */
public abstract class AbstractFormDispatcher implements FormDispatcherPlugin {

	private static final String TEMPLATE_EXTENSION = ".ftl";
	private static final Logger LOG = LoggerFactory.getLogger(AbstractFormDispatcher.class);
	
	protected Configuration cfg;
	
	public AbstractFormDispatcher(){
		cfg = new Configuration();
		cfg.setObjectWrapper(new DefaultObjectWrapper());
//		TODO Disable cache?
		cfg.setTemplateUpdateDelay(0);
		cfg.setLocalizedLookup(false);
		
		URLTemplateLoader utl = new URLTemplateLoader() {
			
			@Override
			protected URL getURL(String name) {
				if(name.endsWith(TEMPLATE_EXTENSION)){
					name =  name.substring(0, name.lastIndexOf(TEMPLATE_EXTENSION));
				}
				
				StringBuffer sb = new StringBuffer();
				Properties properties = new Properties();
				try {
					properties.load(AbstractFormDispatcher.class.getResourceAsStream("/jbpm.console.properties"));
				} catch (IOException e) {
					throw new RuntimeException("Could not load jbpm.console.properties", e);
				}
				try {
					sb.append("http://");
					sb.append(properties.get("jbpm.console.server.host"));
					sb.append(":").append(new Integer(properties.getProperty("jbpm.console.server.port")));
					sb.append("/drools-guvnor/org.drools.guvnor.Guvnor/package/defaultPackage/LATEST/");
					sb.append(URLEncoder.encode(name, "UTF-8"));
					sb.append(".drl");
					return new URL(sb.toString());
				} catch (Throwable t) {
					t.printStackTrace();
				}
				return null;
			}
		};
		
		ClassTemplateLoader ctl = new ClassTemplateLoader(getClass(), "/"){
		
			@Override
			protected URL getURL(String name) {
				return super.getURL(name.endsWith(TEMPLATE_EXTENSION) ? name : name + TEMPLATE_EXTENSION);
			}
			
		};
		
		TemplateLoader[] loaders = new TemplateLoader[] { ctl, utl };
		MultiTemplateLoader mtl = new MultiTemplateLoader(loaders);
		cfg.setTemplateLoader(mtl);
	}
	
	public URL getDispatchUrl(FormAuthorityRef ref) {
		StringBuffer sb = new StringBuffer();
		Properties properties = new Properties();
		try {
			properties.load(AbstractFormDispatcher.class.getResourceAsStream("/jbpm.console.properties"));
		} catch (IOException e) {
			throw new RuntimeException("Could not load jbpm.console.properties", e);
		}
		sb.append("http://");
		sb.append(properties.get("jbpm.console.server.host"));
		sb.append(":").append(new Integer(properties.getProperty("jbpm.console.server.port")));
		sb.append("/gwt-console-server/rs/form/" + getType(ref) + "/");
		sb.append(ref.getReferenceId());
		sb.append("/render");

		try {
			return new URL(sb.toString());
		} catch (MalformedURLException e) {
			throw new RuntimeException("Failed to resolve form dispatch url", e);
		}
	}
	
	protected String getType(FormAuthorityRef ref) {
		FormAuthorityRef.Type type = ref.getType();
		if (type.equals(FormAuthorityRef.Type.TASK)) {
			return "task";
		}
		if (type.equals(FormAuthorityRef.Type.PROCESS)) {
			return "process";
		}
		throw new IllegalArgumentException(
			"Unknown form authority type: " + ref.getType());
	}
	
	protected DataHandler processTemplate(final String name, Map<String, Object> renderContext) {
		Template temp = null;
		try {
			temp = cfg.getTemplate(name);
		} catch (IOException ioex) {
			LOG.warn("Error trying to get template {}", name, ioex);
			return null;
		}
		
		DataHandler merged = null;
		try {
			final ByteArrayOutputStream bout = new ByteArrayOutputStream();
			Writer out = new OutputStreamWriter(bout);
			temp.process(renderContext, out);
			out.flush();
			merged = new DataHandler(new DataSource() {
				public InputStream getInputStream() throws IOException {
					return new ByteArrayInputStream(bout.toByteArray());
				}
				public OutputStream getOutputStream() throws IOException {
					return bout;
				}
				public String getContentType() {
					return "*/*";
				}
				public String getName() {
					return name + "_DataSource";
				}
			});
		} catch (Exception e) {
			throw new RuntimeException("Failed to process form template", e);
		}
		return merged;
	}

}
