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

import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.FormBuilderService;
import org.jbpm.formbuilder.client.bus.GetFormRepresentationEvent;
import org.jbpm.formbuilder.client.bus.GetFormRepresentationResponseEvent;
import org.jbpm.formbuilder.client.bus.GetFormRepresentationResponseHandler;
import org.jbpm.formbuilder.client.bus.PreviewFormResponseEvent;
import org.jbpm.formbuilder.client.bus.PreviewFormResponseHandler;
import org.jbpm.formbuilder.client.bus.ui.NotificationEvent;
import org.jbpm.formbuilder.client.bus.ui.NotificationEvent.Level;
import org.jbpm.formbuilder.client.messages.I18NConstants;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.api.FormRepresentation;
import org.jbpm.formbuilder.shared.api.InputData;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Preview form as a given language base class.
 */
public abstract class PreviewFormCommand implements BaseCommand {

    private final I18NConstants i18n = FormBuilderGlobals.getInstance().getI18n();
    protected final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    private final FormBuilderService server = FormBuilderGlobals.getInstance().getService();
    private final String saveType;
    
    public PreviewFormCommand(final String saveType) {
        this.saveType = saveType;
        this.bus.addHandler(GetFormRepresentationResponseEvent.TYPE, new GetFormRepresentationResponseHandler() {
            @Override
            public void onEvent(GetFormRepresentationResponseEvent event) {
                FormRepresentation form = event.getRepresentation();
                String type = event.getSaveType();
                if (saveType.equals(type)) {
                    popupInputMapPanel(form);
                }
            }
        });
        this.bus.addHandler(PreviewFormResponseEvent.TYPE, new PreviewFormResponseHandler() {
            @Override
            public void onServerResponse(PreviewFormResponseEvent event) {
                refreshPopup(event.getUrl());
            }
        });
    }
    
    @Override
    public void setItem(MenuItem item) {
        /* not implemented */
    }
    
    @Override
    public void setEmbeded(String profile) {
        // shouldn't be disabled on embeded if it doesn't save
    }
    
    @Override
    public void execute() {
        this.bus.fireEvent(new GetFormRepresentationEvent(this.saveType));
    }

    protected void refreshPopup(String url) {
        PopupPanel panel = new PopupPanel(true);
        Frame content = new Frame(url);
        panel.setWidget(content);
        int height = RootPanel.getBodyElement().getClientHeight();
        int width = RootPanel.getBodyElement().getClientWidth();
        int left = RootPanel.getBodyElement().getAbsoluteLeft();
        int top = RootPanel.getBodyElement().getAbsoluteTop();
        panel.setPixelSize(width - 200, height - 200);
        content.setPixelSize(width - 200, height - 200);
        panel.setPopupPosition(left + 100, top + 100);
        panel.show();
    }

    public void popupInputMapPanel(final FormRepresentation form) {
        Map<String, InputData> inputs = form.getInputs();
        if (inputs == null || inputs.isEmpty()) {
            saveForm(form, null);
        } else {
            final InputMapPanel popup = new InputMapPanel(inputs);
            popup.addOkHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    saveForm(form, popup.getInputs());
                }
            });
            int height = RootPanel.getBodyElement().getClientHeight();
            int width = RootPanel.getBodyElement().getClientWidth();
            popup.setPopupPosition((width / 2) - 150, (height / 2) - 150);
            popup.show();
        }
    }
    
    public void saveForm(FormRepresentation form, Map<String, Object> inputMap) {
        try {
            server.generateForm(form, this.saveType, inputMap);
        } catch (FormBuilderException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.UnexpectedWhilePreviewForm(this.saveType), e)); 
        }
    }
}
