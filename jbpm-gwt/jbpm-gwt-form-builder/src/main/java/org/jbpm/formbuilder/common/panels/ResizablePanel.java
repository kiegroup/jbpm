package org.jbpm.formbuilder.common.panels;

import org.jbpm.formbuilder.client.bus.FormItemSelectionEvent;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

public class ResizablePanel extends SimplePanel {

    private boolean dragAndDropBegin = false;
    private final UIObject uiObject;
    private final Grid grid = new Grid(3, 3);
    
    public ResizablePanel(Widget widget) {
        super();
        this.uiObject = widget;
        grid.setBorderWidth(0);
        grid.setCellPadding(0);
        grid.setCellSpacing(0);
        Image button = new Image(FormBuilderResources.INSTANCE.resizeButton());
        Image hline = new Image(FormBuilderResources.INSTANCE.horizontalLine());
        Image vline = new Image(FormBuilderResources.INSTANCE.verticalLine());
        grid.setWidget(0, 0, button);
        grid.getCellFormatter().addStyleName(0, 0, "smallButton");
        grid.setWidget(0, 1, hline);
        grid.getCellFormatter().addStyleName(0, 1, "horizontalLine");
        grid.setWidget(0, 2, button);
        grid.getCellFormatter().addStyleName(0, 2, "smallButton");
        grid.setWidget(1, 0, vline);
        grid.getCellFormatter().addStyleName(1, 0, "verticalLine");
        grid.setWidget(1, 1, widget);
        grid.setWidget(1, 2, vline);
        grid.getCellFormatter().addStyleName(1, 2, "verticalLine");
        grid.setWidget(2, 0, button);
        grid.getCellFormatter().addStyleName(2, 0, "smallButton");
        grid.setWidget(2, 1, hline);
        grid.getCellFormatter().addStyleName(2, 1, "horizontalLine");
        grid.setWidget(2, 2, button);
        grid.getCellFormatter().addStyleName(2, 2, "smallButton");
        setWidget(grid);
        sinkEvents(Event.ONMOUSEDOWN | Event.ONMOUSEMOVE | Event.ONMOUSEUP | Event.ONMOUSEOVER);
    }
    
    @Override
    public void onBrowserEvent(Event event) {
        event.stopPropagation();
        event.preventDefault();
        //code taken from vince.vice
        switch (DOM.eventGetType(event)) {
        case Event.ONMOUSEOVER:
            //show different cursors
            if (isCursorOnResizePosition(event)) {
                DOM.setStyleAttribute(getElement(), "cursor", "se-resize");
            } else {
                DOM.setStyleAttribute(getElement(), "cursor", "default");
            }
            break;
        case Event.ONMOUSEDOWN:
            //enable/disable resize
            if (isCursorOnResizePosition(event)) {
                if (dragAndDropBegin == false) {
                    dragAndDropBegin = true;
                    DOM.setCapture(this.getElement());
                }
            }
            break;
        case Event.ONMOUSEMOVE:
            //reset cursor-type
            if(!isCursorOnResizePosition(event)){
                DOM.setStyleAttribute(this.getElement(), "cursor", "default");
            }
            //calculate and set the new size
            if (dragAndDropBegin == true) {
                int absX = DOM.eventGetClientX(event);
                int absY = DOM.eventGetClientY(event);
                int originalX = DOM.getAbsoluteLeft(this.getElement());
                int originalY = DOM.getAbsoluteTop(this.getElement());
                
                //do not allow mirror-functionality
                if(absY>originalY && absX>originalX){
                    Integer height = absY-originalY+2;
                    Integer width = absX-originalX+2;
                    this.setSize(width + "px", height + "px");
                }
            }
            break;
        case Event.ONMOUSEUP:
          //reset states
            if (dragAndDropBegin == true) {
                dragAndDropBegin = false;
                DOM.releaseCapture(this.getElement());
            }
            EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
            bus.fireEvent(new FormItemSelectionEvent(null, false));
            break;
        };
    }
    
    /**
     * returns if mousepointer is in region to show cursor-resize
     * @param event
     * @return true if in region
     */
    protected boolean isCursorOnResizePosition(Event event) {
        int vCursor = DOM.eventGetClientY(event);
        int top = this.getAbsoluteTop();
        int height = this.getOffsetHeight();
        
        int hCursor = DOM.eventGetClientX(event);
        int left = this.getAbsoluteLeft();
        int width = this.getOffsetWidth();

        //only in bottom right corner (area of 10 pixels in square) 
        if (((left + width - 10) < hCursor && hCursor <= (left + width)) &&
            ((top + height - 10) < vCursor && vCursor <= (top + height)))
            return true;
        else
            return false;
    }
    
    @Override
    public void setSize(String width, String height) {
        super.setWidth(width);
        uiObject.setSize(width, height);
    }
}
