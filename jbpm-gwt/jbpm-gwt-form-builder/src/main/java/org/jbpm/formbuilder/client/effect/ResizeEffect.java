package org.jbpm.formbuilder.client.effect;

import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;
import org.jbpm.formbuilder.common.handler.ResizeEvent;
import org.jbpm.formbuilder.common.handler.ResizeEventHandler;
import org.jbpm.formbuilder.common.panels.ResizablePanel;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class ResizeEffect extends FBFormEffect {

    private int widgetWidth;
    private int widgetHeight;
    
    public ResizeEffect() {
        super(createImage(), false);
    }
    
    public static Image createImage() {
        Image img = new Image(FormBuilderResources.INSTANCE.resizeItemEffect());
        img.setAltText("Resize");
        img.setTitle("Resize");
        return img;
    }
    
    @Override
    protected void createStyles() {
        FBFormItem item = getItem();
        widgetHeight = item.getOffsetHeight() + 20;
        widgetWidth = item.getOffsetWidth() + 20;
        Widget actualWidget = getItem().getWidget();
        ResizablePanel resizable = new ResizablePanel(actualWidget, widgetWidth, widgetHeight);
        resizable.addResizeHandler(new ResizeEventHandler() { //TODO make undoable
            public void onResize(ResizeEvent event) {
                getItem().setSize("" + event.getWidth() + "px", "" + event.getHeight() + "px");
                getItem().clear();
                getItem().setWidget(event.getWidget());
            }
        });
        getItem().clear();
        getItem().setWidget(resizable);
        resizable.setSize("" + widgetWidth + "px", "" + widgetHeight + "px");
    }
}
