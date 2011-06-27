package org.jbpm.formbuilder.server.trans;

import java.net.URL;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

/*
 *  form: FormRepresentation
 *  textField: TextFieldRepresentation
 *  label: LabelRepresentation
 *  table: TableRepresentation
 *  comboBox: ComboBoxRepresentation
 *  completeButton: CompleteButtonRepresentation
 *  header: HeaderRepresentation
 *  option: OptionRepresentation
 *  passwordField: PasswordFieldRepresentation
 *  horizontalPanel: HorizontalPanelRepresentation
 *  textArea: TextAreaRepresentation
 *  hidden: HiddenRepresentation
 *  checkBox: CheckBoxRepresentation
 *  fileInput: FileInputRepresentation
 *  image: ImageRepresentation
 *  html: HTMLRepresentation
 *  radioButton: RadioButtonRepresentation
 *  absolutePanel: AbsolutePanelRepresentation
*/
public interface Language {

    String getLanguage();
    
    URL translateForm(FormRepresentation form) throws LanguageException;
    
    Object translateItem(FormItemRepresentation item) throws LanguageException;
    
}
