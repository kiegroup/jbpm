package org.jbpm.formbuilder.client.layout;

import org.jbpm.formbuilder.client.form.FBForm;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;

public interface LayoutView extends IsWidget {

    FBForm getFormDisplay();

    HasWidgets getUnderlyingLayout(Integer x, Integer y);
}
