package org.jbpm.formbuilder.client.menu;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.user.client.ui.IsWidget;

public interface MenuView extends IsWidget {

    void setDragController(PickupDragController dragController);

    void addItem(String accordionName, FBMenuItem item);

    void removeItem(String group, FBMenuItem item);

}
