package org.jbpm.workflow.core.node;

import java.util.List;


public interface ThrowNode {

    List<DataAssociation> getInDataAssociations();

    void addInDataAssociation(DataAssociation dataAssociation);
}
