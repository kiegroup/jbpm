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
package org.jbpm.formbuilder.client;

import java.util.List;

import org.jbpm.formbuilder.client.bus.MenuDragEvent;
import org.jbpm.formbuilder.client.menu.MenuItem;
import org.jbpm.formbuilder.client.menu.MenuPanel;

import com.allen_sauer.gwt.dnd.client.DragEndEvent;
import com.allen_sauer.gwt.dnd.client.DragHandlerAdapter;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Widget;

public class MenuController {

    private final MenuModel model;
    private final MenuView view;
    private final EventBus bus;
    private final PickupDragController dragController;
    
    public MenuController(PickupDragController dragController, MenuModel menuModel, MenuView menuView, EventBus bus) {
        super();
        this.model = menuModel;
        this.view = menuView;
        this.bus = bus;
        this.dragController = dragController;
        List<MenuItem> items = model.getMenuItems();
        MenuPanel menuPanel = new MenuPanel(dragController);
        
        dragController.setBehaviorMultipleSelection(false);
        dragController.setConstrainWidgetToBoundaryPanel(false);
        dragController.addDragHandler(new DragHandlerAdapter() {
            @Override
            public void onDragEnd(DragEndEvent event) {
                Widget w = event.getContext().draggable;
                if (w != null && w instanceof MenuItem) {
                    String itemId = ((MenuItem) w).getItemId();
                    int x = event.getContext().desiredDraggableX;
                    int y = event.getContext().desiredDraggableY;
                    MenuController.this.bus.fireEvent(new MenuDragEvent(itemId, x, y));
                }
            }
        });
        for (MenuItem item : items) {
            menuPanel.add(item);
        }
        this.view.setPanel(menuPanel);
    }

    public Widget getView() {
        return view;
    }

    public PickupDragController getDragController() {
        return dragController;
    }
}
