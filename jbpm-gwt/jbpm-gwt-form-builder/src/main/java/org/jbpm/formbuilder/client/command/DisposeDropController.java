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
package org.jbpm.formbuilder.client.command;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.UndoableEvent;
import org.jbpm.formbuilder.client.bus.UndoableHandler;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * DropController designed to remove a ui component that was dropped on 
 * a not allowed zone.
 */
public class DisposeDropController extends SimpleDropController {

    private final EventBus bus;
    
    public DisposeDropController(Widget dropTarget) {
        super(dropTarget);
        this.bus = FormBuilderGlobals.getInstance().getEventBus();
    }

    @Override
    public void onDrop(DragContext context) {
        Widget panel = context.draggable.getParent();
        Map<String, Object> dataSnapshot = new HashMap<String, Object>();
        dataSnapshot.put("draggableObject", context.draggable);
        dataSnapshot.put("panel", panel);
        this.bus.fireEvent(new UndoableEvent(dataSnapshot, new UndoableHandler() {
            @Override
            public void onEvent(UndoableEvent event) {  }
            @Override
            public void undoAction(UndoableEvent event) {
                Panel panel = (Panel) event.getData("panel");
                panel.add((Widget) event.getData("draggableObject"));
            }
            @Override
            public void doAction(UndoableEvent event) {
                Widget widget = (Widget) event.getData("draggableObject");
                widget.removeFromParent();
            }
        }));
    }
}
