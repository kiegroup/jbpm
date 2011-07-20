package org.jbpm.formbuilder.client.tasks;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

public class AdvancedSearchView extends Grid {

    private final TextBox queryName = new TextBox();
    private final ListBox queryType = new ListBox();
    private final Button searchButton = new Button("Search");
    
    public AdvancedSearchView() {
        super(3, 2);
        //TODO
        setWidget(0, 0, new Label("Query:"));
        setWidget(0, 1, queryName);
        setWidget(1, 0, new Label("Type"));
        setWidget(1, 1, queryType);
        setWidget(2, 0, new HTML("&nbsp;"));
        setWidget(2, 1, searchButton);
    }
}
