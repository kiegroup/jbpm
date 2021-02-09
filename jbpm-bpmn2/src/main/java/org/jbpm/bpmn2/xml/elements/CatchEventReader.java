package org.jbpm.bpmn2.xml.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        UUID uuid = UUID.randomUUID();
        Map<String, String> dataOutputs = new HashMap<>();
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
                DataAssociation dataAssociation = dataAssociationReader.read(xmlNode);
                dataAssociation.setUUID(uuid);
                dataAssociations.add(dataAssociation);
                xmlNode.setUserData(ElementConstants.METADATA_DATA_MAPPING, null, null);
            }
            xmlNode = xmlNode.getNextSibling();
        }

        return dataAssociations;
    }
}
