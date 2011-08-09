package org.jbpm.formbuilder.client.tasks;

import org.jbpm.formbuilder.client.bus.ui.TaskNameFilterEvent;
import org.jbpm.formbuilder.client.messages.Constants;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;

public class SimpleSearchView extends HorizontalPanel {

    private final Constants i18n = FormBuilderGlobals.getInstance().getI18n();
    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    private final TextBox searchQuery = new TextBox();
    private final Button searchButton = new Button(i18n.SearchButton());
    
    public SimpleSearchView() {
        searchQuery.setWidth("150px");
        searchButton.setWidth("70px");
        searchButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                String query = searchQuery.getValue();
                bus.fireEvent(new TaskNameFilterEvent(query));
            }
        });
        add(searchQuery);
        add(searchButton);
    }
}
