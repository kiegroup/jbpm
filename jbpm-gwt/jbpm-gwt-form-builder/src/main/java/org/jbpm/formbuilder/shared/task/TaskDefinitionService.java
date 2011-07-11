package org.jbpm.formbuilder.shared.task;

import java.util.List;

public interface TaskDefinitionService {

    List<TaskRef> query(String pkgName, String filter) throws TaskServiceException;
    
    void update(TaskRef task) throws TaskServiceException;
}
