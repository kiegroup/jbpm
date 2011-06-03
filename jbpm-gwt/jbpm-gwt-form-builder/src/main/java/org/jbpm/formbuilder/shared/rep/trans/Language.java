package org.jbpm.formbuilder.shared.rep.trans;

import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.rep.items.AbsolutePanelRepresentation;
import org.jbpm.formbuilder.shared.rep.items.CheckBoxRepresentation;
import org.jbpm.formbuilder.shared.rep.items.ComboBoxRepresentation;
import org.jbpm.formbuilder.shared.rep.items.CompleteButtonRepresentation;
import org.jbpm.formbuilder.shared.rep.items.FileInputRepresentation;
import org.jbpm.formbuilder.shared.rep.items.HTMLRepresentation;
import org.jbpm.formbuilder.shared.rep.items.HeaderRepresentation;
import org.jbpm.formbuilder.shared.rep.items.HiddenRepresentation;
import org.jbpm.formbuilder.shared.rep.items.HorizontalPanelRepresentation;
import org.jbpm.formbuilder.shared.rep.items.ImageRepresentation;
import org.jbpm.formbuilder.shared.rep.items.LabelRepresentation;
import org.jbpm.formbuilder.shared.rep.items.OptionRepresentation;
import org.jbpm.formbuilder.shared.rep.items.PasswordFieldRepresentation;
import org.jbpm.formbuilder.shared.rep.items.RadioButtonRepresentation;
import org.jbpm.formbuilder.shared.rep.items.TableRepresentation;
import org.jbpm.formbuilder.shared.rep.items.TextAreaRepresentation;
import org.jbpm.formbuilder.shared.rep.items.TextFieldRepresentation;

public interface Language {

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

    String absolutePanel(AbsolutePanelRepresentation absolutePanel) throws LanguageException;
}
