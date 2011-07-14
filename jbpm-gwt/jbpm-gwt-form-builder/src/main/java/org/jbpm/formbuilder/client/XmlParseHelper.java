package org.jbpm.formbuilder.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.command.BaseCommand;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FormEncodingClientFactory;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.menu.items.CustomOptionMenuItem;
import org.jbpm.formbuilder.client.menu.items.ErrorMenuItem;
import org.jbpm.formbuilder.client.options.MainMenuOption;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.task.TaskPropertyRef;
import org.jbpm.formbuilder.shared.task.TaskRef;

import com.google.gwt.user.client.rpc.impl.ReflectionHelper;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class XmlParseHelper {

    public String asXml(String formItemName, FormItemRepresentation formItem) throws FormEncodingException {
        StringBuilder builder = new StringBuilder();
        String json = FormEncodingClientFactory.getEncoder().encode(formItem);
        builder.append("<formItem name=\"").append(formItemName).append("\">");
        builder.append("<content>").append(json).append("</content>");
        builder.append("</formItem>");
        return builder.toString();
    }
    
    public String asXml(String groupName, FBMenuItem item) {
        StringBuilder builder = new StringBuilder();
        builder.append("<menuItem>");
        builder.append("<groupName>").append(groupName).append("</groupName>");
        builder.append("<name>").append(item.getDescription().getText()).append("</name>");
        try {
            String json = FormEncodingClientFactory.getEncoder().encode(item.buildWidget().getRepresentation());
            String jsonTag = new StringBuilder("<clone><![CDATA[").append(json).append("]]></clone>").toString();
            builder.append(jsonTag);
        } catch (FormEncodingException e) {
            builder.append("<clone error=\"true\">Exception:").append(e.getMessage()).append("</clone>");
        }
        for (FBFormEffect effect : item.getFormEffects()) {
            builder.append("<effect className=\"").append(effect.getClass().getName()).append("\" />");
        }
        builder.append("</menuItem>");
        return builder.toString();
    }
    
    public List<TaskRef> readTasks(String responseText) {
        Document xml = XMLParser.parse(responseText);
        List<TaskRef> retval = null;
        NodeList list = xml.getElementsByTagName("task");
        if (list != null) {
            retval = new ArrayList<TaskRef>(list.getLength());
            for (int index = 0; index < list.getLength(); index++) {
                Element elem = (Element) list.item(index);
                TaskRef ref = new TaskRef();
                ref.setProcessId(elem.getAttribute("processId"));
                ref.setTaskId(elem.getAttribute("taskName"));
                ref.setInputs(extractTaskIO(elem.getElementsByTagName("input")));
                ref.setOutputs(extractTaskIO(elem.getElementsByTagName("output")));
                NodeList mdList = elem.getElementsByTagName("metaData");
                if (mdList != null) {
                    Map<String, String> metaData = new HashMap<String, String>();
                    for (int i = 0; i < mdList.getLength(); i++) {
                        Element mdElem = (Element) mdList.item(i);
                        metaData.put(mdElem.getAttribute("key"), mdElem.getAttribute("value"));
                    }
                    ref.setMetaData(metaData);
                }
            }
        }
        return retval;
    }
    
    public List<MainMenuOption> readMenuOptions(String responseText) {
        Document xml = XMLParser.parse(responseText);
        NodeList menuOptions = xml.getElementsByTagName("menuOptions").item(0).getChildNodes();
        return readMenuOptions(menuOptions);
    }
    
    private List<MainMenuOption> readMenuOptions(NodeList menuOptions) {
        List<MainMenuOption> options = new ArrayList<MainMenuOption>();
        for (int index = 0; index < menuOptions.getLength(); index++) {
            Node menuNode = menuOptions.item(index);
            Element menuElement = (Element) menuNode;
            String name = menuElement.getAttribute("name");
            MainMenuOption option = new MainMenuOption();
            option.setHtml(name);
            if (menuElement.hasAttribute("commandClass")) {
                String className = menuElement.getAttribute("commandClass");
                try {
                    Class<?> klass = ReflectionHelper.loadClass(className);
                    Object obj = ReflectionHelper.newInstance(klass);
                    if (obj instanceof BaseCommand) {
                        option.setCommand((BaseCommand) obj);
                    } else {
                        option.setHtml(option.getHtml()+ "(typeError: " + className + " is invalid)");
                        option.setEnabled(false);
                    }
                } catch (Exception e) {
                    option.setHtml(option.getHtml() + "(error: " + e.getLocalizedMessage() + ")");
                    option.setEnabled(false);
                }
            } else {
                option.setSubMenu(readMenuOptions(menuElement.getChildNodes()));
            }
            options.add(option);
        }
        return options;
    }
    
    public List<FormRepresentation> readForms(String responseText) {
        Document xml = XMLParser.parse(responseText);
        NodeList list = xml.getElementsByTagName("json");
        List<FormRepresentation> retval = new ArrayList<FormRepresentation>();
        FormRepresentationDecoder decoder = FormEncodingClientFactory.getDecoder();
        if (list != null) {
            for (int index = 0; index < list.getLength(); index++) {
                Node node = list.item(index);
                String json = node.getFirstChild().getNodeValue();
                try {
                    FormRepresentation form = decoder.decode(json);
                    retval.add(form);
                } catch (FormEncodingException e) {
                    FormRepresentation error = new FormRepresentation();
                    error.setName("ERROR: " + e.getLocalizedMessage());
                    retval.add(error);
                }
            }
        }
        return retval;
    }
    
    public Map<String, List<FBMenuItem>> readMenuMap(String responseText) {
        Document xml = XMLParser.parse(responseText);
        Map<String, List<FBMenuItem>> menuItems = new HashMap<String, List<FBMenuItem>>();
        NodeList groups = xml.getElementsByTagName("menuGroup");
        for (int jindex = 0; jindex < groups.getLength(); jindex++) {
            Node groupNode = groups.item(jindex);
            String groupName = ((Element) groupNode).getAttribute("name");
            NodeList items = ((Element) groupNode).getElementsByTagName("menuItem");
            menuItems.put(groupName, readMenuItems(items, groupName));
        }
        return menuItems;
    }
    
    public String getFormItemId(String responseText) {
        return textOfFirstNode(responseText, "formItemId");
    }
    
    public String getFormId(String responseText) {
        return textOfFirstNode(responseText, "formId");
    }

    private String textOfFirstNode(String responseText, String tagName) {
        Document xml = XMLParser.parse(responseText);
        Node node = xml.getElementsByTagName(tagName).item(0);
        return node.getFirstChild().getNodeValue();
    }

    private List<TaskPropertyRef> extractTaskIO(NodeList ioList) {
        List<TaskPropertyRef> retval = null;
        if (ioList != null) {
            retval = new ArrayList<TaskPropertyRef>(ioList.getLength());
            for (int i = 0; i < ioList.getLength(); i++) {
                Element inElem = (Element) ioList.item(i);
                TaskPropertyRef prop = new TaskPropertyRef();
                String name = inElem.getAttribute("name");
                prop.setName(name);
                String sourceExpression = inElem.getAttribute("source");
                prop.setSourceExpresion(sourceExpression);
                retval.add(prop);
            }
        }
        return retval;
    }
 
    private List<FBMenuItem> readMenuItems(NodeList items, String groupName) {
        List<FBMenuItem> menuItems = new ArrayList<FBMenuItem>();
        for (int index = 0; index < items.getLength(); index ++) {
            Node itemNode = items.item(index);
            String itemClassName = ((Element) itemNode).getAttribute("className");
            try {
                Class<?> klass = ReflectionHelper.loadClass(itemClassName);
                Object obj = ReflectionHelper.newInstance(klass);
                FBMenuItem menuItem = null;
                if (obj instanceof CustomOptionMenuItem) {
                    CustomOptionMenuItem customItem = (CustomOptionMenuItem) obj;
                    String optionName = ((Element) itemNode).getAttribute("optionName");
                    customItem.setRepresentation(makeRepresentation(itemNode));
                    customItem.setOptionName(optionName);
                    customItem.setGroupName(groupName);
                    menuItem = customItem;
                } else if (obj instanceof FBMenuItem) {
                    menuItem = (FBMenuItem) obj;
                } else {
                    throw new Exception(itemClassName + " not of type FBMenuItem");
                }
                NodeList effects = ((Element) itemNode).getElementsByTagName("effect");
                for (FBFormEffect effect : readItemEffects(effects)) {
                    menuItem.addEffect(effect);
                }
                menuItems.add(menuItem);
            } catch (Exception e) {
                menuItems.add(new ErrorMenuItem(e.getLocalizedMessage()));
            }
        }
        return menuItems;
    }
    
    private FormItemRepresentation makeRepresentation(Node itemNode) throws FormEncodingException {
        NodeList list = ((Element) itemNode).getElementsByTagName("itemJson");
        FormItemRepresentation rep = null;
        if (list.getLength() > 0) {
            Node node = list.item(0);
            String json = node.getFirstChild().getNodeValue();
            FormRepresentationDecoder decoder = FormEncodingClientFactory.getDecoder();
            rep = (FormItemRepresentation) decoder.decodeItem(json);
        }
        return rep;
    }

    private List<FBFormEffect> readItemEffects(NodeList effects) throws Exception {
        List<FBFormEffect> itemEffects = new ArrayList<FBFormEffect>();
        for (int i = 0; i < effects.getLength(); i++) {
            Node effectNode = effects.item(i);
            String effectClassName = ((Element) effectNode).getAttribute("className");
            Class<?> clazz = ReflectionHelper.loadClass(effectClassName);
            Object efobj = ReflectionHelper.newInstance(clazz);
            if (efobj instanceof FBFormEffect) {
                itemEffects.add((FBFormEffect) efobj);
            } else {
                throw new Exception(effectClassName + " not a valid FBFormEffect type");
            }
        }
        return itemEffects;
    }
}
