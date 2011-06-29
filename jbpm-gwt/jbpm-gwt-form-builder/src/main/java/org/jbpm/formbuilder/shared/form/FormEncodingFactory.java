package org.jbpm.formbuilder.shared.form;

import org.jbpm.formbuilder.client.form.FormRepresentationDecoderClient;
import org.jbpm.formbuilder.client.form.FormRepresentationEncoderClient;
import org.jbpm.formbuilder.server.form.FormRepresentationDecoderImpl;

import com.google.gwt.core.client.GWT;

public final class FormEncodingFactory {

    private static final FormRepresentationEncoder ENCODER = new FormRepresentationEncoderClient();
    private static final FormRepresentationDecoder DECODER_CLIENT = new FormRepresentationDecoderClient();
    private static final FormRepresentationDecoder DECODER_SERVER = new FormRepresentationDecoderImpl();
    
    public static FormRepresentationEncoder getEncoder() {
        return ENCODER;
    }
    
    public static FormRepresentationDecoder getDecoder() {
        if (GWT.isClient()) {
            return DECODER_CLIENT;
        } else {
            return DECODER_SERVER;
        }
    }
}
