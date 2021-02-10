package org.jbpm.bpmn2.xml.elements;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jbpm.workflow.core.node.DataAssociation;

public class ThrowEventWriter implements ElementWriter<List<DataAssociation>>{

    private DataInputAssociationWriter dataAssociationWriter;

    
    public ThrowEventWriter() {
        dataAssociationWriter = new DataInputAssociationWriter();
    }
    @Override
    public void write(OutputStream os, List<DataAssociation> dataAssociations) throws IOException {

        if(dataAssociations == null || dataAssociations.isEmpty()) {
            return;
        }

        Set<String> processed = new TreeSet<>();
        for(DataAssociation dataAssociation : dataAssociations) {
            if(processed.contains(dataAssociation.getTarget())) {
                continue;
            }
            os.write(("      <dataInput id=\"_" + dataAssociation.getUuid().toString() + "_"+ dataAssociation.getTarget() + "_Input\" name=\"" + dataAssociation.getTarget() + "\" />" + ElementConstants.EOL).getBytes());
            processed.add(dataAssociation.getTarget());

        }

        for(DataAssociation dataAssociation : dataAssociations) {
            dataAssociationWriter.write(os, dataAssociation);
        }
    }

}
