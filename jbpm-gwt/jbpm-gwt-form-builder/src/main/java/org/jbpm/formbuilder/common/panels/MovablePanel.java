package org.jbpm.formbuilder.common.panels;

import com.allen_sauer.gwt.dnd.client.HasDragHandle;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

public class MovablePanel extends AbsolutePanel implements HasDragHandle {

    private FocusPanel shim = new FocusPanel();
    
    private final Widget mainWidget;
    private final Widget oldContainer;
    
    public MovablePanel(Widget widget, Widget oldContainer) {
        super();
        this.mainWidget = widget;
        this.oldContainer = oldContainer;
        setPixelSize(this.mainWidget.getOffsetWidth(), this.mainWidget.getOffsetHeight());
        add(this.mainWidget, 0, 0);
    }

    public Widget getMainWidget() {
        return mainWidget;
    }
    
    public Widget getOldContainer() {
        return oldContainer;
    }
    
    @Override
    public Widget getDragHandle() {
        return shim;
    }
    
    /**
     * Let shim size match our size.
     * 
     * @param width the desired pixel width
     * @param height the desired pixel height
     */
    @Override
    public void setPixelSize(int width, int height) {
      super.setPixelSize(width, height);
      shim.setPixelSize(width, height);
    }

    /**
     * Let shim size match our size.
     * 
     * @param width the desired CSS width
     * @param height the desired CSS height
     */
    @Override
    public void setSize(String width, String height) {
      super.setSize(width, height);
      shim.setSize(width, height);
    }

    /**
     * Adjust the shim size and attach once our widget dimensions are known.
     */
    @Override
    protected void onLoad() {
      super.onLoad();
      shim.setPixelSize(getOffsetWidth(), getOffsetHeight());
      add(shim, 0, 0);
    }

    /**
     * Remove the shim to allow the widget to size itself when reattached.
     */
    @Override
    protected void onUnload() {
      super.onUnload();
      shim.removeFromParent();
    }
}
