package org.jbpm.formbuilder.client.tasks;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SearchFilterView extends VerticalPanel {

    private final SimpleSearchView simple = new SimpleSearchView();
    private final AdvancedSearchView advanced = new AdvancedSearchView();
    
    private HorizontalPanel tooglePanel = new HorizontalPanel();
    private final Anchor toogleAnchor;
    
    public SearchFilterView() {
        setSize("100%", "90px");
        toogleAnchor = new Anchor("Advanced Search");
        toogleAnchor.setHref("javascript:void(0);");
        toogleAnchor.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (getWidget(0) == simple) {
                    remove(simple);
                    toogleAnchor.setText("Simple Search");
                    insert(advanced, getWidgetIndex(tooglePanel));
                } else {
                    remove(advanced);
                    toogleAnchor.setText("Advanced Search");
                    insert(simple, getWidgetIndex(tooglePanel));
                }
            }
        });
        tooglePanel.setWidth("100%");
        tooglePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        tooglePanel.add(toogleAnchor);
        add(simple);
        add(tooglePanel);
    }
}
