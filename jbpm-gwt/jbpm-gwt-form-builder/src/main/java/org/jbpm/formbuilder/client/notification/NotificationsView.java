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

import java.util.Collection;

import org.jbpm.formbuilder.client.messages.Constants;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Notifications view. Shows a list of messages with color
 */
public class NotificationsView extends FocusPanel {

    private final Constants i18n = FormBuilderGlobals.getInstance().getI18n();
    
    private VerticalPanel panel = new VerticalPanel();
    private ScrollPanel scroll = new ScrollPanel(panel);
    
    private String currentHeight;
    private String savedHeight;
    
    public NotificationsView() {
        setSize("100%", "60px");
        scroll.setSize("100%", "60px");
        add(scroll);
        panel.add(new HTML("<strong>" + i18n.Notifications() + "</strong>"));
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
            msg.append(stringStackTrace(error));
            if (error instanceof UmbrellaException) {
                Collection<Throwable> causes = ((UmbrellaException) error).getCauses();
                if (causes != null) {
                    for (Throwable cause : causes) {
                        msg.append(stringStackTrace(cause));
                    }
                }
            } 
            if (error.getCause() != null && !error.equals(error.getCause())) {
                error = error.getCause();
                msg.append(i18n.CausedBy());
            } else {
                error = null;
            }
        }
        html.setHTML(msg.toString());
        panel.add(html);
    }

    private String stringStackTrace(Throwable error) {
        StringBuilder msg = new StringBuilder();
        msg.append(error.getClass().getName()).append(": ").append(error.getLocalizedMessage()).append("<br/>");
        StackTraceElement[] trace = error.getStackTrace();
        for (int index = 0; trace != null && index < trace.length; index++) {
            msg.append(i18n.StackTraceLine(trace[index].getClassName(), 
                    trace[index].getMethodName(), 
                    trace[index].getFileName(), 
                    String.valueOf(trace[index].getLineNumber()))).
                append("<br/>");
        }
        return msg.toString();
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
