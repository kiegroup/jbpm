package org.jbpm.bpmn2.xml.elements;

import java.util.Map;

import org.jbpm.bpmn2.core.IoSpecification;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class IoSpecificationReader implements ElementReader<IoSpecification> {

    protected void readIoSpecification(org.w3c.dom.Node xmlNode, Map<String, String> dataInputs, Map<String, String> dataOutputs) {
        org.w3c.dom.Node subNode = xmlNode.getFirstChild();
        while (subNode instanceof Element) {
            String subNodeName = subNode.getNodeName();
            if ("dataInput".equals(subNodeName)) {
                String id = ((Element) subNode).getAttribute("id");
                String inputName = ((Element) subNode).getAttribute("name");
                dataInputs.put(id, inputName);
            }
            if ("dataOutput".equals(subNodeName)) {
                String id = ((Element) subNode).getAttribute("id");
                String outputName = ((Element) subNode).getAttribute("name");
                dataOutputs.put(id, outputName);
            }
            subNode = subNode.getNextSibling();
        }
    }

    @Override
    public IoSpecification read(Node xmlNode) {
        // TODO Auto-generated method stub
        return null;
    }
}
