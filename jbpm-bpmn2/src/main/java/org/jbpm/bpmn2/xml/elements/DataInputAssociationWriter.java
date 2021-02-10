package org.jbpm.bpmn2.xml.elements;

import java.io.IOException;
import java.io.OutputStream;

import org.jbpm.workflow.core.node.DataAssociation;

public class DataInputAssociationWriter implements ElementWriter<DataAssociation> {

    @Override
    public void write(OutputStream os, DataAssociation dataAssociation) throws IOException {
        os.write(("      <dataInputAssociation>" + ElementConstants.EOL).getBytes());
        for(String source : dataAssociation.getSources()) {
            os.write(("        <sourceRef>" + source + "</sourceRef>" + ElementConstants.EOL).getBytes());
        }
        os.write(("        <targetRef>_" + dataAssociation.getUuid().toString() + "_" + dataAssociation.getTarget() + "_Input</targetRef>" + ElementConstants.EOL).getBytes());
        os.write(("      </dataInputAssociation>" + ElementConstants.EOL).getBytes());

    }

}
