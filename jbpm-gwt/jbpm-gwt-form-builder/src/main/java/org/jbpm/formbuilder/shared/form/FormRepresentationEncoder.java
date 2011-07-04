package org.jbpm.formbuilder.shared.form;

import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.shared.menu.MenuItemDescription;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

public interface FormRepresentationEncoder {

    String encode(FormRepresentation form) throws FormEncodingException;
    
    String encode(FormItemRepresentation item) throws FormEncodingException;

    String encodeMenuItemsMap(Map<String, List<MenuItemDescription>> items) throws FormEncodingException;
}
