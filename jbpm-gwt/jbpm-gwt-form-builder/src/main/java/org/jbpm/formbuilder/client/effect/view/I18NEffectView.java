package org.jbpm.formbuilder.client.effect.view;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.client.effect.I18NFormEffect;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class I18NEffectView extends PopupPanel {

    private final Grid grid = new Grid(3, 2);
    private final TextBox defaultText;
    private final I18NFormEffect effect;
    
    public I18NEffectView(I18NFormEffect effect) {
        this.effect = effect;
        defaultText = messageTextBox(effect.getItemI18nMap().get("default"));
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.add(grid);
        populateGrid();
        HorizontalPanel buttonPanel = new HorizontalPanel();
        Button addLocaleButton = new Button("Add locale", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addLocaleToGrid("", "");
            }
        });
        Button doneButton = new Button("Done", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Map<String, String> i18nMap = new HashMap<String, String>();
                i18nMap.put("default", defaultText.getValue());
                for (int row = 2; row < grid.getRowCount(); row++) {
                    TextBox keyBox = (TextBox) grid.getWidget(row, 0);
                    TextBox valueBox = (TextBox) grid.getWidget(row, 1);
                    i18nMap.put(keyBox.getValue(), valueBox.getValue());
                }
                I18NEffectView.this.effect.setItemI18NMap(i18nMap);
                I18NEffectView.this.effect.createStyles();
            }
        });
        Button cancelButton = new Button("Cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        buttonPanel.add(addLocaleButton);
        buttonPanel.add(doneButton);
        buttonPanel.add(cancelButton);
        buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        mainPanel.add(buttonPanel);
        add(mainPanel);
    }
    
    private void populateGrid() {
        grid.setWidget(0, 0, new Label("default:"));
        grid.setWidget(0, 1, defaultText);
        grid.setWidget(1, 0, new Label("Locales"));
        grid.setWidget(1, 1, new Label("Messages"));
        for (Map.Entry<String, String> entry : effect.getItemI18nMap().entrySet()) {
            if (!"default".equals(entry.getKey())) {
                addLocaleToGrid(entry.getKey(), entry.getValue());
            }
        }
    }

    private void addLocaleToGrid(String localeName, String localeMessage) {
        grid.resizeRows(grid.getRowCount() + 1);
        int rowNumber = grid.getRowCount() - 1;
        grid.setWidget(rowNumber, 0, messageTextBox(localeName));
        grid.setWidget(rowNumber, 1, messageTextBox(localeMessage));
        grid.setWidget(rowNumber, 2, removeButton(rowNumber));
    }

    private Button removeButton(final int rowNumber) {
        Image img = new Image(FormBuilderResources.INSTANCE.removeSmallIcon());
        SafeHtmlBuilder builder = new SafeHtmlBuilder().appendHtmlConstant(img.toString());
        ClickHandler handler = new ClickHandler() {
           @Override
           public void onClick(ClickEvent event) {
               grid.removeRow(rowNumber);
           }
        };
        return new Button(builder.toSafeHtml(), handler);
    }
    
    private TextBox messageTextBox(String value) {
        TextBox textBox = new TextBox();
        if (value != null) {
            textBox.setValue(value);
        }
        return textBox;
    }
}
