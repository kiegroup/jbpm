package org.jbpm.formbuilder.shared.form;


public final class FormEncodingFactory {

    private static FormRepresentationEncoder ENCODER;
    private static FormRepresentationDecoder DECODER;
    
    public static synchronized void register(FormRepresentationEncoder encoder, FormRepresentationDecoder decoder) {
        ENCODER = encoder;
        DECODER = decoder;
    }
    
    public static FormRepresentationEncoder getEncoder() {
        return ENCODER;
    }
    
    public static FormRepresentationDecoder getDecoder() {
        return DECODER;
    }
}
