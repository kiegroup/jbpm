package org.jbpm.formbuilder.client.tasks;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;

public class SimpleSearchView extends HorizontalPanel {

    private final TextBox searchQuery = new TextBox();
    private final Button searchButton = new Button("Search");
    
    public SimpleSearchView() {
        searchQuery.setWidth("150px");
        searchButton.setWidth("70px");
        //searchButton.addClickHandler(handler) TODO
        add(searchQuery);
        add(searchButton);
    }
}
