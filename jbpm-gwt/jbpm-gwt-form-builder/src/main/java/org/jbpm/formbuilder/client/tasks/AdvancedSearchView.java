package org.jbpm.formbuilder.client.tasks;

import org.jbpm.formbuilder.client.bus.ui.TaskNameFilterEvent;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
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
    
    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    public AdvancedSearchView() {
        super(3, 2);
        queryType.addItem("");
        queryType.addItem("bpmn2 IO references", "bpmn2");
        queryType.addItem("file IO references", "file");
        setWidget(0, 0, new Label("Query:"));
        setWidget(0, 1, queryName);
        setWidget(1, 0, new Label("Type"));
        setWidget(1, 1, queryType);
        setWidget(2, 0, new HTML("&nbsp;"));
        searchButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                String query = queryName.getValue();
                if (queryType.getSelectedIndex() > 0) {
                    query += " iotype:" + queryType.getValue(queryType.getSelectedIndex());
                }
                bus.fireEvent(new TaskNameFilterEvent(query));
            }
        });
        setWidget(2, 1, searchButton);
    }
}
