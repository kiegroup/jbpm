package org.jbpm.formbuilder.client.toolbar;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;

public interface ToolBarView {

    ToolRegistration addButton(ImageResource button, String name, ClickHandler clickHandler);

    ToolRegistration addMessage(String label, String value);

}
