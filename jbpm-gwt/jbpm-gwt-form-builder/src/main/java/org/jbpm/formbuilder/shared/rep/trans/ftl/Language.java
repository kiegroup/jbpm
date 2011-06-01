package org.jbpm.formbuilder.shared.rep.trans.ftl;

import java.util.List;

import org.jbpm.formbuilder.shared.rep.FBScript;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.rep.InputData;
import org.jbpm.formbuilder.shared.rep.OutputData;
import org.jbpm.formbuilder.shared.rep.items.ComboBoxRepresentation;
import org.jbpm.formbuilder.shared.rep.items.CompleteButtonRepresentation;
import org.jbpm.formbuilder.shared.rep.items.HeaderRepresentation;
import org.jbpm.formbuilder.shared.rep.items.HorizontalPanelRepresentation;
import org.jbpm.formbuilder.shared.rep.items.LabelRepresentation;
import org.jbpm.formbuilder.shared.rep.items.OptionRepresentation;
import org.jbpm.formbuilder.shared.rep.items.PasswordFieldRepresentation;
import org.jbpm.formbuilder.shared.rep.items.TableRepresentation;
import org.jbpm.formbuilder.shared.rep.items.TextFieldRepresentation;
import org.jbpm.formbuilder.shared.rep.trans.LanguageException;
import org.jbpm.formbuilder.shared.rep.trans.LanguageFactory;

public class Language implements org.jbpm.formbuilder.shared.rep.trans.Language {

    /*
     * ftl implementation
     */
    public String form(FormRepresentation form) throws LanguageException {
        StringBuilder builder = new StringBuilder();
        
        StringBuilder invokeOnLoad = new StringBuilder();
        List<FBScript> onLoadScripts = form.getOnLoadScripts();
        if (onLoadScripts != null && !onLoadScripts.isEmpty()) {
            for (FBScript loadScript : onLoadScripts) {
                if (LanguageFactory.getInstance().isClientSide(loadScript.getType())) {
                    builder.append("<script type=\"").append(loadScript.getType()).append("\" ");
                    addParam(builder, "src", loadScript.getSrc());
                    builder.append(">\n");
                    if (loadScript.getContent() != null) {
                        builder.append(loadScript.getContent()).append("\n");
                    }
                    builder.append("</script>\n");
                    invokeOnLoad.append(loadScript.getInvokeFunction()).append("; ");
                } else if (isValidScript(loadScript)) {
                    builder.append(asFtlScript(loadScript));
                }
            }
        }
        
        List<FBScript> onSubmitScripts = form.getOnSubmitScripts();
        StringBuilder invokeOnSubmit = new StringBuilder();
        if (onSubmitScripts != null && !onSubmitScripts.isEmpty()) {
            for (FBScript submitScript : onSubmitScripts) {
                builder.append("<script type=\"").append(submitScript.getType()).append("\" ");
                addParam(builder, "src", submitScript.getSrc());
                builder.append(">\n");
                if (submitScript.getContent() != null) {
                    builder.append(submitScript.getContent()).append("\n");
                }
                builder.append("</script>\n");
                invokeOnSubmit.append(submitScript.getInvokeFunction()).append("; ");
            }
        }
        
        //TODO List<FBValidation> validations = form.getFormValidations();
        //TODO Map<String, InputData> inputs = form.getInputs();
        //TODO Map<String, OutputData> outputs = form.getOutputs();
        
        builder.append("<form ");
        if (invokeOnSubmit.length() > 0) {
            addParam(builder, "onsubmit", invokeOnSubmit.toString());
        }
        if (invokeOnLoad.length() > 0) {
            addParam(builder, "onload", invokeOnLoad.toString());
        }
        addParam(builder, "name", form.getName());
        addParam(builder, "action", form.getAction());
        addParam(builder, "method", form.getMethod());
        addParam(builder, "enctype", form.getEnctype());
        builder.append(">\n");
        String taskId = form.getTaskId();
        builder.append("<#-- taskId: ").append(taskId).append(" -->");
        
        List<FormItemRepresentation> items = form.getFormItems();
        for (FormItemRepresentation item : items) {
            builder.append(item.translate("ftl"));
        }
        
        builder.append("</form>\n");
        
        return builder.toString();
    }

