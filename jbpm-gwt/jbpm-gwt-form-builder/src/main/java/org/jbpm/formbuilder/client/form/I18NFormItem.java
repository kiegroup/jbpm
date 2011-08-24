package org.jbpm.formbuilder.client.form;

import java.util.Map;

public interface I18NFormItem {

    enum Format { CURRENCY, NUMBER, DATE, PERCENT, INTEGER };
    
    void setFormat(Format format);
    
    Format getFormat();
    
    boolean containsLocale(String localeName);
    
    void saveI18nMap(Map<String, String> i18nMap);
    
    Map<String, String> getI18nMap();
    
    String getI18n(String key);
}
