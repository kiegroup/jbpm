package org.jbpm.formbuilder.client.menu;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;

public class EffectsPopupPanel extends PopupPanel {

    private List<FBFormEffect> effects = new ArrayList<FBFormEffect>();
    
    public EffectsPopupPanel(final FBFormItem item, boolean autoHide) {
        super(autoHide);
        MenuBar bar = new MenuBar(true);
        this.effects = item.getFormEffects();
        for (final FBFormEffect effect : effects) {
            if (effect.isValidForItem(item)) {
                bar.addItem(new MenuItem(
                    effect.getName(), 
                    new Command() {
                        public void execute() {
                            PopupPanel popup = effect.createPanel();
                            if (popup != null) {
                                popup.setPopupPosition(getPopupLeft(), getPopupTop() + 30);
                                popup.show();
                            } else {
                                hide();
                            }
                            effect.apply(item);
                        }
                    })
                );
            }
        }
        add(bar);
    }
}