    public String textField(TextFieldRepresentation textField) throws LanguageException {
        StringBuilder builder = new StringBuilder();
        
        //TODO textField.getItemValidations();

        String name = textField.getName();
        OutputData output = textField.getOutput();
        if (name == null || "".equals(name)) {
            name = output.getName();
        }
        builder.append("<input type=\"text\" name=\"").append(name).append("\" ");
        
        Integer maxLength = textField.getMaxLength();
        if (maxLength != null && maxLength > 0) {
            builder.append("maxlength=\"").append(maxLength).append("\" ");
        }
        
        String defaultValue = textField.getDefaultValue();
        InputData input = textField.getInput();
        if (defaultValue == null || "".equals(defaultValue)) {
            if (input != null && input.getName() != null && !"".equals(input.getName())) {
                defaultValue = "${" + input.getName() + "}";
            }
        }
        addParam(builder, "value", defaultValue);
        addParam(builder, "id", textField.getId());
        builder.append("/>\n");
        return builder.toString();
    }

    public String passwordField(PasswordFieldRepresentation passwordField) throws LanguageException {
        StringBuilder builder = new StringBuilder();
        
        //TODO textField.getItemValidations();

        String name = passwordField.getName();
        OutputData output = passwordField.getOutput();
        if (name == null || "".equals(name)) {
            name = output.getName();
        }
        builder.append("<input type=\"password\" name=\"").append(name).append("\" ");
        
        Integer maxLength = passwordField.getMaxLength();
        if (maxLength != null && maxLength > 0) {
            builder.append("maxlength=\"").append(maxLength).append("\" ");
        }
        
        String defaultValue = passwordField.getDefaultValue();
        InputData input = passwordField.getInput();
        if (defaultValue == null || "".equals(defaultValue)) {
            if (input != null && input.getName() != null && !"".equals(input.getName())) {
                defaultValue = "${" + input.getName() + "}";
            }
        }
        addParam(builder, "value", defaultValue);
        addParam(builder, "id", passwordField.getId());
        builder.append("/>\n");
        return builder.toString();
    }
    
    private void addParam(StringBuilder builder, String paramName, String id) {
        if (id != null && !"".equals(id)) {
            builder.append(paramName).append("=\"").append(id).append("\" ");
        }
    }

    public String label(LabelRepresentation label) throws LanguageException {
        StringBuilder builder = new StringBuilder();
        //TODO label.getItemValidations();
        builder.append("<span ");
        addParam(builder, "class", label.getCssName());
        String width = label.getWidth();
        StringBuilder cssStyle = new StringBuilder();
        if (width != null && !"".equals(width)) {
            cssStyle.append("width: ").append(width).append("; ");
        }
        String height = label.getHeight();
        if (height != null && !"".equals(height)) {
            cssStyle.append("height: ").append(height).append("; ");
        }
        if (cssStyle.length() > 0) {
            addParam(builder, "style", cssStyle.toString());
        }
        builder.append(">");
        String value = label.getValue();
        if (value == null || "".equals(value)) {
            value = "${" + label.getInput().getName() + "}";
        }
        builder.append(value).append("</span>\n");
        return builder.toString();
    }

    public String header(HeaderRepresentation header) throws LanguageException {
        StringBuilder builder = new StringBuilder();
        //TODO label.getItemValidations();
        builder.append("<h1 ");
        addParam(builder, "class", header.getCssName());
        String width = header.getWidth();
        StringBuilder cssStyle = new StringBuilder();
        if (width != null && !"".equals(width)) {
            cssStyle.append("width: ").append(width).append("; ");
        }
        String height = header.getHeight();
        if (height != null && !"".equals(height)) {
            cssStyle.append("height: ").append(height).append("; ");
        }
        if (cssStyle.length() > 0) {
            addParam(builder, "style", cssStyle.toString());
        }
        builder.append(">");
        String value = header.getValue();
        if (value == null || "".equals(value)) {
            value = "${" + header.getInput().getName() + "}";
        }
        builder.append(value).append("</h1>\n");
        return builder.toString();
    }

    public String option(OptionRepresentation option) throws LanguageException {
        StringBuilder builder = new StringBuilder();
        builder.append("<option ");
        addParam(builder, "value", option.getValue());
        builder.append(">").append(option.getLabel()).append("</option>\n");
        return builder.toString();
    }
    
