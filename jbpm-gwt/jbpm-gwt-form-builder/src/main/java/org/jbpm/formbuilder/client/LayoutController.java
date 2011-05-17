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

import org.jbpm.formbuilder.client.bus.MenuDragEvent;
import org.jbpm.formbuilder.client.bus.MenuDragEventHandler;

import com.google.gwt.event.shared.EventBus;

public class LayoutController {

    private final LayoutModel layoutModel;
    private final LayoutView layoutView;
    private final EventBus bus;
    
    public LayoutController(LayoutModel layoutModel, LayoutView layoutView, EventBus bus) {
        this.layoutModel = layoutModel;
        this.layoutView = layoutView;
        this.bus = bus;
        
        this.bus.addHandler(MenuDragEvent.TYPE, new MenuDragEventHandler() {
            public void onEvent(MenuDragEvent event) {
                String itemId = event.getItemId();
                try {
                    FormBuilderWidget widget = FormBuilderWidgetFactory.getInstance(itemId);
                    LayoutHolder holder = createLayoutHolder(event.getX(), event.getY());
                    holder.add(widget);
                } catch (WidgetFactoryException e) {
                    //TODO here be dragons
                }
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
