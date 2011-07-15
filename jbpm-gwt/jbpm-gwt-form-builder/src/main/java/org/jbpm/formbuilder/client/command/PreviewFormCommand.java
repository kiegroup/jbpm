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
package org.jbpm.formbuilder.client.command;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.FormBuilderService;
import org.jbpm.formbuilder.client.bus.GetFormRepresentationEvent;
import org.jbpm.formbuilder.client.bus.GetFormRepresentationResponseEvent;
import org.jbpm.formbuilder.client.bus.GetFormRepresentationResponseHandler;
import org.jbpm.formbuilder.client.bus.ui.NotificationEvent;
import org.jbpm.formbuilder.client.bus.ui.NotificationEvent.Level;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Preview form as a given language base class.
 */
public abstract class PreviewFormCommand implements BaseCommand {

    protected final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    private final FormBuilderService server = FormBuilderGlobals.getInstance().getService();
    private final String saveType;
    
    public PreviewFormCommand(final String saveType) {
        this.saveType = saveType;
        this.bus.addHandler(GetFormRepresentationResponseEvent.TYPE, new GetFormRepresentationResponseHandler() {
            public void onEvent(GetFormRepresentationResponseEvent event) {
                FormRepresentation form = event.getRepresentation();
                String type = event.getSaveType();
                if (saveType.equals(type)) {
                    saveForm(form);
                }
            }
        });
    }
    
    public void setItem(MenuItem item) {
        /* not implemented */
    }
    
    public void execute() {
        this.bus.fireEvent(new GetFormRepresentationEvent(this.saveType));
    }

    protected void refreshPopupForURL(String url) {
        // TODO This needs to have a control to edit input values, and even to check for expected outputs 
        PopupPanel panel = new PopupPanel(true);
        Frame frame = new Frame(url);
        int height = RootPanel.getBodyElement().getClientHeight();
        int width = RootPanel.getBodyElement().getClientWidth();
        int left = RootPanel.getBodyElement().getAbsoluteLeft();
        int top = RootPanel.getBodyElement().getAbsoluteTop();
        panel.setPixelSize(width - 200, height - 200);
        frame.setPixelSize(width - 200, height - 200);
        panel.setPopupPosition(left + 100, top + 100);
        panel.setWidget(frame);
        panel.show();
    }

    public void saveForm(FormRepresentation form) {
        try {
            String url = server.generateForm(form, this.saveType);
            refreshPopupForURL(url);
        } catch (FormBuilderException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, 
                    "Unexpected error while previewing " + this.saveType + " form", e));
        }
    }

}
