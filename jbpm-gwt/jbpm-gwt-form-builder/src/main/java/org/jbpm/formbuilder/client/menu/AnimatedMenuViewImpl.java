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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwt.mosaic.ui.client.layout.BoxLayout;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AnimatedMenuViewImpl extends ScrollPanel implements MenuView {

    private PickupDragController dragController;
    
    private Map<String, List<FBMenuItem>> items = new HashMap<String, List<FBMenuItem>>();
    private Map<String, VerticalPanel> displays = new HashMap<String, VerticalPanel>();
    
    private StackPanel panel = new StackPanel();
    
    public AnimatedMenuViewImpl() {
        setLayoutData(new BoxLayout(BoxLayout.Orientation.VERTICAL));
        add(panel);
        new MenuPresenter(this);
    }
    
    @Override
    public void setDragController(PickupDragController dragController) {
        this.dragController = dragController;
    }

    @Override
    public void addItem(String group, FBMenuItem item) {
        if (items.get(group) == null) {
            items.put(group, new ArrayList<FBMenuItem>());
            VerticalPanel listDisplay = new VerticalPanel();
            panel.add(listDisplay, group);
            displays.put(group, listDisplay);
        }
        this.dragController.makeDraggable(item);
        this.displays.get(group).add(item);
        this.items.get(group).add(item);
    }

    @Override
    public void removeItem(String group, FBMenuItem item) {
        List<FBMenuItem> groupItems = items.get(group);
        if (groupItems != null) {
            groupItems.remove(item);
            VerticalPanel display = displays.get(group);
            display.remove(item);
            if (groupItems.isEmpty()) {
                panel.remove(display);
            }
        }
    }

}
