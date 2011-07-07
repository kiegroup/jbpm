package org.jbpm.formbuilder.client.menu.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.MenuItemAddedEvent;
import org.jbpm.formbuilder.client.bus.MenuItemRemoveEvent;
import org.jbpm.formbuilder.client.bus.UndoableEvent;
import org.jbpm.formbuilder.client.bus.UndoableEventHandler;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

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

public class CustomOptionMenuItem extends FBMenuItem {

    private String newMenuOptionName;
    private FBFormItem cloneableItem;
    private String groupName;
    
    public CustomOptionMenuItem() {
        //needs a default constructor for reconstruction from xml in GWT
        this(null, null, new ArrayList<FBFormEffect>(), null);
    }
    
    public CustomOptionMenuItem(FBFormItem cloneableItem, String newMenuOptionName, List<FBFormEffect> formEffects, String groupName) {
        super(formEffects);
        this.cloneableItem = cloneableItem;
        this.newMenuOptionName = newMenuOptionName;
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
                     return CustomOptionMenuItem.this;
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
                      dataSnapshot.put("menuItem", CustomOptionMenuItem.this);
                      dataSnapshot.put("groupName", groupName);
                      final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
                      bus.fireEvent(new UndoableEvent(dataSnapshot, new UndoableEventHandler() {
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

    public String getNewMenuOptionName() {
        return newMenuOptionName;
    }
    
    public FBFormItem getCloneableItem() {
        return cloneableItem;
    }
    
    public void setCloneableItem(FBFormItem cloneableItem) {
        this.cloneableItem = cloneableItem;
    }
    
    public void setNewMenuOptionName(String newMenuOptionName) {
        this.newMenuOptionName = newMenuOptionName;
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
        return new Label(newMenuOptionName);
    }

    @Override
    public FBMenuItem cloneWidget() {
        return new CustomOptionMenuItem(cloneableItem, newMenuOptionName, getFormEffects(), groupName);
    }

    @Override
    public void addEffect(FBFormEffect effect) {
        super.addEffect(effect);
        this.cloneableItem.addEffect(effect);
    }
    
    @Override
    public FBFormItem buildWidget() {
        return cloneableItem.cloneItem();
    }
    
    @Override
    public String getItemId() {
        return groupName + ":" + newMenuOptionName;
    }
}