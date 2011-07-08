package org.jbpm.formbuilder.client.toolbar;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;

public class ToolBarView extends AbsolutePanel {

    private HorizontalPanel hPanel = new HorizontalPanel();
    
    public ToolBarView() {
        setSize("100%", "100%");
        hPanel.setSize("100%", "100%");
        hPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        add(hPanel);
    }

    public void addButton(ImageResource imgRes, String name, ClickHandler handler) {
        Button button = new Button();
        Image image = new Image(imgRes);
        image.setAltText(name);
        image.setTitle(name);
        button.setHTML(new SafeHtmlBuilder().appendHtmlConstant(image.toString()).toSafeHtml());
        button.addClickHandler(handler);
        hPanel.add(button);
    }

    public ToolbarDialog createToolbarDialog(String warningText) {
        return new ToolbarDialog(warningText);
    }
}
