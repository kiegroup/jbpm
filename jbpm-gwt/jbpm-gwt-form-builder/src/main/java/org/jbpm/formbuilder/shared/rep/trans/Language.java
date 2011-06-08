package org.jbpm.formbuilder.shared.rep.trans;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

public interface Language {

    String getLanguage();
    
    String translateForm(FormRepresentation form) throws LanguageException;
    
    String translateItem(FormItemRepresentation item) throws LanguageException;
    
    /*
    String form(FormRepresentation form) throws LanguageException;

    String textField(TextFieldRepresentation textField) throws LanguageException;
    
    String label(LabelRepresentation label) throws LanguageException;

    String table(TableRepresentation table) throws LanguageException;

    String comboBox(ComboBoxRepresentation comboBox) throws LanguageException;

    String completeButton(CompleteButtonRepresentation completeButton) throws LanguageException;

    String header(HeaderRepresentation header) throws LanguageException;

    String option(OptionRepresentation option) throws LanguageException;

    String passwordField(PasswordFieldRepresentation passwordField) throws LanguageException;

    String horizontalPanel(HorizontalPanelRepresentation horizontalPanel) throws LanguageException;

    String textArea(TextAreaRepresentation textArea) throws LanguageException;

    String hidden(HiddenRepresentation hidden) throws LanguageException;

    String checkBox(CheckBoxRepresentation checkBox) throws LanguageException;

    String fileInput(FileInputRepresentation fileInput) throws LanguageException;

    String image(ImageRepresentation image) throws LanguageException;

    String html(HTMLRepresentation html) throws LanguageException;

    String radioButton(RadioButtonRepresentation radioButton) throws LanguageException;

    String absolutePanel(AbsolutePanelRepresentation absolutePanel) throws LanguageException;*/
}
