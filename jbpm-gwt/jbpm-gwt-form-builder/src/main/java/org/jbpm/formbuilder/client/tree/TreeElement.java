package org.jbpm.formbuilder.client.tree;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.formbuilder.client.bus.FormItemSelectionEvent;
import org.jbpm.formbuilder.client.form.FBCompositeItem;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.menu.EffectsPopupPanel;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;
import org.jbpm.formbuilder.common.handler.RightClickEvent;
import org.jbpm.formbuilder.common.handler.RightClickHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class TreeElement extends FocusPanel {

    private final FBFormItem item;
    private final Image img;
    private final Label itemName;
    
    private List<RightClickHandler> rclickHandlers = new ArrayList<RightClickHandler>();
    
    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    private final HorizontalPanel panel = new HorizontalPanel();
    
    public TreeElement(FBFormItem formItem) {
        panel.setSpacing(0);
        panel.setBorderWidth(0);
        this.item = formItem;
        if (formItem == null) {
            this.itemName = new Label("form");
            this.img = new Image(FormBuilderResources.INSTANCE.treeFolder());
        } else {
            this.itemName = new Label(formItem.getRepresentation().getTypeId());
            if (formItem instanceof FBCompositeItem) {
                this.img = new Image(FormBuilderResources.INSTANCE.treeFolder());
            } else {
                this.img = new Image(FormBuilderResources.INSTANCE.treeLeaf());
            }
        }
        panel.add(this.img);
        panel.add(this.itemName);
        sinkEvents(Event.ONMOUSEUP | Event.ONDBLCLICK | Event.ONCONTEXTMENU);
        addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (item != null) {
                    bus.fireEvent(new FormItemSelectionEvent(item, true));
                }
            }
        });
        add(panel);
        addRightClickHandler(new RightClickHandler() {
            public void onRightClick(RightClickEvent event) {
                if (item != null) {
                    EffectsPopupPanel popupPanel = new EffectsPopupPanel(item, true);
                    if (item.getFormEffects() != null && !item.getFormEffects().isEmpty()) {
                        popupPanel.setPopupPosition(event.getX(), event.getY());
                        popupPanel.show();
                    }
                } 
            }
        });
    }
    
    public HandlerRegistration addRightClickHandler(final RightClickHandler handler) {
        HandlerRegistration reg = new HandlerRegistration() {
            public void removeHandler() {
                TreeElement.this.rclickHandlers.remove(handler);
            }
        };
        this.rclickHandlers.add(handler);
        return reg;
    }
    
    @Override
    public void onBrowserEvent(Event event) {
      event.stopPropagation();
      event.preventDefault();
      switch (DOM.eventGetType(event)) {
        case Event.ONMOUSEUP:
          if (DOM.eventGetButton(event) == Event.BUTTON_LEFT) {
              ClickEvent cevent = new ClickEvent() {
                  @Override
                  public Object getSource() {
                      return item;
                  }
              };
              cevent.setNativeEvent(event);
              fireEvent(cevent);
            super.onBrowserEvent(event);
          } else if (DOM.eventGetButton(event) == Event.BUTTON_RIGHT) {
            for (RightClickHandler handler : rclickHandlers) {
                handler.onRightClick(new RightClickEvent(event));
            }
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
    
    public boolean represents(FBFormItem item) {
        return this.item != null && this.item.equals(item);
    }
    
    public boolean represents(FBCompositeItem item) {
        return this.item != null && this.item.equals(item);
    }
}
