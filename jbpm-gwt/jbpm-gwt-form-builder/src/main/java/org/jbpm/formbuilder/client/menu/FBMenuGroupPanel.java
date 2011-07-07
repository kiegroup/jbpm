package org.jbpm.formbuilder.client.menu;


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class FBMenuGroupPanel extends SimplePanel {

    private final FBMenuPanel menuPanel;
    private final Button expandButton;
    private final Button collapseButton;
    
    public FBMenuGroupPanel(String group, FBMenuPanel menuPanel) {
        this.menuPanel = menuPanel;
        this.expandButton = new Button(group + " (+)");
        this.collapseButton = new Button(group + " (-)");
        setWidth("100%");
        this.expandButton.setWidth("100%");
        this.collapseButton.setWidth("100%");
        this.expandButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                expand();
            }
        });
        this.collapseButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                collapse();
            }
        });
        add(this.expandButton);
    }

    public void collapse() {
        setWidget(expandButton);
    }
    
    public void expand() {
        VerticalPanel vPanel = new VerticalPanel();
        vPanel.setWidth("100%");
        vPanel.add(collapseButton);
        vPanel.add(menuPanel);
        setWidget(vPanel);
    }
    
    public boolean hasWidgets() {
        return menuPanel.getWidgetCount() > 0;
    }
    
    public void add(FBMenuItem menuItem) {
        menuPanel.add(menuItem);
    }
    
    public void remove(FBMenuItem menuItem) {
        menuPanel.fullRemove(menuItem);
    }
    
}
