package org.jbpm.formbuilder.client.form;

import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.effect.FBFormEffect;

public abstract class OptionsFormItem extends FBFormItem {

    public OptionsFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
    }

    public abstract void addItem(String label, String value);
    
    public abstract void deleteItem(String label);
    
    public abstract Map<String, String> getItems();
}
