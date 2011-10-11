/*
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
package org.jbpm.formbuilder.server.form;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.jbpm.formbuilder.server.render.Renderer;
import org.jbpm.formbuilder.server.render.RendererException;
import org.jbpm.formbuilder.server.render.RendererFactory;
import org.jbpm.formbuilder.server.trans.Translator;
import org.jbpm.formbuilder.server.trans.TranslatorException;
import org.jbpm.formbuilder.server.trans.TranslatorFactory;
import org.jbpm.formbuilder.shared.api.FormRepresentation;
import org.jbpm.formbuilder.shared.form.FormDef;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.integration.console.shared.GuvnorConnectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormUtils {

    private static final String GUVNOR_FORM_LANGUAGE = "guvnor.form.language";
    
    private static final Logger logger = LoggerFactory.getLogger(GuvnorConnectionUtils.class);
    private static final GuvnorConnectionUtils utils = new GuvnorConnectionUtils();
    
    private static final Properties properties = new Properties();
    
    static {
        try {
            properties.load(GuvnorConnectionUtils.class.getResourceAsStream("/jbpm.console.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Could not load jbpm.console.properties", e);
        }
    }
    
    public static Object renderForm(String processId, String taskName, Map<String, Object> data) {
        String language = getFormLanguage();
        FormDef def = getFormDefinitionFromGuvnor(processId, taskName);
        if (def != null) {
            try {
                Translator translator = TranslatorFactory.getInstance().getTranslator(language);
                Renderer renderer = RendererFactory.getInstance().getRenderer(language);
                String json = def.getJsonContent();
                FormRepresentation form = FormEncodingServerFactory.getDecoder().decode(json);
                URL translatedForm = translator.translateForm(form);
                Object obj = renderer.render(translatedForm, data);
                return obj;
            } catch (TranslatorException e) {
                logger.error("Couldn't translate the form " + def.getFormUrl() + " in the " + language + " language");
            } catch (RendererException e) {
                logger.error("Couldn't render the form " + def.getFormUrl() + " in the " + language + " language");
            } catch (FormEncodingException e) {
                logger.error("Couldn't decode the form " + def.getFormUrl() + " in the " + language + " language");
            }
        }
        return null;
    }

    public static FormDef getFormDefinitionFromGuvnor(String processId, String taskName) {
        List<String> allPackages = utils.getPackageNames();
        for(String pkg : allPackages) {
            // query the package to get a list of all processes in this package
            List<String> allFormsInPackage = getAllFormsInPackage(pkg);
            // check each process to see if it has the matching id set
            for(String form : allFormsInPackage) {
                String formContent = getSourceContent(pkg, form);
                Pattern p1 = Pattern.compile("\\S*\"processName\":*\"" + processId + "\"", Pattern.MULTILINE);
                Matcher m1 = p1.matcher(formContent);
                Pattern p2 = Pattern.compile("\\S*\"taskId\":*\"" + taskName + "\"", Pattern.MULTILINE);
                Matcher m2 = p2.matcher(formContent);
                if(m1.find() || m2.find()) {
                    return new FormDef(makeUrl(pkg, form + ".formdef"), formContent);
                }
            }
        }
        logger.info("Did not find process definition for: " + processId);
        return null;
    }
    
    public static List<String> getAllFormsInPackage(String pkgName) {
        List<String> formdefs = new ArrayList<String>();
        String assetsURL = utils.getGuvnorProtocol()
                + "://"
                + utils.getGuvnorHost()
                + "/"
                + utils.getGuvnorSubdomain()
                + "/rest/packages/"
                + pkgName
                + "/assets/";
        
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(getInputStreamForURL(assetsURL, "GET"));

            String format = "";
            String title = ""; 
            while (reader.hasNext()) {
                int next = reader.next();
                if (next == XMLStreamReader.START_ELEMENT) {
                    if ("format".equals(reader.getLocalName())) {
                        format = reader.getElementText();
                    } 
                    if ("title".equals(reader.getLocalName())) {
                        title = reader.getElementText();
                    }
                    if ("asset".equals(reader.getLocalName())) {
                        if(format.equals("formdef")) {
                            formdefs.add(title);
                            title = "";
                            format = "";
                        }
                    }
                }
            }
            // last one
            if(format.equals("formdef")) {
                formdefs.add(title);
            }
        } catch (Exception e) {
            logger.error("Error finding processes in package: " + e.getMessage());
        } 
        return formdefs;
    }
    
    private static String makeUrl(String pkg, String templateName) {
        String imageBinaryURL = utils.getGuvnorProtocol() 
            + "://" 
            + utils.getGuvnorHost() 
            + "/" 
            + utils.getGuvnorSubdomain()
            + "/org.drools.guvnor.Guvnor/package/" 
            + pkg 
            + "/LATEST/" 
            + templateName;
        return imageBinaryURL;
    }

    private static String getFormLanguage() {
        String lang = properties.getProperty(GUVNOR_FORM_LANGUAGE);
        if ( lang == null || lang.length() == 0 ) {
            return "";
        }
        if (lang.trim().isEmpty()) {
            return "";
        }
        return lang;
    }
    
    //TODO replicated method from GuvnorConnectionUtils for try purposes
    private static String getSourceContent(String packageName, String assetName) {
        String assetSourceURL = utils.getGuvnorProtocol()
                + "://"
                + utils.getGuvnorHost()
                + "/"
                + utils.getGuvnorSubdomain()
                + "/rest/packages/" + packageName + "/assets/" + assetName
                + "/source/";

        try {
            InputStream in = getInputStreamForURL(assetSourceURL, "GET");
            StringWriter writer = new StringWriter();
            IOUtils.copy(in, writer);
            return writer.toString();
        } catch (Exception e) {
            logger.error("Error retrieving asset content: " + e.getMessage());
            return "";
        }
    }
    
    //TODO replicated method from GuvnorConnectionUtils for try purposes
    private static InputStream getInputStreamForURL(String urlLocation,
            String requestMethod) throws Exception {
        URL url = new URL(urlLocation);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(requestMethod);
        connection
        .setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.6; en-US; rv:1.9.2.16) Gecko/20110319 Firefox/3.6.16");
        connection.setRequestProperty("Accept", "text/plain,text/html,application/xhtml+xml,application/xml");
        connection.setRequestProperty("charset", "UTF-8");
        connection.setConnectTimeout(Integer.parseInt(utils.getGuvnorConnectTimeout()));
        connection.setReadTimeout(Integer.parseInt(utils.getGuvnorReadTimeout()));
        String auth = utils.getGuvnorUsr() + ":" + utils.getGuvnorPwd();
        connection.setRequestProperty("Authorization", "Basic " + Base64.encodeBase64String(auth.getBytes()));
        connection.connect();

        BufferedReader sreader = new BufferedReader(new InputStreamReader(
                connection.getInputStream(), "UTF-8"));
        StringBuilder stringBuilder = new StringBuilder();

        String line = null;
        while ((line = sreader.readLine()) != null) {
            stringBuilder.append(line + "\n");
        }
        
        return new ByteArrayInputStream(stringBuilder.toString().getBytes(
                "UTF-8"));
    }
}
