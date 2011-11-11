/*
k * Copyright 2011 JBoss Inc 
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderGlobals;
import org.jbpm.formbuilder.client.effect.EventHandlingFormEffect;
import org.jbpm.formbuilder.client.effect.scripthandlers.PlainTextScriptHelper;
import org.jbpm.formbuilder.client.messages.I18NConstants;
import org.jbpm.formbuilder.shared.api.FBScript;
import org.jbpm.formbuilder.shared.api.FBScriptHelper;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class EventHandlingEffectView extends PopupPanel {

    private final I18NConstants i18n = FormBuilderGlobals.getInstance().getI18n();
    private final EventHandlingFormEffect effect;
    private final ListBox eventsSelectionCombo = new ListBox();
    private final VerticalPanel mainPanel = new VerticalPanel();
    
    private Map<String, FBScript> eventActions = new HashMap<String, FBScript>();
    private Map<String, FBScriptHelper> eventHelpers = new HashMap<String, FBScriptHelper>();
    
    public EventHandlingEffectView(EventHandlingFormEffect formEffect) {
        this.effect = formEffect;
        populateEventActions();
        mainPanel.add(createEventPanel());
        mainPanel.add(startScriptPanel());
        HorizontalPanel buttonsPanel = createButtonsPanel();
        mainPanel.add(buttonsPanel);
        add(mainPanel);
    }

    private Widget startScriptPanel() {
        String initialEventName = eventsSelectionCombo.getValue(0);
        FBScript initialScript = eventActions.get(initialEventName);
        FBScriptHelper helper = null;
        if (initialScript != null) {
            helper = initialScript.getHelper();
        }
        if (helper == null) {
            helper = new PlainTextScriptHelper();
            helper.setScript(initialScript);
        }
        return helper.draw();
    }

    private HorizontalPanel createButtonsPanel() {
        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.add(createSaveContinueButton());
        buttonsPanel.add(createConfirmButton());
        buttonsPanel.add(createCancelButton());
        return buttonsPanel;
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
                int selectedIndex = eventsSelectionCombo.getSelectedIndex();
                String evtName = eventsSelectionCombo.getValue(selectedIndex);
                FBScriptHelper helper = eventHelpers.get(evtName);
                effect.confirmEventAction(evtName, toScript(helper));
                hide();
            }
        });
        return confirmButton;
    }

    private Button createSaveContinueButton() {
        Button saveContinueButton = new Button("Save & Continue", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                int selectedIndex = eventsSelectionCombo.getSelectedIndex();
                String evtName = eventsSelectionCombo.getValue(selectedIndex);
                FBScriptHelper helper = eventHelpers.get(evtName);
                effect.storeEventAction(evtName, toScript(helper));
            }
        });
        return saveContinueButton;
    }
    
    private FBScript toScript(FBScriptHelper helper) {
        FBScript script = new FBScript();
        if (helper != null) {
            script.setContent(helper.asScriptContent());
            script.setHelper(helper);
        }
        script.setType("text/javascript");
        return script;
    }

    private Grid createEventPanel() {
        Grid eventPanel = new Grid(2, 2);
        eventPanel.setWidget(0, 0, new Label("Event:"));
        eventPanel.setWidget(0, 1, eventsSelectionCombo);
        eventPanel.setWidget(1, 0, new Label("Type:"));
        eventPanel.setWidget(1, 1, new Label("text/javascript"));
        return eventPanel;
    }

    private void populateEventActions() {
        List<String> possibleEvents = this.effect.getPossibleEvents();
        if (possibleEvents != null) {
            for (String eventName : possibleEvents) {
                eventsSelectionCombo.addItem(eventName);
            }
        }
        Map<String, FBScript> actions = this.effect.getItemActions();
        if (actions != null) {
            this.eventActions.putAll(actions);
        }
        for (Map.Entry<String, FBScript> entry : this.eventActions.entrySet()) {
            FBScript script = entry.getValue();
            FBScriptHelper helper = script == null ? null : script.getHelper();
            String key = entry.getKey();
            this.eventHelpers.put(key, helper);
        }
        eventsSelectionCombo.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                int selectedIndex = eventsSelectionCombo.getSelectedIndex();
                String eventName = eventsSelectionCombo.getValue(selectedIndex);
                FBScript script = eventActions.get(eventName);
                FBScriptHelper helper = null;
                if (script != null) {
                    helper = script.getHelper();
                }
                if (helper == null) {
                    helper = new PlainTextScriptHelper();
                    helper.setScript(script);
                }
                mainPanel.remove(2);
                mainPanel.add(helper.draw());
                //scriptPanel.setValue(script.getContent());
            }
        });
    }
}
