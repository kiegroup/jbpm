package org.jbpm.formbuilder.shared.form;

import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.shared.menu.MenuItemDescription;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

public interface FormRepresentationDecoder {

    FormRepresentation decode(String json) throws FormEncodingException;
    
    FormItemRepresentation decodeItem(String json) throws FormEncodingException;
    
    Object decode(Map<String, Object> data) throws FormEncodingException;

	Map<String, List<MenuItemDescription>> decodeMenuItemsMap(String json) throws FormEncodingException;
    
}
