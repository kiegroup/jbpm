package org.jbpm.formbuilder.client.options;

import java.util.List;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.SimplePanel;

public class OptionsView extends SimplePanel {

    private final MenuBar bar = new MenuBar(false);
    
    public OptionsView() {
        super();
        setSize("778px", "30px");
        Grid grid = new Grid(1,1);
        grid.setSize("100%", "100%");
        grid.setBorderWidth(2);
        grid.setWidget(0, 0, bar);
        add(grid);
    }

    public void addItems(List<MainMenuOption> options) {
        toMenuBar(this.bar, options);
    }
    
    protected MenuBar toMenuBar(MenuBar popup, List<MainMenuOption> menu) {
        for (MainMenuOption option : menu) {
            String html = option.getHtml();
            Command cmd = option.getCommand();
            List<MainMenuOption> subMenu = option.getSubMenu();
            if (cmd == null && subMenu != null && !subMenu.isEmpty()) {
                popup.addItem(new SafeHtmlBuilder().appendHtmlConstant(html).toSafeHtml(), toMenuBar(new MenuBar(true), subMenu));
            } else if (cmd != null && (subMenu == null || subMenu.isEmpty())) {
                popup.addItem(new SafeHtmlBuilder().appendHtmlConstant(html).toSafeHtml(), cmd);
            }
        }
        return popup;
    }
    
}
