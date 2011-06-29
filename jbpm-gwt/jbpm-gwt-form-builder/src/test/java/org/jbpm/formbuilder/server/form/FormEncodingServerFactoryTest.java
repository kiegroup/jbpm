package org.jbpm.formbuilder.server.form;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.jbpm.formbuilder.server.form.FormEncodingServerFactory;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.form.FormRepresentationEncoder;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

public class FormEncodingServerFactoryTest extends TestCase {

    public void testComplexFormDecoding() throws Exception {
        URL url = getClass().getResource("/org/jbpm/formbuilder/shared/form/testComplexFormDecoding.json");
        String json = FileUtils.readFileToString(new File(url.getFile()));
        
        assertNotNull("json shouldn't be null", json);
        assertNotSame("json shouldn't be empty", "", json);
        
        FormRepresentationEncoder encoder = FormEncodingServerFactory.getEncoder();
        FormRepresentationDecoder decoder = FormEncodingServerFactory.getDecoder();
        
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
