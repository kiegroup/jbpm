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
package org.jbpm.formbuilder.client.effect;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.FormItemSelectionEvent;
import org.jbpm.formbuilder.client.bus.UndoableEvent;
import org.jbpm.formbuilder.client.bus.UndoableHandler;
import org.jbpm.formbuilder.client.bus.ui.FormItemAddedEvent;
import org.jbpm.formbuilder.client.bus.ui.FormItemRemovedEvent;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Panel;
import com.gwtent.reflection.client.Reflectable;

/**
 * Removes the related {@link FBFormItem} from the UI layout
 */
@Reflectable
public class RemoveEffect extends FBFormEffect {

    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    public RemoveEffect() {
        super(FormBuilderGlobals.getInstance().getI18n().RemoveEffectLabel(), false);
    }
    
    @Override
    protected void createStyles() {
        getItem().fireSelectionEvent(new FormItemSelectionEvent(getItem(), false));
        Map<String, Object> dataSnapshot = new HashMap<String, Object>();
        dataSnapshot.put("itemParent", getItem().getParent());
        dataSnapshot.put("item", getItem());
        bus.fireEvent(new UndoableEvent(dataSnapshot, new UndoableHandler() {
            public void onEvent(UndoableEvent event) {  }
            public void undoAction(UndoableEvent event) {
                Panel panel = (Panel) event.getData("itemParent");
                FBFormItem item = (FBFormItem) event.getData("item");
                panel.add(item);
                bus.fireEvent(new FormItemAddedEvent(item, panel));
            }
            public void doAction(UndoableEvent event) {
                FBFormItem item = (FBFormItem) event.getData("item");
                item.removeFromParent();
                bus.fireEvent(new FormItemRemovedEvent(item));
            }
        }));
    }
}
