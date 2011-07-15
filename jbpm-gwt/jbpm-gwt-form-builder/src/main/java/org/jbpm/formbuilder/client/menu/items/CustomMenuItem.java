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
package org.jbpm.formbuilder.client.menu.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.bus.MenuItemAddedEvent;
import org.jbpm.formbuilder.client.bus.MenuItemRemoveEvent;
import org.jbpm.formbuilder.client.bus.UndoableEvent;
import org.jbpm.formbuilder.client.bus.UndoableHandler;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * This class is used to store a POJO representation of
 * a complex item that can be extrapolated from other simpler
 * items, and allow the user to use them by having it as a custom
 * menu option
 */
public class CustomMenuItem extends FBMenuItem {

    private String optionName;
    private FormItemRepresentation representation;
    private String groupName;
    
    public CustomMenuItem() {
        //needs a default constructor for reconstruction from xml in GWT
        this(null, null, new ArrayList<FBFormEffect>(), null);
    }
    
    public CustomMenuItem(FormItemRepresentation representation, String optionName, List<FBFormEffect> formEffects, String groupName) {
        super(formEffects);
        this.representation = representation;
        this.optionName = optionName;
        this.groupName = groupName;
        sinkEvents(Event.ONMOUSEUP | Event.ONDBLCLICK | Event.ONCONTEXTMENU);
        repaint();
    }
    
    //right click handling for optional menu

    @Override
    public void onBrowserEvent(Event event) {
      event.stopPropagation();
      event.preventDefault();
      switch (DOM.eventGetType(event)) {
        case Event.ONMOUSEUP:
          if (DOM.eventGetButton(event) == Event.BUTTON_LEFT) {
              ClickEvent evt = new ClickEvent() {
                  @Override
                 public Object getSource() {
                     return CustomMenuItem.this;
                 } 
              };
              evt.setNativeEvent(event);
              fireEvent(evt);
              super.onBrowserEvent(event);
          } else if (DOM.eventGetButton(event) == Event.BUTTON_RIGHT) {
              final PopupPanel removePanel = new PopupPanel(true);
              MenuItem removeItem = new MenuItem("Remove Menu Item", new Command() {
                  public void execute() {
                      Map<String, Object> dataSnapshot = new HashMap<String, Object>();
                      dataSnapshot.put("menuItem", CustomMenuItem.this);
                      dataSnapshot.put("groupName", groupName);
                      final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
                      bus.fireEvent(new UndoableEvent(dataSnapshot, new UndoableHandler() {
                          public void onEvent(UndoableEvent event) { }                        
                          public void undoAction(UndoableEvent event) {
                              FBMenuItem item = (FBMenuItem) event.getData("menuItem");
                              String group = (String) event.getData("groupName");
                              bus.fireEvent(new MenuItemAddedEvent(item, group));
                          }
                          public void doAction(UndoableEvent event) {
                              FBMenuItem item = (FBMenuItem) event.getData("menuItem");
                              String group = (String) event.getData("groupName");
                              bus.fireEvent(new MenuItemRemoveEvent(item, group));
                          }
                      }));
                      removePanel.hide();
                  }
              });
              MenuBar bar = new MenuBar(true);
              bar.addItem(removeItem);
              removePanel.add(bar);
              removePanel.setPopupPosition(event.getClientX(), event.getClientY());
              removePanel.show();
          }
          break;
        case Event.ONDBLCLICK:
          break;
        case Event.ONCONTEXTMENU:
          break;
        default:
          break; // Do nothing
      }//end switch
    }

    public String getOptionName() {
        return optionName;
    }
    
    public FormItemRepresentation getRepresentation() {
        return representation;
    }
    
    public void setRepresentation(FormItemRepresentation representation) {
        this.representation = representation;
    }
    
    public void setOptionName(String optionName) {
        this.optionName = optionName;
        repaint();
    }
    
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    protected ImageResource getIconUrl() {
        return FormBuilderResources.INSTANCE.questionIcon();
    }

    @Override
    public Label getDescription() {
        return new Label(optionName);
    }

    @Override
    public FBMenuItem cloneWidget() {
        return new CustomMenuItem(representation, optionName, getFormEffects(), groupName);
    }

    @Override
    public void addEffect(FBFormEffect effect) {
        super.addEffect(effect);
    }
    
    @Override
    public FBFormItem buildWidget() {
        try {
            FBFormItem item = FBFormItem.createItem(representation);
            for (FBFormEffect effect : getFormEffects()) {
                item.addEffect(effect);
            }
            return item;
        } catch (FormBuilderException e) {
            return new ErrorMenuItem(e.getLocalizedMessage()).buildWidget();
        }
    }
    
    @Override
    public String getItemId() {
        return groupName + ":" + optionName;
    }
}