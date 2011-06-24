package org.jbpm.formbuilder.server.trans;

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
    
    String translateForm(FormRepresentation form) throws LanguageException;
    
    String translateItem(FormItemRepresentation item) throws LanguageException;
    
}
