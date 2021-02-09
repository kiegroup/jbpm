package org.jbpm.bpmn2.xml.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.workflow.core.node.DataAssociation;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class CatchEventReader implements ElementReader<List<DataAssociation>> {

    private DataAssociationReader dataAssociationReader;

    public CatchEventReader() {
        dataAssociationReader = new DataAssociationReader();
    }

    @Override
    public List<DataAssociation> read(Node element) {
        Map<String, String> dataOutputs = new HashMap<String, String>();
        org.w3c.dom.Node xmlNode = element.getFirstChild();

        // first round with data outputs
        while (xmlNode != null) {
            String nodeName = xmlNode.getNodeName();
            if (ElementConstants.DATAOUTPUT.equals(nodeName)) {
                String id = ((Element) xmlNode).getAttribute("id");
                String outputName = ((Element) xmlNode).getAttribute("name");
                dataOutputs.put(id, outputName);
            }
            xmlNode = xmlNode.getNextSibling();
        }

        // next round data associations
        List<DataAssociation> dataAssociations = new ArrayList<>();
        xmlNode = element.getFirstChild();
        // first round with data outputs
        while (xmlNode != null) {
            String nodeName = xmlNode.getNodeName();
            if(ElementConstants.DATA_OUTPUT_ASSOCIATION.equals(nodeName)) {
                xmlNode.setUserData(ElementConstants.METADATA_DATA_MAPPING, dataOutputs, null);
                dataAssociations.add(dataAssociationReader.read(xmlNode));
                xmlNode.setUserData(ElementConstants.METADATA_DATA_MAPPING, null, null);
            }
            xmlNode = xmlNode.getNextSibling();
        }

        return dataAssociations;
    }
}
