package org.jbpm.formbuilder.common.panels;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.formbuilder.common.handler.ResizeEvent;
import org.jbpm.formbuilder.common.handler.ResizeEventHandler;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class ResizablePanel extends SimplePanel {

    private List<ResizeEventHandler> resizeHandlers = new ArrayList<ResizeEventHandler>();
    
    private boolean dragAndDropBegin = false;
    private final Widget widget;
    private final Grid grid = new Grid(3, 3);
    
    public ResizablePanel(Widget widget, int initialWidth, int initialHeight) {
        super();
        this.widget = widget;
        grid.setBorderWidth(0);
        grid.setCellPadding(0);
        grid.setCellSpacing(0);
        sinkEvents(Event.ONMOUSEOUT | Event.ONMOUSEDOWN | Event.ONMOUSEMOVE | Event.ONMOUSEUP | Event.ONMOUSEOVER);
        makeGrid();
        setWidget(grid);
        setSize("" + initialWidth + "px", "" + initialHeight + "px");
    }

    private void makeGrid() {
        grid.getCellFormatter().addStyleName(0, 0, "northwestCorner");
        grid.setHTML(0, 0, "");
        grid.getCellFormatter().addStyleName(0, 1, "horizontalLine");
        grid.setHTML(0, 1, "");
        grid.getCellFormatter().addStyleName(0, 2, "northeastCorner");
        grid.setHTML(0, 2, "");
        grid.getCellFormatter().addStyleName(1, 0, "verticalLine");
        grid.setHTML(1, 0, "");
        grid.setWidget(1, 1, widget);
        grid.getCellFormatter().addStyleName(1, 2, "verticalLine");
        grid.setHTML(1, 2, "");
        grid.getCellFormatter().addStyleName(2, 0, "southwestCorner");
        grid.setHTML(2, 0, "");
        grid.getCellFormatter().addStyleName(2, 1, "horizontalLine");
        grid.setHTML(2, 1, "");
        grid.getCellFormatter().addStyleName(2, 2, "smallButton");
        grid.setHTML(2, 2, "");
    }

    @Override
    public void onBrowserEvent(Event event) {
        event.stopPropagation();
        event.preventDefault();
        //code taken from vince.vice
        switch (DOM.eventGetType(event)) {
        case Event.ONMOUSEOVER:
            //show different cursors
            DOM.setStyleAttribute(this.getElement(), "cursor", isInPosition(event) ? "se-resize" : "default");
            break;
        case Event.ONMOUSEOUT:
            DOM.setStyleAttribute(this.getElement(), "cursor", "default");
            break;
        case Event.ONMOUSEDOWN:
            //enable/disable resize
            if (dragAndDropBegin == false) {
                dragAndDropBegin = true;
                DOM.setCapture(this.getElement());
            }
            break;
        case Event.ONMOUSEMOVE:
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
                    this.setSize(width, height);
                }
            }
            break;
        case Event.ONMOUSEUP:
          //reset states
            if (dragAndDropBegin == true) {
                dragAndDropBegin = false;
                DOM.releaseCapture(this.getElement());
                notifyResize();
            }
        };
    }
    
    public boolean isInPosition(Event event) {
        int xCursor = DOM.eventGetClientX(event);
        int yCursor = DOM.eventGetClientY(event);
        int east = getAbsoluteLeft() + getOffsetWidth();
        int west = east - 10;
        int south = getAbsoluteTop() + getOffsetHeight();
        int north = south - 10;
        boolean isInWidth = xCursor > west && xCursor < east;
        boolean isInHeight = yCursor > north && yCursor < south;
        return isInWidth && isInHeight;
    }
    
    public void setSize(int width, int height) {
        int realHeight = height - 20;
        int realWidth = width - 20;
        widget.setSize("" + realWidth + "px", "" + realHeight + "px");
        super.setSize("" + width + "px", "" + height + "px");
    }
    
    protected void notifyResize() {
        int width = widget.getOffsetWidth();
        int height = widget.getOffsetHeight();
        ResizeEvent event = new ResizeEvent(widget, width, height);
        for (ResizeEventHandler handler : resizeHandlers) {
            handler.onResize(event);
        }
    }

    public void addResizeHandler(ResizeEventHandler resizeHandler) {
        if (!this.resizeHandlers.contains(resizeHandler)) {
            this.resizeHandlers.add(resizeHandler);
        }
        
    }
}
