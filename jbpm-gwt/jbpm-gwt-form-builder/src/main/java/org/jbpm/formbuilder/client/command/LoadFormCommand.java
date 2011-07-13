package org.jbpm.formbuilder.client.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.FormBuilderService;
import org.jbpm.formbuilder.client.bus.LoadServerFormEvent;
import org.jbpm.formbuilder.client.bus.LoadServerFormHandler;
import org.jbpm.formbuilder.client.bus.LoadServerFormResponseEvent;
import org.jbpm.formbuilder.client.bus.LoadServerFormResponseHandler;
import org.jbpm.formbuilder.client.bus.NotificationEvent;
import org.jbpm.formbuilder.client.bus.NotificationEvent.Level;
import org.jbpm.formbuilder.client.bus.ui.UpdateFormViewEvent;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class LoadFormCommand implements BaseCommand {

    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    private final FormBuilderService service = FormBuilderGlobals.getInstance().getService();
    
    public LoadFormCommand() {
        bus.addHandler(LoadServerFormEvent.TYPE, new LoadServerFormHandler() {
            public void onEvent(LoadServerFormEvent event) {
                String formName = event.getFormName();
                if (formName != null) {
                    try {
                        service.getForm(formName);
                    } catch (FormBuilderException e) {
                        bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't load form " + formName, e));
                    }
                } else {
                    try {
                        service.getForms();
                    } catch (FormBuilderException e) {
                        bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't retrieve all forms", e));
                    }
                }
            }
        });
        bus.addHandler(LoadServerFormResponseEvent.TYPE, new LoadServerFormResponseHandler() {
            public void onListForms(LoadServerFormResponseEvent event) {
                popupFormSelection(event.getList());
            }
            
            public void onGetForm(LoadServerFormResponseEvent event) {
                populateFormView(event.getForm());
            }
        });
    }

    private void populateFormView(FormRepresentation form) {
        bus.fireEvent(new UpdateFormViewEvent(form));
    }

    private void popupFormSelection(List<FormRepresentation> forms) {
        final Map<String, FormRepresentation> formMap = new HashMap<String, FormRepresentation>();
        final ListBox names = new ListBox();
        for (FormRepresentation form : forms) {
            names.addItem(form.getName());
            formMap.put(form.getName(), form);
        }
        final PopupPanel panel = new PopupPanel(false, true);
        VerticalPanel vPanel = new VerticalPanel();
        HorizontalPanel selectPanel = new HorizontalPanel();
        selectPanel.add(new Label("Select a Form:"));
        selectPanel.add(names);
        vPanel.add(selectPanel);
        HorizontalPanel buttonPanel = new HorizontalPanel();
        Button loadButton = new Button("Load");
        loadButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                String formName = names.getValue(names.getSelectedIndex());
                populateFormView(formMap.get(formName));
                panel.hide();
            }
        });
        Button cancelButton = new Button("Cancel");
        cancelButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                panel.hide();
            }
        });
        buttonPanel.add(new HTML("&nbsp;"));
        buttonPanel.add(loadButton);
        buttonPanel.add(cancelButton);
        vPanel.add(buttonPanel);
        panel.add(vPanel);
        panel.setPopupPosition(
                RootPanel.getBodyElement().getClientWidth() / 2 - 150, 
                RootPanel.getBodyElement().getClientHeight() / 2 - 150);
        panel.show();
    }
    
    public void execute() {
        bus.fireEvent(new LoadServerFormEvent());
    }

    public void setItem(MenuItem item) {
        item.setEnabled(true);
    }

}
