package org.jbpm.formbuilder.client.effect;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.client.effect.view.I18NEffectView;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.I18NFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.user.client.ui.PopupPanel;
import com.gwtent.reflection.client.Reflectable;

@Reflectable
public class I18NFormEffect extends FBFormEffect {

    private Map<String, String> savedMap = null;
    
    public I18NFormEffect() {
        super(FormBuilderGlobals.getInstance().getI18n().InternationalizeEffectLabel(), true);
    }
    
    @Override
    public void createStyles() {
        I18NFormItem item = (I18NFormItem) getItem();
        item.saveI18nMap(savedMap);
    }

    @Override
    public PopupPanel createPanel() {
        return new I18NEffectView(this);
    }

    @Override
    public boolean isValidForItem(FBFormItem item) {
        return super.isValidForItem(item) && item instanceof I18NFormItem;
    }
    
    public Map<String, String> getItemI18nMap() {
        I18NFormItem item = (I18NFormItem) getItem();
        Map<String, String> map = new HashMap<String, String>();
        map.putAll(item.getI18nMap());
        return map;
    }
    
    public void setItemI18NMap(Map<String, String> i18nMap) {
        this.savedMap = i18nMap;
    }
}
