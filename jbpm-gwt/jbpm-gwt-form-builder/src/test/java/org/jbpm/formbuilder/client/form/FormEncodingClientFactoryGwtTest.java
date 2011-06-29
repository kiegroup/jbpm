package org.jbpm.formbuilder.client.form;

import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.form.FormRepresentationEncoder;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

import com.google.gwt.junit.client.GWTTestCase;

public class FormEncodingClientFactoryGwtTest extends GWTTestCase {

    private static final String JSON = "{"+
    "\"name\": \"myForm\","+
    "\"action\": \"complete\","+
    "\"taskId\": \"myTask\","+
    "\"documentation\": \"This is documentation\","+
    "\"enctype\": \"multipart/form-data\","+
    "\"lastModified\": \"1309368553625\","+
    "\"method\": \"POST\","+
    "\"formItems\": ["+
    "    {"+
    "        \"styleClass\": null,"+
    "        \"value\": \"Login Form Template\","+
    "        \"typeId\": \"header\","+
    "        \"input\": null,"+
    "        \"@className\": \"org.jbpm.formbuilder.shared.rep.items.HeaderRepresentation\","+
    "        \"width\": null,"+
    "        \"height\": null,"+
    "        \"output\": null,"+
    "        \"cssId\": null,"+
    "        \"cssName\": null,"+
    "        \"itemValidations\": []"+
    "    },"+
    "    {"+
    "        \"input\": null,"+
    "        \"width\": \"100%\","+
    "        \"rows\": 3,"+
    "        \"output\": null,"+
    "        \"itemValidations\": [],"+
    "        \"columns\": 2,"+
    "        \"@className\": \"org.jbpm.formbuilder.shared.rep.items.TableRepresentation\","+
    "        \"typeId\": \"table\","+
    "        \"cellPadding\": 0,"+
    "        \"height\": \"200px\","+
    "        \"borderWidth\": 1,"+
    "        \"cellSpacing\": 0,"+
    "        \"elements\": ["+
    "            ["+
    "                {"+
    "                    \"value\": \"Password\","+
    "                    \"typeId\": \"label\","+
    "                    \"input\": null,"+
    "                    \"@className\": \"org.jbpm.formbuilder.shared.rep.items.LabelRepresentation\","+
    "                    \"width\": null,"+
    "                    \"height\": null,"+
    "                    \"output\": null,"+
    "                    \"cssName\": null,"+
    "                    \"itemValidations\": [],"+
    "                    \"id\": null"+
    "                },"+
    "                {"+
    "                    \"maxLength\": null,"+
    "                    \"typeId\": \"textField\","+
    "                    \"input\": null,"+
    "                    \"@className\": \"org.jbpm.formbuilder.shared.rep.items.TextFieldRepresentation\","+
    "                    \"width\": \"160px\","+
    "                    \"defaultValue\": null,"+
    "                    \"height\": \"21px\","+
    "                    \"output\": null,"+
    "                    \"itemValidations\": [],"+
    "                    \"name\": \"usr\","+
    "                    \"id\": null"+
    "                }"+
    "            ],"+
    "            ["+
    "                {"+
    "                    \"value\": null,"+
    "                    \"typeId\": \"label\","+
    "                    \"input\": null,"+
    "                    \"@className\": \"org.jbpm.formbuilder.shared.rep.items.LabelRepresentation\","+
    "                    \"width\": null,"+
    "                    \"height\": null,"+
    "                    \"output\": null,"+
    "                    \"cssName\": null,"+
    "                    \"itemValidations\": [],"+
    "                    \"id\": null"+
    "                },"+
    "                {"+
    "                    \"maxLength\": null,"+
    "                    \"typeId\": \"textField\","+
    "                    \"input\": null,"+
    "                    \"@className\": \"org.jbpm.formbuilder.shared.rep.items.TextFieldRepresentation\","+
    "                    \"width\": \"160px\","+
    "                    \"defaultValue\": null,"+
    "                    \"height\": \"21px\","+
    "                    \"output\": null,"+
    "                    \"itemValidations\": [],"+
    "                    \"name\": \"pwd\","+
    "                    \"id\": null"+
    "                }"+
    "            ],"+
    "            ["+
    "                null,"+
    "                {"+
    "                    \"onClickScript\": {"+
    "                        \"type\": \"text/javascript\","+
    "                        \"documentation\": null,"+
    "                        \"content\": \"document.forms[0].submit();\","+
    "                        \"src\": null,"+
    "                        \"invokeFunction\": null,"+
    "                        \"id\": null"+
    "                    },"+
    "                    \"typeId\": \"completeButton\","+
    "                    \"input\": null,"+
    "                    \"@className\": \"org.jbpm.formbuilder.shared.rep.items.CompleteButtonRepresentation\","+
    "                    \"text\": \"Login\","+
    "                    \"width\": \"140px\","+
    "                    \"height\": \"25px\","+
    "                    \"output\": null,"+
    "                    \"itemValidations\": [],"+
    "                    \"name\": null,"+
    "                    \"id\": null"+
    "                }"+
    "            ]"+
    "        ]"+
    "    }"+
    "],"+
    "\"formValidations\": [],"+
    "\"inputs\": {"+
    "    \"in2\": {"+
    "        \"@className\": \"org.jbpm.formbuilder.shared.rep.InputData\","+
    "        \"mimeType\": null,"+
    "        \"value\": \"${process.dataY}\","+
    "        \"name\": \"in2\","+
    "        \"formatter\": null"+
    "    },"+ 
    "    \"in1\": {"+
    "        \"@className\": \"org.jbpm.formbuilder.shared.rep.InputData\","+
    "        \"mimeType\": null,"+
    "        \"value\": \"${process.dataX}\","+
    "        \"name\": \"in1\","+
    "        \"formatter\": null"+
    "    }"+
    "},"+
    "\"outputs\": {"+
    "    \"process.dataZ\": {"+
    "        \"@className\": \"org.jbpm.formbuilder.shared.rep.OutputData\","+
    "        \"mimeType\": null,"+
    "        \"value\": \"${pwd}\","+
    "        \"name\": \"process.dataZ\","+
    "        \"formatter\": null"+
    "    }"+
    "},"+
    "\"onLoadScripts\": [],"+
    "\"onSubmitScripts\": []"+ 
    "}";
    
    @Override
    public String getModuleName() {
        return "org.jbpm.formbuilder.FormBuilder";
    }
    
    public void testComplexFormDecoding() throws Exception {
        String json = JSON;
        assertNotNull("json shouldn't be null", json);
        assertNotSame("json shouldn't be empty", "", json);
        
        FormRepresentationEncoder encoder = FormEncodingClientFactory.getEncoder();
        FormRepresentationDecoder decoder = FormEncodingClientFactory.getDecoder();
        
        FormRepresentation form = decoder.decode(json);
        assertNotNull("form shouldn't be null", form);
        String json2 = encoder.encode(form);
        FormRepresentation form2 = decoder.decode(json2);
        assertNotNull("json2 shouldn't be null", json2);
        assertNotSame("json2 shouldn't be empty", "", json2);
        
        assertNotNull("form2 shouldn't be null", form2);
        assertEquals("both forms should be the same in contents", form, form2);
    }

}
