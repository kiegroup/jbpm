/**
 * Copyright 2011 JBoss Inc 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.formbuilder.client.notification;

import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Notifications view. Shows a list of messages with color
 */
public class NotificationsView extends FocusPanel {

    private VerticalPanel panel = new VerticalPanel();
    private ScrollPanel scroll = new ScrollPanel(panel);
    
    private String currentHeight;
    private String savedHeight;
    
    public NotificationsView() {
        setSize("100%", "60px");
        scroll.setSize("100%", "60px");
        add(scroll);
        panel.add(new HTML("<strong>Notifications</strong>"));
        panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
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
        while (error != null) {
            msg.append(error.getClass().getName()).append(": ").append(error.getLocalizedMessage()).append("<br/>");
            StackTraceElement[] trace = error.getStackTrace();
            for (int index = 0; trace != null && index < trace.length; index++) {
                msg.append("&nbsp;&nbsp;&nbsp;&nbsp;").
                    append("at ").append(trace[index].getClassName()).
                    append(".").append(trace[index].getMethodName()).
                    append("(").append(trace[index].getFileName()).
                    append(":").append(trace[index].getLineNumber()).append(")<br/>");
            }
            if (error.getCause() != null && !error.equals(error.getCause())) {
                error = error.getCause();
                msg.append("Caused by: ");
            } else {
                error = null;
            }
        }
        html.setHTML(msg.toString());
        panel.add(html);
    }
    
    public String getSavedHeight() {
        return savedHeight;
    }
    
    @Override
    public void setHeight(String height) {
        super.setHeight(height);
        scroll.setHeight(height);
        this.currentHeight = height;
    }
    
    public void saveHeight() {
        this.savedHeight = this.currentHeight;
    }
}
