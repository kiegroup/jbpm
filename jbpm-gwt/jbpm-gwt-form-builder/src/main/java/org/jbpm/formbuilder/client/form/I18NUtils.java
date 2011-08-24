package org.jbpm.formbuilder.client.form;

import java.util.HashMap;
import java.util.Map;

public class I18NUtils implements I18NFormItem {

    private final Map<String, String> i18nMap = new HashMap<String, String>();
    
    @Override
    public boolean containsLocale(String localeName) {
        return i18nMap.containsKey(localeName);
    }

    @Override
    public void saveI18nMap(Map<String, String> i18nMap) {
        this.i18nMap.clear();
        this.i18nMap.putAll(i18nMap);
    }

    @Override
    public Map<String, String> getI18nMap() {
        return this.i18nMap;
    }

    @Override
    public String getI18n(String key) {
        return this.i18nMap.get(key);
    }

}
