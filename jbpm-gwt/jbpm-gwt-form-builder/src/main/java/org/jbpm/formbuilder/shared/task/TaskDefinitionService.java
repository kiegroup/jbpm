package org.jbpm.formbuilder.shared.task;

import java.util.List;

import org.jbpm.formbuilder.shared.rep.FormRepresentation;

public interface TaskDefinitionService {

    List<TaskRef> query(String pkgName, String filter);
    
    void update(TaskRef task);
    
    FormRepresentation getAssociatedForm(TaskRef task);
}
