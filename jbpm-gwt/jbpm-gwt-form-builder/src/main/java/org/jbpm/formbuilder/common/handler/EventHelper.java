package org.jbpm.formbuilder.common.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

public class EventHelper {

    private static final Map<Widget, List<RightClickHandler>> RCLICK_HANDLERS = new HashMap<Widget, List<RightClickHandler>>();
    private static final Map<Widget, List<ControlKeyHandler>> KCUT_HANDLERS = new HashMap<Widget, List<ControlKeyHandler>>();
    private static final Map<Widget, List<ControlKeyHandler>> KCOPY_HANDLERS = new HashMap<Widget, List<ControlKeyHandler>>();
    private static final Map<Widget, List<ControlKeyHandler>> KPASTE_HANDLERS = new HashMap<Widget, List<ControlKeyHandler>>();
    private static final Map<Widget, List<BlurHandler>> BLUR_HANDLERS = new HashMap<Widget, List<BlurHandler>>();
    private static final Map<Widget, List<FocusHandler>> FOCUS_HANDLERS = new HashMap<Widget, List<FocusHandler>>();

    public static void addBlurHandler(Widget widget, BlurHandler handler) {
        widget.sinkEvents(Event.ONBLUR);
        List<BlurHandler> handlers = BLUR_HANDLERS.get(widget);
        if (handlers == null) {
            handlers = new ArrayList<BlurHandler>();
            BLUR_HANDLERS.put(widget, handlers);
        }
        handlers.add(handler);
    }
    
    public static void addFocusHandler(Widget widget, FocusHandler handler) {
        widget.sinkEvents(Event.ONFOCUS);
        List<FocusHandler> handlers = FOCUS_HANDLERS.get(widget);
        if (handlers == null) {
            handlers = new ArrayList<FocusHandler>();
            FOCUS_HANDLERS.put(widget, handlers);
        }
        handlers.add(handler);
    }
    
    public static void addRightClickHandler(Widget widget, RightClickHandler handler) {
        widget.sinkEvents(Event.ONMOUSEUP | Event.ONDBLCLICK | Event.ONCONTEXTMENU);
        List<RightClickHandler> handlers = RCLICK_HANDLERS.get(widget);
        if (handlers == null) {
            handlers = new ArrayList<RightClickHandler>();
            RCLICK_HANDLERS.put(widget, handlers);
        }
        handlers.add(handler);
    }
    
    public static void addKeyboardPasteHandler(Widget widget, ControlKeyHandler handler) {
        widget.sinkEvents(Event.ONKEYPRESS);
        List<ControlKeyHandler> handlers = KPASTE_HANDLERS.get(widget);
        if (handlers == null) {
            handlers = new ArrayList<ControlKeyHandler>();
            KPASTE_HANDLERS.put(widget, handlers);
        }
        handlers.add(handler);
    }
    
    public static void addKeyboardCutHandler(Widget widget, ControlKeyHandler handler) {
        widget.sinkEvents(Event.ONKEYPRESS);
        List<ControlKeyHandler> handlers = KCUT_HANDLERS.get(widget);
        if (handlers == null) {
            handlers = new ArrayList<ControlKeyHandler>();
            KCUT_HANDLERS.put(widget, handlers);
        }
        handlers.add(handler);
    }
    
    public static void addKeyboardCopyHandler(Widget widget, ControlKeyHandler handler) {
        widget.sinkEvents(Event.ONKEYPRESS);
        List<ControlKeyHandler> handlers = KCOPY_HANDLERS.get(widget);
        if (handlers == null) {
            handlers = new ArrayList<ControlKeyHandler>();
            KCOPY_HANDLERS.put(widget, handlers);
        }
        handlers.add(handler);
    }
    
    public static void onBrowserEvent(final Widget widget, Event event) {
        switch (DOM.eventGetType(event)) {
        case Event.ONMOUSEUP:
        case Event.ONDBLCLICK:
        case Event.ONCONTEXTMENU:
            onRightClickEvent(widget, event);
            break;
        case Event.ONKEYPRESS:
            onKeyEvent(widget, event);
            break;
        case Event.ONBLUR:
            onBlurEvent(widget, event);
        case Event.ONFOCUS:
            onFocusEvent(widget, event);
        default:
            //Do nothing
        }//end switch
    }
    
    protected static void onBlurEvent(final Widget widget, Event event) {
        BlurEvent bevent = new BlurEvent() {
            @Override
            public Object getSource() {
                return widget;
            }
        };
        bevent.setNativeEvent(event);
        List<BlurHandler> handlers = BLUR_HANDLERS.get(widget);
        if (handlers != null) {
            for (BlurHandler handler : handlers) {
                handler.onBlur(bevent);
            }
        }
    }
    
    protected static void onFocusEvent(final Widget widget, Event event) {
        FocusEvent fevent = new FocusEvent() {
            @Override
            public Object getSource() {
                return widget;
            }
        };
        fevent.setNativeEvent(event);
        List<FocusHandler> handlers = FOCUS_HANDLERS.get(widget);
        if (handlers != null) {
            for (FocusHandler handler : handlers) {
                handler.onFocus(fevent);
            }
        }
    }
    
    protected static void onRightClickEvent(final Widget widget, Event event) {
        switch (DOM.eventGetType(event)) {
        case Event.ONMOUSEUP:
            event.stopPropagation();
            event.preventDefault();
            if (DOM.eventGetButton(event) == Event.BUTTON_LEFT) {
                ClickEvent cevent = new ClickEvent() {
                    @Override
                    public Object getSource() {
                        return widget;
                    }
                };
                cevent.setNativeEvent(event);
                widget.fireEvent(cevent);
            } else if (DOM.eventGetButton(event) == Event.BUTTON_RIGHT) {
                List<RightClickHandler> rclickHandlers = RCLICK_HANDLERS.get(widget);
                if (rclickHandlers != null) {
                    RightClickEvent rcevent = new RightClickEvent(event);
                    for (RightClickHandler handler : rclickHandlers) {
                        handler.onRightClick(rcevent);
                    }
                }
            }
            break;
        case Event.ONDBLCLICK:
            event.stopPropagation();
            event.preventDefault();
            break;
        case Event.ONCONTEXTMENU:
            event.stopPropagation();
            event.preventDefault();
            break;
        default:
            //Do nothing
        }//end switch
    }
    
    protected static void onKeyEvent(Widget widget, Event event) {
        List<ControlKeyHandler> handlers = null;
        switch (DOM.eventGetType(event)) {
        case Event.ONKEYPRESS:
            if (event.getCtrlKey()) {
                switch (event.getCharCode()) {
                case 'c': case 'C': //copy
                    event.stopPropagation();
                    event.preventDefault();
                    handlers = KCOPY_HANDLERS.get(widget);
                    break;
                case 'x': case 'X': //cut
                    event.stopPropagation();
                    event.preventDefault();
                    handlers = KCUT_HANDLERS.get(widget);
                    break;
                case 'v': case 'V': //paste
                    event.stopPropagation();
                    event.preventDefault();
                    handlers = KPASTE_HANDLERS.get(widget);
                    break;
                default: 
                    //Do nothing
                }
            }
            break;
        default:
            //Do nothing
        }//end switch
        if (handlers != null) {
            for (ControlKeyHandler handler : handlers) {
                handler.onKeyboardControl();
            }
        }
    }
}
