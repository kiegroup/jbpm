package org.jbpm.formbuilder.client.form;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormRepresentationEncoder;
import org.jbpm.formbuilder.shared.rep.FBScript;
import org.jbpm.formbuilder.shared.rep.FBValidation;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.rep.InputData;
import org.jbpm.formbuilder.shared.rep.OutputData;

public class FormRepresentationEncoderClient implements FormRepresentationEncoder {

    public String encode(FormRepresentation form) throws FormEncodingException {
        StringBuilder builder = new StringBuilder();
        builder.append("form {\n");
        builder.append("  \"name\": \"").append(form.getName()).append("\",\n");
        builder.append("  \"action\": \"").append(form.getAction()).append("\",\n");
        builder.append("  \"taskId\": \"").append(form.getTaskId()).append("\",\n");
        builder.append("  \"documentation\": \"").append(form.getDocumentation()).append("\",\n");
        builder.append("  \"enctype\": \"").append(form.getEnctype()).append("\",\n");
        builder.append("  \"lastModified\": \"").append(form.getLastModified()).append("\",\n");
        builder.append("  \"method\": \"").append(form.getMethod()).append("\",\n");
        builder.append("  \"formItems\": ").append(encodeItems(form.getFormItems())).append(",\n");
        builder.append("  \"formValidations\": ").append(encodeValidations(form.getFormValidations())).append(",\n");
        builder.append("  \"inputs\": ").append(encodeInputs(form.getInputs())).append(",\n");
        builder.append("  \"outputs\": ").append(encodeOutputs(form.getOutputs())).append(",\n");
        builder.append("  \"onLoadScripts\": ").append(encodeScripts(form.getOnLoadScripts())).append(",\n");
        builder.append("  \"onSubmitScripts\": ").append(encodeScripts(form.getOnSubmitScripts())).append(",\n");
        builder.append("}\n");
        return builder.toString();
    }

    public String encodeInputs(Map<String, InputData> inputs) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        if (inputs != null) {
            Iterator<Map.Entry<String, InputData>> iter = inputs.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry<String, InputData> input = iter.next();
                builder.append("'").append(input.getKey()).append("': ");
                builder.append(input.getValue().getJsonCode());
                if (iter.hasNext()) {
                    builder.append(", \n");
                }
            }
        }
        return builder.append("}").toString();
    }
    
    public String encodeOutputs(Map<String, OutputData> inputs) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        if (inputs != null) {
            Iterator<Map.Entry<String, OutputData>> iter = inputs.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry<String, OutputData> output = iter.next();
                builder.append("'").append(output.getKey()).append("': ");
                builder.append(output.getValue().getJsonCode());
                if (iter.hasNext()) {
                    builder.append(", \n");
                }
            }
        }
        return builder.append("}").toString();
    }
    

    public String encodeItems(List<FormItemRepresentation> formItems) throws FormEncodingException {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        if (formItems != null) {
            for (Iterator<FormItemRepresentation> iter = formItems.iterator(); iter.hasNext(); ) {
                FormItemRepresentation item = iter.next();
                builder.append(item.getJsonCode());
                if (iter.hasNext()) {
                    builder.append(",");
                }
                builder.append("\n");
            }
            builder.append("]\n");
        }
        return builder.toString();
    }
    
    public String encodeValidations(List<FBValidation> formValidations) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        if (formValidations != null) {
            for (Iterator<FBValidation> iter = formValidations.iterator(); iter.hasNext(); ) {
                FBValidation validation = iter.next();
                builder.append(validation.getJsonCode());
                if (iter.hasNext()) {
                    builder.append(",");
                }
                builder.append("\n");
            }
            builder.append("]\n");
        }
        return builder.toString();
    }
    
    public String encodeScripts(List<FBScript> scripts) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        if (scripts != null) {
            for (Iterator<FBScript> iter = scripts.iterator(); iter.hasNext(); ) {
                FBScript script = iter.next();
                builder.append(script.getJsonCode());
                if (iter.hasNext()) {
                    builder.append(",");
                }
                builder.append("\n");
            }
            builder.append("]\n");
        }
        return builder.toString();
    }
}
