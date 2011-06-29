package org.jbpm.formbuilder.client.form;

import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.form.FormRepresentationEncoder;

public final class FormEncodingClientFactory {

    private static final FormRepresentationEncoder ENCODER = new FormRepresentationEncoderClient();
    private static final FormRepresentationDecoder DECODER = new FormRepresentationDecoderClient();
    
    public static FormRepresentationEncoder getEncoder() {
        return ENCODER;
    }
    
    public static FormRepresentationDecoder getDecoder() {
        return DECODER;
    }
}
