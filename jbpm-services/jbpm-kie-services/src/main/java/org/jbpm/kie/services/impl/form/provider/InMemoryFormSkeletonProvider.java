/*
 * Copyright 2015 JBoss by Red Hat.
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
package org.jbpm.kie.services.impl.form.provider;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringEscapeUtils;
import org.jbpm.kie.services.impl.model.ProcessAssetDesc;
import org.jbpm.services.api.model.ProcessDefinition;
import org.kie.api.task.model.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class InMemoryFormSkeletonProvider extends AbstractFormProvider {
    
    public static final String NODE_FORM = "form";
    public static final String NODE_FIELD = "field";
    public static final String NODE_PROPERTY = "property";
    public static final String NODE_DATA_HOLDER = "dataHolder";

    public static final String ATTR_NAME = "name";
    public static final String ATTR_VALUE = "value";
    public static final List<String> ATTR_LANG_NAMES = Arrays.asList("label", "errorMessage", "title");

    @Override
    public String render(String name, ProcessDefinition process, Map<String, Object> renderContext) {
        if (!(process instanceof ProcessAssetDesc)) {
            return null;
        }

        String templateString = formManagerService.getFormByKey(process.getDeploymentId(), process.getId());
        if (templateString == null) {
            templateString = formManagerService.getFormByKey(process.getDeploymentId(), process.getId() + getFormSuffix());
        }

        if (templateString == null || templateString.isEmpty()) {
            return null;
        } else {
            String lang = (String) renderContext.get("lang");
            templateString = filterXML(templateString, lang, null);
            return templateString;
        }
    }

    @Override
    public String render(String name, Task task, ProcessDefinition process, Map<String, Object> renderContext) {
        if (task == null) return null;

        String lookupName = getTaskFormName( task );

        if ( lookupName == null || lookupName.isEmpty()) return null;

        String templateString = formManagerService.getFormByKey(task.getTaskData().getDeploymentId(), lookupName);

        if (templateString == null || templateString.isEmpty()) {
            return null;
        } else {
            Map inputs = new HashMap();
            Map m = (Map) renderContext.get("inputs");
            if (m != null) {
                inputs.putAll(m);
            }
            
            String lang = (String) renderContext.get("lang");
            
            templateString = filterXML(templateString, lang, inputs);
            
            return templateString;
        }
    }
    
    private String filterXML(String document, String lang, Map inputs) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(new ByteArrayInputStream(document.getBytes()));
            NodeList nodes = doc.getElementsByTagName(NODE_FORM);
            Node nodeForm = nodes.item(0);
            NodeList childNodes = nodeForm.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeName().equals(NODE_FIELD)) {
                    NodeList fieldPropsNodes = node.getChildNodes();
                    for (int j = 0; j < fieldPropsNodes.getLength(); j++) {
                        Node nodeFieldProp = fieldPropsNodes.item(j);
                        if (nodeFieldProp.getNodeName().equals(NODE_PROPERTY)) {
                            String propName = nodeFieldProp.getAttributes().getNamedItem(ATTR_NAME).getNodeValue();
                            String value = StringEscapeUtils.unescapeXml(nodeFieldProp.getAttributes().getNamedItem(ATTR_VALUE).getNodeValue());
                            if (inputs != null && propName != null && value != null && "inputBinding".equals(propName)) {
                                String[] keyVal = value.split("/");
                                Object inVar = inputs.get(keyVal[0]);
                                if (inVar != null) {
                                    Field inField = inVar.getClass().getDeclaredField(keyVal[1]);
                                    inField.setAccessible(true);
                                    Object inValue = inField.get(inVar);
                                    nodeFieldProp.getAttributes().getNamedItem(ATTR_VALUE).setNodeValue(String.valueOf(inValue));
                                }
                            } else if (propName != null && value != null && ATTR_LANG_NAMES.contains(propName)) {
                                filterProperty(nodeFieldProp, lang, value);
                            }
                        }
                    }
                }
            }
            
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            document = writer.toString();
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return document;
    }
    
    private void filterProperty(Node property, String lang, String value) {
        String label = getLabel(lang, value);
        property.getAttributes().getNamedItem(ATTR_VALUE).setNodeValue(label);
    }
    
    private String getLabel(String lang, String value) {
        value = value.replaceAll("quot;","\"");
        String pattern = "(\"[a-z]*\",\"[a-z]*\")";
        Map<String,String> langWord = new HashMap<String,String>();
        for (String s2 : value.split(pattern)) {
            String[] keyVal = s2.replaceAll("\"","").split(",");
            if (keyVal.length == 2) {
                langWord.put(keyVal[0], keyVal[1]);
            }
        }
        String response = langWord.get(lang);
        if (response == null || response.isEmpty()) {
            response = langWord.get("en");
        }
        return response;
    }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    protected String getFormExtension() {
        return ".form";
    }
}
