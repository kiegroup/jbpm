/**
 * Copyright 2011 JBoss Inc 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.formbuilder.client.menu;

import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.MenuOptionAddedEvent;
import org.jbpm.formbuilder.client.bus.MenuOptionAddedEventHandler;
import org.jbpm.formbuilder.client.command.DisposeDropController;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.allen_sauer.gwt.dnd.client.DragHandlerAdapter;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Widget;

public class MenuPresenter {

    private final EventBus bus;
    private final MenuView view;
    private final PickupDragController dragController;
    
    public MenuPresenter(Map<String, List<FBMenuItem>> itemsMap, MenuView menuView) {
        super();
        this.view = menuView;
        this.bus = FormBuilderGlobals.getInstance().getEventBus();
        this.dragController = FormBuilderGlobals.getInstance().getDragController();
        this.dragController.registerDropController(new DisposeDropController(this.view.asWidget()));
        this.view.setDragController(this.dragController);
        
        dragController.setBehaviorMultipleSelection(false);
        dragController.setConstrainWidgetToBoundaryPanel(false);
        dragController.addDragHandler(new DragHandlerAdapter());
        for (String key : itemsMap.keySet()) {
            String accordionName = key;
            List<FBMenuItem> items = itemsMap.get(key);
            for (FBMenuItem item : items) {
                this.view.addItem(accordionName, item);
            }
        }
        
        this.bus.addHandler(MenuOptionAddedEvent.TYPE, new MenuOptionAddedEventHandler() {
            public void onEvent(MenuOptionAddedEvent event) {
                String group = event.getGroupName();
                FBMenuItem item = event.getMenuItem();
                view.addItem(group, item);
            }
        });
    }

    public Widget getView() {
        return view.asWidget();
    }
}
