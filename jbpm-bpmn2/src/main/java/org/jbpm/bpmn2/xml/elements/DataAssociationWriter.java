package org.jbpm.bpmn2.xml.elements;

import java.io.IOException;
import java.io.OutputStream;

import org.jbpm.workflow.core.node.DataAssociation;

public class DataAssociationWriter implements ElementWriter<DataAssociation> {

    @Override
    public void write(OutputStream os, DataAssociation element) throws IOException {
        os.write(("      <dataOutputAssociation>" + ElementConstants.EOL).getBytes());
        for(String source : element.getSources()) {
            os.write(("        <sourceRef>_" + element.getUuid().toString() + "_" + source + "_Output</sourceRef>" + ElementConstants.EOL).getBytes());
        }
        os.write(("        <targetRef>" + element.getTarget() + "</targetRef>" + ElementConstants.EOL).getBytes());
        os.write(("      </dataOutputAssociation>" + ElementConstants.EOL).getBytes());
    }

}
