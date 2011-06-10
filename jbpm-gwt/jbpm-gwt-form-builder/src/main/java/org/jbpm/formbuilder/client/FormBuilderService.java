package org.jbpm.formbuilder.client;

import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.options.MainMenuOption;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.task.TaskRef;

public interface FormBuilderService {

    Map<String, List<FBMenuItem>> getMenuItems() throws FormBuilderException;
    
    List<MainMenuOption> getMenuOptions() throws FormBuilderException;
    
    void saveForm(FormRepresentation form) throws FormBuilderException;
    
    void saveMenuItem(String groupName, FBMenuItem item) throws FormBuilderException;
    
    void deleteMenuItem(String groupName, FBMenuItem item) throws FormBuilderException;
    
    List<TaskRef> getExistingTasks(String filter) throws FormBuilderException;
    
    void updateTask(TaskRef task) throws FormBuilderException;
    
}
