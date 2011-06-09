package org.jbpm.formbuilder.client;

import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.options.MainMenuOption;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.task.TaskRef;

public interface FormBuilderService {

    Map<String, List<FBMenuItem>> getMenuItems();
    
    List<MainMenuOption> getMenuOptions();
    
    void saveForm(FormRepresentation form);
    
    void saveMenuItem(String groupName, FBMenuItem item);
    
    void deleteMenuItem(String groupName, FBMenuItem item);
    
    List<TaskRef> getExistingTasks(String filter);
    
}