    public String comboBox(ComboBoxRepresentation comboBox) throws LanguageException {
        StringBuilder builder = new StringBuilder();
        builder.append("<select ");
        addParam(builder, "id", comboBox.getId());
        String name = comboBox.getName();
        if ((name == null || "".equals(name)) && comboBox.getInput() != null) {
            if (comboBox.getInput().getName() != null && !"".equals(comboBox.getInput().getName())) {
                name = "${" + comboBox.getInput().getName() + "}";
            }
        }
        addParam(builder, "name", name);
        builder.append(">\n");
        if (comboBox.getElementsPopulationScript() != null) {
            FBScript script = comboBox.getElementsPopulationScript();
            if (isValidScript(script)) {
                builder.append(asFtlScript(script));
            }
        } else if (comboBox.getElements() != null && !comboBox.getElements().isEmpty()) {
            for (OptionRepresentation option : comboBox.getElements()) {
                builder.append(option.translate("ftl"));
            }
        }
        builder.append("</select>\n");
        
        //TODO comboBox.getItemValidations();
        //TODO comboBox.getOutput();
        
        return builder.toString();
    }

    private boolean isValidScript(FBScript script) {
        return script.getType().contains("ftl") || script.getType().contains("freemarker");
    }
    
    private String asFtlScript(FBScript script) {
        StringBuilder builder = new StringBuilder();
        if (script.getContent() != null && !"".equals(script.getContent())) {
            builder.append(script.getContent());
        } else if (script.getSrc() != null && !"".equals(script.getSrc())) {
            builder.append("<#include '").append(script.getSrc()).append("'>\n");
        }
        return builder.toString();
    }
    
    public String table(TableRepresentation table) throws LanguageException {
        StringBuilder builder = new StringBuilder();
        //TODO table.getItemValidations();
        builder.append("<table ");
        if (table.getBorderWidth() != null) {
            addParam(builder, "border", table.getBorderWidth().toString());
        }
        if (table.getCellPadding() != null) {
            addParam(builder, "cellpadding", table.getCellPadding().toString());
        }
        if (table.getCellSpacing() != null) {
            addParam(builder, "cellspacing", table.getCellSpacing().toString());
        }
        builder.append(">");
        //TODO table.getInput(); may be used to iterate contents, but not yet
        List<List<FormItemRepresentation>> elements = table.getElements();
        for (List<FormItemRepresentation> row : elements) {
            builder.append("<tr>");
            for (int index = 0; index < row.size(); index++) {
                builder.append("<td>");
                if (row.get(index) != null) {
                    FormItemRepresentation item = row.get(index);
                    builder.append(item.translate("ftl"));
                }
                builder.append("</td>");
            }
            builder.append("</tr>");
        }
        builder.append("</table>");
        return builder.toString();
    }

    public String horizontalPanel(HorizontalPanelRepresentation horizontalPanel) throws LanguageException {
        StringBuilder builder = new StringBuilder();
        //TODO table.getItemValidations();
        builder.append("<table cellpadding=\"0\" ");
        if (horizontalPanel.getSpacing() != null) {
            addParam(builder, "cellspacing", horizontalPanel.getSpacing().toString());
        }
        if (horizontalPanel.getBorderWidth() != null) {
            addParam(builder, "border", horizontalPanel.getBorderWidth().toString());
        }
        addParam(builder, "align", horizontalPanel.getHorizontalAlignment());
        addParam(builder, "valign", horizontalPanel.getVerticalAlignment());
        addParam(builder, "width", horizontalPanel.getWidth());
        addParam(builder, "class", horizontalPanel.getCssClassName());
        addParam(builder, "height", horizontalPanel.getHeight());
        addParam(builder, "id", horizontalPanel.getId());
        builder.append("><tr><td>");
        
        //TODO table.getInput(); may be used to iterate contents, but not yet
        List<FormItemRepresentation> items = horizontalPanel.getItems();
        for (FormItemRepresentation item : items) {
            builder.append(item.translate("ftl"));
        }
        builder.append("</td></tr></table>");
        return builder.toString();
    }
    
    public String completeButton(CompleteButtonRepresentation completeButton) throws LanguageException {
        StringBuilder builder = new StringBuilder();

        //TODO completeButton.getOnClickScript(); should be loaded first
        
        builder.append("<input type=\"submit\" ");
        addParam(builder, "id", completeButton.getId());
        //TODO completeButton.getItemValidations();
        String name = completeButton.getName();
        if (name == null && "".equals(name)) {
            name = completeButton.getOutput().getName();
        }
        addParam(builder, "value", completeButton.getText());
        builder.append("/>");
        return builder.toString();
    }

}
