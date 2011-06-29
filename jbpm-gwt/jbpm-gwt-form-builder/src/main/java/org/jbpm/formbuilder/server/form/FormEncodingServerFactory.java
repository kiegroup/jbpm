package org.jbpm.formbuilder.server.form;

import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.form.FormRepresentationEncoder;

public class FormEncodingServerFactory {

    private static final FormRepresentationEncoder ENCODER = new FormRepresentationEncoderImpl();
    private static final FormRepresentationDecoder DECODER = new FormRepresentationDecoderImpl();
    
    public static FormRepresentationEncoder getEncoder() {
        return ENCODER;
    }
    
    public static FormRepresentationDecoder getDecoder() {
        return DECODER;
    }
}
