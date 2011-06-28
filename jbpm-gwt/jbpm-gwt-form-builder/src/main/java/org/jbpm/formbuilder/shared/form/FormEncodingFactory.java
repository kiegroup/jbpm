package org.jbpm.formbuilder.shared.form;

import org.jbpm.formbuilder.client.form.FormRepresentationEncoderClient;

public final class FormEncodingFactory {

    private static final FormRepresentationEncoder ENCODER = new FormRepresentationEncoderClient();
    private static final FormRepresentationDecoder DECODER = new FormRepresentationDecoderClient();
    
    public static FormRepresentationEncoder getEncoder() {
        return ENCODER;
    }
    
    public static FormRepresentationDecoder getDecoder() {
        return DECODER;
    }
}
