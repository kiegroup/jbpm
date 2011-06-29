package org.jbpm.formbuilder.shared.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.rep.InputData;
import org.jbpm.formbuilder.shared.rep.OutputData;
import org.jbpm.formbuilder.shared.rep.items.CompleteButtonRepresentation;
import org.jbpm.formbuilder.shared.rep.items.HeaderRepresentation;
import org.jbpm.formbuilder.shared.rep.items.LabelRepresentation;
import org.jbpm.formbuilder.shared.rep.items.TableRepresentation;
import org.jbpm.formbuilder.shared.rep.items.TextFieldRepresentation;

public class FormRepresentationDecoderClientTest extends TestCase {

    public void testComplexFormDecoding() throws Exception {
        FormRepresentation form = new FormRepresentation();
        form.setAction("complete");
        form.setDocumentation("This is documentation");
        form.setEnctype("multipart/form-data");
        form.setLastModified(System.currentTimeMillis());
        form.setMethod("POST");
        form.setName("myForm");
        form.setTaskId("myTask");
        List<FormItemRepresentation> formItems = new ArrayList<FormItemRepresentation>();
        HeaderRepresentation header = new HeaderRepresentation();
        header.setValue("Login Form Template");
        formItems.add(header);
        TableRepresentation table = new TableRepresentation(3, 2);
        table.setBorderWidth(1);
        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setHeight("200px");
        table.setWidth("100%");
        LabelRepresentation userLabel = new LabelRepresentation();
        userLabel.setValue("User");
        LabelRepresentation passwordLabel = new LabelRepresentation();
        userLabel.setValue("Password");
        TextFieldRepresentation textField = new TextFieldRepresentation();
        textField.setWidth("160px");
        textField.setHeight("21px");
        textField.setName("usr");
        TextFieldRepresentation passField = new TextFieldRepresentation();
        passField.setWidth("160px");
        passField.setHeight("21px");
        passField.setName("pwd");
        CompleteButtonRepresentation completeButton = new CompleteButtonRepresentation();
        completeButton.setHeight("25px");
        completeButton.setWidth("140px");
        completeButton.setText("Login");
        table.setElement(0, 0, userLabel);
        table.setElement(0, 1, textField);
        table.setElement(1, 0, passwordLabel);
        table.setElement(1, 1, passField);
        table.setElement(2, 1, completeButton);
        formItems.add(table);
        form.setFormItems(formItems);
        Map<String, InputData> inputs = new HashMap<String, InputData>();
        InputData input1 = new InputData();
        input1.setName("in1");
        input1.setValue("${process.dataX}");
        InputData input2 = new InputData();
        input2.setName("in2");
        input2.setValue("${process.dataY}");
        inputs.put("in1", input1);
        inputs.put("in2", input2);
        form.setInputs(inputs);
        Map<String, OutputData> outputs = new HashMap<String, OutputData>();
        OutputData output1 = new OutputData();
        output1.setValue("${pwd}");
        output1.setName("process.dataZ");
        outputs.put("process.dataZ", output1);
        form.setOutputs(outputs);
        
        FormRepresentationEncoder encoder = FormEncodingFactory.getEncoder();
        String json = encoder.encode(form);
        assertNotNull("json shouldn't be null", json);
        assertNotSame("json shouldn't be empty", "", json);

        System.out.println(json);
        
        FormRepresentationDecoder decoder = FormEncodingFactory.getDecoder();
        FormRepresentation form2 = decoder.decode(json);
        assertNotNull("form2 shouldn't be null", form2);
        assertEquals("both forms should be the same in contents", form, form2);
        
    }
}
