package org.jbpm.formbuilder.client.form;

import com.google.gwt.user.client.ui.SimplePanel;

public abstract class FBInplaceEditor extends SimplePanel {

    public abstract void focus();

    public abstract boolean isFocused();
}
