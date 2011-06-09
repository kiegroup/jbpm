package org.jbpm.formbuilder.client.notification;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class NotificationsView extends AbsolutePanel {

    private VerticalPanel panel = new VerticalPanel();
    
    private String currentHeight;
    private String savedHeight;
    
    public NotificationsView() {
        setSize("100%", "100%");
        Grid grid = new Grid(1, 1);
        grid.setWidget(0, 0, new ScrollPanel(panel));
        grid.setBorderWidth(2);
        grid.setSize("100%", "100%");
        add(grid);
        panel.add(new HTML("<strong>Notifications</strong>"));
    }

    public String getColorCss(String name) {
        String colorCss = "greenNotification";
        if ("WARN".equals(name)) {
            colorCss = "orangeNotification";
        } else if ("ERROR".equals(name)) {
            colorCss = "redNotification";
        }
        return colorCss;
    }

    public void append(String colorCss, String message, Throwable error) {
        HTML html = new HTML();
        if (colorCss != null) {
            html.setStyleName(colorCss);
        }
        StringBuilder msg = new StringBuilder(message).append("<br/>");
        if (error != null) {
            StackTraceElement[] trace = error.getStackTrace();
            for (int index = 0; trace != null && index < trace.length; index++) {
                msg.append("&nbsp;&nbsp;&nbsp;&nbsp;").
                    append("at ").append(trace[index].getClassName()).
                    append(".").append(trace[index].getMethodName()).
                    append("(").append(trace[index].getFileName()).
                    append(":").append(trace[index].getLineNumber()).append(")<br/>");
            }
        }
        html.setHTML(msg.toString());
        panel.add(html);
    }
    
    @Override
    public void setHeight(String height) {
        this.currentHeight = height;
        super.setWidth(height);
    }
    
    public String getSavedHeight() {
        return savedHeight;
    }
    
    public void saveHeight() {
        this.savedHeight = this.currentHeight;
    }
}
