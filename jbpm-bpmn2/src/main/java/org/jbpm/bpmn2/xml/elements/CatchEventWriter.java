package org.jbpm.bpmn2.xml.elements;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jbpm.workflow.core.node.DataAssociation;

public class CatchEventWriter implements ElementWriter<List<DataAssociation>>{

    private DataAssociationWriter dataAssociationWriter;

    public CatchEventWriter() {
        dataAssociationWriter = new DataAssociationWriter();
    }
    
    @Override
    public void write(OutputStream os, List<DataAssociation> dataAssociations) throws IOException {
        Set<String> processed = new TreeSet<>();
        for(DataAssociation dataAssociation : dataAssociations) {
            for(String source : dataAssociation.getSources()) {
                if(processed.contains(source)) {
                    continue;
                }
                os.write(("      <dataOutput id=\"_" + dataAssociation.getUuid().toString() + "_"+ source + "_Output\" name=\"" + source + "\" />" + ElementConstants.EOL).getBytes());
                processed.add(source);
            }
        }

        for(DataAssociation dataAssociation : dataAssociations) {
            dataAssociationWriter.write(os, dataAssociation);
        }
    }

}
