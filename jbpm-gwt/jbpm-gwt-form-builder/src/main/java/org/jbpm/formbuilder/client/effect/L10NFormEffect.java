package org.jbpm.formbuilder.client.effect;

import org.jbpm.formbuilder.client.effect.view.L10NEffectView;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.I18NFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.user.client.ui.PopupPanel;
import com.gwtent.reflection.client.Reflectable;

@Reflectable
public class L10NFormEffect extends FBFormEffect {

    private String savedFormat = null;
    
    public L10NFormEffect() {
        super(FormBuilderGlobals.getInstance().getI18n().ApplyLocaleFormattingLabel(), true);
    }
    
    @Override
    public void createStyles() {
        I18NFormItem.Format format = I18NFormItem.Format.valueOf(savedFormat);
        I18NFormItem item = (I18NFormItem) getItem();
        item.setFormat(format);
    }

    @Override
    public PopupPanel createPanel() {
        return new L10NEffectView(this);
    }
    
    @Override
    public boolean isValidForItem(FBFormItem item) {
        return super.isValidForItem(item) && item instanceof I18NFormItem;
    }
    
    public String getSelectedFormat() {
        I18NFormItem item = (I18NFormItem) getItem();
        I18NFormItem.Format format = item.getFormat();
        return format == null ? null : format.toString();
    }
    
    public void setSelectedFormat(String format) {
        this.savedFormat = format;
    }
}
