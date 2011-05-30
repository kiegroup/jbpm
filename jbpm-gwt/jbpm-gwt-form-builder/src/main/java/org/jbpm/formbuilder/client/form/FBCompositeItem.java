package org.jbpm.formbuilder.client.form;

import java.util.List;

public interface FBCompositeItem {

    List<FBFormItem> getItems();

    void setItems(List<FBFormItem> items);

}
