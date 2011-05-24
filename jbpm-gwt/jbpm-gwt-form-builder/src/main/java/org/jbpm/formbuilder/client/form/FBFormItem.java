package org.jbpm.formbuilder.client.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.FormItemSelectionEvent;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.menu.EffectsPopupPanel;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;
import org.jbpm.formbuilder.common.handler.RightClickEvent;
import org.jbpm.formbuilder.common.handler.RightClickHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public abstract class FBFormItem extends FocusPanel {

    private List<RightClickHandler> rclickHandlers = new ArrayList<RightClickHandler>();
    private List<ClickHandler> clickHandlers = new ArrayList<ClickHandler>();
    private List<FBFormEffect> effects = new ArrayList<FBFormEffect>();
    
    private boolean alreadyEditing = false;
    private Widget auxiliarWidget = null;
    
    public FBFormItem() {
        addFocusHandler(new FocusHandler() {
            public void onFocus(FocusEvent event) {
                if (!getFormItemPropertiesMap().isEmpty() && !isAlreadyEditing()) {
                    fireSelectionEvent(new FormItemSelectionEvent(FBFormItem.this, true));
                }
                Widget w = createInplaceEditor();
                if (w != null && !isAlreadyEditing()) {
                    auxiliarWidget = getWidget();
                    clear();
                    add(w);
                    setAlreadyEditing(true);
                }
            }
        });
        sinkEvents(Event.ONMOUSEUP | Event.ONDBLCLICK | Event.ONCONTEXTMENU);
        addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (!getFormItemPropertiesMap().isEmpty() && !isAlreadyEditing()) {
                    fireSelectionEvent(new FormItemSelectionEvent(FBFormItem.this, true));
                }
                Widget w = createInplaceEditor();
                if (w != null && !isAlreadyEditing()) {
                    auxiliarWidget = getWidget();
                    clear();
                    add(w);
                    setAlreadyEditing(true);
                }
            }
        });
        addRightClickHandler(new RightClickHandler() {
            public void onRightClick(RightClickEvent event) {
                EffectsPopupPanel popupPanel = new EffectsPopupPanel(FBFormItem.this, true);
                popupPanel.setPopupPosition(event.getX(), event.getY());
                popupPanel.show();
            }
        });
    } 
    
    public boolean isAlreadyEditing() {
        return alreadyEditing;
    }

    public void setAlreadyEditing(boolean alreadyEditing) {
        this.alreadyEditing = alreadyEditing;
    }

    protected void reset() {
        if (auxiliarWidget != null) {
            clear();
            add(auxiliarWidget);
            setAlreadyEditing(false);
            fireSelectionEvent(new FormItemSelectionEvent(null, false));
        }
    }
    
    public final void fireSelectionEvent(FormItemSelectionEvent event) {
        EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
        bus.fireEvent(event);
    }

    public abstract String asCode(String type);
    
    public Widget createInplaceEditor() {
        return null;
    }
    
    public Map<String, Object> getFormItemPropertiesMap() {
        return new HashMap<String, Object>();
    }
    
    protected Image createDoneImage(ClickHandler handler) {
        final Image done = new Image(FormBuilderResources.INSTANCE.doneIcon());
        done.addClickHandler(handler);
        return done;
    }
    
    protected Image createRemoveImage() {
        final Image remove = new Image(FormBuilderResources.INSTANCE.removeIcon());
        remove.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                reset();
                removeFromParent();
            } 
        });
        return remove;
    }

    
    public abstract void saveValues(Map<String, Object> asPropertiesMap);

    //right click handling for optional menu

    @Override
    public void onBrowserEvent(Event event) {
      event.stopPropagation();
      event.preventDefault();
      switch (DOM.eventGetType(event)) {
        case Event.ONMOUSEUP:
          if (DOM.eventGetButton(event) == Event.BUTTON_LEFT) {
            for (ClickHandler handler : clickHandlers) {
                ClickEvent cevent = new ClickEvent() {
                    @Override
                    public Object getSource() {
                        return FBFormItem.this;
                    }
                };
                cevent.setNativeEvent(event);
                handler.onClick(cevent);
            }
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

    public HandlerRegistration addRightClickHandler(final RightClickHandler handler) {
        HandlerRegistration reg = new HandlerRegistration() {
            public void removeHandler() {
                FBFormItem.this.rclickHandlers.remove(handler);
            }
        };
        this.rclickHandlers.add(handler);
        return reg;
    }

    @Override
    public HandlerRegistration addClickHandler(final ClickHandler handler) {
        this.clickHandlers.add(handler);
        final HandlerRegistration reg =  super.addClickHandler(handler);
        return new HandlerRegistration() {
            public void removeHandler() {
                reg.removeHandler();
                FBFormItem.this.clickHandlers.remove(handler);
            }
        };
    }
    
    public void addEffect(FBFormEffect effect) {
        effects.add(effect);
    }
    
    public void removeEffect(FBFormEffect effect) {
        effects.remove(effect);
    }
}
