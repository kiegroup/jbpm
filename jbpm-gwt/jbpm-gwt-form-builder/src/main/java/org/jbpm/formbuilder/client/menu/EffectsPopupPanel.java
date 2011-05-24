package org.jbpm.formbuilder.client.menu;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.formbuilder.client.bus.FormItemSelectionEvent;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;

public class EffectsPopupPanel extends PopupPanel {

    private List<FBFormEffect> effects = new ArrayList<FBFormEffect>();
    
    public EffectsPopupPanel(final FBFormItem item, boolean autoHide) {
        super(autoHide);
        MenuBar bar = new MenuBar(false);
        bar.addItem(new MenuItem(
                new SafeHtmlBuilder().appendHtmlConstant(
                        new Image(FormBuilderResources.INSTANCE.doneIcon()).toString()).toSafeHtml(), 
                new Command() {
                    public void execute() {
                        item.fireSelectionEvent(new FormItemSelectionEvent(item, false));
                        EffectsPopupPanel.this.hide();
                    }
                })
        );
        bar.addItem(new MenuItem(
                new SafeHtmlBuilder().appendHtmlConstant(
                       new Image(FormBuilderResources.INSTANCE.removeIcon()).toString()).toSafeHtml(),
                new Command() {
                    public void execute() {
                        item.fireSelectionEvent(new FormItemSelectionEvent(item, false));
                        EffectsPopupPanel.this.hide();
                        item.removeFromParent();
                    }
                })
        );
        for (final FBFormEffect effect : effects) {
            bar.addItem(new MenuItem(effect.getImage().toString(), new Command() {
                public void execute() {
                    effect.apply(item);
                }
            }));
        }
        add(bar);
    }
}
