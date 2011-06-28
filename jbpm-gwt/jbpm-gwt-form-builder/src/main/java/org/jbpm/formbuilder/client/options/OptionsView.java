package org.jbpm.formbuilder.client.options;

import java.util.List;

import org.jbpm.formbuilder.client.command.BaseCommand;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.SimplePanel;

public class OptionsView extends SimplePanel {

    private final MenuBar bar = new MenuBar(false);
    
    public OptionsView() {
        super();
        setSize("1000px", "30px");
        setStyleName("claseCSSDeMariela");
        Grid grid = new Grid(1,1);
        grid.setSize("100%", "100%");
        grid.setBorderWidth(2);
        grid.setWidget(0, 0, bar);
        add(grid);
    }

    public void addItems(List<MainMenuOption> options) {
        toMenuBar(this.bar, options);
    }

    public void addItem(MainMenuOption option) {
        toMenuBar(this.bar, option);
    }

    protected MenuBar toMenuBar(MenuBar popup, List<MainMenuOption> menu) {
        for (MainMenuOption option : menu) {
            toMenuBar(popup, option);
        }
        return popup;
    }
    
    protected void toMenuBar(MenuBar popup, MainMenuOption option) {
        String html = option.getHtml();
        BaseCommand cmd = option.getCommand();
        List<MainMenuOption> subMenu = option.getSubMenu();
        MenuItem item = null;
        if (cmd == null && subMenu != null && !subMenu.isEmpty()) {
            item = popup.addItem(new SafeHtmlBuilder().appendHtmlConstant(html).toSafeHtml(), toMenuBar(new MenuBar(true), subMenu));
        } else if (cmd != null && (subMenu == null || subMenu.isEmpty())) {
            item = popup.addItem(new SafeHtmlBuilder().appendHtmlConstant(html).toSafeHtml(), cmd);
            cmd.setItem(item);
        }
        if (item != null && !option.isEnabled()) {
            item.setEnabled(false);
        }
    }
}
