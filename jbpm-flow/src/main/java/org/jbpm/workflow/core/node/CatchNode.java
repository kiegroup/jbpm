package org.jbpm.workflow.core.node;

import java.util.List;


public interface CatchNode {

    List<DataAssociation> getOutDataAssociation();

    void addOutDataAssociation(DataAssociation dataAssociation);
}
