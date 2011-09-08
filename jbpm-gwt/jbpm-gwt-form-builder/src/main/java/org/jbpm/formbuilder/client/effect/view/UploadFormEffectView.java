/*
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
package org.jbpm.formbuilder.client.effect.view;

import java.util.Iterator;
import java.util.List;

import org.jbpm.formbuilder.client.FormBuilderService;
import org.jbpm.formbuilder.client.bus.ui.NotificationEvent;
import org.jbpm.formbuilder.client.bus.ui.NotificationEvent.Level;
import org.jbpm.formbuilder.client.effect.UploadFormEffect;
import org.jbpm.formbuilder.client.messages.I18NConstants;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class UploadFormEffectView extends PopupPanel {

    private final UploadFormEffect effect;
    private final FileUpload fileInput = new FileUpload();
    private final FormPanel form = new FormPanel();
    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    private final FormBuilderService server = FormBuilderGlobals.getInstance().getService();
    private final I18NConstants i18n = FormBuilderGlobals.getInstance().getI18n();
    
    public UploadFormEffectView(UploadFormEffect formEffect) {
        this.effect = formEffect;
        InputElement.as(fileInput.getElement()).setAccept(toString(this.effect.getAllowedTypes()));        
        VerticalPanel content = new VerticalPanel();
        form.setAction(server.getUploadFileURL());
        form.setMethod(FormPanel.METHOD_POST);
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.addSubmitCompleteHandler(new SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(SubmitCompleteEvent event) {
                String srcUrl = removePre(event.getResults());
                if (srcUrl == null || "".equals(srcUrl)) {
                    bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntUploadFile()));
                } else {
                    effect.setSrcUrl(srcUrl);
                    effect.createStyles();
                }
                RootPanel.get().remove(form);
                hide();
            }
        });
        fileInput.setName("uploadFile");

        HorizontalPanel inputPanel = new HorizontalPanel();
        inputPanel.add(new Label(i18n.SelectAFile()));
        inputPanel.add(fileInput);
        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.add(createConfirmButton());
        buttonsPanel.add(createCancelButton());
        content.add(inputPanel);
        content.add(buttonsPanel);
        form.add(content);
        setWidget(form);
    }

    private String toString(List<String> styles) {
        StringBuilder builder = new StringBuilder();
        if (styles != null) {
            String type = null;
            for (Iterator<String> it = styles.iterator(); it.hasNext(); type = it.next()) {
                builder.append(type);
                if (it.hasNext()) {
                    builder.append(", ");
                }
            }
        }
        return builder.toString();
    }
    
    private String removePre(String srcUrl) {
        if (srcUrl.startsWith("<pre>")) {
            srcUrl = srcUrl.replace("<pre>", "");
        }
        if (srcUrl.startsWith("<PRE>")) {
            srcUrl = srcUrl.replace("<PRE>", "");
        }
        if (srcUrl.endsWith("</pre>")) {
            srcUrl = srcUrl.replace("</pre>", "");
        }
        if (srcUrl.endsWith("</PRE>")) {
            srcUrl = srcUrl.replace("</PRE>", "");
        }
        return srcUrl;
    }

    private Button createCancelButton() {
        Button cancelButton = new Button(i18n.CancelButton(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        return cancelButton;
    }

    private Button createConfirmButton() {
        Button confirmButton = new Button(i18n.ConfirmButton(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                form.submit();
            }
        });
        return confirmButton;
    }

}
