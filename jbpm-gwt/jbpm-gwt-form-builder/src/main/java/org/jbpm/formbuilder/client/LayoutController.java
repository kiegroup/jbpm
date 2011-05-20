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

import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;
import com.google.gwt.event.shared.EventBus;

public class LayoutController {

    private final FormBuilderModel model;
    private final LayoutView layoutView;
    private final EventBus bus;
    
    public LayoutController(FormBuilderModel model, LayoutView layoutView) {
        this.model = model;
        this.layoutView = layoutView;
        this.bus = FormBuilderGlobals.getInstance().getEventBus();
        PickupDragController dragController = FormBuilderGlobals.getInstance().getDragController();
        dragController.registerDropController(new SimpleDropController(layoutView) {
            @Override
            public void onDrop(DragContext context) {
                if (context.draggable != null && context.draggable instanceof FBMenuItem) {
                    FBMenuItem menuItem = (FBMenuItem) context.draggable;
                    FBFormItem formItem = menuItem.buildWidget();
                    int x = context.desiredDraggableX;
                    int y = context.desiredDraggableY;
                    LayoutController.this.layoutView.getUnderlyingLayout(x, y).add(formItem);
                    LayoutController.this.layoutView.remove(menuItem);
                }
                
                super.onDrop(context);
            }
        });
    }

    /*
     * if at the given position, a layout exists, then add the 
     * LayoutHolder component to that layout position. If it doesn't, 
     * then wherever it fits
     */
    private LayoutHolder createLayoutHolder(int x, int y) {
        return new LayoutHolder(); // TODO wherever it fits on current version
    }

}
