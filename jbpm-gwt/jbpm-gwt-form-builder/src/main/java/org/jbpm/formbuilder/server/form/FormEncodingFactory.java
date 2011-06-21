package org.jbpm.formbuilder.server.form;

public final class FormEncodingFactory {

    private static final FormRepresentationEncoder ENCODER = new FormRepresentationEncoderImpl();
    private static final FormRepresentationDecoder DECODER = new FormRepresentationDecoderImpl();
    
    public static FormRepresentationEncoder getEncoder() {
        return ENCODER;
    }
    
    public static FormRepresentationDecoder getDecoder() {
        return DECODER;
    }
}
