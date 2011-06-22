package org.jbpm.formbuilder.client.effect.view;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jbpm.formbuilder.client.validation.FBValidationItem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ValidationEditionPanel extends VerticalPanel {
    
    private final Grid editionGrid = new Grid(1, 1);
    private final Map<String, HasValue<String>> validationProperties = new HashMap<String, HasValue<String>>();
    private final Button okButton = new Button("Ok");
    
    private FBValidationItem currentValidation = null;
    
    public ValidationEditionPanel() {
        add(editionGrid);
        HorizontalPanel hPanel = new HorizontalPanel();
        Button resetButton = new Button("Reset");
        resetButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                setVisible(false);
            }
        });
        hPanel.add(resetButton);
        okButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                setVisible(false);
            }
        });
        hPanel.add(okButton);
        add(hPanel);
    }
    
    public void onCommitEdition(ClickHandler handler) {
        okButton.addClickHandler(handler);
    }
    
    public void setCurrentValidation(FBValidationItem newValidation) {
        this.currentValidation = newValidation;
        Widget display = newValidation.createDisplay();
        if (display == null) {
            Map<String, HasValue<String>> newValidationProperties = newValidation.getPropertiesMap();
            int propertiesSize = (newValidationProperties == null ? 0 : newValidationProperties.size());
            editionGrid.clear();
            editionGrid.resize(propertiesSize, 3);
            validationProperties.clear();
            if (newValidationProperties != null) {
                Iterator<Map.Entry<String, HasValue<String>>> iter = newValidationProperties.entrySet().iterator(); 
                for (int index = 0; iter.hasNext(); index++) {
                    Map.Entry<String, HasValue<String>> entry = iter.next();
                    editionGrid.setWidget(index, 0, new Label(entry.getKey() + ": "));
                    editionGrid.setWidget(index, 1, new HTML("&nbsp;&nbsp;&nbsp;"));
                    validationProperties.put(entry.getKey(), entry.getValue());
                    editionGrid.setWidget(index, 2, (Widget) entry.getValue());
                }
            }
        } else {
            editionGrid.clear();
            editionGrid.resize(1, 1);
            editionGrid.setWidget(0, 0, display );
        }
        setVisible(true);
    }
    
    public FBValidationItem getCurrentValidation() {
        return currentValidation;
    }
}
