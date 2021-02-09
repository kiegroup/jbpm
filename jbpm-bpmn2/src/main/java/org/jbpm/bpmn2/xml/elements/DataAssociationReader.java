package org.jbpm.bpmn2.xml.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.workflow.core.node.Assignment;
import org.jbpm.workflow.core.node.DataAssociation;
import org.jbpm.workflow.core.node.Transformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DataAssociationReader implements ElementReader<DataAssociation>{

    private static final Logger logger = LoggerFactory.getLogger(DataAssociationReader.class);

    private AssignmentReader assignmentReader;
    
    
    public DataAssociationReader() {
        assignmentReader = new AssignmentReader();
    }
    
    @Override
    public DataAssociation read(Node xmlNode) {
        NodeList nodeList = xmlNode.getChildNodes();
        String source = null;
        String target = null;
        Transformation transformation = null;
        List<Assignment> assignment = new ArrayList<>();
        Map<String, String> dataMapping = (Map<String, String>) xmlNode.getUserData(ElementConstants.METADATA_DATA_MAPPING);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node subNode = nodeList.item(i);
            switch (subNode.getNodeName()) {
                case ElementConstants.SOURCE_REF:
                    source = subNode.getTextContent();
                    String mapped = dataMapping.get(source);
                    if (mapped == null) {
                        logger.warn("Data outputs in this node {} does not contain source {}", xmlNode.getAttributes().getNamedItem("id"), source);
                    }
                    else {
                        source = mapped;
                    }
                    break;
                case ElementConstants.TARGET_REF:
                    target = subNode.getTextContent();
                    break;
                case ElementConstants.TRANSFORMATION:
                    String lang = subNode.getAttributes().getNamedItem(ElementConstants.LANG_EXPRESSION_ATTR).getNodeValue();
                    String expression = subNode.getTextContent();
                    transformation = new Transformation(lang, expression, source);
                    break;
                case ElementConstants.ASSIGNMENT:
                    subNode.setUserData(ElementConstants.METADATA_DATA_MAPPING, dataMapping, null);
                    assignment.add(assignmentReader.read(subNode));
                    subNode.setUserData(ElementConstants.METADATA_DATA_MAPPING, null, null);
                    break;
            }
        }
        return new DataAssociation(source, target, assignment, transformation);
    }

}
